package cz.logicgo.core.misc.enums.hints;

import cz.logicgo.core.misc.interfaces.Translatable;

public enum BridgeHintType implements Translatable {
    CHECK_ISLAND("bridge.hint_type.check_island"),
    CHECK_COUNT("bridge.hint_type.check_count"),
    CHECK_BRIDGE("bridge.hint_type.check_bridge"),
    RANDOM_BRIDGE("bridge.hint_type.random_bridge"),
    CHOSEN_PAIR("bridge.hint_type.chosen_pair"),
    ;

    final String name;


    BridgeHintType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
