package cz.logicgo.core.gameClasses.maze.dataStructures;

import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;

public record StartAndEnd(MazeCell startCell, MazeDirection startDirection, MazeCell endCell,
                          MazeDirection endDirection) {
}
