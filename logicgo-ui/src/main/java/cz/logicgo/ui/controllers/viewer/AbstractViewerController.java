package cz.logicgo.ui.controllers.viewer;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.Messages;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.ui.controllers.gameControllers.choosers.ViewMode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.controlsfx.control.SegmentedButton;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static cz.logicgo.ui.misc.SvgLoader.setIcon;


public abstract class AbstractViewerController<T> implements Initializable {

    @FXML
    public Button prevButton;
    @FXML
    public Button nextButton;
    @FXML
    public SegmentedButton viewToggleGroup;
    @FXML
    public ToggleButton layoutsButton;
    @FXML
    public ToggleButton favouritesButton;
    @FXML
    public ToggleButton customsButton;
    @FXML
    public Button favouriteButton;
    @FXML
    public Button deleteButton;
    @FXML
    public Button openEditorButton;
    @FXML
    public Canvas canvas;
    @FXML
    public Label statusLabel;
    @FXML
    public Label infoLabel;
    @FXML
    public Button okButton;

    protected UserService userService = new UserService();
    protected Stage stage;
    protected User user;

    protected List<T> layouts = new ArrayList<>();
    protected List<T> favourites = new ArrayList<>();
    protected List<T> customs = new ArrayList<>();

    protected int currentIndexLayouts = 0;
    protected int currentIndexFavourites = 0;
    protected int currentIndexCustoms = 0;

    protected ViewMode currentViewMode = ViewMode.LAYOUTS;

    protected abstract void drawSpecific(T item, GraphicsContext gc, double availableWidth, double availableHeight);

    protected abstract void refreshLists();

    protected abstract void onOk(T item);

    protected abstract void updateLabels(T item, int index, int totalSize);

    protected boolean isFavourite(T item) {
        return false;
    }

    protected void toggleFavouriteLogic(T item, boolean currentlyFavourite) {
    }

    protected void onDelete(T item) {
    }

    protected void openEditorWindow() {
    }

    protected void setLabelsAndIcons() {
        setIcon(prevButton, "/cz/logicgo/ui/images/svg/arrow-left.svg");
        setIcon(nextButton, "/cz/logicgo/ui/images/svg/arrow-right.svg");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (prevButton != null) prevButton.setOnAction(_ -> navigate(-1));
        if (nextButton != null) nextButton.setOnAction(_ -> navigate(1));
        if (okButton != null) okButton.setOnAction(_ -> handleOk());
        if (favouriteButton != null) favouriteButton.setOnAction(_ -> handleFavourite());
        if (deleteButton != null) deleteButton.setOnAction(_ -> handleDelete());
        if (openEditorButton != null) openEditorButton.setOnAction(_ -> openEditorWindow());
    }

    protected void updateToggleGroupVisibility() {
        int visibleCount = 0;
        if (layoutsButton != null && layoutsButton.isVisible()) visibleCount++;
        if (favouritesButton != null && favouritesButton.isVisible()) visibleCount++;
        if (customsButton != null && customsButton.isVisible()) visibleCount++;

        if (viewToggleGroup != null) {
            boolean showGroup = visibleCount > 1;
            viewToggleGroup.setVisible(showGroup);
            viewToggleGroup.setManaged(showGroup);
        }
    }


    protected void initBase(User user) {
        this.user = user;
        setLabelsAndIcons();
        setupToggleGroup();

        if (canvas != null && canvas.getParent() instanceof Pane canvasPane) {
            canvasPane.widthProperty().addListener((obs, oldVal, newVal) -> drawCurrent());
            canvasPane.heightProperty().addListener((obs, oldVal, newVal) -> drawCurrent());
        }

        refreshLists();
        drawCurrent();
    }

