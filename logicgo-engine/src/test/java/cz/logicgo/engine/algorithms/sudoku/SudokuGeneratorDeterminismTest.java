package cz.logicgo.engine.algorithms.sudoku;

import cz.logicgo.core.builders.sudoku.SudokuCreation;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.MultipleSolutionException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifiers;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics;
import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics.SudokuKey;
import cz.logicgo.engine.algorithms.sudoku.custom.CustomLayoutsLoader;
import cz.logicgo.engine.algorithms.sudoku.provider.TestGenerationLimitsProvider;
import cz.logicgo.engine.algorithms.sudoku.solvers.SudokuSequentialSolver;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig;
import cz.logicgo.engine.context.SudokuRemoveContext;
import cz.logicgo.engine.context.SudokuSolveContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.getBoardCopy;
import static org.junit.jupiter.api.Assertions.*;

class SudokuGeneratorDeterminismTest {

    @AfterEach
    void tearDown() {
        GenerationStatistics.resetProvider();
    }

    static Stream<Arguments> provideSudokuConfigs() {
        List<Arguments> arguments = new ArrayList<>();

        Set<SudokuVariant> excluded = EnumSet.of(SudokuVariant.PATTERNED, SudokuVariant.IRREGULAR);
        List<SudokuVariant> targetVariants = Arrays.stream(SudokuVariant.values())
                .filter(v -> !excluded.contains(v))
                .toList();

        long[] baseSeeds = {4829104L, 1928374L, 9918273L};

        for (SudokuVariant variant : targetVariants) {
            if (MultiGridConfig.isMultiDoku(variant)) {
                for (long seed : baseSeeds) {
                    arguments.add(Arguments.of(variant, SudokuSize.NINE, Difficulty.EASY, seed));
                }
                continue;
            }

            SudokuSize size = SudokuSize.NINE;

            for (Difficulty diff : List.of(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)) {
                for (long seed : baseSeeds) {
                    arguments.add(Arguments.of(variant, size, diff, seed));
                }
            }
        }

        return arguments.stream();
    }

    @ParameterizedTest(name = "Board Parity: {0}, {1}, {2}")
    @MethodSource("provideSudokuConfigs")
    void testSudokuCreationAndBoardDeterminism(SudokuVariant variant, SudokuSize size, Difficulty difficulty, long seed)
            throws ThreadTerminationException, MultipleSolutionException {

        SudokuCreation config1 = buildConfig(seed, variant, size, difficulty);
        SudokuCreation config2 = buildConfig(seed, variant, size, difficulty);

        Sudoku sudoku1 = SudokuGenerator.createSudoku(config1);
        Sudoku sudoku2 = SudokuGenerator.createSudoku(config2);

        assertEquals(sudoku1 == null, sudoku2 == null);

        if (sudoku1 == null) {
            return;
        }

        assertEquals(sudoku1.getType(), sudoku2.getType());
        assertEquals(sudoku1.getVariant(), sudoku2.getVariant());
        assertEquals(sudoku1.getDifficulty(), sudoku2.getDifficulty());

        compareBoards(sudoku1.getSolutionBoard(), sudoku2.getSolutionBoard());
        compareBoards(sudoku1.getStartingBoard(), sudoku2.getStartingBoard());

        validateBasicBoardProperties(sudoku1);
        validateModifiers(sudoku1, variant);

        if (!MultiGridConfig.isMultiDoku(variant)) {
            SudokuGame game1 = new SudokuGame(sudoku1, true);
            SudokuGame game2 = new SudokuGame(sudoku2, true);

            SudokuSolveContext solveContext1 = new SudokuSolveContext(2, new ConcurrentHashMap<>());
            SudokuSolveContext solveContext2 = new SudokuSolveContext(2, new ConcurrentHashMap<>());

            boolean limitHit1 = false;
            boolean limitHit2 = false;

            try {
                SudokuSequentialSolver.solvingSequentialStart(game1, solveContext1);
            } catch (LimitReachedException e) {
                limitHit1 = true;
            }

            try {
                SudokuSequentialSolver.solvingSequentialStart(game2, solveContext2);
            } catch (LimitReachedException e) {
                limitHit2 = true;
            }

            assertEquals(limitHit1, limitHit2, "Neshoda ve vyvolání LimitReachedException v řešiči pro stejný seed: " + seed);

            if (!limitHit1) {
                assertEquals(1, solveContext1.getSolutionCount());
                assertEquals(1, solveContext2.getSolutionCount());
                assertEquals(solveContext1.getSolutionCount(), solveContext2.getSolutionCount());
            }
        }
    }

