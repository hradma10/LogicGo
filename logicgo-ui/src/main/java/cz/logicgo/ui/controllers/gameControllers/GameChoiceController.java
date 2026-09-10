package cz.logicgo.ui.controllers.gameControllers;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.factoryInit.bridge.BridgeInit;
import cz.logicgo.core.factoryInit.maze.MazeInit;
import cz.logicgo.core.factoryInit.shikaku.ShikakuInit;
import cz.logicgo.core.factoryInit.sudoku.SudokuInit;
import cz.logicgo.core.gameClasses.favorites.*;
import cz.logicgo.core.misc.enums.OpenType;
import cz.logicgo.core.misc.enums.TabType;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.util.generate.SeedCreator;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.ui.controllers.ControllerClosable;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.misc.ImagePreLoader;
import cz.logicgo.ui.misc.tabChoosingClasses.FavoriteGameWrapper;
import cz.logicgo.ui.misc.tabChoosingClasses.NewGameWrapper;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static cz.logicgo.core.misc.Messages.getFormatted;

public class GameChoiceController implements ControllerClosable {

    private final UserService userService = new UserService();
    @FXML
    public TilePane favoritesTilePane;
    @FXML
    public VBox sudokuChoiceBox;
    @FXML
    public VBox mazeChoiceBox;
    @FXML
    public VBox bridgeChoiceBox;
    @FXML
    public VBox shikakuChoiceBox;
    @FXML
    public VBox content;
    public Label labelSudoku;
    public Label labelShikaku;
    public Label labelFavorite;
    public Label labelMaze;
    public Label labelBridges;
    public VBox favoritesCard;
    @FXML
    private TextField seedInputField;
    @FXML
    private Button btnLaunchSeed;
    @FXML
    private Label seedErrorLabel;
    private User user;
    private MainScreenController mainController;

    public void loadNames() {
        labelSudoku.setText(getFormatted("game.menu.sudoku"));
        labelFavorite.setText(getFormatted("game.menu.favorite"));
        labelMaze.setText(getFormatted("game.menu.maze"));
        labelBridges.setText(getFormatted("game.menu.bridges"));
        labelShikaku.setText(getFormatted("game.menu.shikaku"));
    }

