package cz.logicgo.engine.algorithms.shikaku.grader;


import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;
import cz.logicgo.core.gameClasses.shikaku.ShikakuRectangle;

import java.util.*;

public class ShikakuGrader {

    private static final int MAX_GRADER_STEPS = 1000;

    public static int gradeShikaku(ShikakuCell[][] shikakuBoard, ShikakuType shikakuType) {
        double totalScore = 0;
        boolean changed = true;

        GraderState state = new GraderState(shikakuBoard, shikakuType);
        Map<String, Long> usageStats = new LinkedHashMap<>();

        int stepCount = 0;
        boolean loopLimitReached = false;

        while (changed && !state.isSolved()) {
            stepCount++;
            if (stepCount > MAX_GRADER_STEPS) {

                loopLimitReached = true;
                break;
            }

            changed = false;

            int starting = applyStartingTechniques(state);
            if (starting > 0) {
                totalScore += (1.0 * starting);
                usageStats.put("Starting Techniques", usageStats.getOrDefault("Starting Techniques", 0L) + starting);
                changed = true;
                continue;
            }

            int basic = applyEliminationTechniques(state);
            if (basic > 0) {
                totalScore += (2.5 * basic);
                usageStats.put("Basic Elimination", usageStats.getOrDefault("Basic Elimination", 0L) + basic);
                changed = true;
                continue;
            }

            int isolation = applyIsolatedCellsTechniques(state);
            if (isolation > 0) {
                totalScore += (15.0 * isolation);
                usageStats.put("Isolated Cells", usageStats.getOrDefault("Isolated Cells", 0L) + isolation);
                changed = true;
                continue;
            }

            int advanced = applyAdvancedContradiction(state);
            if (advanced > 0) {
                totalScore += (40.0 * advanced);
                usageStats.put("Parity & Shapes", usageStats.getOrDefault("Parity & Shapes", 0L) + advanced);
                changed = true;
                continue;
            }
        }


        if (loopLimitReached) {
            return 99;
        }

        int remainingUnsolvedClues = state.getUnsolvedClues().size();
        int totalCluesCount = state.getAllClues().size();

        if (remainingUnsolvedClues <= (totalCluesCount * 0.10) || remainingUnsolvedClues <= 2) {
            int calculatedDifficulty;

            long advancedCount = usageStats.getOrDefault("Parity & Shapes", 0L);
            long isolationCount = usageStats.getOrDefault("Isolated Cells", 0L);
            long basicCount = usageStats.getOrDefault("Basic Elimination", 0L);
            long startingCount = usageStats.getOrDefault("Starting Techniques", 0L);

            if (advancedCount > 0) {
                calculatedDifficulty = (advancedCount > 2) ? 9 : 7;
            } else if (isolationCount > 0) {
                if (isolationCount > 8) {
                    calculatedDifficulty = 7;
                } else if (isolationCount > 3) {
                    calculatedDifficulty = 6;
                } else {
                    calculatedDifficulty = 5;
                }
            } else if (basicCount > 0) {
                if (basicCount > 12) {
                    calculatedDifficulty = 5;
                } else if (basicCount > 5) {
                    calculatedDifficulty = 4;
                } else {
                    calculatedDifficulty = 3;
                }
            } else {
                calculatedDifficulty = (startingCount > 4) ? 2 : 1;
            }

            double base = 3.5;
            double scale = 1.8;

            int scoreBasedDiff = 1;
            if (totalScore > 1) {
                scoreBasedDiff = (int) Math.round((Math.log(totalScore) / Math.log(base)) * scale);
            }

            if (scoreBasedDiff > calculatedDifficulty && calculatedDifficulty < 6) {
                calculatedDifficulty++;
            }

            return Math.clamp(calculatedDifficulty, 1, 9);
        } else {
            return 99;
        }
    }