    @ParameterizedTest(name = "Step Measurement Parity: {0}, {1}, {2}")
    @MethodSource("provideSudokuConfigs")
    void testStepMetricsDeterminism(SudokuVariant variant, SudokuSize size, Difficulty difficulty, long seed) {
        SudokuCreation config1 = buildConfig(seed, variant, size, difficulty);
        SudokuCreation config2 = buildConfig(seed, variant, size, difficulty);

        Sudoku s1 = new Sudoku(config1);
        Sudoku s2 = new Sudoku(config2);

        long[] metrics1 = SudokuGenerator.generateAndMeasureSteps(s1);
        long[] metrics2 = SudokuGenerator.generateAndMeasureSteps(s2);

        assertNotNull(metrics1);
        assertNotNull(metrics2);
        assertEquals(3, metrics1.length);
        assertArrayEquals(metrics1, metrics2);

        assertTrue(metrics1[0] > 0 || metrics1[0] == -1);
    }

    @ParameterizedTest(name = "Exhausted Gen Limit Parity: {0}, {1}")
    @MethodSource("provideSudokuConfigs")
    void testGenerationStepParityUnderExhaustedLimit(SudokuVariant variant, SudokuSize size, Difficulty difficulty, long seed) {
        SudokuCreation config1 = buildConfig(seed, variant, size, difficulty);
        SudokuCreation config2 = buildConfig(seed, variant, size, difficulty);

        Sudoku s1 = new Sudoku(config1);
        Sudoku s2 = new Sudoku(config2);

        StepCounter counter1 = new StepCounter();
        counter1.setLimit(1L);

        StepCounter counter2 = new StepCounter();
        counter2.setLimit(1L);

        boolean threw1 = false;
        boolean threw2 = false;

        try {
            if (MultiGridConfig.isMultiDoku(variant)) {
                SudokuGenerator.fillMultiSudokuBoard(s1, counter1);
            } else {
                SudokuGenerator.fillSudokuBoardRecursive(s1, counter1);
            }
        } catch (LimitReachedException | ThreadTerminationException e) {
            threw1 = true;
        }

        try {
            if (MultiGridConfig.isMultiDoku(variant)) {
                SudokuGenerator.fillMultiSudokuBoard(s2, counter2);
            } else {
                SudokuGenerator.fillSudokuBoardRecursive(s2, counter2);
            }
        } catch (LimitReachedException | ThreadTerminationException e) {
            threw2 = true;
        }

        assertEquals(threw1, threw2);
        assertEquals(counter1.get(), counter2.get());
    }

    @ParameterizedTest(name = "Strict Provider Limits Parity: {0}, {1}")
    @MethodSource("provideSudokuConfigs")
    void testCreationParityWithStrictLimits(SudokuVariant variant, SudokuSize size, Difficulty difficulty, long seed) {
        SudokuCreation config1 = buildConfig(seed, variant, size, difficulty);
        SudokuCreation config2 = buildConfig(seed, variant, size, difficulty);

        Sudoku s1 = new Sudoku(config1);
        SudokuKey key = GenerationStatistics.constructKey(s1);

        TestGenerationLimitsProvider testProvider = new TestGenerationLimitsProvider()
                .withSudokuGenLimit(key, 1L)
                .withSudokuDelLimit(key, 1L);

        GenerationStatistics.setProvider(testProvider);
        try {
            Sudoku res1 = SudokuGenerator.createSudoku(config1);
            Sudoku res2 = SudokuGenerator.createSudoku(config2);

            assertEquals(res1 == null, res2 == null);
            if (res1 != null) {
                compareBoards(res1.getStartingBoard(), res2.getStartingBoard());
            }
        } finally {
            GenerationStatistics.resetProvider();
        }
    }

