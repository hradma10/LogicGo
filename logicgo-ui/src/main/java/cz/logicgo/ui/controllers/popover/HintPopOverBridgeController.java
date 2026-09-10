package cz.logicgo.ui.controllers.popover;

import cz.logicgo.core.misc.enums.hints.BridgeHintType;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

import static cz.logicgo.core.misc.Messages.getFormatted;


public class HintPopOverBridgeController {

    @FXML
    private HBox hintButtonBox;

    @FXML
    private HBox helpButtonBox;

    private Consumer<BridgeHintType> onSelect;

    private MainScreenController mainScreenController;

    public void initialize(MainScreenController mainScreenController) {
        this.mainScreenController = mainScreenController;

        Button checkIsland = createButton(BridgeHintType.CHECK_ISLAND, "tooltip.hint_type.check_island");
        Button checkCount = createButton(BridgeHintType.CHECK_COUNT, "tooltip.hint_type.check_count");
        Button checkBridge = createButton(BridgeHintType.CHECK_BRIDGE, "tooltip.hint_type.check_bridge");

        hintButtonBox.getChildren().addAll(checkIsland, checkCount, checkBridge);

        Button randomCellHelp = createButton(BridgeHintType.RANDOM_BRIDGE, "tooltip.hint_type.random_bridge");
        Button chosenCellHelp = createButton(BridgeHintType.CHOSEN_PAIR, "tooltip.hint_type.chosen_pair");

        helpButtonBox.getChildren().addAll(randomCellHelp, chosenCellHelp);
    }

    private Button createButton(BridgeHintType type, String key) {
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

    public void setOnSelect(Consumer<BridgeHintType> onSelect) {
        this.onSelect = onSelect;
    }
}
