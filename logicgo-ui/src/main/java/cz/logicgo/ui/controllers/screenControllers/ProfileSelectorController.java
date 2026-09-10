package cz.logicgo.ui.controllers.screenControllers;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.persistence.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.controllers.Openers.*;
import static cz.logicgo.ui.misc.OpenConfigManager.*;


public class ProfileSelectorController implements Initializable {

    @FXML
    public FlowPane profilesContainer;
    public Button exitAppButton;
    public Label appLabel;
    public Label titleLabel;

    private Stage stage;
    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUpLabels();
    }

    public void setUpLabels() {
        exitAppButton.setText(getFormatted("closeApp"));
        appLabel.setText(getFormatted("app.name"));
        titleLabel.setText(getFormatted("app.profileSelect.title"));
    }

    public void loadProfiles() {
        profilesContainer.getChildren().clear();

        List<User> users = userService.getAllUsers();

        WindowConfig windowConfig = readWindowPosition();

        for (User user : users) {
            Button profileBtn = new Button(user.getUsername());
            profileBtn.getStyleClass().add("profile-button");
            profileBtn.setPrefSize(120, 120);

            profileBtn.setOnAction(event -> handleProfileClick(user, windowConfig));
            profilesContainer.getChildren().add(profileBtn);
        }

        Button newProfileBtn = new Button(getFormatted("app.profileSelect.newProfile"));
        newProfileBtn.getStyleClass().add("new-profile-button");
        newProfileBtn.setPrefSize(120, 120);
        newProfileBtn.setOnAction(event -> {
            try {
                openRegisterScreen(stage, windowConfig);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        profilesContainer.getChildren().add(newProfileBtn);
    }

    private void handleProfileClick(User user, WindowConfig windowConfig) {
        try {
            boolean hasPassword = user.isHasPassword();

            if (hasPassword) {
                openLoginScreen(user, stage, windowConfig);
            } else {
                openMainScreen(user, stage, windowConfig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void closeApp() {
        if (stage != null) {
            writeWindowPosition(stage);
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
