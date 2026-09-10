package cz.logicgo.core.misc.enums.gameTypes.sudoku;

public enum ConsecutiveType {
    CONSECUTIVE(0),
    NON_CONSECUTIVE(1);

    private final int value;

    ConsecutiveType(int value) {
        this.value = value;
    }

    public static ConsecutiveType fromValue(int value) {
        return value == 0 ? CONSECUTIVE : NON_CONSECUTIVE;
    }

    public int getValue() {
        return value;
    }
}
