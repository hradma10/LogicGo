package cz.logicgo.ui.handlers.mazeGame;

import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.core.gameClasses.maze.MazeUtils;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.gameModes.*;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;
import cz.logicgo.core.misc.enums.keys.HotkeyEvent;
import cz.logicgo.core.misc.enums.keys.game.GeneralGameEvents;
import cz.logicgo.core.misc.enums.keys.game.maze.GeneralMazeEvents;
import cz.logicgo.core.misc.enums.keys.game.maze.HexagonalMazeEvents;
import cz.logicgo.core.misc.enums.keys.game.maze.RectangularMazeEvents;
import cz.logicgo.ui.commands.CommandExecutor;
import cz.logicgo.ui.commands.mazeCommands.MoveLevelCommand;
import cz.logicgo.ui.commands.mazeCommands.MoveMazeCommand;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.MazeGameController;
import cz.logicgo.ui.controllers.settingsControllers.DefaultKeyBindings;
import cz.logicgo.ui.misc.windows.AlertBox;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import static cz.logicgo.ui.utils.KeyEventUtils.fromJavaFx;


public class KeyPressedMazeHandler extends MazeHandlerBase {

    public KeyPressedMazeHandler(Maze maze, MazeGameController mazeGameController) {
        super(maze, mazeGameController);
    }

