package cz.logicgo.ui.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class OpenConfigManager {

    private static final String sharedDataPath = System.getenv("APPDATA");
    private static final String appFolderName = "LogicGo";
    private static final String appFolderPath = sharedDataPath + File.separator + appFolderName;

    private static final String configFilePath = appFolderPath + File.separator + "app_config.json";

    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    static {
        try {
            Files.createDirectories(Path.of(appFolderPath));
        } catch (IOException e) {
            throw new RuntimeException("Could not create AppData folder", e);
        }
    }

    public static AppConfig loadConfig() {
        File file = new File(configFilePath);
        if (!file.exists()) return new AppConfig();
        try {
            return mapper.readValue(file, AppConfig.class);
        } catch (IOException e) {

            return new AppConfig();
        }
    }

    private static void saveConfig(AppConfig config) {
        try {
            mapper.writeValue(new File(configFilePath), config);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public static void setDefault(long id) {
        AppConfig config = loadConfig();
        config.setDefaultUserId(id);
        saveConfig(config);
    }

    public static Optional<Integer> readDefaultForCurrentPCUser() {
        AppConfig config = loadConfig();
        Long id = config.getDefaultUserId();
        if (id == null) {
            return Optional.empty();
        }
        return Optional.of(id.intValue());
    }

    public static void writeWindowPosition(Window window) {
        AppConfig config = loadConfig();
        WindowConfig winConfig = config.getWindow();

        winConfig.setX(window.getX());
        winConfig.setY(window.getY());
        winConfig.setWidth(window.getWidth());
        winConfig.setHeight(window.getHeight());
        winConfig.setMaximized(window instanceof Stage stage && stage.isMaximized());

        saveConfig(config);
    }

    public static WindowConfig readWindowPosition() {
        return loadConfig().getWindow();
    }


    public static class AppConfig {
        private Long defaultUserId = null;
        private WindowConfig window = new WindowConfig();

        public Long getDefaultUserId() {
            return defaultUserId;
        }

        public void setDefaultUserId(Long defaultUserId) {
            this.defaultUserId = defaultUserId;
        }

        public WindowConfig getWindow() {
            return window;
        }

        public void setWindow(WindowConfig window) {
            this.window = window;
        }
    }

    public static class WindowConfig {
        private double x = 100;
        private double y = 100;
        private double width = 1280;
        private double height = 720;
        private boolean maximized = false;

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public boolean isMaximized() {
            return maximized;
        }

        public void setMaximized(boolean maximized) {
            this.maximized = maximized;
        }
    }
}
