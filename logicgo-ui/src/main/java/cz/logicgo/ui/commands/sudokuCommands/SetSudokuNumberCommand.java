package cz.logicgo.ui.commands.sudokuCommands;


import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.ui.commands.CommandType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class SetSudokuNumberCommand extends SudokuCommand {

    final public static CommandType type = CommandType.SET_SUDOKU;
    final private int row;
    final private int col;
    final private int oldNumber;
    final private int newNumber;

    public SetSudokuNumberCommand(SudokuGame sudokuGame, int row, int column, int newNumber, int oldNumber) {
        super(sudokuGame);
        this.row = row;
        this.col = column;
        this.oldNumber = oldNumber;
        this.newNumber = newNumber;
    }

    public SetSudokuNumberCommand(LocalDateTime timestamp, SudokuGame sudokuGame, int row, int col, int oldNumber, int newNumber) {
        super(timestamp, sudokuGame);
        this.row = row;
        this.col = col;
        this.oldNumber = oldNumber;
        this.newNumber = newNumber;
    }

    @Override
    public void execute() {
        SudokuGame sudokuGame = this.getSudokuGame();
        sudokuGame.setNumberPlay(this.getRow(), this.getCol(), newNumber);
    }

    @Override
    public void undo() {
        SudokuGame sudokuGame = this.getSudokuGame();
        sudokuGame.setNumberPlay(this.getRow(), this.getCol(), oldNumber);
    }

    public int getOldNumber() {
        return oldNumber;
    }

    public int getNewNumber() {
        return newNumber;
    }

    public byte[] getCommandsAsBytes() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(type.getType());

            long epochSecond = getTimestamp().toEpochSecond(ZoneOffset.UTC);
            int nano = getTimestamp().getNano();

            dos.writeLong(epochSecond);
            dos.writeInt(nano);

            byte row = (byte) this.getRow();
            byte column = (byte) this.getCol();
            byte oldNumber = (byte) this.getOldNumber();
            byte newNumber = (byte) this.getNewNumber();

            byte[] bytes;
            switch (this.getSudokuGame().getSudoku().getType()) {
                case SIXTEEN -> {
                    bytes = new byte[5];
                    bytes[0] = row;
                    bytes[1] = column;
                    bytes[2] = oldNumber;
                    bytes[3] = newNumber;
                }
                default -> {
                    bytes = new byte[2];

                    var value = (byte) (row << 4);
                    value |= (byte) (column & 0xF);
                    bytes[0] = value;

                    value = (byte) (oldNumber << 4);
                    value |= (byte) (newNumber & 0xF);
                    bytes[1] = value;
                }
            }

            int length = bytes.length;
            dos.writeInt(length);
            dos.write(bytes);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public byte getType() {
        return type.getType();
    }
}
