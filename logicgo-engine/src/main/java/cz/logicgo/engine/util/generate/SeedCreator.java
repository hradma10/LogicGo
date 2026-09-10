package cz.logicgo.engine.util.generate;


import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.factoryInit.bridge.BridgeInit;
import cz.logicgo.core.factoryInit.maze.MazeInit;
import cz.logicgo.core.factoryInit.shikaku.ShikakuInit;
import cz.logicgo.core.factoryInit.sudoku.SudokuInit;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.gameClasses.viewers.CustomMask;
import cz.logicgo.core.gameClasses.viewers.SudokuPattern;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.engine.algorithms.sudoku.custom.CustomLayoutsLoader;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Objects;

import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.toIntArray;
import static cz.logicgo.core.util.boardConverters.MazeConverters.booleanMaskToInt;
import static cz.logicgo.core.util.boardConverters.MazeConverters.intMaskToBoolean;


public class SeedCreator {


    public static String encodeGrid(Integer[][] grid) {
        return encodeGrid(toIntArray(grid));
    }

    public static String encodeGrid(boolean[][] grid) {
        return encodeGrid(booleanMaskToInt(grid));
    }

    public static String encodeGrid(int[][] grid) {
        String horizontal = "H" + encodeHorizontal(grid);
        String vertical = "V" + encodeVertical(grid);
        String uncompressed = "U" + encodeRaw(grid);

        String best = horizontal;
        if (vertical.length() < best.length()) best = vertical;
        if (uncompressed.length() < best.length()) best = uncompressed;

        return best;
    }

