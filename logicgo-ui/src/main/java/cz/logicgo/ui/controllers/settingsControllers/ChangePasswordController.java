package cz.logicgo.ui.controllers.settingsControllers;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.persistence.exceptions.database.user.WrongPasswordChangeException;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.ui.misc.windows.AlertBox;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import static cz.logicgo.core.misc.Messages.getFormatted;


public class ChangePasswordController {

    public Stage stage;
    private User user;

    @FXML
    public Label titleLabel;
    @FXML
    public Label oldPinLabel;
    @FXML
    public Label newPinLabel;
    @FXML
    public Label confirmPinLabel;
    @FXML
    public Tooltip pinTooltip;

    @FXML
    public PasswordField oldPasswordField;
    @FXML
    public Label passwordRuleLabel;
    @FXML
    public PasswordField newPasswordField;
    @FXML
    public PasswordField confirmNewPasswordField;
    @FXML
    public Button cancelButton;
    @FXML
    public Button changePasswordButton;

    private final UserService userService = new UserService();

    public void initialize(User user) {
        this.user = user;

        titleLabel.setText(getFormatted("account.pin.change_title"));
        oldPinLabel.setText(getFormatted("account.pin.old_label"));
        newPinLabel.setText(getFormatted("account.pin.new_label"));
        confirmPinLabel.setText(getFormatted("account.pin.confirm_label"));

        oldPasswordField.setPromptText(user.isHasPassword()
                ? getFormatted("account.pin.old_prompt")
                : getFormatted("account.pin.old_prompt_empty"));
        newPasswordField.setPromptText(getFormatted("account.pin.new_prompt"));
        confirmNewPasswordField.setPromptText(getFormatted("account.pin.confirm_prompt"));

        pinTooltip.setText(getFormatted("account.pin.rule_tooltip"));
        cancelButton.setText(getFormatted("cancelButton"));
        changePasswordButton.setText(getFormatted("account.pin.button_save"));
    }

    public void onCancelClicked() {
        if (stage != null) stage.close();
    }

    public Stage getStage() {
        return stage;
    }

    public ChangePasswordController setStage(Stage stage) {
        this.stage = stage;
        return this;
    }

    public void onChangePasswordClicked() {
        String oldPassword = oldPasswordField.getText() != null ? oldPasswordField.getText().trim() : "";
        String newPassword = newPasswordField.getText() != null ? newPasswordField.getText().trim() : "";
        String confirmPassword = confirmNewPasswordField.getText() != null ? confirmNewPasswordField.getText().trim() : "";
        if (user.isHasPassword() && oldPassword.isEmpty()) {
            AlertBox.OkWindowError(getFormatted("account.pin.error.old_required"));
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            AlertBox.OkWindowError(getFormatted("account.pin.error.mismatch"));
            return;
        }
        if (!newPassword.isEmpty() && !newPassword.matches("\\d{4}")) {
            AlertBox.OkWindowError(getFormatted("account.pin.error.format"));
            return;
        }
        try {
            userService.changePassword(user, oldPassword, newPassword);
            user.setHasPassword(!newPassword.isEmpty());
            AlertBox.OkWindowError(getFormatted("account.pin.success"));
            if (stage != null) stage.close();
        } catch (WrongPasswordChangeException e) {
            AlertBox.errorBox();
        }
    }
}
