package cz.logicgo.ui.controllers;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.ui.StartOfApp;
import cz.logicgo.ui.controllers.screenControllers.LoginController;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.controllers.screenControllers.ProfileSelectorController;
import cz.logicgo.ui.controllers.screenControllers.RegisterController;
import cz.logicgo.ui.misc.OpenConfigManager.WindowConfig;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import static cz.logicgo.core.misc.Messages.getFormatted;

public class Openers {

    private static final int MIN_WIDTH = 1410;
    private static final int MIN_HEIGHT = 900;
    private static final int INITIAL_WIDTH = 1410;
    private static final int INITIAL_HEIGHT = 900;

    private static final int MAX_WINDOWED_WIDTH = 1600;
    private static final int MAX_WINDOWED_HEIGHT = 1000;

    private static final String GLOBAL_CSS;

    static {
        URL cssResource = StartOfApp.class.getResource("/cz/logicgo/ui/newStyle/style.css");
        GLOBAL_CSS = cssResource != null ? cssResource.toExternalForm() : null;
    }

    public static void applyGlobalStyle(Scene scene) {
        if (scene != null && GLOBAL_CSS != null && !scene.getStylesheets().contains(GLOBAL_CSS)) {
            scene.getStylesheets().add(GLOBAL_CSS);
        }
    }

    private static double[] clampDimensions(double width, double height) {
        double clampedW = Math.min(Math.max(width, MIN_WIDTH), MAX_WINDOWED_WIDTH);
        double clampedH = Math.min(Math.max(height, MIN_HEIGHT), MAX_WINDOWED_HEIGHT);
        return new double[]{clampedW, clampedH};
    }

    public static void openProfileSelector(Stage stage, WindowConfig windowConfig) throws IOException {
        FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/profile_selector.fxml"));
        Parent root = loader.load();
        ProfileSelectorController controller = loader.getController();
        controller.setStage(stage);
        controller.loadProfiles();

        applyStageProperties(stage, root, windowConfig, true);

        if (!stage.isShowing()) {
            stage.show();
        }
    }

    public static void openMainScreen(User user, Stage stage, WindowConfig windowConfig) throws Exception {
        FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/main_screen.fxml"));
        Parent root = loader.load();
        MainScreenController controller = loader.getController();
        controller.setStage(stage);

        controller.initialize(user);

        applyStageProperties(stage, root, windowConfig, false);

        if (!stage.isShowing()) {
            stage.show();
        }
    }

    public static void openProfileSelectorRunning(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/profile_selector.fxml"));
            Parent root = loader.load();
            ProfileSelectorController controller = loader.getController();
            controller.setStage(stage);
            controller.loadProfiles();

            applyStageProperties(stage, root, null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openLoginScreen(User user, Stage stage, WindowConfig windowConfig) throws IOException {
        FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/login_screen.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setStage(stage);
        controller.setUser(user);

        applyStageProperties(stage, root, windowConfig, false);

        if (!stage.isShowing()) {
            stage.show();
        }
    }

    public static void openRegisterScreen(Stage stage, WindowConfig windowConfig) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/register_screen.fxml"));
            Parent root = loader.load();
            RegisterController controller = loader.getController();
            controller.setStage(stage);

            applyStageProperties(stage, root, windowConfig, false);

            if (!stage.isShowing()) {
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void applyStageProperties(Stage stage, Parent root, WindowConfig windowConfig, boolean canCenter) {
        double width, height, x, y;
        boolean maximized = false;
        boolean shouldCenter = false;

        Scene existingScene = stage.getScene();
        if (existingScene != null) {
            width = existingScene.getWidth();
            height = existingScene.getHeight();
            x = stage.getX();
            y = stage.getY();
            maximized = stage.isMaximized();
        } else if (windowConfig != null) {
            width = windowConfig.getWidth();
            height = windowConfig.getHeight();
            x = windowConfig.getX();
            y = windowConfig.getY();
            maximized = windowConfig.isMaximized();
        } else {
            width = INITIAL_WIDTH;
            height = INITIAL_HEIGHT;
            x = 100;
            y = 100;
            shouldCenter = canCenter;
        }

        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setTitle(getFormatted("app.name"));

        setMainAppIcon(stage);

        Scene scene;
        if (maximized) {
            scene = new Scene(root);
            applyGlobalStyle(scene);
            stage.setScene(scene);
            stage.setMaximized(true);
        } else {
            double[] dim = clampDimensions(width, height);
            scene = new Scene(root, dim[0], dim[1]);
            applyGlobalStyle(scene);
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.setX(x);
            stage.setY(y);
            if (shouldCenter) {
                stage.centerOnScreen();
            }
        }

        javafx.application.Platform.runLater(() -> {
            root.requestLayout();
            if (stage.getScene() != null) {
                stage.getScene().getRoot().requestLayout();
            }
        });

        enableDarkMode(stage);
    }

    private static void setMainAppIcon(Stage stage) {
        if (stage.getIcons().isEmpty()) {
            int[] sizes = {16, 24, 32, 48, 64, 128, 256, 512};
            for (int size : sizes) {
                var is = StartOfApp.class.getResourceAsStream("/cz/logicgo/ui/images/icon/app_icon_" + size + ".png");
                if (is != null) {
                    stage.getIcons().add(new Image(is));
                }
            }
        }
    }

    private static void enableDarkMode(Stage stage) {
        javafx.application.Platform.runLater(() -> {
            if (stage.isShowing()) {
                WindowsDarkModeHelper.enableDarkMode(stage);
            } else {
                stage.setOnShown(e -> WindowsDarkModeHelper.enableDarkMode(stage));
            }
        });
    }

    public static void darkModeIconSet(Stage stage) {
        setMainAppIcon(stage);

        stage.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) {
                javafx.application.Platform.runLater(() -> WindowsDarkModeHelper.enableDarkMode(stage));
            }
        });

        Scene scene = stage.getScene();
        if (scene != null) {
            applyGlobalStyle(scene);
        }
    }
}
