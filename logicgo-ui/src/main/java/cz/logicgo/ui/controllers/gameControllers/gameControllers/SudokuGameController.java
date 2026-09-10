package cz.logicgo.ui.controllers.gameControllers.gameControllers;

import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.gameClasses.sudoku.SudokuUtils;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.core.misc.enums.hints.SudokuHintType;
import cz.logicgo.core.misc.enums.hints.SudokuTechnique;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.core.misc.enums.settings.modes.CellNotesMode;
import cz.logicgo.core.misc.enums.settings.modes.HighlightMode;
import cz.logicgo.core.util.boardConverters.SudokuConverters;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.engine.algorithms.sudoku.custom.SudokuConflictingCells;
import cz.logicgo.engine.algorithms.sudoku.grader.SudokuGrader;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig;
import cz.logicgo.persistence.services.SudokuService;
import cz.logicgo.ui.commands.Command;
import cz.logicgo.ui.commands.CommandExecutor;
import cz.logicgo.ui.commands.sudokuCommands.RestartSudokuCommand;
import cz.logicgo.ui.commands.sudokuCommands.SetSudokuNumberCommand;
import cz.logicgo.ui.commands.sudokuCommands.ToggleCandidateSudoku;
import cz.logicgo.ui.controllers.gameControllers.AbstractGameController;
import cz.logicgo.ui.controllers.popover.HintPopOverSudokuController;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.handlers.sudokuGame.MouseKeySudokuHandlers;
import cz.logicgo.ui.misc.NodeSnapshots;
import cz.logicgo.ui.misc.TabState;
import cz.logicgo.ui.misc.windows.AlertBox;
import cz.logicgo.ui.renderers.sudoku.ConstraintRenderer;
import cz.logicgo.ui.renderers.sudoku.SudokuRenderer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.PopOver;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static cz.logicgo.core.GameUtils.getGameSettingsAsMap;
import static cz.logicgo.core.GameUtils.getTypedSetting;
import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.*;
import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.core.misc.enums.settings.SudokuSettings.*;
import static cz.logicgo.ui.commands.CommandExecutor.SavedStacks;
import static cz.logicgo.ui.controllers.screenControllers.RedrawCanvasFunctions.clearCanvas;
import static cz.logicgo.ui.controllers.screenControllers.RedrawCanvasFunctions.redrawCanvas;
import static cz.logicgo.ui.renderers.sudoku.SudokuRenderer.drawPulsatingCell;
import static cz.logicgo.ui.utils.SudokuUiUtils.markWrong;


public class SudokuGameController extends AbstractGameController<Sudoku> {
    private ArrayList<SudokuCell> hoveredSudokuCells = new ArrayList<>();
    public HighlightMode highlightMode;
    public CellNotesMode cellNotesMode;
    SudokuService sudokuService = new SudokuService();
    private SidePanelSudoku sidePanel;
    private SudokuCell selectedSudokuCell;
    private SudokuGame sudokuGame;
    private boolean hintChoice = false;

    private SudokuHintType activeHint;
    private MouseKeySudokuHandlers mouseKeyHandlers;
    private Boolean notesMode = false;
    private boolean highlightConflicts;

    @FXML
    private Canvas clueCanvas;

    public SudokuGameController() {
        this.commandExecutor = new CommandExecutor();
    }

    public CellNotesMode getCellNotesMode() {
        return cellNotesMode;
    }

    public HighlightMode getHighlightMode() {
        return highlightMode;
    }