    public static Difficulty getBoardDifficulty(int diff) {
        return switch (diff) {
            case 1, 2, 3, 4 -> Difficulty.EASY;
            case 5, 6, 7 -> Difficulty.MEDIUM;
            case 8, 9 -> Difficulty.HARD;
            default -> null;
        };
    }

    private static int applyStartingTechniques(GraderState state) {
        int count = 0;
        for (ShikakuCell clue : state.getUnsolvedClues()) {
            List<ShikakuRectangle> validShapes = state.getValidRectanglesFor(clue);
            if (validShapes.size() == 1) {
                state.placeRectangle(clue, validShapes.getFirst());
                count++;
            }
        }
        return count;
    }

    private static int applyEliminationTechniques(GraderState state) {
        int count = 0;
        for (ShikakuCell clue : state.getUnsolvedClues()) {
            List<ShikakuRectangle> validShapes = state.getValidRectanglesFor(clue);
            if (validShapes.size() == 1) {
                state.placeRectangle(clue, validShapes.get(0));
                count++;
            }
        }
        return count;
    }

    private static int applyIsolatedCellsTechniques(GraderState state) {
        int count = 0;
        for (ShikakuCell emptyCell : state.getAllEmptyCells()) {
            List<ShikakuCell> cluesThatCanReach = state.findCluesThatCanCover(emptyCell);

            if (cluesThatCanReach.size() == 1) {
                ShikakuCell forcedClue = cluesThatCanReach.get(0);
                List<ShikakuRectangle> forcedShapes = state.getShapesCoveringCell(forcedClue, emptyCell);

                if (forcedShapes.size() == 1) {
                    state.placeRectangle(forcedClue, forcedShapes.get(0));
                    count++;
                }
            }
        }
        return count;
    }

    private static int applyAdvancedContradiction(GraderState state) {
        int count = 0;
        for (ShikakuCell clue : state.getUnsolvedClues()) {
            List<ShikakuRectangle> validShapes = state.getValidRectanglesFor(clue);
            ShikakuRectangle validShape = null;
            int nonContradictingCount = 0;

            for (ShikakuRectangle shape : validShapes) {
                state.placeRectangle(clue, shape);

                boolean createsContradiction = hasUnfillableCells(state);

                state.removeRectangle(clue, shape);

                if (!createsContradiction) {
                    validShape = shape;
                    nonContradictingCount++;
                }
            }

            if (nonContradictingCount == 1 && validShape != null) {
                state.placeRectangle(clue, validShape);
                count++;
            }
        }
        return count;
    }

