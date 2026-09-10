package cz.logicgo.ui.misc.tabChoosingClasses;

import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.misc.enums.TypeGame;

public sealed interface TabChoiceWrapper permits FavoriteGameWrapper, LoadedGameWrapper, NewGameWrapper {


    GameInit toGameInit();

    default TypeGame getTypeGame() {
        GameInit init = toGameInit();
        return init != null ? init.getTypeGame() : null;
    }

    default boolean isFixed() {
        return false;
    }
}
