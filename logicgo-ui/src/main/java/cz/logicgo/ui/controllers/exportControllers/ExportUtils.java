package cz.logicgo.ui.controllers.exportControllers;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import cz.logicgo.core.GameUtils;
import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.bridge.BridgeInit;
import cz.logicgo.core.factoryInit.maze.MazeInit;
import cz.logicgo.core.factoryInit.shikaku.ShikakuInit;
import cz.logicgo.core.factoryInit.sudoku.SudokuInit;
import cz.logicgo.core.gameClasses.export.ExportDetailsLocal;
import cz.logicgo.core.gameClasses.export.gameTypes.*;
import cz.logicgo.core.gameClasses.maze.dataStructures.MazeFloor;
import cz.logicgo.core.gameClasses.maze.dataStructures.gameModes.*;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.misc.enums.PreviewType;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiGridConfig;
import cz.logicgo.engine.export.ruleGen.RulesGen;
import cz.logicgo.engine.util.generate.SeedCreator;
import cz.logicgo.persistence.services.UserService;
import cz.logicgo.persistence.services.UserSettingsService;
import cz.logicgo.ui.factories.BridgeGameFactory;
import cz.logicgo.ui.factories.MazeGameFactory;
import cz.logicgo.ui.factories.ShikakuGameFactory;
import cz.logicgo.ui.factories.SudokuGameFactory;
import cz.logicgo.ui.renderers.BridgeRenderer;
import cz.logicgo.ui.renderers.ShikakuRenderer;
import cz.logicgo.ui.renderers.maze.MazeRenderer;
import cz.logicgo.ui.renderers.sudoku.ConstraintRenderer;
import cz.logicgo.ui.renderers.sudoku.SudokuRenderer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.createSudokuForPrint;
import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.hasOutsideClues;
import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.core.misc.enums.settings.UserSettings.SAVE_FILE_PATH_EXPORT;
import static cz.logicgo.ui.renderers.maze.MazeStrokeUtils.strokePatternStuff;


public class ExportUtils {
    private static final double RENDER_SCALE_SUDOKU = 3.0;
    private static final double RENDER_SCALE_BRIDGE = 3.0;
    private static final double RENDER_SCALE_MAZE = 3.0;

    private static final UserService userService = new UserService();

    private static final UserSettingsService userSettingsService = new UserSettingsService();

