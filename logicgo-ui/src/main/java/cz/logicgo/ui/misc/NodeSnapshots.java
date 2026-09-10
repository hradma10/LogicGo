package cz.logicgo.ui.misc;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.persistence.services.GameService;
import cz.logicgo.ui.renderers.BridgeRenderer;
import cz.logicgo.ui.renderers.ShikakuRenderer;
import cz.logicgo.ui.renderers.maze.MazeRenderer;
import cz.logicgo.ui.renderers.sudoku.SudokuRenderer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.createSudokuForPrint;
import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.flattenBoard;


public class NodeSnapshots {
    private static final String sharedDataPath = System.getenv("ProgramData");
    private static final String appFolderName = "LogicGo";
    private static final String appFolderThumbnailsPath = Paths.get(sharedDataPath, appFolderName, "thumbs").toString();

    private static final GameService gameService = new GameService();

    static {
        deleteNotUsedThumbnails();
    }

    public interface OutsideCluesDrawer {
        void draw(Canvas targetCanvas, double paddingX, double paddingY, double gridWidth, double gridHeight);
    }

    public NodeSnapshots() {
    }

    public static void deleteNotUsedThumbnails() {
        Path thumbsPath = Path.of(appFolderThumbnailsPath);

        if (!Files.exists(thumbsPath)) {
            return;
        }

        List<String> paths = gameService.getAllThumbnailPaths();
        List<String> existingFiles = new ArrayList<>();
        try (Stream<Path> st = Files.list(thumbsPath)) {
            st.forEach(path -> {
                String fileName = path.getFileName().toString();
                existingFiles.add(fileName);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        existingFiles.removeAll(paths);

        for (String fileName : existingFiles) {
            if (fileName == null) continue;
            File file = Paths.get(appFolderThumbnailsPath, fileName).toFile();
            file.delete();
        }
    }

    private static BufferedImage createArgbImage(Canvas canvas, int width, int height) {
        var image = new WritableImage(width, height);
        var parameters = new SnapshotParameters();
        canvas.snapshot(parameters, image);
        return SwingFXUtils.fromFXImage(image, null);
    }

    private static BufferedImage createRgbImage(BufferedImage argbBufferedImage) {
        var rgbBufferedImage = new BufferedImage(argbBufferedImage.getWidth(), argbBufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        rgbBufferedImage.createGraphics().drawImage(argbBufferedImage, 0, 0, Color.WHITE, null);
        return rgbBufferedImage;
    }


    public static BufferedImage createThumbnailFromCanvas(Canvas canvas, double scale) {
        if (canvas == null || scale <= 0 || scale > 1) {
            throw new IllegalArgumentException();
        }

        int scaledWidth = (int) Math.ceil(canvas.getWidth() * scale);
        int scaledHeight = (int) Math.ceil(canvas.getHeight() * scale);

        WritableImage fxImage = new WritableImage(scaledWidth, scaledHeight);
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(Transform.scale(scale, scale));

        canvas.snapshot(params, fxImage);
        return SwingFXUtils.fromFXImage(fxImage, null);
    }

    public static javafx.scene.image.Image loadThumbnail(String thumbnailName) {
        try {
            if (thumbnailName == null || thumbnailName.isEmpty()) return null;
            String filePath = Paths.get(appFolderThumbnailsPath, thumbnailName).toString();
            Path path = Paths.get(filePath);

            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return null;
            }
            String uri = path.toUri().toString();
            return new javafx.scene.image.Image(uri);
        } catch (Exception e) {
            return null;
        }
    }

    public static void removeThumbnail(String thumbnailName) {
        try {
            if (thumbnailName == null || thumbnailName.isEmpty()) return;
            Files.deleteIfExists(Paths.get(appFolderThumbnailsPath, thumbnailName));
        } catch (Exception _) {

        }
    }

    public void makeThumbnail(Game game, double width, double height) {
        String newThumbnailName = String.format("%s_%s.png", UUID.randomUUID(), System.currentTimeMillis());
        String newFilePath = Paths.get(appFolderThumbnailsPath, newThumbnailName).toString();

        try {
            Files.createDirectories(Path.of(appFolderThumbnailsPath));
        } catch (Exception e) {

            return;
        }

        Runnable snapshotTask = () -> {
            try {
                Canvas canvas = new Canvas(width, height);
                if (game instanceof Sudoku sudokuExport) {
                    Sudoku sudoku = createSudokuForPrint(sudokuExport);
                    SudokuRenderer.renderFullBoard(canvas, sudoku, true);
                } else if (game instanceof Maze maze) {
                    var grid = maze.getMazeGridFloors().get(maze.getCurrentFloor()).getMazeGrid();
                    MazeRenderer.renderGrid(canvas, grid, 0, 1, true, game.getPlayer());
                } else if (game instanceof Bridge bridge) {
                    BridgeRenderer.render(canvas, bridge, true);
                } else if (game instanceof Shikaku shikaku) {
                    ShikakuRenderer.render(canvas, shikaku);
                }

                BufferedImage bufferedImage = createThumbnailFromCanvas(canvas, 0.5);
                ImageIO.write(bufferedImage, "png", new File(newFilePath));

                if (game.getThumbnailName() != null) {
                    try {
                        Files.deleteIfExists(Paths.get(appFolderThumbnailsPath, game.getThumbnailName()));
                    } catch (Exception e) {

                    }
                }

                game.setThumbnailName(newThumbnailName);

            } catch (Exception exception) {

                try {
                    Files.deleteIfExists(Paths.get(newFilePath));
                } catch (Exception ex) {

                }
            }
        };

        if (Platform.isFxApplicationThread()) {
            snapshotTask.run();
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    snapshotTask.run();
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void exportTrueHighResolution(File outputFile, Game gameToDraw, int width, int height, boolean hasOutsideClues, OutsideCluesDrawer cluesDrawer) {
        if (gameToDraw == null) return;

        Runnable exportTask = () -> {
            try {
                double paddingX = hasOutsideClues ? width * 0.05 : 0;
                double paddingY = hasOutsideClues ? height * 0.05 : 0;
                double gridWidth = width - 2 * paddingX;
                double gridHeight = height - 2 * paddingY;

                Canvas clueCanvas = new Canvas(width, height);
                Canvas gridCanvas = new Canvas(gridWidth, gridHeight);

                GraphicsContext clueGc = clueCanvas.getGraphicsContext2D();
                clueGc.setFill(javafx.scene.paint.Color.WHITE);
                clueGc.fillRect(0, 0, width, height);

                if (hasOutsideClues && cluesDrawer != null) {
                    cluesDrawer.draw(clueCanvas, paddingX, paddingY, gridWidth, gridHeight);
                }

                switch (gameToDraw) {
                    case Sudoku sudokuExport -> {
                        Arrays.stream(flattenBoard(sudokuExport.getBoard())).filter(Objects::nonNull).forEach(cell -> cell.setHints(true));
                        SudokuRenderer.renderFullBoard(gridCanvas, sudokuExport, true);
                    }
                    case Bridge bridge -> BridgeRenderer.render(gridCanvas, bridge, true);
                    case Maze maze -> MazeRenderer.renderGrid(gridCanvas, maze.getMazeGrid(), 0, 1, true, null);
                    case Shikaku shikaku -> ShikakuRenderer.render(gridCanvas, shikaku, true);
                    default -> {
                    }
                }

                javafx.scene.layout.Pane virtualPane = new javafx.scene.layout.Pane();
                gridCanvas.setLayoutX(paddingX);
                gridCanvas.setLayoutY(paddingY);
                virtualPane.getChildren().addAll(clueCanvas, gridCanvas);

                SnapshotParameters params = new SnapshotParameters();
                params.setFill(javafx.scene.paint.Color.TRANSPARENT);

                WritableImage fxImage = new WritableImage(width, height);
                virtualPane.snapshot(params, fxImage);

                BufferedImage bImage = SwingFXUtils.fromFXImage(fxImage, null);
                ImageIO.write(bImage, "png", outputFile);


            } catch (Exception e) {

                e.printStackTrace();
            }
        };

        if (Platform.isFxApplicationThread()) {
            exportTask.run();
        } else {
            Platform.runLater(exportTask);
        }
    }

    public Document convertToPdf(String path, ArrayList<BufferedImage> bufferedImages) throws IOException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(path));
        document.open();

        for (int i = 0; i < bufferedImages.size(); i++) {
            BufferedImage bufferedImage = bufferedImages.get(i);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            baos.close();

            Image image = Image.getInstance(imageBytes);
            image.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
            image.setAlignment(Image.ALIGN_TOP);

            document.add(image);

            if (i < bufferedImages.size() - 1) {
                document.newPage();
            }
        }

        document.close();
        return document;
    }
}