    public void onKeyPressed(KeyEvent keyEvent) throws IOException {
        boolean forceRedraw = false;
        Maze maze = getMaze();
        User user = maze.getPlayer();
        MazeGameController con = getMazeGameController();
        CommandExecutor commandExecutor = con.getCommandExecutor();
        Collection<KeyEventDTO> hotkeys = user.getSavedHotkeys().values();
        KeyEventDTO keyEventDTO = fromJavaFx(keyEvent);

        con.stopHintFeedbackTimer();
        con.getGameInstance().getMazeGridFloors().forEach(floor ->
                floor.getMazeGrid().getPath().setMazeHintType(null)
        );
        con.redrawCanvases(false, true);

        KeyEventDTO matching = null;
        Class<? extends HotkeyEvent> hotkeyEventClass = null;
        switch (maze.getMazeShape()) {
            case RECTANGULAR -> hotkeyEventClass = RectangularMazeEvents.class;
            case HEXAGONAL -> hotkeyEventClass = HexagonalMazeEvents.class;
        }

        List<Class<? extends HotkeyEvent>> allowedEnumsToPress = DefaultKeyBindings.getExclusiveForMazeTypes().get(hotkeyEventClass);
        finding:
        for (KeyEventDTO hotkey : hotkeys) {
            if (keyEventDTO.matches(hotkey)) {
                for (Class<?> allowed : allowedEnumsToPress) {
                    if (allowed.isInstance(hotkey.getKeystrokeEvent())) {
                        matching = hotkey;
                        break finding;
                    }
                }
            }
        }

        if (matching == null) return;

        switch (matching.getKeystrokeEvent()) {
            case GeneralGameEvents generalGameEvents -> {
                switch (generalGameEvents) {
                    case SAVE -> con.saveGame(true);
                    case UNDO -> {
                        commandExecutor.undo();
                        forceRedraw = true;
                    }
                    case REDO -> {
                        commandExecutor.redo();
                        forceRedraw = true;
                    }
                    case RESET -> {
                        if (AlertBox.initRestartGame()) {
                            con.onRestart();
                            forceRedraw = true;
                        }
                    }
                    case SOLUTION -> con.openSolution();
                    case HINT -> con.openHint();
                    case EXPORT -> con.openExport();
                    case PAUSE -> con.pause();
                    case EXIT -> con.onExit();
                }
            }
            case GeneralMazeEvents generalMazeEvents -> {
                if (!maze.isHasMultipleFloors()) return;

                switch (generalMazeEvents) {
                    case LEVEL_CHANGE_UP -> {
                        int currentFloor = maze.getCurrentFloor();
                        int maxFloor = maze.getMazeGridFloors().size() - 1;
                        if (currentFloor >= maxFloor) return;

                        commandExecutor.execute(new MoveLevelCommand(maze, 1));
                        con.initFloorPath();
                        con.refreshDataComponents();
                        forceRedraw = true;
                    }
                    case LEVEL_CHANGE_DOWN -> {
                        int currentFloor = maze.getCurrentFloor();
                        if (currentFloor <= 0) return;

                        commandExecutor.execute(new MoveLevelCommand(maze, -1));
                        con.initFloorPath();
                        con.refreshDataComponents();
                        forceRedraw = true;
                    }
                }
            }
            case RectangularMazeEvents rectangularMazeEvents -> {
                MazeGrid grid = getGrid();
                MazeCell currentCell = getCurrentActiveCell(maze, con);
                MazeCell standardNeighbor = null;

                if (currentCell != null) {
                    switch (rectangularMazeEvents) {
                        case RECTANGLE_MOVE_UP, RECTANGLE_MOVE_DOWN, RECTANGLE_MOVE_LEFT, RECTANGLE_MOVE_RIGHT -> {
                            MazeDirection dir = DefaultKeyBindings.getDirectionsToHotkeys().get(rectangularMazeEvents);
                            standardNeighbor = currentCell.getNeighbourFromDirection(dir);
                        }
                    }

                    MazeCell neighbor = getValidOrPortalNeighbor(currentCell, standardNeighbor);

                    if (tryMovePlayer(maze, grid, currentCell, neighbor, commandExecutor, true)) {
                        forceRedraw = true;
                    }
                }
            }
            case HexagonalMazeEvents hexagonalMazeEvents -> {
                MazeGrid grid = getGrid();
                MazeCell currentCell = getCurrentActiveCell(maze, con);
                MazeCell standardNeighbor = null;

                if (currentCell != null) {
                    switch (hexagonalMazeEvents) {
                        case HEXAGONAL_MOVE_TOP, HEXAGONAL_MOVE_TOP_RIGHT, HEXAGONAL_MOVE_BOTTOM_RIGHT,
                             HEXAGONAL_MOVE_BOTTOM, HEXAGONAL_MOVE_BOTTOM_LEFT, HEXAGONAL_MOVE_TOP_LEFT -> {
                            MazeDirection dir = DefaultKeyBindings.getDirectionsToHotkeys().get(hexagonalMazeEvents);
                            standardNeighbor = currentCell.getNeighbourFromDirection(dir);
                        }
                    }

                    MazeCell neighbor = getValidOrPortalNeighbor(currentCell, standardNeighbor);

                    if (tryMovePlayer(maze, grid, currentCell, neighbor, commandExecutor, true)) {
                        forceRedraw = true;
                    }
                }
            }
            default -> {
            }
        }

        switch (keyEvent.getCode()) {
            case ESCAPE -> {
                if (con.isHintChoice()) {
                    con.setHintChoice(false);
                    con.setActiveHint(null);
                    con.clearHintVisuals();
                    forceRedraw = true;
                }
            }
        }

        if (checkAutoAdvanceFloor(maze, con, commandExecutor)) {
            forceRedraw = true;
        }

        if (forceRedraw) {
            con.redrawCanvases(true, true);
        }

        if (MazeUtils.checkFinishedMaze(maze)) {
            con.gameFinished();
        }

        keyEvent.consume();
    }


    private MazeCell getCurrentActiveCell(Maze maze, MazeGameController con) {
        MazeGrid grid = maze.getMazeGridFloors().get(maze.getCurrentFloor()).getMazeGrid();

        MazeCell currentCell = grid.getPath().getActivePath().peek();
        if (currentCell != null) {
            return currentCell;
        }

        con.initFloorPath();
        return grid.getPath().getActivePath().peek();
    }

    private boolean checkAutoAdvanceFloor(Maze maze, MazeGameController con, CommandExecutor executor) {
        if (!maze.isHasMultipleFloors()) return false;

        int currentFloorIndex = maze.getCurrentFloor();
        MazeGrid currentGrid = maze.getMazeGridFloors().get(currentFloorIndex).getMazeGrid();

        if (MazeUtils.isFloorCompleted(currentGrid) && currentFloorIndex < maze.getMazeGridFloors().size() - 1) {
            executor.execute(new MoveLevelCommand(maze, 1));
            con.initFloorPath();
            con.refreshDataComponents();
            return true;
        }

        return false;
    }

