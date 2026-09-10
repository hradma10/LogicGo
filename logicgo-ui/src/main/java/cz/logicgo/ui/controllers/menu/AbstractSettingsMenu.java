package cz.logicgo.ui.controllers.menu;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.gameClasses.favorites.GameFavorite;
import cz.logicgo.core.util.GameInitFavoriteConverter;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.persistence.services.UserSettingsService;
import cz.logicgo.ui.controllers.ControllerClosable;
import cz.logicgo.ui.controllers.gameControllers.FavoriteEventManager;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.misc.TabState;
import cz.logicgo.ui.misc.tabChoosingClasses.TabChoiceWrapper;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.controlsfx.control.MaskerPane;

import java.util.concurrent.atomic.AtomicReference;

import static cz.logicgo.core.misc.Messages.getFormatted;

public abstract class AbstractSettingsMenu<T> implements ControllerClosable {

    @FXML
    public Button okButton;
    @FXML
    public Button cancelButton;
    public Button favoriteButton;
    @FXML
    public Pane pane;
    @FXML
    public GridPane gridPane;
    @FXML
    public Label statusLabel;

    protected User user;
    protected MainScreenController mainController;
    private TabState state;

    protected boolean isInitializing = true;
    protected final UserSettingsService userSettingsService = new UserSettingsService();
    private Task<T> creationTask;
    final AtomicReference<Task<T>> backgroundCreationTask = new AtomicReference<>();

    private final PauseTransition debounceTimer = new PauseTransition(javafx.util.Duration.millis(300));
    private final UserService userService = new UserService();

    protected Long customSeed = null;
    protected Long specialSeed = null;
    protected boolean isChangingConfigDirectly = false;

    protected abstract void showRealPreview(T game);

    protected abstract T createGameInBackground() throws Exception;

    protected abstract boolean isConfigValid();

    protected abstract void onGameGenerated(T game);

    protected abstract void buildSpecificLayout();

    abstract void updateCanvasPreview();

    protected abstract void updateSeedField();

    public abstract GameInit createConfig();

    public abstract void initialize(TabState state, User user, MainScreenController mainController, TabChoiceWrapper gameSettings);

    public void initialize(TabState state, User user, MainScreenController mainController) {
        this.user = user;
        this.state = state;
        this.mainController = mainController;

        if (okButton != null) {
            okButton.setText(getFormatted("playButton"));
            okButton.setOnAction(_ -> handleOk());
        }
        if (cancelButton != null) {
            cancelButton.setText(getFormatted("cancelButton"));
            cancelButton.setOnAction(_ -> handleCancel());
        }
    }

    protected void settingsChanged() {
        if (isInitializing) return;

        if (isChangingConfigDirectly) {
            this.customSeed = null;
        }

        updateSeedField();
        updateCanvasPreview();
        checkAndUpdateFavoriteState();
        startBackgroundGeneration();
    }

    protected GameFavorite createFavoriteFromCurrentSettings() {
        if (!isConfigValid()) return null;
        GameInit config = createConfig();
        return GameInitFavoriteConverter.toFavorite(config);
    }

    protected void checkAndUpdateFavoriteState() {
        if (favoriteButton == null || user == null) return;

        GameFavorite currentFav = createFavoriteFromCurrentSettings();

        if (currentFav == null) {
            favoriteButton.setDisable(true);
            return;
        }

        favoriteButton.setDisable(false);
        boolean isFavorite = user.getFavoriteComb().contains(currentFav);

        if (isFavorite) {
            favoriteButton.setText(getFormatted("button.unfavorite"));
        } else {
            favoriteButton.setText(getFormatted("button.favorite"));
        }

        FavoriteEventManager.notifyFavoritesChanged();

        favoriteButton.setOnAction(_ -> {
            if (user.getFavoriteComb().contains(currentFav)) {
                user.getFavoriteComb().remove(currentFav);
            } else {
                user.getFavoriteComb().add(currentFav);
            }
            userService.updateUser(user);
            checkAndUpdateFavoriteState();
        });
    }

    protected void addGridRow(int rowIndex, Node label, Node control) {
        gridPane.add(label, 0, rowIndex);
        gridPane.add(control, 1, rowIndex);
    }

