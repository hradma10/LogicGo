package cz.logicgo.engine.algorithms.maze;

import cz.logicgo.core.builders.maze.MazeCreation;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics;
import cz.logicgo.engine.algorithms.mazes.MazeGenerator;
import cz.logicgo.engine.algorithms.sudoku.provider.TestGenerationLimitsProvider;
import cz.logicgo.engine.context.MazeBuildContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MazeGeneratorDeterminismTest {

    private static final int SEED_ATTEMPTS_PER_TYPE = 8;

    @AfterEach
    void tearDown() {
        GenerationStatistics.resetProvider();
    }

    static Stream<MazeType> provideSingleLevelTypes() {
        return Stream.of(
                MazeType.CLASSIC,
                MazeType.WALLS,
                MazeType.PORTAL,
                MazeType.PATTERN,
                MazeType.CHECKPOINT,
                MazeType.EXACT_STEPS,
                MazeType.WRAP_AROUND
        );
    }

    @ParameterizedTest(name = "Maze Parity: {0}")
    @MethodSource("provideSingleLevelTypes")
    void testSingleLevelDeterminism(MazeType mazeType) {
        long baseSeed = 12345L;
        int successfulRuns = 0;
        int bothFailedRuns = 0;

        for (int i = 0; i < SEED_ATTEMPTS_PER_TYPE; i++) {
            long currentSeed = baseSeed + (i * 1009L);

            MazeCreation config1 = buildSingleLevelConfig(currentSeed, mazeType);
            MazeCreation config2 = buildSingleLevelConfig(currentSeed, mazeType);

            Maze maze1 = MazeGenerator.createMaze(config1);
            Maze maze2 = MazeGenerator.createMaze(config2);

            assertEquals(maze1 == null, maze2 == null, "Neshoda v null stavu pro seed " + currentSeed);

            if (maze1 == null) {
                bothFailedRuns++;
                continue;
            }

            successfulRuns++;

            assertFalse(maze1.isHasMultipleFloors());
            assertEquals(maze1.isHasMultipleFloors(), maze2.isHasMultipleFloors());
            assertEquals(maze1.getMazeType(), maze2.getMazeType());

            compareGrids(maze1.getMazeGrid(), maze2.getMazeGrid());
        }

        assertTrue(successfulRuns > 0 || bothFailedRuns > 0);
    }

    @Test
    void testMultiLevelDeterminism() {
        long baseSeed = 98765L;
        int successfulRuns = 0;
        int bothFailedRuns = 0;

        for (int i = 0; i < SEED_ATTEMPTS_PER_TYPE; i++) {
            long currentSeed = baseSeed + (i * 1009L);

            MazeCreation config1 = buildMultiLevelConfig(currentSeed);
            MazeCreation config2 = buildMultiLevelConfig(currentSeed);

            Maze maze1 = MazeGenerator.createMaze(config1);
            Maze maze2 = MazeGenerator.createMaze(config2);

            assertEquals(maze1 == null, maze2 == null, "Neshoda v multi-level null stavu pro seed " + currentSeed);

            if (maze1 == null) {
                bothFailedRuns++;
                continue;
            }

            successfulRuns++;

            assertTrue(maze1.isHasMultipleFloors());
            assertEquals(maze1.isHasMultipleFloors(), maze2.isHasMultipleFloors());
            assertEquals(maze1.getMazeGridFloors().size(), maze2.getMazeGridFloors().size());

            for (int f = 0; f < maze1.getMazeGridFloors().size(); f++) {
                compareGrids(maze1.getMazeGridFloors().get(f).getMazeGrid(), maze2.getMazeGridFloors().get(f).getMazeGrid());
            }
        }

        assertTrue(successfulRuns > 0 || bothFailedRuns > 0);
    }

    @ParameterizedTest(name = "Strict Limits Parity: {0}")
    @MethodSource("provideSingleLevelTypes")
    void testCreationParityWithStrictLimits(MazeType mazeType) {
        long seed = 445566L;
        MazeCreation config1 = buildSingleLevelConfig(seed, mazeType);
        MazeCreation config2 = buildSingleLevelConfig(seed, mazeType);

        TestGenerationLimitsProvider testProvider = new TestGenerationLimitsProvider()
                .withDefaultLimit(1L);

        GenerationStatistics.setProvider(testProvider);
        try {
            StepCounter counter1 = new StepCounter();
            counter1.setLimit(1L);
            MazeBuildContext context1 = new MazeBuildContext(counter1);

            StepCounter counter2 = new StepCounter();
            counter2.setLimit(1L);
            MazeBuildContext context2 = new MazeBuildContext(counter2);

            Maze res1 = MazeGenerator.createMaze(config1, context1);
            Maze res2 = MazeGenerator.createMaze(config2, context2);

            assertEquals(res1 == null, res2 == null);

            if (res1 != null) {
                compareGrids(res1.getMazeGrid(), res2.getMazeGrid());
            }
        } finally {
            GenerationStatistics.resetProvider();
        }
    }

    @ParameterizedTest(name = "Thread Interruption Parity: {0}")
    @MethodSource("provideSingleLevelTypes")
    void testInterruptionParity(MazeType mazeType) {
        long seed = 778899L;
        MazeCreation config1 = buildSingleLevelConfig(seed, mazeType);
        MazeCreation config2 = buildSingleLevelConfig(seed, mazeType);

        boolean failed1 = false;
        boolean failed2 = false;

        Thread.currentThread().interrupt();
        try {
            Maze res1 = MazeGenerator.createMaze(config1);
            if (res1 == null) failed1 = true;
        } catch (Exception ignored) {
            failed1 = true;
        } finally {
            Thread.interrupted();
        }

        Thread.currentThread().interrupt();
        try {
            Maze res2 = MazeGenerator.createMaze(config2);
            if (res2 == null) failed2 = true;
        } catch (Exception ignored) {
            failed2 = true;
        } finally {
            Thread.interrupted();
        }

        assertEquals(failed1, failed2);
    }

    @Test
    void testDifferentSeedsProduceDifferentMazes() {
        boolean verified = false;

        for (int i = 0; i < 15; i++) {
            long s1Seed = 1111L + i;
            long s2Seed = 9999L + i;

            MazeCreation c1 = buildSingleLevelConfig(s1Seed, MazeType.CLASSIC);
            MazeCreation c2 = buildSingleLevelConfig(s2Seed, MazeType.CLASSIC);

            Maze m1 = MazeGenerator.createMaze(c1);
            Maze m2 = MazeGenerator.createMaze(c2);

            if (m1 == null || m2 == null) {
                continue;
            }

            List<MazeCell> cells1 = m1.getMazeGridFloors().getFirst().getMazeGrid().getFlattenedGrid();
            List<MazeCell> cells2 = m2.getMazeGridFloors().getFirst().getMazeGrid().getFlattenedGrid();

            boolean identical = true;
            for (int j = 0; j < cells1.size(); j++) {
                MazeCell cell1 = cells1.get(j);
                MazeCell cell2 = cells2.get(j);

                if (cell1 == null || cell2 == null) continue;

                if (cell1.getLinked().size() != cell2.getLinked().size()) {
                    identical = false;
                    break;
                }
            }

            if (!identical) {
                verified = true;
                break;
            }
        }

        assertTrue(verified, "Nepodařilo se vygenerovat odlišná bludiště pro různé seedy");
    }

    private void compareGrids(MazeGrid g1, MazeGrid g2) {
        assertNotNull(g1);
        assertNotNull(g2);

        assertEquals(g1.getRowCount(), g2.getRowCount(), "Rozdílný počet řádků");
        assertEquals(g1.getColCount(), g2.getColCount(), "Rozdílný počet sloupců");

        List<MazeCell> cells1 = g1.getFlattenedGrid();
        List<MazeCell> cells2 = g2.getFlattenedGrid();
        assertEquals(cells1.size(), cells2.size());

        for (int i = 0; i < cells1.size(); i++) {
            MazeCell c1 = cells1.get(i);
            MazeCell c2 = cells2.get(i);

            if (c1 == null || c2 == null) {
                assertEquals(c1, c2, "Neshoda null na indexu " + i);
                continue;
            }

            assertEquals(c1.getRow(), c2.getRow());
            assertEquals(c1.getCol(), c2.getCol());
            assertEquals(c1.getLinked().size(), c2.getLinked().size(),
                    String.format("Neshoda hran u buňky [%d,%d]", c1.getRow(), c1.getCol()));
        }

        if (g1.getStartCell() != null && g2.getStartCell() != null) {
            assertEquals(g1.getStartCell().getRow(), g2.getStartCell().getRow());
            assertEquals(g1.getStartCell().getCol(), g2.getStartCell().getCol());
        }

        if (g1.getEndCell() != null && g2.getEndCell() != null) {
            assertEquals(g1.getEndCell().getRow(), g2.getEndCell().getRow());
            assertEquals(g1.getEndCell().getCol(), g2.getEndCell().getCol());
        }
    }

    private MazeCreation buildSingleLevelConfig(long seed, MazeType mazeType) {
        int[][] mask = createFullMask(15, 15);
        int countParam = (mazeType == MazeType.EXACT_STEPS) ? 30 : 4;

        return new MazeCreation()
                .setDifficulty(Difficulty.MEDIUM)
                .setMazeAlgorithm(MazeAlgorithm.PRIM)
                .setMazeShape(MazeShape.RECTANGULAR)
                .setMazeType(mazeType)
                .setHeight(15)
                .setWidth(15)
                .setFloorCount(1)
                .setCounts(List.of(countParam))
                .setMazeTypes(List.of(mazeType))
                .setMask(mask)
                .setSeed(seed);
    }

    private MazeCreation buildMultiLevelConfig(long seed) {
        int[][] mask = createFullMask(15, 15);
        return new MazeCreation()
                .setDifficulty(Difficulty.HARD)
                .setMazeAlgorithm(MazeAlgorithm.RECURSIVE_BACKTRACKER)
                .setMazeShape(MazeShape.RECTANGULAR)
                .setMazeType(MazeType.MULTI_LEVEL)
                .setHeight(15)
                .setWidth(15)
                .setFloorCount(3)
                .setCounts(List.of(0, 4, 40))
                .setMazeTypes(List.of(MazeType.CLASSIC, MazeType.PORTAL, MazeType.EXACT_STEPS))
                .setMask(mask)
                .setSeed(seed);
    }

    private int[][] createFullMask(int rows, int cols) {
        int[][] mask = new int[rows][cols];
        for (int[] row : mask) {
            Arrays.fill(row, 1);
        }
        return mask;
    }
}
