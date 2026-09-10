package cz.logicgo.engine.algorithms.sudoku;

import cz.logicgo.core.builders.sudoku.SudokuCreation;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.exceptions.parallel.MultipleSolutionException;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.algorithms.sudoku.custom.CustomLayoutsLoader;
import cz.logicgo.engine.algorithms.sudoku.solvers.SudokuSequentialSolver;
import cz.logicgo.engine.context.SudokuSolveContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class SudokuGeneratorTest {

    @ParameterizedTest
    @ValueSource(longs = {42L, 1337L, 987654321L})
    void testDeterminismWithSameSeed(long seed) {
        Sudoku first = generateStandardSudoku(seed, Difficulty.MEDIUM);
        Sudoku second = generateStandardSudoku(seed, Difficulty.MEDIUM);

        assertNotNull(first);
        assertNotNull(second);
        assertArrayEquals(first.getStartingBoard(), second.getStartingBoard());
        assertArrayEquals(first.getSolutionBoard(), second.getSolutionBoard());
    }

    @Test
    void testDifferentSeedsProduceDifferentBoards() {
        Sudoku s1 = generateStandardSudoku(101L, Difficulty.EASY);
        Sudoku s2 = generateStandardSudoku(202L, Difficulty.EASY);

        assertNotNull(s1);
        assertNotNull(s2);

        boolean identical = true;
        SudokuCell[][] b1 = s1.getStartingBoard();
        SudokuCell[][] b2 = s2.getStartingBoard();

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (b1[r][c].getValue() != b2[r][c].getValue()) {
                    identical = false;
                    break;
                }
            }
        }

        assertFalse(identical);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 555L, 8910L})
    void testGeneratedSudokuValidityAndUniqueSolution(long seed) throws ThreadTerminationException, LimitReachedException, MultipleSolutionException {
        Sudoku sudoku = generateStandardSudoku(seed, Difficulty.HARD);

        assertNotNull(sudoku);
        assertNotNull(sudoku.getStartingBoard());
        assertNotNull(sudoku.getSolutionBoard());

        validateBoardStructure(sudoku.getSolutionBoard());

        SudokuGame game = new SudokuGame(sudoku, true);
        SudokuSolveContext solveContext = new SudokuSolveContext(2, new ConcurrentHashMap<>());
        SudokuSequentialSolver.solvingSequentialStart(game, solveContext);

        assertEquals(1, solveContext.getSolutionCount());
    }

    private Sudoku generateStandardSudoku(long seed, Difficulty difficulty) {
        SudokuCreation creation = new SudokuCreation();
        creation.setSudokuSize(SudokuSize.NINE);
        creation.setSudokuVariant(SudokuVariant.CLASSIC);
        creation.setRegionLayout(CustomLayoutsLoader.getBasicLayout(SudokuSize.NINE));
        creation.setPatternLayout(CustomLayoutsLoader.getEmptyPatternLayout(SudokuSize.NINE));
        creation.setDifficulty(difficulty);
        creation.setSeed(seed);

        return SudokuGenerator.createSudoku(creation);
    }

    private void validateBoardStructure(SudokuCell[][] board) {
        int size = board.length;

        for (SudokuCell[] sudokuCells : board) {
            Set<Integer> rowVals = new HashSet<>();
            for (int c = 0; c < size; c++) {
                int val = sudokuCells[c].getValue();
                assertTrue(val >= 1 && val <= size);
                assertTrue(rowVals.add(val));
            }
        }

        for (int c = 0; c < size; c++) {
            Set<Integer> colVals = new HashSet<>();
            for (SudokuCell[] sudokuCells : board) {
                assertTrue(colVals.add(sudokuCells[c].getValue()));
            }
        }

        int boxSize = (int) Math.sqrt(size);
        for (int br = 0; br < size; br += boxSize) {
            for (int bc = 0; bc < size; bc += boxSize) {
                Set<Integer> boxVals = new HashSet<>();
                for (int r = 0; r < boxSize; r++) {
                    for (int c = 0; c < boxSize; c++) {
                        assertTrue(boxVals.add(board[br + r][bc + c].getValue()));
                    }
                }
            }
        }
    }
}