    @ParameterizedTest(name = "Removal Limit Parity: {0}, {1}")
    @MethodSource("provideSudokuConfigs")
    void testRemovalParityUnderStrictLimit(SudokuVariant variant, SudokuSize size, Difficulty difficulty, long seed) {
        SudokuCreation config1 = buildConfig(seed, variant, size, difficulty);
        SudokuCreation config2 = buildConfig(seed, variant, size, difficulty);

        Sudoku s1 = new Sudoku(config1);
        Sudoku s2 = new Sudoku(config2);

        try {
            if (MultiGridConfig.isMultiDoku(variant)) {
                SudokuGenerator.fillMultiSudokuBoard(s1);
                SudokuGenerator.fillMultiSudokuBoard(s2);
            } else {
                SudokuGenerator.fillSudokuBoardRecursive(s1);
                SudokuGenerator.fillSudokuBoardRecursive(s2);
            }
        } catch (Exception e) {
            return;
        }

        s1.setSolutionBoard(getBoardCopy(s1.getBoard()));
        s2.setSolutionBoard(getBoardCopy(s2.getBoard()));

        SudokuGame game1 = new SudokuGame(s1, true);
        SudokuGame game2 = new SudokuGame(s2, true);

        StepCounter counter1 = new StepCounter();
        counter1.setLimit(1L);
        SudokuRemoveContext context1 = new SudokuRemoveContext(counter1, false);

        StepCounter counter2 = new StepCounter();
        counter2.setLimit(1L);
        SudokuRemoveContext context2 = new SudokuRemoveContext(counter2, false);

        boolean result1 = false;
        boolean result2 = false;

        try {
            result1 = SudokuGenerator.removeElements(
            game1, 20, new Stack<>(), 0,
                    game1.getSudoku().getRandomInstance(),
                    new ArrayList<>(), new ConcurrentHashMap<>(),
                    context1, new StepCounter()
            );
        } catch (Exception ignored) {
        }

        try {
            result2 = SudokuGenerator.removeElements(
            game2, 20, new Stack<>(), 0,
                    game2.getSudoku().getRandomInstance(),
                    new ArrayList<>(), new ConcurrentHashMap<>(),
                    context2, new StepCounter()
            );
        } catch (Exception ignored) {
        }

        assertEquals(result1, result2);
        assertEquals(counter1.get(), counter2.get());
    }

    @ParameterizedTest(name = "Thread Interruption Parity: {0}, {1}")
    @MethodSource("provideSudokuConfigs")
    void testInterruptionParity(SudokuVariant variant, SudokuSize size, Difficulty difficulty, long seed) {
        SudokuCreation config1 = buildConfig(seed, variant, size, difficulty);
        SudokuCreation config2 = buildConfig(seed, variant, size, difficulty);

        Sudoku s1 = new Sudoku(config1);
        Sudoku s2 = new Sudoku(config2);

        boolean interrupted1 = false;
        boolean interrupted2 = false;

        Thread.currentThread().interrupt();
        try {
            if (MultiGridConfig.isMultiDoku(variant)) {
                SudokuGenerator.fillMultiSudokuBoard(s1, new StepCounter());
            } else {
                SudokuGenerator.fillSudokuBoardRecursive(s1, new StepCounter());
            }
        } catch (ThreadTerminationException e) {
            interrupted1 = true;
        } catch (Exception ignored) {
        } finally {
            Thread.interrupted();
        }

        Thread.currentThread().interrupt();
        try {
            if (MultiGridConfig.isMultiDoku(variant)) {
                SudokuGenerator.fillMultiSudokuBoard(s2, new StepCounter());
            } else {
                SudokuGenerator.fillSudokuBoardRecursive(s2, new StepCounter());
            }
        } catch (ThreadTerminationException e) {
            interrupted2 = true;
        } catch (Exception ignored) {
        } finally {
            Thread.interrupted();
        }

        assertEquals(interrupted1, interrupted2);
    }

    private void compareBoards(SudokuCell[][] b1, SudokuCell[][] b2) {
        assertNotNull(b1);
        assertNotNull(b2);
        assertEquals(b1.length, b2.length);
        assertEquals(b1[0].length, b2[0].length);

        for (int r = 0; r < b1.length; r++) {
            for (int c = 0; c < b1[r].length; c++) {
                SudokuCell c1 = b1[r][c];
                SudokuCell c2 = b2[r][c];

                if (c1 == null || c2 == null) {
                    assertEquals(c1, c2);
                    continue;
                }
                assertEquals(c1.getValue(), c2.getValue());
            }
        }
    }

