package cz.logicgo.core.builders.shikaku;


import cz.logicgo.core.builders.GameCreation;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;

public abstract sealed class ShikakuBuilderBase<T extends ShikakuBuilderBase<T>> extends GameCreation<T> permits ShikakuCreation {

    private ShikakuType shikakuType;

    public ShikakuBuilderBase() {
        this.setTypeGame(TypeGame.SHIKAKU);
    }

    public ShikakuType getShikakuType() {
        return shikakuType;
    }

    public T setShikakuType(ShikakuType shikakuType) {
        this.shikakuType = shikakuType;
        return self();
    }

}
