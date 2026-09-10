package cz.logicgo.ui.controllers.exportControllers.exportTabs;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.export.gameTypes.*;
import cz.logicgo.core.gameClasses.favorites.*;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController;
import cz.logicgo.ui.controllers.gameControllers.FavoriteEventManager;
import cz.logicgo.core.misc.formatter.FavoriteFormatter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController.*;
import static cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController.MAX_GENERATED;
import static cz.logicgo.ui.controllers.helpers.ElementsSetterHelpers.setUpCountGamesTextField;


public class FavoriteTabController {
    @FXML
    private ChoiceBox<GameFavorite> favoriteChoiceBox;
    @FXML
    private TextField countGames;

    @FXML
    private Label multiChoiceLabel;
    @FXML
    private Label multiCountGamesLabel;
    private ExportMultipleGamesController controller;
    private User user;

    public GameMode getCurrentConfig() {
        GameFavorite selected = favoriteChoiceBox.getValue();
        if (selected == null) return null;
        GameMode mode = convertToMode(selected, 1);
        if (mode instanceof SudokuTypes s) return new SudokuTypes(Difficulty.EASY, 1, s.sudokuSize(), s.sudokuVariant(), s.regionLayout(), s.patternLayout());
        if (mode instanceof MazeTypes m) return new MazeTypes(Difficulty.EASY, 1, m.mazeType(), m.mazeShape(), m.mazeAlgorithm(), m.width(), m.height(), m.mask(), m.typeCounts());
        if (mode instanceof BridgeTypes b) return new BridgeTypes(Difficulty.EASY, 1, b.bridgeType(), b.width(), b.height(), b.multipleCount());
        if (mode instanceof ShikakuTypes sh) return new ShikakuTypes(Difficulty.EASY, 1, sh.shikakuType(), sh.width(), sh.height());
        return mode;
    }

    public void initialize(Canvas canvas, User user, ExportMultipleGamesController controller) {
        this.user = user;
        this.controller = controller;

        multiChoiceLabel.setText(getFormatted("export.multiTab.favorites"));
        multiCountGamesLabel.setText(getFormatted("export.tab.countGames"));

        setUpCountGamesTextField(countGames, MAX_GENERATED);
        if (addButton != null) {
            String btnText = getFormatted("export.tab.addToExport") != null ? getFormatted("export.tab.addToExport") : "Přidat do exportu";
            addButton.setText(btnText);
        }
        favoriteChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(GameFavorite favorite) {
                return favorite != null ? FavoriteFormatter.format(favorite) : "";
            }
            @Override
            public GameFavorite fromString(String string) {
                return null;
            }
        });

        favoriteChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (controller != null) controller.updatePreview();
        });

        refreshChoiceBox();

        FavoriteEventManager.addChangeListener((obs, oldVal, newVal) -> {
            Platform.runLater(this::refreshChoiceBox);
        });
    }

    private void refreshChoiceBox() {
        if (user != null && user.getFavoriteComb() != null) {
            GameFavorite currentlySelected = favoriteChoiceBox.getValue();
            favoriteChoiceBox.getItems().setAll(user.getFavoriteComb());
            if (currentlySelected != null && favoriteChoiceBox.getItems().contains(currentlySelected)) {
                favoriteChoiceBox.getSelectionModel().select(currentlySelected);
            } else if (!favoriteChoiceBox.getItems().isEmpty()) {
                favoriteChoiceBox.getSelectionModel().selectFirst();
            }
        }
    }

    @FXML
    public Button addButton;

    @FXML
    private void handleAddToCart() {
        GameFavorite selected = favoriteChoiceBox.getValue();
        if (selected == null || controller == null) return;

        int count = Integer.parseInt(countGames.getText());
        ExportItem item = new ExportItem(
                FavoriteFormatter.format(selected),
                convertToMode(selected, count)
        );

        controller.addToList(item);
    }

    public static GameMode convertToMode(GameFavorite favorite, int count) {
        return switch (favorite) {
            case SudokuFavorite s -> new SudokuTypes(
                    s.getDifficulty(), count, s.getSize(), s.getVariant(), s.getRegionLayout(), s.getPatternLayout()
            );
            case MazeFavorite m -> new MazeTypes(
                    m.getDifficulty(), count, m.getMazeType(), m.getMazeShape(), m.getMazeAlgorithm(),
                    m.getWidth(), m.getHeight(), m.getMask(), m.getTypeCount()
            );
            case BridgeFavorite b -> new BridgeTypes(
                    b.getDifficulty(), count, b.getBridgeType(), b.getWidth(), b.getHeight(), b.getMultipleCount()
            );
            case ShikakuFavorite s -> new ShikakuTypes(
                    s.getDifficulty(), count, s.getShikakuType(), s.getWidth(), s.getHeight()
            );
            case null -> null;
        };
    }
}
