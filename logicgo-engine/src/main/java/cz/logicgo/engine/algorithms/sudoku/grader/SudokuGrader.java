package cz.logicgo.engine.algorithms.sudoku.grader;


import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.CompType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.ParityType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.misc.enums.hints.SudokuTechnique;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.engine.algorithms.sudoku.TargetedCell;
import cz.logicgo.engine.algorithms.sudoku.multi.SubGrid;
import cz.logicgo.engine.algorithms.sudoku.validators.ISudokuValidator;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiSudokuValidator;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.SelectiveUnitConstraint;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.UnitConstraint;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.*;
import java.util.stream.IntStream;

import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.DotColor;
import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.MarkType;


public class SudokuGrader {
    private static final int MAX_GRADER_STEPS = 200;

    public static SudokuTechnique getNextLogicalHint(SudokuGame currentGameState) {
        SudokuGame copy = new SudokuGame(currentGameState, true);

        if (applyNakedSingles(copy)) return SudokuTechnique.NAKED_SINGLE;
        if (applyHiddenSingles(copy)) return SudokuTechnique.HIDDEN_SINGLE;
        if (!MultiGridConfig.isMultiDoku(currentGameState.getSudoku().getVariant()) && (
                applyXVSingles(copy) ||
                        applySkyscraperBasics(copy) ||
                        applyEvenOddBasics(copy) ||
                        applyConsecutiveBasics(copy) ||
                        applyGreaterThanBasics(copy) ||
                        applyBetweenBasics(copy) ||
                        applySandwichBasics(copy) ||
                        applyVudokuBasics(copy) ||
                        applyKropkiBasics(copy) ||
                        applyGroupSumsBasics(copy) ||
                        applyQuadruplesBasics(copy) ||
                        applyKillerBasics(copy) ||
                        applyKillerAdvanced(copy))) {

            return SudokuTechnique.VARIANT_BASIC;
        }
        if (applyNakedPairs(copy)) return SudokuTechnique.NAKED_PAIR;
        if (applyPointingPairs(copy)) return SudokuTechnique.POINTING_PAIR;
        if (applyBoxLineReduction(copy)) return SudokuTechnique.BOX_LINE_REDUCTION;
        if (applyNakedTriples(copy)) return SudokuTechnique.NAKED_TRIPLE;
        if (applyHiddenPairs(copy)) return SudokuTechnique.HIDDEN_PAIR;
        if (applyHiddenTriples(copy)) return SudokuTechnique.HIDDEN_TRIPLE;

        if (applyXWing(copy)) return SudokuTechnique.X_WING;
        if (applySwordfish(copy)) return SudokuTechnique.SWORDFISH;
        if (applyXYWing(copy)) return SudokuTechnique.XY_WING;
        if (applyAvoidableRectangle(copy)) return SudokuTechnique.AVOIDABLE_RECTANGLE;

        return null;
    }

