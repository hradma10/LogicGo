package cz.logicgo.ui.controllers.settingsControllers;

import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.HexagonalDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;
import cz.logicgo.core.misc.enums.keys.GeneralEvent;
import cz.logicgo.core.misc.enums.keys.HotkeyEvent;
import cz.logicgo.core.misc.enums.keys.game.GeneralGameEvents;
import cz.logicgo.core.misc.enums.keys.game.maze.GeneralMazeEvents;
import cz.logicgo.core.misc.enums.keys.game.maze.HexagonalMazeEvents;
import cz.logicgo.core.misc.enums.keys.game.maze.RectangularMazeEvents;
import cz.logicgo.core.misc.enums.keys.game.sudoku.SudokuEvents;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static javafx.scene.input.KeyCode.valueOf;

public class DefaultKeyBindings {

    private static final Map<Class<? extends HotkeyEvent>, List<KeyEventDTO>> staticHotkeys;
    private static final Map<Class<? extends HotkeyEvent>, List<Class<? extends HotkeyEvent>>> exclusiveByClass;
    private static final Map<Class<? extends HotkeyEvent>, List<Class<? extends HotkeyEvent>>> exclusiveForMazeTypes;
    private static final List<Class<? extends HotkeyEvent>> allClasses;
    private static final Map<HotkeyEvent, MazeDirection> directionsToHotkeys;

    static {
        HashMap<Class<? extends HotkeyEvent>, List<KeyEventDTO>> staticHotkeysNew = new HashMap<>();

        IntStream.rangeClosed(0, 9).forEach(i -> {
            bindStatic(staticHotkeysNew, SudokuEvents.class, valueOf("NUMPAD" + i));
            bindStatic(staticHotkeysNew, SudokuEvents.class, valueOf("DIGIT" + i));
        });
        for (char c = 'A'; c <= 'G'; c++) {
            KeyCode code = KeyCode.getKeyCode(String.valueOf(c));
            if (code != null) {
                bindStatic(staticHotkeysNew, SudokuEvents.class, code);
            }
        }

        bindStatic(staticHotkeysNew, SudokuEvents.class, KeyCode.DELETE);
        bindStatic(staticHotkeysNew, SudokuEvents.class, KeyCode.BACK_SPACE);
        bindStatic(staticHotkeysNew, GeneralEvent.class, KeyCode.ESCAPE);

        staticHotkeys = Map.copyOf(staticHotkeysNew);

        HashMap<Class<? extends HotkeyEvent>, List<Class<? extends HotkeyEvent>>> getExclusiveNew = new HashMap<>();

        getExclusiveNew.put(SudokuEvents.class, List.of(SudokuEvents.class, GeneralEvent.class, GeneralGameEvents.class));

        getExclusiveNew.put(GeneralMazeEvents.class, List.of(GeneralMazeEvents.class, GeneralEvent.class, GeneralGameEvents.class, HexagonalMazeEvents.class, RectangularMazeEvents.class));
        getExclusiveNew.put(RectangularMazeEvents.class, List.of(GeneralMazeEvents.class, GeneralEvent.class, GeneralGameEvents.class, HexagonalMazeEvents.class, RectangularMazeEvents.class));
        getExclusiveNew.put(HexagonalMazeEvents.class, List.of(GeneralMazeEvents.class, GeneralEvent.class, GeneralGameEvents.class, HexagonalMazeEvents.class, RectangularMazeEvents.class));

        getExclusiveNew.put(GeneralEvent.class, List.of(GeneralEvent.class, GeneralGameEvents.class, SudokuEvents.class, GeneralMazeEvents.class, RectangularMazeEvents.class, HexagonalMazeEvents.class));

        exclusiveByClass = Map.copyOf(getExclusiveNew);

        HashMap<Class<? extends HotkeyEvent>, List<Class<? extends HotkeyEvent>>> exclusiveForMazeTypesNew = new HashMap<>();

        exclusiveForMazeTypesNew.put(RectangularMazeEvents.class, List.of(GeneralMazeEvents.class, GeneralEvent.class, GeneralGameEvents.class, RectangularMazeEvents.class));
        exclusiveForMazeTypesNew.put(HexagonalMazeEvents.class, List.of(GeneralMazeEvents.class, GeneralEvent.class, GeneralGameEvents.class, HexagonalMazeEvents.class));
        exclusiveForMazeTypesNew.put(GeneralMazeEvents.class, List.of(GeneralMazeEvents.class, GeneralEvent.class, GeneralGameEvents.class, RectangularMazeEvents.class, HexagonalMazeEvents.class));

        exclusiveForMazeTypes = Map.copyOf(exclusiveForMazeTypesNew);

        allClasses = exclusiveByClass.get(GeneralEvent.class);

        HashMap<HotkeyEvent, MazeDirection> directionsToHotkeysTemp = new HashMap<>();

        directionsToHotkeysTemp.put(RectangularMazeEvents.RECTANGLE_MOVE_UP, RectangularDirection.NORTH);
        directionsToHotkeysTemp.put(RectangularMazeEvents.RECTANGLE_MOVE_DOWN, RectangularDirection.SOUTH);
        directionsToHotkeysTemp.put(RectangularMazeEvents.RECTANGLE_MOVE_LEFT, RectangularDirection.WEST);
        directionsToHotkeysTemp.put(RectangularMazeEvents.RECTANGLE_MOVE_RIGHT, RectangularDirection.EAST);

        directionsToHotkeysTemp.put(HexagonalMazeEvents.HEXAGONAL_MOVE_TOP, HexagonalDirection.NORTH);
        directionsToHotkeysTemp.put(HexagonalMazeEvents.HEXAGONAL_MOVE_TOP_LEFT, HexagonalDirection.NORTH_WEST);
        directionsToHotkeysTemp.put(HexagonalMazeEvents.HEXAGONAL_MOVE_TOP_RIGHT, HexagonalDirection.NORTH_EAST);
        directionsToHotkeysTemp.put(HexagonalMazeEvents.HEXAGONAL_MOVE_BOTTOM, HexagonalDirection.SOUTH);
        directionsToHotkeysTemp.put(HexagonalMazeEvents.HEXAGONAL_MOVE_BOTTOM_LEFT, HexagonalDirection.SOUTH_WEST);
        directionsToHotkeysTemp.put(HexagonalMazeEvents.HEXAGONAL_MOVE_BOTTOM_RIGHT, HexagonalDirection.SOUTH_EAST);

        directionsToHotkeys = Map.copyOf(directionsToHotkeysTemp);
    }

