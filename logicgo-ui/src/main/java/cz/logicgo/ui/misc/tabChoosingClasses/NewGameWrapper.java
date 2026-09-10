package cz.logicgo.ui.misc.tabChoosingClasses;

import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.misc.enums.OpenType;

public record NewGameWrapper(GameInit init) implements TabChoiceWrapper {

    @Override
    public GameInit toGameInit() {
        return init;
    }

    @Override
    public boolean isFixed() {
        return init != null && init.getOpenType() == OpenType.SEEDED;
    }
}
