package cz.logicgo.ui.controllers.viewer;

import cz.logicgo.core.builders.sudoku.SudokuCreation;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.sudoku.SudokuInit;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.viewers.SudokuPattern;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.algorithms.sudoku.SudokuGenerator;
import cz.logicgo.engine.algorithms.sudoku.custom.CustomLayoutsLoader;
import cz.logicgo.engine.util.generate.RandomizationFactory;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.ui.controllers.screenControllers.RedrawCanvasFunctions;
import cz.logicgo.ui.renderers.sudoku.ConstraintRenderer;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import static cz.logicgo.core.misc.Messages.getFormatted;


public class SudokuCustomEditorController implements Initializable {

    private final UserService userService = new UserService();
    @FXML
    public Label mainTitleLabel;
    @FXML
    public Canvas canvas;
    @FXML
    public TilePane palettePane;
    @FXML
    public Label statusLabel;
    @FXML
    public Label infoLabel;
    @FXML
    public Button clearButton;
    @FXML
    public Button saveButton;
    @FXML
    public Button testButton;
    @FXML
    public ProgressIndicator testProgress;
    @FXML
    public Label testResultLabel;

    private Stage stage;
    private User user;
    private int size;
    private int[][] layout;
    private int activeRegion = 1;
    private Task<Boolean> testingTask;