    private void validateBasicBoardProperties(Sudoku sudoku) {
        SudokuCell[][] board = sudoku.getSolutionBoard();
        assertNotNull(board);

        if (MultiGridConfig.isMultiDoku(sudoku.getVariant())) {
            var setup = MultiGridConfig.LAYOUTS.get(sudoku.getVariant());
            assertNotNull(setup);

            for (int[] offset : setup.offsets()) {
                int rowOff = offset[0];
                int colOff = offset[1];
                validateSubGrid(board, rowOff, colOff, 9);
            }
        } else {
            int size = sudoku.getType().getGridSize();
            for (int r = 0; r < size; r++) {
                Set<Integer> rowVals = new HashSet<>();
                for (int c = 0; c < size; c++) {
                    if (board[r][c] != null) {
                        int val = board[r][c].getValue();
                        assertTrue(val >= 1 && val <= size, "Hodnota mimo rozsah na [" + r + "," + c + "]: " + val);
                        assertTrue(rowVals.add(val), "Duplicita v řádku " + r + ": " + val);
                    }
                }
                assertEquals(size, rowVals.size(), "Neúplný řádek " + r);
            }

            for (int c = 0; c < size; c++) {
                Set<Integer> colVals = new HashSet<>();
                for (int r = 0; r < size; r++) {
                    if (board[r][c] != null) {
                        int val = board[r][c].getValue();
                        assertTrue(colVals.add(val), "Duplicita ve sloupci " + c + ": " + val);
                    }
                }
                assertEquals(size, colVals.size(), "Neúplný sloupec " + c);
            }
        }
    }

    private void validateSubGrid(SudokuCell[][] board, int rowOff, int colOff, int subSize) {
        for (int r = 0; r < subSize; r++) {
            Set<Integer> rowVals = new HashSet<>();
            for (int c = 0; c < subSize; c++) {
                SudokuCell cell = board[rowOff + r][colOff + c];
                assertNotNull(cell, "Chybějící buňka v sub-mřížce na [" + (rowOff + r) + "," + (colOff + c) + "]");
                int val = cell.getValue();
                assertTrue(val >= 1 && val <= subSize, "Hodnota mimo rozsah v sub-mřížce: " + val);
                assertTrue(rowVals.add(val), "Duplicita v řádku sub-mřížky na [" + (rowOff + r) + "," + (colOff + c) + "]: " + val);
            }
            assertEquals(subSize, rowVals.size());
        }

        for (int c = 0; c < subSize; c++) {
            Set<Integer> colVals = new HashSet<>();
            for (int r = 0; r < subSize; r++) {
                SudokuCell cell = board[rowOff + r][colOff + c];
                int val = cell.getValue();
                assertTrue(colVals.add(val), "Duplicita ve sloupci sub-mřížky na [" + (rowOff + r) + "," + (colOff + c) + "]: " + val);
            }
            assertEquals(subSize, colVals.size());
        }

        for (int boxR = 0; boxR < 3; boxR++) {
            for (int boxC = 0; boxC < 3; boxC++) {
                Set<Integer> boxVals = new HashSet<>();
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        int val = board[rowOff + boxR * 3 + r][colOff + boxC * 3 + c].getValue();
                        assertTrue(boxVals.add(val), "Duplicita v 3x3 bloku sub-mřížky: " + val);
                    }
                }
                assertEquals(9, boxVals.size());
            }
        }
    }

    private void validateModifiers(Sudoku sudoku, SudokuVariant variant) {
        SudokuModifiers modifiers = sudoku.getModifiers();
        assertNotNull(modifiers);

        switch (variant) {
            case DIAGONAL -> {
                int size = sudoku.getType().getGridSize();
                Set<Integer> mainDiag = new HashSet<>();
                Set<Integer> antiDiag = new HashSet<>();
                for (int i = 0; i < size; i++) {
                    assertTrue(mainDiag.add(sudoku.getSolutionBoard()[i][i].getValue()));
                    assertTrue(antiDiag.add(sudoku.getSolutionBoard()[i][size - 1 - i].getValue()));
                }
            }
            case GREATER_THAN -> assertTrue(modifiers.hasGreaterThan());
            case CONSECUTIVE -> assertTrue(modifiers.hasConsecutive());
            case KROPKI -> assertNotNull(modifiers.getKropki());
            case SKYSCRAPER -> assertTrue(modifiers.hasSkyscraper());
            case XV -> assertTrue(modifiers.hasXv());
            case BETWEEN -> assertTrue(modifiers.hasBetween());
            case EVEN_ODD -> assertTrue(modifiers.hasEvenOdd());
            case SANDWICH -> assertTrue(modifiers.hasSandwich());
            case KILLER -> assertTrue(modifiers.hasKiller());
            default -> {}
        }
    }

    private SudokuCreation buildConfig(long seed, SudokuVariant variant, SudokuSize size, Difficulty difficulty) {
        SudokuCreation creation = new SudokuCreation();
        creation.setSudokuSize(size);
        creation.setSudokuVariant(variant);
        creation.setDifficulty(difficulty);
        creation.setSeed(seed);
        creation.setForExport(false);
        creation.setPatternLayout(CustomLayoutsLoader.getEmptyPatternLayout(size));
        creation.setRegionLayout(CustomLayoutsLoader.getBasicLayout(size));
        return creation;
    }
}
