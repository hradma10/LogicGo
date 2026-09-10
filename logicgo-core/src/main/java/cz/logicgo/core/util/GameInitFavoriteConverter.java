package cz.logicgo.core.util;

import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.factoryInit.bridge.BridgeInit;
import cz.logicgo.core.factoryInit.maze.MazeInit;
import cz.logicgo.core.factoryInit.shikaku.ShikakuInit;
import cz.logicgo.core.factoryInit.sudoku.SudokuInit;
import cz.logicgo.core.gameClasses.favorites.*;

public final class GameInitFavoriteConverter {

    private GameInitFavoriteConverter() {}

    public static GameInit toGameInit(GameFavorite favorite, User user) {
        if (favorite == null) return null;

        return switch (favorite) {
            case SudokuFavorite sFav -> {
                SudokuInit init = new SudokuInit(user);
                init.setDifficulty(sFav.getDifficulty());
                init.setSudokuVariant(sFav.getVariant());
                init.setSudokuSize(sFav.getSize());
                init.setRegionLayout(sFav.getRegionLayout());
                init.setPatternLayout(sFav.getPatternLayout());
                yield init;
            }
            case BridgeFavorite bFav -> {
                BridgeInit init = new BridgeInit(user);
                init.setDifficulty(bFav.getDifficulty());
                init.setWidth(bFav.getWidth());
                init.setHeight(bFav.getHeight());
                init.setBridgeType(bFav.getBridgeType());
                init.setMultipleCount(bFav.getMultipleCount());
                init.setIslandCount(bFav.getIslandCount());
                yield init;
            }
            case MazeFavorite mFav -> {
                MazeInit init = new MazeInit(user);
                init.setDifficulty(mFav.getDifficulty());
                init.setWidth(mFav.getWidth());
                init.setHeight(mFav.getHeight());
                init.setMazeType(mFav.getMazeType());
                init.setMazeAlgorithm(mFav.getMazeAlgorithm());
                init.setMazeShape(mFav.getMazeShape());
                init.setHasMultipleFloors(mFav.isHasMultipleFloors());
                init.setMask(mFav.getMask());
                if (mFav.getTypeCount() != null) {
                    mFav.getTypeCount().forEach(init::setTypeCount);
                }
                yield init;
            }
            case ShikakuFavorite shFav -> {
                ShikakuInit init = new ShikakuInit(user);
                init.setDifficulty(shFav.getDifficulty());
                init.setWidth(shFav.getWidth());
                init.setHeight(shFav.getHeight());
                init.setShikakuType(shFav.getShikakuType());
                yield init;
            }
        };
    }

    public static GameInit toGameInit(GameFavorite favorite) {
        return toGameInit(favorite, null);
    }

    public static GameFavorite toFavorite(GameInit gameInit) {
        if (gameInit == null) return null;

        return switch (gameInit) {
            case SudokuInit sInit -> {
                SudokuFavorite fav = new SudokuFavorite();
                fav.setDifficulty(sInit.getDifficulty());
                fav.setVariant(sInit.getSudokuVariant());
                fav.setSize(sInit.getSudokuSize());
                if (sInit.getSudokuSize() != null) {
                    fav.setWidth(sInit.getSudokuSize().getGridSize());
                    fav.setHeight(sInit.getSudokuSize().getGridSize());
                }
                fav.setRegionLayout(sInit.getRegionLayout());
                fav.setPatternLayout(sInit.getPatternLayout());
                yield fav;
            }
            case BridgeInit bInit -> {
                BridgeFavorite fav = new BridgeFavorite();
                fav.setDifficulty(bInit.getDifficulty());
                fav.setWidth(bInit.getWidth());
                fav.setHeight(bInit.getHeight());
                fav.setBridgeType(bInit.getBridgeType());
                fav.setMultipleCount(bInit.getMultipleCount());
                fav.setIslandCount(bInit.getIslandCount());
                yield fav;
            }
            case MazeInit mInit -> {
                MazeFavorite fav = new MazeFavorite();
                fav.setDifficulty(mInit.getDifficulty());
                fav.setWidth(mInit.getWidth());
                fav.setHeight(mInit.getHeight());
                fav.setMazeType(mInit.getMazeType());
                fav.setMazeAlgorithm(mInit.getMazeAlgorithm());
                fav.setMazeShape(mInit.getMazeShape());
                fav.setHasMultipleFloors(mInit.isHasMultipleFloors());
                fav.setMask(mInit.getMask());
                if (mInit.getTypeCount() != null) {
                    fav.getTypeCount().putAll(mInit.getTypeCount());
                }
                yield fav;
            }
            case ShikakuInit shInit -> {
                ShikakuFavorite fav = new ShikakuFavorite();
                fav.setDifficulty(shInit.getDifficulty());
                fav.setWidth(shInit.getWidth());
                fav.setHeight(shInit.getHeight());
                fav.setShikakuType(shInit.getShikakuType());
                yield fav;
            }
            default -> throw new IllegalArgumentException("Unsupported GameInit type: " + gameInit.getClass().getName());
        };
    }
}
