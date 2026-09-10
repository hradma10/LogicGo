package cz.logicgo.ui.controllers.gameControllers;

import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.ui.renderers.BridgeRenderer;
import cz.logicgo.ui.renderers.ShikakuRenderer;
import cz.logicgo.ui.renderers.maze.MazeRenderer;
import cz.logicgo.ui.renderers.sudoku.ConstraintRenderer;
import cz.logicgo.ui.renderers.sudoku.SudokuRenderer;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static cz.logicgo.core.GameUtils.createNewInstance;
import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.flattenBoard;
import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.hasOutsideClues;


public class SolutionWindowController implements Initializable {

    @FXML
    public Canvas canvas;
    @FXML
    public Canvas clueCanvas;
    @FXML
    public Pane pane;

    Game game;
    @FXML
    Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void initialize(Game input) {
        pane.setPrefSize(canvas.getWidth(), canvas.getHeight());
        this.game = createNewInstance(input);
        switch (game) {
            case Sudoku sudoku -> {
                SudokuCell[][] board = sudoku.getSolutionBoard();
                Arrays.stream(flattenBoard(board)).filter(Objects::nonNull).forEach(cell -> cell.setChangeable(false));
                sudoku.setBoard(board);
            }
            case Maze maze -> {
                int currentFloor = maze.getCurrentFloor();
                MazeGrid grid = maze.getMazeGridFloors().get(currentFloor).getMazeGrid();
                var path = grid.getPath();
                List<MazeCell> sPath = path.getSolutionPath();
                path.setShowTravelPath(true);
                path.getActivePath().clear();
                path.getActivePath().addAll(sPath);
            }
            case Bridge bridge -> bridge.setIslandBridges(bridge.getSolutionBridges());
            case Shikaku shikaku -> shikaku.setRectangles(shikaku.getSolutionRectangles());
            default -> {
            }
        }

        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> {
            if (pane.getWidth() > 0 && pane.getHeight() > 0) {
                updateLayoutAndRedraw(game);
            }
        };

        pane.widthProperty().addListener(resizeListener);
        pane.heightProperty().addListener(resizeListener);

        updateLayoutAndRedraw(game);
    }

    private void updateLayoutAndRedraw(Game targetGame) {
        if (targetGame == null || pane == null) return;

        double fullWidth = pane.getWidth();
        double fullHeight = pane.getHeight();

        boolean hasOutsideClues = targetGame instanceof Sudoku sudoku && hasOutsideClues(sudoku);

        double paddingX = hasOutsideClues ? fullWidth * 0.05 : 0;
        double paddingY = hasOutsideClues ? fullHeight * 0.05 : 0;

        double gridWidth = fullWidth - 2 * paddingX;
        double gridHeight = fullHeight - 2 * paddingY;

        if (clueCanvas != null) {
            clueCanvas.setWidth(fullWidth);
            clueCanvas.setHeight(fullHeight);
            clueCanvas.setLayoutX(0);
            clueCanvas.setLayoutY(0);

            clueCanvas.getGraphicsContext2D().setFill(Color.web("#1e1e1e"));
            clueCanvas.getGraphicsContext2D().fillRect(0, 0, fullWidth, fullHeight);
        }

        if (canvas != null) {
            canvas.setWidth(gridWidth);
            canvas.setHeight(gridHeight);
            canvas.setLayoutX(paddingX);
            canvas.setLayoutY(paddingY);

            switch (targetGame) {
                case Sudoku sudoku -> SudokuRenderer.renderFullBoard(canvas, sudoku, false);
                case Bridge bridge -> BridgeRenderer.render(canvas, bridge);
                case Maze maze -> {
                    int currentFloor = maze.getCurrentFloor();
                    int totalFloors = maze.getMazeGridFloors().size();
                    MazeGrid grid = maze.getMazeGridFloors().get(currentFloor).getMazeGrid();

                    MazeRenderer.renderGrid(canvas, grid, currentFloor, totalFloors, false, game.getPlayer());
                    if (grid.getPath() != null) {
                        MazeRenderer.renderPath(canvas, grid.getPath(), grid, false);
                    }
                }
                case Shikaku shikaku -> ShikakuRenderer.render(canvas, shikaku);
                default -> {
                }
            }
        }

        if (hasOutsideClues && clueCanvas != null && targetGame instanceof Sudoku sudoku) {
            int size = sudoku.getType().getGridSize();
            double cellW = gridWidth / size;
            double cellH = gridHeight / size;

            ConstraintRenderer.drawOutsideModifiers(
                    clueCanvas.getGraphicsContext2D(), sudoku.getModifiers(), size,
                    cellW, cellH, paddingX, paddingY, gridWidth, gridHeight, false
            );
        }
    }


    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
