package cz.logicgo.ui.controllers.popover;

import cz.logicgo.core.misc.enums.hints.ShikakuHintType;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

import static cz.logicgo.core.misc.Messages.getFormatted;


public class HintPopOverShikakuController {

    @FXML
    private HBox hintButtonBox;

    private Consumer<ShikakuHintType> onSelect;

    private MainScreenController mainScreenController;

    public void initialize(MainScreenController mainScreenController) {
        this.mainScreenController = mainScreenController;

        Button checkIsland = createButton(ShikakuHintType.CHECK_VALIDITY, "tooltip.hint_type.check_island");
        Button checkCount = createButton(ShikakuHintType.INSERT_RANDOM_RECTANGLE, "tooltip.hint_type.check_count");
        Button checkBridge = createButton(ShikakuHintType.INSERT_RECTANGLE_TO_NUMBER, "tooltip.hint_type.check_bridge");

        hintButtonBox.getChildren().addAll(checkIsland, checkCount, checkBridge);
    }

    private Button createButton(ShikakuHintType type, String key) {
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

    public void setOnSelect(Consumer<ShikakuHintType> onSelect) {
        this.onSelect = onSelect;
    }
}
