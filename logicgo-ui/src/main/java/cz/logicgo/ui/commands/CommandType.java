package cz.logicgo.ui.commands;

import java.util.HashMap;
import java.util.Map;

public enum CommandType {

    SET_SUDOKU(0), REPLACE_SUDOKU(1),
    ADD_BRIDGE(2), REMOVE_BRIDGE(3), CHANGE_BRIDGE_COUNT(4), REPLACE_BRIDGES(5),
    REMOVE_SNAPSHOT(6),
    MOVE_MAZE(7), MOVE_LEVEL(8), TOGGLE_CANDIDATE_SUDOKU(9), MULTIPLE_CANDIDATE_CHANGE_SUDOKU(10), MARK_MAZE(11), REPLACE_MAZE(12),
    ADD_RECTANGLE(13), REMOVE_RECTANGLE(14), REPLACE_SHIKAKU(15), REPLACE_RECTANGLES_SHIKAKU(16);

    private static final Map<Byte, CommandType> BY_TYPE = new HashMap<>();

    static {
        for (CommandType ct : values()) {
            BY_TYPE.put(ct.type, ct);
        }
    }

    final private byte type;

    CommandType(byte type) {
        this.type = type;
    }

    CommandType(int type) {
        this.type = (byte) type;
    }

    public static CommandType fromByte(byte type) {
        return BY_TYPE.get(type);
    }

    public byte getType() {
        return type;
    }
}