    public static Map<HotkeyEvent, KeyEventDTO> generateDefaults() {
        Map<HotkeyEvent, KeyEventDTO> map = new HashMap<>();
        bind(map, GeneralGameEvents.SAVE, KeyCode.S, true, false, false);
        bind(map, GeneralGameEvents.UNDO, KeyCode.Z, true, false, false);
        bind(map, GeneralGameEvents.REDO, KeyCode.Y, true, false, false);
        bind(map, GeneralGameEvents.EXIT, KeyCode.ESCAPE);
        bind(map, GeneralGameEvents.RESET, KeyCode.R, true, false, false);
        bind(map, GeneralGameEvents.HINT, KeyCode.H, true, false, false);
        bind(map, GeneralGameEvents.SOLUTION, KeyCode.F1);
        bind(map, GeneralGameEvents.EXPORT, KeyCode.E, true, false, false);
        bind(map, GeneralGameEvents.PAUSE, KeyCode.PAUSE, false, false, false);

        bind(map, SudokuEvents.MOVE_UP, KeyCode.UP);
        bind(map, SudokuEvents.MOVE_DOWN, KeyCode.DOWN);
        bind(map, SudokuEvents.MOVE_LEFT, KeyCode.LEFT);
        bind(map, SudokuEvents.MOVE_RIGHT, KeyCode.RIGHT);
        bind(map, SudokuEvents.DELETE, KeyCode.DELETE);
        bind(map, SudokuEvents.NOTES_MODE, KeyCode.N);

        bind(map, GeneralMazeEvents.LEVEL_CHANGE_UP, KeyCode.R);
        bind(map, GeneralMazeEvents.LEVEL_CHANGE_DOWN, KeyCode.F);

        bind(map, RectangularMazeEvents.RECTANGLE_MOVE_UP, KeyCode.W);
        bind(map, RectangularMazeEvents.RECTANGLE_MOVE_DOWN, KeyCode.S);
        bind(map, RectangularMazeEvents.RECTANGLE_MOVE_LEFT, KeyCode.A);
        bind(map, RectangularMazeEvents.RECTANGLE_MOVE_RIGHT, KeyCode.D);

        bind(map, HexagonalMazeEvents.HEXAGONAL_MOVE_TOP, KeyCode.W);
        bind(map, HexagonalMazeEvents.HEXAGONAL_MOVE_TOP_RIGHT, KeyCode.E);
        bind(map, HexagonalMazeEvents.HEXAGONAL_MOVE_BOTTOM_RIGHT, KeyCode.D);
        bind(map, HexagonalMazeEvents.HEXAGONAL_MOVE_BOTTOM, KeyCode.S);
        bind(map, HexagonalMazeEvents.HEXAGONAL_MOVE_BOTTOM_LEFT, KeyCode.A);
        bind(map, HexagonalMazeEvents.HEXAGONAL_MOVE_TOP_LEFT, KeyCode.Q);

        return map;
    }

    private static void bind(Map<HotkeyEvent, KeyEventDTO> map, HotkeyEvent event, KeyCode code) {
        bind(map, event, code, false, false, false);
    }

    private static void bindStatic(HashMap<Class<? extends HotkeyEvent>, List<KeyEventDTO>> map, Class<? extends HotkeyEvent> eventType, KeyCode code) {
        bindStatic(map, eventType, code, false, false, false);
    }

    private static void bind(Map<HotkeyEvent, KeyEventDTO> map, HotkeyEvent event, KeyCode code, boolean ctrl, boolean shift, boolean alt) {
        KeyEventDTO dto = new KeyEventDTO(
                code != null ? code.name() : "",
                "",
                "",
                shift,
                ctrl,
                alt,
                false,
                event
        );
        map.put(event, dto);
    }

    private static void bindStatic(HashMap<Class<? extends HotkeyEvent>, List<KeyEventDTO>> map, Class<? extends HotkeyEvent> eventType, KeyCode code, boolean ctrl, boolean shift, boolean alt) {
        KeyEventDTO dto = new KeyEventDTO(
                code != null ? code.name() : "",
                "",
                "",
                shift,
                ctrl,
                alt,
                false,
                null
        );
        map.computeIfAbsent(eventType, _ -> new ArrayList<>()).add(dto);
    }

    public static Map<Class<? extends HotkeyEvent>, List<KeyEventDTO>> getStaticHotkeys() {
        return staticHotkeys;
    }

    public static Map<Class<? extends HotkeyEvent>, List<Class<? extends HotkeyEvent>>> getExclusiveByClass() {
        return exclusiveByClass;
    }

    public static List<Class<? extends HotkeyEvent>> getAllClasses() {
        return allClasses;
    }

    public static Map<HotkeyEvent, MazeDirection> getDirectionsToHotkeys() {
        return directionsToHotkeys;
    }

    public static Map<Class<? extends HotkeyEvent>, List<Class<? extends HotkeyEvent>>> getExclusiveForMazeTypes() {
        return exclusiveForMazeTypes;
    }
}
