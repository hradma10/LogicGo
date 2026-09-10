package cz.logicgo.core.gameClasses.favorites;

import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;

import java.util.Objects;

public final class BridgeFavorite extends GameFavorite {

    private BridgeType bridgeType;
    private int multipleCount;
    private int islandCount;

    public BridgeFavorite() {
        super();
    }

    public BridgeType getBridgeType() {
        return bridgeType;
    }

    @Override
    public TypeGame getTypeGame() {
        return TypeGame.BRIDGE;
    }

    public void setBridgeType(BridgeType bridgeType) {
        this.bridgeType = bridgeType;
    }

    public int getIslandCount() {
        return islandCount;
    }

    public BridgeFavorite setIslandCount(int islandCount) {
        this.islandCount = islandCount;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BridgeFavorite that)) return false;
        if (!super.equals(o)) return false;
        return getIslandCount() == that.getIslandCount() && getBridgeType() == that.getBridgeType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getBridgeType(), getIslandCount());
    }

    public int getMultipleCount() {
        return multipleCount;
    }

    public BridgeFavorite setMultipleCount(int multipleCount) {
        this.multipleCount = multipleCount;
        return this;
    }
}
