package cz.logicgo.engine.algorithms.shikaku;


import cz.logicgo.core.builders.shikaku.ShikakuCreation;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;
import cz.logicgo.core.gameClasses.shikaku.ShikakuRectangle;
import cz.logicgo.core.gameClasses.shikaku.ShikakuUtils;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.misc.UniqueNumberGenerator;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics;
import cz.logicgo.engine.context.ShikakuCreateContext;
import cz.logicgo.engine.context.ShikakuSolveContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static cz.logicgo.engine.algorithms.shikaku.grader.ShikakuGrader.getBoardDifficulty;
import static cz.logicgo.engine.algorithms.shikaku.grader.ShikakuGrader.gradeShikaku;
import static cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics.getShikakuGenLimit;
import static cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics.getShikakuSolveLimit;

public class ShikakuGenerator {

    public static Shikaku generateGame(ShikakuCreation creation) throws LimitReachedException, ThreadTerminationException {
        ShikakuCreateContext genContext = new ShikakuCreateContext(new StepCounter());
        ShikakuSolveContext solveContext = new ShikakuSolveContext(new StepCounter(), 2);
        return generateGame(creation, genContext, solveContext);
    }

    public static Shikaku generateGame(ShikakuCreation creation, ShikakuCreateContext genContext, ShikakuSolveContext solveContext) throws LimitReachedException, ThreadTerminationException {
        final Difficulty statsDifficulty = creation.getDifficulty();

        GenerationStatistics.ShikakuKey key = new GenerationStatistics.ShikakuKey(creation.getShikakuType(), statsDifficulty, creation.getWidth(), creation.getHeight());

        long limitGen = genContext.stepCounter().getLimit() == null ? getShikakuGenLimit(key) : genContext.stepCounter().getLimit();
        long limitSolution = genContext.stepCounter().getLimit() == null ? getShikakuSolveLimit(key) : solveContext.stepCounter().getLimit();
        Shikaku shikaku;

        try {
            genContext.reset();
            solveContext.reset();

            shikaku = new Shikaku(creation);

            genContext.stepCounter().setLimit(limitGen);
            genContext.checkContext();

            List<ShikakuRectangle> solution = generateBoard(shikaku, genContext);
            shikaku.setSolutionRectangles(solution);

            solveContext.stepCounter().reset();
            solveContext.setStepsLimit(limitSolution);

            int solutionCount = ShikakuSolverDLX.solve(shikaku.getBoard(), shikaku.getShikakuType(), solveContext);

            if (solutionCount == 1) {
                switch (creation.getShikakuType()) {
                    case CLASSIC -> {
                        if (creation.getHeight() >= 19 || creation.getWidth() >= 19) {
                            shikaku.setDifficulty(statsDifficulty);
                            return shikaku;
                        }
                    }
                    case OFF_BY_ONE -> {
                        if (creation.getHeight() >= 11 || creation.getWidth() >= 11) {
                            shikaku.setDifficulty(statsDifficulty);
                            return shikaku;
                        }
                    }
                }

                int diff = gradeShikaku(shikaku.getBoard(), shikaku.getShikakuType());
                Difficulty gradedDifficulty = getBoardDifficulty(diff);
                shikaku.setGrade(diff);
                shikaku.setDifficulty(gradedDifficulty);
                return shikaku;
            }
        } catch (LimitReachedException limitReachedException) {
            return null;
        }
        return null;
    }


    public static List<ShikakuRectangle> generateBoard(Shikaku shikaku) throws LimitReachedException, ThreadTerminationException {
        return generateBoard(shikaku, new ShikakuCreateContext(new StepCounter()));
    }