    private MazeCell getValidOrPortalNeighbor(MazeCell currentCell, MazeCell standardNeighbor) {
        if (standardNeighbor != null && currentCell.isLinked(standardNeighbor)) {
            return standardNeighbor;
        }

        for (MazeCell linked : currentCell.getLinked()) {
            if (!currentCell.getNeighbours().contains(linked)) {
                return linked;
            }
        }

        return null;
    }

    private boolean tryMovePlayer(Maze maze, MazeGrid grid, MazeCell currentCell, MazeCell neighbor, CommandExecutor executor, boolean enforceLogic) {
        if (neighbor == null) return false;

        if (enforceLogic) {
            Deque<MazeCell> activePath = grid.getPath().getActivePath();
            boolean isBacktracking = false;

            if (activePath != null && activePath.size() >= 2) {
                List<MazeCell> pathList = new ArrayList<>(activePath);
                MazeCell previousCell = pathList.get(1);

                if (neighbor.equals(previousCell)) {
                    isBacktracking = true;
                }
            }

            OneWayModifier oneWayMod = null;
            TollModifier tollMod = null;
            PatternModifier patternMod = null;
            CheckpointModifier checkpointModifier = null;
            ExactStepsModifier exactStepsModifier = null;

            for (var mod : grid.getModifiers()) {
                if (mod instanceof OneWayModifier o) oneWayMod = o;
                if (mod instanceof TollModifier t) tollMod = t;
                if (mod instanceof PatternModifier p) patternMod = p;
                if (mod instanceof CheckpointModifier ch) checkpointModifier = ch;
                if (mod instanceof ExactStepsModifier e) exactStepsModifier = e;
            }

            if (!isBacktracking && activePath != null) {
                if (oneWayMod != null) {
                    Pair<MazeCell, MazeCell> moveForward = new Pair<>(currentCell, neighbor);
                    Pair<MazeCell, MazeCell> moveBackward = new Pair<>(neighbor, currentCell);

                    List<Pair<MazeCell, MazeCell>> paths = oneWayMod.oneWayPaths();
                    if (paths.contains(moveBackward) && !paths.contains(moveForward)) {
                        return false;
                    }
                }

                if (tollMod != null) {
                    int currentBalance = MazeUtils.countCurrentCoins(grid, tollMod);
                    int cellModifier = tollMod.tolls().getOrDefault(neighbor, 0);

                    if (currentBalance + cellModifier < 0) {
                        return false;
                    }
                }

                if (patternMod != null) {
                    Integer neighborPattern = patternMod.getCellPatterns().get(neighbor);

                    if (neighborPattern != null) {
                        int currentProgress = MazeUtils.countPatternProgress(grid, patternMod);
                        if (neighborPattern != (currentProgress % patternMod.getPatternCount())) {
                            return false;
                        }
                    }
                }

                if (checkpointModifier != null) {
                    Pair<MazeCell, MazeCell> moveForward = new Pair<>(currentCell, neighbor);
                    Pair<MazeCell, MazeCell> moveBackward = new Pair<>(neighbor, currentCell);

                    List<Pair<MazeCell, MazeCell>> oneWays = checkpointModifier.getOneWays();
                    if (oneWays.contains(moveBackward) && !oneWays.contains(moveForward)) {
                        return false;
                    }
                }

                if (exactStepsModifier != null) {
                    int steps = activePath.size();

                    int neededSteps = exactStepsModifier.targetSteps();
                    if (steps > neededSteps) {
                        return false;
                    }
                }
            }
        }

        executor.execute(new MoveMazeCommand(maze, currentCell, neighbor, maze.getCurrentFloor()));

        getMazeGameController().getTabState().setChangePending();

        if (maze.isHasMultipleFloors()) {
            MazeGrid currentGrid = maze.getMazeGridFloors().get(maze.getCurrentFloor()).getMazeGrid();

            if (currentGrid.getCell(neighbor.getRow(), neighbor.getCol()) != neighbor) {
                for (int i = 0; i < maze.getMazeGridFloors().size(); i++) {
                    MazeGrid floorGrid = maze.getMazeGridFloors().get(i).getMazeGrid();
                    if (floorGrid.getCell(neighbor.getRow(), neighbor.getCol()) == neighbor) {
                        int delta = i - maze.getCurrentFloor();
                        executor.execute(new MoveLevelCommand(maze, delta));
                        getMazeGameController().refreshDataComponents();
                        break;
                    }
                }
            }
        }

        return true;
    }
}
