package cz.logicgo.engine.algorithms.sudoku;

import cz.logicgo.core.builders.sudoku.SudokuBuilderBase;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.MultipleSolutionException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifiers;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.ParityType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.algorithms.settings.DifficultyChoosing;
import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics;
import cz.logicgo.engine.algorithms.sudoku.grader.SudokuGrader;
import cz.logicgo.engine.algorithms.sudoku.records.RemovedElement;
import cz.logicgo.engine.algorithms.sudoku.solvers.SudokuSequentialSolver;
import cz.logicgo.engine.algorithms.sudoku.validators.ISudokuValidator;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiSudokuValidator;
import cz.logicgo.engine.context.SudokuFillingContext;
import cz.logicgo.engine.context.SudokuRemoveContext;
import cz.logicgo.engine.context.SudokuSolveContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.*;
import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.*;
import static cz.logicgo.engine.algorithms.settings.DifficultyChoosing.getSudokuCellsCountToRemove;
import static cz.logicgo.engine.algorithms.sudoku.TypeGenerators.*;

public class SudokuGenerator {


    public static void ensureMultiGridBoardAllocated(Sudoku sudoku) {
        if (MultiGridConfig.isMultiDoku(sudoku.getVariant())) {
            MultiGridConfig.GridSetup setup = MultiGridConfig.LAYOUTS.get(sudoku.getVariant());
            if (setup != null) {
                int targetSize = setup.globalSize();
                SudokuCell[][] currentBoard = sudoku.getBoard();
                if (currentBoard == null || currentBoard.length < targetSize) {
                    SudokuCell[][] newBoard = new SudokuCell[targetSize][targetSize];
                    for (int r = 0; r < targetSize; r++) {
                        for (int c = 0; c < targetSize; c++) {
                            if (currentBoard != null && r < currentBoard.length && c < currentBoard[r].length && currentBoard[r][c] != null) {
                                newBoard[r][c] = currentBoard[r][c];
                            } else {
                                newBoard[r][c] = new SudokuCell(r, c, 0);
                            }
                        }
                    }
                    sudoku.setBoard(newBoard);
                }
            }
        }
    }

