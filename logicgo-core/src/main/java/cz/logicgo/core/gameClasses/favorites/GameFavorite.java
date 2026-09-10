package cz.logicgo.core.gameClasses.favorites;


import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TypeGame;

import java.util.Objects;

import static cz.logicgo.core.misc.formatter.FavoriteFormatter.format;


public abstract sealed class GameFavorite permits BridgeFavorite, MazeFavorite, ShikakuFavorite, SudokuFavorite {
    private Difficulty difficulty;
    private int width;
    private int height;

    public GameFavorite() {
    }

    public GameFavorite(Difficulty difficulty, int width, int height) {
        this.difficulty = difficulty;
        this.width = width;
        this.height = height;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public abstract TypeGame getTypeGame();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameFavorite that)) return false;
        return getWidth() == that.getWidth() && getHeight() == that.getHeight() && getDifficulty() == that.getDifficulty() && getTypeGame() == that.getTypeGame();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDifficulty(), getWidth(), getHeight(), getTypeGame());
    }

    @Override
    public String toString() {
        return format(this);
    }
}
