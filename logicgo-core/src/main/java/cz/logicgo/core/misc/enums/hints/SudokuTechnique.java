package cz.logicgo.core.misc.enums.hints;

import cz.logicgo.core.misc.interfaces.Translatable;

public enum SudokuTechnique implements Translatable {
    NAKED_SINGLE("sudoku.technique.naked_single"),
    HIDDEN_SINGLE("sudoku.technique.hidden_single"),
    VARIANT_BASIC("sudoku.technique.variant_basic"),
    NAKED_PAIR("sudoku.technique.naked_pair"),
    POINTING_PAIR("sudoku.technique.pointing_pair"),
    BOX_LINE_REDUCTION("sudoku.technique.box_line_reduction"),
    NAKED_TRIPLE("sudoku.technique.naked_triple"),
    HIDDEN_PAIR("sudoku.technique.hidden_pair"),
    HIDDEN_TRIPLE("sudoku.technique.hidden_triple"),
    X_WING("sudoku.technique.x_wing"),
    SWORDFISH("sudoku.technique.swordfish"),
    XY_WING("sudoku.technique.xy_wing"),
    AVOIDABLE_RECTANGLE("sudoku.technique.avoidable_rectangle");

    private final String name;

    SudokuTechnique(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
