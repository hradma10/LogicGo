package cz.logicgo.engine.algorithms.sudoku;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.algorithms.sudoku.multi.SubGrid;
import cz.logicgo.engine.algorithms.sudoku.solvers.ZobristTable;
import cz.logicgo.engine.algorithms.sudoku.validators.ISudokuValidator;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiSudokuValidator;
import cz.logicgo.engine.algorithms.sudoku.validators.SudokuValidator;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.ArrayList;
import java.util.List;

import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.flattenBoard;
import static cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig.LAYOUTS;
import static cz.logicgo.engine.algorithms.sudoku.validators.ValidatorFactory.createAllConstraints;


public class SudokuGame {
    final private ISudokuValidator validator;
    final private Sudoku sudoku;

    private final ZobristTable zobristTable;
    private long currentHash = 0L;

    boolean useValidator = true;

    public SudokuGame(Sudoku sudoku, boolean createValidator) {
        this(sudoku, createValidator, true);
    }

    public SudokuGame(Sudoku sudoku, boolean createValidator, boolean afterFillingCells) {
        useValidator = createValidator;

        this.validator = createValidator(sudoku, afterFillingCells);
        this.sudoku = sudoku;

        boolean isMultidoku = MultiGridConfig.isMultiDoku(sudoku.getVariant());

        this.zobristTable = new ZobristTable(this.validator.getGridSize(), isMultidoku);
        computeInitialHash();
    }

    private static ISudokuValidator createValidator(Sudoku sudoku, boolean afterFillingCells) {
        if (MultiGridConfig.isMultiDoku(sudoku.getVariant())) {
            return createMultiValidator(sudoku, afterFillingCells);
        }

        ArrayList<Constraint> constraints = createAllConstraints(sudoku, afterFillingCells);

        switch (sudoku.getVariant()) {
            case OFFSET, PATTERNED -> {
                SudokuPatternLayout pattern = sudoku.getPattern();
                for (SudokuCell cell : flattenBoard(sudoku.getBoard())) {
                    cell.setPattern(pattern);
                }
            }
            case null, default -> {
            }
        }

        ISudokuValidator sudokuValidator = new SudokuValidator(sudoku, constraints);
        sudokuValidator.fillUnitsInConstraints();
        return sudokuValidator;
    }

    private static ISudokuValidator createMultiValidator(Sudoku sudoku, boolean afterFillingCells) {
        List<SubGrid> subGrids = new ArrayList<>();
        var setup = LAYOUTS.get(sudoku.getVariant());

        for (int[] offset : setup.offsets()) {
            ArrayList<Constraint> localConstraints = createAllConstraints(sudoku, afterFillingCells);
            SudokuValidator localValidator = new SudokuValidator(sudoku, localConstraints);
            subGrids.add(new SubGrid(offset[0], offset[1], localValidator));
        }

        MultiSudokuValidator multiValidator = new MultiSudokuValidator(sudoku, subGrids, setup.globalSize());
        multiValidator.fillUnitsInConstraints();

        return multiValidator;
    }

    public int getBoxIdForCell(int row, int col) {
        if (sudoku.getVariant() != null && sudoku.getVariant() == SudokuVariant.IRREGULAR) {
            return sudoku.getRegionLayout().getRegions()[row][col];
        }

        int gridSize = validator.getGridSize();

        int boxHeight = (int) Math.sqrt(gridSize);
        int boxWidth = (gridSize % boxHeight == 0) ? (gridSize / boxHeight) : boxHeight;

        int boxRow = row / boxHeight;
        int boxCol = col / boxWidth;
        int boxesPerRow = (int) Math.ceil((double) gridSize / boxWidth);

        return boxRow * boxesPerRow + boxCol;
    }

    public Iterable<TargetedCell> getCellsInBox(int boxId) {
        List<TargetedCell> cells = new ArrayList<>();
        int gridSize = validator.getGridSize();

        if (sudoku.getVariant() != null && sudoku.getVariant().name().equals("PATTERNED")) {
            for (int r = 0; r < gridSize; r++) {
                for (int c = 0; c < gridSize; c++) {
                    if (getBoxIdForCell(r, c) == boxId) {
                        cells.add(new TargetedCell(r, c));
                    }
                }
            }
            return cells;
        }

        int boxHeight = (int) Math.sqrt(gridSize);
        int boxWidth = (gridSize % boxHeight == 0) ? (gridSize / boxHeight) : boxHeight;
        int boxesPerRow = (int) Math.ceil((double) gridSize / boxWidth);

        int boxRow = boxId / boxesPerRow;
        int boxCol = boxId % boxesPerRow;

        int maxR = Math.min((boxRow + 1) * boxHeight, gridSize);
        int maxC = Math.min((boxCol + 1) * boxWidth, gridSize);

        for (int r = boxRow * boxHeight; r < maxR; r++) {
            for (int c = boxCol * boxWidth; c < maxC; c++) {
                cells.add(new TargetedCell(r, c));
            }
        }

        return cells;
    }

