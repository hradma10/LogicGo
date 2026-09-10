package cz.logicgo.ui.controllers.viewer;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.viewers.CustomMask;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.ui.StartOfApp;
import cz.logicgo.ui.controllers.Openers;
import cz.logicgo.ui.controllers.gameControllers.choosers.ChosenMask;
import cz.logicgo.ui.controllers.gameControllers.choosers.ViewMode;
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


public class MaskViewerController extends AbstractViewerController<CustomMask> {

    private ChosenMask wrapper;
    private TypeGame gameType;

    public void initialize(User user, ChosenMask wrapper, TypeGame gameType) {
        this.wrapper = wrapper;
        this.gameType = gameType;

        super.initBase(user);

        this.currentViewMode = ViewMode.CUSTOM;
        if (customsButton != null) {
            customsButton.setSelected(true);
        }
        drawCurrent();
    }

    @Override
    protected void setLabelsAndIcons() {
        if (layoutsButton != null) {
            layoutsButton.setVisible(false);
            layoutsButton.setManaged(false);
        }
        if (favouritesButton != null) {
            favouritesButton.setVisible(false);
            favouritesButton.setManaged(false);
        }
        if (favouriteButton != null) {
            favouriteButton.setVisible(false);
            favouriteButton.setManaged(false);
        }
        if (customsButton != null) customsButton.setText(getFormatted("viewer.layouts.custom"));

        updateToggleGroupVisibility();
    }

    @Override
    protected void onDelete(CustomMask item) {
        if (item == null) return;
        user.getCustomMasks().remove(item);
        userService.updateUser(user);
        refreshLists();
    }

    @Override
    protected void refreshLists() {
        if (user == null) return;

        layouts.clear();
        favourites.clear();
        customs.clear();

        List<CustomMask> filteredMasks = user.getCustomMasks().stream()
                .distinct()
                .toList();

        customs.addAll(filteredMasks);
    }

    @Override
    protected boolean isFavourite(CustomMask item) {
        return false;
    }

    @Override
    protected void toggleFavouriteLogic(CustomMask item, boolean currentlyFavourite) {
    }

    @Override
    protected void openEditorWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/gameChoice/maze_mask.fxml"));
            MaskEditorController editorController = new MaskEditorController();
            loader.setController(editorController);

            Parent root = loader.load();
            root.getStyleClass().add("root-pane");
            Stage editorStage = new Stage();

            editorController.setStage(editorStage);
            editorController.initialize(user, gameType);

            Openers.applyGlobalStyle(editorStage.getScene());

            editorStage.setTitle("Editor masky - " + gameType.getTranslation());
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
                currentViewMode = ViewMode.CUSTOM;
                if (!customs.isEmpty()) currentIndexCustoms = customs.size() - 1;
            }

            drawCurrent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onOk(CustomMask item) {
        if (item != null) {
            wrapper.setMask(item.getLayout());
        }
    }

    @Override
    protected void updateLabels(CustomMask item, int index, int totalSize) {
        if (statusLabel == null) return;
        if (item == null) {
            statusLabel.setText(getFormatted("maze.mask.no_layouts"));
            if (infoLabel != null) infoLabel.setText(getFormatted("mask.editor.create_new"));
        } else {
            statusLabel.setText(getFormatted("maze.mask.layout", index + 1, totalSize));
            boolean[][] layout = item.getLayout();
            if (infoLabel != null) {
                infoLabel.setText(String.format("%d x %d", layout[0].length, layout.length));
            }
        }
    }

    @Override
    protected void drawSpecific(CustomMask item, GraphicsContext gc, double parentW, double parentH) {
        boolean[][] layout = item.getLayout();
        if (layout == null || layout.length == 0 || layout[0].length == 0) return;

        int rows = layout.length;
        int cols = layout[0].length;

        double padding = 20.0;
        double availableW = Math.max(10, parentW - padding);
        double availableH = Math.max(10, parentH - padding);

        double currentCellSize = Math.min(availableW / cols, availableH / rows);

        double canvasWidth = cols * currentCellSize;
        double canvasHeight = rows * currentCellSize;

        var canvas = gc.getCanvas();
        if (Math.abs(canvas.getWidth() - canvasWidth) > 0.01 || Math.abs(canvas.getHeight() - canvasHeight) > 0.01) {
            canvas.setWidth(canvasWidth);
            canvas.setHeight(canvasHeight);
        }

        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                gc.setFill(layout[r][c] ? Color.WHITE : Color.web("#2d2d2d"));
                gc.fillRect(c * currentCellSize, r * currentCellSize, currentCellSize, currentCellSize);
            }
        }

        gc.setStroke(Color.web("#555555"));
        gc.setLineWidth(0.5);
        for (int i = 0; i <= cols; i++) gc.strokeLine(i * currentCellSize, 0, i * currentCellSize, canvasHeight);
        for (int i = 0; i <= rows; i++) gc.strokeLine(0, i * currentCellSize, canvasWidth, i * currentCellSize);
    }
}
