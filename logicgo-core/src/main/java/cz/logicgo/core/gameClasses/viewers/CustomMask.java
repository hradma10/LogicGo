package cz.logicgo.core.gameClasses.viewers;


import cz.logicgo.core.misc.enums.TypeGame;

import java.util.Arrays;
import java.util.Objects;

public class CustomMask {

    private TypeGame gameType;
    private boolean[][] layout;

    public TypeGame getGameType() {
        return gameType;
    }

    public void setGameType(TypeGame gameType) {
        this.gameType = gameType;
    }

    public boolean[][] getLayout() {
        return layout;
    }

    public void setLayout(boolean[][] layout) {
        this.layout = layout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomMask that)) return false;
        return gameType == that.gameType && Arrays.deepEquals(layout, that.layout);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(gameType);
        result = 31 * result + Arrays.deepHashCode(layout);
        return result;
    }
}
