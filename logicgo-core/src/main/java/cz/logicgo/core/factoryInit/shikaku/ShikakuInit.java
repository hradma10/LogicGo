package cz.logicgo.core.factoryInit.shikaku;


import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;

public class ShikakuInit extends GameInit {


    public ShikakuInit() {
        super(null, TypeGame.SHIKAKU);
    }

    public ShikakuInit(User player) {
        super(player, TypeGame.SHIKAKU);
    }

    public ShikakuInit(long id, User player) {
        super(id, player);
    }

    int width;
    int height;
    ShikakuType shikakuType;
    private boolean[][] mask;

    public int getWidth() {
        return width;
    }

    public ShikakuInit setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public ShikakuInit setHeight(int height) {
        this.height = height;
        return this;
    }

    public ShikakuType getShikakuType() {
        return shikakuType;
    }

    public ShikakuInit setShikakuType(ShikakuType shikakuType) {
        this.shikakuType = shikakuType;
        return this;
    }

    @Override
    public ShikakuInit setId(Long id) {
        super.setId(id);
        return this;
    }

    @Override
    public ShikakuInit setDifficulty(Difficulty difficulty) {
        super.setDifficulty(difficulty);
        return this;
    }

    @Override
    public ShikakuInit setSeed(Long seed) {
        super.setSeed(seed);
        return this;
    }

    @Override
    public ShikakuInit setTypeGame(TypeGame typeGame) {
        super.setTypeGame(typeGame);
        return this;
    }
}
