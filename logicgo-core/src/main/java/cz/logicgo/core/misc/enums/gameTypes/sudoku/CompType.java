package cz.logicgo.core.misc.enums.gameTypes.sudoku;

public enum CompType {
    BIGGER(0),
    SMALLER(1);

    private final int value;

    CompType(int value) {
        this.value = value;
    }

    public static CompType fromValue(int value) {
        return value == 0 ? BIGGER : SMALLER;
    }

    public int getValue() {
        return value;
    }
}