    public SudokuGame(ISudokuValidator sudokuValidator, Sudoku sudoku) {
        this.validator = sudokuValidator;
        this.sudoku = sudoku;

        boolean isMultidoku = MultiGridConfig.isMultiDoku(sudoku.getVariant());

        this.zobristTable = new ZobristTable(sudokuValidator.getGridSize(), isMultidoku);
        computeInitialHash();
    }

    private Sudoku getInstance(SudokuGame sudokuGame) {
        Sudoku s = sudokuGame.getSudoku();
        return switch (s.getVariant()) {
            default -> new Sudoku(s);
        };
    }

    public SudokuGame(SudokuGame sudokuGame) {
        Sudoku sudokuCopy = getInstance(sudokuGame);
        this.sudoku = sudokuCopy;
        this.validator = sudokuGame.getSudokuValidator().copy(sudokuCopy);

        this.zobristTable = sudokuGame.zobristTable;
        this.currentHash = sudokuGame.currentHash;
    }

    public SudokuGame(SudokuGame sudokuGame, boolean createValidator) {
        this.sudoku = getInstance(sudokuGame);
        useValidator = createValidator;
        if (createValidator) {
            this.validator = createValidator(sudoku, true);
        } else {
            this.validator = null;
        }

        this.zobristTable = sudokuGame.zobristTable;
        this.currentHash = sudokuGame.currentHash;
    }

    public SudokuGame(SudokuGame sudokuGame, Constraint... constraints) {
        Sudoku sudokuCopy = getInstance(sudokuGame);
        this.sudoku = sudokuCopy;
        this.validator = sudokuGame.getSudokuValidator().copy(sudokuCopy);
        validator.addConstraints(constraints);

        this.zobristTable = sudokuGame.zobristTable;
        this.currentHash = sudokuGame.currentHash;
    }

    private void computeInitialHash() {
        currentHash = 0L;
        SudokuCell[][] board = sudoku.getBoard();
        if (board == null) return;

        int size = sudoku.getType().getGridSize();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] != null) {
                    int val = board[r][c].getValue();
                    if (val != 0) {
                        currentHash ^= zobristTable.getHashValue(r, c, val);
                    }
                }
            }
        }
    }

    public long getCurrentHash() {
        return currentHash;
    }

    public ISudokuValidator getSudokuValidator() {
        return validator;
    }

    public Sudoku getSudoku() {
        return sudoku;
    }

    public void setNumber(int rowIndex, int columnIndex, int number) {
        int oldNum = sudoku.setNumber(rowIndex, columnIndex, number);

        if (oldNum != 0) currentHash ^= zobristTable.getHashValue(rowIndex, columnIndex, oldNum);
        if (number != 0) currentHash ^= zobristTable.getHashValue(rowIndex, columnIndex, number);

        validator.unset(rowIndex, columnIndex, oldNum);
        validator.update(rowIndex, columnIndex, number);
    }

    public int removeNumber(int rowIndex, int columnIndex) {
        int removedNum = sudoku.setNumberToZero(rowIndex, columnIndex);

        if (removedNum != 0) currentHash ^= zobristTable.getHashValue(rowIndex, columnIndex, removedNum);

        validator.unset(rowIndex, columnIndex, removedNum);
        return removedNum;
    }

    public void setNumberPlay(int rowIndex, int columnIndex, int number) {
        if (sudoku.getBoard()[rowIndex][columnIndex] == null) return;
        int oldNum = sudoku.setNumber(rowIndex, columnIndex, number);

        if (oldNum != 0) currentHash ^= zobristTable.getHashValue(rowIndex, columnIndex, oldNum);
        if (number != 0) currentHash ^= zobristTable.getHashValue(rowIndex, columnIndex, number);
    }

    public int removeNumberPlay(int rowIndex, int columnIndex) {
        int removedNum = sudoku.setNumberToZero(rowIndex, columnIndex);

        if (removedNum != 0) currentHash ^= zobristTable.getHashValue(rowIndex, columnIndex, removedNum);

        return removedNum;
    }

    public void removeCandidate(int row, int col, int num) {
        if (validator instanceof SudokuValidator sv) {
            sv.removeCandidate(row, col, num);
        } else if (validator instanceof MultiSudokuValidator multiValidator) {
            for (SubGrid sub : multiValidator.getSubGrids()) {
                if (sub.containsGlobal(row, col)) {
                    if (sub.validator() instanceof SudokuValidator subSv) {
                        subSv.removeCandidate(sub.getLocalRow(row), sub.getLocalCol(col), num);
                    }
                }
            }
        }
    }

    public SudokuGame makeCopy() {
        return new SudokuGame(this);
    }

    public long computeBoardHash() {
        return currentHash;
    }
}
