package cz.logicgo.core.misc.enums.hints;


import cz.logicgo.core.misc.interfaces.Translatable;

public enum SudokuHintType implements Translatable {
    RANDOM_CELL("sudoku.hint_type.random_cell"),
    CHOSEN_CELL("sudoku.hint_type.chosen_cell"),

    CHECK_ROW("sudoku.hint_type.check_row"),
    CHECK_COLUMN("sudoku.hint_type.check_column"),
    CHECK_CELL("sudoku.hint_type.check_cell"),
    CHECK_REGION("sudoku.hint_type.check_box"),
    CHECK_VALIDITY("sudoku.hint_type.check_validity"),
    CHECK_AFFECTED_CELLS("sudoku.hint_type.check_affected_cells"),
    CHECK_ONE_BOARD("sudoku.hint_type.check_one_board"),
    CHECK_MODIFIER_CELLS("sudoku.hint_type.check_modifier_cells"),
    NEXT_LOGICAL_STEP("sudoku.hint_type.next_logical_step");

    final String name;

    SudokuHintType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
