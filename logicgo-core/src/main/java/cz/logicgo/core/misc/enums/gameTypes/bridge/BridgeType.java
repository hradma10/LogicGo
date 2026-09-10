package cz.logicgo.core.misc.enums.gameTypes.bridge;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.annotations.PreloadCategory;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.GameType;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;

import java.util.HashMap;

@PreloadCategory(folder = "bridge")
public enum BridgeType implements GameType, Translatable, PersistableEnum {

    CLASSIC(1, "bridge.type.classic"),
    MULTIPLE(3, "bridge.type.multiple");

    final private int id;
    final private String name;
    BridgeType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static HashMap<BridgeType, String> getNames() {
        HashMap<BridgeType, String> strings = new HashMap<>();
        for (BridgeType bridgeType : BridgeType.values()) {
            strings.put(bridgeType, bridgeType.getName().split("\\.")[2]);
        }
        return strings;
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public TypeGame getTypeGame() {
        return TypeGame.BRIDGE;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getTranslation() {
        return TranslationLoader.getTranslation(name);
    }

    public int getId() {
        return id;
    }
}