    protected void startBackgroundGeneration() {
        if (!isConfigValid()) return;

        debounceTimer.setOnFinished(event -> {
            Task<T> oldTask = backgroundCreationTask.getAndSet(null);
            if (oldTask != null && oldTask.isRunning()) {
                oldTask.cancel(true);
            }

            if (statusLabel != null) {
                statusLabel.setText(getFormatted("gameSettings.generating.in_progress"));
            }

            Task<T> newTask = new Task<>() {
                @Override
                protected T call() throws Exception {
                    if (isCancelled()) return null;
                    return createGameInBackground();
                }
            };

            newTask.setOnSucceeded(e -> {
                if (!newTask.isCancelled() && newTask.getValue() != null) {
                    showRealPreview(newTask.getValue());
                    if (statusLabel != null) {
                        statusLabel.setText(getFormatted("gameSettings.generating.done"));
                    }
                }
            });

            newTask.setOnFailed(e -> {
                if (statusLabel != null) {
                    statusLabel.setText(getFormatted("gameSettings.generating.error"));
                }
            });

            backgroundCreationTask.set(newTask);
            Thread bgThread = new Thread(newTask);
            bgThread.setDaemon(true);
            bgThread.start();
        });

        debounceTimer.playFromStart();
    }

    public void handleOk() {
        if (!isConfigValid()) {
            resetButtons();
            return;
        }
        MaskerPane masker = new MaskerPane();
        masker.setText("Generuji herní plán...");
        Platform.runLater(() -> {
            okButton.setGraphic(new ProgressIndicator());
            okButton.setText("Generuji...");
            okButton.setDisable(true);
            cancelButton.setText(getFormatted("cancelButton"));
            if (pane.getChildren().stream().noneMatch(node -> node instanceof MaskerPane)) {
                masker.prefWidthProperty().bind(pane.widthProperty());
                masker.prefHeightProperty().bind(pane.heightProperty());
                pane.getChildren().add(masker);
            }
        });

        Task<T> currentBgTask = backgroundCreationTask.get();

        if (currentBgTask != null && currentBgTask.isDone() && !currentBgTask.isCancelled()) {
            try {
                onGameGenerated(currentBgTask.get());
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(this::resetButtons);
            }
        } else {
            if (currentBgTask != null) {
                currentBgTask.cancel(true);
            }

            if (!isConfigValid()) {
                resetButtons();
                return;
            }

            creationTask = new Task<>() {
                @Override
                protected T call() throws Exception {
                    if (isCancelled()) return null;
                    return createGameInBackground();
                }
            };

            creationTask.setOnSucceeded(e -> onGameGenerated(creationTask.getValue()));
            creationTask.setOnFailed(e -> {
                creationTask.getException().printStackTrace();
                resetButtons();
            });
            creationTask.setOnCancelled(e -> resetButtons());

            Thread mainThread = new Thread(creationTask);
            mainThread.setDaemon(true);
            mainThread.start();
        }
    }

    public void handleCancel() {
        boolean isRunning = (creationTask != null && creationTask.isRunning()) ||
                (backgroundCreationTask.get() != null && backgroundCreationTask.get().isRunning());

        if (isRunning) {
            terminateAllActiveActions();
            resetButtons();
            if (statusLabel != null) {
                statusLabel.setText(getFormatted("gameSettings.cancelled_user"));
            }
        } else {
            mainController.closeTabWithId(state.getId());
        }
    }

    @Override
    public void terminateAllActiveActions() {
        Task<T> bgTask = backgroundCreationTask.getAndSet(null);
        if (bgTask != null && bgTask.isRunning()) {
            bgTask.cancel(true);
        }

        if (creationTask != null && creationTask.isRunning()) {
            creationTask.cancel(true);
        }
    }

    protected void resetButtons() {
        pane.getChildren().removeIf(node -> node instanceof MaskerPane);
        Platform.runLater(() -> {
            okButton.setGraphic(null);
            okButton.setText(getFormatted("playButton"));
            okButton.setDisable(false);
            cancelButton.setText(getFormatted("cancelButton"));
        });
    }

    public TabState getState() {
        return state;
    }

    public AbstractSettingsMenu<T> setState(TabState state) {
        this.state = state;
        return this;
    }
}
