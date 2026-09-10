package cz.logicgo.ui.commands.sudokuCommands;


import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.ui.commands.CommandType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

import static cz.logicgo.core.util.boardConverters.SudokuConverters.serializeBoard;
import static cz.logicgo.core.util.boardConverters.SudokuConverters.sudokuCandidatesToBytes;


public class RestartSudokuCommand extends SudokuCommand {

    final public static CommandType type = CommandType.REPLACE_SUDOKU;

    final private SudokuCell[][] oldCells;

    final private SudokuCell[][] newCells;

    final private Map<GridCell, Set<Integer>> oldCandidates;
    final private Map<GridCell, Set<Integer>> newCandidates;


    public RestartSudokuCommand(SudokuGame sudokuGame, SudokuCell[][] oldCells, SudokuCell[][] newCells, Map<GridCell, Set<Integer>> oldCandidates, Map<GridCell, Set<Integer>> newCandidates) {
        super(sudokuGame);
        this.oldCells = oldCells;
        this.newCells = newCells;
        this.oldCandidates = oldCandidates;
        this.newCandidates = newCandidates;
    }

    public RestartSudokuCommand(LocalDateTime timestamp, SudokuGame sudokuGame, SudokuCell[][] oldCells, SudokuCell[][] newCells, Map<GridCell, Set<Integer>> oldCandidates, Map<GridCell, Set<Integer>> newCandidates) {
        super(timestamp, sudokuGame);
        this.oldCells = oldCells;
        this.newCells = newCells;
        this.oldCandidates = oldCandidates;
        this.newCandidates = newCandidates;
    }

    @Override
    public void execute() {
        SudokuCell.copyBoardProperties(getSudokuGame().getSudoku().getBoard(), newCells);
        getSudokuGame().getSudoku().setBoard(newCells);

    }

    @Override
    public void undo() {
        SudokuCell.copyBoardProperties(getSudokuGame().getSudoku().getBoard(), oldCells);
        getSudokuGame().getSudoku().setBoard(oldCells);
    }

    @Override
    public byte[] getCommandsAsBytes() {
        byte[] oldCellsByte = serializeBoard(oldCells);
        byte[] newCellsByte = serializeBoard(newCells);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(this.getType());

            long epochSecond = getTimestamp().toEpochSecond(ZoneOffset.UTC);
            int nano = getTimestamp().getNano();

            dos.writeLong(epochSecond);
            dos.writeInt(nano);

            dos.writeShort(oldCellsByte.length);
            dos.write(oldCellsByte);

            dos.writeShort(newCellsByte.length);
            dos.write(newCellsByte);

            var oldCandidateBytes = sudokuCandidatesToBytes(oldCandidates);
            dos.writeInt(oldCandidateBytes.length);
            dos.write(oldCandidateBytes);

            var newCandidateBytes = sudokuCandidatesToBytes(newCandidates);
            dos.writeInt(newCandidateBytes.length);
            dos.write(newCandidateBytes);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SudokuCell[][] getOldCells() {
        return oldCells;
    }

    public SudokuCell[][] getNewCells() {
        return newCells;
    }

    @Override
    public byte getType() {
        return type.getType();
    }

}
