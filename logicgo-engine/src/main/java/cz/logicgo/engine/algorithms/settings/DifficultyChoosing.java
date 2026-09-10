package cz.logicgo.engine.algorithms.settings;


import cz.logicgo.core.factoryInit.maze.MazeInit;
import cz.logicgo.core.gameClasses.bridge.BoardSize;
import cz.logicgo.core.gameClasses.sudoku.misc.SizeRange;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics.*;
import cz.logicgo.engine.algorithms.mazes.gen.RegionGenConfig;

import java.util.Arrays;
import java.util.Random;

import static cz.logicgo.core.gameClasses.maze.MazeUtils.initAllTrue;
import static cz.logicgo.core.util.boardConverters.MazeConverters.booleanMaskToInt;
import static cz.logicgo.core.util.boardConverters.MazeConverters.intMaskToBoolean;
import static cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics.*;
import static cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig.LAYOUTS;
import static cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig.isMultiDoku;


public class DifficultyChoosing {

    public static int getSudokuCellsCountToRemove(SudokuSize sudokuSize, SudokuVariant variant, Difficulty difficulty, long seed) {
        int gridSize = sudokuSize.getGridSize();
        int totalCells = gridSize * gridSize;
        int maxRemovable = getBaseMaxRemovable(gridSize, totalCells, variant);

        double difficultyRatio = switch (difficulty) {
            case EASY -> 0.85;
            case MEDIUM -> 0.92;
            case HARD -> 1.0;
        };

        if (variant == SudokuVariant.ANTI_ALL) {
            difficultyRatio = 1.0;
        }

        double variationFactor = (Math.abs(seed % 100) / 100.0 - 0.5) * 0.04;
        double finalRatio = Math.min(Math.max(difficultyRatio + variationFactor, 0.1), 1.0);

        int removeCount = (int) (maxRemovable * finalRatio);

        if (difficulty == Difficulty.HARD) {
            removeCount += 2;
        }

        return Math.min(removeCount, maxRemovable);
    }

    public static int getMaxSudokuCellsToRemove(SudokuSize sudokuSize, SudokuVariant variant, Difficulty difficulty) {
        int gridSize = sudokuSize.getGridSize();
        int totalCells = gridSize * gridSize;
        int maxRemovable = getBaseMaxRemovable(gridSize, totalCells, variant);

        double difficultyRatio = switch (difficulty) {
            case EASY -> 0.85;
            case MEDIUM -> 0.92;
            case HARD -> 1.00;
        };

        if (variant == SudokuVariant.ANTI_ALL) {
            difficultyRatio = 1.0;
        }

        double finalRatio = Math.min(difficultyRatio + 0.02, 1.0);
        int removeCount = (int) (maxRemovable * finalRatio);

        if (difficulty == Difficulty.HARD) {
            removeCount += 2;
        }

        return Math.min(removeCount, maxRemovable);
    }



    public static SizeRange getValidSizeRange(SudokuVariant variant) {
        if (variant == SudokuVariant.CLASSIC) {
            return new SizeRange(4, 16);
        }

        if (isMultiDoku(variant)) {
            return new SizeRange(9, 9);
        }

        return switch (variant) {
            case EVEN_ODD, DIAGONAL, SKYSCRAPER, OFFSET -> new SizeRange(6, 10);

            case ANTI_KNIGHT, ANTI_KING, ANTI_CONSECUTIVE, ANTI_ALL, IRREGULAR, PATTERNED -> new SizeRange(9);

            case KILLER, KROPKI, CONSECUTIVE, XV, GREATER_THAN, SANDWICH, BETWEEN, VUDOKU, X_SUMS,
                 QUADRUPLES, GROUP_SUMS -> new SizeRange(8, 10);

            default -> new SizeRange(8, 10);
        };
    }

