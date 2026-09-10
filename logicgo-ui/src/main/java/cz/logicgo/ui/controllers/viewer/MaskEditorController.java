package cz.logicgo.ui.controllers.viewer;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.viewers.CustomMask;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.persistence.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import static cz.logicgo.core.misc.Messages.getFormatted;


public class MaskEditorController implements Initializable {

    @FXML
    public Canvas canvas;

    @FXML
    public TextField widthField;
    @FXML
    public TextField heightField;
    @FXML
    public Button resizeButton;

    @FXML
    public Label statusLabel;
    @FXML
    public Label infoLabel;
    @FXML
    public Button saveButton;
    @FXML
    public Button clearButton;

    private Stage stage;
    private User user;
    private TypeGame gameType;

    private int width = 20;
    private int height = 20;
    private int[][] mask;

    private static final int MIN_MASK_SIZE = 10;
    private static final int MAX_MASK_SIZE = 50;

    private final UserService userService = new UserService();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void initialize(User user, TypeGame gameType) {
        this.user = user;
        this.gameType = gameType;
        if (resizeButton != null) resizeButton.setText(getFormatted("mask.editor.resize"));
    }

    private int[][] createInitialMask(int h, int w) {
        int[][] newMask = new int[h][w];
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                newMask[r][c] = 1;
            }
        }
        return newMask;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (widthField != null) widthField.setText(String.valueOf(width));
        if (heightField != null) heightField.setText(String.valueOf(height));

        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) return change;
            return null;
        };

        if (widthField != null) {
            widthField.setTextFormatter(new TextFormatter<>(integerFilter));
            widthField.setText(String.valueOf(width));
        }

        if (heightField != null) {
            heightField.setTextFormatter(new TextFormatter<>(integerFilter));
            heightField.setText(String.valueOf(height));
        }

        resizeButton.setOnAction(_ -> applyNewSize());

        mask = createInitialMask(height, width);
        draw();
        validateMask();

        canvas.setOnMousePressed(this::handlePainting);
        canvas.setOnMouseDragged(this::handlePainting);

        clearButton.setOnAction(_ -> {
            mask = createInitialMask(height, width);
            draw();
            validateMask();
        });

        saveButton.setOnAction(_ -> save());
    }

    private void applyNewSize() {
        try {
            int newW = Integer.parseInt(widthField.getText());
            int newH = Integer.parseInt(heightField.getText());

            newW = Math.max(MIN_MASK_SIZE, Math.min(MAX_MASK_SIZE, newW));
            newH = Math.max(MIN_MASK_SIZE, Math.min(MAX_MASK_SIZE, newH));

            widthField.setText(String.valueOf(newW));
            heightField.setText(String.valueOf(newH));

            int[][] newMask = createInitialMask(newH, newW);

            if (mask != null) {
                for (int r = 0; r < Math.min(height, newH); r++) {
                    if (Math.min(width, newW) >= 0) {
                        System.arraycopy(mask[r], 0, newMask[r], 0, Math.min(width, newW));
                    }
                }
            }

            this.width = newW;
            this.height = newH;
            this.mask = newMask;

            draw();
            validateMask();

        } catch (NumberFormatException e) {
            widthField.setText(String.valueOf(width));
            heightField.setText(String.valueOf(height));
        }
    }

    private void handlePainting(MouseEvent event) {
        double cellW = canvas.getWidth() / width;
        double cellH = canvas.getHeight() / height;

        int col = (int) (event.getX() / cellW);
        int row = (int) (event.getY() / cellH);

        if (row >= 0 && row < height && col >= 0 && col < width) {
            int value = -1;

            if (event.getButton() == MouseButton.PRIMARY || event.isPrimaryButtonDown()) {
                value = 1;
            } else if (event.getButton() == MouseButton.SECONDARY || event.isSecondaryButtonDown()) {
                value = 0;
            }

            if (value != -1 && mask[row][col] != value) {
                mask[row][col] = value;
                draw();
                validateMask();
            }
        }
    }

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double cellWidth = canvas.getWidth() / width;
        double cellHeight = canvas.getHeight() / height;

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                gc.setFill(mask[r][c] == 1 ? Color.WHITE : Color.web("#2d2d2d"));
                gc.fillRect(c * cellWidth, r * cellHeight, cellWidth, cellHeight);
            }
        }

        gc.setStroke(Color.web("#555555"));
        gc.setLineWidth(0.5);
        for (int i = 0; i <= width; i++) {
            gc.strokeLine(i * cellWidth, 0, i * cellWidth, height * cellHeight);
        }
        for (int i = 0; i <= height; i++) {
            gc.strokeLine(0, i * cellHeight, width * cellWidth, i * cellHeight);
        }
    }

    private void validateMask() {
        boolean isConnected = isMaskConnected();

        if (isConnected) {
            statusLabel.setText(getFormatted("mask.editor.connected"));
            statusLabel.setTextFill(Color.web("#4caf50"));
            infoLabel.setText("");
        } else {
            statusLabel.setText(getFormatted("mask.editor.disconnected"));
            statusLabel.setTextFill(Color.web("#ff6b6b"));
            infoLabel.setText("");
        }

        if (saveButton != null) {
            saveButton.setDisable(!isConnected);
        }
    }

    private boolean isMaskConnected() {
        int startRow = -1;
        int startCol = -1;
        int totalOnes = 0;

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (mask[r][c] == 1) {
                    totalOnes++;
                    if (startRow == -1) {
                        startRow = r;
                        startCol = c;
                    }
                }
            }
        }

        if (totalOnes == 0) return false;

        boolean[][] visited = new boolean[height][width];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;

        int visitedCount = 0;
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            visitedCount++;

            for (int[] dir : directions) {
                int newRow = current[0] + dir[0];
                int newCol = current[1] + dir[1];

                if (newRow >= 0 && newRow < height && newCol >= 0 && newCol < width) {
                    if (mask[newRow][newCol] == 1 && !visited[newRow][newCol]) {
                        visited[newRow][newCol] = true;
                        queue.add(new int[]{newRow, newCol});
                    }
                }
            }
        }

        return visitedCount == totalOnes;
    }

    private void save() {
        if (user != null && gameType != null) {
            boolean[][] copy = new boolean[height][width];
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < width; c++) {
                    copy[r][c] = (mask[r][c] == 1);
                }
            }

            boolean alreadyExists = user.getCustomMasks().stream()
                    .anyMatch(m -> java.util.Arrays.deepEquals(m.getLayout(), copy));

            if (!alreadyExists) {
                CustomMask newCustomMask = new CustomMask();
                newCustomMask.setLayout(copy);
                newCustomMask.setGameType(gameType);
                user.getCustomMasks().add(newCustomMask);
                userService.updateUser(user);
            }
        }

        if (stage != null) stage.close();
    }
}
