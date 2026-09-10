package cz.logicgo.ui;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.persistence.jpa.JpaUtil;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.ui.misc.OpenConfigManager;
import cz.logicgo.ui.misc.appStart.LockFileSemaphore;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;

import static cz.logicgo.ui.controllers.Openers.*;


public class StartOfApp extends Application {
    private UserService userService;

    static StartOfApp instance;

    public static StartOfApp getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        instance = this;
        var semaphore = new LockFileSemaphore();
        semaphore.setOnFocusRequest(() -> {
            if (stage.isIconified()) stage.setIconified(false);
            if (!stage.isShowing()) stage.show();
            stage.toFront();
            stage.requestFocus();
        });

        if (!semaphore.acquire()) {
            Platform.exit();
            return;
        }

        stage.setOnCloseRequest(e -> semaphore.release());
        Runtime.getRuntime().addShutdownHook(new Thread(semaphore::release));

        JpaUtil.initAsync();

        OpenConfigManager.WindowConfig winConf = OpenConfigManager.readWindowPosition();

        CompletableFuture.runAsync(() -> {
            try {
                JpaUtil.warmUp();

                userService = new UserService();
                boolean usersExist = userService.usersExist();

                Platform.runLater(() -> {
                    try {
                        if (!usersExist) {
                            openRegisterScreen(stage, winConf);
                        } else {
                            var optionalDefaultUser = OpenConfigManager.readDefaultForCurrentPCUser();

                            if (optionalDefaultUser.isPresent()) {
                                int id = optionalDefaultUser.get();
                                User user = userService.getUserById(id);
                                if (user == null) {
                                    openProfileSelector(stage, winConf);
                                } else {
                                    if (user.isHasPassword()) {
                                        openLoginScreen(user, stage, winConf);
                                    } else {
                                        openMainScreen(user, stage, winConf);
                                    }
                                }
                            } else {
                                openProfileSelector(stage, winConf);
                            }
                        }
                    } catch (Exception e) {
                        Platform.exit();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(Platform::exit);
            }
        });
    }

}
