package cz.logicgo.ui.controllers.gameControllers;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;

public class FavoriteEventManager {

    private static final IntegerProperty updates = new SimpleIntegerProperty(0);

    public static void notifyFavoritesChanged() {
        updates.set(updates.get() + 1);
    }

    public static void addChangeListener(ChangeListener<Number> listener) {
        updates.addListener(listener);
    }
}
