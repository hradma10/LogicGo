package cz.logicgo.ui.controllers.screenControllers;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.persistence.exceptions.database.user.UserNotExistsException;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.ui.misc.windows.AlertBox;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.Openers.openMainScreen;
import static cz.logicgo.ui.controllers.Openers.openProfileSelectorRunning;


public class LoginController implements Initializable {

    @FXML
    public PasswordField passwordField;
    @FXML
    public Label welcomeLabel;
    public Label passwordLabel;
    public Button cancelButton;
    public Button loginButton;

    private Stage stage;
    private final UserService userService = new UserService();
    private User user;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void setUser(User user) {
        this.user = user;
        this.welcomeLabel.setText(getFormatted("app.login.welcome", user.getUsername()));
        this.passwordLabel.setText(getFormatted("app.login.password"));
        this.cancelButton.setText(getFormatted("cancelButton"));
        this.loginButton.setText(getFormatted("login"));
    }

    @FXML
    public void loginUser() {
        String password = passwordField.getText();

        try {
            User validatedUser = userService.login(user.getUsername(), password);
            if (validatedUser != null) {
                openMainScreen(validatedUser, stage, null);
            }
        } catch (UserNotExistsException e) {
            AlertBox.initNonValidPassword();
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
