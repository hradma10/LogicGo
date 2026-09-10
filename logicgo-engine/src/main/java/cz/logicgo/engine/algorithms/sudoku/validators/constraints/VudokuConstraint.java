package cz.logicgo.engine.algorithms.sudoku.validators.constraints;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.VudokuMark;


public class VudokuConstraint implements Constraint {

    private final SudokuCell[][] board;
    private final HashMap<SudokuCell, List<VudokuMark>> marksToCell;

    public VudokuConstraint(List<VudokuMark> vudokuMarks, SudokuCell[][] board) {
        this.board = board;
        this.marksToCell = new HashMap<>();

        for (VudokuMark mark : vudokuMarks) {
            int vertexR = mark.vertex().row();
            int vertexC = mark.vertex().col();
            int arm1R = mark.arm1().row();
            int arm1C = mark.arm1().col();
            int arm2R = mark.arm2().row();
            int arm2C = mark.arm2().col();

            SudokuCell vertexCell = board[vertexR][vertexC];
            SudokuCell arm1Cell = board[arm1R][arm1C];
            SudokuCell arm2Cell = board[arm2R][arm2C];

            marksToCell.computeIfAbsent(vertexCell, k -> new ArrayList<>()).add(mark);
            marksToCell.computeIfAbsent(arm1Cell, k -> new ArrayList<>()).add(mark);
            marksToCell.computeIfAbsent(arm2Cell, k -> new ArrayList<>()).add(mark);
        }
    }

    @Override
    public boolean isViolated(int row, int col, int num) {
        List<VudokuMark> marks = marksToCell.get(board[row][col]);
        if (marks == null) {
            return false;
        }

        for (VudokuMark mark : marks) {
            int vertexR = mark.vertex().row();
            int vertexC = mark.vertex().col();
            int arm1R = mark.arm1().row();
            int arm1C = mark.arm1().col();
            int arm2R = mark.arm2().row();
            int arm2C = mark.arm2().col();

            int vertexVal = (vertexR == row && vertexC == col) ? num : board[vertexR][vertexC].getValue();
            int arm1Val = (arm1R == row && arm1C == col) ? num : board[arm1R][arm1C].getValue();
            int arm2Val = (arm2R == row && arm2C == col) ? num : board[arm2R][arm2C].getValue();

            if (vertexVal != 0 && arm1Val != 0 && arm2Val != 0) {
                boolean isValid = (vertexVal == arm1Val + arm2Val) ||
                        (vertexVal == Math.abs(arm1Val - arm2Val));

                if (!isValid) {
                    return true;
                }
            }
        }
        return false;
    }

    public HashMap<SudokuCell, List<VudokuMark>> getMarksToCell() {
        return marksToCell;
    }

    public Constraint copy(Sudoku sudoku) {
        return new VudokuConstraint(sudoku.getModifiers().getVudoku().marks(), sudoku.getBoard());
    }
}
