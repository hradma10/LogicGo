package cz.logicgo.ui.controllers.viewer;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.gameClasses.viewers.SudokuPattern;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.algorithms.sudoku.custom.CustomLayoutsLoader;
import cz.logicgo.ui.controllers.Openers;
import cz.logicgo.ui.controllers.gameControllers.choosers.ChosenPattern;
import cz.logicgo.ui.controllers.gameControllers.choosers.IChooser;
import cz.logicgo.ui.controllers.gameControllers.choosers.ViewMode;
import cz.logicgo.ui.renderers.sudoku.ConstraintRenderer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.Openers.darkModeIconSet;
import static cz.logicgo.ui.controllers.screenControllers.RedrawCanvasFunctions.strokeBoard;

public class SudokuCustomViewerController extends AbstractViewerController<SudokuPatternLayout> {

    private SudokuVariant variant;
    private IChooser wrapper;
    private int gridDimension;

    public void initialize(User user, int size, SudokuVariant variant, IChooser wrapper) {
        this.variant = variant;
        this.wrapper = wrapper;
        this.gridDimension = size;

        super.initBase(user);
    }

    @Override
    protected void setLabelsAndIcons() {
        if (layoutsButton != null) layoutsButton.setText(getFormatted("viewer.layouts.pattern"));
        if (favouritesButton != null) {
            favouritesButton.setVisible(false);
            favouritesButton.setManaged(false);
        }
        if (favouriteButton != null) {
            favouriteButton.setVisible(false);
            favouriteButton.setManaged(false);
        }
        if (customsButton != null) customsButton.setText(getFormatted("viewer.layouts.custom"));
    }

    @Override
    protected void onDelete(SudokuPatternLayout item) {
        if (item == null) return;
        user.getCustomPatterns().removeIf(p -> p.getLayout().equals(item));
        userService.updateUser(user);
        refreshLists();
    }

    @Override
    protected void refreshLists() {
        if (user == null) return;

        layouts.clear();
        favourites.clear();
        customs.clear();

        if (variant == SudokuVariant.PATTERNED) {
            layouts.addAll(CustomLayoutsLoader.getPatternsForSize(gridDimension).stream()
                    .filter(pattern -> !pattern.getName().contains("offset"))
                    .toList());

            var distinctEntities = user.getCustomPatterns().stream().distinct().toList();

            customs.addAll(distinctEntities.stream()
                    .filter(e -> e.getLayout().getPattern().length == gridDimension)
                    .map(SudokuPattern::getLayout)
                    .toList());
        }
    }

    @Override
    protected void openEditorWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cz/logicgo/ui/windows/gameChoice/sudoku_custom_editor.fxml"));
            SudokuCustomEditorController editorController = new SudokuCustomEditorController();
            loader.setController(editorController);

            Parent root = loader.load();
            root.getStyleClass().add("root");
            Stage editorStage = new Stage();

            editorController.setStage(editorStage);
            editorController.initialize(user, gridDimension);

            Openers.applyGlobalStyle(editorStage.getScene());

            editorStage.setTitle(getFormatted("sudoku.editor.pattern.open"));
            editorStage.setScene(new Scene(root, 800, 600));

            editorStage.setMinWidth(800);
            editorStage.setMinHeight(600);
            editorStage.setMaxWidth(1300);
            editorStage.setMaxHeight(900);
            editorStage.initModality(Modality.APPLICATION_MODAL);

            darkModeIconSet(editorStage);
            editorStage.showAndWait();
            refreshLists();

            if (customsButton != null) {
                customsButton.setSelected(true);
                if (!customs.isEmpty()) currentIndexCustoms = customs.size() - 1;
            }
            drawCurrent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean isFavourite(SudokuPatternLayout item) {
        return false;
    }

    @Override
    protected void toggleFavouriteLogic(SudokuPatternLayout item, boolean currentlyFavourite) {
    }

    @Override
    protected void onOk(SudokuPatternLayout item) {
        if (wrapper instanceof ChosenPattern chosenPattern) {
            chosenPattern.setPatternLayout(item);
        }
    }

    @Override
    protected void updateLabels(SudokuPatternLayout item, int index, int totalSize) {
        if (statusLabel == null) return;

        if (deleteButton != null) {
            deleteButton.setVisible(currentViewMode == ViewMode.CUSTOM);
        }

        if (item == null) {
            statusLabel.setText(getFormatted("sudoku.irregular.no_layouts"));
        } else {
            statusLabel.setText(getFormatted("sudoku.irregular.layout", index + 1, totalSize));
        }
        if (infoLabel != null) infoLabel.setText("");
    }

    @Override
    protected void drawSpecific(SudokuPatternLayout item, GraphicsContext gc, double parentW, double parentH) {
        double padding = 20.0;
        double availableSize = Math.min(parentW, parentH) - padding;
        double squareSize = Math.max(10, availableSize);

        var canvas = gc.getCanvas();
        if (Math.abs(canvas.getWidth() - squareSize) > 0.01 || Math.abs(canvas.getHeight() - squareSize) > 0.01) {
            canvas.setWidth(squareSize);
            canvas.setHeight(squareSize);
        }

        gc.clearRect(0, 0, squareSize, squareSize);

        double calculatedCellSize = squareSize / gridDimension;

        if (variant == SudokuVariant.PATTERNED && item != null) {
            List<Color> colors = user.getUserColors().stream().map(Color::web).toList();
            ConstraintRenderer.drawPatternLayout(gc, item, calculatedCellSize, calculatedCellSize, 0, 0, colors);

            SudokuRegionLayout regionLayout = CustomLayoutsLoader.getBasicLayout(gridDimension);
            strokeBoard(regionLayout.getRegions(), calculatedCellSize, calculatedCellSize, gc, SudokuVariant.PATTERNED, item, true);
        }
    }
}
