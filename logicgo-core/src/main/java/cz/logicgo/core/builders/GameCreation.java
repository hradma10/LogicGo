package cz.logicgo.core.builders;


import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.StepCounter;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TypeGame;

public abstract class GameCreation<T extends GameCreation<T>> {

    protected User player;
    protected Difficulty difficulty;
    protected Long seed;
    protected int height;
    protected int width;
    protected TypeGame typeGame;
    protected boolean forExport;
    private StepCounter stepCounter = null;

    protected abstract T self();

    public T setPlayer(User player) {
        this.player = player;
        return self();
    }

    public T setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        return self();
    }

    public T setSeed(Long seed) {
        this.seed = seed;
        return self();
    }

    public User getPlayer() {
        return player;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Long getSeed() {
        return seed;
    }


    public int getHeight() {
        return height;
    }

    public T setHeight(int height) {
        this.height = height;
        return self();
    }

    public int getWidth() {
        return width;
    }

    public T setWidth(int width) {
        this.width = width;
        return self();
    }

    public TypeGame getTypeGame() {
        return typeGame;
    }

    protected T setTypeGame(TypeGame typeGame) {
        this.typeGame = typeGame;
        return self();
    }

    public boolean isForExport() {
        return forExport;
    }

    public T setForExport(boolean forExport) {
        this.forExport = forExport;
        return self();
    }

    public StepCounter getStepCounter() {
        return stepCounter;
    }

    public T setStepCounter(StepCounter stepCounter) {
        this.stepCounter = stepCounter;
        return self();
    }
}
