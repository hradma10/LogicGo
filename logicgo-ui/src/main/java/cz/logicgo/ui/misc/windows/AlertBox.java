package cz.logicgo.ui.misc.windows;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.gameControllers.AbstractGameController.getFormattedTime;


public class AlertBox {

    public static boolean initCloseApp() {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(AlertBox.class.
                getResource("/cz/logicgo/ui/newStyle/style.css")).toExternalForm());

        dialog.setTitle(getFormatted("confirmDialogTitle"));
        dialog.setHeaderText(getFormatted("closeBox.header"));

        dialog.setContentText(getFormatted("closeBox.content"));

        ButtonType buttonOK = new ButtonType(getFormatted("closeBox.okButton"), ButtonBar.ButtonData.FINISH);
        ButtonType buttonCancel = new ButtonType(getFormatted("cancelButton"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getButtonTypes().setAll(buttonOK, buttonCancel);
        Optional<ButtonType> result = dialog.showAndWait();

        return result.isPresent() && result.get() == buttonOK;
    }

    public static boolean initCloseWithRunningExports(long activeExportsCount) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(AlertBox.class.
                getResource("/cz/logicgo/ui/newStyle/style.css")).toExternalForm());

        dialog.setTitle(getFormatted("confirmDialogTitle"));
        dialog.setHeaderText("Probíhá export souborů (" + activeExportsCount + ")");
        dialog.setContentText("V aplikaci stále probíhá generování nebo export na pozadí. Pokud aplikaci ukončíte, tyto exporty budou zrušeny.");

        ButtonType buttonTerminate = new ButtonType("Ukončit a zrušit exporty", ButtonBar.ButtonData.FINISH);
        ButtonType buttonCancel = new ButtonType(getFormatted("cancelButton"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getButtonTypes().setAll(buttonTerminate, buttonCancel);
        Optional<ButtonType> result = dialog.showAndWait();

        return result.isPresent() && result.get() == buttonTerminate;
    }

    public static void errorBox() {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle("Chyba");
        dialog.setHeaderText("error.wrongPassword");

        ButtonType buttonOK = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);

        dialog.getButtonTypes().setAll(buttonOK);
        dialog.showAndWait();
    }

    public static void errorBoxGenerationTooMuch() {
        Alert dialog = new Alert(Alert.AlertType.ERROR);

        String title = getFormatted("validation.errorTitle") != null ? getFormatted("validation.errorTitle") : "Chyba";
        String header = getFormatted("validation.limitExceededHeader") != null ? getFormatted("validation.limitExceededHeader") : "Limit generování překročen";
        String content = getFormatted("validation.limitExceededContent") != null ? getFormatted("validation.limitExceededContent") : "Maximální povolený počet je 200.";

        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        ButtonType buttonOK = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.getButtonTypes().setAll(buttonOK);

        if (dialog.getDialogPane().getScene() != null) {
            dialog.getDialogPane().getScene().getRoot().getStyleClass().add("root");
        }

        dialog.showAndWait();
    }


    public static boolean initCloseGame() {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(AlertBox.class.
                getResource("/cz/logicgo/ui/newStyle/style.css")).toExternalForm());

        dialog.setTitle(getFormatted("confirmDialogTitle"));
        dialog.setHeaderText(getFormatted("closeGame.header"));

        dialog.setContentText(getFormatted("closeGame.content"));

        ButtonType buttonOK = new ButtonType(getFormatted("closeGame.okButton"), ButtonBar.ButtonData.FINISH);
        ButtonType buttonCancel = new ButtonType(getFormatted("cancelButton"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getButtonTypes().setAll(buttonOK, buttonCancel);
        Optional<ButtonType> result = dialog.showAndWait();

        return result.isPresent() && result.get() == buttonOK;
    }


    public static boolean initRestartGame() {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(AlertBox.class.
                getResource("/cz/logicgo/ui/newStyle/style.css")).toExternalForm());

        dialog.setTitle(getFormatted("confirmDialogTitle"));
        dialog.setHeaderText(getFormatted("restartGame.header"));

        dialog.setContentText(getFormatted("restartGame.content"));

        ButtonType buttonOK = new ButtonType(getFormatted("closeGame.okButton"), ButtonBar.ButtonData.FINISH);
        ButtonType buttonCancel = new ButtonType(getFormatted("cancelButton"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getButtonTypes().setAll(buttonOK, buttonCancel);
        Optional<ButtonType> result = dialog.showAndWait();

        return result.isPresent() && result.get() == buttonOK;
    }


    public static void showOneInstanceDialog(Runnable onFocusAction, Runnable onTerminateAction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(AlertBox.class.
                getResource("/cz/logicgo/ui/newStyle/style.css")).toExternalForm());
        alert.setTitle(getFormatted("app.instances.title"));
        alert.setHeaderText(getFormatted("app.instances.header"));
        alert.setContentText(getFormatted("app.instances.content"));

        ButtonType btnFocus = new ButtonType(getFormatted("app.instances.focus"));
        ButtonType btnTerminate = new ButtonType(getFormatted("app.instances.terminate"));
        ButtonType btnCancel = new ButtonType(getFormatted("cancelButton"), ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnFocus, btnTerminate, btnCancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == btnFocus) {
                if (onFocusAction != null) {
                    onFocusAction.run();
                }
            } else if (result.get() == btnTerminate) {
                if (onTerminateAction != null) {
                    onTerminateAction.run();
                }
            }
        }
    }