    private static boolean hasUnfillableCells(GraderState state) {
        for (ShikakuCell emptyCell : state.getAllEmptyCells()) {
            if (state.findCluesThatCanCover(emptyCell).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static class GraderState {
        private final ShikakuCell[][] shikakuBoard;
        private final int width;
        private final int height;
        private final List<ShikakuCell> allClues;
        private final boolean[][] grid;
        private final Map<ShikakuCell, ShikakuRectangle> placedRectangles = new HashMap<>();
        private final ShikakuType shikakuType;

        public GraderState(ShikakuCell[][] shikakuBoard, ShikakuType type) {
            this.shikakuBoard = shikakuBoard;
            this.width = shikakuBoard[0].length;
            this.height = shikakuBoard.length;
            this.allClues = new ArrayList<>();
            this.grid = new boolean[height][width];
            this.shikakuType = type;


            for (int r = 0; r < height; r++) {
                for (int c = 0; c < width; c++) {
                    if (shikakuBoard[r][c] != null && shikakuBoard[r][c].hasClue()) {
                        allClues.add(shikakuBoard[r][c]);
                    }
                }
            }
        }

        public List<ShikakuCell> getAllClues() {
            return allClues;
        }

        public boolean isSolved() {
            return placedRectangles.size() == allClues.size();
        }

        public List<ShikakuCell> getUnsolvedClues() {
            List<ShikakuCell> unsolved = new ArrayList<>();
            for (ShikakuCell clue : allClues) {
                if (!placedRectangles.containsKey(clue)) {
                    unsolved.add(clue);
                }
            }
            return unsolved;
        }

        public void placeRectangle(ShikakuCell clue, ShikakuRectangle rect) {
            placedRectangles.put(clue, rect);
            markGrid(rect, true);
        }

        public void removeRectangle(ShikakuCell clue, ShikakuRectangle rect) {
            placedRectangles.remove(clue);
            markGrid(rect, false);
        }

        private void markGrid(ShikakuRectangle rect, boolean state) {
            for (int r = rect.getMinRow(); r <= rect.getMaxRow(); r++) {
                for (int c = rect.getMinCol(); c <= rect.getMaxCol(); c++) {
                    grid[r][c] = state;
                }
            }
        }

        public List<ShikakuCell> getAllEmptyCells() {
            List<ShikakuCell> emptyCells = new ArrayList<>();
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < width; c++) {
                    if (!grid[r][c]) {
                        emptyCells.add(shikakuBoard[r][c]);
                    }
                }
            }
            return emptyCells;
        }

        public List<ShikakuRectangle> getValidRectanglesFor(ShikakuCell clue) {
            List<ShikakuRectangle> valid = new ArrayList<>();
            int displayedClue = clue.getClue();

            List<Integer> possibleAreas = new ArrayList<>();
            if (shikakuType == ShikakuType.OFF_BY_ONE) {
                if (displayedClue == 1) {
                    possibleAreas.add(2);
                } else {
                    possibleAreas.add(displayedClue - 1);
                    possibleAreas.add(displayedClue + 1);
                }
            } else {
                possibleAreas.add(displayedClue);
            }

            for (int area : possibleAreas) {
                for (int w = 1; w <= area; w++) {
                    if (area % w != 0) continue;
                    int h = area / w;

                    for (int dr = 0; dr < h; dr++) {
                        for (int dc = 0; dc < w; dc++) {
                            int r1 = clue.getRow() - dr;
                            int c1 = clue.getCol() - dc;
                            int r2 = r1 + h - 1;
                            int c2 = c1 + w - 1;

                            ShikakuRectangle rect = new ShikakuRectangle(0, r1, r2, c1, c2);
                            if (isValidPlacement(rect, clue)) {
                                valid.add(rect);
                            }
                        }
                    }
                }
            }
            return valid;
        }

        private boolean isValidPlacement(ShikakuRectangle rect, ShikakuCell ownerClue) {
            int r1 = rect.getMinRow();
            int c1 = rect.getMinCol();
            int r2 = rect.getMaxRow();
            int c2 = rect.getMaxCol();

            if (r1 < 0 || c1 < 0 || r2 >= height || c2 >= width) return false;

            for (int r = r1; r <= r2; r++) {
                for (int c = c1; c <= c2; c++) {
                    if (grid[r][c]) return false;

                    ShikakuCell cell = shikakuBoard[r][c];
                    if (cell != null && cell.hasClue() && !cell.equals(ownerClue)) {
                        return false;
                    }
                }
            }
            return true;
        }

        public List<ShikakuCell> findCluesThatCanCover(ShikakuCell cell) {
            List<ShikakuCell> canCover = new ArrayList<>();
            for (ShikakuCell clue : getUnsolvedClues()) {
                if (!getShapesCoveringCell(clue, cell).isEmpty()) {
                    canCover.add(clue);
                }
            }
            return canCover;
        }

        public List<ShikakuRectangle> getShapesCoveringCell(ShikakuCell clue, ShikakuCell cell) {
            List<ShikakuRectangle> coveringShapes = new ArrayList<>();
            for (ShikakuRectangle rect : getValidRectanglesFor(clue)) {
                if (rect.contains(cell)) {
                    coveringShapes.add(rect);
                }
            }
            return coveringShapes;
        }

        public ShikakuCell[][] getShikakuBoard() {
            return shikakuBoard;
        }

        public ShikakuType getShikakuType() {
            return shikakuType;
        }
    }
}
