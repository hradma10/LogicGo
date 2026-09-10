package cz.logicgo.ui.misc.tabChoosingClasses;

import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.gameClasses.favorites.GameFavorite;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.util.GameInitFavoriteConverter;

public record FavoriteGameWrapper(GameFavorite favorite) implements TabChoiceWrapper {

    @Override
    public GameInit toGameInit() {
        return GameInitFavoriteConverter.toGameInit(favorite);
    }

    @Override
    public TypeGame getTypeGame() {
        return favorite != null ? favorite.getTypeGame() : null;
    }

    @Override
    public boolean isFixed() {
        return false;
    }
}