    public static boolean removeElements(SudokuGame sudokuGame, int numbersElementsToRemove, Stack<RemovedElement> removedElements,
                                         int order, Random random, List<GridCell> priorityRemove,
                                         ConcurrentHashMap<Long, Integer> sharedCache, SudokuRemoveContext context,
                                         StepCounter solveCounter) throws ThreadTerminationException, LimitReachedException {
        if (numbersElementsToRemove == 0) return true;

        context.checkContext();
        StepCounter stepCounter = context.stepCounter();
        Sudoku sudoku = sudokuGame.getSudoku();
        ISudokuValidator validator = sudokuGame.getSudokuValidator();
        int size = validator.getGridSize();

        boolean isMulti = validator instanceof MultiSudokuValidator;

        LinkedHashSet<GridCell> allCandidatesToTry = new LinkedHashSet<>();

        List<GridCell> validPriority = new ArrayList<>();
        if (priorityRemove != null && !priorityRemove.isEmpty()) {
            SudokuCell[][] board = sudoku.getBoard();
            for (GridCell pc : priorityRemove) {
                if (pc.row() < board.length && pc.col() < board[pc.row()].length
                        && board[pc.row()][pc.col()] != null && !sudoku.isZero(pc.row(), pc.col())) {
                    validPriority.add(pc);
                }
            }

            if (isMulti) {
                double clusterChance = switch (sudoku.getDifficulty()) {
                    case EASY -> 0.1;
                    case MEDIUM -> 0.4;
                    case HARD -> 0.7;
                };
                allCandidatesToTry.addAll(roundRobinCells(validPriority, (MultiSudokuValidator) validator, random, clusterChance));
            } else {
                Collections.shuffle(validPriority, random);
                allCandidatesToTry.addAll(validPriority);
            }
        }

        if (!isMulti && !removedElements.isEmpty()) {
            double clusterChance = switch (sudoku.getDifficulty()) {
                case EASY -> 0.0;
                case MEDIUM -> 0.35;
                case HARD -> 0.7;
            };

            if (random.nextDouble() < clusterChance) {
                RemovedElement last = removedElements.peek();
                int lr = last.rowIndex();
                int lc = last.columnIndex();
                List<GridCell> clusterCells = new ArrayList<>();

                int boxSize = (int) Math.sqrt(size);
                if (boxSize * boxSize == size) {
                    int startR = (lr / boxSize) * boxSize;
                    int startC = (lc / boxSize) * boxSize;

                    for (int br = startR; br < startR + boxSize; br++) {
                        for (int bc = startC; bc < startC + boxSize; bc++) {
                            if (!sudoku.isZero(br, bc)) {
                                clusterCells.add(new GridCell(br, bc));
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        if (!sudoku.isZero(lr, i)) clusterCells.add(new GridCell(lr, i));
                        if (!sudoku.isZero(i, lc)) clusterCells.add(new GridCell(i, lc));
                    }
                }

                Collections.shuffle(clusterCells, random);
                allCandidatesToTry.addAll(clusterCells);
            }
        }

        List<GridCell> normalCells = new ArrayList<>();
        SudokuCell[][] board = sudoku.getBoard();
        for (int r = 0; r < size; r++) {
            if (r >= board.length) break;
            for (int c = 0; c < size; c++) {
                if (c >= board[r].length) break;
                if (board[r][c] != null && !sudoku.isZero(r, c)) {
                    GridCell gc = new GridCell(r, c);
                    if (!allCandidatesToTry.contains(gc)) {
                        normalCells.add(gc);
                    }
                }
            }
        }

        if (size >= 8 && (sudoku.getDifficulty() == Difficulty.HARD)) {
            normalCells.sort((c1, c2) -> Integer.compare(
                    calculateProximityScore(c2, sudokuGame, size),
                    calculateProximityScore(c1, sudokuGame, size)
            ));
        }

        double advancedHeuristicChance = switch (sudoku.getDifficulty()) {
            case EASY -> 0.0;
            case MEDIUM -> 0.15;
            case HARD -> 0.45;
        };

        boolean heuristicApplied = false;
        if (!isMulti && random.nextDouble() < advancedHeuristicChance) {
            List<GridCell> patternCells;
            int roll = random.nextInt(5);

            switch (roll) {
                case 0 -> patternCells = findXWingPatternCells(sudoku, normalCells, random);
                case 1 -> patternCells = findSwordfishPatternCells(sudoku, normalCells, random);
                case 2 -> patternCells = findInterBoxRectangleCells(sudoku, normalCells, random);
                case 3 -> patternCells = findGeometricRectangleCells(sudoku, normalCells, random);
                default -> patternCells = findXYWingPatternCells(sudoku, normalCells, random);
            }

            if (!patternCells.isEmpty()) {
                heuristicApplied = true;
                normalCells.removeAll(patternCells);

                if (size < 8) Collections.shuffle(normalCells, random);
                allCandidatesToTry.addAll(normalCells);
                allCandidatesToTry.addAll(patternCells);
            }
        }

        if (!heuristicApplied) {
            if (isMulti) {
                double clusterChance = switch (sudoku.getDifficulty()) {
                    case EASY -> 0.0;
                    case MEDIUM -> 0.4;
                    case HARD -> 0.7;
                };
                allCandidatesToTry.addAll(roundRobinCells(normalCells, (MultiSudokuValidator) validator, random, clusterChance));
            } else {
                if (size < 8 || sudoku.getDifficulty() == Difficulty.EASY) {
                    Collections.shuffle(normalCells, random);
                }
                allCandidatesToTry.addAll(normalCells);
            }
        }

        for (GridCell cell : allCandidatesToTry) {
            int r = cell.row();
            int c = cell.col();

            int element = sudokuGame.removeNumber(r, c);
            stepCounter.increment();

            int candsFound = validator.getCellCandidates(r, c).size();
            boolean isRightDifficulty = true;

            if (!context.ignoreDifficultyHeuristics()) {
                if (numbersElementsToRemove <= 12) {
                    isRightDifficulty = true;
                } else {
                    if (sudoku.getVariant() == SudokuVariant.CLASSIC) {
                        isRightDifficulty = switch (sudoku.getDifficulty()) {
                            case EASY -> candsFound == 1 || random.nextDouble() < 0.2;
                            case MEDIUM ->
                                    candsFound == 2 || (candsFound == 1 && random.nextDouble() < 0.30) || (candsFound >= 3 && random.nextDouble() < 0.15);
                            case HARD -> candsFound >= 3 || random.nextDouble() < 0.20;
                        };
                    } else if (isMulti) {
                        isRightDifficulty = switch (sudoku.getDifficulty()) {
                            case EASY -> candsFound == 1 || random.nextDouble() < 0.5;
                            case MEDIUM -> candsFound == 2 || (candsFound == 1 && random.nextDouble() < 0.35);
                            case HARD -> candsFound >= 3 || random.nextDouble() < 0.40;
                        };
                    } else {
                        isRightDifficulty = switch (sudoku.getDifficulty()) {
                            case EASY -> candsFound == 1 || random.nextDouble() < 0.5;
                            case MEDIUM -> candsFound == 2 || (candsFound == 1 && random.nextDouble() < 0.40);
                            case HARD -> candsFound >= 3 || random.nextDouble() < 0.30;
                        };
                    }
                }
            }

            if (isRightDifficulty) {
                removedElements.push(new RemovedElement(r, c, element, order));

                SudokuGame copy = new SudokuGame(sudokuGame);
                SudokuSolveContext solveContext = new SudokuSolveContext(2, sharedCache);

                try {
                    SudokuSequentialSolver.solvingSequentialStart(copy, solveContext);

                    if (solveCounter != null) {
                        solveCounter.add(solveContext.stepCounter().get());
                    }

                    if (solveContext.getSolutionCount() == 1) {
                        boolean wasPriority = priorityRemove != null && priorityRemove.remove(cell);

                        if (removeElements(sudokuGame, numbersElementsToRemove - 1, removedElements, order + 1, random, priorityRemove, sharedCache, context, solveCounter)) {
                            return true;
                        }

                        context.checkContext();
                        if (wasPriority) priorityRemove.add(cell);
                    }
                } catch (LimitReachedException | MultipleSolutionException ignored) {
                }

                removedElements.pop();
            }
            sudokuGame.setNumber(r, c, element);
        }
        return false;
    }

    private static int getGridSize(Sudoku sudoku) {
        return sudoku.getType().getGridSize();
    }

    private static boolean shareSameSubGrid(Sudoku sudoku, GridCell c1, GridCell c2) {
        if (MultiGridConfig.isMultiDoku(sudoku.getVariant())) {
            var setup = MultiGridConfig.LAYOUTS.get(sudoku.getVariant());
            if (setup != null) {
                for (int[] offset : setup.offsets()) {
                    int rOff = offset[0];
                    int cOff = offset[1];

                    boolean c1InSub = (c1.row() >= rOff && c1.row() < rOff + 9 && c1.col() >= cOff && c1.col() < cOff + 9);
                    boolean c2InSub = (c2.row() >= rOff && c2.row() < rOff + 9 && c2.col() >= cOff && c2.col() < cOff + 9);

                    if (c1InSub && c2InSub) return true;
                }
                return false;
            }
        }
        return true;
    }

    private static boolean[][] createCellPresenceMatrix(List<GridCell> cells, int size) {
        boolean[][] matrix = new boolean[size][size];
        for (GridCell c : cells) {
            if (c.row() < size && c.col() < size) {
                matrix[c.row()][c.col()] = true;
            }
        }
        return matrix;
    }

    private static List<GridCell> findXWingPatternCells(Sudoku sudoku, List<GridCell> availableCells, Random random) {
        List<GridCell> priorityCells = new ArrayList<>();
        SudokuCell[][] board = sudoku.getBoard();
        int size = getGridSize(sudoku);

        boolean[][] availableLookup = createCellPresenceMatrix(availableCells, size);

        List<Integer> digits = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) digits.add(i);
        Collections.shuffle(digits, random);

        for (int targetDigit : digits) {
            List<GridCell> digitCells = new ArrayList<>();
            for (GridCell cell : availableCells) {
                if (board[cell.row()][cell.col()].getValue() == targetDigit) {
                    digitCells.add(cell);
                }
            }

            int count = digitCells.size();
            for (int i = 0; i < count; i++) {
                GridCell c1 = digitCells.get(i);
                for (int j = i + 1; j < count; j++) {
                    GridCell c2 = digitCells.get(j);

                    if (c1.row() != c2.row() && c1.col() != c2.col() && shareSameSubGrid(sudoku, c1, c2)) {
                        int r1 = c1.row();
                        int c1Col = c1.col();
                        int r2 = c2.row();
                        int c2Col = c2.col();

                        if (availableLookup[r1][c2Col] && availableLookup[r2][c1Col]) {
                            priorityCells.add(c1);
                            priorityCells.add(c2);
                            priorityCells.add(new GridCell(r1, c2Col));
                            priorityCells.add(new GridCell(r2, c1Col));
                            return priorityCells;
                        }
                    }
                }
            }
        }
        return priorityCells;
    }

    private static List<GridCell> findSwordfishPatternCells(Sudoku sudoku, List<GridCell> availableCells, Random random) {
        List<GridCell> priorityCells = new ArrayList<>();
        SudokuCell[][] board = sudoku.getBoard();
        int size = getGridSize(sudoku);

        boolean[][] availableLookup = createCellPresenceMatrix(availableCells, size);

        List<Integer> digits = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) digits.add(i);
        Collections.shuffle(digits, random);

        for (int targetDigit : digits) {
            List<GridCell> digitCells = new ArrayList<>();
            for (GridCell cell : availableCells) {
                if (board[cell.row()][cell.col()].getValue() == targetDigit) {
                    digitCells.add(cell);
                }
            }

            int count = digitCells.size();
            if (count >= 3) {
                for (int i = 0; i < count - 2; i++) {
                    GridCell c1 = digitCells.get(i);
                    for (int j = i + 1; j < count - 1; j++) {
                        GridCell c2 = digitCells.get(j);
                        if (c1.row() == c2.row() || c1.col() == c2.col()) continue;

                        for (int k = j + 1; k < count; k++) {
                            GridCell c3 = digitCells.get(k);
                            if (c1.row() == c3.row() || c2.row() == c3.row() ||
                                    c1.col() == c3.col() || c2.col() == c3.col()) continue;

                            if (shareSameSubGrid(sudoku, c1, c2) && shareSameSubGrid(sudoku, c1, c3)) {
                                priorityCells.add(c1);
                                priorityCells.add(c2);
                                priorityCells.add(c3);

                                int[] rows = {c1.row(), c2.row(), c3.row()};
                                int[] cols = {c1.col(), c2.col(), c3.col()};

                                for (int r : rows) {
                                    for (int c : cols) {
                                        if (availableLookup[r][c]) {
                                            GridCell corner = new GridCell(r, c);
                                            if (!priorityCells.contains(corner)) {
                                                priorityCells.add(corner);
                                            }
                                        }
                                    }
                                }
                                return priorityCells;
                            }
                        }
                    }
                }
            }
        }
        return priorityCells;
    }

