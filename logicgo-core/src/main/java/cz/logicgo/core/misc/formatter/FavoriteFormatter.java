package cz.logicgo.core.misc.formatter;


import cz.logicgo.core.entity.export.ExportDetail;
import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.export.ExportDetailsLocal;
import cz.logicgo.core.gameClasses.favorites.*;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cz.logicgo.core.misc.Messages.getFormatted;


public class FavoriteFormatter {

    public static String format(GameFavorite favorite) {
        if (favorite == null) return "";
        Game dummyGame = convertFavoriteToDummyGame(favorite);
        return parseGameDetails(dummyGame);
    }


    public static String format(Game game) {
        if (game == null) return "";
        return parseGameDetails(game);
    }

    public static String generateExportDescription(int totalGames, List<ExportDetailsLocal> gameModes, ExportDetail exportDetail, List<Game> games) {
        if ((games == null || games.isEmpty()) &&
                (gameModes == null || gameModes.isEmpty()) &&
                (exportDetail == null || exportDetail.getExportedGames() == null || exportDetail.getExportedGames().isEmpty())) {
            return getFormatted("export.multi_export.name") + " (" + totalGames + " ks)";
        }

        boolean sameClass = false;
        TypeGame typeGame = null;

        if (games != null && !games.isEmpty()) {
            Class<? extends Game> firstGameClass = games.getFirst().getClass();
            sameClass = games.stream()
                    .filter(Objects::nonNull)
                    .allMatch(g -> g.getClass().equals(firstGameClass));
            if (sameClass) {
                typeGame = games.getFirst().getGameType();
            }
        } else if (exportDetail != null && exportDetail.getExportedGames() != null && !exportDetail.getExportedGames().isEmpty()) {
            var exportedGames = exportDetail.getExportedGames();
            Class<? extends Game> firstGameClass = exportedGames.getFirst().getClass();
            sameClass = exportedGames.stream()
                    .filter(Objects::nonNull)
                    .allMatch(g -> g.getClass().equals(firstGameClass));
            if (sameClass) {
                typeGame = exportedGames.getFirst().getGameType();
            }
        } else if (gameModes != null && !gameModes.isEmpty()) {
            var firstMode = gameModes.getFirst();
            if (firstMode != null && firstMode.gameConfig() != null) {
                Class<?> firstConfigClass = firstMode.gameConfig().getClass();
                sameClass = gameModes.stream()
                        .filter(Objects::nonNull)
                        .map(ExportDetailsLocal::gameConfig)
                        .filter(Objects::nonNull)
                        .allMatch(config -> config.getClass().equals(firstConfigClass));
                if (sameClass) {
                    typeGame = firstMode.gameConfig().getTypeGame();
                }
            }
        }

        if (sameClass && typeGame != null) {
            String singleGameName = switch (typeGame) {
                case SUDOKU, MAZE, BRIDGE, SHIKAKU -> typeGame.getTranslation();
                default -> "";
            };
            return singleGameName + " (" + totalGames + " ks)";
        } else {
            return getFormatted("export.multi_export.name") + " (" + totalGames + " ks)";
        }
    }

    public static String parseGameDetails(Game game) {
        if (game == null) return "";

        String difficulty = game.getDifficulty() != null ? game.getDifficulty().getTranslation() : "";
        String classicFallback = getFormatted("export.detail.fallback.classic");

        switch (game) {
            case Sudoku sudoku -> {
                String variantName = sudoku.getVariant() != null ? sudoku.getVariant().getTranslation() : classicFallback;
                int size = sudoku.getType() != null ? sudoku.getType().getGridSize() : sudoku.getWidth();
                return getFormatted("export.detail.sudoku", variantName, size, size, difficulty);
            }
            case Maze maze -> {
                String shape = maze.getMazeShape() != null ? maze.getMazeShape().getTranslation() : getFormatted("export.detail.fallback.square");
                String type = maze.getMazeType() != null ? maze.getMazeType().getTranslation() : classicFallback;
                if (maze.isHasMultipleFloors()) {
                    int floors = maze.getMazeGridFloors() != null ? maze.getMazeGridFloors().size() : 1;
                    return getFormatted("export.detail.maze.multi", type, shape, maze.getWidth(), maze.getHeight(), floors, difficulty);
                }
                return getFormatted("export.detail.maze.single", type, shape, maze.getWidth(), maze.getHeight(), difficulty);
            }
            case Bridge bridge -> {
                String type = bridge.getType() != null ? bridge.getType().getTranslation() : classicFallback;
                if (bridge.getType() == BridgeType.MULTIPLE) {
                    type += getFormatted("export.detail.bridge.multiple_suffix", bridge.getMaxMultipleBridges());
                }
                return getFormatted("export.detail.bridge", type, bridge.getWidth(), bridge.getHeight(), difficulty);
            }
            case Shikaku shikaku -> {
                String type = shikaku.getShikakuType() != null ? shikaku.getShikakuType().getTranslation() : classicFallback;
                return getFormatted("export.detail.shikaku", type, shikaku.getWidth(), shikaku.getHeight(), difficulty);
            }
            default -> {
                return getFormatted("export.detail.generic", game.getWidth(), game.getHeight(), difficulty);
            }
        }
    }

    private static Game convertFavoriteToDummyGame(GameFavorite favorite) {
        switch (favorite) {
            case SudokuFavorite sFav -> {
                Sudoku s = new Sudoku();
                applyBaseProperties(favorite, s);
                s.setVariant(sFav.getVariant());
                s.setType(sFav.getSize());
                return s;
            }
            case BridgeFavorite bFav -> {
                Bridge b = new Bridge();
                applyBaseProperties(favorite, b);
                b.setType(bFav.getBridgeType());
                b.setMaxMultipleBridges(bFav.getMultipleCount());
                return b;
            }
            case MazeFavorite mFav -> {
                Maze m = new Maze();
                applyBaseProperties(favorite, m);
                m.setMazeType(mFav.getMazeType());
                m.setMazeShape(mFav.getMazeShape());

                if (mFav.isHasMultipleFloors() && mFav.getFloorCount() > 1) {
                    List<MazeGrid> dummyFloors = new ArrayList<>();
                    for (int i = 0; i < mFav.getFloorCount(); i++) {
                        dummyFloors.add(null);
                    }
                    m.setMazeGridFloors(dummyFloors);
                }
                return m;
            }
            case ShikakuFavorite shFav -> {
                Shikaku sh = new Shikaku();
                applyBaseProperties(favorite, sh);
                sh.setShikakuType(shFav.getShikakuType());
                return sh;
            }
        }
    }


    private static void applyBaseProperties(GameFavorite src, Game dest) {
        dest.setWidth(src.getWidth());
        dest.setHeight(src.getHeight());
        dest.setDifficulty(src.getDifficulty());
    }
}
