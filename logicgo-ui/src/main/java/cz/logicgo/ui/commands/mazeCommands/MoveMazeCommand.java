package cz.logicgo.ui.commands.mazeCommands;


import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.ui.commands.CommandType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class MoveMazeCommand extends MazeCommand {

    final private MazeCell previousCell;
    final private MazeCell nextCell;
    final private int level;

    public MoveMazeCommand(Maze maze, MazeCell previousCell, MazeCell nextCell, int level) {
        super(maze);
        this.previousCell = previousCell;
        this.nextCell = nextCell;
        this.level = level;
    }

    public MoveMazeCommand(Maze maze, MazeCell previousCell, MazeCell nextCell) {
        super(maze);
        this.previousCell = previousCell;
        this.nextCell = nextCell;
        this.level = 0;
    }

    public MoveMazeCommand(LocalDateTime timestamp, Maze maze, MazeCell previousCell, MazeCell nextCell, int level) {
        super(timestamp, maze);
        this.previousCell = previousCell;
        this.nextCell = nextCell;
        this.level = level;
    }

    @Override
    public void execute() {
        MazeGrid mazeGrid = getMaze().getMazeGridFloors().get(level).getMazeGrid();
        mazeGrid.getPath().move(nextCell);
    }

    @Override
    public void undo() {
        MazeGrid mazeGrid = getMaze().getMazeGridFloors().get(level).getMazeGrid();
        mazeGrid.getPath().move(previousCell);
    }

    @Override
    public byte getType() {
        return CommandType.MOVE_MAZE.getType();
    }

    public MazeCell getPreviousCell() {
        return previousCell;
    }

    public MazeCell getNextCell() {
        return nextCell;
    }

    @Override
    public byte[] getCommandsAsBytes() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            byte type = getType();
            dos.writeByte(type);

            long epochSecond = getTimestamp().toEpochSecond(ZoneOffset.UTC);
            int nano = getTimestamp().getNano();

            dos.writeLong(epochSecond);
            dos.writeInt(nano);

            dos.writeShort(level);

            if (previousCell != null) {
                dos.writeBoolean(true);
                dos.writeShort(previousCell.getRow());
                dos.writeShort(previousCell.getCol());
            } else {
                dos.writeBoolean(false);
            }
            if (nextCell != null) {
                dos.writeBoolean(true);
                dos.writeShort(nextCell.getRow());
                dos.writeShort(nextCell.getCol());
            } else {
                dos.writeBoolean(false);
            }

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getLevel() {
        return level;
    }
}
