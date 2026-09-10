package cz.logicgo.ui.controllers.settingsControllers;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.persistence.services.GameService;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.ui.StartOfApp;
import cz.logicgo.ui.controllers.Openers;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import cz.logicgo.ui.misc.OpenConfigManager;
import cz.logicgo.ui.misc.windows.AlertBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

import static cz.logicgo.core.misc.Messages.getFormatted;


public class AccountTabController {

    private final GameService gameService = new GameService();
    public Button setDefaultButton;
    MainScreenController msc;
    UserService userService = new UserService();

    @FXML
    private Label usernameLabel;
    @FXML
    private Label totalGamesLabel;
    @FXML
    private Label finishedGamesLabel;
    @FXML
    private Label inProgressGamesLabel;
    @FXML
    private Label accountTitleLabel;
    @FXML
    private Label loggedInAsLabel;
    @FXML
    private Button changePasswordButton;
    @FXML
    private Button deleteAccountButton;
    @FXML
    private Label statisticsTitleLabel;
    @FXML
    private Label totalGamesTextLabel;
    @FXML
    private Label finishedGamesTextLabel;
    @FXML
    private Label inProgressGamesTextLabel;

    private User currentUser;

    private void loadLabels() {
        accountTitleLabel.setText(getFormatted("account.title"));
        loggedInAsLabel.setText(getFormatted("account.label.logged_in"));

        changePasswordButton.setText(getFormatted("account.button.change_password"));
        deleteAccountButton.setText(getFormatted("account.button.delete_account"));

        updateDefaultButtonState();

        statisticsTitleLabel.setText(getFormatted("account.statistics.title"));
        totalGamesTextLabel.setText(getFormatted("account.statistics.total"));
        finishedGamesTextLabel.setText(getFormatted("account.statistics.finished"));
        inProgressGamesTextLabel.setText(getFormatted("account.statistics.in_progress"));
    }

    public void initData(User user, MainScreenController msc) {
        this.currentUser = user;
        this.msc = msc;

        loadLabels();
        usernameLabel.setText(user.getUsername());
        loadStatistics();
    }

    private void updateDefaultButtonState() {
        if (currentUser == null) return;

        var optiInt = OpenConfigManager.readDefaultForCurrentPCUser();
        boolean isDefault = optiInt.isPresent() && optiInt.get() == currentUser.getId();

        if (isDefault) {
            setDefaultButton.setText(getFormatted("account.default.yes"));
            setDefaultButton.setDisable(true);
            setDefaultButton.setStyle("-fx-opacity: 0.9; -fx-font-weight: bold;");
        } else {
            setDefaultButton.setText(getFormatted("account.button.set_default"));
            setDefaultButton.setDisable(false);
            setDefaultButton.setStyle("");
        }
    }

    private void loadStatistics() {
        if (currentUser == null) return;

        long total = gameService.getCountPlayedGames(currentUser);
        long finished = gameService.getCountGamesByStatus(currentUser, Status.FINISHED);
        long inProgress = gameService.getCountGamesByStatus(currentUser, Status.IN_PROGRESS);

        totalGamesLabel.setText(String.valueOf(total));
        finishedGamesLabel.setText(String.valueOf(finished));
        inProgressGamesLabel.setText(String.valueOf(inProgress));
    }

    @FXML
    void onChangePasswordClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/settings/change_password.fxml"));
        Parent root = loader.load();
        ChangePasswordController controller = loader.getController();
        Scene scene = new Scene(root, 800, 500, false);
        var stage = new Stage();
        controller.setStage(stage);
        stage.setTitle(getFormatted("account.window.change_password.title"));
        controller.initialize(currentUser);
        stage.setScene(scene);
        Openers.darkModeIconSet(stage);
        stage.showAndWait();
    }

    @FXML
    void onDeleteAccountClicked() {
        boolean delete = AlertBox.deleteUser();
        if (delete) {
            userService.removeUser(currentUser);
            msc.setSkipSave(true);
            msc.close();
        }
    }

    public void onSetDefaultAccountClicked() {
        OpenConfigManager.setDefault(currentUser.getId());
        updateDefaultButtonState();
    }
}