    private static int getBaseMaxRemovable(int gridSize, int totalCells, SudokuVariant variant) {
        if (isMultiDoku(variant)) {
            var setup = LAYOUTS.get(variant);
            if (setup != null) {
                int realCells = setup.getRealCellCount();
                return (int) switch (variant) {
                    case DOUBLEDOKU, TRIPLEDOKU -> (realCells * 0.65);
                    default -> (realCells * 0.61);
                };
            }
        }

        double sizeMultiplier = 1.0;
        if (gridSize > 10) {
            if (gridSize == 11) sizeMultiplier = 0.80;
            else if (gridSize >= 13 && gridSize < 15) sizeMultiplier = 0.72;
            else if (gridSize == 15) sizeMultiplier = 0.64;
            else if (gridSize == 16) sizeMultiplier = 0.55;
        }

        double classicFactor = 0.715;
        if (gridSize > 9) {
            if (gridSize == 13 || gridSize == 11) classicFactor = 0.55;
            else if (gridSize == 12) classicFactor = 0.65;
            else if (gridSize >= 13 && gridSize < 15) classicFactor = 0.55;
            else if (gridSize == 15) classicFactor = 0.48;
            else if (gridSize == 16) classicFactor = 0.42;
        }

        int rawMax = switch (variant) {
            case XV -> (int) (totalCells * 0.82 * sizeMultiplier);
            case CONSECUTIVE -> (int) (totalCells * 0.85 * sizeMultiplier);
            case KILLER -> (int) (totalCells * 0.90 * sizeMultiplier);
            case KROPKI -> (int) (totalCells * 0.84 * sizeMultiplier);
            case GREATER_THAN -> (int) (totalCells * 0.82 * sizeMultiplier);
            case SANDWICH -> (int) (totalCells * 0.76 * sizeMultiplier);
            case QUADRUPLES -> (int) (totalCells * 0.74 * sizeMultiplier);
            case SKYSCRAPER, VUDOKU -> {
                if (gridSize <= 9) {
                    yield (int) (totalCells * 0.74 * sizeMultiplier);
                } else {
                    yield (int) (totalCells * 0.71 * sizeMultiplier);
                }

            }
            case BETWEEN -> {
                if (gridSize <= 9) {
                    yield (int) (totalCells * 0.73 * sizeMultiplier);
                } else {
                    yield (int) (totalCells * 0.68 * sizeMultiplier);
                }

            }

            case CLASSIC, DIAGONAL, EVEN_ODD -> (gridSize == 9) ? 56 : (int) (totalCells * classicFactor);

            case ANTI_ALL -> (int) (totalCells * 0.91 * sizeMultiplier);
            case ANTI_KING, ANTI_KNIGHT -> (int) (totalCells * 0.76 * sizeMultiplier);
            case ANTI_CONSECUTIVE -> (int) (totalCells * 0.76 * sizeMultiplier);

            default -> (gridSize == 9) ? 56 : (int) (totalCells * classicFactor);
        };

        return rawMax;
    }

    public static int getRegionCount(int[][] mask, Difficulty difficulty) {
        int activeCells = getActiveCellsCount(mask);
        double mod = getDiffModifierForMaze(difficulty);
        double cellsPerRegion = 45.0 / mod;
        int calculatedRegions = (int) (activeCells / cellsPerRegion);
        int maxSafeRegions = Math.max(3, activeCells / 10);
        return Math.clamp(calculatedRegions, 3, Math.min(25, maxSafeRegions));
    }

    public static RegionGenConfig getRegionConfig(int rows, int cols, int[][] mask, int numRegions, Difficulty difficulty) {
        int activeCells = getActiveCellsCount(mask);
        int avgRegionSize = activeCells / numRegions;
        int minRegionSize = Math.max(4, (int) (avgRegionSize * 0.3));
        return new RegionGenConfig(cols, rows, numRegions, minRegionSize);
    }

    private static int getActiveCellsCount(int[][] mask) {
        return Arrays.stream(mask).flatMapToInt(Arrays::stream).sum();
    }