    private void setupToggleGroup() {
        ToggleGroup group = null;
        if (viewToggleGroup != null) {
            group = viewToggleGroup.getToggleGroup();
        }

        if (group == null && layoutsButton != null) {
            group = new ToggleGroup();
            if (layoutsButton != null) layoutsButton.setToggleGroup(group);
            if (favouritesButton != null) favouritesButton.setToggleGroup(group);
            if (customsButton != null) customsButton.setToggleGroup(group);
        }

        if (group != null) {
            group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    if (oldVal != null) oldVal.setSelected(true);
                    return;
                }

                if (newVal == layoutsButton) currentViewMode = ViewMode.LAYOUTS;
                else if (newVal == favouritesButton) {
                    currentViewMode = ViewMode.FAVOURITES;
                    refreshLists();
                    currentIndexFavourites = 0;
                } else if (newVal == customsButton) {
                    currentViewMode = ViewMode.CUSTOM;
                    refreshLists();
                    currentIndexCustoms = 0;
                }

                drawCurrent();
            });
        }
    }

    protected List<T> getCurrentList() {
        return switch (currentViewMode) {
            case LAYOUTS -> layouts;
            case FAVOURITES -> favourites;
            case CUSTOM -> customs;
        };
    }

    protected int getCurrentIndex() {
        return switch (currentViewMode) {
            case LAYOUTS -> currentIndexLayouts;
            case FAVOURITES -> currentIndexFavourites;
            case CUSTOM -> currentIndexCustoms;
        };
    }

    private void setCurrentIndex(int newIndex) {
        switch (currentViewMode) {
            case LAYOUTS -> currentIndexLayouts = newIndex;
            case FAVOURITES -> currentIndexFavourites = newIndex;
            case CUSTOM -> currentIndexCustoms = newIndex;
        }
    }

    private void navigate(int direction) {
        List<T> currentList = getCurrentList();
        if (currentList.isEmpty()) return;

        int newIndex = getCurrentIndex() + direction;
        if (newIndex < 0) newIndex = currentList.size() - 1;
        if (newIndex >= currentList.size()) newIndex = 0;

        setCurrentIndex(newIndex);
        drawCurrent();
    }

    private void handleOk() {
        List<T> currentList = getCurrentList();
        if (!currentList.isEmpty()) {
            onOk(currentList.get(getCurrentIndex()));
        }
        if (stage != null) stage.close();
    }

    private void handleFavourite() {
        List<T> currentList = getCurrentList();
        if (currentList.isEmpty()) return;

        T currentItem = currentList.get(getCurrentIndex());
        boolean currentlyFavourite = isFavourite(currentItem);

        toggleFavouriteLogic(currentItem, currentlyFavourite);
        userService.updateUser(user);

        if (currentViewMode != ViewMode.FAVOURITES) {
            refreshLists();
        }
        drawCurrent();
    }

    private void handleDelete() {
        List<T> currentList = getCurrentList();
        if (currentList.isEmpty()) return;

        int index = getCurrentIndex();
        T toRemove = currentList.get(index);
        onDelete(toRemove);

        if (index >= currentList.size() && index > 0) {
            setCurrentIndex(currentList.size() - 1);
        }
        drawCurrent();
    }

    protected void drawCurrent() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        List<T> currentList = getCurrentList();

        if (currentList.isEmpty()) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            if (favouriteButton != null) favouriteButton.setDisable(true);
            if (deleteButton != null) deleteButton.setDisable(true);
            if (okButton != null) okButton.setDisable(true);
            updateLabels(null, 0, 0);
            return;
        }

        if (okButton != null) okButton.setDisable(false);
        if (deleteButton != null) deleteButton.setDisable(false);

        T data = currentList.get(getCurrentIndex());
        updateLabels(data, getCurrentIndex(), currentList.size());
        updateFavouriteButton(data);

        if (canvas.getParent() instanceof Pane canvasPane) {
            double parentW = canvasPane.getWidth();
            double parentH = canvasPane.getHeight();
            if (parentW > 0 && parentH > 0) {
                drawSpecific(data, gc, parentW, parentH);
            }
        }
    }

    private void updateFavouriteButton(T item) {
        if (favouriteButton == null) return;
        favouriteButton.setDisable(false);
        if (isFavourite(item)) {
            favouriteButton.setText(Messages.getFormatted("sudoku.button.unfavourite"));
        } else {
            favouriteButton.setText(Messages.getFormatted("sudoku.button.favourite"));
        }
    }
}
