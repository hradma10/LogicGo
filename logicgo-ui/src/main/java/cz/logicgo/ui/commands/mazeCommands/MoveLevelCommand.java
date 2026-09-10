package cz.logicgo.ui.commands.mazeCommands;


import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.ui.commands.CommandType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class MoveLevelCommand extends MazeCommand {

    final private int levelDelta;

    public MoveLevelCommand(Maze maze, int levelDelta) {
        super(maze);
        this.levelDelta = levelDelta;
    }

    public MoveLevelCommand(LocalDateTime timestamp, Maze maze, int levelDelta) {
        super(timestamp, maze);
        this.levelDelta = levelDelta;
    }

    @Override
    public byte getType() {
        return CommandType.MOVE_LEVEL.getType();
    }

    @Override
    public void execute() {
        int currentLevel = getMaze().getCurrentFloor();
        int newLevel = currentLevel + levelDelta;
        getMaze().setCurrentFloor(newLevel);
    }

    @Override
    public void undo() {
        int currentLevel = getMaze().getCurrentFloor();
        int newLevel = currentLevel - levelDelta;
        getMaze().setCurrentFloor(newLevel);
    }

    public int getLevelDelta() {
        return levelDelta;
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

            dos.writeShort(levelDelta);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
