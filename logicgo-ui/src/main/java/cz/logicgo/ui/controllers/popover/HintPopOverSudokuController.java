package cz.logicgo.ui.controllers.popover;

import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.misc.enums.hints.SudokuHintType;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

import static cz.logicgo.core.misc.Messages.getFormatted;


public class HintPopOverSudokuController {

    @FXML
    private HBox hintButtonBox;

    @FXML
    private HBox helpButtonBox;

    private Consumer<SudokuHintType> onSelect;

    private MainScreenController mainScreenController;

    public void initialize(MainScreenController mainScreenController, SudokuVariant sudokuVariant) {
        this.mainScreenController = mainScreenController;

        Button rowHint = createButton(SudokuHintType.CHECK_ROW, "tooltip.hint_type.check_row");
        Button colHint = createButton(SudokuHintType.CHECK_COLUMN, "tooltip.hint_type.check_column");
        Button checkCellHint = createButton(SudokuHintType.CHECK_CELL, "tooltip.hint_type.check_cell");
        Button checkBoxHint = createButton(SudokuHintType.CHECK_REGION, "tooltip.hint_type.check_box");
        Button checkAffectedCells = createButton(SudokuHintType.CHECK_AFFECTED_CELLS, "tooltip.hint_type.check_affected_cells");
        Button checkValid = createButton(SudokuHintType.CHECK_VALIDITY, "tooltip.hint_type.check_validity");
        Button grade = createButton(SudokuHintType.NEXT_LOGICAL_STEP, "tooltip.hint_type.next_logical_step");

        if (MultiGridConfig.isMultiDoku(sudokuVariant)) {
            Button one = createButton(SudokuHintType.CHECK_ONE_BOARD, "tooltip.hint_type.check_one_board");
            hintButtonBox.getChildren().add(one);
        }

        if (hasAnyActiveCustomModifier(sudokuVariant)) {
            Button activeModifiers = createButton(SudokuHintType.CHECK_MODIFIER_CELLS, "tooltip.hint_type.check_active_modifiers");
            hintButtonBox.getChildren().add(activeModifiers);
        }

        hintButtonBox.getChildren().addAll(rowHint, colHint, checkBoxHint, checkAffectedCells, checkCellHint, checkValid, grade);


        Button randomCellHelp = createButton(SudokuHintType.RANDOM_CELL, "tooltip.hint_type.random_cell");
        Button chosenCellHelp = createButton(SudokuHintType.CHOSEN_CELL, "tooltip.hint_type.chosen_cell");

        helpButtonBox.getChildren().addAll(randomCellHelp, chosenCellHelp);
    }

    public boolean hasAnyActiveCustomModifier(SudokuVariant sudokuVariant) {
        switch (sudokuVariant) {
            case BETWEEN, KILLER, XV, CONSECUTIVE, VUDOKU -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private Button createButton(SudokuHintType type, String key) {
        Button button = new Button(type.getTranslation());
        button.setOnAction(e -> {
            if (onSelect != null) onSelect.accept(type);
        });
        addTooltip(button, key);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-padding: 6 12; -fx-background-radius: 8;");
        return button;
    }

    private void addTooltip(Node node, String key) {
        node.setOnMouseEntered(_ -> mainScreenController.setTextToLabel(getFormatted(key)));
        node.setOnMouseExited(_ -> mainScreenController.unsetTextToLabel());
    }

    public void setOnSelect(Consumer<SudokuHintType> onSelect) {
        this.onSelect = onSelect;
    }
}
