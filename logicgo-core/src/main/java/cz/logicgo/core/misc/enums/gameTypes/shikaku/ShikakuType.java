package cz.logicgo.core.misc.enums.gameTypes.shikaku;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.annotations.PreloadCategory;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.GameType;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;

@PreloadCategory(folder = "shikaku")
public enum ShikakuType implements GameType, PersistableEnum, Translatable {
    CLASSIC(0, "shikaku.type.classic"),
    OFF_BY_ONE(1, "shikaku.type.off_by_one"),
    ;

    final int type;
    final String name;

    ShikakuType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static ShikakuType fromId(int id) {
        if (CLASSIC.getId() == id) {
            return CLASSIC;
        }
        if (OFF_BY_ONE.getId() == id) {
            return OFF_BY_ONE;
        }
        return null;
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public TypeGame getTypeGame() {
        return TypeGame.SHIKAKU;
    }

    public int getType() {
        return type;
    }

    @Override
    public int getId() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTranslation() {
        return TranslationLoader.getTranslation(name);
    }
}