    private static String encodeHorizontal(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        char lastChar = '\0';

        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                char currentChar = (char) ('a' + grid[r][c] + 1);
                if (currentChar == lastChar) {
                    count++;
                } else {
                    if (count > 0) {
                        if (count > 1) sb.append(count);
                        sb.append(lastChar);
                    }
                    lastChar = currentChar;
                    count = 1;
                }
            }
        }
        if (count > 0) {
            if (count > 1) sb.append(count);
            sb.append(lastChar);
        }
        return sb.toString();
    }

    private static String encodeVertical(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        char lastChar = '\0';
        int cols = grid[0].length;
        int rows = grid.length;

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                char currentChar = (char) ('a' + grid[r][c] + 1);
                if (currentChar == lastChar) {
                    count++;
                } else {
                    if (count > 0) {
                        if (count > 1) sb.append(count);
                        sb.append(lastChar);
                    }
                    lastChar = currentChar;
                    count = 1;
                }
            }
        }
        if (count > 0) {
            if (count > 1) sb.append(count);
            sb.append(lastChar);
        }
        return sb.toString();
    }

    private static String encodeRaw(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : grid) {
            for (int val : row) {
                sb.append((char) ('a' + val + 1));
            }
        }
        return sb.toString();
    }

    public static int[][] decodeGrid(String encoded, int width, int height) {
        char direction = encoded.charAt(0);
        String data = encoded.substring(1);

        int[][] grid = new int[height][width];
        int count = 0;

        if (direction == 'U') {
            int i = 0;
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < width; c++) {
                    if (i < data.length()) {
                        grid[r][c] = data.charAt(i) - 'a' - 1;
                        i++;
                    }
                }
            }
        } else if (direction == 'H') {
            int r = 0, c = 0;
            for (int i = 0; i < data.length(); i++) {
                char ch = data.charAt(i);
                if (Character.isDigit(ch)) {
                    count = count * 10 + (ch - '0');
                } else {
                    if (count == 0) count = 1;
                    int val = ch - 'a' - 1;
                    for (int k = 0; k < count; k++) {
                        if (r < height && c < width) grid[r][c] = val;
                        c++;
                        if (c >= width) {
                            c = 0;
                            r++;
                        }
                    }
                    count = 0;
                }
            }
        } else if (direction == 'V') {
            int r = 0, c = 0;
            for (int i = 0; i < data.length(); i++) {
                char ch = data.charAt(i);
                if (Character.isDigit(ch)) {
                    count = count * 10 + (ch - '0');
                } else {
                    if (count == 0) count = 1;
                    int val = ch - 'a' - 1;
                    for (int k = 0; k < count; k++) {
                        if (r < height && c < width) grid[r][c] = val;
                        r++;
                        if (r >= height) {
                            r = 0;
                            c++;
                        }
                    }
                    count = 0;
                }
            }
        } else {
            return null;
        }

        return grid;
    }


    public static SudokuPatternLayout resolvePattern(SudokuPatternLayout patternLayout, User user) {
        if (patternLayout == null || patternLayout.getPattern() == null) {
            return patternLayout;
        }

        if (user != null && user.getCustomPatterns() != null) {
            Integer[][] gridObj = patternLayout.getPattern();
            return user.getCustomPatterns().stream()
                    .map(SudokuPattern::getLayout)
                    .filter(p -> p != null && Objects.deepEquals(p.getPattern(), gridObj))
                    .findFirst()
                    .orElse(patternLayout);
        }

        return patternLayout;
    }

    public static boolean[][] resolveMask(boolean[][] mask, TypeGame gameType, User user) {
        if (mask == null) {
            return null;
        }

        if (user != null && user.getCustomMasks() != null) {
            return user.getCustomMasks().stream()
                    .filter(m -> m.getGameType() == gameType && Arrays.deepEquals(m.getLayout(), mask))
                    .map(CustomMask::getLayout)
                    .findFirst()
                    .orElse(mask);
        }

        return mask;
    }

    public static GameInit convertToGameInit(Game game) {
        if (game == null) return null;

        GameInit gameInit = switch (game) {
            case Sudoku sudoku -> {
                SudokuInit sudokuInit = new SudokuInit();
                sudokuInit.setSudokuVariant(sudoku.getVariant());
                sudokuInit.setSudokuSize(sudoku.getType());
                sudokuInit.setRegionLayout(sudoku.getRegionLayout());
                sudokuInit.setPatternLayout(sudoku.getPattern());
                yield sudokuInit;
            }
            case Maze maze -> {
                MazeInit mazeInit = new MazeInit();
                mazeInit.setMazeType(maze.getMazeType());
                mazeInit.setMazeAlgorithm(maze.getMazeAlgorithm());
                mazeInit.setMazeShape(maze.getMazeShape());
                mazeInit.setWidth(maze.getWidth());
                mazeInit.setHeight(maze.getHeight());
                mazeInit.setHasMultipleFloors(maze.isHasMultipleFloors());
                mazeInit.setMask(intMaskToBoolean(maze.getMazeGridFloors().getFirst().getMazeGrid().getMask()));

                if (maze.isHasMultipleFloors() && maze.getMazeGridFloors() != null) {
                    EnumMap<MazeType, Integer> typeCount = new EnumMap<>(MazeType.class);
                    maze.getMazeGridFloors().forEach(floor -> typeCount.merge(floor.getMazeGrid().getMazeType(), 1, Integer::sum));
                    mazeInit.getTypeCount().putAll(typeCount);
                }
                yield mazeInit;
            }
            case Bridge bridge -> {
                BridgeInit bridgeInit = new BridgeInit();
                bridgeInit.setBridgeType(bridge.getType());
                bridgeInit.setHeight(bridge.getHeight());
                bridgeInit.setWidth(bridge.getWidth());
                bridgeInit.setMultipleCount(bridge.getMaxMultipleBridges());
                yield bridgeInit;
            }
            case Shikaku shikaku -> {
                ShikakuInit shikakuInit = new ShikakuInit();
                shikakuInit.setShikakuType(shikaku.getShikakuType());
                shikakuInit.setWidth(shikaku.getWidth());
                shikakuInit.setHeight(shikaku.getHeight());
                yield shikakuInit;
            }
            default -> throw new IllegalStateException("Unexpected value: " + game);
        };

        gameInit.setPlayer(game.getPlayer());
        gameInit.setDifficulty(game.getDifficulty());
        gameInit.setSeed(game.getSeed());
        gameInit.setForExport(false);

        return gameInit;
    }

    public static String createSeed(Game game) {
        return createSeed(convertToGameInit(game));
    }

    public static String createSeed(GameInit gameInit) {
        StringBuilder sb = new StringBuilder();

        TypeGame game = gameInit.getTypeGame();
        Difficulty difficulty = gameInit.getDifficulty();
        sb.append(game.getId()).append(";");
        sb.append(difficulty.getId()).append(";");

        switch (game) {
            case SUDOKU -> {
                SudokuInit sudokuInit = (SudokuInit) gameInit;

                SudokuVariant variant = sudokuInit.getSudokuVariant();
                sb.append(variant.getId()).append(";");

                SudokuSize size = sudokuInit.getSudokuSize();
                sb.append(size.getGridSize()).append(";");

                switch (variant) {
                    case CLASSIC -> {
                        SudokuRegionLayout layout = sudokuInit.getRegionLayout();
                        if (layout.getType() == SudokuRegionLayout.getCUSTOM_TYPE_ID()) {
                            throw new IllegalStateException("SudokuRegionLayout CUSTOM_TYPE_ID for classic is not supported");
                        }
                        int type = layout.getType();
                        sb.append(type).append(";");
                    }
                    case PATTERNED -> {
                        SudokuPatternLayout patternLayout = sudokuInit.getPatternLayout();
                        boolean isCustom = patternLayout != null && patternLayout.getType() == SudokuRegionLayout.getCUSTOM_TYPE_ID();
                        if (isCustom) {
                            String grid = encodeGrid(patternLayout.getGrid());
                            sb.append(1).append(";").append(grid).append(";");
                        } else {
                            int typeId = patternLayout != null ? patternLayout.getType() : 0;
                            sb.append(0).append(";").append(typeId).append(";");
                        }
                    }
                    case IRREGULAR -> {
                        SudokuRegionLayout layout = sudokuInit.getRegionLayout();
                        if (layout == null) {
                            layout = CustomLayoutsLoader.getBasicLayout(size);
                        }
                        if (layout != null && layout.getType() == SudokuRegionLayout.getCUSTOM_TYPE_ID()) {
                            int idInDB = CustomLayoutsLoader.getIrregularRegionId(size.getGridSize(), toIntArray(layout.getGrid()));
                            sb.append(1).append(";").append(idInDB).append(";");
                        } else {
                            int type = layout != null ? layout.getType() : 0;
                            sb.append(0).append(";").append(type).append(";");
                        }
                    }
                    default -> {
                    }
                }
            }
            case MAZE -> {
                MazeInit mazeInit = (MazeInit) gameInit;

                sb.append(mazeInit.getMazeType().getId()).append(";");
                sb.append(mazeInit.getMazeAlgorithm().getId()).append(";");
                sb.append(mazeInit.getMazeShape().getId()).append(";");

                sb.append(mazeInit.getWidth()).append(";")
                        .append(mazeInit.getHeight()).append(";");

                boolean[][] mask = mazeInit.getMask();
                boolean hasCustomOmissions = false;

                if (mask != null) {
                    for (int r = 0; r < mazeInit.getHeight(); r++) {
                        for (int c = 0; c < mazeInit.getWidth(); c++) {
                            if (!mask[r][c]) {
                                hasCustomOmissions = true;
                                break;
                            }
                        }
                        if (hasCustomOmissions) break;
                    }
                }

                if (hasCustomOmissions) {
                    sb.append(1).append(";").append(encodeGrid(mask)).append(";");
                } else {
                    sb.append(0).append(";");
                }

                if (mazeInit.getMazeType() == MazeType.MULTI_LEVEL) {
                    mazeInit.getTypeCount().forEach((type, count) -> {
                        if (count != null && count > 0) {
                            sb.append(type.getId()).append(":").append(count).append("_");
                        }
                    });
                    sb.append(";");
                }
            }
            case BRIDGE -> {
                BridgeInit bridgeInit = (BridgeInit) gameInit;

                BridgeType bridgeType = bridgeInit.getBridgeType();
                sb.append(bridgeType.getId()).append(";");

                int height = bridgeInit.getHeight();
                int width = bridgeInit.getWidth();

                sb.append(height).append(";").append(width).append(";");

                if (bridgeType == BridgeType.MULTIPLE) {
                    int multipleCount = bridgeInit.getMultipleCount();
                    sb.append(multipleCount).append(";");
                }
            }
            case SHIKAKU -> {
                ShikakuInit shikakuInit = (ShikakuInit) gameInit;

                sb.append(shikakuInit.getShikakuType().getId()).append(";");
                sb.append(shikakuInit.getWidth()).append(";");
                sb.append(shikakuInit.getHeight()).append(";");
            }
        }

        Long seed = gameInit.getSeed();
        if (seed == null) {
            seed = RandomizationFactory.generateSeed();
            gameInit.setSeed(seed);
        }

        sb.append(seed);

        return sb.toString();
    }

    public static long hashToLong(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            long result = 0;
            for (int i = 0; i < 8; i++) {
                result = (result << 8) | (hash[i] & 0xFF);
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    public static GameInit parseSeed(String seed, User user) {
        if (seed == null || seed.isEmpty()) return null;

        String[] parts = seed.split(";");
        int i = 0;
        GameInit gameInit = null;

        TypeGame gameType = PersistableEnum.fromId(Integer.parseInt(parts[i++]), TypeGame.class);
        Difficulty difficulty = PersistableEnum.fromId(Integer.parseInt(parts[i++]), Difficulty.class);

        switch (gameType) {
            case SUDOKU -> {
                SudokuInit sudokuInit = new SudokuInit();
                sudokuInit.setDifficulty(difficulty);
                sudokuInit.setSudokuVariant(PersistableEnum.fromId(Integer.parseInt(parts[i++]), SudokuVariant.class));
                SudokuSize size = SudokuSize.getTypeByGridSize(Integer.parseInt(parts[i++]));
                int sizeInt = size.getGridSize();
                sudokuInit.setSudokuSize(size);

                switch (sudokuInit.getSudokuVariant()) {
                    case CLASSIC -> {
                        int typeId = Integer.parseInt(parts[i++]);
                        SudokuRegionLayout layout = CustomLayoutsLoader.getTypeOfRegionSizeBySize(sizeInt, typeId);
                        if (layout != null) {
                            sudokuInit.setRegionLayout(layout);
                        } else {
                            sudokuInit.setRegionLayout(CustomLayoutsLoader.getBasicLayout(sizeInt));
                        }
                    }
                    case PATTERNED -> {
                        boolean isCustom = Integer.parseInt(parts[i++]) == 1;

                        int[][] grid = decodeGrid(parts[i++], sizeInt, sizeInt);
                        if (grid == null) throw new IllegalArgumentException("Invalid pattern grid in seed");
                        SudokuPatternLayout layout = CustomLayoutsLoader.createPatternFromData(grid, true);
                        sudokuInit.setPatternLayout(resolvePattern(layout, user));
                    }
                    case IRREGULAR -> {
                        int isInDB = Integer.parseInt(parts[i++]);
                        if (isInDB == 1) {
                            int dbIndex = Integer.parseInt(parts[i++]);
                            int[][] dbGrid = CustomLayoutsLoader.getIrregularRegionBySize(sizeInt, dbIndex);
                            if (dbGrid != null) {
                                sudokuInit.setRegionLayout(CustomLayoutsLoader.createLayoutFromData(dbGrid));
                            } else {
                                sudokuInit.setRegionLayout(CustomLayoutsLoader.getBasicLayout(sizeInt));
                            }
                        } else {
                            int type = Integer.parseInt(parts[i++]);
                            SudokuRegionLayout layout = CustomLayoutsLoader.getTypeOfRegionSizeBySize(sizeInt, type);
                            sudokuInit.setRegionLayout(layout != null ? layout : CustomLayoutsLoader.getBasicLayout(sizeInt));
                        }
                    }
                }
                gameInit = sudokuInit;
            }

            case MAZE -> {
                MazeInit mazeInit = new MazeInit();
                mazeInit.setDifficulty(difficulty);
                mazeInit.setMazeType(PersistableEnum.fromId(Integer.parseInt(parts[i++]), MazeType.class));
                mazeInit.setMazeAlgorithm(PersistableEnum.fromId(Integer.parseInt(parts[i++]), MazeAlgorithm.class));
                mazeInit.setMazeShape(PersistableEnum.fromId(Integer.parseInt(parts[i++]), MazeShape.class));

                mazeInit.setWidth(Integer.parseInt(parts[i++]));
                mazeInit.setHeight(Integer.parseInt(parts[i++]));

                if (Integer.parseInt(parts[i++]) == 1) {
                    int[][] decodedMask = decodeGrid(parts[i++], mazeInit.getWidth(), mazeInit.getHeight());
                    boolean[][] mask = new boolean[mazeInit.getHeight()][mazeInit.getWidth()];

                    for (int r = 0; r < mazeInit.getHeight(); r++) {
                        for (int c = 0; c < mazeInit.getWidth(); c++) {
                            mask[r][c] = decodedMask[r][c] > 0;
                        }
                    }

                    mazeInit.setMask(resolveMask(mask, gameType, user));
                }

                if (mazeInit.getMazeType() == MazeType.MULTI_LEVEL) {
                    mazeInit.setHasMultipleFloors(true);
                    String counts = parts[i++];
                    for (String t : counts.split("_")) {
                        if (t.contains(":")) {
                            String[] info = t.split(":");
                            mazeInit.getTypeCount().put(PersistableEnum.fromId(Integer.parseInt(info[0]), MazeType.class), Integer.parseInt(info[1]));
                        }
                    }
                }
                gameInit = mazeInit;
            }

            case BRIDGE -> {
                BridgeInit bridgeInit = new BridgeInit();
                bridgeInit.setDifficulty(difficulty);
                bridgeInit.setBridgeType(PersistableEnum.fromId(Integer.parseInt(parts[i++]), BridgeType.class));

                bridgeInit.setHeight(Integer.parseInt(parts[i++]));
                bridgeInit.setWidth(Integer.parseInt(parts[i++]));

                if (bridgeInit.getBridgeType() == BridgeType.MULTIPLE) {
                    bridgeInit.setMultipleCount(Integer.parseInt(parts[i++]));
                }
                gameInit = bridgeInit;
            }

            case SHIKAKU -> {
                ShikakuInit shikakuInit = new ShikakuInit();
                shikakuInit.setDifficulty(difficulty);
                shikakuInit.setShikakuType(PersistableEnum.fromId(Integer.parseInt(parts[i++]), ShikakuType.class));

                shikakuInit.setWidth(Integer.parseInt(parts[i++]));
                shikakuInit.setHeight(Integer.parseInt(parts[i++]));
                gameInit = shikakuInit;
            }
        }

        if (gameInit != null) {
            var lastPart = parts[i];
            gameInit.setSeed(Long.valueOf(lastPart));
        }

        return gameInit;
    }
}
