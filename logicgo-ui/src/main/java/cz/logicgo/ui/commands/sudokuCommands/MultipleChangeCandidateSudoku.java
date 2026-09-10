package cz.logicgo.ui.commands.sudokuCommands;


import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.ui.commands.CommandType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class MultipleChangeCandidateSudoku extends SudokuCommand {

    final private int row;
    final private int col;
    final List<Integer> oldCandidates;
    final List<Integer> newCandidates;

    public MultipleChangeCandidateSudoku(SudokuGame sudokuGame, int row, int col, List<Integer> oldCandidates, List<Integer> newCandidates) {
        this.row = row;
        this.col = col;
        this.oldCandidates = Collections.unmodifiableList(oldCandidates);
        this.newCandidates = Collections.unmodifiableList(newCandidates);
        super(sudokuGame);
    }

    public MultipleChangeCandidateSudoku(LocalDateTime timestamp, SudokuGame sudokuGame, int row, int col, List<Integer> oldCandidates, List<Integer> newCandidates) {
        this.row = row;
        this.col = col;
        this.oldCandidates = Collections.unmodifiableList(oldCandidates);
        this.newCandidates = Collections.unmodifiableList(newCandidates);
        super(timestamp, sudokuGame);
    }

    @Override
    public byte getType() {
        return CommandType.MULTIPLE_CANDIDATE_CHANGE_SUDOKU.getType();
    }

    @Override
    public void execute() {
        SudokuCell cell = getSudokuGame().getSudoku().getSudokuCell(row, col);
        getSudokuGame().getSudoku().getCandidates().put(cell, new HashSet<>(newCandidates));
    }

    @Override
    public void undo() {
        SudokuCell cell = getSudokuGame().getSudoku().getSudokuCell(row, col);
        getSudokuGame().getSudoku().getCandidates().put(cell, new HashSet<>(oldCandidates));
    }

    @Override
    public byte[] getCommandsAsBytes() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(getType());

            long epochSecond = getTimestamp().toEpochSecond(ZoneOffset.UTC);
            int nano = getTimestamp().getNano();

            dos.writeLong(epochSecond);
            dos.writeInt(nano);

            SudokuSize sudokuSize = getSudokuGame().getSudoku().getType();
            dos.writeByte(sudokuSize.getGridSize());

            dos.writeByte(row);
            dos.writeByte(col);

            int oldCandidatesCount = oldCandidates.size();
            int newCandidatesCount = newCandidates.size();
            dos.writeByte(oldCandidatesCount);
            dos.writeByte(newCandidatesCount);


            switch (sudokuSize) {
                case SIXTEEN -> {
                    for (int candidate : oldCandidates) {
                        dos.writeByte(candidate);
                    }

                    for (int candidate : newCandidates) {
                        dos.writeByte(candidate);
                    }
                }
                default -> {
                    halfByteSerialization(dos, oldCandidates);
                    halfByteSerialization(dos, newCandidates);
                }
            }
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private void halfByteSerialization(DataOutputStream dos, List<Integer> candidates) throws IOException {
        boolean halfByte = false;
        byte currentByte = 0;

        for (int candidate : candidates) {

            if (halfByte) {
                currentByte |= (byte) (candidate & 0xF);
                dos.writeByte(currentByte);
            } else {
                currentByte = (byte) (candidate << 4);
            }
            halfByte = !halfByte;
        }

        if (halfByte) {
            dos.writeByte(currentByte);
        }
    }
}
