package cz.logicgo.core.builders.bridge;


import cz.logicgo.core.builders.GameCreation;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;

public abstract sealed class BridgeBuilderBase<T extends BridgeBuilderBase<T>> extends GameCreation<T> permits BridgeCreation {

    private BridgeType bridgeType;
    private int numberOfIslands;
    private int maxBridgeCount;

    public BridgeBuilderBase() {
        this.setTypeGame(TypeGame.BRIDGE);
    }

    public BridgeType getBridgeType() {
        return bridgeType;
    }

    public T setBridgeType(BridgeType bridgeType) {
        this.bridgeType = bridgeType;
        return self();
    }

    public int getNumberOfIslands() {
        return numberOfIslands;
    }

    public T setNumberOfIslands(int numberOfIslands) {
        this.numberOfIslands = numberOfIslands;
        return self();
    }

    public int getMaxBridgeCount() {
        return maxBridgeCount;
    }

    public T setMaxBridgeCount(int maxBridgeCount) {
        this.maxBridgeCount = maxBridgeCount;
        return self();
    }
}
