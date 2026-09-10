package cz.logicgo.engine.algorithms.bridge;

import cz.logicgo.core.builders.bridge.BridgeCreation;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.gameClasses.bridge.Island;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.engine.algorithms.bridges.BridgesGenerator;
import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics;
import cz.logicgo.engine.algorithms.sudoku.provider.TestGenerationLimitsProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BridgeGeneratorDeterminismTest {

    private static final int SEED_ATTEMPTS_PER_CONFIG = 10;

    @AfterEach
    void tearDown() {
        GenerationStatistics.resetProvider();
    }

    static Stream<Arguments> provideBridgeConfigs() {
        return Stream.of(
                Arguments.of(Difficulty.EASY, 2, 5, 5, 7352819L),
        Arguments.of(Difficulty.EASY, 2, 7, 7, 4829104L),
        Arguments.of(Difficulty.EASY, 2, 9, 9, 1928374L),
        Arguments.of(Difficulty.EASY, 2, 11, 11, 9918273L),
        Arguments.of(Difficulty.EASY, 2, 13, 13, 3819204L),
        Arguments.of(Difficulty.MEDIUM, 2, 10, 10, 5521902L),
        Arguments.of(Difficulty.MEDIUM, 3, 8, 8, 8472910L),
        Arguments.of(Difficulty.MEDIUM, 3, 10, 10, 6102934L),
        Arguments.of(Difficulty.MEDIUM, 3, 12, 12, 7728190L),
        Arguments.of(Difficulty.HARD, 3, 10, 10, 1420953L),
        Arguments.of(Difficulty.HARD, 3, 12, 12, 9301824L),
        Arguments.of(Difficulty.HARD, 4, 7, 7, 2291048L),
        Arguments.of(Difficulty.HARD, 4, 9, 9, 6638201L),
        Arguments.of(Difficulty.HARD, 4, 10, 10, 3192048L)
        );
    }

    @ParameterizedTest(name = "Bridge Parity: {0}, MaxMostů: {1}, Rozměr: {2}x{3}")
    @MethodSource("provideBridgeConfigs")
    void testBridgeDeterminism(Difficulty difficulty, int maxBridges, int width, int height, long baseSeed) {
        int successfulRuns = 0;
        int bothFailedRuns = 0;

        for (int i = 0; i < SEED_ATTEMPTS_PER_CONFIG; i++) {
            long currentSeed = baseSeed + (i * 1009L);

            BridgeCreation config1 = buildConfig(currentSeed, difficulty, maxBridges, width, height);
            BridgeCreation config2 = buildConfig(currentSeed, difficulty, maxBridges, width, height);

            Bridge bridge1 = null;
            Bridge bridge2 = null;
            Class<?> exceptionType1 = null;
            Class<?> exceptionType2 = null;

            try {
                bridge1 = BridgesGenerator.generateFullBridges(config1);
            } catch (Exception e) {
                exceptionType1 = e.getClass();
            }

            try {
                bridge2 = BridgesGenerator.generateFullBridges(config2);
            } catch (Exception e) {
                exceptionType2 = e.getClass();
            }

            assertEquals(exceptionType1, exceptionType2, "Neshoda ve vyvolané výjimce pro seed " + currentSeed);
            assertEquals(bridge1 == null, bridge2 == null, "Neshoda v null stavu pro seed " + currentSeed);

            if (bridge1 == null) {
                bothFailedRuns++;
                continue;
            }

            successfulRuns++;
            compareBridges(bridge1, bridge2);
            validateBridgeIntegrity(bridge1, width, height, maxBridges);
        }

        assertTrue(successfulRuns > 0 || bothFailedRuns > 0);
    }

    @ParameterizedTest(name = "Strict Limits Parity: {0}, Rozměr: {2}x{3}")
    @MethodSource("provideBridgeConfigs")
    void testCreationParityWithStrictLimits(Difficulty difficulty, int maxBridges, int width, int height, long seed) {
        BridgeCreation config1 = buildConfig(seed, difficulty, maxBridges, width, height);
        BridgeCreation config2 = buildConfig(seed, difficulty, maxBridges, width, height);

        TestGenerationLimitsProvider testProvider = new TestGenerationLimitsProvider()
                .withDefaultLimit(1L);

        GenerationStatistics.setProvider(testProvider);
        try {
            Bridge res1 = null;
            Bridge res2 = null;
            boolean failed1 = false;
            boolean failed2 = false;

            try {
                res1 = BridgesGenerator.generateFullBridges(config1);
            } catch (ThreadTerminationException e) {
                failed1 = true;
            }

            try {
                res2 = BridgesGenerator.generateFullBridges(config2);
            } catch (ThreadTerminationException e) {
                failed2 = true;
            }

            assertEquals(failed1, failed2);
            assertEquals(res1 == null, res2 == null);

            if (res1 != null) {
                compareBridges(res1, res2);
            }
        } finally {
            GenerationStatistics.resetProvider();
        }
    }

    @ParameterizedTest(name = "Thread Interruption Parity: {0}, Rozměr: {2}x{3}")
    @MethodSource("provideBridgeConfigs")
    void testInterruptionParity(Difficulty difficulty, int maxBridges, int width, int height, long seed) {
        BridgeCreation config1 = buildConfig(seed, difficulty, maxBridges, width, height);
        BridgeCreation config2 = buildConfig(seed, difficulty, maxBridges, width, height);

        boolean interrupted1 = false;
        boolean interrupted2 = false;

        Thread.currentThread().interrupt();
        try {
            BridgesGenerator.generateFullBridges(config1);
        } catch (ThreadTerminationException e) {
            interrupted1 = true;
        } catch (Exception ignored) {
        } finally {
            Thread.interrupted();
        }

        Thread.currentThread().interrupt();
        try {
            BridgesGenerator.generateFullBridges(config2);
        } catch (ThreadTerminationException e) {
            interrupted2 = true;
        } catch (Exception ignored) {
        } finally {
            Thread.interrupted();
        }

        assertEquals(interrupted1, interrupted2);
    }

    @Test
    void testDifferentSeedsProduceDifferentIslands() {
        boolean verified = false;

        for (int i = 0; i < 15; i++) {
            long s1Seed = 1111L + i;
            long s2Seed = 9999L + i;

            BridgeCreation c1 = buildConfig(s1Seed, Difficulty.EASY, 2, 10, 10);
            BridgeCreation c2 = buildConfig(s2Seed, Difficulty.EASY, 2, 10, 10);

            Bridge b1 = null;
            Bridge b2 = null;

            try {
                b1 = BridgesGenerator.generateFullBridges(c1);
                b2 = BridgesGenerator.generateFullBridges(c2);
            } catch (Exception ignored) {
                continue;
            }

            if (b1 == null || b2 == null) {
                continue;
            }

            List<Island> islands1 = new ArrayList<>(b1.getIslands());
            List<Island> islands2 = new ArrayList<>(b2.getIslands());

            if (islands1.size() != islands2.size()) {
                verified = true;
                break;
            }

            islands1.sort(Comparator.comparingInt(Island::getRow).thenComparingInt(Island::getCol));
            islands2.sort(Comparator.comparingInt(Island::getRow).thenComparingInt(Island::getCol));

            boolean identical = true;
            for (int j = 0; j < islands1.size(); j++) {
                Island i1 = islands1.get(j);
                Island i2 = islands2.get(j);
                if (i1.getRow() != i2.getRow() || i1.getCol() != i2.getCol() || i1.getBridgeCount() != i2.getBridgeCount()) {
                    identical = false;
                    break;
                }
            }

            if (!identical) {
                verified = true;
                break;
            }
        }

        assertTrue(verified, "Nepodařilo se vygenerovat validní dvojici pro porovnání seedů");
    }

    private void validateBridgeIntegrity(Bridge bridge, int width, int height, int maxBridgesPerDirection) {
        List<Island> islands = bridge.getIslands();
        assertNotNull(islands);
        assertFalse(islands.isEmpty(), "Generátor vrátil prázdný seznam ostrovů");

        boolean[][] occupied = new boolean[height][width];

        for (Island island : islands) {
            int r = island.getRow();
            int c = island.getCol();

            assertTrue(r >= 0 && r < height, "Ostrov mimo řádky hrací plochy: " + r);
            assertTrue(c >= 0 && c < width, "Ostrov mimo sloupce hrací plochy: " + c);
            assertFalse(occupied[r][c], String.format("Překryv dvou ostrovů na souřadnici [%d,%d]", r, c));
            occupied[r][c] = true;

            int count = island.getBridgeCount();
            assertTrue(count > 0, "Ostrov má neplatný počet mostů: " + count);
            assertTrue(count <= 4 * maxBridgesPerDirection, "Ostrov překračuje maximální možný počet mostů pro 4 směry");
        }
    }

    private void compareBridges(Bridge b1, Bridge b2) {
        List<Island> islands1 = b1.getIslands();
        List<Island> islands2 = b2.getIslands();

        assertNotNull(islands1);
        assertNotNull(islands2);
        assertEquals(islands1.size(), islands2.size(), "Rozdílný počet ostrovů mezi deterministickými běhy");

        List<Island> sorted1 = new ArrayList<>(islands1);
        List<Island> sorted2 = new ArrayList<>(islands2);

        sorted1.sort(Comparator.comparingInt(Island::getRow).thenComparingInt(Island::getCol));
        sorted2.sort(Comparator.comparingInt(Island::getRow).thenComparingInt(Island::getCol));

        for (int i = 0; i < sorted1.size(); i++) {
            Island i1 = sorted1.get(i);
            Island i2 = sorted2.get(i);

            assertEquals(i1.getRow(), i2.getRow(), "Odlišný řádek ostrova");
            assertEquals(i1.getCol(), i2.getCol(), "Odlišný sloupec ostrova");
            assertEquals(i1.getBridgeCount(), i2.getBridgeCount(),
            String.format("Odlišný počet požadovaných mostů na ostrově [%d,%d]", i1.getRow(), i1.getCol()));
        }
    }

    private BridgeCreation buildConfig(long seed, Difficulty difficulty, int maxBridges, int width, int height) {
        return new BridgeCreation()
                .setSeed(seed)
                .setDifficulty(difficulty)
                .setMaxBridgeCount(maxBridges)
                .setNumberOfIslands(-1)
                .setHeight(height)
                .setWidth(width)
                .setForExport(false);
    }
}
