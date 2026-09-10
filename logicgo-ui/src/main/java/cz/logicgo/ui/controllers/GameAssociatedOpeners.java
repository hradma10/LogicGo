package cz.logicgo.ui.controllers;

import cz.logicgo.core.entity.games.Game;
import cz.logicgo.ui.StartOfApp;
import cz.logicgo.ui.controllers.gameControllers.ExportWindowController;
import cz.logicgo.ui.controllers.gameControllers.SolutionWindowController;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GameAssociatedOpeners {

    public static void openSolution(Game game) throws IOException {
        FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/misc/solution_window.fxml"));
        Parent root = loader.load();
        SolutionWindowController controller = loader.getController();
        Scene scene = new Scene(root, 500, 500, false);
        Stage stage = new Stage();
        controller.setStage(stage);
        controller.initialize(game);
        stage.setMinWidth(300);
        stage.setMinHeight(300);
        stage.setMaxWidth(800);
        stage.setMaxHeight(800);
        stage.setTitle("Řešení");
        stage.setScene(scene);
        stage.setMaximized(false);
        stage.centerOnScreen();

        Openers.darkModeIconSet(stage);

        stage.showAndWait();
    }

    public static void openExport(Game game, Stage mainStage, MainScreenController mainScreenController) throws IOException {
        FXMLLoader loader = new FXMLLoader(StartOfApp.class.getResource("/cz/logicgo/ui/windows/misc/export_window.fxml"));
        Parent root = loader.load();
        ExportWindowController controller = loader.getController();
        Scene scene = new Scene(root, 1075, 700, false);
        Stage stage = new Stage();
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setMaxWidth(1400);
        stage.setMaxHeight(900);
        controller.setStage(stage);
        controller.initialize(game, game.getPlayer(), mainStage, mainScreenController);
        stage.setTitle("Export");
        stage.setScene(scene);
        stage.setMaximized(false);

        Openers.darkModeIconSet(stage);

        stage.showAndWait();
    }
}
