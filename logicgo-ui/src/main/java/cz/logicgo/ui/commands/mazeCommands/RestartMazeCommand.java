package cz.logicgo.ui.commands.mazeCommands;


import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.gameClasses.maze.dataStructures.MazeFloor;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.gameClasses.maze.dataStructures.path.PlayerPath;
import cz.logicgo.core.util.boardConverters.MazeConverters;
import cz.logicgo.ui.commands.CommandType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.IntStream;

public class RestartMazeCommand extends MazeCommand {
    final public static CommandType type = CommandType.REPLACE_MAZE;

    final private List<PlayerPath> oldPaths;
    final private List<PlayerPath> newPaths;


    public RestartMazeCommand(Maze maze, List<PlayerPath> oldPaths, List<PlayerPath> newPaths) {
        super(maze);
        this.oldPaths = oldPaths;
        this.newPaths = newPaths;
    }

    public RestartMazeCommand(LocalDateTime timestamp, Maze maze, List<PlayerPath> oldPaths, List<PlayerPath> newPaths) {
        super(timestamp, maze);
        this.oldPaths = oldPaths;
        this.newPaths = newPaths;
    }

    @Override
    public byte getType() {
        return type.getType();
    }

    @Override
    public void execute() {
        replacePaths(newPaths);
    }

    @Override
    public void undo() {
        replacePaths(oldPaths);
    }

    private void replacePaths(List<PlayerPath> paths) {
        List<MazeFloor> mazeGridFloors = getMaze().getMazeGridFloors();
        int bound = mazeGridFloors.size();
        IntStream.range(0, bound).forEachOrdered(i -> {
            MazeFloor floor = mazeGridFloors.get(i);
            MazeGrid grid = floor.getMazeGrid();
            PlayerPath path = paths.get(i);
            grid.setPath(path);
        });
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

            int oldPathsLength = oldPaths.size();
            dos.writeInt(oldPathsLength);
            for (var path : oldPaths) {
                byte[] pathAsBytes = MazeConverters.serializePath(path);
                int length = pathAsBytes.length;
                dos.writeInt(length);
                dos.write(pathAsBytes);
            }
            int newPathsLength = newPaths.size();
            dos.writeInt(newPathsLength);
            for (var path : newPaths) {
                byte[] pathAsBytes = MazeConverters.serializePath(path);
                int length = pathAsBytes.length;
                dos.writeInt(length);
                dos.write(pathAsBytes);
            }

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