    public static List<ShikakuRectangle> generateBoard(Shikaku shikaku, ShikakuCreateContext genContext) throws LimitReachedException, ThreadTerminationException {
        UniqueNumberGenerator regionGen = new UniqueNumberGenerator(0);
        int height = shikaku.getHeight();
        int width = shikaku.getWidth();
        Random rand = shikaku.getRandomInstance();
        Difficulty difficulty = shikaku.getDifficulty();
        ShikakuType type = shikaku.getShikakuType();

        generateLayout(shikaku.getBoard(), 0, 0, height - 1, width - 1, rand, regionGen, difficulty, type, genContext);
        return ShikakuUtils.getSolutionRectangles(shikaku.getBoard(), width, height);
    }

    private static void generateLayout(ShikakuCell[][] board, int r1, int c1, int r2, int c2, Random rand, UniqueNumberGenerator regionGen, Difficulty difficulty, ShikakuType type, ShikakuCreateContext genContext) throws LimitReachedException, ThreadTerminationException {
        genContext.checkContext();
        genContext.stepCounter().increment();

        int width = c2 - c1 + 1;
        int height = r2 - r1 + 1;
        int totalArea = width * height;
        int validCells = totalArea;

        if (validCells == 0) return;

        if (validCells <= 1) {
            assignRegionAndClue(board, r1, c1, r2, c2, rand, regionGen, type, difficulty);
            return;
        }

        boolean needsCut = false;
        boolean stopCutting = false;

        if (!needsCut) {
            if (totalArea <= 3) {
                stopCutting = true;
            }

            if (!stopCutting) {
                if (type == ShikakuType.OFF_BY_ONE) {
                    stopCutting = switch (difficulty) {
                        case EASY -> totalArea <= 6 || (totalArea <= 12 && rand.nextInt(2) == 0);
                        case MEDIUM -> totalArea <= 8 || (totalArea <= 16 && rand.nextInt(3) == 0);
                        case HARD -> totalArea <= 6 || (totalArea <= 14 && rand.nextInt(4) == 0);
                        default -> false;
                    };
                } else {
                    stopCutting = switch (difficulty) {
                        case EASY -> totalArea <= 6 || (totalArea <= 14 && rand.nextInt(2) == 0);
                        case MEDIUM -> totalArea <= 9 || (totalArea <= 18 && rand.nextInt(3) == 0);
                        case HARD -> totalArea <= 5 || (totalArea <= 16 && rand.nextInt(4) == 0);
                        default -> false;
                    };
                }

                if (difficulty == Difficulty.HARD && totalArea <= 24 && rand.nextDouble() < 0.15) {
                    stopCutting = true;
                }

                if (difficulty == Difficulty.EASY && totalArea <= 10 && rand.nextDouble() < 0.15) {
                    stopCutting = true;
                }

                boolean checkDifficulty = (difficulty == Difficulty.HARD && totalArea > 8 && rand.nextDouble() > 0.7)
                        || (difficulty == Difficulty.MEDIUM && rand.nextDouble() > 0.4);

                if (checkDifficulty && stopCutting && (width == 1 || height == 1) && totalArea > 4) {
                    stopCutting = false;
                }
            }
        }

        if (stopCutting || (width == 1 && height == 1)) {
            assignRegionAndClue(board, r1, c1, r2, c2, rand, regionGen, type, difficulty);
            return;
        }

        boolean splitVertically;
        if (width == 1) {
            splitVertically = false;
        } else if (height == 1) {
            splitVertically = true;
        } else {
            double splitChance = (double) width / (width + height);
            splitVertically = rand.nextDouble() < splitChance;
        }

        if (splitVertically) {
            int cutCol = getValidVerticalCut(r1, c1, r2, c2, rand, difficulty);
            if (cutCol == -1) {
                assignRegionAndClue(board, r1, c1, r2, c2, rand, regionGen, type, difficulty);
                return;
            }
            generateLayout(board, r1, c1, r2, cutCol, rand, regionGen, difficulty, type, genContext);
            generateLayout(board, r1, cutCol + 1, r2, c2, rand, regionGen, difficulty, type, genContext);
        } else {
            int cutRow = getValidHorizontalCut(r1, c1, r2, c2, rand, difficulty);
            if (cutRow == -1) {
                assignRegionAndClue(board, r1, c1, r2, c2, rand, regionGen, type, difficulty);
                return;
            }
            generateLayout(board, r1, c1, cutRow, c2, rand, regionGen, difficulty, type, genContext);
            generateLayout(board, cutRow + 1, c1, r2, c2, rand, regionGen, difficulty, type, genContext);
        }
    }

