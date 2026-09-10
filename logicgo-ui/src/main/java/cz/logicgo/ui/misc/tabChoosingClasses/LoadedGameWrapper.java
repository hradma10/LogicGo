package cz.logicgo.ui.misc.tabChoosingClasses;

import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.engine.util.generate.SeedCreator;

public record LoadedGameWrapper(Game game) implements TabChoiceWrapper {

    @Override
    public GameInit toGameInit() {
        return SeedCreator.convertToGameInit(game);
    }

    @Override
    public TypeGame getTypeGame() {
        return game != null ? game.getGameType() : null;
    }

    @Override
    public boolean isFixed() {
        return true;
    }
}
