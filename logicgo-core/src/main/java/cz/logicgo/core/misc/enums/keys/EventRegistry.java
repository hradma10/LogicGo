package cz.logicgo.core.misc.enums.keys;

import cz.logicgo.core.misc.enums.keys.game.GeneralGameEvents;
import cz.logicgo.core.misc.enums.keys.game.maze.GeneralMazeEvents;
import cz.logicgo.core.misc.enums.keys.game.maze.HexagonalMazeEvents;
import cz.logicgo.core.misc.enums.keys.game.maze.RectangularMazeEvents;
import cz.logicgo.core.misc.enums.keys.game.sudoku.SudokuEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventRegistry {

    private static final List<HotkeyEvent> ALL_EVENTS = new ArrayList<>();

    static {
        register(GeneralGameEvents.values());
        register(GeneralMazeEvents.values());
        register(HexagonalMazeEvents.values());
        register(RectangularMazeEvents.values());
        register(SudokuEvents.values());
    }

    private static void register(HotkeyEvent[] keys) {
        Collections.addAll(ALL_EVENTS, keys);
    }

    public static List<HotkeyEvent> getAllEvents() {
        return Collections.unmodifiableList(ALL_EVENTS);
    }
}
