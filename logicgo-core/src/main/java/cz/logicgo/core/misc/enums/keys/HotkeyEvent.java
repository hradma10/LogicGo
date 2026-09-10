package cz.logicgo.core.misc.enums.keys;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.enums.keys.game.GeneralGameEvents;
import cz.logicgo.core.misc.enums.keys.game.maze.GeneralMazeEvents;
import cz.logicgo.core.misc.enums.keys.game.maze.HexagonalMazeEvents;
import cz.logicgo.core.misc.enums.keys.game.maze.RectangularMazeEvents;
import cz.logicgo.core.misc.enums.keys.game.sudoku.SudokuEvents;
import cz.logicgo.core.misc.interfaces.Translatable;

public sealed interface HotkeyEvent extends Translatable permits GeneralEvent, GeneralGameEvents, GeneralMazeEvents, HexagonalMazeEvents, RectangularMazeEvents, SudokuEvents {
    static HotkeyEvent getEventTypeFromName(Class<? extends HotkeyEvent> enumClass, String name) {
        if (name == null || enumClass == null) return null;

        for (HotkeyEvent event : enumClass.getEnumConstants()) {
            if (event.getQualifiedName().equals(name)) {
                return event;
            }
        }
        return null;
    }

    String getName();

    String getNamespace();

    default String getQualifiedName() {
        return getNamespace() + ":" + getName();
    }

    default String getTranslationName() {
        return getNamespace() + ".hotkey." + getName();
    }

    @Override
    default String getTranslation() {
        return TranslationLoader.getTranslation(getTranslationName());
    }

    String getSectionTranslationKey();
}
