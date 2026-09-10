package cz.logicgo.core.misc.enums.gameTypes.sudoku;

public enum RossiniType {
    ASCENDING(0), DESCENDING(1), NONE(2);

    private final int value;

    RossiniType(int value) {
        this.value = value;
    }

    public static RossiniType fromValue(int value) {
        return switch (value) {
            case 0 -> ASCENDING;
            case 1 -> DESCENDING;
            case 2 -> NONE;
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }

    public int getValue() {
        return value;
    }
}
