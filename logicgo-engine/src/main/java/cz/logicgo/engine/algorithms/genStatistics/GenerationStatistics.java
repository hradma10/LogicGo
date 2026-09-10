package cz.logicgo.engine.algorithms.genStatistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.algorithms.sudoku.custom.CustomLayoutsLoader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerationStatistics {

    private static final DefaultLimitsProvider DEFAULT_INSTANCE = new DefaultLimitsProvider();
    private static IGenerationLimitsProvider currentProvider = DEFAULT_INSTANCE;

    private static final Map<SudokuKey, Long> sudokuGenLimits;
    private static final Map<SudokuKey, Long> sudokuDelLimits;
    private static final Map<SudokuKey, Long> sudokuSolveLimits;
    private static final Map<BridgeKey, Long> bridgeGenLimits;
    private static final Map<BridgeKey, Long> bridgeSolveLimits;
    private static final Map<BridgeLimitKey, Integer> bridgeIslandLimits;
    private static final Map<BridgeLimitKey, Integer> bridgeIslandLowerLimits;
    private static final Map<MazeKey, Long> mazeGenLimits;
    private static final Map<ShikakuKey, Long> shikakuGenLimits;
    private static final Map<ShikakuKey, Long> shikakuSolveLimits;
    private static final Map<MazeVariantKey, Long> mazeVariantLimits;
    public static boolean shikakuCount = false;
    static boolean sudokuLi = false;
    static boolean bridgeTest = false;
    static boolean variantsTest = false;

    static {
        Map<SudokuKey, Long> sudokuGenTemp = new HashMap<>();
        Map<SudokuKey, Long> sudokuDelTemp = new HashMap<>();
        Map<SudokuKey, Long> sudokuSolveTemp = new HashMap<>();

        Map<BridgeKey, Long> bridgeGenTemp = new HashMap<>();
        Map<BridgeLimitKey, Integer> bridgeIslandLimitTemp = new HashMap<>();
        Map<BridgeLimitKey, Integer> bridgeIslandLowerLimitTemp = new HashMap<>();
        Map<BridgeKey, Long> bridgeSolveTemp = new HashMap<>();

        Map<MazeKey, Long> mazeGenTemp = new HashMap<>();
        Map<ShikakuKey, Long> shikakuGenTemp = new HashMap<>();
        Map<ShikakuKey, Long> shikakuSolveTemp = new HashMap<>();
        Map<MazeVariantKey, Long> mazeVariantTemp = new HashMap<>();

        String resourcePath = "/cz/logicgo/engine/game/statistics.json";
        try (InputStream is = GenerationStatistics.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Soubor statistik nebyl v resources nalezen: " + resourcePath);
            }
            ObjectMapper mapper = new ObjectMapper();
            StatsData data = mapper.readValue(is, StatsData.class);

            if (data.sudokuGen != null) {
                for (SudokuGenStat stat : data.sudokuGen) {
                    SudokuKey key = new SudokuKey(stat.variant, stat.size, stat.layoutName, stat.difficulty);
                    sudokuGenTemp.put(key, stat.limitGenSteps);
                    sudokuDelTemp.put(key, stat.limitDelSteps);
                    sudokuSolveTemp.put(key, stat.limitSolveSteps);
                }
            }

            if (data.mazeGen != null) {
                for (MazeGenStat stat : data.mazeGen) {
                    mazeGenTemp.put(new MazeKey(stat.algo, stat.shape, stat.width, stat.height), stat.limitSteps);
                }
            }

            if (data.mazeVariants != null) {
                for (MazeVariantStat stat : data.mazeVariants) {
                    mazeVariantTemp.put(new MazeVariantKey(stat.type, stat.difficulty, stat.width, stat.height), stat.limitSteps);
                }
            }

            if (data.bridgeGen != null) {
                for (BridgeGenStat stat : data.bridgeGen) {
                    var key = new BridgeKey(stat.maxBridges, stat.islands, stat.difficulty, stat.width, stat.height);
                    bridgeGenTemp.put(key, stat.limitSteps);
                    if (stat.limitSteps > 1) {
                        var limitKey = new BridgeLimitKey(stat.maxBridges, stat.width, stat.height, stat.difficulty);
                        bridgeIslandLimitTemp.merge(limitKey, stat.islands, Math::max);
                        bridgeIslandLowerLimitTemp.merge(limitKey, stat.islands, Math::min);
                    }
                }
            }

            if (data.bridgeSolve != null) {
                for (BridgeSolveStat stat : data.bridgeSolve) {
                    bridgeSolveTemp.put(new BridgeKey(stat.maxBridges, stat.islands, stat.difficulty, stat.width, stat.height), stat.limitSteps);
                }
            }

            if (data.shikakuGen != null) {
                for (ShikakuGenStat stat : data.shikakuGen) {
                    var key = new ShikakuKey(stat.type, stat.difficulty, stat.width, stat.height);
                    shikakuGenTemp.put(key, stat.limitGenSteps);
                    shikakuSolveTemp.put(key, stat.limitSolveSteps);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        sudokuGenLimits = Map.copyOf(sudokuGenTemp);
        sudokuDelLimits = Map.copyOf(sudokuDelTemp);
        sudokuSolveLimits = Map.copyOf(sudokuSolveTemp);
        bridgeGenLimits = Map.copyOf(bridgeGenTemp);
        bridgeSolveLimits = Map.copyOf(bridgeSolveTemp);
        bridgeIslandLimits = Map.copyOf(bridgeIslandLimitTemp);
        bridgeIslandLowerLimits = Map.copyOf(bridgeIslandLowerLimitTemp);
        mazeGenLimits = Map.copyOf(mazeGenTemp);
        shikakuGenLimits = Map.copyOf(shikakuGenTemp);
        shikakuSolveLimits = Map.copyOf(shikakuSolveTemp);
        mazeVariantLimits = Map.copyOf(mazeVariantTemp);
    }

    public static void setProvider(IGenerationLimitsProvider provider) {
        currentProvider = (provider != null) ? provider : DEFAULT_INSTANCE;
    }

    public static void resetProvider() {
        currentProvider = DEFAULT_INSTANCE;
    }

    public static Long getSudokuGenLimit(SudokuKey key) {
        return currentProvider.getSudokuGenLimit(key);
    }

    public static Long getSudokuDelLimit(SudokuKey key) {
        return currentProvider.getSudokuDelLimit(key);
    }

    public static Long getSudokuSolveLimit(SudokuKey key) {
        return currentProvider.getSudokuSolveLimit(key);
    }

    public static Long getSudokuLimit(SudokuKey key) {
        return getSudokuGenLimit(key);
    }

    public static Long getShikakuGenLimit(ShikakuKey key) {
        return currentProvider.getShikakuGenLimit(key);
    }

    public static Long getShikakuSolveLimit(ShikakuKey key) {
        return currentProvider.getShikakuSolveLimit(key);
    }

    public static Long getBridgeLimit(BridgeKey key) {
        return currentProvider.getBridgeGenLimit(key);
    }

    public static Long getBridgeSolveLimit(BridgeKey key) {
        return currentProvider.getBridgeSolveLimit(key);
    }

    public static Integer getBridgeIslandLimit(BridgeLimitKey key) {
        return currentProvider.getBridgeIslandLimit(key);
    }

    public static Integer getBridgeIslandLowerLimit(BridgeLimitKey key) {
        return currentProvider.getBridgeIslandLowerLimit(key);
    }

    public static Long getMazeLimit(MazeKey key) {
        return currentProvider.getMazeLimit(key);
    }

    public static Long getMazeVariantLimit(MazeVariantKey key) {
        return currentProvider.getMazeVariantLimit(key);
    }

    private static class DefaultLimitsProvider implements IGenerationLimitsProvider {
        @Override
        public Long getSudokuGenLimit(SudokuKey key) {
            return getExtrapolatedSudokuLimit(key, sudokuGenLimits);
        }

        @Override
        public Long getSudokuDelLimit(SudokuKey key) {
            return getExtrapolatedSudokuLimit(key, sudokuDelLimits);
        }

        @Override
        public Long getSudokuSolveLimit(SudokuKey key) {
            return getExtrapolatedSudokuLimit(key, sudokuSolveLimits);
        }

        @Override
        public Long getShikakuGenLimit(ShikakuKey key) {
            return resolveShikakuGenLimit(key);
        }

        @Override
        public Long getShikakuSolveLimit(ShikakuKey key) {
            return resolveShikakuSolveLimit(key);
        }

        @Override
        public Long getBridgeGenLimit(BridgeKey key) {
            return resolveBridgeLimit(key);
        }

        @Override
        public Long getBridgeSolveLimit(BridgeKey key) {
            return resolveBridgeSolveLimit(key);
        }

        @Override
        public Integer getBridgeIslandLimit(BridgeLimitKey key) {
            return resolveBridgeIslandLimit(key);
        }

        @Override
        public Integer getBridgeIslandLowerLimit(BridgeLimitKey key) {
            return resolveBridgeIslandLowerLimit(key);
        }

        @Override
        public Long getMazeLimit(MazeKey key) {
            return resolveMazeLimit(key);
        }

        @Override
        public Long getMazeVariantLimit(MazeVariantKey key) {
            return resolveMazeVariantLimit(key);
        }
    }

    public static long scaleLimitFromBaseline(long baseLimit, int baseMaxBridges, int targetMaxBridges, int islands) {
        double ratio = (targetMaxBridges + 1.0) / (baseMaxBridges + 1.0);
        double exponent = Math.min(islands / 5.0, 10.0);
        double scalingFactor = Math.pow(ratio, exponent);
        long calculatedLimit = (long) (baseLimit * scalingFactor);
        return Math.min(calculatedLimit, 50_000_000L);
    }

    public static SudokuKey constructKey(Sudoku sudoku) {
        String layoutName = "default";
        if (sudoku.getVariant() == SudokuVariant.IRREGULAR && sudoku.getRegionLayout() != null) {
            Integer[][] integerGrid = sudoku.getRegionLayout().getRegions();
            int size = sudoku.getType().getGridSize();
            int[][] rawGrid = new int[size][size];
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    rawGrid[r][c] = integerGrid[r][c];
                }
            }
            int layoutId = CustomLayoutsLoader.getIrregularRegionId(size, rawGrid);
            if (layoutId != -1) layoutName = "L" + layoutId;
        } else if (sudoku.getVariant() == SudokuVariant.CLASSIC && sudoku.getRegionLayout() != null) {
            String fullName = sudoku.getRegionLayout().getName();
            if (fullName != null && fullName.contains(".")) {
                try {
                    int lastDot = fullName.lastIndexOf('.');
                    int secondToLastDot = fullName.lastIndexOf('.', lastDot - 1);
                    if (secondToLastDot != -1) {
                        layoutName = fullName.substring(secondToLastDot + 1, lastDot);
                    } else {
                        layoutName = fullName.substring(lastDot + 1);
                    }
                } catch (Exception e) {
                    layoutName = "default";
                }
            }
        }
        return new SudokuKey(sudoku.getVariant(), sudoku.getType(), layoutName, sudoku.getDifficulty());
    }

    private static Long getExtrapolatedSudokuLimit(SudokuKey key, Map<SudokuKey, Long> sourceMap) {
        if (sudokuLi) return Long.MAX_VALUE;
        Long val = sourceMap.get(key);
        if (val != null && val > 0) return val;
        boolean isGen = (sourceMap == sudokuGenLimits);
        boolean isDel = (sourceMap == sudokuDelLimits);
        boolean isSolve = (sourceMap == sudokuSolveLimits);
        if (key.variant() == SudokuVariant.IRREGULAR && key.size() == SudokuSize.NINE) {
            if (isGen) return 5_000_000L;
            if (isDel) return 300L;
            if (isSolve) return 16_000L;
        }
        if (key.variant() == SudokuVariant.PATTERNED) {
            if (key.size() == SudokuSize.EIGHT) {
                if (isGen) return 200_000L;
                if (isDel) return 150L;
                if (isSolve) return 4_500L;
            } else if (key.size() == SudokuSize.NINE) {
                if (isGen) return 1_000_000L;
                if (isDel) return 250L;
                if (isSolve) return 7_500L;
            } else if (key.size() == SudokuSize.TEN) {
                if (isGen) return 3_500_000L;
                if (isDel) return 400L;
                if (isSolve) return 18_000L;
            }
        }
        if (key.difficulty() != Difficulty.EASY) {
            SudokuKey easyKey = new SudokuKey(key.variant(), key.size(), key.layoutName(), Difficulty.EASY);
            Long easyVal = sourceMap.get(easyKey);
            if (easyVal != null && easyVal > 0) {
                return applyDifficultyMultiplier(easyVal, key.difficulty());
            }
        }
        if (key.size() == SudokuSize.EIGHT || key.size() == SudokuSize.TEN) {
            SudokuKey base9Key = new SudokuKey(key.variant(), SudokuSize.NINE, "default", key.difficulty());
            Long base9Val = getExtrapolatedSudokuLimit(base9Key, sourceMap);

            if (base9Val != null && base9Val != Long.MAX_VALUE) {
                double currentCells = (key.size() == SudokuSize.EIGHT) ? 64.0 : 100.0;
                double ratio = currentCells / 81.0;
                double scaleFactor = Math.pow(ratio, 2.0);
                return Math.round(base9Val * scaleFactor);
            }
        }

        return Long.MAX_VALUE;
    }

    private static Long resolveMazeVariantLimit(MazeVariantKey key) {
        if (variantsTest) return Long.MAX_VALUE;

        Long exactLimit = mazeVariantLimits.get(key);
        if (exactLimit != null) return exactLimit;

        if (key.difficulty() != Difficulty.EASY) {
            var easyKey = new MazeVariantKey(key.type(), Difficulty.EASY, key.width(), key.height());
            Long easyLimit = mazeVariantLimits.get(easyKey);
            if (easyLimit != null) {
                return applyDifficultyMultiplier(easyLimit, key.difficulty());
            }
        }

        int width = key.width();
        int height = key.height();
        int equivalentN = (int) Math.round(Math.sqrt(width * height));

        var squareKey = new MazeVariantKey(key.type(), key.difficulty(), equivalentN, equivalentN);
        Long squareLimit = mazeVariantLimits.get(squareKey);

        if (squareLimit == null && key.difficulty() != Difficulty.EASY) {
            var easySquareKey = new MazeVariantKey(key.type(), Difficulty.EASY, equivalentN, equivalentN);
            Long easySquareLimit = mazeVariantLimits.get(easySquareKey);
            if (easySquareLimit != null) {
                squareLimit = applyDifficultyMultiplier(easySquareLimit, key.difficulty());
            }
        }

        if (squareLimit != null) {
            if (width != height) {
                double aspectRatio = (double) Math.max(width, height) / Math.min(width, height);
                if (aspectRatio > 1.3) {
                    double bonusFactor = 1.0 + (Math.log(aspectRatio) * 0.2);
                    bonusFactor = Math.clamp(bonusFactor, 1.0, 1.4);
                    return Math.round(squareLimit * bonusFactor);
                }
            }
            return squareLimit;
        }

        return calculateMathematicalMazeVariantLimit(width, height, key.type(), key.difficulty());
    }

    private static long calculateMathematicalMazeVariantLimit(int width, int height, MazeType type, Difficulty difficulty) {
        long area = (long) width * height;
        long baseSteps = 1000L * area;

        double variantMultiplier = switch (type) {
            case CLASSIC -> 1.0;
            case WRAP_AROUND -> 1.5;
            case EXACT_STEPS -> 2.0;
            case WALLS, PATTERN, CHECKPOINT -> 3.5;
            case PORTAL -> 2.5;
            case MULTI_LEVEL -> 0.0;
        };

        long calculatedBase = (long) (baseSteps * variantMultiplier);

        if (area > 625) {
            calculatedBase += Math.round(Math.pow(area, 1.6));
        }

        return switch (difficulty) {
            case EASY -> Math.min(calculatedBase, 5_000_000L);
            case MEDIUM -> Math.min(calculatedBase * 3, 15_000_000L);
            case HARD -> Math.min(calculatedBase * 8, 40_000_000L);
        };
    }

    private static Integer resolveBridgeIslandLimit(BridgeLimitKey key) {
        Integer exactLimit = bridgeIslandLimits.get(key);
        if (exactLimit != null) return exactLimit;

        int width = key.width();
        int height = key.height();
        Difficulty difficulty = key.difficulty();
        int equivalentN = (int) Math.round(Math.sqrt(width * height));

        var squareKey = new BridgeLimitKey(key.bridgeMultipleCount(), equivalentN, equivalentN, difficulty);
        Integer squareLimit = bridgeIslandLimits.get(squareKey);

        if (squareLimit == null && key.bridgeMultipleCount() > 2) {
            var baseSquareKey = new BridgeLimitKey(2, equivalentN, equivalentN, difficulty);
            squareLimit = bridgeIslandLimits.get(baseSquareKey);
        }

        if (squareLimit == null) return 0;

        if (width != height) {
            double aspectRatio = (double) Math.max(width, height) / Math.min(width, height);
            if (aspectRatio > 1.5) {
                double reductionFactor = 1.0 - (Math.log(aspectRatio) * 0.15);
                reductionFactor = Math.clamp(reductionFactor, 0.6, 1.0);
                return (int) Math.round(squareLimit * reductionFactor);
            }
        }
        return squareLimit;
    }

    private static Integer resolveBridgeIslandLowerLimit(BridgeLimitKey key) {
        Integer exactLimit = bridgeIslandLowerLimits.get(key);
        if (exactLimit != null) return exactLimit;

        int width = key.width();
        int height = key.height();
        Difficulty difficulty = key.difficulty();
        int equivalentN = (int) Math.round(Math.sqrt(width * height));

        var squareKey = new BridgeLimitKey(key.bridgeMultipleCount(), equivalentN, equivalentN, difficulty);
        Integer squareLimit = bridgeIslandLowerLimits.get(squareKey);

        if (squareLimit == null && key.bridgeMultipleCount() > 2) {
            var baseSquareKey = new BridgeLimitKey(2, equivalentN, equivalentN, difficulty);
            squareLimit = bridgeIslandLowerLimits.get(baseSquareKey);
        }

        if (squareLimit == null) return 0;

        return squareLimit;
    }

    public static BridgeKey constructKey(Bridge bridge) {
        return new BridgeKey(bridge.getMaxMultipleBridges(), bridge.getIslands().size(), bridge.getDifficulty(), bridge.getWidth(), bridge.getHeight());
    }

    public static BridgeKey constructKey(Bridge bridge, int islandCount) {
        return new BridgeKey(bridge.getMaxMultipleBridges(), islandCount, bridge.getDifficulty(), bridge.getWidth(), bridge.getHeight());
    }

    public static MazeKey constructKey(Maze maze) {
        return new MazeKey(maze.getMazeAlgorithm(), maze.getMazeShape(), maze.getWidth(), maze.getHeight());
    }

    public static ShikakuKey constructKey(Shikaku shikaku) {
        return new ShikakuKey(shikaku.getShikakuType(), shikaku.getDifficulty(), shikaku.getWidth(), shikaku.getHeight());
    }

    private static Long resolveBridgeLimit(BridgeKey key) {
        if (bridgeTest) return Long.MAX_VALUE;

        Long exactLimit = bridgeGenLimits.get(key);
        if (exactLimit != null) return exactLimit;

        if (key.difficulty() != Difficulty.EASY) {
            var easyKey = new BridgeKey(key.bridgeMultipleCount(), key.islandCount(), Difficulty.EASY, key.width(), key.height());
            Long easyLimit = bridgeGenLimits.get(easyKey);
            if (easyLimit != null) {
                return applyDifficultyMultiplier(easyLimit, key.difficulty());
            }
        }

        int width = key.width();
        int height = key.height();
        int equivalentN = (int) Math.round(Math.sqrt(width * height));

        var squareKey = new BridgeKey(key.bridgeMultipleCount(), key.islandCount(), key.difficulty(), equivalentN, equivalentN);
        Long squareLimit = bridgeGenLimits.get(squareKey);

        if (squareLimit == null && key.difficulty() != Difficulty.EASY) {
            var easySquareKey = new BridgeKey(key.bridgeMultipleCount(), key.islandCount(), Difficulty.EASY, equivalentN, equivalentN);
            Long easySquareLimit = bridgeGenLimits.get(easySquareKey);
            if (easySquareLimit != null) {
                squareLimit = applyDifficultyMultiplier(easySquareLimit, key.difficulty());
            }
        }

        if (squareLimit != null) {
            if (width != height) {
                double aspectRatio = (double) Math.max(width, height) / Math.min(width, height);
                if (aspectRatio > 1.5) {
                    double bonusFactor = 1.0 + (Math.log(aspectRatio) * 0.2);
                    bonusFactor = Math.clamp(bonusFactor, 1.0, 1.4);
                    return Math.round(squareLimit * bonusFactor);
                }
            }
            return squareLimit;
        }

        if (key.bridgeMultipleCount() > 2) {
            var baseKey2 = new BridgeKey(2, key.islandCount(), key.difficulty(), width, height);
            Long baseLimit2 = bridgeGenLimits.get(baseKey2);

            if (baseLimit2 == null) {
                var baseSquareKey2 = new BridgeKey(2, key.islandCount(), key.difficulty(), equivalentN, equivalentN);
                baseLimit2 = bridgeGenLimits.get(baseSquareKey2);
            }

            if (baseLimit2 == null && key.difficulty() != Difficulty.EASY) {
                var easyBaseKey2 = new BridgeKey(2, key.islandCount(), Difficulty.EASY, width, height);
                baseLimit2 = bridgeGenLimits.get(easyBaseKey2);
                if (baseLimit2 == null) {
                    var easyBaseSquareKey2 = new BridgeKey(2, key.islandCount(), Difficulty.EASY, equivalentN, equivalentN);
                    baseLimit2 = bridgeGenLimits.get(easyBaseSquareKey2);
                }
                if (baseLimit2 != null) {
                    baseLimit2 = applyDifficultyMultiplier(baseLimit2, key.difficulty());
                }
            }

            if (baseLimit2 != null) {
                return scaleLimitFromBaseline(baseLimit2, 2, key.bridgeMultipleCount(), key.islandCount());
            }
        }
        return 50_000_000L;
    }

    private static Long resolveBridgeSolveLimit(BridgeKey key) {
        if (bridgeTest) return Long.MAX_VALUE;

        Long exactLimit = bridgeSolveLimits.get(key);
        if (exactLimit != null) return exactLimit;

        if (key.difficulty() != Difficulty.EASY) {
            var easyKey = new BridgeKey(key.bridgeMultipleCount(), key.islandCount(), Difficulty.EASY, key.width(), key.height());
            Long easyLimit = bridgeSolveLimits.get(easyKey);
            if (easyLimit != null) {
                long finalLimit = applyDifficultyMultiplier(easyLimit, key.difficulty());
                if (key.bridgeMultipleCount() > 2) {
                    finalLimit = Math.round(finalLimit * Math.pow(1.5, key.bridgeMultipleCount() - 2));
                }
                return finalLimit;
            }
        }

        int width = key.width();
        int height = key.height();
        int equivalentN = (int) Math.round(Math.sqrt(width * height));

        var squareKey = new BridgeKey(key.bridgeMultipleCount(), key.islandCount(), key.difficulty(), equivalentN, equivalentN);
        Long squareLimit = bridgeSolveLimits.get(squareKey);

        if (squareLimit == null && key.difficulty() != Difficulty.EASY) {
            var easySquareKey = new BridgeKey(key.bridgeMultipleCount(), key.islandCount(), Difficulty.EASY, equivalentN, equivalentN);
            Long easySquareLimit = bridgeSolveLimits.get(easySquareKey);
            if (easySquareLimit != null) {
                squareLimit = applyDifficultyMultiplier(easySquareLimit, key.difficulty());
            }
        }

        if (squareLimit != null) {
            if (width != height) {
                double aspectRatio = (double) Math.max(width, height) / Math.min(width, height);
                if (aspectRatio > 1.5) {
                    double bonusFactor = 1.0 + (Math.log(aspectRatio) * 0.2);
                    bonusFactor = Math.clamp(bonusFactor, 1.0, 1.4);
                    squareLimit = Math.round(squareLimit * bonusFactor);
                }
            }
            if (key.bridgeMultipleCount() > 2) {
                squareLimit = Math.round(squareLimit * Math.pow(1.6, key.bridgeMultipleCount() - 2));
            }
            return squareLimit;
        }

        if (key.bridgeMultipleCount() > 2) {
            if (key.difficulty() != Difficulty.EASY) {
                var easyMultipleKey = new BridgeKey(key.bridgeMultipleCount(), key.islandCount(), Difficulty.EASY, width, height);
                Long easyMultipleLimit = bridgeSolveLimits.get(easyMultipleKey);
                if (easyMultipleLimit == null) {
                    var easyMultipleSquareKey = new BridgeKey(key.bridgeMultipleCount(), key.islandCount(), Difficulty.EASY, equivalentN, equivalentN);
                    easyMultipleLimit = bridgeSolveLimits.get(easyMultipleSquareKey);
                }
                if (easyMultipleLimit != null) {
                    long finalLimit = applyDifficultyMultiplier(easyMultipleLimit, key.difficulty());
                    return Math.round(finalLimit * 1.3);
                }
            }

            var baseKey2 = new BridgeKey(2, key.islandCount(), key.difficulty(), width, height);
            Long baseLimit2 = bridgeSolveLimits.get(baseKey2);

            if (baseLimit2 == null) {
                var baseSquareKey2 = new BridgeKey(2, key.islandCount(), key.difficulty(), equivalentN, equivalentN);
                baseLimit2 = bridgeSolveLimits.get(baseSquareKey2);
            }

            if (baseLimit2 != null) {
                long scaled = scaleLimitFromBaseline(baseLimit2, 2, key.bridgeMultipleCount(), key.islandCount());
                double multipleBonus = key.bridgeMultipleCount() == 3 ? 1.8 : 3.5;
                return Math.round(scaled * multipleBonus);
            }

            if (key.difficulty() != Difficulty.EASY) {
                var easyBaseKey2 = new BridgeKey(2, key.islandCount(), Difficulty.EASY, width, height);
                Long rawEasyLimit = bridgeSolveLimits.get(easyBaseKey2);
                if (rawEasyLimit == null) {
                    var easyBaseSquareKey2 = new BridgeKey(2, key.islandCount(), Difficulty.EASY, equivalentN, equivalentN);
                    rawEasyLimit = bridgeSolveLimits.get(easyBaseSquareKey2);
                }
                if (rawEasyLimit != null) {
                    long scaled = scaleLimitFromBaseline(rawEasyLimit, 2, key.bridgeMultipleCount(), key.islandCount());
                    long finalLimit = applyDifficultyMultiplier(scaled, key.difficulty());
                    double multipleBonus = key.bridgeMultipleCount() == 3 ? 2.0 : 4.5;
                    return Math.round(finalLimit * multipleBonus);
                }
            }
        }

        long mathLimit = calculateMathematicalBridgeLimit(key.islandCount(), key.bridgeMultipleCount());
        if (key.bridgeMultipleCount() > 2) {
            mathLimit = Math.round(mathLimit * (key.bridgeMultipleCount() == 3 ? 2.5 : 6.0));
        }
        return mathLimit;
    }

    private static long applyDifficultyMultiplier(long easyLimit, Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> easyLimit;
            case MEDIUM -> (easyLimit * 2) + 1_000L;
            case HARD -> (easyLimit * 5) + 5_000L;
        };
    }

    public static long calculateMathematicalBridgeLimit(int islands, int maxBridges) {
        if (maxBridges <= 2) return 200L * islands * islands;
        long baseLimit = 500L * islands * islands;
        double exponent = Math.min(islands / 5.0, 10.0);
        double exponentialScale = Math.pow((maxBridges + 1.0) / 3.0, exponent);
        long calculatedLimit = (long) (baseLimit * exponentialScale);
        return Math.min(calculatedLimit, 50_000_000L);
    }

    private static Long resolveShikakuGenLimit(ShikakuKey key) {
        if (shikakuCount) return Long.MAX_VALUE;

        Long exactLimit = shikakuGenLimits.get(key);
        if (exactLimit != null) return exactLimit;

        if (key.difficulty() != Difficulty.EASY) {
            var easyKey = new ShikakuKey(key.shikakuType(), Difficulty.EASY, key.width(), key.height());
            Long easyLimit = shikakuGenLimits.get(easyKey);
            if (easyLimit != null) {
                return applyDifficultyMultiplier(easyLimit, key.difficulty());
            }
        }

        int width = key.width();
        int height = key.height();
        int equivalentN = (int) Math.round(Math.sqrt(width * height));

        var squareKey = new ShikakuKey(key.shikakuType(), key.difficulty(), equivalentN, equivalentN);
        Long squareLimit = shikakuGenLimits.get(squareKey);

        if (squareLimit == null && key.difficulty() != Difficulty.EASY) {
            var easySquareKey = new ShikakuKey(key.shikakuType(), Difficulty.EASY, equivalentN, equivalentN);
            Long easySquareLimit = shikakuGenLimits.get(easySquareKey);
            if (easySquareLimit != null) {
                squareLimit = applyDifficultyMultiplier(easySquareLimit, key.difficulty());
            }
        }

        if (squareLimit != null) {
            if (width != height) {
                double aspectRatio = (double) Math.max(width, height) / Math.min(width, height);
                if (aspectRatio > 1.3) {
                    double bonusFactor = 1.0 + (Math.log(aspectRatio) * 0.25);
                    bonusFactor = Math.clamp(bonusFactor, 1.0, 1.5);
                    return Math.round(squareLimit * bonusFactor);
                }
            }
            return squareLimit;
        }

        return calculateMathematicalShikakuLimit(width, height, key.difficulty());
    }

    private static Long resolveShikakuSolveLimit(ShikakuKey key) {
        if (shikakuCount) return Long.MAX_VALUE;

        Long exactLimit = shikakuSolveLimits.get(key);
        if (exactLimit != null) return exactLimit;

        if (key.difficulty() != Difficulty.EASY) {
            var easyKey = new ShikakuKey(key.shikakuType(), Difficulty.EASY, key.width(), key.height());
            Long easyLimit = shikakuSolveLimits.get(easyKey);
            if (easyLimit != null) {
                return applyDifficultyMultiplier(easyLimit, key.difficulty());
            }
        }

        int width = key.width();
        int height = key.height();
        int equivalentN = (int) Math.round(Math.sqrt(width * height));

        var squareKey = new ShikakuKey(key.shikakuType(), key.difficulty(), equivalentN, equivalentN);
        Long squareLimit = shikakuSolveLimits.get(squareKey);

        if (squareLimit == null && key.difficulty() != Difficulty.EASY) {
            var easySquareKey = new ShikakuKey(key.shikakuType(), Difficulty.EASY, equivalentN, equivalentN);
            Long easySquareLimit = sudokuGenLimits.get(easySquareKey);
            if (easySquareLimit != null) {
                squareLimit = applyDifficultyMultiplier(easySquareLimit, key.difficulty());
            }
        }

        if (squareLimit != null) {
            if (width != height) {
                double aspectRatio = (double) Math.max(width, height) / Math.min(width, height);
                if (aspectRatio > 1.3) {
                    double bonusFactor = 1.0 + (Math.log(aspectRatio) * 0.2);
                    bonusFactor = Math.clamp(bonusFactor, 1.0, 1.4);
                    return Math.round(squareLimit * bonusFactor);
                }
            }
            return squareLimit;
        }

        long baseSolveLimit = calculateMathematicalShikakuLimit(width, height, key.difficulty());
        return Math.round(baseSolveLimit * 1.5);
    }

    private static long calculateMathematicalShikakuLimit(int width, int height, Difficulty difficulty) {
        long area = (long) width * height;
        long baseSteps = 500L * area;
        if (area > 100) {
            baseSteps += Math.round(Math.pow(area, 1.8));
        }

        return switch (difficulty) {
            case EASY -> Math.min(baseSteps, 2_000_000L);
            case MEDIUM -> Math.min(baseSteps * 3, 10_000_000L);
            case HARD -> Math.min(baseSteps * 8, 30_000_000L);
        };
    }

    public static long calculateMathematicalMazeLimit(MazeAlgorithm algo, MazeShape shape, int width, int height) {
        int v = width * height;
        int logV = 31 - Integer.numberOfLeadingZeros(v);
        int e = (shape == MazeShape.HEXAGONAL) ? 3 * v : 2 * v;
        return switch (algo) {
            case RECURSIVE_BACKTRACKER, HUNT_AND_KILL, ELLER, BINARY_TREE, SIDEWINDER -> 10L * (v + e);
            case KRUSKAL, PRIM, RECURSIVE_DIVISION -> (10L * e * logV);
            case ALDOUS_BRODER, WILSON -> 100L * v * v;
        };
    }

    private static Long resolveMazeLimit(MazeKey key) {
        Long value = mazeGenLimits.get(key);
        if (value != null) return value;
        int equivalentN = (int) Math.round(Math.sqrt(key.width() * key.height()));
        var squareKey = new MazeKey(key.algo(), key.shape(), equivalentN, equivalentN);
        Long squareLimit = mazeGenLimits.get(squareKey);
        if (squareLimit != null) return squareLimit;
        return calculateMathematicalMazeLimit(key.algo(), key.shape(), key.width(), key.height());
    }

    public record SudokuKey(SudokuVariant variant, SudokuSize size, String layoutName, Difficulty difficulty) {
    }

    public record MazeKey(MazeAlgorithm algo, MazeShape shape, int width, int height) {
    }

    public record BridgeKey(int bridgeMultipleCount, int islandCount, Difficulty difficulty, int width, int height) {
    }

    public record BridgeLimitKey(int bridgeMultipleCount, int width, int height, Difficulty difficulty) {
    }

    public record ShikakuKey(ShikakuType shikakuType, Difficulty difficulty, int width, int height) {
    }

    public record MazeVariantKey(MazeType type, Difficulty difficulty, int width, int height) {
    }

    public static class StatsData {
        public List<SudokuGenStat> sudokuGen;
        public List<MazeGenStat> mazeGen;
        public List<BridgeGenStat> bridgeGen;
        public List<ShikakuGenStat> shikakuGen;
        public List<BridgeSolveStat> bridgeSolve;
        public List<MazeVariantStat> mazeVariants;
    }

    public static class SudokuGenStat {
        public SudokuVariant variant;
        public SudokuSize size;
        public String layoutName;
        public Difficulty difficulty;
        public long limitGenSteps;
        public long limitDelSteps;
        public long limitSolveSteps;
    }

    public static class BridgeGenStat {
        public int width;
        public int height;
        public int islands;
        public int maxBridges;
        public Difficulty difficulty;
        public long limitSteps;
    }

    public static class BridgeSolveStat {
        public int width;
        public int height;
        public int islands;
        public int maxBridges;
        public Difficulty difficulty;
        public long limitSteps;
    }

    public static class MazeGenStat {
        public MazeAlgorithm algo;
        public MazeShape shape;
        public int width;
        public int height;
        public Long limitSteps;
    }

    public static class MazeVariantStat {
        public int width;
        public int height;
        public MazeType type;
        public Difficulty difficulty;
        public long limitSteps;
    }

    public static class ShikakuGenStat {
        public int width;
        public int height;
        public ShikakuType type;
        public Difficulty difficulty;
        public long limitGenSteps;
        public long limitSolveSteps;
    }
}