    private static List<GridCell> findInterBoxRectangleCells(Sudoku sudoku, List<GridCell> availableCells, Random random) {
        List<GridCell> priorityCells = new ArrayList<>();
        int size = getGridSize(sudoku);
        int boxSize = (int) Math.sqrt(size);
        if (boxSize * boxSize != size) return priorityCells;

        boolean[][] availableLookup = createCellPresenceMatrix(availableCells, size);
        List<GridCell> pool = new ArrayList<>(availableCells);
        Collections.shuffle(pool, random);

        int sampleSize = Math.min(pool.size(), 40);

        for (int i = 0; i < sampleSize; i++) {
            GridCell c1 = pool.get(i);
            for (int j = i + 1; j < sampleSize; j++) {
                GridCell c4 = pool.get(j);
                if (c1.row() != c4.row() && c1.col() != c4.col() && shareSameSubGrid(sudoku, c1, c4)) {

                    int b1 = (c1.row() / boxSize) * boxSize + (c1.col() / boxSize);
                    int b4 = (c4.row() / boxSize) * boxSize + (c4.col() / boxSize);

                    int r1 = c1.row();
                    int c1Col = c1.col();
                    int r4 = c4.row();
                    int c4Col = c4.col();

                    int b2 = (r1 / boxSize) * boxSize + (c4Col / boxSize);
                    int b3 = (r4 / boxSize) * boxSize + (c1Col / boxSize);

                    if ((b1 == b2 && b3 == b4 && b1 != b3) || (b1 == b3 && b2 == b4 && b1 != b2)) {
                        if (availableLookup[r1][c4Col] && availableLookup[r4][c1Col]) {
                            priorityCells.add(c1);
                            priorityCells.add(new GridCell(r1, c4Col));
                            priorityCells.add(new GridCell(r4, c1Col));
                            priorityCells.add(c4);
                            return priorityCells;
                        }
                    }
                }
            }
        }
        return priorityCells;
    }

