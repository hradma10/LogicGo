package cz.logicgo.ui.controllers.exportControllers.cart;

import cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.IOException;

import static cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController.*;

public class CartItemCell extends ListCell<ExportItem> {
    private HBox root;
    private ExportItemController controller;

    public CartItemCell(ExportMultipleGamesController mainController) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cz/logicgo/ui/windows/exportMultiple/export_item.fxml"));
            root = loader.load();
            controller = loader.getController();
            controller.initialize(mainController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void updateItem(ExportItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            controller.setData(item);
            setGraphic(root);
        }
    }
}
