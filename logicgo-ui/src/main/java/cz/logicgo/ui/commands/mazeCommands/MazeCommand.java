package cz.logicgo.ui.commands.mazeCommands;


import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.ui.commands.Command;

import java.time.LocalDateTime;

public abstract class MazeCommand extends Command {

    final private Maze maze;

    public MazeCommand(Maze maze) {
        this.maze = maze;
    }

    public MazeCommand(LocalDateTime timestamp, Maze maze) {
        super(timestamp);
        this.maze = maze;
    }

    public Maze getMaze() {
        return maze;
    }
}
