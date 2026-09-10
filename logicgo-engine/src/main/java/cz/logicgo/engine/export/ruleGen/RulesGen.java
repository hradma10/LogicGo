package cz.logicgo.engine.export.ruleGen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig.isMultiDoku;


public class RulesGen {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String RULES_PATH = "/cz/logicgo/engine/game/rules/rules.json";

    private static final Font FONT_TITLE = new Font(Font.HELVETICA, 20, Font.BOLD, Color.DARK_GRAY);
    private static final Font FONT_H2 = new Font(Font.HELVETICA, 15, Font.BOLD, new Color(41, 128, 185));
    private static final Font FONT_H3 = new Font(Font.HELVETICA, 12, Font.BOLD, Color.DARK_GRAY);
    private static final Font FONT_P = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

    public static void generateAllRulesPdfFile(File outputFile) {
        Document document = new Document(PageSize.A4, 40, 40, 54, 54);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            PdfWriter.getInstance(document, fos);
            document.open();
            appendAllRulesToDocument(document);
        } catch (Exception e) {

        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    public static void appendRulesToDocument(Document document, List<Game> instances) {
        if (instances == null || instances.isEmpty()) return;

        try (InputStream rulesStream = RulesGen.class.getResourceAsStream(RULES_PATH)) {
            if (rulesStream == null) return;

            JsonNode root = mapper.readTree(rulesStream);

            Paragraph mainTitle = new Paragraph("Pravidla vygenerovaných her", FONT_TITLE);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            mainTitle.setSpacingAfter(20);
            document.add(mainTitle);
            Map<TypeGame, List<Game>> gamesByType = instances.stream()
                    .collect(Collectors.groupingBy(Game::getGameType));

            for (Map.Entry<TypeGame, List<Game>> entry : gamesByType.entrySet()) {
                TypeGame typeGame = entry.getKey();
                List<Game> gamesInGroup = entry.getValue();

                String gameKey = typeGame.name().toLowerCase();
                JsonNode gameNode = root.path(gameKey);
                if (gameNode.isMissingNode()) continue;
                String gameTitleStr = gameNode.path("title").asText(typeGame.getTranslation());
                Paragraph h1 = new Paragraph(gameTitleStr, FONT_H2);
                h1.setSpacingBefore(15);
                h1.setSpacingAfter(8);
                document.add(h1);
                appendRulesParagraphs(document, gameNode.path("base"), gamesInGroup.get(0));
                Set<String> activeVariants = new LinkedHashSet<>();
                boolean customSizeDetected = false;
                Game sampleGameForSize = null;

                for (Game g : gamesInGroup) {
                    switch (g) {
                        case Sudoku sudoku -> {
                            if (isMultiDoku(sudoku.getVariant())) {
                                activeVariants.add("multidoku");
                            } else {
                                activeVariants.add(sudoku.getVariant().name().toLowerCase());
                            }
                            if (sudoku.getBoard().length != 9 && !isMultiDoku(sudoku.getVariant())) {
                                customSizeDetected = true;
                                sampleGameForSize = sudoku;
                            }
                        }
                        case Bridge bridge -> activeVariants.add(bridge.getType().name().toLowerCase());
                        case Maze maze -> {
                            Set<MazeType> types = maze.getMazeGridFloors().stream()
                                    .map(f -> f.getMazeGrid().getMazeType())
                                    .collect(Collectors.toSet());
                            if (maze.getMazeGridFloors().size() > 1) {
                                activeVariants.add("multi_level");
                            }
                            activeVariants.addAll(types.stream().map(t -> t.name().toLowerCase()).toList());
                        }
                        case Shikaku shikaku -> {
                            if (shikaku.getTypeOfGame() instanceof Enum<?> shType) {
                                activeVariants.add(shType.name().toLowerCase());
                            }
                        }
                        default -> {
                        }
                    }
                }
                activeVariants.remove("classic");
                JsonNode variantsNode = gameNode.path("variants");
                if (!variantsNode.isMissingNode()) {
                    if (customSizeDetected && variantsNode.has("custom_size")) {
                        buildNativeVariant(document, variantsNode.get("custom_size"), gameKey, "custom_size", sampleGameForSize);
                    }
                    for (String variantKey : activeVariants) {
                        JsonNode specificVariant = variantsNode.path(variantKey);
                        if (!specificVariant.isMissingNode()) {
                            Game sampleGame = gamesInGroup.stream()
                                    .filter(g -> matchesVariant(g, variantKey))
                                    .findFirst()
                                    .orElse(gamesInGroup.get(0));

                            buildNativeVariant(document, specificVariant, gameKey, variantKey, sampleGame);
                        }
                    }
                }
                document.add(new Paragraph(" "));
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static boolean matchesVariant(Game game, String variantKey) {
        switch (game) {
            case Sudoku s -> {
                if (variantKey.equals("multidoku")) return isMultiDoku(s.getVariant());
                return s.getVariant().name().equalsIgnoreCase(variantKey);
            }
            case Bridge b -> {
                return b.getType().name().equalsIgnoreCase(variantKey);
            }
            case Maze m -> {
                if (variantKey.equals("multi_level")) return m.getMazeGridFloors().size() > 1;
                return m.getMazeGridFloors().stream().anyMatch(f -> f.getMazeGrid().getMazeType().name().equalsIgnoreCase(variantKey));
            }
            case Shikaku sh -> {
                if (sh.getTypeOfGame() instanceof Enum<?> shType) {
                    return shType.name().equalsIgnoreCase(variantKey);
                }
            }
            default -> {
            }
        }
        return false;
    }

    public static void appendAllRulesToDocument(Document document) {
        try (InputStream rulesStream = RulesGen.class.getResourceAsStream(RULES_PATH)) {
            if (rulesStream == null) return;

            JsonNode root = mapper.readTree(rulesStream);

            Paragraph mainTitle = new Paragraph("Kompletní přehled pravidel her", FONT_TITLE);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            mainTitle.setSpacingAfter(25);
            document.add(mainTitle);

            Iterator<Map.Entry<String, JsonNode>> gamesIterator = root.fields();
            while (gamesIterator.hasNext()) {
                Map.Entry<String, JsonNode> gameEntry = gamesIterator.next();
                String gameKey = gameEntry.getKey();
                JsonNode gameNode = gameEntry.getValue();

                String gameTitleStr = gameNode.path("title").asText(gameKey);
                Paragraph h1 = new Paragraph(gameTitleStr, FONT_TITLE);
                h1.setSpacingBefore(15);
                h1.setSpacingAfter(10);
                document.add(h1);

                appendRulesParagraphs(document, gameNode.path("base"), null);

                JsonNode variantsNode = gameNode.path("variants");
                if (!variantsNode.isMissingNode() && !variantsNode.isEmpty()) {
                    Paragraph variantsSectionTitle = new Paragraph("Podporované herní varianty:", FONT_H2);
                    variantsSectionTitle.setSpacingBefore(12);
                    variantsSectionTitle.setSpacingAfter(6);
                    document.add(variantsSectionTitle);

                    Iterator<Map.Entry<String, JsonNode>> variantsIterator = variantsNode.fields();
                    while (variantsIterator.hasNext()) {
                        Map.Entry<String, JsonNode> variantEntry = variantsIterator.next();
                        buildNativeVariant(document, variantEntry.getValue(), gameKey, variantEntry.getKey(), null);
                    }
                }
                document.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(0.5f, 100, Color.LIGHT_GRAY, Element.ALIGN_CENTER, -10)));
            }
        } catch (Exception e) {

        }
    }

    private static void buildNativeVariant(Document document, JsonNode specificVariant, String gameKey, String variantKey, Game gameInstance) throws DocumentException {
        if (specificVariant.has("title")) {
            String vTitleStr = specificVariant.get("title").asText();
            Paragraph h3 = new Paragraph("• " + vTitleStr, FONT_H3);
            h3.setSpacingBefore(6);
            h3.setSpacingAfter(4);
            h3.setIndentationLeft(10);
            document.add(h3);
        }

        JsonNode rulesNode = specificVariant.has("rules") ? specificVariant.get("rules") : specificVariant;
        appendRulesParagraphs(document, rulesNode, gameInstance);
    }

    private static void appendRulesParagraphs(Document document, JsonNode rulesArray, Game gameInstance) throws DocumentException {
        if (rulesArray.isArray()) {
            for (JsonNode ruleLine : rulesArray) {
                String text = applyReplacements(ruleLine.asText(), gameInstance);
                Paragraph p = new Paragraph(text, FONT_P);
                p.setSpacingAfter(4);
                p.setIndentationLeft(20);
                p.setAlignment(Element.ALIGN_JUSTIFIED);
                document.add(p);
            }
        }
    }

    private static String applyReplacements(String text, Game gameInstance) {
        if (gameInstance == null) {
            return text.replace("{{SIZE}}", "N")
                    .replace("{{MAX_DIGIT}}", "dle velikosti")
                    .replace("{{EXTRA_LETTERS}}", "");
        }

        if (gameInstance instanceof Sudoku sudoku) {
            int boardSize = sudoku.getBoard().length;
            String sizeRep = String.valueOf(boardSize);
            String maxDigRep = String.valueOf(Math.min(boardSize, 9));
            String extraLetRep = "";
            if (boardSize > 9) {
                int letterCount = boardSize - 9;
                extraLetRep = (letterCount == 1) ? " a~písmeno A" : " a~písmena A–" + (char) ('A' + letterCount - 1);
            }
            return text.replace("{{SIZE}}", sizeRep)
                    .replace("{{MAX_DIGIT}}", maxDigRep)
                    .replace("{{EXTRA_LETTERS}}", extraLetRep);
        }
        return text;
    }
}
