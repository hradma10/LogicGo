package cz.logicgo.ui.controllers.settingsControllers;

import cz.logicgo.core.entity.user.User;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ColorSettingsUIGenerator {

    public static Node createColorControl(List<Color> colorBuffer, User user) {
        List<Color> currentPalette;

        if (!colorBuffer.isEmpty()) {
            currentPalette = new ArrayList<>(colorBuffer);
        } else {
            currentPalette = user.getUserColors().stream().map(Color::web).collect(Collectors.toList());
        }

        FlowPane palettePane = new FlowPane(5, 5);
        palettePane.setMaxWidth(350);

        for (int i = 0; i < currentPalette.size(); i++) {
            final int index = i;
            ColorPicker picker = new ColorPicker(currentPalette.get(i));
            picker.getStyleClass().add("settings-color-picker");
            picker.setPrefWidth(125);

            picker.setOnAction(e -> {
                currentPalette.set(index, picker.getValue());
                colorBuffer.clear();
                colorBuffer.addAll(currentPalette);
            });

            palettePane.getChildren().add(picker);
        }

        return palettePane;
    }
}
