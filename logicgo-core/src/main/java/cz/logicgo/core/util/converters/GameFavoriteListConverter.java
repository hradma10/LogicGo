package cz.logicgo.core.util.converters;

import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.favorites.*;
import cz.logicgo.core.gameClasses.maze.dataStructures.MazeFloor;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
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
import cz.logicgo.core.util.boardConverters.MazeConverters;
import cz.logicgo.core.util.boardConverters.SudokuConverters;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static cz.logicgo.core.misc.Compression.compress;
import static cz.logicgo.core.misc.Compression.decompress;
import static cz.logicgo.core.util.boardConverters.MazeConverters.booleanMaskToInt;
import static cz.logicgo.core.util.boardConverters.MazeConverters.intMaskToBoolean;


@Converter
public class GameFavoriteListConverter implements AttributeConverter<List<GameFavorite>, byte[]> {

    private static SudokuFavorite gameToSudokuFavorite(Sudoku sudoku) {

        Difficulty difficulty = sudoku.getDifficulty();
        int width = sudoku.getWidth();
        int height = sudoku.getHeight();
        SudokuRegionLayout regionLayout = sudoku.getRegionLayout();
        SudokuPatternLayout patternLayout = sudoku.getPattern();
        SudokuSize size = sudoku.getType();
        SudokuVariant sudokuVariant = sudoku.getVariant();
        boolean symmetric = sudoku.isSymmetric();

        SudokuFavorite sudokuFavorite = new SudokuFavorite();
        sudokuFavorite.setDifficulty(difficulty);
        sudokuFavorite.setWidth(width);
        sudokuFavorite.setHeight(height);
        sudokuFavorite.setRegionLayout(regionLayout);
        sudokuFavorite.setPatternLayout(patternLayout);
        sudokuFavorite.setSize(size);
        sudokuFavorite.setVariant(sudokuVariant);

        return sudokuFavorite;
    }

    private static BridgeFavorite gameToBridgeFavorite(Bridge bridge) {
        Difficulty difficulty = bridge.getDifficulty();
        int width = bridge.getWidth();
        int height = bridge.getHeight();
        int islandCount = bridge.getIslands().size();
        int multipleCount = bridge.getMaxMultipleBridges();
        BridgeType bridgeType = bridge.getType();

        BridgeFavorite bridgeFavorite = new BridgeFavorite();
        bridgeFavorite.setDifficulty(difficulty);
        bridgeFavorite.setWidth(width);
        bridgeFavorite.setHeight(height);
        bridgeFavorite.setIslandCount(islandCount);
        bridgeFavorite.setMultipleCount(multipleCount);
        bridgeFavorite.setBridgeType(bridgeType);

        return bridgeFavorite;
    }

    private static MazeFavorite gameToMazeFavorite(Maze maze) {
        MazeType mazeType = maze.getMazeType();
        Difficulty difficulty = maze.getDifficulty();
        int width = maze.getWidth();
        int height = maze.getHeight();
        MazeShape shape = maze.getMazeShape();
        MazeAlgorithm algorithm = maze.getMazeAlgorithm();
        boolean multipleFloors = maze.isHasMultipleFloors();

        MazeFavorite mazeFavorite = new MazeFavorite();
        mazeFavorite.setMazeType(mazeType);
        mazeFavorite.setDifficulty(difficulty);
        mazeFavorite.setWidth(width);
        mazeFavorite.setHeight(height);
        mazeFavorite.setMazeShape(shape);
        mazeFavorite.setMazeAlgorithm(algorithm);
        mazeFavorite.setHasMultipleFloors(multipleFloors);

        mazeFavorite.setMask(intMaskToBoolean(maze.getMazeGrid().getMask()));

        EnumMap<MazeType, Integer> typeCounts = new EnumMap<>(MazeType.class);

        if (maze.getMazeGridFloors().size() > 1) {
            for (MazeFloor mazeFloor : maze.getMazeGridFloors()) {
                MazeGrid mazeGrid = mazeFloor.getMazeGrid();
                var type = mazeGrid.getMazeType();

                if (type != null) {
                    typeCounts.merge(type, 1, Integer::sum);
                }

            }
            mazeFavorite.getTypeCount().putAll(typeCounts);
        }

        return mazeFavorite;
    }