    private static List<GridCell> findGeometricRectangleCells(Sudoku sudoku, List<GridCell> availableCells, Random random) {
        List<GridCell> priorityCells = new ArrayList<>();
        int size = getGridSize(sudoku);
        boolean[][] availableLookup = createCellPresenceMatrix(availableCells, size);

        List<GridCell> pool = new ArrayList<>(availableCells);
        Collections.shuffle(pool, random);

        int count = Math.min(pool.size(), 30);
        for (int i = 0; i < count; i++) {
            GridCell c1 = pool.get(i);
            for (int j = i + 1; j < count; j++) {
                GridCell c4 = pool.get(j);

                if (c1.row() != c4.row() && c1.col() != c4.col() && shareSameSubGrid(sudoku, c1, c4)) {
                    int r1 = c1.row();
                    int c1Col = c1.col();
                    int r4 = c4.row();
                    int c4Col = c4.col();

                    if (availableLookup[r1][c4Col] && availableLookup[r4][c1Col]) {
                        priorityCells.add(c1);
                        priorityCells.add(new GridCell(r1, c4Col));
                        priorityCells.add(new GridCell(r4, c1Col));
                        priorityCells.add(c4);

                        if (priorityCells.size() >= 8) {
                            return priorityCells;
                        }
                    }
                }
            }
        }
        return priorityCells;
    }

    private static List<GridCell> findXYWingPatternCells(Sudoku sudoku, List<GridCell> availableCells, Random random) {
        List<GridCell> priorityCells = new ArrayList<>();
        int size = getGridSize(sudoku);
        int boxSize = (int) Math.sqrt(size);
        boolean[][] availableLookup = createCellPresenceMatrix(availableCells, size);

        List<GridCell> pool = new ArrayList<>(availableCells);
        Collections.shuffle(pool, random);

        int sampleLimit = Math.min(pool.size(), 35);

        for (int i = 0; i < sampleLimit; i++) {
            GridCell pivot = pool.get(i);
            GridCell pincer1 = null;
            GridCell pincer2 = null;

            for (GridCell c : pool) {
                if (c.equals(pivot)) continue;
                if ((c.row() == pivot.row() || c.col() == pivot.col()) && shareSameSubGrid(sudoku, pivot, c)) {
                    pincer1 = c;
                    break;
                }
            }

            if (pincer1 == null) continue;

            for (GridCell c : pool) {
                if (c.equals(pivot) || c.equals(pincer1)) continue;
                boolean seesPivotInBox = (boxSize > 0) && (c.row() / boxSize == pivot.row() / boxSize) && (c.col() / boxSize == pivot.col() / boxSize);
                boolean seesPincer1InBox = (boxSize > 0) && (c.row() / boxSize == pincer1.row() / boxSize) && (c.col() / boxSize == pincer1.col() / boxSize);

                boolean seesPivot = c.row() == pivot.row() || c.col() == pivot.col() || seesPivotInBox;
                boolean seesPincer1 = c.row() == pincer1.row() || c.col() == pincer1.col() || seesPincer1InBox;

                if (seesPivot && !seesPincer1 && shareSameSubGrid(sudoku, pivot, c)) {
                    pincer2 = c;
                    break;
                }
            }

            if (pincer2 != null) {
                priorityCells.add(pivot);
                priorityCells.add(pincer1);
                priorityCells.add(pincer2);

                int interR = pincer1.row();
                int interC = pincer2.col();
                if (interR < size && interC < size && availableLookup[interR][interC]) {
                    priorityCells.add(new GridCell(interR, interC));
                }
                return priorityCells;
            }
        }
        return priorityCells;
    }

    private static List<GridCell> roundRobinCells(List<GridCell> cells, MultiSudokuValidator multiValidator, Random random, double clusterChance) {
        int subGridCount = multiValidator.getSubGrids().size();
        List<List<GridCell>> buckets = new ArrayList<>(subGridCount);
        for (int i = 0; i < subGridCount; i++) {
            buckets.add(new ArrayList<>());
        }

        List<GridCell> intersectionCells = new ArrayList<>();
        Collections.shuffle(cells, random);

        for (GridCell gc : cells) {
            List<Integer> validBuckets = new ArrayList<>();
            for (int i = 0; i < subGridCount; i++) {
                if (multiValidator.getSubGrids().get(i).containsGlobal(gc.row(), gc.col())) {
                    validBuckets.add(i);
                }
            }

            if (validBuckets.size() > 1) {
                intersectionCells.add(gc);
            } else if (!validBuckets.isEmpty()) {
                buckets.get(validBuckets.getFirst()).add(gc);
            }
        }

        List<GridCell> result = new ArrayList<>(intersectionCells);

        boolean added = true;
        while (added) {
            added = false;
            List<List<GridCell>> activeBuckets = new ArrayList<>(buckets);
            Collections.shuffle(activeBuckets, random);

            for (List<GridCell> bucket : activeBuckets) {
                if (!bucket.isEmpty()) {
                    result.add(bucket.removeLast());
                    added = true;

                    int extraCells = 0;
                    int maxExtraCells = 8;

                    while (extraCells < maxExtraCells && !bucket.isEmpty() && random.nextDouble() < clusterChance) {
                        result.add(bucket.removeLast());
                        extraCells++;
                    }
                }
            }
        }
        return result;
    }

    public static SudokuGame fillSudokuBoardRecursive(Sudoku sudoku) throws ThreadTerminationException, LimitReachedException {
        StepCounter stepCounter = new StepCounter();
        GenerationStatistics.SudokuKey key = GenerationStatistics.constructKey(sudoku);
        Long limit = GenerationStatistics.getSudokuGenLimit(key);

        stepCounter.setLimit(Objects.requireNonNullElse(limit, Long.MAX_VALUE));
        return fillSudokuBoardRecursive(sudoku, stepCounter);
    }

    public static SudokuGame fillSudokuBoardRecursive(Sudoku sudoku, Long limit) throws ThreadTerminationException, LimitReachedException {
        StepCounter stepCounter = new StepCounter();
        stepCounter.setLimit(Objects.requireNonNullElse(limit, Long.MAX_VALUE));
        return fillSudokuBoardRecursive(sudoku, stepCounter);
    }

    public static SudokuGame fillMultiSudokuBoard(Sudoku sudoku) throws ThreadTerminationException, LimitReachedException {
        ensureMultiGridBoardAllocated(sudoku);
        StepCounter stepCounter = new StepCounter();
        GenerationStatistics.SudokuKey key = GenerationStatistics.constructKey(sudoku);
        Long limit = GenerationStatistics.getSudokuGenLimit(key);
        if (limit != null) {
            stepCounter.setLimit(limit);
        }
        return fillMultiSudokuBoard(sudoku, stepCounter);
    }

