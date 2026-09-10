package cz.logicgo.core;

import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.entity.setting.UserSetting;
import cz.logicgo.core.gameClasses.sudoku.SudokuUtils;
import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.core.misc.enums.settings.SettingKey;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GameUtils {

    private GameUtils() {}

    public static Game createNewInstance(Game input) {
        return switch (input) {
            case Sudoku sudoku -> new Sudoku(sudoku);
            case Bridge bridge -> new Bridge(bridge);
            case Maze maze -> new Maze(maze);
            case Shikaku shikaku -> new Shikaku(shikaku);
            default -> throw new IllegalStateException("Unexpected value: " + input);
        };
    }

    private static final List<String> DEF_COLORS = List.of(
            "#F05A5A", // rgb(240, 90, 90)
            "#FFA05A", // rgb(255, 160, 90)
            "#FFD250", // rgb(255, 210, 80)
            "#B4E664", // rgb(180, 230, 100)
            "#50DCDC", // rgb(80, 220, 220)
            "#6EAAF5", // rgb(110, 170, 245)
            "#B482FF", // rgb(180, 130, 255)
            "#FF78B4", // rgb(255, 120, 180)
            "#C86EC8"  // rgb(200, 110, 200)
    );

    public static List<String> getDefaultColorTheme() {
        return DEF_COLORS;
    }

    public static Game createNewUnsolvedGameFromSolved(Game input) {
        Game newGame = switch (input) {
            case Sudoku sudoku -> {
                Sudoku newSudoku = new Sudoku(sudoku);
                newSudoku.setId(null);
                var copyBoardUnsolved = SudokuUtils.getBoardCopy(newSudoku.getStartingBoard());
                newSudoku.setBoard(copyBoardUnsolved);
                if (newSudoku.getCandidates() != null) {
                    newSudoku.getCandidates().clear();
                }
                newSudoku.getHistorySudokuPlay().clear();
                yield newSudoku;
            }
            case Bridge bridge -> {
                Bridge newBridge = new Bridge(bridge);
                newBridge.setId(null);
                newBridge.setIslandBridges(new ArrayList<>());
                yield newBridge;
            }
            case Maze maze -> {
                var newMaze = new Maze(maze);
                newMaze.setId(null);
                newMaze.getMazeGridFloors().forEach(floor -> {
                    if (floor.getMazeGrid() != null && floor.getMazeGrid().getPath() != null) {
                        floor.getMazeGrid().getPath().getActivePath().clear();
                        floor.getMazeGrid().getPath().getFullHistory().clear();
                    }
                });
                yield newMaze;
            }
            case Shikaku shikaku -> {
                var newShikaku = new Shikaku(shikaku);
                newShikaku.setId(null);
                newShikaku.getRectangles().clear();
                newShikaku.setCounter(0);
                yield newShikaku;
            }
            default -> throw new IllegalStateException("Unexpected value: " + input);
        };

        newGame.setStatus(Status.IN_PROGRESS);
        newGame.setLastPlayed(null);
        newGame.setFinishedAt(null);
        newGame.setElapsedTime(Duration.ZERO);
        newGame.setUndoStack(new byte[0]);
        newGame.setRedoStack(new byte[0]);

        return newGame;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getTypedSetting(Map<SettingKey, GameSetting> settingsMap, SettingKey key) {
        if (settingsMap == null) {
            return (T) key.getDefaultValue();
        }
        GameSetting override = settingsMap.get(key);
        if (override != null && override.getTypedValue() != null) {
            return (T) override.getTypedValue();
        }
        return (T) key.getDefaultValue();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getTypedSetting(List<UserSetting> settingsList, SettingKey key) {
        if (settingsList == null) {
            return (T) key.getDefaultValue();
        }
        UserSetting override = settingsList.stream()
                .filter(setting -> setting.getKey().equals(key))
                .findFirst()
                .orElse(null);

        if (override != null && override.getTypedValue() != null) {
            return (T) override.getTypedValue();
        }
        return (T) key.getDefaultValue();
    }

    public static Map<SettingKey, GameSetting> getGameSettingsAsMap(List<GameSetting> settings) {
        Map<SettingKey, GameSetting> settingsMap = new HashMap<>();
        if (settings != null) {
            for (GameSetting setting : settings) {
                settingsMap.put(setting.getKey(), setting);
            }
        }
        return settingsMap;
    }
}
