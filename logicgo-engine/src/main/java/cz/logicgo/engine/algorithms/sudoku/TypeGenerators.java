package cz.logicgo.engine.algorithms.sudoku;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.CompType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.ParityType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.RossiniType;

import java.util.*;

import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.*;
import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.MarkType.V;
import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.MarkType.X;


public class TypeGenerators {

    public static EvenOddModifier generateParityGrid(SudokuCell[][] filledBoard, Random random, double targetProbability) {
        int gridSize = filledBoard.length;
        ParityType[][] parityGrid = new ParityType[gridSize][gridSize];

        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (random.nextDouble() < targetProbability) {
                    if (filledBoard[r][c].getValue() % 2 == 0) {
                        parityGrid[r][c] = ParityType.EVEN;
                    } else {
                        parityGrid[r][c] = ParityType.ODD;
                    }
                } else {
                    parityGrid[r][c] = ParityType.NONE;
                }
            }
        }

        return new EvenOddModifier(parityGrid);
    }


    public static XSumsModifier deriveXSumsConstraints(Sudoku sudoku) {
        int size = sudoku.getType().getGridSize();
        SudokuCell[][] board = sudoku.getSolutionBoard();
        int[] top = new int[size];
        int[] bottom = new int[size];
        int[] left = new int[size];
        int[] right = new int[size];

        var random = sudoku.getRandomInstance();
        var doubleR = random.nextDouble();
        double probability = switch (sudoku.getDifficulty()) {
            case EASY -> 0.75 + (doubleR * 0.15);
            case MEDIUM -> 0.50 + (doubleR * 0.15);
            case HARD -> 0.25 + (doubleR * 0.15);
        };

        for (int i = 0; i < size; i++) {
            if (random.nextDouble() < probability) {
                int xLeft = board[i][0].getValue();
                for (int j = 0; j < xLeft; j++) left[i] += board[i][j].getValue();
            }

            if (random.nextDouble() < probability) {
                int xRight = board[i][size - 1].getValue();
                for (int j = 0; j < xRight; j++) right[i] += board[i][size - 1 - j].getValue();
            }

            if (random.nextDouble() < probability) {
                int xTop = board[0][i].getValue();
                for (int j = 0; j < xTop; j++) top[i] += board[j][i].getValue();
            }

            if (random.nextDouble() < probability) {
                int xBottom = board[size - 1][i].getValue();
                for (int j = 0; j < xBottom; j++) bottom[i] += board[size - 1 - j][i].getValue();
            }
        }

        return new XSumsModifier(top, bottom, left, right);
    }

    public static KillerModifier deriveKillerConstraints(Sudoku sudoku) {
        int size = sudoku.getType().getGridSize();
        SudokuCell[][] board = sudoku.getSolutionBoard() != null ? sudoku.getSolutionBoard() : sudoku.getBoard();
        Random rand = sudoku.getRandomInstance();
        var difficulty = sudoku.getDifficulty();

        List<KillerCage> cages = new ArrayList<>();
        boolean[][] used = new boolean[size][size];
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        double extremeCageChance = switch (difficulty) {
            case EASY -> 1.0;
            case MEDIUM -> 0.50;
            case HARD -> 0.25;
        };

        int maxSum = (size * 2) - 1;
        int maxSumMinusOne = (size * 2) - 2;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (used[r][c]) continue;
                int val1 = board[r][c].getValue();

                for (int[] d : dirs) {
                    int nr = r + d[0];
                    int nc = c + d[1];

                    if (nr >= 0 && nr < size && nc >= 0 && nc < size && !used[nr][nc]) {
                        int val2 = board[nr][nc].getValue();
                        int sum = val1 + val2;

                        if (sum == 3 || sum == 4 || sum == maxSum || sum == maxSumMinusOne) {
                            if (rand.nextDouble() < extremeCageChance) {
                                List<GridCell> extremeCage = new ArrayList<>();
                                extremeCage.add(new GridCell(r, c));
                                extremeCage.add(new GridCell(nr, nc));

                                cages.add(new KillerCage(sum, extremeCage));
                                used[r][c] = true;
                                used[nr][nc] = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (used[r][c]) continue;

                int targetSize = determineTargetSize(rand, size);
                List<GridCell> cageCells = new ArrayList<>();
                Set<Integer> cageValues = new HashSet<>();

                List<GridCell> frontier = new ArrayList<>();

                GridCell startCell = new GridCell(r, c);
                cageCells.add(startCell);
                cageValues.add(board[r][c].getValue());
                used[r][c] = true;

                addNeighborsToFrontier(startCell, frontier, used, size, dirs);

                while (cageCells.size() < targetSize && !frontier.isEmpty()) {
                    frontier.removeIf(cell -> cageValues.contains(board[cell.row()][cell.col()].getValue()));

                    if (frontier.isEmpty()) break;

                    GridCell next = frontier.remove(rand.nextInt(frontier.size()));

                    cageCells.add(next);
                    cageValues.add(board[next.row()][next.col()].getValue());
                    used[next.row()][next.col()] = true;

                    addNeighborsToFrontier(next, frontier, used, size, dirs);
                }

                int sum = cageValues.stream().mapToInt(Integer::intValue).sum();
                cages.add(new KillerCage(sum, cageCells));
            }
        }

        return new KillerModifier(cages);
    }

    private static void addNeighborsToFrontier(GridCell cell, List<GridCell> frontier, boolean[][] used, int size, int[][] dirs) {
        for (int[] d : dirs) {
            int nr = cell.row() + d[0];
            int nc = cell.col() + d[1];
            if (nr >= 0 && nr < size && nc >= 0 && nc < size && !used[nr][nc]) {
                GridCell neighbor = new GridCell(nr, nc);
                if (!frontier.contains(neighbor)) {
                    frontier.add(neighbor);
                }
            }
        }
    }

    private static int determineTargetSize(Random rand, int boardSize) {
        int roll = rand.nextInt(100);
        int maxSize = boardSize > 9 ? 8 : 6;

        if (roll < 10) return 2;
        if (roll < 45) return 3;
        if (roll < 75) return 4;
        if (roll < 95) return 5;
        return rand.nextInt(maxSize - 5 + 1) + 6;
    }

    public static GroupSumsModifier deriveGroupSums(Sudoku sudoku) {
        int size = sudoku.getType().getGridSize();
        var board = sudoku.getSolutionBoard() != null ? sudoku.getSolutionBoard() : sudoku.getBoard();
        var random = sudoku.getRandomInstance();
        List<GroupSumMark> marks = new ArrayList<>();

        List<GridCell> possibleIntersections = new ArrayList<>();
        for (int r = 0; r < size - 1; r++) {
            for (int c = 0; c < size - 1; c++) {
                possibleIntersections.add(new GridCell(r, c));
            }
        }
        double targetRatio = switch (sudoku.getDifficulty()) {
            case EASY -> 0.35;
            case MEDIUM -> 0.25;
            case HARD -> 0.15;
        };

        int targetCount = (int) Math.round(possibleIntersections.size() * targetRatio);
        targetCount = Math.max(2, targetCount);

        Collections.shuffle(possibleIntersections, random);
        List<GridCell> selectedPositions = possibleIntersections.subList(0, targetCount);

        for (GridCell cell : selectedPositions) {
            int r = cell.row();
            int c = cell.col();

            int sum = board[r][c].getValue() + board[r + 1][c].getValue() +
                    board[r][c + 1].getValue() + board[r + 1][c + 1].getValue();
            marks.add(new GroupSumMark(new GridCell(r, c), sum));
        }

        return new GroupSumsModifier(marks);
    }

    public static QuadruplesModifier deriveQuadruples(Sudoku sudoku) {
        int size = sudoku.getType().getGridSize();
        var board = sudoku.getSolutionBoard() != null ? sudoku.getSolutionBoard() : sudoku.getBoard();
        var random = sudoku.getRandomInstance();
        List<QuadrupleMark> marks = new ArrayList<>();

        List<GridCell> possibleIntersections = new ArrayList<>();
        for (int r = 0; r < size - 1; r++) {
            for (int c = 0; c < size - 1; c++) {
                possibleIntersections.add(new GridCell(r, c));
            }
        }

        double targetRatio = switch (sudoku.getDifficulty()) {
            case EASY -> 0.28;
            case MEDIUM -> 0.15;
            case HARD -> 0.09;
        };

        int targetCount = (int) Math.round(possibleIntersections.size() * targetRatio);
        targetCount = Math.max(2, targetCount);

        Collections.shuffle(possibleIntersections, random);
        List<GridCell> selectedPositions = possibleIntersections.subList(0, targetCount);

        for (GridCell cell : selectedPositions) {
            int r = cell.row();
            int c = cell.col();

            List<Integer> values = Arrays.asList(
                    board[r][c].getValue(), board[r + 1][c].getValue(),
                    board[r][c + 1].getValue(), board[r + 1][c + 1].getValue()
            );

            List<Integer> sortedValues = new ArrayList<>(values);
            Collections.sort(sortedValues);

            marks.add(new QuadrupleMark(new GridCell(r, c), sortedValues));
        }

        return new QuadruplesModifier(marks);
    }

    public static SandwichModifier deriveSandwichModifier(Sudoku sudoku) {
        int size = sudoku.getType().getGridSize();
        int[] left = new int[size];
        for (int r = 0; r < size; r++) {
            left[r] = countWholePart(sudoku.getBoard(), r, 0, 0, 1, size);
        }

        int[] top = new int[size];
        for (int c = 0; c < size; c++) {
            top[c] = countWholePart(sudoku.getBoard(), 0, c, 1, 0, size);
        }

        return new SandwichModifier(top, left);
    }

    private static int countWholePart(SudokuCell[][] board, int rStart, int cStart, int rStep, int cStep, int gridSize) {
        int count = 0;
        int r = rStart;
        int c = cStart;
        boolean foundFirstBoundary = false;

        for (int i = 0; i < gridSize; i++) {
            int val = board[r][c].getValue();

            boolean isOne = (val == 1);
            boolean isMax = (val == gridSize);

            if (isOne || isMax) {
                if (foundFirstBoundary) {
                    break;
                } else {
                    foundFirstBoundary = true;
                }
            } else {
                if (foundFirstBoundary) {
                    count += val;
                }
            }

            r += rStep;
            c += cStep;
        }
        return count;
    }

    public static VudokuModifier deriveVudokuConstraints(Sudoku sudoku) {
        int size = sudoku.getType().getGridSize();
        SudokuCell[][] board = sudoku.getBoard();
        List<VudokuMark> generatedMarks = new ArrayList<>();
        Random rand = sudoku.getRandomInstance();

        int[][] dRow = {{-1, 0}, {-1, 0}, {1, 0}, {1, 0}};
        int[][] dCol = {{0, -1}, {0, 1}, {0, -1}, {0, 1}};

        List<VudokuMark> allPossibleMarks = new ArrayList<>();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int vertexVal = board[r][c].getValue();

                for (int dir = 0; dir < 4; dir++) {
                    int rA = r + dRow[dir][0];
                    int cA = c + dCol[dir][0];
                    int rB = r + dRow[dir][1];
                    int cB = c + dCol[dir][1];

                    if (rA >= 0 && rA < size && cA >= 0 && cA < size &&
                            rB >= 0 && rB < size && cB >= 0 && cB < size) {

                        int valA = board[rA][cA].getValue();
                        int valB = board[rB][cB].getValue();

                        if (vertexVal == (valA + valB) || vertexVal == Math.abs(valA - valB)) {
                            allPossibleMarks.add(new VudokuMark(new GridCell(r, c), new GridCell(rA, cA), new GridCell(rB, cB)));
                        }
                    }
                }
            }
        }

        if (allPossibleMarks.isEmpty()) {
            return new VudokuModifier(generatedMarks);
        }

        double targetRatio = switch (sudoku.getDifficulty()) {
            case EASY -> 0.40;
            case MEDIUM -> 0.26;
            case HARD -> 0.12;
        };

        int targetCount = (int) Math.round(allPossibleMarks.size() * targetRatio);
        if (sudoku.getDifficulty() != Difficulty.HARD) {
            targetCount = Math.max(2, targetCount);
        }

        targetCount = Math.min(targetCount, allPossibleMarks.size());

        Collections.shuffle(allPossibleMarks, rand);
        boolean[][] usedCells = new boolean[size][size];

        for (VudokuMark mark : allPossibleMarks) {
            if (generatedMarks.size() >= targetCount) {
                break;
            }

            int rV = mark.vertex().row(), cV = mark.vertex().col();
            int rA = mark.arm1().row(), cA = mark.arm1().col();
            int rB = mark.arm2().row(), cB = mark.arm2().col();
            if (!usedCells[rV][cV] && !usedCells[rA][cA] && !usedCells[rB][cB]) {
                usedCells[rV][cV] = true;
                usedCells[rA][cA] = true;
                usedCells[rB][cB] = true;
                generatedMarks.add(mark);
            }
        }

        return new VudokuModifier(generatedMarks);
    }

    public static BetweenModifier deriveBetweenConstraints(Sudoku sudoku, int targetLineCount) {
        int size = sudoku.getType().getGridSize();
        SudokuCell[][] board = sudoku.getBoard();
        List<BetweenLine> lines = new ArrayList<>();
        Random rand = sudoku.getRandomInstance();
        Set<GridCell> usedCells = new HashSet<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        int attempts = 0;
        while (lines.size() < targetLineCount && attempts < 1000) {
            attempts++;
            int startR = rand.nextInt(size);
            int startC = rand.nextInt(size);
            int[] dir = directions[rand.nextInt(directions.length)];
            int length = rand.nextInt(4) + 3;
            int endR = startR + dir[0] * (length - 1);
            int endC = startC + dir[1] * (length - 1);
            if (endR >= 0 && endR < size && endC >= 0 && endC < size) {

                int valStart = board[startR][startC].getValue();

                int valEnd = board[endR][endC].getValue();


                int minBoundary = Math.min(valStart, valEnd);

                int maxBoundary = Math.max(valStart, valEnd);


                if (maxBoundary - minBoundary < 2) continue;


                boolean isValid = true;

                List<GridCell> candidateCells = new ArrayList<>();

                List<GridCell> linePath = new ArrayList<>();


                candidateCells.add(new GridCell(startR, startC));

                candidateCells.add(new GridCell(endR, endC));

                Set<Integer> uniqueValues = new HashSet<>();

                uniqueValues.add(valStart);

                uniqueValues.add(valEnd);


                for (int i = 1; i < length - 1; i++) {

                    int pathR = startR + dir[0] * i;

                    int pathC = startC + dir[1] * i;

                    int pathVal = board[pathR][pathC].getValue();


                    GridCell pathCell = new GridCell(pathR, pathC);

                    candidateCells.add(pathCell);

                    linePath.add(pathCell);


                    if (pathVal <= minBoundary || pathVal >= maxBoundary) {

                        isValid = false;

                        break;

                    }

                    if (!uniqueValues.add(pathVal)) {

                        isValid = false;

                        break;

                    }

                }


                if (isValid) {

                    for (GridCell cell : candidateCells) {

                        if (usedCells.contains(cell)) {

                            isValid = false;

                            break;

                        }

                    }

                }


                if (isValid) {

                    usedCells.addAll(candidateCells);


                    lines.add(new BetweenLine(new GridCell(startR, startC), new GridCell(endR, endC), linePath));

                }

            }

        }

        return new BetweenModifier(lines);

    }


    public static XvModifier deriveXVConstraints(Sudoku sudoku) {
        int size = sudoku.getType().getGridSize();
        SudokuCell[][] board = sudoku.getBoard();
        List<XVPair> xvMarks = new ArrayList<>();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int currentVal = board[r][c].getValue();

                if (c + 1 < size) {
                    int rightVal = board[r][c + 1].getValue();
                    if (currentVal + rightVal == 10) {
                        xvMarks.add(new XVPair(new GridCell(r, c), new GridCell(r, c + 1), X));
                    } else if (currentVal + rightVal == 5) {
                        xvMarks.add(new XVPair(new GridCell(r, c), new GridCell(r, c + 1), V));
                    }
                }

                if (r + 1 < size) {
                    int bottomVal = board[r + 1][c].getValue();
                    if (currentVal + bottomVal == 10) {
                        xvMarks.add(new XVPair(new GridCell(r, c), new GridCell(r + 1, c), X));
                    } else if (currentVal + bottomVal == 5) {
                        xvMarks.add(new XVPair(new GridCell(r, c), new GridCell(r + 1, c), V));
                    }
                }
            }
        }

        return new XvModifier(xvMarks);
    }

    public static SkyscraperModifier deriveSkyscraperClues(SudokuCell[][] board) {
        int gridSize = board.length;
        int[] topClues = new int[gridSize];
        int[] bottomClues = new int[gridSize];
        int[] leftClues = new int[gridSize];
        int[] rightClues = new int[gridSize];

        for (int i = 0; i < gridSize; i++) {
            topClues[i] = countVisibleSkyscrapers(board, 0, i, 1, 0, gridSize);
            bottomClues[i] = countVisibleSkyscrapers(board, gridSize - 1, i, -1, 0, gridSize);
            leftClues[i] = countVisibleSkyscrapers(board, i, 0, 0, 1, gridSize);
            rightClues[i] = countVisibleSkyscrapers(board, i, gridSize - 1, 0, -1, gridSize);
        }

        return new SkyscraperModifier(topClues, bottomClues, leftClues, rightClues);
    }

    private static int countVisibleSkyscrapers(SudokuCell[][] board, int rStart, int cStart, int rStep, int cStep, int gridSize) {
        int count = 0;
        int maxSeen = 0;
        int r = rStart;
        int c = cStart;

        for (int i = 0; i < gridSize; i++) {
            int val = board[r][c].getValue();

            if (val > maxSeen) {
                count++;
                maxSeen = val;
            }
            r += rStep;
            c += cStep;
        }
        return count;
    }

    public static RossiniType[] generateRossiniTypes(int rossiniCount, Random random) {
        int count = 9 * 2;
        Set<Integer> indexes = new HashSet<>();
        int i = 0;
        while (i < rossiniCount) {
            indexes.add(random.nextInt(0, count));
            i++;
        }
        RossiniType[] rossiniTypes = new RossiniType[count];

        for (int j = 0; j < count; j++) {
            int value = indexes.contains(j) ? random.nextInt(0, 2) : 2;
            rossiniTypes[j] = RossiniType.fromValue(value);
        }

        return rossiniTypes;
    }

    public static ConsecutiveModifier deriveConsecutiveConstraints(Sudoku sudoku) {
        List<Pair<GridCell, GridCell>> consecutivePairs = new ArrayList<>();
        int size = sudoku.getType().getGridSize();
        var board = sudoku.getBoard();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int currentVal = board[r][c].getValue();
                if (currentVal == 0) continue;

                if (c + 1 < size) {
                    int rightVal = board[r][c + 1].getValue();
                    if (Math.abs(currentVal - rightVal) == 1) {
                        consecutivePairs.add(new Pair<>(new GridCell(r, c), new GridCell(r, c + 1)));
                    }
                }

                if (r + 1 < size) {
                    int bottomVal = board[r + 1][c].getValue();
                    if (Math.abs(currentVal - bottomVal) == 1) {
                        consecutivePairs.add(new Pair<>(new GridCell(r, c), new GridCell(r + 1, c)));
                    }
                }
            }
        }
        return new ConsecutiveModifier(consecutivePairs);
    }

    public static KropkiModifier deriveKropkiConstraints(Sudoku sudoku) {
        int size = sudoku.getType().getGridSize();
        var board = sudoku.getBoard();
        var random = sudoku.getRandomInstance();
        List<KropkiDot> generatedDots = new ArrayList<>();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int currentVal = board[r][c].getValue();
                if (currentVal == 0) continue;

                if (c + 1 < size) {
                    int rightVal = board[r][c + 1].getValue();
                    addKropkiDotIfValid(r, c, r, c + 1, currentVal, rightVal, random, generatedDots);
                }

                if (r + 1 < size) {
                    int bottomVal = board[r + 1][c].getValue();
                    addKropkiDotIfValid(r, c, r + 1, c, currentVal, bottomVal, random, generatedDots);
                }
            }
        }
        return new KropkiModifier(generatedDots);
    }

    private static void addKropkiDotIfValid(int r1, int c1, int r2, int c2, int val1, int val2,
                                            Random random, List<KropkiDot> dots) {
        if (val1 == 0 || val2 == 0) return;

        boolean isConsecutive = Math.abs(val1 - val2) == 1;
        boolean isRatio = (val1 * 2 == val2) || (val2 * 2 == val1);

        if (isConsecutive || isRatio) {
            DotColor color;

            if (isConsecutive && isRatio) {
                color = random.nextBoolean() ? DotColor.WHITE : DotColor.BLACK;
            } else if (isConsecutive) {
                color = DotColor.WHITE;
            } else {
                color = DotColor.BLACK;
            }

            dots.add(new KropkiDot(new GridCell(r1, c1), new GridCell(r2, c2), color));
        }
    }

    public static GreaterThanModifier deriveGreaterThanConstraints(Sudoku sudoku) {
        int size = sudoku.getType().getGridSize();
        var board = sudoku.getSolutionBoard() != null ? sudoku.getSolutionBoard() : sudoku.getBoard();

        CompType[][] horizontal = new CompType[size][size - 1];
        CompType[][] vertical = new CompType[size - 1][size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size - 1; c++) {
                int leftVal = board[r][c].getValue();
                int rightVal = board[r][c + 1].getValue();
                horizontal[r][c] = (leftVal > rightVal) ? CompType.BIGGER : CompType.SMALLER;
            }
        }

        for (int r = 0; r < size - 1; r++) {
            for (int c = 0; c < size; c++) {
                int topVal = board[r][c].getValue();
                int bottomVal = board[r + 1][c].getValue();
                vertical[r][c] = (topVal > bottomVal) ? CompType.BIGGER : CompType.SMALLER;
            }
        }

        return new GreaterThanModifier(horizontal, vertical);
    }

    public static HashMap<Integer, List<GridCell>> deriveCloneConstraints(int gridSize, int copiesCount, int shapeSize, boolean forcePlusShape, Random random) {
        List<GridCell> baseShape;
        if (forcePlusShape) {
            baseShape = List.of(
                    new GridCell(0, 0), new GridCell(-1, 0), new GridCell(1, 0),
                    new GridCell(0, -1), new GridCell(0, 1)
            );
        } else {
            baseShape = generateRandomConnectedShape(shapeSize, random);
        }

        List<List<GridCell>> allValidPlacements = new ArrayList<>();
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                List<GridCell> placement = new ArrayList<>();
                boolean isValid = true;

                for (GridCell offset : baseShape) {
                    int nr = r + offset.row();
                    int nc = c + offset.col();

                    if (nr < 0 || nr >= gridSize || nc < 0 || nc >= gridSize) {
                        isValid = false;
                        break;
                    }
                    placement.add(new GridCell(nr, nc));
                }

                if (isValid) {
                    allValidPlacements.add(placement);
                }
            }
        }

        Collections.shuffle(allValidPlacements, random);
        List<List<GridCell>> selectedClones = new ArrayList<>();
        boolean[][] usedCells = new boolean[gridSize][gridSize];

        HashMap<Integer, List<GridCell>> groupsMap = new HashMap<>();

        if (findNonOverlappingSelection(allValidPlacements, selectedClones, copiesCount, 0, usedCells)) {
            for (int i = 0; i < selectedClones.size(); i++) {
                groupsMap.put(i, selectedClones.get(i));
            }
        } else {

        }

        return groupsMap;
    }

    private static List<GridCell> generateRandomConnectedShape(int size, Random random) {
        List<GridCell> shape = new ArrayList<>();
        shape.add(new GridCell(0, 0));
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (shape.size() < size) {
            GridCell base = shape.get(random.nextInt(shape.size()));
            int[] d = dirs[random.nextInt(4)];
            GridCell newCell = new GridCell(base.row() + d[0], base.col() + d[1]);

            if (!shape.contains(newCell)) {
                shape.add(newCell);
            }
        }
        return shape;
    }

    private static boolean findNonOverlappingSelection(
            List<List<GridCell>> placements,
            List<List<GridCell>> selected,
            int targetCount,
            int index,
            boolean[][] used) {

        if (selected.size() == targetCount) return true;
        if (index >= placements.size()) return false;

        List<GridCell> currentPlacement = placements.get(index);

        boolean canPlace = true;
        for (GridCell cell : currentPlacement) {
            if (used[cell.row()][cell.col()]) {
                canPlace = false;
                break;
            }
        }

        if (canPlace) {
            for (GridCell cell : currentPlacement) used[cell.row()][cell.col()] = true;
            selected.add(currentPlacement);

            if (findNonOverlappingSelection(placements, selected, targetCount, index + 1, used)) {
                return true;
            }

            selected.removeLast();
            for (GridCell cell : currentPlacement) used[cell.row()][cell.col()] = false;
        }

        return findNonOverlappingSelection(placements, selected, targetCount, index + 1, used);
    }
}
