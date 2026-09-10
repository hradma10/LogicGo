package cz.logicgo.ui.controllers.listControllers;

import cz.logicgo.core.entity.export.ExportDetail;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class ExportedItemsCell extends ListCell<ExportDetail> {
    private HBox root;
    private ExportedItemController controller;

    public ExportedItemsCell(User user, MainScreenController mainScreenController, ExportMultipleGamesController parentController) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cz/logicgo/ui/windows/exportMultiple/exported_history_item.fxml"));
            root = loader.load();
            controller = loader.getController();
            controller.initialize(user, mainScreenController, parentController);
        } catch (IOException e) {

        }
    }

    @Override
    protected void updateItem(ExportDetail item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            controller.setData(item);
            setGraphic(root);
        }
    }
}
