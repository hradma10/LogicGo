package cz.logicgo.core.gameClasses.export.gameTypes;


import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;

public record BridgeTypes(
        Difficulty difficulty,
        int count,
        BridgeType bridgeType,
        Integer width,
        Integer height,
        Integer multipleCount
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
        return TypeGame.BRIDGE;
    }
}