    public void initialize(Sudoku sudoku, MainScreenController mainScreenController, TabState tabState) {
        this.gameInstance = sudoku;
        this.mainScreenController = mainScreenController;
        this.tabState = tabState;

        buildToolbar(sudoku);
        setupTooltips(true);

        sudokuGame = new SudokuGame(sudoku, false);

        setGameParameters(sudoku);

        commandExecutor.loadSudokuCommands(sudokuGame);

        tabState.setChangePending();

        tabState.doneProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveGame(true);
                tabState.markSavingFinished();
            }
        });

        gamePane.widthProperty().addListener(this::changeBoard);
        gamePane.heightProperty().addListener(this::changeBoard);

        if (clueCanvas != null) {
            clueCanvas.setManaged(false);
        }
        primaryCanvas.setManaged(false);
        secondaryCanvas.setManaged(false);

        redrawCanvas(primaryCanvas, sudokuGame.getSudoku());

        if (cellNotesMode != CellNotesMode.NONE) {
            int gridSize = sudokuGame.getSudokuValidator().getGridSize();
            this.sidePanel = new SidePanelSudoku(gridSize, MultiGridConfig.isMultiDoku(sudoku.getVariant()), commandExecutor);

            if (gamePane.getParent() instanceof BorderPane bp) {
                Node rightNode = bp.getRight();
                if (rightNode instanceof VBox timerBox) {
                    timerBox.getChildren().addFirst(sidePanel);
                } else {
                    bp.setRight(sidePanel);
                }
            }
        }

        saveGame(true);

        tabState.setIdOfGame(sudokuGame.getSudoku().getId());

        Tab tab = tabState.getAssociatedTab();
        tab.setClosable(true);

        tab.setOnCloseRequest((event) -> {
            tab.getTabPane().setTabDragPolicy(TabPane.TabDragPolicy.FIXED);
            event.consume();
            onExit();
        });

        javafx.application.Platform.runLater(() -> {
            gamePane.setFocusTraversable(true);
            gamePane.requestFocus();
        });
    }

    @Override
    protected void resizeCanvas() {
        Sudoku sudoku = sudokuGame.getSudoku();

        double paneWidth = gamePane.getWidth();
        double paneHeight = gamePane.getHeight();

        if (paneWidth <= 0 || paneHeight <= 0) return;

        int gridCols = sudokuGame.getSudokuValidator().getGridSize();

        int colsForCalculation = gridCols + (hasOutsideClues(sudoku) ? 3 : 0);
        double minSide = Math.min(paneWidth, paneHeight) * 0.95;
        double cellSize = minSide / colsForCalculation;

        double gridSize = cellSize * gridCols;

        double cluePadding = hasOutsideClues(sudoku) ? (cellSize * 1.5) : 0.0;

        double gridOffsetX = (paneWidth - gridSize) / 2.0;
        double gridOffsetY = (paneHeight - gridSize) / 2.0;

        if (clueCanvas != null) {
            double clueCanvasWidth = gridSize + (2 * cluePadding);
            double clueCanvasHeight = gridSize + (2 * cluePadding);

            clueCanvas.setWidth(clueCanvasWidth);
            clueCanvas.setHeight(clueCanvasHeight);

            clueCanvas.setLayoutX((paneWidth - clueCanvasWidth) / 2.0);
            clueCanvas.setLayoutY((paneHeight - clueCanvasHeight) / 2.0);

            GraphicsContext gc = clueCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, clueCanvasWidth, clueCanvasHeight);

            gc.setFill(Color.web("#1e1e1e"));
            gc.fillRect(0, 0, clueCanvasWidth, clueCanvasHeight);

            gc.setStroke(Color.GRAY);
            gc.setLineWidth(2);
            gc.strokeRect(cluePadding, cluePadding, gridSize, gridSize);
        }

        for (Canvas c : new Canvas[]{primaryCanvas, secondaryCanvas}) {
            c.setWidth(gridSize);
            c.setHeight(gridSize);
            c.setLayoutX(gridOffsetX);
            c.setLayoutY(gridOffsetY);
        }

        redrawActive();

        if (hasOutsideClues(sudoku)) {
            drawOutsideClues(clueCanvas, sudoku, cluePadding, cluePadding, gridSize, gridSize, false);
        }
    }

    private void drawOutsideClues(Canvas targetCanvas, Sudoku sudoku, double offsetX, double offsetY, double gridWidth, double gridHeight, boolean forPrint) {
        if (clueCanvas == null || sudoku.getModifiers() == null) return;

        int size = sudokuGame.getSudokuValidator().getGridSize();
        double cellW = gridWidth / size;
        double cellH = gridHeight / size;

        ConstraintRenderer.drawOutsideModifiers(
                targetCanvas.getGraphicsContext2D(), sudoku.getModifiers(), size,
                cellW, cellH, offsetX, offsetY, gridWidth, gridHeight, forPrint
        );
    }

    @Override
    protected void setGameParameters(Sudoku sudoku) {
        Map<SettingKey, GameSetting> settings = getGameSettingsAsMap(sudoku.getSettings());

        CellNotesMode cellNotes = getTypedSetting(settings, CELL_NOTES_MODE);
        setCellsProperties(sudoku, false, cellNotes != CellNotesMode.NONE);

        boolean shouldTimerExist = getTypedSetting(settings, TIMER);
        highlightMode = getTypedSetting(settings, HIGHLIGHT_MODE);
        cellNotesMode = getTypedSetting(settings, CELL_NOTES_MODE);
        highlightConflicts = getTypedSetting(settings, HIGHLIGHT_CONFLICTS);
        if (sudoku.getCandidates() == null || sudoku.getCandidates().isEmpty()) {
            if (sudoku.getCandidateBytes() != null && sudoku.getCandidateBytes().length > 0) {
                var restored = SudokuConverters
                        .bytesToSudokuCellCandidates(sudoku.getCandidateBytes(), sudoku.getBoard());
                sudoku.setCandidates(restored);
            } else {
                sudoku.setCandidates(new HashMap<>());
            }
        }

        SudokuCell[] flatBoard = flattenBoard(sudoku.getBoard());

        Consumer<? super SudokuCell> action = cellNotesMode == CellNotesMode.NONE ? (cell -> cell.setShowCandidates(false)) : (cell -> cell.setShowCandidates(true));
        for (SudokuCell sudokuCell : flatBoard) {
            if (sudokuCell != null) {
                action.accept(sudokuCell);
            }
        }

        if (!shouldTimerExist) {
            buttonPause.setVisible(false);
            buttonPause.setManaged(false);
            countdownTimer.setVisible(false);
            countdownTimer.setManaged(false);
        } else {
            buttonPause.setVisible(true);
            buttonPause.setManaged(true);
            buttonPause.setDisable(false);
        }

        setCandidates();

        if (cellNotesMode != CellNotesMode.MANUAL) {
            setCandidates(sudoku.getStartingBoard());
        }

        mouseKeyHandlers = new MouseKeySudokuHandlers(sudokuGame, this);

        if (shouldTimerExist) {
            startTimer();
        }
        startHoverAnimation();
    }

    @Override
    public void openHint() {
        hintChoice = true;
        showPopOver();
    }

    public List<SudokuCell> gatherEmptyCells(SudokuGame sudokuGame) {
        var validator = sudokuGame.getSudokuValidator();
        List<SudokuCell> cells = new ArrayList<>();
        if (validator != null) {
            for (var cell : validator.getEmptyCells()) {
                cells.add(sudokuGame.getSudoku().getSudokuCell(cell.getRow(), cell.getCol()));
            }
        } else {
            cells = Arrays.stream(flattenBoard(sudokuGame.getSudoku().getBoard()))
                    .filter(cell -> cell != null && cell.getValue() == 0).toList();
        }
        return cells;
    }

    public void showPopOver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cz/logicgo/ui/windows/popovers/HintPopOverSudoku.fxml"));
            Parent content = loader.load();
            HintPopOverSudokuController controller = loader.getController();

            PopOver popOver = new PopOver(content);
            controller.initialize(mainScreenController, sudokuGame.getSudoku().getVariant());
            content.getStyleClass().add("root");
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
            popOver.setAutoHide(true);
            popOver.setDetachable(false);
            Random random = new Random();

            controller.setOnSelect(type -> {
                this.setActiveHint(type);
                switch (type) {
                    case RANDOM_CELL -> {
                        SudokuGame sudokuGame = this.getSudokuGame();
                        List<SudokuCell> emptyCells = gatherEmptyCells(sudokuGame);
                        if (emptyCells.isEmpty()) break;
                        int index = random.nextInt(emptyCells.size());

                        SudokuCell sudokuCell = emptyCells.get(index);
                        int row = sudokuCell.getRow();
                        int col = sudokuCell.getCol();
                        int value = getSudokuGame().getSudoku().getSolutionBoard()[row][col].getValue();

                        SetSudokuNumberCommand command = new SetSudokuNumberCommand(sudokuGame, row, col, value, 0);
                        this.getCommandExecutor().execute(command);
                        this.tabState.setChangePending();

                        this.setHintChoice(false);
                        this.setActiveHint(null);
                        redrawCanvas(primaryCanvas, sudokuGame.getSudoku());
                    }
                    case CHECK_VALIDITY -> {
                        Sudoku sudoku = this.getSudokuGame().getSudoku();
                        markWrong(sudokuGame, Arrays.stream(flattenBoard(sudoku.getBoard())).toList(), this);
                        this.setHintChoice(false);
                        this.setActiveHint(null);
                        redrawCanvas(primaryCanvas, sudokuGame.getSudoku());
                    }
                    case NEXT_LOGICAL_STEP -> {
                        SudokuTechnique technique = SudokuGrader.getNextLogicalHint(sudokuGame);
                        if (technique != null) {
                            String translatedName = getFormatted(technique.getName());
                            AlertBox.OkWindowError(technique == SudokuTechnique.VARIANT_BASIC ? getFormatted("hint.sudoku.grader.variant_basic") : getFormatted("hint.sudoku.grader.basic", translatedName));
                        } else {
                            AlertBox.OkWindowError(getFormatted("hint.sudoku.grader.error"));
                        }
                        this.setHintChoice(false);
                        this.setActiveHint(null);
                    }
                    default -> {
                        popOver.hide();
                        return;
                    }
                }
                redrawActive();
                popOver.hide();
            });

            popOver.show(buttonHint);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCandidates() {
        switch (cellNotesMode) {
            case MANUAL -> {
                for (var cell : flattenBoard(sudokuGame.getSudoku().getBoard())) {
                    var set = sudokuGame.getSudoku().getCandidates().get(cell);
                    if (set != null) {
                        cell.setCandidates(set.stream().toList());
                    }
                }
            }
            default -> {
                SudokuCell[][] board = sudokuGame.getSudoku().getBoard();
                setCandidates(board);
            }
        }
    }

    public void refreshConflicts() {
        if (highlightConflicts) {
            SudokuGame copy = new SudokuGame(gameInstance, true);
            boolean[][] conflictingCells = SudokuConflictingCells.getConflictingCellsMatrix(copy);
            Arrays.stream(flattenBoard(gameInstance.getBoard())).filter(Objects::nonNull).forEach(sudokuCell -> sudokuCell.setConflicting(conflictingCells[sudokuCell.getRow()][sudokuCell.getCol()]));
        }
    }

    public void setCandidates(SudokuCell[][] board) {
        SudokuGame tempGame = new SudokuGame(this.gameInstance, true);
        for (int row = 0; row < tempGame.getSudokuValidator().getGridSize(); row++) {
            for (int col = 0; col < tempGame.getSudokuValidator().getGridSize(); col++) {
                if (board[row][col] != null) {
                    var candidates = getCellCandidatesPlay(tempGame, row, col);
                    board[row][col].setCandidates(candidates);
                }
            }
        }
    }

    public static ArrayList<Integer> getCellCandidatesPlay(SudokuGame sudokuGame, int setRow, int setCol) {
        ArrayList<Integer> candidates = new ArrayList<>();

        var sudoku = sudokuGame.getSudoku();
        SudokuGame game = new SudokuGame(sudoku, true);

        for (int num : game.getSudoku().getType().getPossibleNumbers()) {
            if (game.getSudokuValidator().isValidMove(setRow, setCol, num)) {
                candidates.add(num);
            }
        }

        return candidates;
    }

    @Override
    protected void handleHoverPreview(double timeSeconds) {
        if (hoveredSudokuCells == null) return;
        if (!hoveredSudokuCells.isEmpty() && isHintChoice()) {
            redrawActive();
            int gridSize = sudokuGame.getSudokuValidator().getGridSize();
            drawPulsatingCell(primaryCanvas, timeSeconds, gridSize, hoveredSudokuCells);
        } else {
            redrawActive();
            clearCanvas(secondaryCanvas);
        }
    }

    public void clearHintVisuals() {
        this.hoveredSudokuCells = null;
        this.activeHint = null;
        this.hintChoice = false;

        if (secondaryCanvas != null) {
            secondaryCanvas.getGraphicsContext2D().clearRect(0, 0, secondaryCanvas.getWidth(), secondaryCanvas.getHeight());
        }

        if (primaryCanvas != null && sudokuGame != null && sudokuGame.getSudoku() != null) {
            SudokuRenderer.renderFullBoard(primaryCanvas, sudokuGame.getSudoku(), false);
        }
    }

    public void gameFinished() {
        sudokuGame.getSudoku().setStatus(Status.FINISHED);
        pause();
        buttonPause.setDisable(true);
        buttonHint.setDisable(true);
        tabState.markSavingFinished();
        saveGame(true);
        AlertBox.YouWonWindow(sudokuGame.getSudoku().getElapsedTime());
    }

    @Override
    public void saveGame(boolean newThumbnail) {
        updateElapsedTime();
        Sudoku sudoku = sudokuGame.getSudoku();
        try {
            if (sudoku.getCandidates() != null) {
                byte[] candidateBytes = SudokuConverters
                        .sudokuCellCandidatesToBytes(sudoku.getCandidates());
                sudoku.setCandidateBytes(candidateBytes);
            }

            SavedStacks stacks = commandExecutor.serializeCommandsToByteArrays();
            sudoku.setUndoStack(stacks.undoStack());
            sudoku.setRedoStack(stacks.redoStack());
            if (newThumbnail) {
                double w = primaryCanvas.getWidth() <= 0 ? 600 : primaryCanvas.getWidth();
                double h = primaryCanvas.getHeight() <= 0 ? 600 : primaryCanvas.getHeight();
                new NodeSnapshots().makeThumbnail(gameInstance, w, h);
            }
        } catch (Exception _) {
        } finally {
            sudokuService.saveGame(sudoku);
            tabState.setIdOfGame(sudoku.getId());
            tabState.unsetChangePending();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void onMouseClicked(MouseEvent mouseEvent) {
        gamePane.setFocusTraversable(true);
        gamePane.requestFocus();
        this.getMouseKeyHandlers().onMouseClicked(mouseEvent);
    }

    public void onKeyPressed(KeyEvent keyEvent) throws IOException {
        this.getMouseKeyHandlers().onKeyPressed(keyEvent);
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        this.getMouseKeyHandlers().onKeyReleased(keyEvent);
    }

    public void onMouseReleased(MouseEvent mouseEvent) {
        this.getMouseKeyHandlers().onMouseReleased(mouseEvent);
    }

    public void onMouseMoved(MouseEvent mouseEvent) {
        this.getMouseKeyHandlers().onMouseMoved(mouseEvent);
    }

    public SudokuCell getSelectedSudokuCell() {
        return selectedSudokuCell;
    }

    public void setSelectedSudokuCell(SudokuCell selectedSudokuCell) {
        this.selectedSudokuCell = selectedSudokuCell;

        if (selectedSudokuCell == null && sudokuGame != null && sudokuGame.getSudoku() != null) {
            for (SudokuCell[] row : sudokuGame.getSudoku().getBoard()) {
                for (SudokuCell cell : row) {
                    if (cell != null) {
                        cell.setDrawToPrimary(true);
                    }
                }
            }
        }

        if (this.sidePanel != null) {
            this.sidePanel.updateSelection(selectedSudokuCell);
        }
    }

    public SudokuGame getSudokuGame() {
        return sudokuGame;
    }

    public MouseKeySudokuHandlers getMouseKeyHandlers() {
        return mouseKeyHandlers;
    }

    public boolean isHintChoice() {
        return hintChoice;
    }

    public void setHintChoice(boolean hintChoice) {
        this.hintChoice = hintChoice;
    }

    public SudokuHintType getActiveHint() {
        return activeHint;
    }

    public void setActiveHint(SudokuHintType activeHint) {
        this.activeHint = activeHint;
    }

    public ArrayList<SudokuCell> getHoveredSudokuCells() {
        return hoveredSudokuCells;
    }

    public void setHoveredSudokuCells(SudokuCell... cells) {
        if (cells == null) cells = new SudokuCell[0];
        if (hoveredSudokuCells == null) {
            hoveredSudokuCells = new ArrayList<>();
        }
        hoveredSudokuCells.clear();
        hoveredSudokuCells.addAll(List.of(cells));
    }

    @Override
    public void onRestart() {
        SudokuCell[][] oldCells = deepCopyBoard(sudokuGame.getSudoku().getBoard());
        SudokuCell[][] newCells = deepCopyBoard(sudokuGame.getSudoku().getStartingBoard());

        Map<GridCell, Set<Integer>> oldCandidates = SudokuUtils.createCandidatesMap(sudokuGame.getSudoku().getCandidates());
        Map<GridCell, Set<Integer>> newCandidates = new HashMap<>();

        Command command = new RestartSudokuCommand(sudokuGame, oldCells, newCells, oldCandidates, newCandidates);
        commandExecutor.execute(command);
    }

    @Override
    public void onRedo() {
        commandExecutor.redo();
        redrawActive();
    }

    public void redrawActive() {
        refreshConflicts();

        List<SudokuCell> wrongList = new ArrayList<>(this.getWrongHintCells());

        SudokuRenderer.renderFullBoard(primaryCanvas, this.getSudokuGame().getSudoku(), false, wrongList);

        clearCanvas(secondaryCanvas);

        SudokuRenderer.renderWrongHints(secondaryCanvas, this.getSudokuGame().getSudoku(), wrongList);
    }

    @Override
    public void onUndo() {
        commandExecutor.undo();
        redrawActive();
    }

    private final Set<SudokuCell> wrongHintCells = new HashSet<>();

    public Set<SudokuCell> getWrongHintCells() {
        return wrongHintCells;
    }

    @Override
    public void terminateAllActiveActions() {
        super.terminateAllActiveActions();
    }

    public Boolean getNotesMode() {
        return notesMode;
    }

    public SudokuGameController setNotesMode(Boolean notesMode) {
        this.notesMode = notesMode;
        refreshPanel();
        return this;
    }

    public void refreshPanel() {
        if (sidePanel != null) {
            sidePanel.redrawCurrent();
        }
    }

    public class SidePanelSudoku extends VBox {
        private final Canvas zoomCanvas;
        private final Label lblInfo;
        private final int gridSize;
        private final boolean isMultiDoku;
        private javafx.scene.layout.FlowPane keypadPane;
        private ToggleButton btnToggleNotes;
        private SudokuCell currentCell;
        CommandExecutor commandExecutor;

        public SidePanelSudoku(int gridSize, boolean isMultiDoku, CommandExecutor commandExecutor) {
            this.getStyleClass().add("root");
            this.gridSize = gridSize;
            this.isMultiDoku = isMultiDoku;
            this.commandExecutor = commandExecutor;
            this.setSpacing(10);
            this.setPadding(new Insets(10));
            this.setPrefWidth(220);
            this.setMinWidth(220);

            Label lblHeader = new Label("Detail buňky");
            lblHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            this.getChildren().add(lblHeader);

            if (cellNotesMode == CellNotesMode.MANUAL) {
                btnToggleNotes = new ToggleButton("Režim poznámek");
                btnToggleNotes.getStyleClass().add("side-panel-toggle");
                btnToggleNotes.setMaxWidth(Double.MAX_VALUE);

                btnToggleNotes.setOnAction(e -> {
                    setNotesMode(btnToggleNotes.isSelected());
                    syncNotesModeState();
                    redrawZoom();
                    redrawActive();
                    redrawCanvas(primaryCanvas, SudokuGameController.this.sudokuGame.getSudoku());
                });

                this.getChildren().addAll(btnToggleNotes, new javafx.scene.control.Separator());
                syncNotesModeState();
            }

            zoomCanvas = new Canvas(180, 180);
            StackPane canvasContainer = new StackPane(zoomCanvas);
            canvasContainer.setMaxSize(182, 182);
            canvasContainer.setStyle("-fx-border-color: gray; -fx-border-width: 1;");

            lblInfo = new Label("Vyberte buňku");
            lblInfo.setWrapText(true);

            this.getChildren().addAll(canvasContainer, lblInfo);

            if (cellNotesMode == CellNotesMode.MANUAL) {
                keypadPane = new javafx.scene.layout.FlowPane();
                keypadPane.setHgap(5);
                keypadPane.setVgap(5);
                keypadPane.setAlignment(javafx.geometry.Pos.CENTER);

                keypadPane.setPrefWrapLength(130);
                keypadPane.setMaxWidth(130);

                generateKeypad();
                this.getChildren().addAll(new Label("Poznámky:"), keypadPane);
            }

            drawEmptyState();
        }

        private void syncNotesModeState() {
            boolean active = Boolean.TRUE.equals(getNotesMode());
            if (btnToggleNotes != null) {
                btnToggleNotes.setSelected(active);
                if (active) {
                    btnToggleNotes.setStyle("-fx-base: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                } else {
                    btnToggleNotes.setStyle("");
                }
            }
        }

        public void updateSelection(SudokuCell cell) {
            this.currentCell = cell;

            syncNotesModeState();

            if (cell == null) {
                drawEmptyState();
                if (keypadPane != null) keypadPane.setDisable(true);
                lblInfo.setText(getFormatted("sudoku.panel.no_cell"));
                return;
            }

            lblInfo.setText(getFormatted("sudoku.panel.cell_coords", cell.getRow(), cell.getCol()));

            redrawZoom();
            updateKeypadState();
            redrawActive();
        }

        public void redrawCurrent() {
            syncNotesModeState();
            redrawZoom();
            updateKeypadState();
        }

        private void drawEmptyState() {
            GraphicsContext gc = zoomCanvas.getGraphicsContext2D();
            gc.setFill(Color.WHITESMOKE);
            gc.fillRect(0, 0, zoomCanvas.getWidth(), zoomCanvas.getHeight());
            gc.setFill(Color.GRAY);
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText("", zoomCanvas.getWidth() / 2, zoomCanvas.getHeight() / 2);
        }

        private void redrawZoom() {
            if (currentCell == null) return;

            GraphicsContext gc = zoomCanvas.getGraphicsContext2D();
            double w = zoomCanvas.getWidth();
            double h = zoomCanvas.getHeight();

            gc.clearRect(0, 0, w, h);
            gc.setFill(Color.WHITESMOKE);
            gc.fillRect(0, 0, w, h);

            if (currentCell.getValue() != 0 && !currentCell.isChangeable()) {
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(0, 0, w, h);
            }

            if (currentCell.getValue() != 0) {
                gc.setFill(currentCell.isChangeable() ? Color.BLUE : Color.BLACK);
                gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, w * 0.6));
                gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
                gc.setTextBaseline(javafx.geometry.VPos.CENTER);
                String value = SudokuUtils.valueToString(currentCell.getValue());
                gc.fillText(value, w / 2, h / 2);
            } else {
                int dim = isMultiDoku ? 3 : (int) Math.ceil(Math.sqrt(gridSize));
                double noteSize = w / dim;

                List<Integer> candidates = switch (cellNotesMode) {
                    case AUTO -> currentCell.getCandidates();
                    case null, default -> {
                        if (sudokuGame.getSudoku().getCandidates() == null) yield new ArrayList<>();
                        else yield sudokuGame.getSudoku().getCandidates().get(currentCell) != null
                                ? sudokuGame.getSudoku().getCandidates().get(currentCell).stream().toList()
                                : new ArrayList<>();
                    }
                };

                if (candidates != null && !candidates.isEmpty()) {
                    for (Integer cand : candidates) {
                        int val = cand;
                        int col = (val - 1) % dim;
                        int row = (val - 1) / dim;

                        double x = col * noteSize + noteSize / 2;
                        double y = row * noteSize + noteSize / 2;

                        gc.setFill(Color.BLACK);
                        gc.setFont(javafx.scene.text.Font.font("Arial", noteSize * 0.6));
                        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
                        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
                        gc.fillText(valueToString(val), x, y);
                    }
                }
            }
        }

        private void generateKeypad() {
            keypadPane.getChildren().clear();
            int limit = isMultiDoku ? 9 : gridSize;

            for (int i = 1; i <= limit; i++) {
                final int num = i;
                ToggleButton btn = new ToggleButton(valueToString(i));
                btn.getStyleClass().add("side-panel-number");
                btn.setPrefSize(40, 40);
                btn.setFocusTraversable(false);

                btn.setOnAction(e -> {
                    if (currentCell == null || !currentCell.isChangeable() || currentCell.getValue() != 0) return;

                    int row = currentCell.getRow();
                    int col = currentCell.getCol();
                    commandExecutor.execute(new ToggleCandidateSudoku(sudokuGame, row, col, num));
                    redrawZoom();
                    setCandidates();
                    redrawActive();
                    redrawCanvas(primaryCanvas, SudokuGameController.this.sudokuGame.getSudoku());
                });

                btn.setUserData(num);
                keypadPane.getChildren().add(btn);
            }
        }

        private void updateKeypadState() {
            if (keypadPane == null) return;

            boolean isEditable = currentCell != null && currentCell.isChangeable() && currentCell.getValue() == 0;
            keypadPane.setDisable(!isEditable);

            if (isEditable) {
                Set<Integer> cands = sudokuGame.getSudoku().getCandidates().get(currentCell);
                for (Node n : keypadPane.getChildren()) {
                    if (n instanceof ToggleButton btn) {
                        int val = (int) btn.getUserData();
                        btn.setSelected(cands != null && cands.contains(val));
                    }
                }
            }
        }
    }
}