    public static BufferedImage renderNodeToImage(Game game, double scale) {
        final BufferedImage[] result = new BufferedImage[1];
        CountDownLatch latch = new CountDownLatch(1);
        User user = game.getPlayer();
        Platform.runLater(() -> {
            try {
                double canvasWidth = 1000.0 * scale;
                double canvasHeight = 1000.0 * scale;

                if (game instanceof Maze maze) {
                    int cols = maze.getWidth();
                    int rows = maze.getHeight();
                    double aw = cols;
                    double ah = rows;
                    double aspect = 1.0;

                    if (maze.getMazeShape() == MazeShape.HEXAGONAL) {
                        aw = 1.0 + 0.75 * (cols - 1);
                        ah = rows + (cols > 1 ? 0.5 : 0.0);
                        aspect = Math.sqrt(3) / 2.0;
                    }
                    canvasHeight = (canvasWidth / aw) * (ah * aspect);
                }

                Canvas canvas = new Canvas(canvasWidth, canvasHeight);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setImageSmoothing(false);

                switch (game) {
                    case Sudoku sudoku -> {
                        if (hasOutsideClues(sudoku)) {
                            gc.setFill(Color.WHITE);
                            gc.fillRect(0, 0, canvasWidth, canvasHeight);

                            double pX = canvasWidth * 0.05;
                            double pY = canvasHeight * 0.05;
                            double gW = canvasWidth - 2 * pX;
                            double gH = canvasHeight - 2 * pY;

                            Canvas gridCanvas = new Canvas(gW, gH);

                            SudokuRenderer.renderFullBoard(gridCanvas, sudoku, true);

                            int size = sudoku.getType().getGridSize();
                            ConstraintRenderer.drawOutsideModifiers(gc, sudoku.getModifiers(), size, gW / size, gH / size, pX, pY, gW, gH, true);

                            WritableImage gridSnap = new WritableImage((int) gW, (int) gH);
                            gridCanvas.snapshot(null, gridSnap);
                            gc.drawImage(gridSnap, pX, pY);
                        } else {
                            SudokuRenderer.renderFullBoard(canvas, sudoku, true);
                        }
                    }
                    case Bridge bridge -> BridgeRenderer.render(canvas, bridge, true);
                    case Maze maze -> {
                        var currentGrid = maze.getMazeGridFloors().get(maze.getCurrentFloor()).getMazeGrid();
                        int totalFloors = maze.getMazeGridFloors().size();
                        MazeRenderer.renderGrid(canvas, currentGrid, maze.getCurrentFloor(), totalFloors, true, user);
                        if (currentGrid.getPath() != null) {
                            MazeRenderer.renderPath(canvas, currentGrid.getPath(), currentGrid, true);
                        }
                    }
                    case Shikaku shikaku -> {
                        ShikakuRenderer.render(canvas, shikaku, true);
                    }
                    default -> {
                    }
                }

                WritableImage fxImage = new WritableImage((int) canvasWidth, (int) canvasHeight);
                canvas.snapshot(null, fxImage);
                result[0] = SwingFXUtils.fromFXImage(fxImage, null);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result[0];
    }


    public static ImageWithGame render(Game game, PreviewType pType) {
        return finalizeRender(game, pType);
    }

    public static Game makeSolvedVersion(Game game) {
        Game copy = GameUtils.createNewInstance(game);
        switch (copy) {
            case Sudoku sudoku -> {
                var solution = sudoku.getSolutionBoard();
                sudoku.setBoard(solution);
            }
            case Bridge b -> {
                var solution = b.getSolutionBridges();
                b.setIslandBridges(solution);
            }
            case Maze m -> {
                m.getMazeGridFloors().forEach(floor -> {
                    var path = floor.getMazeGrid().getPath();
                    path.getActivePath().clear();
                    path.getActivePath().addAll(path.getSolutionPath());
                    path.setShowTravelPath(true);
                });
            }
            case Shikaku s -> {
                var solution = s.getSolutionRectangles();
                s.setRectangles(solution);
            }
            default -> throw new IllegalStateException("Unexpected value: " + copy);
        }
        return copy;
    }

    private static ImageWithGame finalizeRender(Game game, PreviewType pType) {
        BufferedImage taskImg = null;
        BufferedImage solImg = null;

        double scale = (game instanceof Sudoku) ? RENDER_SCALE_SUDOKU :
                (game instanceof Maze) ? RENDER_SCALE_MAZE : RENDER_SCALE_BRIDGE;

        if (pType == PreviewType.UNSOLVED || pType == PreviewType.BOTH) {
            Game taskVersion = (game instanceof Sudoku s) ? createSudokuForPrint(s) : game;
            taskImg = renderNodeToImage(taskVersion, scale);
        }

        if (pType == PreviewType.SOLVED || pType == PreviewType.BOTH) {
            solImg = renderNodeToImage(makeSolvedVersion(game), scale);
        }

        return new ImageWithGame(game, taskImg, solImg);
    }

    public static Path getSaveFilePath(User user, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(getFormatted("export.window.title"));

        String initialPath = userSettingsService.loadAllSettings(user).get(SAVE_FILE_PATH_EXPORT).toString();
        if (initialPath != null) {
            initialPath = initialPath.replace("\"", "");
        }

        File initDir = new File(initialPath != null ? initialPath : ".");

        if (!initDir.exists() || !initDir.isDirectory()) {
            initDir = new File(".");
            initialPath = ".";
        }
        fileChooser.setInitialDirectory(initDir);
        String fileName = "export-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMM-HHmmss"));

        fileChooser.setInitialFileName(fileName);

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf")
        );

        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            String parentPath = selectedFile.getParent();
            if (parentPath != null) {
                userSettingsService.saveSetting(user, SAVE_FILE_PATH_EXPORT, parentPath);
            }
            return selectedFile.toPath();
        }
        return null;
    }

    public static Font getTitleFont() {
        FontFactory.registerDirectories();
        return FontFactory.getFont("Arial", "Identity-H", true, 13);
    }

    public static Font getIdFont() {
        FontFactory.registerDirectories();
        return FontFactory.getFont("Arial", "Identity-H", true, 10);
    }

    public static void saveSingleGameToPdf(Path path, Game game, PreviewType pType, String baseTitle, boolean forPrint, boolean wantHelp) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            document.open();

            PdfLayoutManager layoutManager = new PdfLayoutManager(document, getTitleFont(), getIdFont(), 1);

            if (pType == PreviewType.UNSOLVED || pType == PreviewType.BOTH) {
                layoutManager.addGameWithFloors(game, PreviewType.UNSOLVED, baseTitle);
            }

            if (pType == PreviewType.SOLVED || pType == PreviewType.BOTH) {
                layoutManager.flush();
                layoutManager.addGameWithFloors(game, PreviewType.SOLVED, baseTitle);
            }

            layoutManager.flush();

            if (wantHelp) {
                document.newPage();
                RulesGen.appendRulesToDocument(document, List.of(game));
            }

            if (forPrint) {
                writer.addJavaScript("this.print({bUI: true, bSilent: false, bShrinkToFit: true});");
            }

            document.close();
        }
    }

    public static void addAdditionalGameInfo(PdfPCell cell, Game game, Font idFont) {
        Font largerFont = new Font(idFont);
        largerFont.setSize(idFont.getSize() + 1.5f);

        if (game instanceof Maze maze) {
            var floors = maze.getMazeGridFloors();
            int currentFloorIdx = maze.getCurrentFloor();
            if (floors != null && currentFloorIdx >= 0 && currentFloorIdx < floors.size()) {
                MazeFloor floor = floors.get(currentFloorIdx);
                MazeGrid grid = floor.getMazeGrid();
                switch (grid.getMazeType()) {
                    case WALLS -> {
                        Optional<MazeModifier> wallModifier = grid.getModifiers().stream().filter(mazeModifier -> mazeModifier instanceof WallModifier).findFirst();
                        if (wallModifier.isPresent()) {
                            WallModifier modifier = (WallModifier) wallModifier.get();
                            Paragraph info = new Paragraph(getFormatted("maze.print.walls", modifier.getTrueWayWallCount()), largerFont);
                            info.setAlignment(Element.ALIGN_LEFT);
                            cell.addElement(info);
                        }
                    }
                    case PATTERN -> {
                        Optional<MazeModifier> patternModifier = grid.getModifiers().stream()
                                .filter(mazeModifier -> mazeModifier instanceof PatternModifier)
                                .findFirst();

                        if (patternModifier.isPresent()) {
                            PatternModifier modifier = (PatternModifier) patternModifier.get();

                            List<Integer> distinctPatterns = modifier.getCellPatterns()
                                    .values()
                                    .stream()
                                    .distinct()
                                    .sorted()
                                    .toList();

                            if (!distinctPatterns.isEmpty()) {
                                Paragraph info = new Paragraph();
                                info.setAlignment(Element.ALIGN_LEFT);

                                info.add(new Chunk(getFormatted("maze.print.pattern"), idFont));

                                try {
                                    double iconSize = 14.0;
                                    double spacing = 4.0;

                                    double totalWidth = distinctPatterns.size() * iconSize + (distinctPatterns.size() - 1) * spacing;
                                    double totalHeight = iconSize;

                                    WritableImage fxImg = new WritableImage((int) Math.ceil(totalWidth), (int) Math.ceil(totalHeight));
                                    CountDownLatch latch = new CountDownLatch(1);

                                    Platform.runLater(() -> {
                                        try {
                                            Canvas patternCanvas = new Canvas(totalWidth, totalHeight);
                                            GraphicsContext pGc = patternCanvas.getGraphicsContext2D();
                                            pGc.setImageSmoothing(false);
                                            pGc.setLineWidth(1.2);

                                            for (int i = 0; i < distinctPatterns.size(); i++) {
                                                int patternId = distinctPatterns.get(i);
                                                double cx = i * (iconSize + spacing) + iconSize / 2.0;
                                                double cy = totalHeight / 2.0;
                                                double size = iconSize / 2.0 - 1.0;

                                                strokePatternStuff(pGc, patternId, cx, cy, size);
                                            }

                                            patternCanvas.snapshot(null, fxImg);
                                        } finally {
                                            latch.countDown();
                                        }
                                    });

                                    latch.await();

                                    BufferedImage patternImg = SwingFXUtils.fromFXImage(fxImg, null);

                                    if (patternImg != null) {
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        ImageIO.write(patternImg, "png", baos);
                                        Image img = Image.getInstance(baos.toByteArray());

                                        img.scaleToFit((float) totalWidth, 12f);

                                        Chunk imgChunk = new Chunk(img, 5f, -1f, true);
                                        info.add(imgChunk);

                                        baos.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                cell.addElement(info);
                            }
                        }
                    }
                    case CHECKPOINT -> {
                        Optional<MazeModifier> checkpointModifier = grid.getModifiers().stream().filter(mazeModifier -> mazeModifier instanceof CheckpointModifier).findFirst();
                        if (checkpointModifier.isPresent()) {
                            CheckpointModifier modifier = (CheckpointModifier) checkpointModifier.get();
                            Paragraph info = new Paragraph(getFormatted("maze.print.checkpoint", modifier.getCheckpoints().size()), largerFont);
                            info.setAlignment(Element.ALIGN_LEFT);
                            cell.addElement(info);
                        }
                    }
                    case EXACT_STEPS -> {
                        Optional<MazeModifier> exactStepsModifier = grid.getModifiers().stream().filter(mazeModifier -> mazeModifier instanceof ExactStepsModifier).findFirst();
                        if (exactStepsModifier.isPresent()) {
                            ExactStepsModifier modifier = (ExactStepsModifier) exactStepsModifier.get();
                            Paragraph info = new Paragraph(getFormatted("maze.print.exact_steps", modifier.targetSteps()), largerFont);
                            info.setAlignment(Element.ALIGN_LEFT);
                            cell.addElement(info);
                        }
                    }
                    case null, default -> {
                    }
                }
            }
        } else if (game instanceof Bridge bridge) {
            Paragraph info = new Paragraph(getFormatted("export.print.bridge_info", bridge.getMaxMultipleBridges()), largerFont);
            info.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(info);
        }
    }


    public static Game generateGameOnly(ExportDetailsLocal details) {
        if (details.gameConfig() instanceof GameMode gameMode) {
            try {
                switch (gameMode) {
                    case SudokuTypes s -> {
                        SudokuInit config = new SudokuInit();
                        config.setSudokuSize(s.sudokuSize()).setSudokuVariant(s.sudokuVariant())
                                .setRegionLayout(s.regionLayout()).setPatternLayout(s.patternLayout())
                                .setDifficulty(gameMode.getDifficulty()).setSeed(details.seed()).setForExport(true);
                        return SudokuGameFactory.createGame(config);
                    }
                    case BridgeTypes b -> {
                        BridgeInit config = new BridgeInit();
                        config.setBridgeType(b.bridgeType()).setDifficulty(gameMode.getDifficulty())
                                .setSeed(details.seed()).setForExport(true);
                        if (b.width() != null) config.setWidth(b.width());
                        if (b.height() != null) config.setHeight(b.height());
                        if (b.multipleCount() != null) config.setMultipleCount(b.multipleCount());
                        return BridgeGameFactory.createGame(config);
                    }
                    case MazeTypes m -> {
                        MazeInit config = new MazeInit();
                        config.setMazeType(m.mazeType()).setMazeShape(m.mazeShape()).setMazeAlgorithm(m.mazeAlgorithm())
                                .setDifficulty(gameMode.getDifficulty()).setSeed(details.seed()).setForExport(true);
                        if (m.width() != null) config.setWidth(m.width());
                        if (m.height() != null) config.setHeight(m.height());
                        if (m.mask() != null) config.setMask(m.mask());
                        if (m.typeCounts() != null) {
                            for (var entry : m.typeCounts().entrySet()) {
                                config.setTypeCount(entry.getKey(), entry.getValue());
                            }
                        }
                        return MazeGameFactory.createGame(config);
                    }
                    case ShikakuTypes s -> {
                        ShikakuInit config = new ShikakuInit();
                        config.setDifficulty(gameMode.getDifficulty()).setSeed(details.seed()).setForExport(true);
                        if (s.width() != null) config.setWidth(s.width());
                        if (s.height() != null) config.setHeight(s.height());
                        if (s.shikakuType() != null) config.setShikakuType(s.shikakuType());
                        return ShikakuGameFactory.createGame(config);
                    }
                    default -> {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static int getGamesPerPage(Game game) {
        if (game instanceof Sudoku sudoku) {
            if (!MultiGridConfig.isMultiDoku(sudoku.getVariant())) {
                int size = sudoku.getType().getGridSize();
                if (size <= 10) return 2;
            }
            return 1;
        }
        if (game instanceof Maze maze) {
            int maxDim = Math.max(maze.getHeight(), maze.getWidth());
            if (maxDim <= 15) return 2;
            if (maxDim <= 25) return 1;
            return 1;
        }
        if (game instanceof Bridge bridge) {
            int maxDim = Math.max(bridge.getHeight(), bridge.getWidth());
            if (maxDim <= 15) return 2;
            return 1;
        }
        if (game instanceof Shikaku shikaku) {
            int maxDim = Math.max(shikaku.getHeight(), shikaku.getWidth());
            if (maxDim <= 15) return 2;
            return 1;
        }
        return 2;
    }

    public record ImageWithGame(Game game, BufferedImage image, BufferedImage solutionImage) {
    }

    public record RenderedGameData(ImageWithGame item, String title) {
    }

    public static class PdfLayoutManager {
        private final Document document;
        private final Font titleFont;
        private final Font idFont;
        private final Integer forcedGamesPerPage;
        private PdfPTable currentTable;
        private int currentGamesPerPage = -1;
        private int cellCount = 0;

        public PdfLayoutManager(Document document, Font titleFont, Font idFont, Integer forcedGamesPerPage) {
            this.document = document;
            this.titleFont = titleFont;
            this.idFont = idFont;
            this.forcedGamesPerPage = forcedGamesPerPage;
        }

        public void addGameWithFloors(Game game, PreviewType pType, String baseTitle) throws Exception {
            boolean isMultiFloor = game instanceof Maze maze && maze.isHasMultipleFloors();
            int floors = isMultiFloor ? ((Maze) game).getMazeGridFloors().size() : 1;
            int origFloor = isMultiFloor ? ((Maze) game).getCurrentFloor() : 0;

            for (int f = 0; f < floors; f++) {
                if (isMultiFloor) ((Maze) game).setCurrentFloor(f);

                String title = baseTitle;
                if (pType == PreviewType.SOLVED) {
                    title = getFormatted("export.solution.label", baseTitle);
                }
                if (isMultiFloor) {
                    title += " " + getFormatted("export.level.label", f + 1, floors);
                }

                ImageWithGame item = render(game, pType);
                BufferedImage targetImage = (pType == PreviewType.SOLVED) ? item.solutionImage() : item.image();

                if (targetImage != null) {
                    addSinglePageCell(targetImage, game, title);
                }
            }

            if (isMultiFloor) ((Maze) game).setCurrentFloor(origFloor);
        }

        private void addSinglePageCell(BufferedImage image, Game game, String titleText) throws Exception {
            int neededGamesPerPage = (forcedGamesPerPage != null) ? forcedGamesPerPage : getGamesPerPage(game);

            int cols = (neededGamesPerPage >= 4) ? 2 : 1;
            float rows = (neededGamesPerPage >= 5) ? 3 : (neededGamesPerPage >= 2) ? 2 : 1;

            float availablePageHeight = PageSize.A4.getHeight() - 72f;
            float rowHeight = availablePageHeight / rows;

            if (currentTable != null && (neededGamesPerPage != currentGamesPerPage || cellCount >= currentGamesPerPage)) {
                flush();
            }

            if (currentTable == null) {
                currentTable = new PdfPTable(cols);
                currentTable.setWidthPercentage(100);
                currentTable.setSplitLate(true);
                currentGamesPerPage = neededGamesPerPage;
                cellCount = 0;
            }

            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(10f);
            cell.setFixedHeight(rowHeight);

            Paragraph title = new Paragraph(titleText, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5f);
            cell.addElement(title);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            Image img = Image.getInstance(baos.toByteArray());

            float maxWidth = (cols == 2) ? (PageSize.A4.getWidth() / 2f) - 30f : PageSize.A4.getWidth() - 60f;
            float maxHeight = rowHeight - 85f;

            img.scaleToFit(maxWidth, maxHeight);
            img.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(img);

            String seedText = SeedCreator.createSeed(game);
            Font seedFont = new Font(idFont);

            if (seedText.length() > 60) {
                seedFont.setSize(idFont.getSize() - 3.5f);
            } else if (seedText.length() > 35) {
                seedFont.setSize(idFont.getSize() - 2.0f);
            } else {
                seedFont.setSize(idFont.getSize() - 0.5f);
            }

            Paragraph id = new Paragraph(seedText, seedFont);
            id.setAlignment(Element.ALIGN_RIGHT);
            id.setSpacingBefore(3f);
            id.setLeading(seedFont.getSize() + 2f);
            cell.addElement(id);

            addAdditionalGameInfo(cell, game, idFont);

            currentTable.addCell(cell);
            cellCount++;

            baos.flush();
            baos.close();
            image.flush();
        }

        public void flush() throws DocumentException {
            if (currentTable != null && cellCount > 0) {
                int cols = currentTable.getNumberOfColumns();
                while (cellCount % cols != 0) {
                    PdfPCell empty = new PdfPCell();
                    empty.setBorder(Rectangle.NO_BORDER);
                    currentTable.addCell(empty);
                    cellCount++;
                }
                document.add(currentTable);
                document.newPage();
            }
            currentTable = null;
            cellCount = 0;
        }
    }
}
