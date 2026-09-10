package cz.logicgo.core.misc.enums.gameTypes.sudoku;

public enum ParityType {
    NONE(0),
    ODD(1),
    EVEN(2);

    private final int value;

    ParityType(int value) {
        this.value = value;
    }

    public static ParityType fromValue(int value) {
        for (ParityType pc : values()) {
            if (pc.value == value) {
                return pc;
            }
        }
        return NONE;
    }

    public static ParityType fromNumber(int number) {
        if (number < 1 || number > 16) {
            return NONE;
        }
        return (number % 2 == 0) ? EVEN : ODD;
    }

    public int getValue() {
        return value;
    }

    public boolean matches(int number) {
        if (this == NONE) return true;
        if (this == ODD) return number % 2 != 0;
        if (this == EVEN) return number % 2 == 0;
        return false;
    }
}
