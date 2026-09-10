package cz.logicgo.ui.controllers.listControllers;

import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.bridge.BridgeInit;
import cz.logicgo.core.factoryInit.maze.MazeInit;
import cz.logicgo.core.factoryInit.shikaku.ShikakuInit;
import cz.logicgo.core.factoryInit.sudoku.SudokuInit;
import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.core.misc.enums.TabType;
import cz.logicgo.persistence.services.GameService;
import cz.logicgo.ui.controllers.listControllers.history.GameHistoryEntry;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.factories.BridgeGameFactory;
import cz.logicgo.ui.factories.MazeGameFactory;
import cz.logicgo.ui.factories.ShikakuGameFactory;
import cz.logicgo.ui.factories.SudokuGameFactory;
import cz.logicgo.ui.misc.tabChoosingClasses.LoadedGameWrapper;
import cz.logicgo.ui.misc.windows.AlertBox;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.Objects;

import static cz.logicgo.core.GameUtils.createNewUnsolvedGameFromSolved;
import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.core.misc.enums.TypeGame.*;


public class GameItemController {

    GameService gameService = new GameService();
    @FXML
    public Button viewButton;
    @FXML
    public Button deleteButton;
    public HBox list_item;
    @FXML
    private ImageView thumbnailView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label infoLabel;
    private MainScreenController mainScreenController;
    private ItemsViewController itemsViewController;
    User user;


    public void initialize(User user, MainScreenController mainScreenController, ItemsViewController itemsViewController) {
        this.mainScreenController = mainScreenController;
        this.itemsViewController = itemsViewController;
        this.user = user;
    }

    public void setData(GameHistoryEntry entry) {
        thumbnailView.setImage(entry.thumbnail());
        titleLabel.setText(entry.title());
        infoLabel.setText(getFormatted("history.label.info", entry.difficulty().getTranslation(), entry.getPlayedAt()));

        setupThumbnailPreview(entry.thumbnail());

        if (entry.status() == Status.FINISHED) {
            viewButton.setText(getFormatted("history.button.playAgain"));
        } else {
            viewButton.setText(getFormatted("history.button.continue"));
        }
        deleteButton.setText(getFormatted("history.button.delete"));

        viewButton.setOnAction(e -> {
            var states = mainScreenController.getActiveTabStates();
            for (var state : states) {
                if (state.getIdOfGame() != null && Objects.equals(state.getIdOfGame(), entry.id())) {
                    mainScreenController.switchToTab(state.getAssociatedTab());
                    return;
                }
            }
            try {
                switch (entry.typeGame()) {
                    case SUDOKU -> {
                        var init = new SudokuInit().setId(entry.id());
                        var game = SudokuGameFactory.createGame(init);

                        if (entry.status() == Status.FINISHED) {
                            game = (Sudoku) createNewUnsolvedGameFromSolved(game);
                        }

                        mainScreenController.openTab(TabType.SUDOKU, new LoadedGameWrapper(game));

                    }
                    case BRIDGE -> {
                        var init = new BridgeInit().setId(entry.id());
                        var game = BridgeGameFactory.createGame(init);

                        if (entry.status() == Status.FINISHED) {
                            game = (Bridge) createNewUnsolvedGameFromSolved(game);
                        }

                        mainScreenController.openTab(TabType.BRIDGE, new LoadedGameWrapper(game));

                    }
                    case MAZE -> {
                        var init = new MazeInit().setId(entry.id());
                        var game = MazeGameFactory.createGame(init);

                        if (entry.status() == Status.FINISHED) {
                            game = (Maze) createNewUnsolvedGameFromSolved(game);
                        }

                        mainScreenController.openTab(TabType.MAZE, new LoadedGameWrapper(game));
                    }
                    case SHIKAKU -> {
                        var init = new ShikakuInit().setId(entry.id());
                        var game = ShikakuGameFactory.createGame(init);

                        if (entry.status() == Status.FINISHED) {
                            game = (Shikaku) createNewUnsolvedGameFromSolved(game);
                        }

                        mainScreenController.openTab(TabType.SHIKAKU, new LoadedGameWrapper(game));
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + entry.typeGame());
                }
            } catch (Exception ex) {

                throw new RuntimeException(ex.getMessage());
            }

            itemsViewController.refresh();
        });

        deleteButton.setOnAction(_ -> {
            if (AlertBox.deleteGames()) {
                gameService.deleteById(entry.id());
                itemsViewController.refresh();
            }
        });
    }

    private void setupThumbnailPreview(javafx.scene.image.Image gameImage) {
        if (gameImage == null) return;

        ImageView largePreview = new ImageView(gameImage);
        largePreview.setFitWidth(300);
        largePreview.setPreserveRatio(true);

        Tooltip previewTooltip = new Tooltip();
        previewTooltip.setGraphic(largePreview);
        previewTooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        previewTooltip.setShowDelay(Duration.millis(400));
        previewTooltip.setHideDelay(Duration.millis(200));

        Tooltip.install(thumbnailView, previewTooltip);

    }
}
