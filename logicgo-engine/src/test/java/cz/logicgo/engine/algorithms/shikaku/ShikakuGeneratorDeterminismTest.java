package cz.logicgo.engine.algorithms.shikaku;

import cz.logicgo.core.builders.shikaku.ShikakuCreation;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics;
import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics.ShikakuKey;
import cz.logicgo.engine.algorithms.sudoku.provider.TestGenerationLimitsProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ShikakuGeneratorDeterminismTest {

    private static final int SEED_ATTEMPTS_PER_CONFIG = 12;

    @AfterEach
    void tearDown() {
        GenerationStatistics.resetProvider();
    }

    static Stream<Arguments> provideShikakuConfigs() {
        List<Arguments> args = new ArrayList<>();
        long[] baseSeeds = {8462944L, 1928374L, 56554152L, 1544564645L};

        record GridDim(int w, int h) {}
        List<GridDim> dimensions = List.of(
                new GridDim(5, 5),
                new GridDim(7, 7),
                new GridDim(8, 12),
                new GridDim(10, 10),
                new GridDim(12, 8),
                new GridDim(15, 15),
                new GridDim(20, 20)
        );

        for (ShikakuType type : ShikakuType.values()) {
            for (GridDim dim : dimensions) {
                if (type == ShikakuType.OFF_BY_ONE && (dim.w > 13 || dim.h > 13)) {
                    continue;
                }

                for (Difficulty diff : Difficulty.values()) {
                    for (long seed : baseSeeds) {
                        args.add(Arguments.of(type, dim.w, dim.h, diff, seed));
                    }
                }
            }
        }

        return args.stream();
    }

    @ParameterizedTest(name = "Board Parity: {0}, {1}x{2}, {3}")
    @MethodSource("provideShikakuConfigs")
    void testShikakuDeterminismAndBoardIntegrity(ShikakuType type, int width, int height, Difficulty difficulty, long baseSeed) {
        int successfulRuns = 0;
        int bothFailedRuns = 0;

        for (int i = 0; i < SEED_ATTEMPTS_PER_CONFIG; i++) {
            long currentSeed = baseSeed + (i * 1009L);

            ShikakuCreation config1 = buildConfig(currentSeed, type, width, height, difficulty);
            ShikakuCreation config2 = buildConfig(currentSeed, type, width, height, difficulty);

            Shikaku game1 = null;
            Shikaku game2 = null;
            Class<?> exceptionType1 = null;
            Class<?> exceptionType2 = null;

            try {
                game1 = ShikakuGenerator.generateGame(config1);
            } catch (Exception e) {
                exceptionType1 = e.getClass();
            }

            try {
                game2 = ShikakuGenerator.generateGame(config2);
            } catch (Exception e) {
                exceptionType2 = e.getClass();
            }

            assertEquals(exceptionType1, exceptionType2, "Neshoda ve vyvolané výjimce pro seed " + currentSeed);
            assertEquals(game1 == null, game2 == null, "Neshoda v null stavu pro seed " + currentSeed);

            if (game1 == null) {
                bothFailedRuns++;
                continue;
            }

            successfulRuns++;

            assertEquals(width, game1.getWidth());
            assertEquals(height, game1.getHeight());
            assertEquals(game1.getWidth(), game2.getWidth());
            assertEquals(game1.getHeight(), game2.getHeight());
            assertEquals(game1.getShikakuType(), game2.getShikakuType());
            assertEquals(game1.getDifficulty(), game2.getDifficulty());

            compareBoards(game1.getBoard(), game2.getBoard());
            validateBoardFullCoverageAndClues(game1.getBoard(), width, height, type);
        }

        assertTrue(successfulRuns > 0 || bothFailedRuns > 0);
    }

    @ParameterizedTest(name = "Strict Limits Parity: {0}, {1}x{2}")
    @MethodSource("provideShikakuConfigs")
    void testCreationParityWithStrictLimits(ShikakuType type, int width, int height, Difficulty difficulty, long seed) {
        ShikakuCreation config1 = buildConfig(seed, type, width, height, difficulty);
        ShikakuCreation config2 = buildConfig(seed, type, width, height, difficulty);

        ShikakuKey key = new ShikakuKey(type, difficulty, width, height);
        TestGenerationLimitsProvider testProvider = new TestGenerationLimitsProvider()
                .withShikakuGenLimit(key, 1L);

        GenerationStatistics.setProvider(testProvider);
        try {
            Shikaku res1 = null;
            Shikaku res2 = null;
            boolean limitHit1 = false;
            boolean limitHit2 = false;

            try {
                res1 = ShikakuGenerator.generateGame(config1);
            } catch (LimitReachedException e) {
                limitHit1 = true;
            } catch (ThreadTerminationException ignored) {
            }

            try {
                res2 = ShikakuGenerator.generateGame(config2);
            } catch (LimitReachedException e) {
                limitHit2 = true;
            } catch (ThreadTerminationException ignored) {
            }

            assertEquals(limitHit1, limitHit2);
            assertEquals(res1 == null, res2 == null);

            if (res1 != null) {
                compareBoards(res1.getBoard(), res2.getBoard());
            }
        } finally {
            GenerationStatistics.resetProvider();
        }
    }

    @ParameterizedTest(name = "Thread Interruption Parity: {0}, {1}x{2}")
    @MethodSource("provideShikakuConfigs")
    void testInterruptionParity(ShikakuType type, int width, int height, Difficulty difficulty, long seed) {
        ShikakuCreation config1 = buildConfig(seed, type, width, height, difficulty);
        ShikakuCreation config2 = buildConfig(seed, type, width, height, difficulty);

        boolean interrupted1 = false;
        boolean interrupted2 = false;

        Thread.currentThread().interrupt();
        try {
            ShikakuGenerator.generateGame(config1);
        } catch (ThreadTerminationException e) {
            interrupted1 = true;
        } catch (Exception ignored) {
        } finally {
            Thread.interrupted();
        }

        Thread.currentThread().interrupt();
        try {
            ShikakuGenerator.generateGame(config2);
        } catch (ThreadTerminationException e) {
            interrupted2 = true;
        } catch (Exception ignored) {
        } finally {
            Thread.interrupted();
        }

        assertEquals(interrupted1, interrupted2);
    }

    @Test
    void testDifferentSeedsProduceDifferentPartitions() {
        boolean verified = false;

        for (int i = 0; i < 15; i++) {
            long s1Seed = 1111L + i;
            long s2Seed = 9999L + i;

            ShikakuCreation c1 = buildConfig(s1Seed, ShikakuType.CLASSIC, 10, 10, Difficulty.EASY);
            ShikakuCreation c2 = buildConfig(s2Seed, ShikakuType.CLASSIC, 10, 10, Difficulty.EASY);

            Shikaku s1 = null;
            Shikaku s2 = null;

            try {
                s1 = ShikakuGenerator.generateGame(c1);
                s2 = ShikakuGenerator.generateGame(c2);
            } catch (Exception ignored) {
                continue;
            }

            if (s1 == null || s2 == null) {
                continue;
            }

            boolean identical = true;
            ShikakuCell[][] b1 = s1.getBoard();
            ShikakuCell[][] b2 = s2.getBoard();

            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    if (b1[r][c].getRegionId() != b2[r][c].getRegionId() || b1[r][c].getClue() != b2[r][c].getClue()) {
                        identical = false;
                        break;
                    }
                }
            }

            assertFalse(identical, "Různé seedy vygenerovaly shodné rozdělení");
            verified = true;
            break;
        }

        assertTrue(verified, "Nepodařilo se vygenerovat validní dvojici pro porovnání seedů");
    }

    private void validateBoardFullCoverageAndClues(ShikakuCell[][] board, int width, int height, ShikakuType type) {
        Map<Integer, Integer> regionCellCounts = new HashMap<>();
        Map<Integer, Integer> regionClueCounts = new HashMap<>();
        Map<Integer, Integer> regionClueValues = new HashMap<>();

        Map<Integer, Integer> minR = new HashMap<>();
        Map<Integer, Integer> maxR = new HashMap<>();
        Map<Integer, Integer> minC = new HashMap<>();
        Map<Integer, Integer> maxC = new HashMap<>();

        int totalCells = 0;

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                ShikakuCell cell = board[r][c];
                assertNotNull(cell, "Nalezena null buňka na pozici [" + r + "," + c + "]");

                int regionId = cell.getRegionId();
                assertTrue(regionId >= 0, "Záporné regionId na pozici [" + r + "," + c + "]");

                totalCells++;
                regionCellCounts.put(regionId, regionCellCounts.getOrDefault(regionId, 0) + 1);

                minR.put(regionId, Math.min(minR.getOrDefault(regionId, Integer.MAX_VALUE), r));
                maxR.put(regionId, Math.max(maxR.getOrDefault(regionId, Integer.MIN_VALUE), r));
                minC.put(regionId, Math.min(minC.getOrDefault(regionId, Integer.MAX_VALUE), c));
                maxC.put(regionId, Math.max(maxC.getOrDefault(regionId, Integer.MIN_VALUE), c));

                int clue = cell.getClue();
                if (clue > 0) {
                    regionClueCounts.put(regionId, regionClueCounts.getOrDefault(regionId, 0) + 1);
                    regionClueValues.put(regionId, clue);
                }
            }
        }

        assertEquals(width * height, totalCells);

        for (int regionId : regionCellCounts.keySet()) {
            assertEquals(1, regionClueCounts.getOrDefault(regionId, 0), "Region " + regionId + " nemá právě 1 stopu");

            int actualArea = regionCellCounts.get(regionId);
            int boxWidth = maxC.get(regionId) - minC.get(regionId) + 1;
            int boxHeight = maxR.get(regionId) - minR.get(regionId) + 1;

            assertEquals(boxWidth * boxHeight, actualArea, "Region " + regionId + " netvoří souvislý obdélník");

            int clueValue = regionClueValues.get(regionId);
            if (type == ShikakuType.CLASSIC) {
                assertEquals(actualArea, clueValue);
            } else if (type == ShikakuType.OFF_BY_ONE) {
                int diff = Math.abs(actualArea - clueValue);
                assertTrue(diff == 0 || diff == 1, "Stopa se liší o více než 1 pro region " + regionId);
            }
        }
    }

    private void compareBoards(ShikakuCell[][] b1, ShikakuCell[][] b2) {
        assertNotNull(b1);
        assertNotNull(b2);

        assertEquals(b1.length, b2.length);
        assertEquals(b1[0].length, b2[0].length);

        for (int r = 0; r < b1.length; r++) {
            for (int c = 0; c < b1[r].length; c++) {
                ShikakuCell c1 = b1[r][c];
                ShikakuCell c2 = b2[r][c];

                if (c1 == null || c2 == null) {
                    assertEquals(c1, c2);
                    continue;
                }

                assertEquals(c1.getRegionId(), c2.getRegionId(), "Neshoda v regionId na [" + r + "," + c + "]");
                assertEquals(c1.getClue(), c2.getClue(), "Neshoda ve stopě na [" + r + "," + c + "]");
            }
        }
    }

    private ShikakuCreation buildConfig(long seed, ShikakuType type, int width, int height, Difficulty difficulty) {
        return new ShikakuCreation()
                .setSeed(seed)
                .setShikakuType(type)
                .setDifficulty(difficulty)
                .setHeight(height)
                .setWidth(width)
                .setForExport(false);
    }
}
