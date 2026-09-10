package cz.logicgo.ui.controllers.gameControllers.howToPlay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.misc.enums.TypeGame;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;

public class HelpWindowManager {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String RULES_PATH = "/cz/logicgo/engine/rules/rules.json";

    private static final String FONT_FAMILY = "Segoe UI";
    private static final String COLOR_PRIMARY = "#2980b9";
    private static final String COLOR_TEXT = "#2c3e50";

    public static void showHelp(Game instance) {
        if (instance == null) return;

        try (InputStream rulesStream = HelpWindowManager.class.getResourceAsStream(RULES_PATH)) {
            if (rulesStream == null) return;

            JsonNode root = mapper.readTree(rulesStream);
            TypeGame typeGame = instance.getGameType();
            String gameKey = typeGame.name().toLowerCase();
            JsonNode gameNode = root.path(gameKey);

            if (gameNode.isMissingNode()) return;

            TextFlow textFlow = new TextFlow();
            textFlow.setPadding(new Insets(20));
            textFlow.setLineSpacing(5);
            String gameTitleStr = gameNode.path("title").asText(typeGame.getTranslation());
            addHeading(textFlow, gameTitleStr, 22, COLOR_PRIMARY, true);
            if (gameNode.path("base").isArray()) {
                for (JsonNode baseLine : gameNode.path("base")) {
                    addParagraph(textFlow, baseLine.asText());
                }
            }
            String variantKey = "";
            if (instance instanceof Sudoku sudoku) {
                variantKey = sudoku.getVariant().name().toLowerCase();
            }

            JsonNode variantNode = gameNode.path("variants").path(variantKey);
            if (!variantNode.isMissingNode()) {
                String vTitleStr = variantNode.path("title").asText(variantKey);
                addHeading(textFlow, "Aktivní varianta: " + vTitleStr, 15, "#7f8c8d", false);

                JsonNode rulesNode = variantNode.has("rules") ? variantNode.get("rules") : variantNode;
                if (rulesNode.isArray()) {
                    for (JsonNode ruleLine : rulesNode) {
                        addBulletPoint(textFlow, applyReplacements(ruleLine.asText(), instance));
                    }
                }
            }

            openFluentWindow(gameTitleStr, textFlow);

        } catch (Exception e) {

        }
    }

    public static void showAllRules() {
        try (InputStream rulesStream = HelpWindowManager.class.getResourceAsStream(RULES_PATH)) {
            if (rulesStream == null) return;

            JsonNode root = mapper.readTree(rulesStream);
            TextFlow textFlow = new TextFlow();
            textFlow.setPadding(new Insets(25));
            textFlow.setLineSpacing(5);

            addHeading(textFlow, "Kompletní přehled pravidel her", 24, "#2c3e50", true);
            addDivider(textFlow);

            var fields = root.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                JsonNode gameNode = entry.getValue();
                addHeading(textFlow, gameNode.path("title").asText(), 18, COLOR_PRIMARY, true);
                for (JsonNode line : gameNode.path("base")) {
                    addParagraph(textFlow, line.asText());
                }
                JsonNode variants = gameNode.path("variants");
                if (!variants.isMissingNode() && !variants.isEmpty()) {
                    addHeading(textFlow, "Podporované varianty:", 13, "#7f8c8d", false);

                    var varFields = variants.fields();
                    while (varFields.hasNext()) {
                        var varEntry = varFields.next();
                        JsonNode varNode = varEntry.getValue();
                        Text vTitle = new Text("  • " + varNode.path("title").asText() + ": ");
                        vTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 13));
                        vTitle.setFill(Color.web(COLOR_TEXT));
                        textFlow.getChildren().add(vTitle);
                        if (varNode.path("rules").isArray()) {
                            StringBuilder rBuilder = new StringBuilder();
                            for (JsonNode rLine : varNode.path("rules")) {
                                rBuilder.append(rLine.asText()).append(" ");
                            }
                            Text vRules = new Text(rBuilder.toString().trim() + "\n");
                            vRules.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 13));
                            vRules.setFill(Color.web("#555555"));
                            textFlow.getChildren().add(vRules);
                        }
                    }
                }
                addDivider(textFlow);
            }

            openFluentWindow("Kompletní přehled pravidel", textFlow);
        } catch (Exception e) {

        }
    }

    private static void addHeading(TextFlow flow, String text, double size, String hexColor, boolean bold) {
        Text heading = new Text(text + "\n");
        heading.setFont(Font.font(FONT_FAMILY, bold ? FontWeight.BOLD : FontWeight.SEMI_BOLD, size));
        heading.setFill(Color.web(hexColor));
        flow.getChildren().add(heading);
        Text space = new Text("\n");
        space.setFont(Font.font(FONT_FAMILY, 4));
        flow.getChildren().add(space);
    }

    private static void addParagraph(TextFlow flow, String text) {
        Text p = new Text(text + "\n\n");
        p.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 14));
        p.setFill(Color.web(COLOR_TEXT));
        flow.getChildren().add(p);
    }

    private static void addBulletPoint(TextFlow flow, String text) {
        Text bullet = new Text("  •  ");
        bullet.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        bullet.setFill(Color.web(COLOR_PRIMARY));

        Text t = new Text(text + "\n");
        t.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 14));
        t.setFill(Color.web(COLOR_TEXT));

        flow.getChildren().addAll(bullet, t);
    }

    private static void addDivider(TextFlow flow) {
        Text divider = new Text("\n__________________________________________________________________\n\n");
        divider.setFont(Font.font(FONT_FAMILY, FontWeight.LIGHT, 12));
        divider.setFill(Color.web("#dcdde1"));
        flow.getChildren().add(divider);
    }

    private static void openFluentWindow(String title, TextFlow textFlow) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);

        ScrollPane scrollPane = new ScrollPane(textFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #ffffff;");

        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 650, 480);

        stage.setScene(scene);
        stage.show();
    }

    private static String applyReplacements(String text, Game gameInstance) {
        if (gameInstance instanceof Sudoku sudoku) {
            int boardSize = sudoku.getBoard().length;
            String sizeRep = String.valueOf(boardSize);
            String maxDigRep = String.valueOf(Math.min(boardSize, 9));
            String extraLetRep = "";
            if (boardSize > 9) {
                int letterCount = boardSize - 9;
                extraLetRep = (letterCount == 1) ? " a písmeno A" : " a písmena A–" + (char) ('A' + letterCount - 1);
            }
            return text.replace("{{SIZE}}", sizeRep)
                    .replace("{{MAX_DIGIT}}", maxDigRep)
                    .replace("{{EXTRA_LETTERS}}", extraLetRep);
        }
        return text;
    }
}
