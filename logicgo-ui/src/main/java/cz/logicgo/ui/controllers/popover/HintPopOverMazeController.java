package cz.logicgo.ui.controllers.popover;

import cz.logicgo.core.misc.enums.hints.MazeHintType;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

import static cz.logicgo.core.misc.Messages.getFormatted;


public class HintPopOverMazeController {

    @FXML
    private HBox hintButtonBox;

    private Consumer<MazeHintType> onSelect;

    private MainScreenController mainScreenController;

    public void initialize(MainScreenController mainScreenController) {
        this.mainScreenController = mainScreenController;

        Button wholePath = createButton(MazeHintType.SHOW_ALL_OF_PATH, "tooltip.hint_type.all_of_path");
        Button partPath = createButton(MazeHintType.SHOW_LITTLE_OF_PATH, "tooltip.hint_type.little_of_path");

        hintButtonBox.getChildren().addAll(wholePath, partPath);
    }

    private Button createButton(MazeHintType type, String key) {
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

    public void setOnSelect(Consumer<MazeHintType> onSelect) {
        this.onSelect = onSelect;
    }
}
