package cz.logicgo.core.gameClasses.favorites;

import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;

import java.util.Objects;

public final class ShikakuFavorite extends GameFavorite {

    private ShikakuType shikakuType;

    public ShikakuFavorite() {
        super();
    }

    @Override
    public TypeGame getTypeGame() {
        return TypeGame.SHIKAKU;
    }

    public ShikakuType getShikakuType() {
        return shikakuType;
    }

    public ShikakuFavorite setShikakuType(ShikakuType shikakuType) {
        this.shikakuType = shikakuType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ShikakuFavorite that)) return false;
        if (!super.equals(o)) return false;
        return getShikakuType() == that.getShikakuType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getShikakuType());
    }
}