    private static final int MIN_PATTERN_SIZE = 8;
    private static final int MAX_PATTERN_SIZE = 10;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void initialize(User user, int size) {
        this.user = user;
        this.size = Math.max(MIN_PATTERN_SIZE, Math.min(MAX_PATTERN_SIZE, size));
        this.layout = new int[this.size][this.size];

        mainTitleLabel.setText(getFormatted("sudoku.editor.pattern.title"));

        buildPalette();
        draw();
        validateLayout();

        canvas.setOnMousePressed(this::handlePainting);
        canvas.setOnMouseDragged(this::handlePainting);

        clearButton.setOnAction(_ -> {
            if (testingTask != null && testingTask.isRunning()) {
                testingTask.cancel(true);
                resetAfterCancel();
            } else {
                for (int r = 0; r < this.size; r++) Arrays.fill(layout[r], 0);
                draw();
                validateLayout();
                invalidateTest();
            }
        });
        if (testResultLabel != null) testResultLabel.setText(getFormatted("sudoku.editor.untested"));
        testButton.setOnAction(_ -> runGenerationTest());
        saveButton.setOnAction(_ -> saveAndClose());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    private void buildPalette() {
        List<String> colorsString = user.getUserColors();
        List<Color> colors = colorsString.stream().map(Color::web).toList();
        ToggleGroup group = new ToggleGroup();
        int paletteSize = Math.min(size, colors.size());

        palettePane.setPrefColumns(3);

        for (int i = 1; i <= paletteSize; i++) {
            ToggleButton btn = new ToggleButton(String.valueOf(i));
            btn.setPrefSize(40, 40);

            Color color = colors.get(i - 1);
            String hex = String.format("#%02x%02x%02x",
                    (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));

            btn.setStyle("-fx-background-color: " + hex + "; -fx-text-fill: black; -fx-font-weight: bold;");

            int regionId = i;
            btn.setOnAction(_ -> activeRegion = regionId);
            btn.setToggleGroup(group);
            palettePane.getChildren().add(btn);

            if (i == 1) btn.setSelected(true);
        }
    }

    private void handlePainting(MouseEvent event) {
        double cellWidth = canvas.getWidth() / size;
        double cellHeight = canvas.getHeight() / size;
        int col = (int) (event.getX() / cellWidth);
        int row = (int) (event.getY() / cellHeight);

        if (row >= 0 && row < size && col >= 0 && col < size) {
            int valueToWrite = -1;

            if (event.getButton() == MouseButton.PRIMARY || event.isPrimaryButtonDown()) {
                valueToWrite = activeRegion;
            } else if (event.getButton() == MouseButton.SECONDARY || event.isSecondaryButtonDown()) {
                valueToWrite = 0;
            }

            if (valueToWrite != -1 && layout[row][col] != valueToWrite) {
                layout[row][col] = valueToWrite;
                draw();
                validateLayout();
                invalidateTest();
            }
        }
    }

    private void invalidateTest() {
        saveButton.setDisable(true);
        testResultLabel.setText(getFormatted("sudoku.editor.new_test_needed"));
        testResultLabel.setTextFill(Color.web("#aaaaaa"));
    }

    private void runGenerationTest() {
        if (testingTask != null && testingTask.isRunning()) testingTask.cancel(true);

        testButton.setDisable(true);
        testProgress.setVisible(true);
        testProgress.setManaged(true);
        testResultLabel.setText(getFormatted("sudoku.editor.test_running"));
        testResultLabel.setTextFill(Color.web("#e0e0e0"));
        saveButton.setDisable(true);

        clearButton.setText(getFormatted("sudoku.editor.cancel_test"));

        SudokuSize sSize = SudokuSize.getTypeByGridSize(size);

        Integer[][] patternCopy = new Integer[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) patternCopy[r][c] = layout[r][c];
        }
        SudokuPatternLayout patternLayout = new SudokuPatternLayout(patternCopy, true);

        if (patternLayout.getIndexCount() > 0){
            testingTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    Random rand = new Random();
                    Difficulty[] difficultiesToTest = {Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD};

                    long genLimit = Long.MAX_VALUE;
                    long delLimit = Long.MAX_VALUE;
                    long solveLimit = Long.MAX_VALUE;

                    if (sSize == SudokuSize.EIGHT) {
                        genLimit = 200_000L;
                        delLimit = 150L;
                        solveLimit = 4_500L;
                    } else if (sSize == SudokuSize.NINE) {
                        genLimit = 1_000_000L;
                        delLimit = 250L;
                        solveLimit = 7_500L;
                    } else if (sSize == SudokuSize.TEN) {
                        genLimit = 3_500_000L;
                        delLimit = 400L;
                        solveLimit = 18_000L;
                    }

                    for (Difficulty targetDifficulty : difficultiesToTest) {
                        if (sSize.getGridSize() <= 6 && targetDifficulty != Difficulty.EASY) continue;
                        if (sSize.getGridSize() <= 8 && targetDifficulty == Difficulty.HARD) continue;

                        boolean difficultyPassed = false;
                        int totalFailures = 0;
                        int maxTotalFailures = 40;

                        while (!difficultyPassed) {
                            if (isCancelled()) return false;

                            if (totalFailures >= maxTotalFailures) {
                                return false;
                            }

                            long seed = RandomizationFactory.generateSeed(rand.nextLong());

                            SudokuInit config = new SudokuInit()
                                    .setSudokuVariant(SudokuVariant.PATTERNED)
                                    .setSudokuSize(sSize)
                                    .setRegionLayout(CustomLayoutsLoader.getBasicLayout(sSize))
                                    .setDifficulty(targetDifficulty)
                                    .setSeed(seed)
                                    .setPatternLayout(patternLayout);

                            Sudoku sudoku = new Sudoku(new SudokuCreation()
                                    .setSudokuSize(config.getSudokuSize())
                                    .setDifficulty(config.getDifficulty())
                                    .setSudokuVariant(config.getSudokuVariant())
                                    .setRegionLayout(config.getRegionLayout())
                                    .setSeed(config.getSeed())
                                    .setPatternLayout(config.getPatternLayout()));

                            long[] metrics = SudokuGenerator.generateAndMeasureSteps(sudoku);

                            if (metrics != null && metrics[0] != -1 && metrics[0] != -5L) {
                                long actualGen = metrics[0];
                                long actualDel = metrics[1];
                                long actualSolve = metrics[2];

                                if (actualGen <= genLimit && actualDel <= delLimit && actualSolve <= solveLimit) {
                                    difficultyPassed = true;
                                } else {
                                    totalFailures++;
                                }
                            } else {
                                totalFailures++;
                            }
                        }
                    }
                    return true;
                }
            };

