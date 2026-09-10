package cz.logicgo.ui.commands.mazeCommands;


import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.ui.commands.CommandType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MarkAreaCommand extends MazeCommand {

    private final List<GridCell> cellCoordinates;
    private final boolean mark;
    private final int floor;

    public MarkAreaCommand(Maze maze, List<MazeCell> affectedCells, int floor, boolean mark) {
        super(maze);
        this.floor = floor;
        this.mark = mark;
        this.cellCoordinates = new ArrayList<>();
        for (MazeCell cell : affectedCells) {
            this.cellCoordinates.add(new GridCell(cell.getRow(), cell.getCol()));
        }
    }

    public MarkAreaCommand(LocalDateTime timestamp, Maze maze, List<GridCell> affectedCells, int floor, boolean mark) {
        super(timestamp, maze);
        this.floor = floor;
        this.mark = mark;
        this.cellCoordinates = affectedCells;
    }

    @Override
    public byte getType() {
        return CommandType.MARK_MAZE.getType();
    }

    @Override
    public void execute() {
        Set<MazeCell> triedWays = getGrid().getTriedWays();
        for (GridCell coords : cellCoordinates) {
            MazeCell cell = getGrid().getCell(coords.row(), coords.col());

            if (mark) {
                cell.addMark();
                if (cell.isXMarked()) {
                    triedWays.add(cell);
                }
            } else {
                cell.removeMark();
                if (!cell.isXMarked()) {
                    triedWays.remove(cell);
                }
            }
        }
    }

    @Override
    public void undo() {
        Set<MazeCell> triedWays = getGrid().getTriedWays();
        for (GridCell coords : cellCoordinates) {
            MazeCell cell = getGrid().getCell(coords.row(), coords.col());

            if (mark) {
                cell.removeMark();
                if (!cell.isXMarked()) {
                    triedWays.remove(cell);
                }
            } else {
                cell.addMark();
                if (cell.isXMarked()) {
                    triedWays.add(cell);
                }
            }
        }
    }

    private MazeGrid getGrid() {
        Maze maze = getMaze();
        return maze.isHasMultipleFloors() ? maze.getMazeGridFloors().get(floor).getMazeGrid() : maze.getMazeGrid();
    }

    @Override
    public byte[] getCommandsAsBytes() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(getType());
            dos.writeLong(getTimestamp().toEpochSecond(ZoneOffset.UTC));
            dos.writeInt(getTimestamp().getNano());
            dos.writeShort(floor);
            dos.writeBoolean(mark);

            dos.writeInt(cellCoordinates.size());
            for (GridCell coords : cellCoordinates) {
                dos.writeShort(coords.row());
                dos.writeShort(coords.col());
            }
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