    private static int getValidVerticalCut(int r1, int c1, int r2, int c2, Random rand, Difficulty difficulty) {
        int width = c2 - c1 + 1;
        List<Integer> validCuts = new ArrayList<>();

        for (int offset = 0; offset < width - 1; offset++) {
            int cutCol = c1 + offset;

            int leftValid = (r2 - r1 + 1) * (cutCol - c1 + 1);
            int rightValid = (r2 - r1 + 1) * (c2 - cutCol);

            if ((leftValid > 0 && rightValid > 0) || leftValid == 0 || rightValid == 0) {
                if (difficulty == Difficulty.EASY && width > 4) {
                    int mid = (width / 2) - 1;
                    if (Math.abs(offset - mid) <= 1) validCuts.add(cutCol);
                } else if (difficulty == Difficulty.MEDIUM && width > 4) {
                    if (offset > 0 && offset < width - 2) validCuts.add(cutCol);
                } else {
                    validCuts.add(cutCol);
                }
            }
        }

        if (validCuts.isEmpty()) return -1;
        return validCuts.get(rand.nextInt(validCuts.size()));
    }

    private static int getValidHorizontalCut(int r1, int c1, int r2, int c2, Random rand, Difficulty difficulty) {
        int height = r2 - r1 + 1;
        List<Integer> validCuts = new ArrayList<>();

        for (int offset = 0; offset < height - 1; offset++) {
            int cutRow = r1 + offset;

            int topValid = (cutRow - r1 + 1) * (c2 - c1 + 1);
            int bottomValid = (r2 - cutRow) * (c2 - c1 + 1);

            if ((topValid > 0 && bottomValid > 0) || topValid == 0 || bottomValid == 0) {
                if (difficulty == Difficulty.EASY && height > 4) {
                    int mid = (height / 2) - 1;
                    if (Math.abs(offset - mid) <= 1) validCuts.add(cutRow);
                } else if (difficulty == Difficulty.MEDIUM && height > 4) {
                    if (offset > 0 && offset < height - 2) validCuts.add(cutRow);
                } else {
                    validCuts.add(cutRow);
                }
            }
        }

        if (validCuts.isEmpty()) return -1;
        return validCuts.get(rand.nextInt(validCuts.size()));
    }

    private static void assignRegionAndClue(ShikakuCell[][] board, int r1, int c1, int r2, int c2, Random rand, UniqueNumberGenerator regionGen, ShikakuType type, Difficulty difficulty) {
        int area = 0;
        List<int[]> validCells = new ArrayList<>();

        for (int r = r1; r <= r2; r++) {
            for (int c = c1; c <= c2; c++) {
                area++;
                validCells.add(new int[]{r, c});
            }
        }

        if (validCells.isEmpty()) return;

        int regionId = regionGen.generateNewId();
        int clueValue = area;

        if (type == ShikakuType.OFF_BY_ONE) {
            clueValue = (area == 1) ? 2 : (rand.nextBoolean() ? area + 1 : area - 1);
        }

        int[] chosen;
        if (difficulty == Difficulty.HARD && validCells.size() > 1) {
            chosen = validCells.get(rand.nextBoolean() ? 0 : validCells.size() - 1);
        } else if (difficulty == Difficulty.EASY) {
            chosen = validCells.get(validCells.size() / 2);
        } else {
            chosen = validCells.get(rand.nextInt(validCells.size()));
        }

        int clueR = chosen[0];
        int clueC = chosen[1];

        for (int[] cell : validCells) {
            int r = cell[0];
            int c = cell[1];
            board[r][c].setRegionId(regionId);
            if (r == clueR && c == clueC) {
                board[r][c].setClue(clueValue);
            }
        }
    }
}
