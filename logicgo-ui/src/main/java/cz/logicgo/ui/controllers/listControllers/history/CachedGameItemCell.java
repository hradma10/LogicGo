package cz.logicgo.ui.controllers.listControllers.history;


import cz.logicgo.core.entity.user.User;
import cz.logicgo.ui.controllers.listControllers.GameItemController;
import cz.logicgo.ui.controllers.listControllers.ItemsViewController;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class CachedGameItemCell extends ListCell<GameHistoryEntry> {
    private HBox root;
    private GameItemController controller;
    private ItemsViewController itemsViewController;

    public CachedGameItemCell(User user, MainScreenController mainScreenController, ItemsViewController itemsViewController) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cz/logicgo/ui/windows/list/game_history_item.fxml"));
            root = loader.load();
            controller = loader.getController();
            controller.initialize(user, mainScreenController, itemsViewController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void updateItem(GameHistoryEntry item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            controller.setData(item);
            setGraphic(root);
        }
    }

    public ItemsViewController getItemsViewController() {
        return itemsViewController;
    }

    public CachedGameItemCell setItemsViewController(ItemsViewController itemsViewController) {
        this.itemsViewController = itemsViewController;
        return this;
    }
}