    private static double getDiffModifierForMaze(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 0.75;
            case MEDIUM -> 1.25;
            case HARD -> 1.75;
        };
    }

    public static double getEvenOddProbabilities(Difficulty difficulty, Random random) {
        double[] probRange = switch (difficulty) {
            case EASY -> new double[]{0.55, 0.70};
            case MEDIUM -> new double[]{0.30, 0.40};
            case HARD -> new double[]{0.10, 0.30};
        };

        double minProb = probRange[0];
        double maxProb = probRange[1];

        return minProb + (maxProb - minProb) * random.nextDouble();
    }

    public static int getPortalCount(int[][] mask, Difficulty difficulty) {
        int numRegions = getRegionCount(mask, difficulty);
        double mod = getDiffModifierForMaze(difficulty);
        int portalCount = (int) (numRegions * 0.75 * mod);
        return Math.clamp(portalCount, 2, 16);
    }

    public static int getRandomCountForType(MazeInit config, MazeType type, Random random) {
        Difficulty difficulty = config.getDifficulty();
        if (config.getMask() == null || config.getMask().length == 0) {
            config.setMask(intMaskToBoolean(initAllTrue(config.getHeight(), config.getWidth())));
        }

        int width = config.getWidth();
        int height = config.getHeight();
        int totalArea = width * height;

        return switch (type) {
            case PATTERN -> {
                int[][] mask = booleanMaskToInt(config.getMask());
                yield getRegionCount(mask, difficulty);
            }
            case WALLS -> {
                int[][] mask = booleanMaskToInt(config.getMask());
                yield getWallsRegionCount(mask, difficulty, width, height);
            }
            case CHECKPOINT -> {
                int[][] mask = booleanMaskToInt(config.getMask());
                yield getCheckpointRegionCount(mask, difficulty, width, height);
            }
            case PORTAL -> {
                int[][] mask = booleanMaskToInt(config.getMask());
                yield getPortalCount(mask, difficulty);
            }
            case WRAP_AROUND -> {
                int baseScale = Math.max(1, (width + height) / 12);
                yield switch (difficulty) {
                    case EASY -> baseScale;
                    case MEDIUM -> (int) (baseScale * 1.5);
                    case HARD -> baseScale * random.nextInt(2, 4);
                };
            }
            case EXACT_STEPS -> {
                int minSteps = width + height;
                double multiplier = switch (difficulty) {
                    case EASY -> 1.2;
                    case MEDIUM -> 1.6;
                    case HARD -> 2.2;
                };
                int baseSteps = (int) (minSteps * multiplier);
                int variance = (int) (baseSteps * 0.15);
                int randomNoise = random.nextInt((variance * 2) + 1) - variance;

                int finalSteps = baseSteps + randomNoise;
                yield Math.max(minSteps, finalSteps);
            }
            default -> 0;
        };
    }

    public static int getWallsRegionCount(int[][] mask, Difficulty difficulty, int width, int height) {
        int activeCells = getActiveCellsCount(mask);
        double baseCellsPerRegion = Math.max(30.0, Math.min(80.0, (width * height) / 8.0));
        double mod = switch (difficulty) {
            case EASY -> 0.6;
            case MEDIUM -> 1.0;
            case HARD -> 1.4;
        };
        double cellsPerRegion = baseCellsPerRegion / mod;
        int calculatedRegions = (int) (activeCells / cellsPerRegion);
        int maxSafeRegions = Math.max(2, activeCells / 15);
        return Math.clamp(calculatedRegions, 2, Math.min(16, maxSafeRegions));
    }

    public static int getCheckpointRegionCount(int[][] mask, Difficulty difficulty, int width, int height) {
        int activeCells = getActiveCellsCount(mask);
        double baseCellsPerRegion = Math.max(30.0, Math.min(80.0, (width * height) / 8.0));
        double mod = switch (difficulty) {
            case EASY -> 0.6;
            case MEDIUM -> 0.8;
            case HARD -> 1;
        };
        double cellsPerRegion = baseCellsPerRegion / mod;
        int calculatedRegions = (int) (activeCells / cellsPerRegion);

        int maxSafeRegions = Math.max(3, activeCells / 15);
        return Math.clamp(calculatedRegions, 3, Math.min(16, maxSafeRegions));
    }

    public static BridgeDifficulty getBridgeDifficulty(Difficulty difficulty, Random random, BoardSize boardSize, BridgeType bridgeType, Integer maxMultiple) {
        int width = boardSize.width();
        int height = boardSize.height();
        int totalCells = width * height;
        int maxDim = Math.max(width, height);

        double baseDensity = (maxDim < 7) ? 0.28 : (maxDim < 10 ? 0.24 : 0.18);
        double maxSafeIslands = totalCells * baseDensity;

        double start = 15, end = 30, densMult = 0.6;

        switch (difficulty != null ? difficulty : Difficulty.MEDIUM) {
            case EASY -> {
                start = 10;
                end = 25;
                densMult = random.nextDouble(0.40, 0.55);
            }
            case MEDIUM -> {
                start = 20;
                end = 40;
                densMult = random.nextDouble(0.65, 0.75);
            }
            case HARD -> {
                start = 35;
                end = 55;
                densMult = random.nextDouble(0.85, 0.95);
            }
        }

        double prob = random.nextDouble(start, end) / 100.0;

        int minIslands = (maxDim < 7) ? 3 : 5;
        int maxIslands = (int) (totalCells * (maxDim < 7 ? 0.45 : 0.25));
        int islandCount = Math.clamp((int) Math.round(maxSafeIslands * densMult), minIslands, maxIslands);
        int multipleCount = maxMultiple;

        BridgeLimitKey limitKey = new BridgeLimitKey(multipleCount, width, height, difficulty);

        Integer islandCountStepsMax = getBridgeIslandLimit(limitKey);
        Integer islandCountStepsMin = getBridgeIslandLowerLimit(limitKey);

        if (islandCountStepsMax != null && islandCountStepsMax > 0 && islandCountStepsMin != null && islandCountStepsMin > 0) {
            islandCount = Math.clamp(islandCount, islandCountStepsMin, islandCountStepsMax);
        }

        long islandSolveSteps = -1L;
        int minAllowedIslands = (maxDim < 7) ? 3 : 5;

        while (islandSolveSteps == -1L && islandCount >= minAllowedIslands) {
            islandSolveSteps = getBridgeSolveLimit(
                    new BridgeKey(multipleCount, islandCount, difficulty, width, height)
            );

            if (islandSolveSteps == -1L) {
                islandCount--;
            }
        }

        if (islandCount < minAllowedIslands) {
            islandCount = minAllowedIslands;
        }

        return new BridgeDifficulty(islandCount, prob, multipleCount);
    }

    public record BridgeDifficulty(int islandCount, double doubleProbability, int multipleCount) {
    }
}
