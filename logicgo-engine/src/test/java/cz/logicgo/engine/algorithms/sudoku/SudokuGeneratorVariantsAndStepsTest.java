package cz.logicgo.engine.algorithms.sudoku;

import cz.logicgo.core.builders.sudoku.SudokuCreation;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.MultipleSolutionException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifiers;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.algorithms.sudoku.custom.CustomLayoutsLoader;
import cz.logicgo.engine.algorithms.sudoku.solvers.SudokuSequentialSolver;
import cz.logicgo.engine.context.SudokuSolveContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class SudokuGeneratorVariantsAndStepsTest {

    @ParameterizedTest
    @EnumSource(
            value = SudokuVariant.class,
            names = {"CLASSIC", "DIAGONAL", "EVEN_ODD", "CONSECUTIVE", "GREATER_THAN", "KROPKI", "XV", "SKYSCRAPER", "BETWEEN", "SANDWICH"}
    )
    void testStandardVariantsGenerationAndModifiers(SudokuVariant variant) throws ThreadTerminationException, LimitReachedException, MultipleSolutionException {
        Sudoku sudoku = generateSudoku(1024L, SudokuSize.NINE, variant, Difficulty.MEDIUM);

        assertNotNull(sudoku);
        assertNotNull(sudoku.getStartingBoard());
        assertNotNull(sudoku.getSolutionBoard());
        assertEquals(variant, sudoku.getVariant());

        validateBasicBoardProperties(sudoku.getSolutionBoard());

        SudokuModifiers modifiers = sudoku.getModifiers();
        assertNotNull(modifiers);

        switch (variant) {
            case DIAGONAL -> validateDiagonalUniqueness(sudoku.getSolutionBoard());
            case GREATER_THAN -> assertTrue(modifiers.hasGreaterThan());
            case CONSECUTIVE -> assertTrue(modifiers.hasConsecutive());
            case KROPKI -> assertNotNull(modifiers.getKropki());
            case SKYSCRAPER -> assertTrue(modifiers.hasSkyscraper());
            case XV -> assertTrue(modifiers.hasXv());
            case BETWEEN -> assertTrue(modifiers.hasBetween());
            case EVEN_ODD -> assertTrue(modifiers.hasEvenOdd());
            case SANDWICH -> assertTrue(modifiers.hasSandwich());
            default -> {}
        }

        SudokuGame game = new SudokuGame(sudoku, true);
        SudokuSolveContext solveContext = new SudokuSolveContext(2, new ConcurrentHashMap<>());
        SudokuSequentialSolver.solvingSequentialStart(game, solveContext);

        assertEquals(1, solveContext.getSolutionCount());
    }

    @ParameterizedTest
    @EnumSource(
            value = SudokuVariant.class,
            names = {"CLASSIC", "DIAGONAL", "EVEN_ODD", "CONSECUTIVE", "GREATER_THAN", "KROPKI", "XV", "SKYSCRAPER", "BETWEEN", "SANDWICH"}
    )
    void testMeasureStepsContractForAllVariants(SudokuVariant variant) {
        long seed = 12345L;
        Sudoku sudoku1 = createSudokuForMeasurement(seed, variant, Difficulty.EASY);
        Sudoku sudoku2 = createSudokuForMeasurement(seed, variant, Difficulty.EASY);

        long[] metrics1 = SudokuGenerator.generateAndMeasureSteps(sudoku1);
        long[] metrics2 = SudokuGenerator.generateAndMeasureSteps(sudoku2);

        assertNotNull(metrics1);
        assertNotNull(metrics2);
        assertEquals(3, metrics1.length);
        assertEquals(3, metrics2.length);

        long genSteps = metrics1[0];
        long removeSteps = metrics1[1];
        long solveSteps = metrics1[2];

        assertTrue(genSteps > 0);
        assertTrue(removeSteps >= 0);
        assertTrue(solveSteps >= 0);

        assertArrayEquals(metrics1, metrics2);
    }

    @ParameterizedTest
    @EnumSource(
            value = SudokuVariant.class,
            names = {"CLASSIC", "DIAGONAL", "EVEN_ODD", "CONSECUTIVE", "GREATER_THAN", "KROPKI", "XV", "SKYSCRAPER", "BETWEEN", "SANDWICH"}
    )
    void testClueDensityDecreasesWithHigherDifficultyForAllVariants(SudokuVariant variant) {
        long seed = 44444L;
        Sudoku easy = generateSudoku(seed, SudokuSize.NINE, variant, Difficulty.EASY);
        Sudoku hard = generateSudoku(seed, SudokuSize.NINE, variant, Difficulty.HARD);

        assertNotNull(easy);
        assertNotNull(hard);

        int easyGivenClues = countGivenClues(easy.getStartingBoard());
        int hardGivenClues = countGivenClues(hard.getStartingBoard());

        assertTrue(easyGivenClues > hardGivenClues);
    }

    private Sudoku createSudokuForMeasurement(long seed, SudokuVariant variant, Difficulty difficulty) {
        SudokuCreation creation = new SudokuCreation();
        creation.setSudokuSize(SudokuSize.NINE);
        creation.setSudokuVariant(variant);
        creation.setRegionLayout(CustomLayoutsLoader.getBasicLayout(SudokuSize.NINE));
        creation.setPatternLayout(CustomLayoutsLoader.getEmptyPatternLayout(SudokuSize.NINE));
        creation.setDifficulty(difficulty);
        creation.setSeed(seed);

        return new Sudoku(creation);
    }

    private Sudoku generateSudoku(long seed, SudokuSize size, SudokuVariant variant, Difficulty difficulty) {
        SudokuCreation creation = new SudokuCreation();
        creation.setSudokuSize(size);
        creation.setSudokuVariant(variant);
        creation.setRegionLayout(CustomLayoutsLoader.getBasicLayout(size));
        creation.setPatternLayout(CustomLayoutsLoader.getEmptyPatternLayout(size));
        creation.setDifficulty(difficulty);
        creation.setSeed(seed);

        return SudokuGenerator.createSudoku(creation);
    }

    private int countGivenClues(SudokuCell[][] board) {
        int count = 0;
        for (SudokuCell[] row : board) {
            for (SudokuCell cell : row) {
                if (cell != null && cell.getValue() != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private void validateBasicBoardProperties(SudokuCell[][] board) {
        int size = board.length;

        for (SudokuCell[] row : board) {
            Set<Integer> rowVals = new HashSet<>();
            for (int c = 0; c < size; c++) {
                int val = row[c].getValue();
                assertTrue(val >= 1 && val <= size);
                assertTrue(rowVals.add(val));
            }
        }

        for (int c = 0; c < size; c++) {
            Set<Integer> colVals = new HashSet<>();
            for (SudokuCell[] row : board) {
                assertTrue(colVals.add(row[c].getValue()));
            }
        }
    }

    private void validateDiagonalUniqueness(SudokuCell[][] board) {
        int size = board.length;
        Set<Integer> mainDiag = new HashSet<>();
        Set<Integer> antiDiag = new HashSet<>();

        for (int i = 0; i < size; i++) {
            assertTrue(mainDiag.add(board[i][i].getValue()));
            assertTrue(antiDiag.add(board[i][size - 1 - i].getValue()));
        }
    }
}
