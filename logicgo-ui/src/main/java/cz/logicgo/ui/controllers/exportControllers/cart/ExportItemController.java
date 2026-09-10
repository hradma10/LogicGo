package cz.logicgo.ui.controllers.exportControllers.cart;

import cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController;
import cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController.ExportItem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ExportItemController {
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label detailsLabel;
    @FXML
    private Button removeButton;

    private ExportItem currentItem;
    private ExportMultipleGamesController mainController;

    public void initialize(ExportMultipleGamesController mainController) {
        this.mainController = mainController;
    }

    public void setData(ExportItem item) {
        this.currentItem = item;
        descriptionLabel.setText(item.description());
        detailsLabel.setText(String.format("%dx, %s", item.gameConfig().getCount(), item.gameConfig().getDifficulty().getTranslation()));

        removeButton.setOnAction(event -> {
            if (mainController != null) {
                mainController.removeFromList(item);
            }
        });
    }
}
