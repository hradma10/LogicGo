package cz.logicgo.ui.controllers.gameControllers;

import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.misc.enums.settings.*;
import cz.logicgo.engine.util.generate.SeedCreator;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.ui.commands.CommandExecutor;
import cz.logicgo.ui.controllers.ControllerClosable;
import cz.logicgo.ui.controllers.GameAssociatedOpeners;
import cz.logicgo.ui.controllers.gameControllers.howToPlay.HelpWindowManager;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.misc.TabState;
import cz.logicgo.ui.misc.windows.AlertBox;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static cz.logicgo.core.GameUtils.getGameSettingsAsMap;
import static cz.logicgo.core.GameUtils.getTypedSetting;
import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.misc.SvgLoader.setIcon;


public abstract class AbstractGameController<T extends Game> implements Initializable, ControllerClosable {

    @FXML
    public ToolBar topToolbar;
    @FXML
    public Button buttonPause;
    @FXML
    public Canvas primaryCanvas;
    @FXML
    public Canvas secondaryCanvas;
    @FXML
    public Label countdownTimer;
    @FXML
    public Pane gamePane;

    public Button buttonHint;
    public Button buttonShare;
    public Button buttonUndo;
    public Button buttonRedo;
    public Button buttonHelp;
    public Button buttonSolution;
    public Button buttonExport;
    public Button buttonRestart;
    public Button buttonExit;

    protected T gameInstance;
    protected MainScreenController mainScreenController;
    protected TabState tabState;
    protected Stage stage;
    protected CommandExecutor commandExecutor = new CommandExecutor();
    protected UserService userService = new UserService();

    protected long startTime;
    protected Duration elapsedTime;
    protected AnimationTimer elapsedTimeTimer;
    protected boolean timerIsRunning = false;

    private PauseTransition hintHideTimer;
    protected AnimationTimer hoverAnimationTimer;

    public void startHintFeedbackTimer(double seconds, Runnable onTimeout) {
        if (hintHideTimer != null) {
            hintHideTimer.stop();
        }
        hintHideTimer = new PauseTransition(javafx.util.Duration.seconds(seconds));
        hintHideTimer.setOnFinished(_ -> onTimeout.run());
        hintHideTimer.play();
    }

    private Timeline autoSaveTimeline;