    public static int gradeSudoku(SudokuGame sudokuGame) {
        double totalScore = 0;
        boolean changed = true;
        ISudokuValidator validator = sudokuGame.getSudokuValidator();

        SudokuVariant variant = sudokuGame.getSudoku().getVariant();

        int maxNumber = (validator instanceof MultiSudokuValidator multi)
                ? multi.getSubGrids().getFirst().validator().getGridSize()
                : validator.getGridSize();
        double maxCandidates = validator.getEmptyCells().size() * maxNumber;

        Map<String, Long> usageStats = new LinkedHashMap<>();
        int maxDifficultyCap = 4;

        int middleTierTechniquesCount = 0;

        int stepCount = 0;
        boolean loopLimitReached = false;

        while (changed && !validator.getEmptyCells().isEmpty()) {
            stepCount++;
            if (stepCount > MAX_GRADER_STEPS) {

                loopLimitReached = true;
                break;
            }

            changed = false;

            double F = calculateDensityFactor(sudokuGame, maxCandidates);

            if (applyNakedSingles(sudokuGame)) {
                totalScore += (F * 0.1);
                usageStats.put("Naked Singles", usageStats.getOrDefault("Naked Singles", 0L) + 1);
                changed = true;
                continue;
            }

            if (applyHiddenSingles(sudokuGame)) {
                totalScore += (F * 0.5);
                usageStats.put("Hidden Singles", usageStats.getOrDefault("Hidden Singles", 0L) + 1);
                changed = true;
                continue;
            }

            if (variant != null) {
                switch (variant) {
                    case XV -> {
                        if (applyXVSingles(sudokuGame)) {
                            totalScore += (F * 1.5);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 7);
                            usageStats.put("XV Basics", usageStats.getOrDefault("XV Basics", 0L) + 1);
                            changed = true;
                        }
                    }
                    case SKYSCRAPER -> {
                        if (applySkyscraperBasics(sudokuGame)) {
                            totalScore += (F * 1.5);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 7);
                            usageStats.put("Skyscraper Basics", usageStats.getOrDefault("Skyscraper Basics", 0L) + 1);
                            changed = true;
                        }
                    }
                    case EVEN_ODD -> {
                        if (applyEvenOddBasics(sudokuGame)) {
                            totalScore += (F * 1.5);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 7);
                            usageStats.put("Parity Basics", usageStats.getOrDefault("Parity Basics", 0L) + 1);
                            changed = true;
                        }
                    }
                    case CONSECUTIVE -> {
                        if (applyConsecutiveBasics(sudokuGame)) {
                            totalScore += (F * 1.5);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 7);
                            usageStats.put("Consecutive Basics", usageStats.getOrDefault("Consecutive Basics", 0L) + 1);
                            changed = true;
                        }
                    }
                    case GREATER_THAN -> {
                        if (applyGreaterThanBasics(sudokuGame)) {
                            totalScore += (F * 1.5);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 7);
                            usageStats.put("Greater Than Basics", usageStats.getOrDefault("Greater Than Basics", 0L) + 1);
                            changed = true;
                        }
                    }
                    case BETWEEN -> {
                        if (applyBetweenBasics(sudokuGame)) {
                            totalScore += (F * 1.5);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 7);
                            usageStats.put("Between Basics", usageStats.getOrDefault("Between Basics", 0L) + 1);
                            changed = true;
                        }
                    }
                    case SANDWICH -> {
                        if (applySandwichBasics(sudokuGame)) {
                            totalScore += (F * 1.5);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 7);
                            usageStats.put("Sandwich Basics", usageStats.getOrDefault("Sandwich Basics", 0L) + 1);
                            changed = true;
                        }
                    }
                    case VUDOKU -> {
                        if (applyVudokuBasics(sudokuGame)) {
                            totalScore += (F * 1.5);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 7);
                            usageStats.put("Vudoku Basics", usageStats.getOrDefault("Vudoku Basics", 0L) + 1);
                            changed = true;
                        }
                    }
                    case X_SUMS -> {
                        if (applyXSumsBasics(sudokuGame)) {
                            totalScore += (F * 1.5);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 7);
                            usageStats.put("X Sums Basics", usageStats.getOrDefault("X Sums Basics", 0L) + 1);
                            changed = true;
                        }
                    }
                    case KROPKI -> {
                        if (applyKropkiBasics(sudokuGame)) {
                            totalScore += (F * 1.5);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 7);
                            usageStats.put("Kropki Basics", usageStats.getOrDefault("Kropki Basics", 0L) + 1);
                            changed = true;
                        }
                    }
                    case KILLER -> {
                        if (applyKillerBasics(sudokuGame)) {
                            totalScore += (F * 1.5);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 7);
                            usageStats.put("Killer Basics", usageStats.getOrDefault("Killer Basics", 0L) + 1);
                            changed = true;
                            break;
                        }
                        if (applyKillerAdvanced(sudokuGame)) {
                            totalScore += (F * 5.0);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 8);
                            usageStats.put("Killer Advanced", usageStats.getOrDefault("Killer Advanced", 0L) + 1);
                            changed = true;
                            continue;
                        }
                    }
                    case QUADRUPLES -> {
                        if (applyQuadruplesBasics(sudokuGame)) {
                            totalScore += (F * 1.5);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 7);
                            usageStats.put("Quadruples Basics", usageStats.getOrDefault("Quadruples Basics", 0L) + 1);
                            changed = true;
                        }
                    }
                    case GROUP_SUMS -> {
                        if (applyGroupSumsBasics(sudokuGame)) {
                            totalScore += (F * 1.5);
                            maxDifficultyCap = Math.max(maxDifficultyCap, 7);
                            usageStats.put("Group Sums Basics", usageStats.getOrDefault("Group Sums Basics", 0L) + 1);
                            changed = true;
                        }
                    }
                    default -> {
                    }
                }
                if (changed) continue;
            }

            if (applyNakedPairs(sudokuGame)) {
                totalScore += (F * 3);
                middleTierTechniquesCount++;
                usageStats.put("Naked Pairs", usageStats.getOrDefault("Naked Pairs", 0L) + 1);
                changed = true;
                continue;
            }

            if (applyPointingPairs(sudokuGame)) {
                totalScore += 15;
                middleTierTechniquesCount++;
                usageStats.put("Pointing Pairs", usageStats.getOrDefault("Pointing Pairs", 0L) + 1);
                changed = true;
                continue;
            }

            if (applyBoxLineReduction(sudokuGame)) {
                totalScore += 15;
                middleTierTechniquesCount++;
                usageStats.put("Box Line Reduction", usageStats.getOrDefault("Box Line Reduction", 0L) + 1);
                changed = true;
                continue;
            }

            if (applyHiddenPairs(sudokuGame)) {
                totalScore += 20;
                middleTierTechniquesCount++;
                usageStats.put("Hidden Pairs", usageStats.getOrDefault("Hidden Pairs", 0L) + 1);
                changed = true;
                continue;
            }

            if (applyNakedTriples(sudokuGame)) {
                totalScore += (F * 8);
                middleTierTechniquesCount++;
                usageStats.put("Naked Triples", usageStats.getOrDefault("Naked Triples", 0L) + 1);
                changed = true;
                continue;
            }

            if (applyHiddenTriples(sudokuGame)) {
                totalScore += 30;
                middleTierTechniquesCount++;
                usageStats.put("Hidden Triples", usageStats.getOrDefault("Hidden Triples", 0L) + 1);
                changed = true;
                continue;
            }

            if (applyXWing(sudokuGame)) {
                totalScore += 40;
                maxDifficultyCap = Math.max(maxDifficultyCap, 9);
                usageStats.put("X-Wing", usageStats.getOrDefault("X-Wing", 0L) + 1);
                changed = true;
                continue;
            }

            if (applySwordfish(sudokuGame)) {
                totalScore += 60;
                maxDifficultyCap = Math.max(maxDifficultyCap, 9);
                usageStats.put("Swordfish", usageStats.getOrDefault("Swordfish", 0L) + 1);
                changed = true;
                continue;
            }

            if (applyXYWing(sudokuGame)) {
                totalScore += 25;
                maxDifficultyCap = Math.max(maxDifficultyCap, 8);
                usageStats.put("XY-Wing", usageStats.getOrDefault("XY-Wing", 0L) + 1);
                changed = true;
                continue;
            }

            if (applyAvoidableRectangle(sudokuGame)) {
                totalScore += 45;
                maxDifficultyCap = Math.max(maxDifficultyCap, 9);
                usageStats.put("Avoidable Rectangle", usageStats.getOrDefault("Avoidable Rectangle", 0L) + 1);
                changed = true;
                continue;
            }
        }


        if (loopLimitReached) {
            return 99;
        }

        int remainingEmpty = validator.getEmptyCells().size();

        int allowedTolerance = switch (maxNumber) {
            case 4 -> 1;
            case 5 -> 2;
            case 6 -> 3;
            case 7 -> 5;
            case 8 -> 7;
            case 9 -> 9;
            case 10 -> 11;
            case 11 -> 13;
            case 12 -> 15;
            case 13 -> 17;
            case 14 -> 19;
            case 15 -> 21;
            case 16 -> 24;
            default -> (int) (maxNumber * 0.8);
        };

        boolean isClueHeavyVariant = switch (variant) {
            case KROPKI, CONSECUTIVE, KILLER, XV, GREATER_THAN, BETWEEN -> true;
            default -> false;
        };

        if (isClueHeavyVariant) {
            int variantBonus = switch (variant) {
                case KROPKI, KILLER -> Math.max(12, (int) Math.round(maxNumber * 0.35));
                case BETWEEN -> 12;
                default -> Math.max(6, (int) Math.round(maxNumber * 0.35));
            };

            allowedTolerance += variantBonus;
        }

        if (remainingEmpty <= allowedTolerance) {
            int calculatedDifficulty;

            boolean hasAdvanced = usageStats.containsKey("X-Wing") ||
                    usageStats.containsKey("Swordfish") ||
                    usageStats.containsKey("XY-Wing") ||
                    usageStats.containsKey("Avoidable Rectangle") ||
                    usageStats.containsKey("Killer Advanced");

            if (hasAdvanced) {
                calculatedDifficulty = (usageStats.getOrDefault("X-Wing", 0L) > 1 || usageStats.containsKey("Avoidable Rectangle")) ? 8 : 7;
            } else if (middleTierTechniquesCount > 0) {
                if (middleTierTechniquesCount > 6) {
                    calculatedDifficulty = 7;
                } else if (middleTierTechniquesCount > 2) {
                    calculatedDifficulty = 5;
                } else {
                    calculatedDifficulty = 4;
                }
            } else {
                calculatedDifficulty = (usageStats.getOrDefault("Hidden Singles", 0L) > 5) ? 3 : 2;
            }

            double base = Math.max(2, Math.sqrt(maxNumber) + 2);
            double scale = maxNumber / 3.0;
            int scoreBasedDiff = (int) Math.round((Math.log(totalScore) / Math.log(base)) * scale);

            if (scoreBasedDiff > calculatedDifficulty && calculatedDifficulty < 7) {
                calculatedDifficulty++;
            } else if (scoreBasedDiff < calculatedDifficulty && calculatedDifficulty > 2) {
                calculatedDifficulty--;
            }

            calculatedDifficulty = Math.clamp(calculatedDifficulty, 1, 9);
            return calculatedDifficulty;

        } else {
            return 99;
        }
    }


    public static boolean applySwordfish(SudokuGame sudokuGame) {
        ISudokuValidator baseValidator = sudokuGame.getSudokuValidator();

        if (baseValidator instanceof MultiSudokuValidator multiValidator) {
            for (SubGrid sub : multiValidator.getSubGrids()) {
                if (applySwordfishLocal(sudokuGame, sub)) return true;
            }
            return false;
        } else {
            return applySwordfishLocal(sudokuGame, new SubGrid(0, 0, baseValidator));
        }
    }

    private static boolean applySwordfishLocal(SudokuGame sudokuGame, SubGrid subGrid) {
        ISudokuValidator validator = subGrid.validator();
        int gridSize = validator.getGridSize();
        int rOff = subGrid.rowOffset();
        int cOff = subGrid.colOffset();

        for (int candidate = 1; candidate <= gridSize; candidate++) {
            Map<Integer, Set<Integer>> rowsWithCand = new HashMap<>();
            for (int r = 0; r < gridSize; r++) {
                int gR = rOff + r;
                Set<Integer> cols = new HashSet<>();
                for (int c = 0; c < gridSize; c++) {
                    int gC = cOff + c;
                    if (sudokuGame.getSudoku().getBoard()[gR][gC] != null &&
                            sudokuGame.getSudoku().isZero(gR, gC) &&
                            sudokuGame.getSudokuValidator().getCellCandidates(gR, gC).contains(candidate)) {
                        cols.add(c);
                    }
                }
                if (cols.size() >= 2 && cols.size() <= 3) {
                    rowsWithCand.put(r, cols);
                }
            }

            List<Integer> validRows = new ArrayList<>(rowsWithCand.keySet());
            int rCount = validRows.size();
            for (int i = 0; i < rCount - 2; i++) {
                int r1 = validRows.get(i);
                for (int j = i + 1; j < rCount - 1; j++) {
                    int r2 = validRows.get(j);
                    for (int k = j + 1; k < rCount; k++) {
                        int r3 = validRows.get(k);

                        Set<Integer> combinedCols = new HashSet<>();
                        combinedCols.addAll(rowsWithCand.get(r1));
                        combinedCols.addAll(rowsWithCand.get(r2));
                        combinedCols.addAll(rowsWithCand.get(r3));

                        if (combinedCols.size() == 3) {
                            boolean changedHere = false;
                            for (int targetRow = 0; targetRow < gridSize; targetRow++) {
                                if (targetRow != r1 && targetRow != r2 && targetRow != r3) {
                                    int gTR = rOff + targetRow;
                                    for (int col : combinedCols) {
                                        int gC = cOff + col;
                                        if (sudokuGame.getSudoku().getBoard()[gTR][gC] != null &&
                                                sudokuGame.getSudoku().isZero(gTR, gC) &&
                                                sudokuGame.getSudokuValidator().getCellCandidates(gTR, gC).contains(candidate)) {
                                            sudokuGame.removeCandidate(gTR, gC, candidate);
                                            changedHere = true;
                                        }
                                    }
                                }
                            }
                            if (changedHere) return true;
                        }
                    }
                }
            }

            Map<Integer, Set<Integer>> colsWithCand = new HashMap<>();
            for (int c = 0; c < gridSize; c++) {
                int gC = cOff + c;
                Set<Integer> rows = new HashSet<>();
                for (int r = 0; r < gridSize; r++) {
                    int gR = rOff + r;
                    if (sudokuGame.getSudoku().getBoard()[gR][gC] != null &&
                            sudokuGame.getSudoku().isZero(gR, gC) &&
                            sudokuGame.getSudokuValidator().getCellCandidates(gR, gC).contains(candidate)) {
                        rows.add(r);
                    }
                }
                if (rows.size() >= 2 && rows.size() <= 3) {
                    colsWithCand.put(c, rows);
                }
            }

            List<Integer> validCols = new ArrayList<>(colsWithCand.keySet());
            int cCount = validCols.size();
            for (int i = 0; i < cCount - 2; i++) {
                int c1 = validCols.get(i);
                for (int j = i + 1; j < cCount - 1; j++) {
                    int c2 = validCols.get(j);
                    for (int k = j + 1; k < cCount; k++) {
                        int c3 = validCols.get(k);

                        Set<Integer> combinedRows = new HashSet<>();
                        combinedRows.addAll(colsWithCand.get(c1));
                        combinedRows.addAll(colsWithCand.get(c2));
                        combinedRows.addAll(colsWithCand.get(c3));

                        if (combinedRows.size() == 3) {
                            boolean changedHere = false;
                            for (int targetCol = 0; targetCol < gridSize; targetCol++) {
                                if (targetCol != c1 && targetCol != c2 && targetCol != c3) {
                                    int gTC = cOff + targetCol;
                                    for (int row : combinedRows) {
                                        int gR = rOff + row;
                                        if (sudokuGame.getSudoku().getBoard()[gR][gTC] != null &&
                                                sudokuGame.getSudoku().isZero(gR, gTC) &&
                                                sudokuGame.getSudokuValidator().getCellCandidates(gR, gTC).contains(candidate)) {
                                            sudokuGame.removeCandidate(gR, gTC, candidate);
                                            changedHere = true;
                                        }
                                    }
                                }
                            }
                            if (changedHere) return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static double calculateDensityFactor(SudokuGame sudokuGame, double maxCandidates) {
        int candidatesCount = 0;
        ISudokuValidator validator = sudokuGame.getSudokuValidator();

        for (TargetedCell cell : validator.getEmptyCells()) {
            candidatesCount += validator.getCellCandidates(cell.getRow(), cell.getCol()).size();
        }

        return (candidatesCount / maxCandidates) * 20.0;
    }

    private static boolean canSee(int r1, int c1, int r2, int c2, List<List<TargetedCell>> allUnits) {
        if (r1 == r2 && c1 == c2) return false;

        for (List<TargetedCell> unit : allUnits) {
            boolean hasCell1 = false;
            boolean hasCell2 = false;

            for (TargetedCell cell : unit) {
                if (cell.getRow() == r1 && cell.getCol() == c1) hasCell1 = true;
                if (cell.getRow() == r2 && cell.getCol() == c2) hasCell2 = true;
            }

            if (hasCell1 && hasCell2) return true;
        }

        return false;
    }

    public static List<List<TargetedCell>> getAllUnits(SudokuGame sudokuGame) {
        List<List<TargetedCell>> allUnits = new ArrayList<>();
        ISudokuValidator baseValidator = sudokuGame.getSudokuValidator();

        if (baseValidator instanceof MultiSudokuValidator multiValidator) {
            for (SubGrid sub : multiValidator.getSubGrids()) {
                int subSize = sub.validator().getGridSize();

                for (int i = 0; i < subSize; i++) {
                    List<TargetedCell> row = new ArrayList<>();
                    List<TargetedCell> col = new ArrayList<>();
                    for (int j = 0; j < subSize; j++) {
                        row.add(new TargetedCell(sub.rowOffset() + i, sub.colOffset() + j));
                        col.add(new TargetedCell(sub.rowOffset() + j, sub.colOffset() + i));
                    }
                    allUnits.add(row);
                    allUnits.add(col);
                }
                extractConstraints(sub.validator(), allUnits, sub.rowOffset(), sub.colOffset());
            }
        } else {
            int gridSize = baseValidator.getGridSize();
            for (int i = 0; i < gridSize; i++) {
                List<TargetedCell> row = new ArrayList<>();
                List<TargetedCell> col = new ArrayList<>();
                for (int j = 0; j < gridSize; j++) {
                    row.add(new TargetedCell(i, j));
                    col.add(new TargetedCell(j, i));
                }
                allUnits.add(row);
                allUnits.add(col);
            }
            extractConstraints(baseValidator, allUnits, 0, 0);
        }
        return allUnits;
    }

    private static void extractConstraints(ISudokuValidator validator, List<List<TargetedCell>> allUnits, int rowOff, int colOff) {
        int gridSize = validator.getGridSize();
        for (Constraint c : validator.getConstraints()) {
            if (c instanceof UnitConstraint uc) {
                int numUnits = uc.getUnits().length;
                boolean isSelective = (c instanceof SelectiveUnitConstraint);

                for (int u = 0; u < numUnits; u++) {
                    int targetIndex = isSelective ? (u + 1) : u;
                    List<TargetedCell> unitCells = new ArrayList<>();

                    for (int row = 0; row < gridSize; row++) {
                        for (int col = 0; col < gridSize; col++) {
                            if (uc.getIndexFunction().index(row, col) == targetIndex) {
                                unitCells.add(new TargetedCell(rowOff + row, colOff + col));
                            }
                        }
                    }
                    if (!unitCells.isEmpty()) {
                        allUnits.add(unitCells);
                    }
                }
            }
        }
    }

    public static boolean applyNakedSingles(SudokuGame sudokuGame) {
        ISudokuValidator validator = sudokuGame.getSudokuValidator();

        for (TargetedCell cell : validator.getEmptyCells()) {
            var candidates = validator.getCellCandidates(cell.getRow(), cell.getCol());

            if (candidates.size() == 1) {
                int value = candidates.getFirst();
                sudokuGame.setNumber(cell.getRow(), cell.getCol(), value);
                return true;
            }
        }
        return false;
    }

    public static boolean applyHiddenSingles(SudokuGame sudokuGame) {
        ISudokuValidator validator = sudokuGame.getSudokuValidator();
        List<List<TargetedCell>> allUnits = getAllUnits(sudokuGame);

        int maxCandidate = (validator instanceof MultiSudokuValidator multi)
                ? multi.getSubGrids().getFirst().validator().getGridSize()
                : validator.getGridSize();

        for (List<TargetedCell> unit : allUnits) {
            for (int candidate = 1; candidate <= maxCandidate; candidate++) {
                TargetedCell targetCell = null;
                int count = 0;

                for (TargetedCell cell : unit) {
                    if (sudokuGame.getSudoku().isZero(cell.getRow(), cell.getCol()) &&
                            validator.getCellCandidates(cell.getRow(), cell.getCol()).contains(candidate)) {
                        count++;
                        targetCell = cell;
                    }
                }

                if (count == 1) {
                    sudokuGame.setNumber(targetCell.getRow(), targetCell.getCol(), candidate);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean applyNakedPairs(SudokuGame sudokuGame) {
        ISudokuValidator validator = sudokuGame.getSudokuValidator();
        List<List<TargetedCell>> allUnits = getAllUnits(sudokuGame);

        for (List<TargetedCell> unit : allUnits) {
            List<TargetedCell> pairsInUnit = new ArrayList<>();
            for (TargetedCell cell : unit) {
                if (sudokuGame.getSudoku().isZero(cell.getRow(), cell.getCol()) &&
                        validator.getCellCandidates(cell.getRow(), cell.getCol()).size() == 2) {
                    pairsInUnit.add(cell);
                }
            }

            for (int i = 0; i < pairsInUnit.size(); i++) {
                for (int j = i + 1; j < pairsInUnit.size(); j++) {
                    TargetedCell cellA = pairsInUnit.get(i);
                    TargetedCell cellB = pairsInUnit.get(j);

                    var candsA = validator.getCellCandidates(cellA.getRow(), cellA.getCol());
                    var candsB = validator.getCellCandidates(cellB.getRow(), cellB.getCol());

                    if (candsA.containsAll(candsB)) {
                        int cand1 = candsA.get(0);
                        int cand2 = candsA.get(1);
                        boolean changedThisPair = false;

                        for (TargetedCell otherCell : unit) {
                            if (sudokuGame.getSudoku().isZero(otherCell.getRow(), otherCell.getCol()) &&
                                    (otherCell.getRow() != cellA.getRow() || otherCell.getCol() != cellA.getCol()) &&
                                    (otherCell.getRow() != cellB.getRow() || otherCell.getCol() != cellB.getCol())) {

                                var otherCands = validator.getCellCandidates(otherCell.getRow(), otherCell.getCol());
                                if (otherCands.contains(cand1)) {
                                    sudokuGame.removeCandidate(otherCell.getRow(), otherCell.getCol(), cand1);
                                    changedThisPair = true;
                                }
                                if (otherCands.contains(cand2)) {
                                    sudokuGame.removeCandidate(otherCell.getRow(), otherCell.getCol(), cand2);
                                    changedThisPair = true;
                                }
                            }
                        }

                        if (changedThisPair) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean applyHiddenPairs(SudokuGame sudokuGame) {
        ISudokuValidator validator = sudokuGame.getSudokuValidator();
        List<List<TargetedCell>> allUnits = getAllUnits(sudokuGame);

        int maxCandidate = (validator instanceof MultiSudokuValidator multi)
                ? multi.getSubGrids().getFirst().validator().getGridSize()
                : validator.getGridSize();

        for (List<TargetedCell> unit : allUnits) {
            for (int cand1 = 1; cand1 <= maxCandidate; cand1++) {
                for (int cand2 = cand1 + 1; cand2 <= maxCandidate; cand2++) {

                    List<TargetedCell> cellsWithCands = new ArrayList<>();
                    for (TargetedCell cell : unit) {
                        if (sudokuGame.getSudoku().isZero(cell.getRow(), cell.getCol())) {
                            var cands = validator.getCellCandidates(cell.getRow(), cell.getCol());
                            if (cands.contains(cand1) || cands.contains(cand2)) {
                                cellsWithCands.add(cell);
                            }
                        }
                    }

                    if (cellsWithCands.size() == 2) {
                        TargetedCell c1 = cellsWithCands.get(0);
                        TargetedCell c2 = cellsWithCands.get(1);

                        var cands1 = validator.getCellCandidates(c1.getRow(), c1.getCol());
                        var cands2 = validator.getCellCandidates(c2.getRow(), c2.getCol());

                        if (cands1.contains(cand1) && cands1.contains(cand2) &&
                                cands2.contains(cand1) && cands2.contains(cand2)) {

                            boolean changedThisPair = false;
                            for (int c = 1; c <= maxCandidate; c++) {
                                if (c != cand1 && c != cand2) {
                                    if (cands1.contains(c)) {
                                        sudokuGame.removeCandidate(c1.getRow(), c1.getCol(), c);
                                        changedThisPair = true;
                                    }
                                    if (cands2.contains(c)) {
                                        sudokuGame.removeCandidate(c2.getRow(), c2.getCol(), c);
                                        changedThisPair = true;
                                    }
                                }
                            }
                            if (changedThisPair) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean applyPointingPairs(SudokuGame sudokuGame) {
        ISudokuValidator baseValidator = sudokuGame.getSudokuValidator();

        if (baseValidator instanceof MultiSudokuValidator multiValidator) {
            for (SubGrid sub : multiValidator.getSubGrids()) {
                if (applyPointingPairsLocal(sudokuGame, sub)) return true;
            }
            return false;
        } else {
            return applyPointingPairsLocal(sudokuGame, new SubGrid(0, 0, baseValidator));
        }
    }

    private static boolean applyPointingPairsLocal(SudokuGame sudokuGame, SubGrid subGrid) {
        ISudokuValidator validator = subGrid.validator();
        int gridSize = validator.getGridSize();
        int boxSize = (int) Math.sqrt(gridSize);
        int rOff = subGrid.rowOffset();
        int cOff = subGrid.colOffset();

        for (int candidate = 1; candidate <= gridSize; candidate++) {
            for (int boxRow = 0; boxRow < boxSize; boxRow++) {
                for (int boxCol = 0; boxCol < boxSize; boxCol++) {

                    int startR = boxRow * boxSize;
                    int startC = boxCol * boxSize;

                    Set<Integer> rows = new HashSet<>();
                    Set<Integer> cols = new HashSet<>();

                    for (int r = startR; r < startR + boxSize; r++) {
                        for (int c = startC; c < startC + boxSize; c++) {
                            int globalR = rOff + r;
                            int globalC = cOff + c;
                            if (sudokuGame.getSudoku().isZero(globalR, globalC) && sudokuGame.getSudokuValidator().getCellCandidates(globalR, globalC).contains(candidate)) {
                                rows.add(globalR);
                                cols.add(globalC);
                            }
                        }
                    }

                    if (rows.size() == 1) {
                        int targetRow = rows.iterator().next();
                        boolean changedHere = false;
                        for (int c = 0; c < gridSize; c++) {
                            int globalC = cOff + c;
                            if ((c < startC || c >= startC + boxSize) && sudokuGame.getSudoku().isZero(targetRow, globalC)) {
                                if (sudokuGame.getSudokuValidator().getCellCandidates(targetRow, globalC).contains(candidate)) {
                                    sudokuGame.removeCandidate(targetRow, globalC, candidate);
                                    changedHere = true;
                                }
                            }
                        }
                        if (changedHere) return true;
                    }

                    if (cols.size() == 1) {
                        int targetCol = cols.iterator().next();
                        boolean changedHere = false;
                        for (int r = 0; r < gridSize; r++) {
                            int globalR = rOff + r;
                            if ((r < startR || r >= startR + boxSize) && sudokuGame.getSudoku().isZero(globalR, targetCol)) {
                                if (sudokuGame.getSudokuValidator().getCellCandidates(globalR, targetCol).contains(candidate)) {
                                    sudokuGame.removeCandidate(globalR, targetCol, candidate);
                                    changedHere = true;
                                }
                            }
                        }
                        if (changedHere) return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean applyBoxLineReduction(SudokuGame sudokuGame) {
        ISudokuValidator baseValidator = sudokuGame.getSudokuValidator();

        if (baseValidator instanceof MultiSudokuValidator multiValidator) {
            for (SubGrid sub : multiValidator.getSubGrids()) {
                if (applyBoxLineReductionLocal(sudokuGame, sub)) return true;
            }
            return false;
        } else {
            return applyBoxLineReductionLocal(sudokuGame, new SubGrid(0, 0, baseValidator));
        }
    }

    private static boolean applyBoxLineReductionLocal(SudokuGame sudokuGame, SubGrid subGrid) {
        ISudokuValidator validator = subGrid.validator();
        int gridSize = validator.getGridSize();
        int rOff = subGrid.rowOffset();
        int cOff = subGrid.colOffset();

        for (int candidate = 1; candidate <= gridSize; candidate++) {

            for (int r = 0; r < gridSize; r++) {
                int globalR = rOff + r;
                Set<Integer> boxesInRow = new HashSet<>();

                for (int c = 0; c < gridSize; c++) {
                    int globalC = cOff + c;
                    if (sudokuGame.getSudoku().isZero(globalR, globalC) &&
                            validator.getCellCandidates(globalR, globalC).contains(candidate)) {

                        boxesInRow.add(sudokuGame.getBoxIdForCell(globalR, globalC));
                    }
                }

                if (boxesInRow.size() == 1) {
                    int boxId = boxesInRow.iterator().next();
                    boolean changedHere = false;

                    for (TargetedCell cell : sudokuGame.getCellsInBox(boxId)) {
                        if (cell.getRow() != globalR) {
                            if (sudokuGame.getSudoku().isZero(cell.getRow(), cell.getCol()) &&
                                    validator.getCellCandidates(cell.getRow(), cell.getCol()).contains(candidate)) {

                                sudokuGame.removeCandidate(cell.getRow(), cell.getCol(), candidate);
                                changedHere = true;
                            }
                        }
                    }
                    if (changedHere) return true;
                }
            }

            for (int c = 0; c < gridSize; c++) {
                int globalC = cOff + c;
                Set<Integer> boxesInCol = new HashSet<>();

                for (int r = 0; r < gridSize; r++) {
                    int globalR = rOff + r;
                    if (sudokuGame.getSudoku().isZero(globalR, globalC) &&
                            validator.getCellCandidates(globalR, globalC).contains(candidate)) {

                        boxesInCol.add(sudokuGame.getBoxIdForCell(globalR, globalC));
                    }
                }

                if (boxesInCol.size() == 1) {
                    int boxId = boxesInCol.iterator().next();
                    boolean changedHere = false;

                    for (TargetedCell cell : sudokuGame.getCellsInBox(boxId)) {
                        if (cell.getCol() != globalC) {
                            if (sudokuGame.getSudoku().isZero(cell.getRow(), cell.getCol()) &&
                                    validator.getCellCandidates(cell.getRow(), cell.getCol()).contains(candidate)) {

                                sudokuGame.removeCandidate(cell.getRow(), cell.getCol(), candidate);
                                changedHere = true;
                            }
                        }
                    }
                    if (changedHere) return true;
                }
            }
        }
        return false;
    }

    public static boolean applyNakedTriples(SudokuGame sudokuGame) {
        ISudokuValidator validator = sudokuGame.getSudokuValidator();
        List<List<TargetedCell>> allUnits = getAllUnits(sudokuGame);

        for (List<TargetedCell> unit : allUnits) {
            List<TargetedCell> candidates = new ArrayList<>();
            for (TargetedCell cell : unit) {
                if (sudokuGame.getSudoku().isZero(cell.getRow(), cell.getCol())) {
                    int size = validator.getCellCandidates(cell.getRow(), cell.getCol()).size();
                    if (size >= 2 && size <= 3) candidates.add(cell);
                }
            }

            for (int i = 0; i < candidates.size(); i++) {
                for (int j = i + 1; j < candidates.size(); j++) {
                    for (int k = j + 1; k < candidates.size(); k++) {
                        Set<Integer> tripleCands = new HashSet<>();
                        tripleCands.addAll(validator.getCellCandidates(candidates.get(i).getRow(), candidates.get(i).getCol()));
                        tripleCands.addAll(validator.getCellCandidates(candidates.get(j).getRow(), candidates.get(j).getCol()));
                        tripleCands.addAll(validator.getCellCandidates(candidates.get(k).getRow(), candidates.get(k).getCol()));

                        if (tripleCands.size() == 3) {
                            boolean changedThisTriple = false;
                            for (TargetedCell cell : unit) {
                                if (!cell.equals(candidates.get(i)) && !cell.equals(candidates.get(j)) && !cell.equals(candidates.get(k))) {
                                    for (int cand : tripleCands) {
                                        if (validator.getCellCandidates(cell.getRow(), cell.getCol()).contains(cand)) {
                                            sudokuGame.removeCandidate(cell.getRow(), cell.getCol(), cand);
                                            changedThisTriple = true;
                                        }
                                    }
                                }
                            }
                            if (changedThisTriple) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean applyHiddenTriples(SudokuGame sudokuGame) {
        ISudokuValidator validator = sudokuGame.getSudokuValidator();
        List<List<TargetedCell>> allUnits = getAllUnits(sudokuGame);

        int maxCandidate = (validator instanceof MultiSudokuValidator multi)
                ? multi.getSubGrids().getFirst().validator().getGridSize()
                : validator.getGridSize();

        for (List<TargetedCell> unit : allUnits) {
            for (int c1 = 1; c1 <= maxCandidate; c1++) {
                for (int c2 = c1 + 1; c2 <= maxCandidate; c2++) {
                    for (int c3 = c2 + 1; c3 <= maxCandidate; c3++) {

                        List<TargetedCell> cellsWithTheseCands = new ArrayList<>();
                        for (TargetedCell cell : unit) {
                            if (sudokuGame.getSudoku().isZero(cell.getRow(), cell.getCol())) {
                                var cands = validator.getCellCandidates(cell.getRow(), cell.getCol());
                                if (cands.contains(c1) || cands.contains(c2) || cands.contains(c3)) {
                                    cellsWithTheseCands.add(cell);
                                }
                            }
                        }

                        if (cellsWithTheseCands.size() == 3) {
                            boolean changedThisTriple = false;
                            for (TargetedCell cell : cellsWithTheseCands) {
                                var cands = validator.getCellCandidates(cell.getRow(), cell.getCol());
                                for (int cand = 1; cand <= maxCandidate; cand++) {
                                    if (cand != c1 && cand != c2 && cand != c3 && cands.contains(cand)) {
                                        sudokuGame.removeCandidate(cell.getRow(), cell.getCol(), cand);
                                        changedThisTriple = true;
                                    }
                                }
                            }
                            if (changedThisTriple) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean applyXWing(SudokuGame sudokuGame) {
        ISudokuValidator baseValidator = sudokuGame.getSudokuValidator();

        if (baseValidator instanceof MultiSudokuValidator multiValidator) {
            for (SubGrid sub : multiValidator.getSubGrids()) {
                if (applyXWingLocal(sudokuGame, sub)) return true;
            }
            return false;
        } else {
            return applyXWingLocal(sudokuGame, new SubGrid(0, 0, baseValidator));
        }
    }

    private static boolean applyXWingLocal(SudokuGame sudokuGame, SubGrid subGrid) {
        ISudokuValidator validator = subGrid.validator();
        int gridSize = validator.getGridSize();
        int rOff = subGrid.rowOffset();
        int cOff = subGrid.colOffset();

        for (int candidate = 1; candidate <= gridSize; candidate++) {
            Map<Integer, List<Integer>> rowsWithCandidate = new HashMap<>();
            for (int row = 0; row < gridSize; row++) {
                int gR = rOff + row;
                List<Integer> possibleCols = new ArrayList<>();
                for (int col = 0; col < gridSize; col++) {
                    int gC = cOff + col;
                    if (sudokuGame.getSudoku().getBoard()[gR][gC] != null &&
                            sudokuGame.getSudoku().isZero(gR, gC) &&
                            sudokuGame.getSudokuValidator().getCellCandidates(gR, gC).contains(candidate)) {
                        possibleCols.add(col);
                    }
                }
                if (possibleCols.size() == 2) rowsWithCandidate.put(row, possibleCols);
            }

            List<Integer> matchingRows = new ArrayList<>(rowsWithCandidate.keySet());
            for (int i = 0; i < matchingRows.size(); i++) {
                for (int j = i + 1; j < matchingRows.size(); j++) {
                    int rowA = matchingRows.get(i);
                    int rowB = matchingRows.get(j);
                    List<Integer> colsA = rowsWithCandidate.get(rowA);

                    if (colsA.equals(rowsWithCandidate.get(rowB))) {
                        boolean changedHere = false;
                        for (int targetRow = 0; targetRow < gridSize; targetRow++) {
                            if (targetRow != rowA && targetRow != rowB) {
                                int gTR = rOff + targetRow;
                                int gC0 = cOff + colsA.get(0);
                                int gC1 = cOff + colsA.get(1);

                                if (sudokuGame.getSudoku().getBoard()[gTR][gC0] != null &&
                                        sudokuGame.getSudoku().isZero(gTR, gC0) &&
                                        sudokuGame.getSudokuValidator().getCellCandidates(gTR, gC0).contains(candidate)) {
                                    sudokuGame.removeCandidate(gTR, gC0, candidate);
                                    changedHere = true;
                                }
                                if (sudokuGame.getSudoku().getBoard()[gTR][gC1] != null &&
                                        sudokuGame.getSudoku().isZero(gTR, gC1) &&
                                        sudokuGame.getSudokuValidator().getCellCandidates(gTR, gC1).contains(candidate)) {
                                    sudokuGame.removeCandidate(gTR, gC1, candidate);
                                    changedHere = true;
                                }
                            }
                        }
                        if (changedHere) return true;
                    }
                }
            }

            Map<Integer, List<Integer>> colsWithCandidate = new HashMap<>();
            for (int col = 0; col < gridSize; col++) {
                int gC = cOff + col;
                List<Integer> possibleRows = new ArrayList<>();
                for (int row = 0; row < gridSize; row++) {
                    int gR = rOff + row;
                    if (sudokuGame.getSudoku().getBoard()[gR][gC] != null &&
                            sudokuGame.getSudoku().isZero(gR, gC) &&
                            sudokuGame.getSudokuValidator().getCellCandidates(gR, gC).contains(candidate)) {
                        possibleRows.add(row);
                    }
                }
                if (possibleRows.size() == 2) colsWithCandidate.put(col, possibleRows);
            }

            List<Integer> matchingCols = new ArrayList<>(colsWithCandidate.keySet());
            for (int i = 0; i < matchingCols.size(); i++) {
                for (int j = i + 1; j < matchingCols.size(); j++) {
                    int colA = matchingCols.get(i);
                    int colB = matchingCols.get(j);
                    List<Integer> rowsA = colsWithCandidate.get(colA);

                    if (rowsA.equals(colsWithCandidate.get(colB))) {
                        boolean changedHere = false;
                        for (int targetCol = 0; targetCol < gridSize; targetCol++) {
                            if (targetCol != colA && targetCol != colB) {
                                int gTC = cOff + targetCol;
                                int gR0 = rOff + rowsA.get(0);
                                int gR1 = rOff + rowsA.get(1);

                                if (sudokuGame.getSudoku().getBoard()[gR0][gTC] != null &&
                                        sudokuGame.getSudoku().isZero(gR0, gTC) &&
                                        sudokuGame.getSudokuValidator().getCellCandidates(gR0, gTC).contains(candidate)) {
                                    sudokuGame.removeCandidate(gR0, gTC, candidate);
                                    changedHere = true;
                                }
                                if (sudokuGame.getSudoku().getBoard()[gR1][gTC] != null &&
                                        sudokuGame.getSudoku().isZero(gR1, gTC) &&
                                        sudokuGame.getSudokuValidator().getCellCandidates(gR1, gTC).contains(candidate)) {
                                    sudokuGame.removeCandidate(gR1, gTC, candidate);
                                    changedHere = true;
                                }
                            }
                        }
                        if (changedHere) return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean applyXYWing(SudokuGame sudokuGame) {
        ISudokuValidator validator = sudokuGame.getSudokuValidator();
        List<TargetedCell> emptyCells = validator.getEmptyCells();
        List<List<TargetedCell>> allUnits = getAllUnits(sudokuGame);

        List<TargetedCell> bivalueCells = new ArrayList<>();
        for (TargetedCell cell : emptyCells) {
            if (validator.getCellCandidates(cell.getRow(), cell.getCol()).size() == 2) {
                bivalueCells.add(cell);
            }
        }

        for (TargetedCell pivot : bivalueCells) {
            List<Integer> pivotCands = validator.getCellCandidates(pivot.getRow(), pivot.getCol());
            int A = pivotCands.get(0);
            int B = pivotCands.get(1);

            List<TargetedCell> wings = new ArrayList<>();
            for (TargetedCell wing : bivalueCells) {
                if (wing.equals(pivot)) continue;
                if (canSee(pivot.getRow(), pivot.getCol(), wing.getRow(), wing.getCol(), allUnits)) {
                    List<Integer> wingCands = validator.getCellCandidates(wing.getRow(), wing.getCol());
                    if (wingCands.size() == 2 && ((wingCands.contains(A) && !wingCands.contains(B)) || (!wingCands.contains(A) && wingCands.contains(B)))) {
                        wings.add(wing);
                    }
                }
            }

            for (int i = 0; i < wings.size(); i++) {
                for (int j = i + 1; j < wings.size(); j++) {
                    TargetedCell w1 = wings.get(i);
                    TargetedCell w2 = wings.get(j);

                    if (canSee(w1.getRow(), w1.getCol(), w2.getRow(), w2.getCol(), allUnits)) continue;

                    List<Integer> w1Cands = validator.getCellCandidates(w1.getRow(), w1.getCol());
                    List<Integer> w2Cands = validator.getCellCandidates(w2.getRow(), w2.getCol());

                    int sharedZ = -1;
                    for (int cand : w1Cands) {
                        if (cand != A && cand != B && w2Cands.contains(cand)) {
                            sharedZ = cand;
                            break;
                        }
                    }

                    if (sharedZ != -1) {
                        boolean changed = false;
                        for (TargetedCell target : emptyCells) {
                            if (target.equals(pivot) || target.equals(w1) || target.equals(w2)) continue;

                            if (canSee(target.getRow(), target.getCol(), w1.getRow(), w1.getCol(), allUnits) &&
                                    canSee(target.getRow(), target.getCol(), w2.getRow(), w2.getCol(), allUnits)) {

                                if (validator.getCellCandidates(target.getRow(), target.getCol()).contains(sharedZ)) {
                                    sudokuGame.removeCandidate(target.getRow(), target.getCol(), sharedZ);
                                    changed = true;
                                }
                            }
                        }
                        if (changed) {

                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean applyAvoidableRectangle(SudokuGame sudokuGame) {
        ISudokuValidator validator = sudokuGame.getSudokuValidator();
        int gridSize = validator.getGridSize();
        int boxSize = (int) Math.sqrt(gridSize);
        if (boxSize * boxSize != gridSize) return false;

        for (int r1 = 0; r1 < gridSize; r1++) {
            for (int r2 = r1 + 1; r2 < gridSize; r2++) {
                for (int c1 = 0; c1 < gridSize; c1++) {
                    for (int c2 = c1 + 1; c2 < gridSize; c2++) {

                        if ((r1 / boxSize == r2 / boxSize) == (c1 / boxSize == c2 / boxSize)) continue;

                        TargetedCell[] corners = {
                                new TargetedCell(r1, c1), new TargetedCell(r1, c2),
                                new TargetedCell(r2, c1), new TargetedCell(r2, c2)
                        };

                        List<TargetedCell> emptyCorners = new ArrayList<>();
                        List<TargetedCell> filledCorners = new ArrayList<>();

                        for (TargetedCell c : corners) {
                            if (sudokuGame.getSudoku().isZero(c.getRow(), c.getCol())) {
                                emptyCorners.add(c);
                            } else {
                                filledCorners.add(c);
                            }
                        }

                        if (emptyCorners.size() == 1) {
                            TargetedCell empty = emptyCorners.get(0);
                            int valR1C1 = sudokuGame.getSudoku().getBoard()[r1][c1].getValue();
                            int valR1C2 = sudokuGame.getSudoku().getBoard()[r1][c2].getValue();
                            int valR2C1 = sudokuGame.getSudoku().getBoard()[r2][c1].getValue();
                            int valR2C2 = sudokuGame.getSudoku().getBoard()[r2][c2].getValue();

                            int requiredVal = -1;

                            if (empty.getRow() == r1 && empty.getCol() == c1) {
                                if (valR1C2 == valR2C1 && valR2C2 != 0 && valR2C2 != valR1C2) requiredVal = valR2C2;
                            } else if (empty.getRow() == r1 && empty.getCol() == c2) {
                                if (valR1C1 == valR2C2 && valR2C1 != 0 && valR2C1 != valR1C1) requiredVal = valR2C1;
                            } else if (empty.getRow() == r2 && empty.getCol() == c1) {
                                if (valR1C1 == valR2C2 && valR1C2 != 0 && valR1C2 != valR1C1) requiredVal = valR1C2;
                            } else if (empty.getRow() == r2 && empty.getCol() == c2) {
                                if (valR1C2 == valR2C1 && valR1C1 != 0 && valR1C1 != valR1C2) requiredVal = valR1C1;
                            }

                            if (requiredVal != -1 && validator.getCellCandidates(empty.getRow(), empty.getCol()).contains(requiredVal)) {
                                sudokuGame.removeCandidate(empty.getRow(), empty.getCol(), requiredVal);

                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean applyXVSingles(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || !modifiers.hasXv()) return false;

        var validator = sudokuGame.getSudokuValidator();

        for (var mark : modifiers.getXv().marks()) {
            int targetSum = (mark.markType() == MarkType.X) ? 10 : 5;

            TargetedCell c1 = new TargetedCell(mark.first().row(), mark.first().col());
            TargetedCell c2 = new TargetedCell(mark.second().row(), mark.second().col());

            int val1 = sudokuGame.getSudoku().getBoard()[c1.getRow()][c1.getCol()].getValue();
            int val2 = sudokuGame.getSudoku().getBoard()[c2.getRow()][c2.getCol()].getValue();

            if (tryApplyXv(sudokuGame, validator, targetSum, c2, val1, val2)) return true;
            if (tryApplyXv(sudokuGame, validator, targetSum, c1, val2, val1)) return true;
        }
        return false;
    }

    private static boolean tryApplyXv(SudokuGame sudokuGame, ISudokuValidator validator, int targetSum, TargetedCell c2, int val1, int val2) {
        if (val1 != 0 && val2 == 0) {
            int required = targetSum - val1;
            if (validator.getCellCandidates(c2.getRow(), c2.getCol()).contains(required)) {
                sudokuGame.setNumber(c2.getRow(), c2.getCol(), required);
                return true;
            }
        }
        return false;
    }

    private static boolean processSkyscraperDirection(SudokuGame game, int clue, int startR, int startC, int dr, int dc, int size) {
        if (game.getSudoku().getBoard()[startR][startC] == null) return false;

        if (clue == 1) {
            if (game.getSudoku().isZero(startR, startC)) {
                if (game.getSudokuValidator().getCellCandidates(startR, startC).contains(size)) {
                    game.setNumber(startR, startC, size);
                    return true;
                }
            }
        } else if (clue == size) {
            int r = startR;
            int c = startC;
            for (int val = 1; val <= size; val++) {
                if (game.getSudoku().getBoard()[r][c] != null && game.getSudoku().isZero(r, c) && game.getSudokuValidator().getCellCandidates(r, c).contains(val)) {
                    game.setNumber(r, c, val);
                    return true;
                }
                r += dr;
                c += dc;
            }
        }
        return false;
    }

    private static boolean applySkyscraperBasics(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || !modifiers.hasSkyscraper()) return false;

        int size = sudokuGame.getSudoku().getType().getGridSize();
        var sky = modifiers.getSkyscraper();

        int[] top = sky.top();
        int[] bot = sky.bottom();
        int[] left = sky.left();
        int[] right = sky.right();

        for (int i = 0; i < size; i++) {
            if (top != null && processSkyscraperDirection(sudokuGame, top[i], 0, i, 1, 0, size)) return true;
            if (bot != null && processSkyscraperDirection(sudokuGame, bot[i], size - 1, i, -1, 0, size)) return true;
            if (left != null && processSkyscraperDirection(sudokuGame, left[i], i, 0, 0, 1, size)) return true;
            if (right != null && processSkyscraperDirection(sudokuGame, right[i], i, size - 1, 0, -1, size))
                return true;
        }
        return false;
    }

    private static boolean applyEvenOddBasics(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || !modifiers.hasEvenOdd()) return false;
        var validator = sudokuGame.getSudokuValidator();
        int size = validator.getGridSize();

        ParityType[][] parityTypes = modifiers.getEvenOdd().parityTypes();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (sudokuGame.getSudoku().getBoard()[r][c] == null || !sudokuGame.getSudoku().isZero(r, c)) continue;

                ParityType pt = parityTypes[r][c];
                if (pt == ParityType.NONE) continue;

                List<Integer> cands = validator.getCellCandidates(r, c);
                int validCount = 0;
                int lastValid = -1;

                for (int cand : cands) {
                    if ((pt == ParityType.EVEN && cand % 2 == 0) || (pt == ParityType.ODD && cand % 2 != 0)) {
                        validCount++;
                        lastValid = cand;
                    }
                }

                if (validCount == 1) {
                    sudokuGame.setNumber(r, c, lastValid);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean applyConsecutiveBasics(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || !modifiers.hasConsecutive()) return false;

        var validator = sudokuGame.getSudokuValidator();
        int maxVal = (validator instanceof MultiSudokuValidator multi)
                ? multi.getSubGrids().getFirst().validator().getGridSize()
                : validator.getGridSize();

        for (var pair : modifiers.getConsecutive().cells()) {
            int r1 = pair.getFirst().row();
            int c1 = pair.getFirst().col();
            int r2 = pair.getSecond().row();
            int c2 = pair.getSecond().col();

            int v1 = sudokuGame.getSudoku().getBoard()[r1][c1].getValue();
            int v2 = sudokuGame.getSudoku().getBoard()[r2][c2].getValue();

            if (v1 != 0 && v2 == 0) {
                if (trySetConsecutiveEdge(sudokuGame, r1, c1, r2, c2, maxVal)) return true;
            } else if (v2 != 0 && v1 == 0) {
                if (trySetConsecutiveEdge(sudokuGame, r2, c2, r1, c1, maxVal)) return true;
            }
        }
        return false;
    }

    private static boolean trySetConsecutiveEdge(SudokuGame game, int rKnown, int cKnown, int rTarget, int cTarget, int size) {
        int knownVal = game.getSudoku().getBoard()[rKnown][cKnown].getValue();

        if (knownVal == 1 || knownVal == size) {
            int required = (knownVal == 1) ? 2 : size - 1;
            if (game.getSudokuValidator().getCellCandidates(rTarget, cTarget).contains(required)) {
                game.setNumber(rTarget, cTarget, required);
                return true;
            }
        }
        return false;
    }

    private static boolean applyGreaterThanBasics(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || !modifiers.hasGreaterThan()) return false;
        var validator = sudokuGame.getSudokuValidator();
        int size = validator.getGridSize();

        int maxVal = (validator instanceof MultiSudokuValidator multi)
                ? multi.getSubGrids().getFirst().validator().getGridSize()
                : size;

        java.util.function.BiFunction<int[], Integer, Boolean> checkAndSet = (coords, val) -> {
            if (validator.getCellCandidates(coords[0], coords[1]).contains(val)) {
                sudokuGame.setNumber(coords[0], coords[1], val);
                return true;
            }
            return false;
        };

        var horiz = modifiers.getGreaterThan().horizontal();
        if (horiz != null) {
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size - 1; c++) {
                    if (sudokuGame.getSudoku().getBoard()[r][c] == null || sudokuGame.getSudoku().getBoard()[r][c + 1] == null)
                        continue;
                    CompType type = horiz[r][c];
                    if (type == null) continue;
                    int leftVal = sudokuGame.getSudoku().getBoard()[r][c].getValue();
                    int rightVal = sudokuGame.getSudoku().getBoard()[r][c + 1].getValue();

                    if (leftVal != 0 && rightVal == 0) {
                        if (type == CompType.BIGGER && leftVal == 2)
                            if (checkAndSet.apply(new int[]{r, c + 1}, 1)) return true;
                        if (type == CompType.SMALLER && leftVal == maxVal - 1)
                            if (checkAndSet.apply(new int[]{r, c + 1}, maxVal)) return true;
                    } else if (rightVal != 0 && leftVal == 0) {
                        if (type == CompType.BIGGER && rightVal == maxVal - 1)
                            if (checkAndSet.apply(new int[]{r, c}, maxVal)) return true;
                        if (type == CompType.SMALLER && rightVal == 2)
                            if (checkAndSet.apply(new int[]{r, c}, 1)) return true;
                    }
                }
            }
        }

        var vert = modifiers.getGreaterThan().vertical();
        if (vert != null) {
            for (int r = 0; r < size - 1; r++) {
                for (int c = 0; c < size; c++) {
                    if (sudokuGame.getSudoku().getBoard()[r][c] == null || sudokuGame.getSudoku().getBoard()[r + 1][c] == null)
                        continue;
                    CompType type = vert[r][c];
                    if (type == null) continue;
                    int topVal = sudokuGame.getSudoku().getBoard()[r][c].getValue();
                    int botVal = sudokuGame.getSudoku().getBoard()[r + 1][c].getValue();

                    if (topVal != 0 && botVal == 0) {
                        if (type == CompType.BIGGER && topVal == 2)
                            if (checkAndSet.apply(new int[]{r + 1, c}, 1)) return true;
                        if (type == CompType.SMALLER && topVal == maxVal - 1)
                            if (checkAndSet.apply(new int[]{r + 1, c}, maxVal)) return true;
                    } else if (botVal != 0 && topVal == 0) {
                        if (type == CompType.BIGGER && botVal == maxVal - 1)
                            if (checkAndSet.apply(new int[]{r, c}, maxVal)) return true;
                        if (type == CompType.SMALLER && botVal == 2)
                            if (checkAndSet.apply(new int[]{r, c}, 1)) return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean applyBetweenBasics(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || !modifiers.hasBetween()) return false;
        var validator = sudokuGame.getSudokuValidator();

        for (var line : modifiers.getBetween().lines()) {
            int v1 = sudokuGame.getSudoku().getBoard()[line.startCircle().row()][line.startCircle().col()].getValue();
            int v2 = sudokuGame.getSudoku().getBoard()[line.endCircle().row()][line.endCircle().col()].getValue();

            if (v1 != 0 && v2 != 0) {
                int min = Math.min(v1, v2);
                int max = Math.max(v1, v2);

                if (max - min == 2 && line.lineCells().size() == 1) {
                    var target = line.lineCells().getFirst();
                    if (sudokuGame.getSudoku().isZero(target.row(), target.col())) {
                        int required = min + 1;
                        if (validator.getCellCandidates(target.row(), target.col()).contains(required)) {
                            sudokuGame.setNumber(target.row(), target.col(), required);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean applySandwichBasics(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || !modifiers.hasSandwich()) return false;

        int size = sudokuGame.getSudoku().getType().getGridSize();
        int maxSum = IntStream.rangeClosed(2, size - 1).sum();
        var sand = modifiers.getSandwich();

        if (sand.left() != null) {
            int[] leftClues = sand.left();
            for (int i = 0; i < size; i++) {
                if (leftClues[i] == maxSum) {
                    if (trySetSandwichEdge(sudokuGame, i, 0, i, size - 1, size)) return true;
                }
            }
        }

        if (sand.top() != null) {
            int[] topClues = sand.top();
            for (int i = 0; i < size; i++) {
                if (topClues[i] == maxSum) {
                    if (trySetSandwichEdge(sudokuGame, 0, i, size - 1, i, size)) return true;
                }
            }
        }

        return false;
    }

    private static boolean trySetSandwichEdge(SudokuGame game, int r1, int c1, int r2, int c2, int size) {
        if (game.getSudoku().getBoard()[r1][c1] == null || game.getSudoku().getBoard()[r2][c2] == null) return false;

        int first = game.getSudoku().getBoard()[r1][c1].getValue();
        int second = game.getSudoku().getBoard()[r2][c2].getValue();

        if ((first == 0) == (second == 0)) return false;

        int existing, r, c;
        if (first != 0) {
            existing = first;
            r = r2;
            c = c2;
        } else {
            existing = second;
            r = r1;
            c = c1;
        }

        if (existing == 1 || existing == size) {
            int required = (existing == 1) ? size : 1;

            if (game.getSudokuValidator().getCellCandidates(r, c).contains(required)) {
                game.setNumber(r, c, required);
                return true;
            }
        }
        return false;
    }

    private static boolean applyVudokuBasics(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || !modifiers.hasVudoku()) return false;
        var validator = sudokuGame.getSudokuValidator();

        for (var mark : modifiers.getVudoku().marks()) {
            int vertex = sudokuGame.getSudoku().getBoard()[mark.vertex().row()][mark.vertex().col()].getValue();
            int arm1 = sudokuGame.getSudoku().getBoard()[mark.arm1().row()][mark.arm1().col()].getValue();
            int arm2 = sudokuGame.getSudoku().getBoard()[mark.arm2().row()][mark.arm2().col()].getValue();

            if (arm1 != 0 && arm2 != 0 && vertex == 0) {
                int sum = arm1 + arm2;
                int diff = Math.abs(arm1 - arm2);
                var cands = validator.getCellCandidates(mark.vertex().row(), mark.vertex().col());

                boolean sumCandidate = cands.contains(sum);
                boolean diffCandidate = cands.contains(diff);

                if (sumCandidate && !diffCandidate) {
                    sudokuGame.setNumber(mark.vertex().row(), mark.vertex().col(), sum);
                    return true;
                }
                if (diffCandidate && !sumCandidate) {
                    sudokuGame.setNumber(mark.vertex().row(), mark.vertex().col(), diff);
                    return true;
                }
            }

            if (arm2 == 0 && trySetVudokuArm(sudokuGame, vertex, arm1, mark.arm2().row(), mark.arm2().col()))
                return true;

            if (arm1 == 0 && trySetVudokuArm(sudokuGame, vertex, arm2, mark.arm1().row(), mark.arm1().col()))
                return true;
        }
        return false;
    }

    private static boolean trySetVudokuArm(SudokuGame game, int vVert, int knownArmVal, int targetRow, int targetCol) {
        if (vVert == 0 || knownArmVal == 0) return false;

        int pos1 = vVert - knownArmVal;
        int pos2 = knownArmVal - vVert;
        int pos3 = knownArmVal + vVert;

        var cands = game.getSudokuValidator().getCellCandidates(targetRow, targetCol);
        int validCount = 0;
        int lastValid = -1;

        if (pos1 > 0 && cands.contains(pos1)) {
            validCount++;
            lastValid = pos1;
        }
        if (pos2 > 0 && pos2 != pos1 && cands.contains(pos2)) {
            validCount++;
            lastValid = pos2;
        }
        if (pos3 > 0 && pos3 != pos1 && pos3 != pos2 && cands.contains(pos3)) {
            validCount++;
            lastValid = pos3;
        }

        if (validCount == 1) {
            game.setNumber(targetRow, targetCol, lastValid);
            return true;
        }
        return false;
    }

    private static boolean applyKropkiBasics(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || modifiers.getKropki() == null) return false;

        var validator = sudokuGame.getSudokuValidator();
        int maxVal = (validator instanceof MultiSudokuValidator multi)
                ? multi.getSubGrids().getFirst().validator().getGridSize()
                : validator.getGridSize();

        for (var dot : modifiers.getKropki().dots()) {
            int r1 = dot.first().row();
            int c1 = dot.first().col();
            int r2 = dot.second().row();
            int c2 = dot.second().col();

            int v1 = sudokuGame.getSudoku().getBoard()[r1][c1].getValue();
            int v2 = sudokuGame.getSudoku().getBoard()[r2][c2].getValue();

            if (v1 != 0 && v2 == 0) {
                if (trySetKropkiDot(sudokuGame, r1, c1, r2, c2, maxVal, dot.color())) return true;
            } else if (v2 != 0 && v1 == 0) {
                if (trySetKropkiDot(sudokuGame, r2, c2, r1, c1, maxVal, dot.color())) return true;
            }
        }
        return false;
    }

    private static boolean trySetKropkiDot(SudokuGame game, int rKnown, int cKnown, int rTarget, int cTarget, int size, DotColor color) {
        int knownVal = game.getSudoku().getBoard()[rKnown][cKnown].getValue();
        var cands = game.getSudokuValidator().getCellCandidates(rTarget, cTarget);

        List<Integer> allowedValues = new ArrayList<>();

        if (color == DotColor.WHITE) {
            if (knownVal - 1 >= 1) allowedValues.add(knownVal - 1);
            if (knownVal + 1 <= size) allowedValues.add(knownVal + 1);
        } else {
            if (knownVal * 2 <= size) allowedValues.add(knownVal * 2);
            if (knownVal % 2 == 0) allowedValues.add(knownVal / 2);
        }

        int validCount = 0;
        int lastValid = -1;

        for (int val : allowedValues) {
            if (cands.contains(val)) {
                validCount++;
                lastValid = val;
            }
        }

        if (validCount == 1) {
            game.setNumber(rTarget, cTarget, lastValid);
            return true;
        }
        return false;
    }

    private static boolean applyGroupSumsBasics(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || !modifiers.hasGroupSums()) return false;
        var validator = sudokuGame.getSudokuValidator();

        for (var mark : modifiers.getGroupSums().marks()) {
            int targetSum = mark.targetSum();
            var topLeft = mark.topLeft();
            int r = topLeft.row();
            int c = topLeft.col();

            int currentSum = 0;
            int emptyCount = 0;
            GridCell emptyCell = null;

            for (int dr = 0; dr <= 1; dr++) {
                for (int dc = 0; dc <= 1; dc++) {
                    int val = sudokuGame.getSudoku().getBoard()[r + dr][c + dc].getValue();
                    if (val == 0) {
                        emptyCount++;
                        emptyCell = new GridCell(r + dr, c + dc);
                    } else {
                        currentSum += val;
                    }
                }
            }

            if (emptyCount == 1) {
                int required = targetSum - currentSum;
                if (required > 0 && validator.getCellCandidates(emptyCell.row(), emptyCell.col()).contains(required)) {
                    sudokuGame.setNumber(emptyCell.row(), emptyCell.col(), required);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean applyQuadruplesBasics(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || !modifiers.hasQuadruples()) return false;
        var validator = sudokuGame.getSudokuValidator();

        for (var mark : modifiers.getQuadruples().marks()) {
            var topLeft = mark.topLeft();
            int r = topLeft.row();
            int c = topLeft.col();

            int emptyCount = 0;
            GridCell emptyCell = null;
            List<Integer> placed = new ArrayList<>();

            for (int dr = 0; dr <= 1; dr++) {
                for (int dc = 0; dc <= 1; dc++) {
                    int val = sudokuGame.getSudoku().getBoard()[r + dr][c + dc].getValue();
                    if (val == 0) {
                        emptyCount++;
                        emptyCell = new GridCell(r + dr, c + dc);
                    } else {
                        placed.add(val);
                    }
                }
            }

            if (emptyCount == 1) {
                List<Integer> required = new ArrayList<>(mark.values());
                for (int p : placed) {
                    required.remove((Integer) p);
                }

                if (required.size() == 1) {
                    int reqVal = required.getFirst();
                    if (validator.getCellCandidates(emptyCell.row(), emptyCell.col()).contains(reqVal)) {
                        sudokuGame.setNumber(emptyCell.row(), emptyCell.col(), reqVal);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean applyKillerAdvanced(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || !modifiers.hasKiller()) return false;
        var validator = sudokuGame.getSudokuValidator();
        int gridSize = validator.getGridSize();

        for (var cage : modifiers.getKiller().cages()) {
            List<GridCell> emptyCells = new ArrayList<>();
            int currentSum = 0;
            boolean[] usedInCage = new boolean[gridSize + 1];

            for (var cell : cage.cells()) {
                int val = sudokuGame.getSudoku().getBoard()[cell.row()][cell.col()].getValue();
                if (val == 0) {
                    emptyCells.add(cell);
                } else {
                    currentSum += val;
                    usedInCage[val] = true;
                }
            }

            int remainingSum = cage.targetSum() - currentSum;
            if (emptyCells.size() <= 1 || remainingSum <= 0) continue;

            for (var cell : emptyCells) {
                for (int cand : validator.getCellCandidates(cell.row(), cell.col())) {
                    if (usedInCage[cand]) {
                        sudokuGame.removeCandidate(cell.row(), cell.col(), cand);
                        return true;
                    }
                }
            }

            for (var targetCell : emptyCells) {
                var candidates = new ArrayList<>(validator.getCellCandidates(targetCell.row(), targetCell.col()));

                for (int cand : candidates) {
                    usedInCage[cand] = true;
                    boolean validComboExists = hasValidKillerCombo(emptyCells, 0, targetCell, remainingSum - cand, usedInCage, validator);
                    usedInCage[cand] = false;

                    if (!validComboExists) {
                        sudokuGame.removeCandidate(targetCell.row(), targetCell.col(), cand);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasValidKillerCombo(List<GridCell> emptyCells, int index,
                                               GridCell targetCell, int targetSum,
                                               boolean[] used, ISudokuValidator validator) {
        if (index == emptyCells.size()) {
            return targetSum == 0;
        }

        GridCell cell = emptyCells.get(index);
        if (cell.equals(targetCell)) {
            return hasValidKillerCombo(emptyCells, index + 1, targetCell, targetSum, used, validator);
        }

        for (int cand : validator.getCellCandidates(cell.row(), cell.col())) {
            if (!used[cand] && targetSum - cand >= 0) {
                used[cand] = true;
                if (hasValidKillerCombo(emptyCells, index + 1, targetCell, targetSum - cand, used, validator)) {
                    used[cand] = false;
                    return true;
                }
                used[cand] = false;
            }
        }
        return false;
    }

    private static boolean applyKillerBasics(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || !modifiers.hasKiller()) return false;
        var validator = sudokuGame.getSudokuValidator();

        for (var cage : modifiers.getKiller().cages()) {
            int targetSum = cage.targetSum();
            int currentSum = 0;
            int emptyCount = 0;
            GridCell emptyCell = null;

            for (var cell : cage.cells()) {
                int val = sudokuGame.getSudoku().getBoard()[cell.row()][cell.col()].getValue();
                if (val == 0) {
                    emptyCount++;
                    emptyCell = cell;
                } else {
                    currentSum += val;
                }
            }

            if (emptyCount == 1) {
                int required = targetSum - currentSum;
                if (required > 0 && validator.getCellCandidates(emptyCell.row(), emptyCell.col()).contains(required)) {
                    sudokuGame.setNumber(emptyCell.row(), emptyCell.col(), required);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean applyXSumsBasics(SudokuGame sudokuGame) {
        var modifiers = sudokuGame.getSudoku().getModifiers();
        if (modifiers == null || !modifiers.hasXSums()) return false;

        int size = sudokuGame.getSudoku().getType().getGridSize();
        int maxSum = (size * (size + 1)) / 2;

        var xsums = modifiers.getXSums();
        int[] top = xsums.top();
        int[] bot = xsums.bottom();
        int[] left = xsums.left();
        int[] right = xsums.right();

        for (int i = 0; i < size; i++) {
            if (top != null && top[i] > 0 && processXSumsDirection(sudokuGame, top[i], 0, i, 1, 0, size, maxSum))
                return true;
            if (bot != null && bot[i] > 0 && processXSumsDirection(sudokuGame, bot[i], size - 1, i, -1, 0, size, maxSum))
                return true;
            if (left != null && left[i] > 0 && processXSumsDirection(sudokuGame, left[i], i, 0, 0, 1, size, maxSum))
                return true;
            if (right != null && right[i] > 0 && processXSumsDirection(sudokuGame, right[i], i, size - 1, 0, -1, size, maxSum))
                return true;
        }
        return false;
    }

    private static boolean processXSumsDirection(SudokuGame game, int targetSum, int startR, int startC, int dr, int dc, int size, int maxSum) {
        var validator = game.getSudokuValidator();

        if (targetSum == 1) {
            if (game.getSudoku().isZero(startR, startC) && validator.getCellCandidates(startR, startC).contains(1)) {
                game.setNumber(startR, startC, 1);
                return true;
            }
        } else if (targetSum == maxSum) {
            if (game.getSudoku().isZero(startR, startC) && validator.getCellCandidates(startR, startC).contains(size)) {
                game.setNumber(startR, startC, size);
                return true;
            }
        }

        int x = game.getSudoku().getBoard()[startR][startC].getValue();
        if (x > 0) {
            int currentSum = 0;
            int emptyCount = 0;
            int emptyR = -1, emptyC = -1;

            int r = startR;
            int c = startC;
            for (int i = 0; i < x; i++) {
                int val = game.getSudoku().getBoard()[r][c].getValue();
                if (val == 0) {
                    emptyCount++;
                    emptyR = r;
                    emptyC = c;
                } else {
                    currentSum += val;
                }
                r += dr;
                c += dc;
            }

            if (emptyCount == 1) {
                int required = targetSum - currentSum;
                if (required > 0 && validator.getCellCandidates(emptyR, emptyC).contains(required)) {
                    game.setNumber(emptyR, emptyC, required);
                    return true;
                }
            }
        }
        return false;
    }
}
