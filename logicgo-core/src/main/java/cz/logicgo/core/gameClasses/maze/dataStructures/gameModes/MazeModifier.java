package cz.logicgo.core.gameClasses.maze.dataStructures.gameModes;


import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;

public sealed interface MazeModifier permits CheckpointModifier, ExactStepsModifier, OneWayModifier, PatternModifier, PortalModifier, TollModifier, WallModifier, WrapAroundModifier {

    byte[] serialize();

    int getId();

    MazeModifier copy(MazeCell[][] grid);
}
