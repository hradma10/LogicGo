package cz.logicgo.ui.utils;

import javafx.scene.paint.Color;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public final class GameUiUtils {

    private static final List<Color> DEF_COLORS = List.of(
            Color.rgb(240, 90, 90, 1.0),
            Color.rgb(255, 160, 90, 1.0),
            Color.rgb(255, 210, 80, 1.0),
            Color.rgb(180, 230, 100, 1.0),
            Color.rgb(80, 220, 220, 1.0),
            Color.rgb(110, 170, 245, 1.0),
            Color.rgb(180, 130, 255, 1.0),
            Color.rgb(255, 120, 180, 1.0),
            Color.rgb(200, 110, 200, 1.0)
    );

    private GameUiUtils() {}

    public static List<Color> getDefaultColorTheme() {
        return DEF_COLORS;
    }

    public static void openFile(String path) {
        if (path == null || path.isBlank()) return;

        File file = new File(path);
        if (!file.exists()) {
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            } else {
            }
        } catch (IOException e) {
        }
    }
}
