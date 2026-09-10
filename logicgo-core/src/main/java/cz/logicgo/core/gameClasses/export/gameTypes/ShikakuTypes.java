package cz.logicgo.core.gameClasses.export.gameTypes;


import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;

public record ShikakuTypes(
        Difficulty difficulty,
        int count,
        ShikakuType shikakuType,
        Integer width,
        Integer height
) implements GameMode {
    @Override
    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public TypeGame getTypeGame() {
        return TypeGame.SHIKAKU;
    }
}