    protected void startAutoSaveTimer() {
        if (autoSaveTimeline != null) {
            autoSaveTimeline.stop();
        }

        autoSaveTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(30), _ -> {
            if (tabState != null && tabState.isChangePending() && gameInstance != null) {
                saveGame(false);
            }
        }));
        autoSaveTimeline.setCycleCount(Timeline.INDEFINITE);
        autoSaveTimeline.play();
    }

    public void stopAutoSaveTimer() {
        if (autoSaveTimeline != null) {
            autoSaveTimeline.stop();
        }
    }

    public void stopHintFeedbackTimer() {
        if (hintHideTimer != null) {
            hintHideTimer.stop();
        }
    }

    public AbstractGameController<T> setStage(Stage stage) {
        this.stage = stage;
        return this;
    }

    public Stage getStage() {
        return stage;
    }

    protected abstract void handleHoverPreview(double timeSeconds);

    protected abstract void showPopOver();

    protected void startHoverAnimation() {
        if (hoverAnimationTimer != null) {
            hoverAnimationTimer.stop();
        }
        hoverAnimationTimer = new AnimationTimer() {
            long lastTime = 0;
            double timeSeconds = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double delta = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                timeSeconds += delta;

                handleHoverPreview(timeSeconds);
            }
        };
        hoverAnimationTimer.start();
    }

    public T getGameInstance() {
        return gameInstance;
    }

    public static String getFormattedTime(long totalSeconds) {
        String text;

        if (totalSeconds > 86400) {
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;

            text = String.format("%02d:%02d", minutes, seconds);
        } else {
            long hours = totalSeconds / 3600;
            long minutes = totalSeconds % 3600 / 60;
            long seconds = totalSeconds % 3600 % 60;

            text = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return text;
    }

    private SettingKey getHintsOn(Game game) {
        return switch (game.getGameType()) {
            case SUDOKU -> SudokuSettings.HINTS_ON;
            case MAZE -> MazeSettings.HINTS_ON;
            case BRIDGE -> BridgeSettings.HINTS_ON;
            case SHIKAKU -> ShikakuSettings.HINTS_ON;
        };
    }

    protected void buildToolbar(Game game) {
        Map<SettingKey, GameSetting> settings = getGameSettingsAsMap(game.getSettings());

        boolean hintsOn = getTypedSetting(settings, getHintsOn(game));

        buttonUndo = createButton(getFormatted("game.toolbar.undo"), "btn-secondary", _ -> onUndo());
        buttonRedo = createButton(getFormatted("game.toolbar.redo"), "btn-secondary", _ -> onRedo());
        buttonHelp = createButton(getFormatted("game.toolbar.help"), "btn-secondary", _ -> openHelp());

        if (hintsOn) {
            buttonHint = createButton(getFormatted("game.toolbar.hint"), "btn-secondary", _ -> openHint());
        }

        buttonSolution = createButton(getFormatted("game.toolbar.solution"), "btn-secondary", _ -> {
            try {
                openSolution();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        buttonExport = createButton(getFormatted("game.toolbar.export"), "btn-secondary", _ -> {
            try {
                openExport();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        buttonShare = createButton(getFormatted("game.seed.copy"), "btn-secondary", _ -> copySeed(game, buttonShare));
        buttonShare.getStyleClass().add("icon-only");

        buttonRestart = createButton(getFormatted("game.toolbar.restart"), "btn-secondary", _ -> onRestart());
        buttonExit = createButton(getFormatted("game.toolbar.exit"), "btn-secondary", _ -> onExit());

        topToolbar.getItems().addAll(
                buttonUndo, buttonRedo,
                new Separator(Orientation.VERTICAL)
        );

        if (hintsOn) {
            topToolbar.getItems().addAll(buttonHint);
        }

        Pane rightSpacer = new Pane();
        javafx.scene.layout.HBox.setHgrow(rightSpacer, javafx.scene.layout.Priority.ALWAYS);

        topToolbar.getItems().addAll(
                buttonHelp,
                new Separator(Orientation.VERTICAL),
                buttonSolution,
                new Separator(Orientation.VERTICAL),
                buttonExport,
                new Separator(Orientation.VERTICAL),
                buttonRestart,
                new Separator(Orientation.VERTICAL),
                buttonExit,

                rightSpacer,
                buttonShare
        );

        setIcons();
    }

    protected void setIcons() {
        setIcon(buttonPause, "/cz/logicgo/ui/images/svg/pause.svg");
        setIcon(buttonUndo, "/cz/logicgo/ui/images/svg/arrow-left.svg");
        setIcon(buttonRedo, "/cz/logicgo/ui/images/svg/arrow-right.svg");
        setIcon(buttonExit, "/cz/logicgo/ui/images/svg/exit.svg");
        setIcon(buttonExport, "/cz/logicgo/ui/images/svg/file-export.svg");
        setIcon(buttonSolution, "/cz/logicgo/ui/images/svg/solution.svg");
        setIcon(buttonHelp, "/cz/logicgo/ui/images/svg/help.svg");
        setIcon(buttonHint, "/cz/logicgo/ui/images/svg/progress-help.svg");
        setIcon(buttonRestart, "/cz/logicgo/ui/images/svg/progress-help.svg");
    }

    public void openSolution() throws IOException {
        GameAssociatedOpeners.openSolution(gameInstance);
    }

    public void openExport() throws IOException {
        GameAssociatedOpeners.openExport(gameInstance, stage, mainScreenController);
    }

    public void setupTooltips(boolean hintsWindow) {
        addTooltip(buttonPause, "tooltip.game.play");
        addTooltip(buttonUndo, "tooltip.game.undo");
        addTooltip(buttonRedo, "tooltip.game.redo");
        addTooltip(buttonExit, "tooltip.game.exit");
        addTooltip(buttonExport, "tooltip.game.export");
        addTooltip(buttonSolution, "tooltip.game.solution");
        addTooltip(buttonHelp, "tooltip.game.help");
        addTooltip(buttonHint, hintsWindow ? "tooltip.game.hintWindow" : "tooltip.game.noHintWindow");
        addTooltip(buttonRestart, "tooltip.game.restart");
    }

    private void addTooltip(Node node, String key) {
        if (node == null) return;
        node.setOnMouseEntered(_ -> mainScreenController.setTextToLabel(getFormatted(key)));
        node.setOnMouseExited(_ -> mainScreenController.unsetTextToLabel());
    }

    public void openHelp() {
        HelpWindowManager.showHelp(gameInstance);
    }

    public void onExit() {
        if (tabState.isDone() && !tabState.isChangePending()) {
            mainScreenController.closeTab(tabState.getAssociatedTab());
        } else {
            boolean close = AlertBox.initCloseGame();
            if (close) {
                mainScreenController.closeTab(tabState.getAssociatedTab());
            }
        }
    }

    protected Button createButton(String text, String styleClass, EventHandler<ActionEvent> action) {
        Button btn = new Button(text);
        btn.getStyleClass().add(styleClass);
        btn.setOnAction(action);
        return btn;
    }

    protected void updateElapsedTime() {
        if (timerIsRunning) {
            long elapsedMillis = System.currentTimeMillis() - startTime;
            elapsedTime = Duration.ofMillis(elapsedMillis);
            if (gameInstance != null) {
                gameInstance.setElapsedTime(elapsedTime);
            }
        }
    }

    public void startTimer() {
        if (elapsedTime == null && gameInstance != null && gameInstance.getElapsedTime() != null) {
            elapsedTime = gameInstance.getElapsedTime();
        }

        long offset = (elapsedTime != null) ? elapsedTime.toMillis() : 0;
        startTime = System.currentTimeMillis() - offset;

        if (elapsedTimeTimer != null) {
            elapsedTimeTimer.stop();
        }

        elapsedTimeTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateElapsedTime();
                if (countdownTimer != null && elapsedTime != null) {
                    countdownTimer.setText(getFormattedTime(elapsedTime.toSeconds()));
                }
            }
        };

        timerIsRunning = true;
        elapsedTimeTimer.start();
        startAutoSaveTimer();
    }

    public void pause() {
        if (elapsedTimeTimer != null) {
            elapsedTimeTimer.stop();
            updateElapsedTime();
            timerIsRunning = false;
        }
    }

    @FXML
    public void onPauseButtonPressed() {
        if (timerIsRunning) {
            pause();
            setIcon(buttonPause, "/cz/logicgo/ui/images/svg/play.svg");
        } else {
            startTimer();
            setIcon(buttonPause, "/cz/logicgo/ui/images/svg/pause.svg");
        }
    }

    protected abstract void onUndo();

    protected abstract void onRedo();

    protected void changeBoard(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
        resizeCanvas();
    }

    private static void copySeed(Game game, Button btnCopySeed) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();

        clipboardContent.putString(SeedCreator.createSeed(game));
        clipboard.setContent(clipboardContent);

        String originalText = btnCopySeed.getText();
        btnCopySeed.setText(getFormatted("game.seed.copy.copied"));

        PauseTransition pause = new PauseTransition(javafx.util.Duration.seconds(2));
        pause.setOnFinished(event -> btnCopySeed.setText(originalText));
        pause.play();
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public TabState getTabState() {
        return tabState;
    }

    public Canvas getPrimaryCanvas() {
        return primaryCanvas;
    }

    public Canvas getSecondaryCanvas() {
        return secondaryCanvas;
    }

    @Override
    public void terminateAllActiveActions() {
        pause();
        stopAutoSaveTimer();
        stopHintFeedbackTimer();
        if (hoverAnimationTimer != null) {
            hoverAnimationTimer.stop();
        }
        saveGame(true);
    }

    protected abstract void setGameParameters(T game);

    protected abstract void resizeCanvas();

    protected abstract void saveGame(boolean newThumbnail);

    public abstract void gameFinished();

    protected abstract void openHint();

    protected abstract void onRestart();
}