    public static GameFavorite gameToGameFavorite(Game game) {
        return switch (game) {
            case Sudoku sudoku -> gameToSudokuFavorite(sudoku);
            case Maze maze -> gameToMazeFavorite(maze);
            case Bridge bridge -> gameToBridgeFavorite(bridge);
            case Shikaku shikaku -> gameToShikakuFavorite(shikaku);
            default -> throw new IllegalStateException("Unexpected value: " + game);
        };
    }

    private static ShikakuFavorite gameToShikakuFavorite(Shikaku shikaku) {
        Difficulty difficulty = shikaku.getDifficulty();
        int width = shikaku.getWidth();
        int height = shikaku.getHeight();
        ShikakuType shikakuType = shikaku.getShikakuType();

        ShikakuFavorite shikakuFavorite = new ShikakuFavorite();
        shikakuFavorite.setDifficulty(difficulty);
        shikakuFavorite.setWidth(width);
        shikakuFavorite.setHeight(height);
        shikakuFavorite.setShikakuType(shikakuType);

        return shikakuFavorite;
    }

    @Override
    public byte[] convertToDatabaseColumn(List<GameFavorite> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return new byte[0];
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dis = new DataOutputStream(baos)) {

            for (GameFavorite gameFavorite : attribute) {
                int diff = gameFavorite.getDifficulty().getId();
                int width = gameFavorite.getWidth();
                int height = gameFavorite.getHeight();
                int typeGame = gameFavorite.getTypeGame().getId();

                dis.writeByte(diff);
                dis.writeShort(width);
                dis.writeShort(height);
                dis.writeByte(typeGame);

                switch (gameFavorite) {
                    case SudokuFavorite sudokuFavorite -> {
                        byte[] sudokuRegionBytes = SudokuConverters.sudokuRegionToBytes(sudokuFavorite.getRegionLayout());
                        byte[] patternRegionBytes = SudokuConverters.sudokuPatternToBytes(sudokuFavorite.getPatternLayout());
                        int variant = sudokuFavorite.getVariant().getId();
                        int size = sudokuFavorite.getSize().getGridSize();

                        dis.writeInt(sudokuRegionBytes.length);
                        dis.write(sudokuRegionBytes);

                        dis.writeInt(patternRegionBytes.length);
                        dis.write(patternRegionBytes);

                        dis.writeByte(size);
                        dis.writeByte(variant);
                    }
                    case BridgeFavorite bridgeFavorite -> {
                        int type = bridgeFavorite.getBridgeType().getId();
                        int islandCount = bridgeFavorite.getIslandCount();

                        dis.writeByte(type);
                        dis.writeShort(islandCount);
                    }
                    case MazeFavorite mazeFavorite -> {
                        int mazeTypeId = mazeFavorite.getMazeType().getId();
                        int shape = mazeFavorite.getMazeShape().getId();
                        int algo = mazeFavorite.getMazeAlgorithm().getId();
                        boolean multipleFloors = mazeFavorite.isHasMultipleFloors();

                        boolean[][] mask = mazeFavorite.getMask();
                        byte[] maskBytes = MazeConverters.serializeMazeMask(booleanMaskToInt(mask));

                        dis.writeByte(mazeTypeId);
                        dis.writeByte(shape);
                        dis.writeByte(algo);
                        dis.writeBoolean(multipleFloors);

                        dis.writeInt(maskBytes.length);
                        dis.write(maskBytes);

                        EnumMap<MazeType, Integer> typesCount = mazeFavorite.getTypeCount();
                        if (!typesCount.isEmpty()) {
                            dis.writeByte(typesCount.size());
                            for (Map.Entry<MazeType, Integer> entry : typesCount.entrySet()) {
                                dis.writeByte(entry.getKey().getId());
                                dis.writeShort(entry.getValue());
                            }
                        } else {
                            dis.writeByte(0);
                        }
                    }
                    case ShikakuFavorite shikakuFavorite -> {
                        int type = shikakuFavorite.getShikakuType().getId();
                        dis.writeByte(type);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + gameFavorite);
                }
            }

            return compress(baos.toByteArray());

        } catch (IOException e) {
            throw new IllegalStateException("Chyba při serializaci seznamu oblíbených konfigurací.", e);
        }
    }

    @Override
    public List<GameFavorite> convertToEntityAttribute(byte[] dbData) {
        if (dbData == null || dbData.length == 0) {
            return new ArrayList<>();
        }

        dbData = decompress(dbData);

        List<GameFavorite> gameFavorites = new ArrayList<>();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(dbData);
             DataInputStream dis = new DataInputStream(bais)) {

            while (dis.available() > 0) {
                int diffId = dis.readByte();
                Difficulty difficulty = PersistableEnum.fromId(diffId, Difficulty.class);

                int width = dis.readShort();
                int height = dis.readShort();

                int typeGameId = dis.readByte();
                TypeGame typeGame = PersistableEnum.fromId(typeGameId, TypeGame.class);

                switch (typeGame) {
                    case SUDOKU -> {
                        int sudokuRegionBytesLength = dis.readInt();
                        byte[] sudokuRegionBytes = dis.readNBytes(sudokuRegionBytesLength);
                        SudokuRegionLayout regionLayout = SudokuConverters.bytesToSudokuRegion(sudokuRegionBytes);

                        int sudokuPatternBytesLength = dis.readInt();
                        byte[] sudokuPatternBytes = dis.readNBytes(sudokuPatternBytesLength);
                        SudokuPatternLayout patternLayout = SudokuConverters.bytesToSudokuPattern(sudokuPatternBytes);

                        int sizeInt = dis.readByte();
                        SudokuSize size = SudokuSize.getTypeByGridSize(sizeInt);
                        int variantId = dis.readByte();
                        SudokuVariant sudokuVariant = PersistableEnum.fromId(variantId, SudokuVariant.class);

                        SudokuFavorite sudokuFavorite = new SudokuFavorite();
                        sudokuFavorite.setDifficulty(difficulty);
                        sudokuFavorite.setWidth(width);
                        sudokuFavorite.setHeight(height);
                        sudokuFavorite.setRegionLayout(regionLayout);
                        sudokuFavorite.setPatternLayout(patternLayout);
                        sudokuFavorite.setSize(size);
                        sudokuFavorite.setVariant(sudokuVariant);

                        gameFavorites.add(sudokuFavorite);
                    }
                    case MAZE -> {
                        int typeId = dis.readByte();
                        MazeType mazeType = PersistableEnum.fromId(typeId, MazeType.class);

                        int shapeId = dis.readByte();
                        MazeShape mazeShape = PersistableEnum.fromId(shapeId, MazeShape.class);

                        int algoId = dis.readByte();
                        MazeAlgorithm mazeAlgorithm = PersistableEnum.fromId(algoId, MazeAlgorithm.class);

                        boolean multipleFloors = dis.readBoolean();

                        int maskSize = dis.readInt();
                        byte[] maskBytes = dis.readNBytes(maskSize);
                        int[][] mask = MazeConverters.deserializeMazeMask(maskBytes);

                        MazeFavorite mazeFavorite = new MazeFavorite();
                        mazeFavorite.setDifficulty(difficulty);
                        mazeFavorite.setWidth(width);
                        mazeFavorite.setHeight(height);

                        mazeFavorite.setMazeType(mazeType);
                        mazeFavorite.setMazeAlgorithm(mazeAlgorithm);
                        mazeFavorite.setMazeShape(mazeShape);
                        mazeFavorite.setHasMultipleFloors(multipleFloors);
                        mazeFavorite.setMask(intMaskToBoolean(mask));

                        int typeCountSize = dis.readByte();
                        if (typeCountSize > 0) {
                            EnumMap<MazeType, Integer> typeCounts = new EnumMap<>(MazeType.class);
                            for (int i = 0; i < typeCountSize; i++) {
                                MazeType t = PersistableEnum.fromId(dis.readByte(), MazeType.class);
                                int count = dis.readShort();
                                typeCounts.put(t, count);
                            }
                            mazeFavorite.getTypeCount().putAll(typeCounts);
                        }

                        gameFavorites.add(mazeFavorite);
                    }
                    case BRIDGE -> {
                        int bridgeId = dis.readByte();
                        BridgeType bridgeType = PersistableEnum.fromId(bridgeId, BridgeType.class);

                        int islandCount = dis.readShort();

                        BridgeFavorite bridgeFavorite = new BridgeFavorite();
                        bridgeFavorite.setDifficulty(difficulty);
                        bridgeFavorite.setWidth(width);
                        bridgeFavorite.setHeight(height);

                        bridgeFavorite.setIslandCount(islandCount);
                        bridgeFavorite.setBridgeType(bridgeType);

                        gameFavorites.add(bridgeFavorite);
                    }
                }
            }
            return gameFavorites;

        } catch (IOException e) {
            throw new IllegalStateException("Chyba při deserializaci seznamu oblíbených konfigurací.", e);
        }
    }
}