    public static SudokuGame fillMultiSudokuBoard(Sudoku sudoku, StepCounter stepCounter) throws ThreadTerminationException, LimitReachedException {
        ensureMultiGridBoardAllocated(sudoku);
        SudokuGame sudokuGame = new SudokuGame(sudoku, true);
        MultiSudokuValidator validator = (MultiSudokuValidator) sudokuGame.getSudokuValidator();
        List<GridCell> fillPath = buildMultiFillPath(validator);
        Random random = sudokuGame.getSudoku().getRandomInstance();
        fillMultiSudokuBoard(0, fillPath, sudokuGame, random, stepCounter);

        clearCellsOutsideSubGrids(sudokuGame.getSudoku());

        return sudokuGame;
    }

    public static boolean fillMultiSudokuBoard(int pathIndex, List<GridCell> fillPath, SudokuGame sudokuGame, Random random) {
        return fillMultiSudokuBoard(pathIndex, fillPath, sudokuGame, random, null);
    }

    public static boolean fillMultiSudokuBoard(int pathIndex, List<GridCell> fillPath, SudokuGame sudokuGame, Random random, StepCounter stepCounter) {
        if (pathIndex == fillPath.size()) return true;

        ISudokuValidator validator = sudokuGame.getSudokuValidator();
        GridCell currentCell = fillPath.get(pathIndex);
        int row = currentCell.row();
        int col = currentCell.col();

        ArrayList<Integer> candidatesList = validator.getCellCandidates(row, col);
        int candSize = candidatesList.size();
        int[] candidates = new int[candSize];
        for (int i = 0; i < candSize; i++) {
            candidates[i] = candidatesList.get(i);
        }

        SudokuGenerator.shuffleFY(candidates, candidates.length, random);

        for (int number : candidates) {
            if (stepCounter != null) stepCounter.increment();

            if (validator.isValidMove(row, col, number)) {
                sudokuGame.setNumber(row, col, number);
                if (stepCounter != null) stepCounter.increment();

                if (fillMultiSudokuBoard(pathIndex + 1, fillPath, sudokuGame, random, stepCounter)) {
                    return true;
                }

                sudokuGame.removeNumber(row, col);
                if (stepCounter != null) stepCounter.increment();
            }
        }
        return false;
    }

    public static List<GridCell> buildMultiFillPath(MultiSudokuValidator validator) {
        List<GridCell> path = new ArrayList<>();
        for (var targetCell : validator.getEmptyCells()) {
            path.add(new GridCell(targetCell.getRow(), targetCell.getCol()));
        }
        return path;
    }

    public static void validateAllCellsFilled(SudokuCell[] cells) {
        for (SudokuCell cell : cells) {
            if (cell.getValue() == 0) {
                throw new IllegalStateException("Empty cell found at position: " + cell.getRow() + "," + cell.getCol());
            }
        }
    }

    public static SudokuGame fillSudokuBoardRecursive(Sudoku sudoku, StepCounter stepCounter) throws ThreadTerminationException, LimitReachedException {
        SudokuGame sudokuGame = initAfter(sudoku.getVariant())
                ? new SudokuGame(sudoku, true, false)
                : new SudokuGame(sudoku, true);

        int gridSize = sudoku.getType().getGridSize();
        int fullMask = createFullMask(gridSize);

        int[] rowMasks = new int[gridSize];
        Arrays.fill(rowMasks, fullMask);

        List<GridCell> fillPath = generateOptimizedFillPath(sudoku);
        SudokuFillingContext fillingContext = new SudokuFillingContext(stepCounter);

        fillSudokuBoard(0, fillPath, rowMasks, sudokuGame, fillingContext);

        validateAllCellsFilled(flattenBoard(sudokuGame.getSudoku().getBoard()));
        return sudokuGame;
    }

