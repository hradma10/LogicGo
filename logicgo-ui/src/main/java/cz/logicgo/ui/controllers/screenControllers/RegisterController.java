package cz.logicgo.ui.controllers.screenControllers;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.persistence.exceptions.database.user.UserExistsException;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.ui.controllers.settingsControllers.DefaultKeyBindings;
import cz.logicgo.ui.misc.OpenConfigManager;
import cz.logicgo.ui.misc.windows.AlertBox;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.Openers.openProfileSelectorRunning;


public class RegisterController implements Initializable {

    @FXML
    public TextField textFieldUsername;
    @FXML
    public PasswordField passwordFieldUser;
    @FXML
    public CheckBox setAsDefault;
    public Label nameLabel;
    public Label passwordLabel;
    public Label newProfileLabel;
    public Button createButton;
    public Button cancelButton;
    public Label asDefaultLabel;

    private Stage stage;
    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        boolean hasDefault = OpenConfigManager.readDefaultForCurrentPCUser().isPresent();
        setAsDefault.setSelected(!hasDefault);
        setLabels();
    }

    public void setLabels() {
        newProfileLabel.setText(getFormatted("app.register.newProfile"));
        nameLabel.setText(getFormatted("app.register.name"));
        passwordLabel.setText(getFormatted("app.register.password"));
        asDefaultLabel.setText(getFormatted("app.register.asDefault"));
        createButton.setText(getFormatted("create"));
        cancelButton.setText(getFormatted("cancelButton"));
    }

    @FXML
    public void onCreateUserButtonClicked() {
        String username = textFieldUsername.getText().trim();
        String password = passwordFieldUser.getText();

        if (username.isEmpty()) {
            return;
        }

        try {
            var defaultHotkeys = DefaultKeyBindings.generateDefaults();
            User user = userService.register(username, password, defaultHotkeys);
            if (setAsDefault.isSelected()) {
                OpenConfigManager.setDefault(user.getId());
            }
            goBack();
        } catch (UserExistsException e) {
            AlertBox.initExistingUser(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack() {
        openProfileSelectorRunning(stage);
    }


    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