    @FXML
    private void handleLaunchSeed() {
        String seedText = seedInputField.getText();
        if (seedText == null || seedText.isBlank()) {
            showSeedError(getFormatted("game.seed.error.empty"));
            return;
        }

        try {
            GameInit gameInit = SeedCreator.parseSeed(seedText.trim(), user);

            if (gameInit != null) {
                seedErrorLabel.setVisible(false);
                seedErrorLabel.setManaged(false);

                gameInit.setPlayer(user);
                gameInit.setOpenType(OpenType.SEEDED);
                TabType tabType = switch (gameInit) {
                    case SudokuInit _ -> TabType.SUDOKU_SETTINGS;
                    case BridgeInit _ -> TabType.BRIDGE_SETTINGS;
                    case MazeInit _ -> TabType.MAZE_SETTINGS;
                    case ShikakuInit _ -> TabType.SHIKAKU_SETTINGS;
                    default -> throw new IllegalStateException("Unexpected value: " + gameInit);
                };
                mainController.openTab(tabType, new NewGameWrapper(gameInit));
            } else {
                showSeedError(getFormatted("game.seed.error.unrecognized"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showSeedError(getFormatted("game.seed.error.invalid"));
        }
    }

    private void showSeedError(String errorMessage) {
        seedErrorLabel.setText(errorMessage);
        seedErrorLabel.setVisible(true);
        seedErrorLabel.setManaged(true);
    }

    public void initialize(User user, MainScreenController mainController) {
        this.user = user;
        this.mainController = mainController;
        loadNames();
        createTypes();
        buildFavoritesSection();

        FavoriteEventManager.addChangeListener((obs, oldVal, newVal) -> {
            Platform.runLater(this::buildFavoritesSection);
        });
    }

    public void refreshFavorites() {
        Platform.runLater(this::buildFavoritesSection);
    }

    @FXML
    private void createTypes() {
        ReadOnlyDoubleProperty sudokuWidthProp = sudokuChoiceBox.widthProperty();

        sudokuChoiceBox.getChildren().addAll(
                createGameCategory(getFormatted("game.sudoku.cat.size_shape"), List.of(
                        SudokuVariant.CLASSIC, SudokuVariant.IRREGULAR
                ), sudokuWidthProp, "cat_size_shape"),

                createGameCategory(getFormatted("game.sudoku.cat.extra_regions"), List.of(
                        SudokuVariant.DIAGONAL, SudokuVariant.PATTERNED, SudokuVariant.OFFSET
                ), sudokuWidthProp, "cat_extra_regions"),

                createGameCategory(getFormatted("game.sudoku.cat.edge_relations"), List.of(
                        SudokuVariant.CONSECUTIVE, SudokuVariant.KROPKI, SudokuVariant.XV, SudokuVariant.GREATER_THAN
                ), sudokuWidthProp, "cat_edge_relations"),

                createGameCategory(getFormatted("game.sudoku.cat.intersections_math"), List.of(
                        SudokuVariant.GROUP_SUMS, SudokuVariant.QUADRUPLES, SudokuVariant.KILLER, SudokuVariant.VUDOKU
                ), sudokuWidthProp, "cat_intersections_math"),

                createGameCategory(getFormatted("game.sudoku.cat.external_clues"), List.of(
                        SudokuVariant.SKYSCRAPER, SudokuVariant.SANDWICH, SudokuVariant.X_SUMS
                ), sudokuWidthProp, "cat_external_clues"),

                createGameCategory(getFormatted("game.sudoku.cat.global_constraints"), List.of(
                        SudokuVariant.EVEN_ODD, SudokuVariant.BETWEEN, SudokuVariant.ANTI_CONSECUTIVE, SudokuVariant.ANTI_KING, SudokuVariant.ANTI_KNIGHT, SudokuVariant.ANTI_ALL
                ), sudokuWidthProp, "cat_global_constraints"),

                createGameCategory(getFormatted("game.sudoku.cat.multidoku"), List.of(
                        SudokuVariant.SAMURAI, SudokuVariant.TWODOKU, SudokuVariant.TRIPLEDOKU, SudokuVariant.CROSS, SudokuVariant.DOUBLEDOKU, SudokuVariant.COLUMNDOKU
                ), sudokuWidthProp, "cat_multidoku")
        );

        ReadOnlyDoubleProperty mazeWidthProp = mazeChoiceBox.widthProperty();
        TilePane mazeTilePane = createDirectGameGrid(List.of(
                MazeType.CLASSIC, MazeType.WALLS, MazeType.PORTAL, MazeType.WRAP_AROUND
                , MazeType.CHECKPOINT, MazeType.EXACT_STEPS, MazeType.PATTERN, MazeType.MULTI_LEVEL
        ), mazeWidthProp);

        startImageCyclingForMultiLevel(mazeTilePane);
        mazeChoiceBox.getChildren().add(mazeTilePane);

        ReadOnlyDoubleProperty bridgeWidthProp = bridgeChoiceBox.widthProperty();
        TilePane bridgeTilePane = createDirectGameGrid(List.of(
                BridgeType.CLASSIC, BridgeType.MULTIPLE
        ), bridgeWidthProp);
        bridgeChoiceBox.getChildren().add(bridgeTilePane);

        ReadOnlyDoubleProperty shikakuWidthProp = shikakuChoiceBox.widthProperty();
        TilePane shikakuTilePane = createDirectGameGrid(List.of(
                ShikakuType.CLASSIC, ShikakuType.OFF_BY_ONE
        ), shikakuWidthProp);
        shikakuChoiceBox.getChildren().add(shikakuTilePane);
    }

    private TilePane createDirectGameGrid(List<? extends Enum<?>> variants, ReadOnlyDoubleProperty widthProp) {
        TilePane tilePane = new TilePane();
        tilePane.setHgap(15);
        tilePane.setVgap(15);
        tilePane.setPrefColumns(3);
        tilePane.setAlignment(Pos.TOP_LEFT);

        for (Enum<?> variant : variants) {
            tilePane.getChildren().add(createVariantBox(variant, widthProp));
        }

        return tilePane;
    }

    private TitledPane createGameCategory(String title, List<? extends Enum<?>> variants, ReadOnlyDoubleProperty widthProp, String prefsKey) {
        TilePane tilePane = new TilePane();
        tilePane.setHgap(15);
        tilePane.setVgap(15);
        tilePane.setPrefColumns(3);
        tilePane.setAlignment(Pos.TOP_LEFT);

        for (Enum<?> variant : variants) {
            tilePane.getChildren().add(createVariantBox(variant, widthProp));
        }

        TitledPane titledPane = new TitledPane(title, tilePane);
        titledPane.setAnimated(false);

        titledPane.setCollapsible(false);
        titledPane.setExpanded(true);

        return titledPane;
    }

    private void buildFavoritesSection() {
        favoritesTilePane.getChildren().clear();

        List<GameFavorite> favoriteConfigs = user.getFavoriteComb();

        if (favoriteConfigs == null || favoriteConfigs.isEmpty()) {
            favoritesCard.setManaged(false);
            favoritesCard.setVisible(false);
            labelFavorite.setManaged(false);
            labelFavorite.setVisible(false);
        } else {
            favoritesCard.setManaged(true);
            favoritesCard.setVisible(true);
            labelFavorite.setManaged(true);
            labelFavorite.setVisible(true);

            favoriteConfigs.sort(Comparator.comparing(f -> f.getClass().getSimpleName()));

            ReadOnlyDoubleProperty favWidthProp = favoritesTilePane.widthProperty();
            favoriteConfigs.stream().map(fav ->
                    createFavoriteConfigBox(fav, favWidthProp)).forEachOrdered(node ->
                    favoritesTilePane.getChildren().add(node));
        }
    }

    private Node createFavoriteConfigBox(GameFavorite favorite, ReadOnlyDoubleProperty widthProp) {
        if (favorite == null) return null;

        HBox root = new HBox(10);
        root.setAlignment(Pos.CENTER_LEFT);
        root.getStyleClass().add("favoriteItemBox");

        Image image = switch (favorite) {
            case BridgeFavorite bFav -> ImagePreLoader.get(bFav.getBridgeType());
            case MazeFavorite mFav -> ImagePreLoader.get(mFav.getMazeType());
            case SudokuFavorite sFav -> ImagePreLoader.get(sFav.getVariant());
            case ShikakuFavorite shFav -> ImagePreLoader.get(shFav.getShikakuType());
        };

        ImageView icon = new ImageView(image);
        icon.setFitWidth(32);
        icon.setFitHeight(32);

        Label titleLabel = new Label(favorite.toString());
        titleLabel.getStyleClass().add("favoriteItemLabel");

        root.getChildren().addAll(icon, titleLabel);

        root.setOnMouseClicked(e -> {
            try {
                TabType tabType = switch (favorite) {
                    case SudokuFavorite _ -> TabType.SUDOKU_SETTINGS;
                    case BridgeFavorite _ -> TabType.BRIDGE_SETTINGS;
                    case MazeFavorite _ -> TabType.MAZE_SETTINGS;
                    case ShikakuFavorite _ -> TabType.SHIKAKU_SETTINGS;
                };
                mainController.openTab(tabType, new FavoriteGameWrapper(favorite));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        });

        return root;
    }

    private Node createVariantBox(Enum<?> type, ReadOnlyDoubleProperty widthProp) {
        return createVariantBox(getTranslation(type), type, widthProp);
    }

    private Node createVariantBox(String description, Enum<?> type, ReadOnlyDoubleProperty widthProp) {
        Image image = ImagePreLoader.get(type);
        return buildRawGameBox(description, createImageView(image), widthProp, getOnClickRunnable(type));
    }


    private Runnable getOnClickRunnable(Enum<?> type) {
        return () -> {
            try {
                switch (type) {
                    case SudokuVariant s -> {
                        SudokuInit sInit = new SudokuInit().setSudokuVariant(s);
                        sInit.setOpenType(OpenType.NORMAL);
                        mainController.openTab(TabType.SUDOKU_SETTINGS, new NewGameWrapper(sInit));
                    }
                    case BridgeType b -> {
                        BridgeInit bInit = new BridgeInit().setBridgeType(b);
                        bInit.setOpenType(OpenType.NORMAL);
                        mainController.openTab(TabType.BRIDGE_SETTINGS,new NewGameWrapper(bInit));
                    }
                    case MazeType m -> {
                        MazeInit mInit = new MazeInit().setMazeType(m);
                        mainController.openTab(TabType.MAZE_SETTINGS, new NewGameWrapper(mInit));
                    }
                    case ShikakuType s -> {
                        ShikakuInit sInit = new ShikakuInit().setShikakuType(s);
                        mainController.openTab(TabType.SHIKAKU_SETTINGS, new NewGameWrapper(sInit));

                    }
                    default -> {
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException("Failed to open game settings", ex);
            }
        };
    }

    private Node buildRawGameBox(String description, Node graphicNode, ReadOnlyDoubleProperty parentWidthProperty, Runnable onClick) {
        StackPane root = new StackPane();
        root.getStyleClass().add("gameTypeBox");

        VBox box = new VBox(5);
        box.setAlignment(Pos.BOTTOM_CENTER);

        StackPane labelContainer = new StackPane();
        labelContainer.getStyleClass().add("labelContainer");

        Label name = new Label(description);
        name.getStyleClass().add("gameNameLabel");
        name.setStyle("-fx-text-alignment: center; -fx-alignment: center;");
        labelContainer.getChildren().add(name);

        box.getChildren().addAll(graphicNode, labelContainer);
        root.getChildren().add(box);

        addResponsiveListener(root, parentWidthProperty);
        setHoverTransitions(root);

        root.setOnMouseClicked(e -> onClick.run());

        return root;
    }

    private Node createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(160);
        imageView.setFitHeight(160);

        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.setCacheHint(CacheHint.QUALITY);

        imageView.getStyleClass().add("gamePreviewImage");
        return imageView;
    }

    private void setHoverTransitions(StackPane root) {

        TranslateTransition moveUp = new TranslateTransition(Duration.millis(150), root);
        moveUp.setToY(-6.0);

        TranslateTransition moveDown = new TranslateTransition(Duration.millis(150), root);
        moveDown.setToY(0.0);

        root.setOnMouseEntered(e -> {
            moveDown.stop();
            moveUp.playFromStart();
        });

        root.setOnMouseExited(e -> {
            moveUp.stop();
            moveDown.playFromStart();
        });
    }

    private ImageView findImageView(Node node) {
        if (node instanceof ImageView) {
            return (ImageView) node;
        } else if (node instanceof Pane pane) {
            for (Node child : pane.getChildren()) {
                ImageView found = findImageView(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void addResponsiveListener(StackPane root, ReadOnlyDoubleProperty widthProp) {
        widthProp.addListener((obs, oldWidth, newWidth) -> {
            double width = newWidth.doubleValue();
            double scale = 1.0;
            if (width < 800) {
                scale = 0.8 + 0.2 * (width / 800);
                scale = Math.max(scale, 0.8);
            }
            root.setScaleX(scale);
            root.setScaleY(scale);
        });
    }

    private String getTranslation(Enum<?> type) {
        if (type instanceof SudokuVariant s) return s.getTranslation();
        if (type instanceof BridgeType b) return b.getTranslation();
        if (type instanceof MazeType m) return m.getTranslation();
        if (type instanceof ShikakuType s) {
            return s.getTranslation();
        }
        return type.toString();
    }

    @Override
    public void terminateAllActiveActions() {
    }

    public UserService getUserService() {
        return userService;
    }

    private void startImageCyclingForMultiLevel(TilePane spatialMazePane) {
        if (spatialMazePane == null) return;

        List<MazeType> cyclingTypes = List.of(
                MazeType.CLASSIC, MazeType.WALLS, MazeType.PORTAL, MazeType.WRAP_AROUND, MazeType.CHECKPOINT
        );

        for (Node node : spatialMazePane.getChildren()) {
            if (node instanceof StackPane rootBox) {
                ImageView imageView = findImageView(rootBox);

                if (imageView != null && (rootBox.toString().contains("MULTI_LEVEL") || getTranslation(MazeType.MULTI_LEVEL).equals(getLabelText(rootBox)))) {

                    Timeline timeline = new Timeline(
                            new KeyFrame(Duration.seconds(10), new EventHandler<>() {
                                private int index = 0;

                                @Override
                                public void handle(ActionEvent event) {
                                    MazeType nextType = cyclingTypes.get(index);
                                    Image nextImage = ImagePreLoader.get(nextType);

                                    if (nextImage == null || nextImage.isError()) {
                                        return;
                                    }

                                    FadeTransition fadeOut = new FadeTransition(Duration.millis(500), imageView);
                                    fadeOut.setToValue(0.2);
                                    fadeOut.setOnFinished(e -> {
                                        imageView.setImage(nextImage);
                                        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), imageView);
                                        fadeIn.setToValue(1.0);
                                        fadeIn.play();
                                    });
                                    fadeOut.play();

                                    index = (index + 1) % cyclingTypes.size();
                                }
                            })
                    );
                    timeline.setCycleCount(Animation.INDEFINITE);
                    timeline.play();
                    break;
                }
            }
        }
    }

    private String getLabelText(StackPane rootBox) {
        if (rootBox.getChildren().getFirst() instanceof VBox box) {
            if (box.getChildren().size() > 1 && box.getChildren().get(1) instanceof StackPane container) {
                if (!container.getChildren().isEmpty() && container.getChildren().getFirst() instanceof Label label) {
                    return label.getText();
                }
            }
        }
        return "";
    }
}