    private static List<GridCell> generateOptimizedFillPath(Sudoku sudoku) {
        int gridSize = sudoku.getType().getGridSize();
        List<GridCell> path = new ArrayList<>();
        int[][] cellScores = new int[gridSize][gridSize];

        switch (sudoku.getVariant()) {
            case DIAGONAL -> {
                for (int i = 0; i < gridSize; i++) {
                    cellScores[i][i] += 10;
                    cellScores[i][gridSize - 1 - i] += 10;
                }
            }
            case OFFSET, PATTERNED -> {
                var pattern = sudoku.getPattern();
                if (pattern != null) {
                    int max = pattern.getIndexCount();
                    int startIndex = pattern.isSelective() ? 1 : 0;
                    int baseScore = (max + 2) * 10;

                    for (int i = startIndex; i <= max; i++) {
                        int currentScore = baseScore - (i * 10);
                        for (GridCell cell : pattern.getCellsBelongToSubgrid(i)) {
                            cellScores[cell.row()][cell.col()] += currentScore;
                        }
                    }
                }
            }
        }

        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                path.add(new GridCell(r, c));
            }
        }

        path.sort((c1, c2) -> Integer.compare(
                cellScores[c2.row()][c2.col()],
                cellScores[c1.row()][c1.col()]
        ));

        return path;
    }

    public static int createFullMask(int gridSize) {
        return (1 << (gridSize + 1)) - 2;
    }

    public static boolean isNumberAvailable(int mask, int number) {
        return (mask & (1 << number)) != 0;
    }

    public static int removeNumberFromMask(int mask, int number) {
        return mask & ~(1 << number);
    }

    public static int addNumberToMask(int mask, int number) {
        return mask | (1 << number);
    }

    public static void shuffleFY(int[] candidates, int candidateCount, Random random) {
        for (int i = candidateCount - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = candidates[i];
            candidates[i] = candidates[j];
            candidates[j] = temp;
        }
    }

    private static int gatherCandidates(int rowRemainingMask, int gridSize, int[] candidates) {
        int candidateCount = 0;
        for (int i = 1; i <= gridSize; i++) {
            if (isNumberAvailable(rowRemainingMask, i)) {
                candidates[candidateCount++] = i;
            }
        }
        return candidateCount;
    }

    private static boolean fillSudokuBoard(int pathIndex, List<GridCell> fillPath, int[] rowMasks, SudokuGame sudokuGame, SudokuFillingContext context) throws ThreadTerminationException, LimitReachedException {
        Sudoku sudoku = sudokuGame.getSudoku();
        Random random = sudoku.getRandomInstance();
        int gridSize = sudoku.getType().getGridSize();

        if (pathIndex == fillPath.size()) return true;

        context.checkContext();
        StepCounter stepCounter = context.stepCounter();
        ISudokuValidator validator = sudokuGame.getSudokuValidator();

        GridCell currentCell = fillPath.get(pathIndex);
        int rowIndex = currentCell.row();
        int columnIndex = currentCell.col();

        int[] candidates = new int[gridSize];
        int candidateCount = gatherCandidates(rowMasks[rowIndex], gridSize, candidates);

        shuffleFY(candidates, candidateCount, random);

        for (int c = 0; c < candidateCount; c++) {
            int number = candidates[c];
            stepCounter.increment();

            if (validator.isValidMove(rowIndex, columnIndex, number)) {
                sudokuGame.setNumber(rowIndex, columnIndex, number);
                stepCounter.increment();

                int oldMask = rowMasks[rowIndex];
                rowMasks[rowIndex] = removeNumberFromMask(oldMask, number);

                if (fillSudokuBoard(pathIndex + 1, fillPath, rowMasks, sudokuGame, context)) {
                    return true;
                }

                sudokuGame.removeNumber(rowIndex, columnIndex);
                rowMasks[rowIndex] = oldMask;
                stepCounter.increment();
            }
        }
        return false;
    }

    public static Difficulty getDifficultyLabel(int numericScore) {
        return switch (numericScore) {
            case 99 -> null;
            case int i when i <= 4 -> Difficulty.EASY;
            case int i when i <= 6 -> Difficulty.MEDIUM;
            default -> Difficulty.HARD;
        };
    }

    public static long[] generateAndMeasureSteps(Sudoku sudoku) {
        long genSteps = 0;
        long removeSteps = 0;
        long solveSteps = 0;

        SudokuVariant variant = sudoku.getVariant();
        ensureMultiGridBoardAllocated(sudoku);

        StepCounter genCounter = new StepCounter();

        try {
            if (MultiGridConfig.isMultiDoku(variant)) {
                fillMultiSudokuBoard(sudoku, genCounter);
            } else {
                fillSudokuBoardRecursive(sudoku, genCounter);
            }
            genSteps = genCounter.get();
        } catch (ThreadTerminationException | LimitReachedException e) {
            return new long[]{-1, -1, -1};
        }

        sudoku.setSolutionBoard(getBoardCopy(sudoku.getBoard()));
        List<GridCell> priorityDelete = initVariantsAfter(sudoku);

        int removeCount = DifficultyChoosing.getMaxSudokuCellsToRemove(sudoku.getType(), sudoku.getVariant(), sudoku.getDifficulty());

        SudokuGame gameToCreate = initAfter(variant) ? new SudokuGame(sudoku, true, true) : new SudokuGame(sudoku, true);
        Random random = gameToCreate.getSudoku().getRandomInstance();
        Stack<RemovedElement> removedElements = new Stack<>();
        ConcurrentHashMap<Long, Integer> sharedCache = new ConcurrentHashMap<>();

        StepCounter removeCounter = new StepCounter();
        StepCounter solverCounter = new StepCounter();
        SudokuRemoveContext context = new SudokuRemoveContext(removeCounter, false);

        boolean success = false;
        try {
            if (!sudoku.isSymmetric()) {
                List<GridCell> priorityCopy = new ArrayList<>(priorityDelete);
                success = removeElements(gameToCreate, removeCount, removedElements, 0, random, priorityCopy, sharedCache, context, solverCounter);
            }
        } catch (ThreadTerminationException | LimitReachedException ignored) {
        }

        if (success) {
            removeSteps = removeCounter.get();
            solveSteps = solverCounter.get();
            return new long[]{genSteps, removeSteps, solveSteps};
        }

        return new long[]{-1, -1, -1};
    }

    private static int calculateProximityScore(GridCell cell, SudokuGame game, int gridSize) {
        int score = 0;
        int r = cell.row();
        int c = cell.col();
        int boxSize = (int) Math.sqrt(gridSize);

        SudokuCell[][] board = game.getSudoku().getBoard();

        for (int i = 0; i < gridSize; i++) {
            if (r < board.length && i < board[r].length && board[r][i] != null && !game.getSudoku().isZero(r, i)) score += 2;
            if (i < board.length && c < board[i].length && board[i][c] != null && !game.getSudoku().isZero(i, c)) score += 2;
        }
        if (boxSize * boxSize == gridSize) {
            int startR = (r / boxSize) * boxSize;
            int startC = (c / boxSize) * boxSize;
            for (int br = startR; br < startR + boxSize; br++) {
                for (int bc = startC; bc < startC + boxSize; bc++) {
                    if (br < board.length && bc < board[br].length && board[br][bc] != null && !game.getSudoku().isZero(br, bc)) score += 3;
                }
            }
        }

        return score;
    }

    public static Sudoku createSudoku(SudokuBuilderBase<?> sudokuCreation) {
        Sudoku sudoku = new Sudoku(sudokuCreation);
        ensureMultiGridBoardAllocated(sudoku);

        SudokuGame filledGame;

        try {
            if (MultiGridConfig.isMultiDoku(sudokuCreation.getSudokuVariant())) {
                filledGame = fillMultiSudokuBoard(sudoku);
            } else {
                filledGame = fillSudokuBoardRecursive(sudoku);
            }
        } catch (Exception e) {
            return null;
        }

        sudoku.setSolutionBoard(getBoardCopy(sudoku.getBoard()));
        List<GridCell> priorityDelete = initVariantsAfter(sudoku);

        Difficulty targetDifficulty = sudoku.getDifficulty();
        var variant = sudoku.getVariant();
        int removeCount = getSudokuCellsCountToRemove(sudoku.getType(), variant, targetDifficulty, sudoku.getSeed());

        SudokuGame gameToCreate = initAfter(sudoku.getVariant()) ? new SudokuGame(sudoku, true, true) : new SudokuGame(sudoku, true);
        Random random = gameToCreate.getSudoku().getRandomInstance();

        GenerationStatistics.SudokuKey key = GenerationStatistics.constructKey(sudoku);
        Long delLimit = GenerationStatistics.getSudokuDelLimit(key);

        StepCounter solveCounter = new StepCounter();
        Stack<RemovedElement> removedElements = new Stack<>();
        ConcurrentHashMap<Long, Integer> sharedCache = new ConcurrentHashMap<>();

        boolean success = false;

        if (!sudoku.isSymmetric()) {
            List<GridCell> priorityCopy = new ArrayList<>(priorityDelete);

            StepCounter stepCounterRemove = new StepCounter();
            if (delLimit != null) {
                stepCounterRemove.setLimit(delLimit);
            }

            boolean ignoreDiff = (sudoku.getType().getGridSize() >= 15 || MultiGridConfig.isMultiDoku(sudoku.getVariant()));
            SudokuRemoveContext context = new SudokuRemoveContext(stepCounterRemove, ignoreDiff);
            try {
                success = removeElements(gameToCreate, removeCount, removedElements, 0, random, priorityCopy, sharedCache, context, solveCounter);
            } catch (Exception ignored) {
            }
        }

        if (!success) {
            return null;
        }

        sudoku.setStartingBoard(getBoardCopy(sudoku.getBoard()));

        boolean skipGrading = switch (sudoku.getType()) {
            case FOUR, FIVE, SIX, ELEVEN, TWELVE, THIRTEEN, FOURTEEN, FIFTEEN, SIXTEEN -> true;
            default -> false;
        };

        if (MultiGridConfig.isMultiDoku(sudoku.getVariant()) || skipGrading) {
            sudoku.setDifficulty(targetDifficulty);
        } else {
            SudokuGame gameToGrade = new SudokuGame(sudoku, true);
            int gradeSudoku = SudokuGrader.gradeSudoku(gameToGrade);

            sudoku.setGrade(gradeSudoku);
            Difficulty actualDifficulty = getDifficultyLabel(gradeSudoku);

            sudoku.setDifficulty(actualDifficulty != null ? actualDifficulty : targetDifficulty);
        }

        sudoku.setBoard(getBoardCopy(sudoku.getStartingBoard()));
        return sudoku;
    }

    private static void clearCellsOutsideSubGrids(Sudoku sudoku) {
        if (!MultiGridConfig.isMultiDoku(sudoku.getVariant())) {
            return;
        }

        MultiGridConfig.GridSetup setup = MultiGridConfig.LAYOUTS.get(sudoku.getVariant());
        if (setup == null) return;

        SudokuCell[][] board = sudoku.getBoard();
        SudokuCell[][] solution = sudoku.getSolutionBoard();
        SudokuCell[][] starting = sudoku.getStartingBoard();
        int size = setup.globalSize();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                boolean inAnySubGrid = false;
                for (int[] offset : setup.offsets()) {
                    int rOff = offset[0];
                    int cOff = offset[1];
                    if (r >= rOff && r < rOff + 9 && c >= cOff && c < cOff + 9) {
                        inAnySubGrid = true;
                        break;
                    }
                }

                if (!inAnySubGrid) {
                    if (board != null && r < board.length && c < board[r].length) {
                        board[r][c] = null;
                    }
                    if (solution != null && r < solution.length && c < solution[r].length) {
                        solution[r][c] = null;
                    }
                    if (starting != null && r < starting.length && c < starting[r].length) {
                        starting[r][c] = null;
                    }
                }
            }
        }
    }

    public static List<GridCell> initVariantsAfter(Sudoku sudoku) {
        Set<GridCell> priorityDelete = new LinkedHashSet<>();
        SudokuModifiers modifiers = sudoku.getModifiers();
        int size = sudoku.getType().getGridSize();

        switch (sudoku.getVariant()) {
            case GREATER_THAN -> {
                GreaterThanModifier clues = deriveGreaterThanConstraints(sudoku);
                modifiers.setGreaterThan(clues);
            }
            case CONSECUTIVE -> {
                ConsecutiveModifier clues = deriveConsecutiveConstraints(sudoku);
                modifiers.setConsecutive(clues);
                for (Pair<GridCell, GridCell> pair : clues.cells()) {
                    priorityDelete.add(pair.getFirst());
                    priorityDelete.add(pair.getSecond());
                }
            }
            case KROPKI -> {
                KropkiModifier kropkiModifier = deriveKropkiConstraints(sudoku);
                modifiers.setKropki(kropkiModifier);
                for (KropkiDot dot : kropkiModifier.dots()) {
                    priorityDelete.add(dot.first());
                    priorityDelete.add(dot.second());
                }
            }
            case SKYSCRAPER -> {
                SkyscraperModifier clues = deriveSkyscraperClues(sudoku.getBoard());
                modifiers.setSkyscraper(clues);
                for (int i = 0; i < size; i++) {
                    if (clues.top() != null && clues.top()[i] > 0) priorityDelete.add(new GridCell(0, i));
                    if (clues.bottom() != null && clues.bottom()[i] > 0) priorityDelete.add(new GridCell(size - 1, i));
                    if (clues.left() != null && clues.left()[i] > 0) priorityDelete.add(new GridCell(i, 0));
                    if (clues.right() != null && clues.right()[i] > 0) priorityDelete.add(new GridCell(i, size - 1));
                }
            }
            case XV -> {
                XvModifier clues = deriveXVConstraints(sudoku);
                modifiers.setXv(clues);
                for (XVPair mark : clues.marks()) {
                    priorityDelete.add(mark.first());
                    priorityDelete.add(mark.second());
                }
            }
            case BETWEEN -> {
                int target = switch (sudoku.getDifficulty()) {
                    case EASY -> 4;
                    case MEDIUM -> 7;
                    case HARD -> 12;
                };
                BetweenModifier clues = deriveBetweenConstraints(sudoku, target);
                modifiers.setBetween(clues);
                for (BetweenLine line : clues.lines()) {
                    priorityDelete.add(line.startCircle());
                    priorityDelete.add(line.endCircle());
                    priorityDelete.addAll(line.lineCells());
                }
            }
            case VUDOKU -> {
                var clues = deriveVudokuConstraints(sudoku);
                modifiers.setVudoku(clues);
                for (VudokuMark mark : clues.marks()) {
                    priorityDelete.add(mark.vertex());
                    priorityDelete.add(mark.arm1());
                    priorityDelete.add(mark.arm2());
                }
            }
            case EVEN_ODD -> {
                double probability = DifficultyChoosing.getEvenOddProbabilities(sudoku.getDifficulty(), sudoku.getRandomInstance());
                EvenOddModifier clues = generateParityGrid(sudoku.getSolutionBoard(), sudoku.getRandomInstance(), probability);
                modifiers.setEvenOdd(clues);
                for (int r = 0; r < size; r++) {
                    for (int c = 0; c < size; c++) {
                        if (clues.parityTypes()[r][c] != ParityType.NONE) {
                            priorityDelete.add(new GridCell(r, c));
                        }
                    }
                }
            }
            case SANDWICH -> {
                SandwichModifier clues = deriveSandwichModifier(sudoku);
                modifiers.setSandwich(clues);
            }
            case X_SUMS -> {
                XSumsModifier xSumsModifier = deriveXSumsConstraints(sudoku);
                modifiers.setXSums(xSumsModifier);
            }
            case KILLER -> {
                KillerModifier killerModifier = deriveKillerConstraints(sudoku);
                modifiers.setKiller(killerModifier);

                for (var cage : killerModifier.cages()) {
                    List<GridCell> cageCells = cage.cells();
                    if (cageCells == null || cageCells.isEmpty()) continue;

                    int cageSize = cageCells.size();

                    if (cageSize == 1) {
                        priorityDelete.addAll(cageCells);
                    } else if (cageSize == 2) {
                        for (GridCell cell : cageCells) {
                            if (sudoku.getRandomInstance().nextDouble() < 0.85) {
                                priorityDelete.add(cell);
                            }
                        }
                    } else {
                        double deleteChance = 1.2 / cageSize;
                        int maxDeletes = cageSize / 2;
                        int deletedCount = 0;

                        List<GridCell> shuffledCageCells = new ArrayList<>(cageCells);
                        Collections.shuffle(shuffledCageCells, sudoku.getRandomInstance());

                        for (GridCell cell : shuffledCageCells) {
                            if (deletedCount < maxDeletes && sudoku.getRandomInstance().nextDouble() < deleteChance) {
                                priorityDelete.add(cell);
                                deletedCount++;
                            }
                        }

                        if (deletedCount == 0) {
                            priorityDelete.add(shuffledCageCells.getFirst());
                        }
                    }
                }
            }
            case QUADRUPLES -> {
                QuadruplesModifier quadruplesModifier = deriveQuadruples(sudoku);
                modifiers.setQuadruples(quadruplesModifier);
                quadruplesModifier.marks().forEach(mark -> gatherAllFour(priorityDelete, mark.topLeft()));
            }
            case GROUP_SUMS -> {
                GroupSumsModifier groupSumsModifier = deriveGroupSums(sudoku);
                modifiers.setGroupSums(groupSumsModifier);
                groupSumsModifier.marks().forEach(mark -> gatherAllFour(priorityDelete, mark.topLeft()));
            }
            case PATTERNED -> {
                if (sudoku.getPattern() != null && sudoku.getPattern().isSelective() && sudoku.getPattern().getIndexCount() <= 3) {
                    Integer[][] pattern = sudoku.getPattern().getPattern();
                    for (int r = 0; r < size; r++) {
                        for (int c = 0; c < size; c++) {
                            if (pattern[r][c] != null && pattern[r][c] > 0) {
                                priorityDelete.add(new GridCell(r, c));
                            }
                        }
                    }
                }
            }
        }

        int globalSize = sudoku.getType().getGridSize();

        if (MultiGridConfig.isMultiDoku(sudoku.getVariant())) {
            var setup = MultiGridConfig.LAYOUTS.get(sudoku.getVariant());
            int subSize = 9;

            for (int[] offset : setup.offsets()) {
                int rowOff = offset[0];
                int colOff = offset[1];

                List<Integer> colIndices = new ArrayList<>(subSize);
                for (int i = 0; i < subSize; i++) {
                    colIndices.add(i);
                }
                Collections.shuffle(colIndices, sudoku.getRandomInstance());

                for (int r = 0; r < subSize; r++) {
                    priorityDelete.add(new GridCell(rowOff + r, colOff + colIndices.get(r)));
                }
            }
        } else {
            List<Integer> colIndices = new ArrayList<>(globalSize);
            for (int i = 0; i < globalSize; i++) {
                colIndices.add(i);
            }
            Collections.shuffle(colIndices, sudoku.getRandomInstance());

            for (int r = 0; r < globalSize; r++) {
                priorityDelete.add(new GridCell(r, colIndices.get(r)));
            }
        }

        return new ArrayList<>(priorityDelete);
    }

    private static void gatherAllFour(Set<GridCell> priorityDelete, GridCell gridCell) {
        int r = gridCell.row();
        int c = gridCell.col();
        priorityDelete.add(new GridCell(r, c));
        priorityDelete.add(new GridCell(r + 1, c));
        priorityDelete.add(new GridCell(r, c + 1));
        priorityDelete.add(new GridCell(r + 1, c + 1));
    }
}
