package cz.logicgo.ui.commands.sudokuCommands;


import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.ui.commands.CommandType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ToggleCandidateSudoku extends SudokuCommand {

    private final int row;
    private final int col;
    private final int candidate;

    public ToggleCandidateSudoku(SudokuGame sudokuGame, int row, int col, int candidate) {
        super(sudokuGame);
        this.row = row;
        this.col = col;
        this.candidate = candidate;
    }

    public ToggleCandidateSudoku(LocalDateTime timestamp, SudokuGame sudokuGame, int row, int col, int candidate) {
        super(timestamp, sudokuGame);
        this.row = row;
        this.col = col;
        this.candidate = candidate;
    }

    @Override
    public byte getType() {
        return CommandType.TOGGLE_CANDIDATE_SUDOKU.getType();
    }

    @Override
    public void execute() {
        toggle();
    }

    @Override
    public void undo() {
        toggle();
    }

    private void toggle() {
        SudokuCell cell = getSudokuGame().getSudoku().getSudokuCell(row, col);

        if (getSudokuGame().getSudoku().getCandidates() == null) {
            getSudokuGame().getSudoku().setCandidates(new HashMap<>());
        }

        Set<Integer> candidates = getSudokuGame().getSudoku().getCandidates()
                .computeIfAbsent(cell, k -> new HashSet<>());

        if (candidates.contains(candidate)) {
            candidates.remove(candidate);
        } else {
            candidates.add(candidate);
        }
    }

    @Override
    public byte[] getCommandsAsBytes() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(getType());

            long epochSecond = getTimestamp().toEpochSecond(ZoneOffset.UTC);
            dos.writeLong(epochSecond);
            dos.writeInt(getTimestamp().getNano());

            SudokuSize sudokuSize = getSudokuGame().getSudoku().getType();
            dos.writeByte(sudokuSize.getGridSize());

            dos.writeByte(row);
            dos.writeByte(col);

            dos.writeByte(candidate);

            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
