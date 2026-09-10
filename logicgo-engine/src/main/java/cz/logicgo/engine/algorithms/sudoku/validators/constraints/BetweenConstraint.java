package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.BetweenLine;


public class BetweenConstraint implements Constraint {

    private final SudokuCell[][] board;
    private final HashMap<SudokuCell, List<BetweenLine>> marksToCell;

    public BetweenConstraint(List<BetweenLine> lines, SudokuCell[][] board) {
        this.board = board;
        this.marksToCell = new HashMap<>();

        for (BetweenLine line : lines) {
            marksToCell.computeIfAbsent(board[line.startCircle().row()][line.startCircle().col()], k -> new ArrayList<>()).add(line);
            marksToCell.computeIfAbsent(board[line.endCircle().row()][line.endCircle().col()], k -> new ArrayList<>()).add(line);
            for (GridCell cell : line.lineCells()) {
                marksToCell.computeIfAbsent(board[cell.row()][cell.col()], k -> new ArrayList<>()).add(line);
            }
        }
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        List<BetweenLine> lines = marksToCell.get(board[row][col]);
        if (lines == null) return false;

        for (BetweenLine line : lines) {
            boolean isStart = (line.startCircle().row() == row && line.startCircle().col() == col);
            boolean isEnd = (line.endCircle().row() == row && line.endCircle().col() == col);
            boolean isBetween = !isStart && !isEnd;

            int valStart = isStart ? num : board[line.startCircle().row()][line.startCircle().col()].getValue();
            int valEnd = isEnd ? num : board[line.endCircle().row()][line.endCircle().col()].getValue();

            if (isStart) {
                if (num == valEnd) return true;
                for (GridCell pathCell : line.lineCells()) {
                    if (num == board[pathCell.row()][pathCell.col()].getValue()) return true;
                }
            } else if (isEnd) {
                if (num == valStart) return true;
                for (GridCell pathCell : line.lineCells()) {
                    if (num == board[pathCell.row()][pathCell.col()].getValue()) return true;
                }
            } else {
                if (num == valStart || num == valEnd) return true;
                for (GridCell pathCell : line.lineCells()) {
                    if (pathCell.row() == row && pathCell.col() == col) continue;
                    if (num == board[pathCell.row()][pathCell.col()].getValue()) return true;
                }
            }

            if (isStart || isEnd) {
                for (GridCell pathCell : line.lineCells()) {
                    int pathVal = board[pathCell.row()][pathCell.col()].getValue();
                    if (pathVal == 0) continue;

                    if (valStart != 0 && valEnd != 0) {
                        int min = Math.min(valStart, valEnd);
                        int max = Math.max(valStart, valEnd);
                        if (pathVal <= min || pathVal >= max) return true;
                    }
                }
            }

            if (isBetween) {
                if (valStart != 0 && valEnd != 0) {
                    int min = Math.min(valStart, valEnd);
                    int max = Math.max(valStart, valEnd);
                    if (num <= min || num >= max) return true;
                }
            }
        }
        return false;
    }

    public Constraint copy(Sudoku sudoku) {
        return new BetweenConstraint(sudoku.getModifiers().getBetween().lines(), sudoku.getBoard());
    }
}
