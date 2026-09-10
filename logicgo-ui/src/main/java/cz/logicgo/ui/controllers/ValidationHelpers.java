package cz.logicgo.ui.controllers;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;

import java.util.function.UnaryOperator;

import static cz.logicgo.core.misc.Messages.getFormatted;


public class ValidationHelpers {

    public static void setError(TextField field, String message) {
        if (field == null) return;
        field.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        field.setTooltip(new Tooltip(message));
    }

    public static void clearError(TextField field) {
        if (field == null) return;
        field.setStyle(null);
        field.setTooltip(null);
    }

    public static boolean validateDimRange(TextField field, int maxDim, String errorKeyEmpty, String errorKeyFormat) {
        if (field == null) return true;
        String t = field.getText();

        if (t == null || t.isEmpty()) {
            setError(field, getFormatted(errorKeyEmpty));
            return false;
        }

        try {
            int v = Integer.parseInt(t);
            if (v < 1 || v > maxDim) {
                setError(field, getFormatted(errorKeyEmpty));
                return false;
            } else {
                clearError(field);
                return true;
            }
        } catch (NumberFormatException e) {
            setError(field, getFormatted(errorKeyFormat));
            return false;
        }
    }

    public static void setupNumericFiltersAndValidation(
            TextField widthField,
            TextField heightField,
            CheckBox sameDimCheckBox,
            TextField extraField,
            int maxDim,
            String errorKeyEmpty,
            String errorKeyFormat) {

        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) return change;
            return null;
        };

        if (widthField != null) {
            widthField.setTextFormatter(new TextFormatter<>(integerFilter));
            widthField.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV) validateDimRange(widthField, maxDim, errorKeyEmpty, errorKeyFormat);
            });
        }

        if (heightField != null) {
            heightField.setTextFormatter(new TextFormatter<>(integerFilter));
            heightField.focusedProperty().addListener((obs, oldV, newV) -> {
                if (!newV && !heightField.isDisabled())
                    validateDimRange(heightField, maxDim, errorKeyEmpty, errorKeyFormat);
            });
        }

        if (extraField != null) {
            extraField.setTextFormatter(new TextFormatter<>(integerFilter));
        }

        if (sameDimCheckBox != null) {
            sameDimCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal && heightField != null && widthField != null) {
                    heightField.setText(widthField.getText());
                    heightField.setDisable(true);
                    clearError(heightField);
                } else if (heightField != null) {
                    heightField.setDisable(false);
                    validateDimRange(heightField, maxDim, errorKeyEmpty, errorKeyFormat);
                }
            });

            if (widthField != null && heightField != null) {
                widthField.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (sameDimCheckBox.isSelected()) heightField.setText(newVal);
                });
            }
        }
    }
}
