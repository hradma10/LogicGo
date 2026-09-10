package cz.logicgo.core.factoryInit.bridge;


import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;

public class BridgeInit extends GameInit {

    public BridgeInit() {
        super(null, TypeGame.BRIDGE);
    }

    public BridgeInit(User player) {
        super(player, TypeGame.BRIDGE);
    }

    public BridgeInit(long id, User player) {
        super(id, player);
    }

    int width;
    int height;
    int islandCount;
    BridgeType bridgeType;
    int multipleCount;

    public int getWidth() {
        return width;
    }

    public BridgeInit setMultipleCount(int multipleCount) {
        this.multipleCount = multipleCount;
        return this;
    }

    public int getMultipleCount() {
        return multipleCount;
    }

    public BridgeInit setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public BridgeInit setHeight(int height) {
        this.height = height;
        return this;
    }

    public int getIslandCount() {
        return islandCount;
    }

    public BridgeInit setIslandCount(int islandCount) {
        this.islandCount = islandCount;
        return this;
    }

    public BridgeType getBridgeType() {
        return bridgeType;
    }

    public BridgeInit setBridgeType(BridgeType bridgeType) {
        this.bridgeType = bridgeType;
        return this;
    }

    @Override
    public BridgeInit setId(Long id) {
        super.setId(id);
        return this;
    }

    @Override
    public BridgeInit setDifficulty(Difficulty difficulty) {
        super.setDifficulty(difficulty);
        return this;
    }

    @Override
    public BridgeInit setSeed(Long seed) {
        super.setSeed(seed);
        return this;
    }

    @Override
    public BridgeInit setTypeGame(TypeGame typeGame) {
        super.setTypeGame(typeGame);
        return this;
    }
}