            testingTask.setOnSucceeded(e -> {
                if (testingTask.getValue()) {
                    testResultLabel.setText(getFormatted("sudoku.editor.success"));
                    testResultLabel.setTextFill(Color.web("#4caf50"));
                    saveButton.setDisable(false);
                } else {
                    testResultLabel.setText(getFormatted("sudoku.editor.unstable"));
                    testResultLabel.setTextFill(Color.web("#ff6b6b"));
                }
                finishTestUI();
            });

            testingTask.setOnFailed(e -> {
                if (testingTask.getException() != null) testingTask.getException().printStackTrace();
                testResultLabel.setText("Chyba");
                testResultLabel.setTextFill(Color.web("#ff6b6b"));
                finishTestUI();
            });

            Thread th = new Thread(testingTask);
            th.setDaemon(true);
            th.start();
        }
    }

    private void finishTestUI() {
        testButton.setDisable(false);

        testProgress.setVisible(false);
        testProgress.setManaged(false);

        clearButton.setText(getFormatted("cancelButton"));
    }

    private void resetAfterCancel() {
        finishTestUI();
        invalidateTest();
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double cellWidth = canvas.getWidth() / size;
        double cellHeight = canvas.getHeight() / size;

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        List<Color> colors = user.getUserColors().stream().map(Color::web).toList();

        ConstraintRenderer.drawEditorPattern(gc, layout, cellWidth, cellHeight, colors);

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1.0);
        for (int i = 0; i <= size; i++) {
            gc.strokeLine(i * cellWidth, 0, i * cellWidth, size * cellHeight);
            gc.strokeLine(0, i * cellHeight, size * cellWidth, i * cellHeight);
        }

        RedrawCanvasFunctions.strokeBoard(
                CustomLayoutsLoader.getBasicLayout(SudokuSize.getTypeByGridSize(size)).getRegions(),
                cellWidth, cellHeight, gc, SudokuVariant.PATTERNED, null, true
        );
    }

    private void validateLayout() {
        boolean isValid = true;
        int[] counts = new int[size + 1];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int index = layout[r][c];
                if (index == 0) continue;
                counts[index - 1]++;
            }
        }
        for (int i = 0; i <= size; i++) {
            if (counts[i] > 0 && counts[i] != size) {
                isValid = false;
                break;
            }
        }

        if (isValid) {
            statusLabel.setText(getFormatted("sudoku.editor.valid"));
            statusLabel.setTextFill(Color.web("#4caf50"));
            infoLabel.setText("");
        } else {
            statusLabel.setText(getFormatted("sudoku.editor.non_valid"));
            statusLabel.setTextFill(Color.web("#ff6b6b"));
            infoLabel.setText(getFormatted("sudoku.editor.needed_cell_count", size));
        }

        testButton.setDisable(!isValid);
    }

    private void saveAndClose() {
        Integer[][] copy = new Integer[size][size];

        int[] distinctCount = Arrays.stream(layout)
                .flatMapToInt(Arrays::stream)
                .filter(i -> i >= 1 && i <= size)
                .distinct()
                .sorted()
                .toArray();

        boolean selective = distinctCount.length != size;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                copy[r][c] = layout[r][c];
            }
        }

        boolean alreadyExists = user.getCustomPatterns().stream()
                .anyMatch(p -> Arrays.deepEquals(p.getLayout().getPattern(), copy));

        if (!alreadyExists) {
            SudokuPattern newCustom = new SudokuPattern();
            SudokuPatternLayout patternLayout = new SudokuPatternLayout(copy, selective);
            newCustom.setLayout(patternLayout);
            newCustom.setCreated(true);
            user.getCustomPatterns().add(newCustom);

            userService.updateUser(user);
        }

        if (stage != null) stage.close();
    }
}
