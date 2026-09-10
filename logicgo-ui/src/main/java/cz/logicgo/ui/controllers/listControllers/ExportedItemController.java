package cz.logicgo.ui.controllers.listControllers;

import cz.logicgo.core.entity.export.ExportDetail;
import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.persistence.services.ExportedGameService;
import cz.logicgo.ui.controllers.exportControllers.ExportMultipleGamesController;
import cz.logicgo.ui.controllers.screenControllers.MainScreenController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import static cz.logicgo.core.misc.formatter.FavoriteFormatter.parseGameDetails;


public class ExportedItemController {

    private final ExportedGameService exportedGameService = new ExportedGameService();

    @FXML
    public Button viewButton;
    @FXML
    public Button deleteButton;
    @FXML
    public HBox list_item;
    @FXML
    private ImageView thumbnailView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label infoLabel;

    private MainScreenController mainScreenController;
    private ExportMultipleGamesController parentController;
    private User user;

    public void initialize(User user, MainScreenController mainScreenController, ExportMultipleGamesController parentController) {
        this.user = user;
        this.mainScreenController = mainScreenController;
        this.parentController = parentController;
    }

    public void setData(ExportDetail item) {
        String filePath = item.getPathToAssocFileNormal();
        String fileName = "Neznámý export";
        if (filePath != null && !filePath.isEmpty()) {
            fileName = Path.of(filePath).getFileName().toString().replace(".pdf", "");
        }
        titleLabel.setText(fileName);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String dateStr = item.getCreatedAt() != null ? item.getCreatedAt().format(formatter) : "Neznámé datum";
        infoLabel.setText(String.format("Vytvořeno: %s | Počet her: %d", dateStr, item.getCountOfGames()));

        if (item.getExportedGames() != null && !item.getExportedGames().isEmpty()) {
            Map<String, Integer> detailedCounts = new LinkedHashMap<>();

            for (Game game : item.getExportedGames()) {
                String key = parseGameDetails(game);
                detailedCounts.put(key, detailedCounts.getOrDefault(key, 0) + 1);
            }

            StringBuilder tooltipText = new StringBuilder("Exportované hry:\n");
            detailedCounts.forEach((specification, count) ->
                    tooltipText.append("• ").append(count).append("x ").append(specification).append("\n")
            );

            Tooltip tooltip = new Tooltip(tooltipText.toString());
            tooltip.setShowDelay(Duration.millis(500));
            tooltip.setStyle("-fx-font-size: 13px; -fx-line-spacing: 2px;");

            Tooltip.install(list_item, tooltip);
        }
    }

}
