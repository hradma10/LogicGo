package cz.logicgo.engine.algorithms.shikaku.solver;

import java.util.ArrayList;
import java.util.List;

public class Clue {
    final int row, col, value;
    final List<Rect> candidates = new ArrayList<>(16);

    public Clue(int row, int col, int value) {
        this.row = row;
        this.col = col;
        this.value = value;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getValue() {
        return value;
    }

    public List<Rect> getCandidates() {
        return candidates;
    }
}