    public static Boolean deleteGames() {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(getFormatted("confirmDialogTitle"));
        dialog.setHeaderText(getFormatted("alert.delete.export"));

        ButtonType buttonYes = new ButtonType(getFormatted("confirmButton"), ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType(getFormatted("cancelButton"), ButtonBar.ButtonData.NO);

        dialog.getButtonTypes().setAll(buttonYes, buttonNo);

        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(AlertBox.class.
                getResource("/cz/logicgo/ui/newStyle/style.css")).toExternalForm());

        var result = dialog.showAndWait();

        return result.map(buttonType -> buttonType.equals(buttonYes)).orElse(false);

    }

    public static Boolean deleteUser() {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle(getFormatted("confirmDialogTitle"));
        dialog.setHeaderText(getFormatted("gamelist.alert.header"));

        ButtonType buttonYes = new ButtonType(getFormatted("gamelist.alert.saveButton"), ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType(getFormatted("cancelButton"), ButtonBar.ButtonData.NO);

        dialog.getButtonTypes().setAll(buttonYes, buttonNo);

        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(AlertBox.class.
                getResource("/cz/logicgo/ui/newStyle/style.css")).toExternalForm());

        var result = dialog.showAndWait();

        return result.map(buttonType -> buttonType.equals(buttonYes)).orElse(false);

    }

    public static void YouWonWindow(Duration timer) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(getFormatted("sudoku.win.window.title"));

        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(AlertBox.class.
                getResource("/cz/logicgo/ui/newStyle/style.css")).toExternalForm());

        dialog.getDialogPane().getStyleClass().add("win-dialog");

        if (timer != null) {
            long totalSeconds = timer.toSeconds();
            String time = getFormattedTime(totalSeconds);
            dialog.setHeaderText(getFormatted("sudoku.win.window.header", time));
        } else {
            dialog.setHeaderText(getFormatted("sudoku.win.window.title"));
        }

        dialog.setContentText("");

        ButtonType buttonOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getButtonTypes().setAll(buttonOK);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(buttonOK);
        if (okBtn != null) {
            okBtn.getStyleClass().add("default");
        }

        dialog.showAndWait();
    }

    public static void showBasicInfoBox(String title, String headerText, String contentText, Alert.AlertType alertType) {
        Alert dialog = new Alert(alertType);

        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(AlertBox.class.
                getResource("/cz/logicgo/ui/newStyle/style.css")).toExternalForm());

        dialog.setTitle(title);
        dialog.setHeaderText(headerText);

        if (contentText != null && !contentText.isEmpty()) {
            dialog.setContentText(contentText);
        }

        ButtonType buttonOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getButtonTypes().setAll(buttonOK);

        dialog.showAndWait();
    }


    public static void OkWindowError(String headerText) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(AlertBox.class.
                getResource("/cz/logicgo/ui/newStyle/style.css")).toExternalForm());
        dialog.setTitle("Chyba");
        dialog.setHeaderText(headerText);

        ButtonType buttonOK = new ButtonType("Ukončit", ButtonBar.ButtonData.OK_DONE);

        dialog.getButtonTypes().setAll(buttonOK);
        dialog.showAndWait();
    }

    public static void initExistingUser(String username) {
        OkWindowError(getFormatted("register.existingUser", username));
    }

    public static void initNonValidPassword() {
        OkWindowError(getFormatted("login.wrongPassword"));
    }

}
