package cz.logicgo.core.util.boardConverters;


import cz.logicgo.core.gameClasses.maze.dataStructures.MazeFloor;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.gameModes.*;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.gameClasses.maze.dataStructures.helpers.MazeGridConstructor;
import cz.logicgo.core.gameClasses.maze.dataStructures.path.PlayerPath;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.HexagonalDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;
import cz.logicgo.core.misc.interfaces.PersistableEnum;

import java.io.*;
import java.util.*;

import static cz.logicgo.core.gameClasses.maze.MazeUtils.returnNewInstance;
import static cz.logicgo.core.misc.Compression.compress;
import static cz.logicgo.core.misc.Compression.decompress;


public class MazeConverters {

    public static byte FLAG_EMPTY = (byte) 255;

    public static byte[] mazeCellToBytes(MazeCell cell) {
        byte[] result = new byte[4];
        int row = cell.getRow();
        int col = cell.getCol();

        result[0] = (byte) (row >> 8);
        result[1] = (byte) row;

        result[2] = (byte) (col >> 8);
        result[3] = (byte) col;

        return result;
    }

    public static byte[] mazeFloorsToBytes(List<MazeFloor> floors) {
        if (floors == null || floors.isEmpty()) return new byte[0];

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeShort(floors.size());

            for (MazeFloor floor : floors) {
                byte[] gridBytes = floor.getGridBytes();
                if (gridBytes == null) {
                    dos.writeInt(0);
                } else {
                    dos.writeInt(gridBytes.length);
                    dos.write(gridBytes);
                }
            }

            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static List<MazeFloor> bytesToMazeFloors(byte[] data) {
        if (data == null || data.length == 0) return new ArrayList<>();

        List<MazeFloor> floors = new ArrayList<>();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {

            int count = dis.readShort();
            for (int i = 0; i < count; i++) {
                int length = dis.readInt();
                byte[] gridBytes = dis.readNBytes(length);

                MazeFloor floor = new MazeFloor();
                floor.setFloorNumber(i);
                floor.setGridBytes(gridBytes);
                floors.add(floor);
            }

            return floors;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public static MazeCell bytesToMazeCell(byte[] bytes, MazeCell[][] board) {
        if (bytes == null || bytes.length < 4) return null;

        int row = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);

        int col = ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);

        return board[row][col];
    }

    public static byte[] mazeModifiersToBytes(Set<MazeModifier> mazeModifiers) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            for (MazeModifier mazeModifier : mazeModifiers) {
                int id = mazeModifier.getId();
                byte[] bytes = mazeModifier.serialize();
                int length = bytes.length;
                dos.writeByte(id);
                dos.writeInt(length);
                dos.write(bytes);
            }

            return baos.toByteArray();

        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static Set<MazeModifier> bytesToMazeModifiers(byte[] bytes, MazeGrid grid) {
        byte[] decompressed = decompress(bytes);
        Set<MazeModifier> mazeModifiers = new HashSet<>();
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(decompressed))) {
            while (dis.available() > 0) {
                MazeType mazeType = PersistableEnum.fromId(dis.readByte(), MazeType.class);
                int length = dis.readInt();
                byte[] modifierBytes = dis.readNBytes(length);

                switch (mazeType) {
                    case WALLS -> {
                        WallModifier modifier = WallModifier.deserialize(modifierBytes, grid);
                        if (modifier != null) mazeModifiers.add(modifier);
                    }
                    case PORTAL -> {
                        PortalModifier modifier = PortalModifier.deserialize(modifierBytes, grid);
                        if (modifier != null) mazeModifiers.add(modifier);
                    }
                    case CHECKPOINT -> {
                        CheckpointModifier modifier = CheckpointModifier.deserialize(modifierBytes, grid);
                        if (modifier != null) mazeModifiers.add(modifier);
                    }
                    case PATTERN -> {
                        PatternModifier modifier = PatternModifier.deserialize(modifierBytes, grid);
                        if (modifier != null) mazeModifiers.add(modifier);
                    }
                    case WRAP_AROUND -> {
                        WrapAroundModifier modifier = WrapAroundModifier.deserialize(modifierBytes, grid);
                        if (modifier != null) mazeModifiers.add(modifier);
                    }
                    case EXACT_STEPS -> {
                        ExactStepsModifier modifier = ExactStepsModifier.deserialize(modifierBytes, grid);
                        if (modifier != null) mazeModifiers.add(modifier);
                    }
                    default -> {
                    }
                }
            }
            return mazeModifiers;
        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] serializePath(PlayerPath playerPath) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            Deque<MazeCell> active = playerPath.getActivePath();
            dos.writeShort(active.size());

            for (MazeCell cell : active) {
                dos.writeShort(cell.getRow());
                dos.writeShort(cell.getCol());
            }

            var fullHistory = playerPath.getFullHistory();
            dos.writeShort(fullHistory.size());
            for (MazeCell cell : fullHistory) {
                dos.writeShort(cell.getRow());
                dos.writeShort(cell.getCol());
            }

            var solutionPath = playerPath.getSolutionPath();
            dos.writeShort(solutionPath.size());
            for (MazeCell cell : solutionPath) {
                dos.writeShort(cell.getRow());
                dos.writeShort(cell.getCol());
            }
            return baos.toByteArray();
        }
    }


    public record PathsDummy(List<GridCell> activeCellsTemp, List<GridCell> fullHistoryTemp,
                             List<GridCell> solutionPathTemp) {
    }

    public static PlayerPath deserializePaths(byte[] data, MazeGrid mazeGrid) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);

        List<MazeCell> activeCellsTemp = gatherMazeCellsFromSerializedList(dis, dis.readShort(), mazeGrid);
        List<MazeCell> fullHistoryTemp = gatherMazeCellsFromSerializedList(dis, dis.readShort(), mazeGrid);
        List<MazeCell> solutionPathTemp = gatherMazeCellsFromSerializedList(dis, dis.readShort(), mazeGrid);

        var path = new PlayerPath();

        path.getActivePath().addAll(activeCellsTemp);
        path.getFullHistory().addAll(fullHistoryTemp);
        path.getSolutionPath().addAll(solutionPathTemp);

        return path;
    }

    public static byte[] serializeSingleGrid(MazeGrid mazeGrid) {
        if (mazeGrid == null) return new byte[0];

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeShort(mazeGrid.getRowCount());
            dos.writeShort(mazeGrid.getColCount());
            dos.writeByte(mazeGrid.getMazeShape().getId());
            dos.writeByte(mazeGrid.getMazeType().getId());

            dos.writeShort(mazeGrid.getStartCell().getRow());
            dos.writeShort(mazeGrid.getStartCell().getCol());
            dos.writeShort(mazeGrid.getEndCell().getRow());
            dos.writeShort(mazeGrid.getEndCell().getCol());

            dos.writeByte(mazeGrid.getStartDirection() != null ? mazeGrid.getStartDirection().getId() : -1);
            dos.writeByte(mazeGrid.getEndDirection() != null ? mazeGrid.getEndDirection().getId() : -1);

            byte[] modifiers = mazeModifiersToBytes(mazeGrid.getModifiers());
            dos.writeInt(modifiers.length);
            dos.write(modifiers);

            dos.writeShort(mazeGrid.getFloorNumber());

            byte[] mask = serializeMazeMask(mazeGrid.getMask());
            dos.writeInt(mask.length);
            dos.write(mask);

            ByteArrayOutputStream dirBaos = new ByteArrayOutputStream();
            DataOutputStream dirDos = new DataOutputStream(dirBaos);


            for (MazeCell mazeCell : mazeGrid.getFlattenedGrid()) {
                if (mazeCell != null) {
                    byte linksToCell = 0;
                    for (MazeDirection direction : mazeGrid.getMazeShape().getAssociatedDirectionEnum().getEnumConstants()) {
                        List<MazeCell> neighbourCellList = mazeCell.getNeighbouringCells().get(direction);
                        if (neighbourCellList != null && !neighbourCellList.isEmpty()) {
                            if (mazeCell.isLinked(neighbourCellList.getFirst())) {
                                linksToCell |= direction.getMask();
                            }
                        }
                    }
                    dirDos.writeByte(linksToCell);
                } else {
                    dirDos.writeByte(FLAG_EMPTY);
                }
            }

            byte[] dirBytes = dirBaos.toByteArray();
            dos.writeInt(dirBytes.length);
            dos.write(dirBytes);

            byte[] pathBytes = serializePath(mazeGrid.getPath());
            int lengthPaths = pathBytes.length;
            dos.writeInt(lengthPaths);
            dos.write(pathBytes);

            return compress(baos.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static MazeGrid deserializeSingleGrid(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;

        byte[] decompressed = decompress(bytes);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(decompressed);
             DataInputStream dis = new DataInputStream(bais)) {

            int rowCount = dis.readShort();
            int colCount = dis.readShort();
            MazeShape mazeShape = PersistableEnum.fromId(dis.readByte(), MazeShape.class);
            MazeType mazeType = PersistableEnum.fromId(dis.readByte(), MazeType.class);

            int startRow = dis.readShort();
            int startCol = dis.readShort();
            int endRow = dis.readShort();
            int endCol = dis.readShort();

            byte startDirId = dis.readByte();
            byte endDirId = dis.readByte();
            MazeDirection startDir;
            MazeDirection endDir;
            if (mazeShape == MazeShape.RECTANGULAR) {
                startDir = startDirId != -1 ? PersistableEnum.fromId(startDirId, RectangularDirection.class) : null;
                endDir = endDirId != -1 ? PersistableEnum.fromId(endDirId, RectangularDirection.class) : null;
            } else {
                startDir = startDirId != -1 ? PersistableEnum.fromId(startDirId, HexagonalDirection.class) : null;
                endDir = endDirId != -1 ? PersistableEnum.fromId(endDirId, HexagonalDirection.class) : null;
            }


            int modifierLength = dis.readInt();
            byte[] modifierBytes = dis.readNBytes(modifierLength);

            int level = dis.readShort();

            int maskLength = dis.readInt();
            byte[] maskBytes = dis.readNBytes(maskLength);
            int[][] mask = deserializeMazeMask(maskBytes);

            int directionsLength = dis.readInt();
            byte[] directions = dis.readNBytes(directionsLength);

            MazeGridConstructor builder = new MazeGridConstructor()
                    .setRowCount(rowCount)
                    .setColCount(colCount)
                    .setMazeShape(mazeShape)
                    .setMazeType(mazeType)
                    .setMask(mask)
                    .setDirections(directions);

            MazeGrid grid = returnNewInstance(builder);

            MazeCell finalStartCell = grid.getCell(startRow, startCol);
            MazeCell finalEndCell = grid.getCell(endRow, endCol);

            grid.setStartCell(finalStartCell);
            grid.setEndCell(finalEndCell);
            grid.setStartDirection(startDir);
            grid.setEndDirection(endDir);

            int lengthPaths = dis.readInt();
            byte[] pathBytes = dis.readNBytes(lengthPaths);

            PlayerPath path = deserializePaths(pathBytes, grid);

            grid.setPath(path);

            grid.setFloorNumber(level);

            Set<MazeModifier> modifiers = MazeConverters.bytesToMazeModifiers(modifierBytes, grid);
            if (modifiers != null) {
                grid.getModifiers().addAll(modifiers);
            }

            return grid;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static List<MazeCell> gatherMazeCellsFromSerializedList(DataInputStream dis, int count, MazeGrid mazeGrid) throws IOException {

        List<MazeCell> list = new ArrayList<>();
        while (count > 0) {
            int rowActive = dis.readShort();
            int colActive = dis.readShort();
            list.add(mazeGrid.getCell(rowActive, colActive));
            count--;
        }
        return list;
    }


    public static MaskType returnMaskType(int[][] mask) {
        if (mask == null || mask.length == 0 || mask[0] == null || mask[0].length == 0) {
            return MaskType.NONE;
        }

        IntSummaryStatistics stats = Arrays.stream(mask)
                .flatMapToInt(Arrays::stream)
                .summaryStatistics();

        int max = stats.getMax();
        int min = stats.getMin();

        if (min == 1 && max == 1) {
            return MaskType.ALL;
        }

        if (min >= 0 && max <= 1) {
            return max == 0 ? MaskType.NONE : MaskType.BOOL;
        }

        if (max >= 16) {
            return MaskType.BIG;
        }

        return MaskType.SMALL;
    }

    public static byte[] serializeMazeMaskBoolean(boolean[][] mask) {
        int[][] intMask = booleanMaskToInt(mask);
        return MazeConverters.serializeMazeMask(intMask);
    }

    public static int[][] booleanMaskToInt(boolean[][] mask) {
        if (mask == null || mask.length == 0 || mask[0] == null || mask[0].length == 0) return null;
        int[][] intMask = new int[mask.length][mask[0].length];
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[0].length; j++) {
                if (mask[i][j]) intMask[i][j] = 1;
                else intMask[i][j] = 0;
            }
        }
        return intMask;
    }

    public static boolean[][] deserializeMazeMaskBoolean(byte[] data) {
        int[][] mask = MazeConverters.deserializeMazeMask(data);
        return intMaskToBoolean(mask);
    }

    public static boolean[][] intMaskToBoolean(int[][] mask) {
        boolean[][] booleanMask = new boolean[mask.length][mask[0].length];
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[0].length; j++) {
                booleanMask[i][j] = mask[i][j] == 1;
            }
        }
        return booleanMask;
    }

    public static byte[] serializeMazeMask(int[][] mask) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            int rows = mask.length;
            int cols = mask[0].length;

            dos.writeShort(rows);
            dos.writeShort(cols);

            MaskType maskType = returnMaskType(mask);

            dos.writeByte(maskType.getType());

            switch (maskType) {
                case BOOL -> {
                    for (int[] row : mask) {
                        int bitIndex = 0;
                        byte currentByte = 0;
                        for (int cell : row) {
                            if (cell == 1) {
                                currentByte |= (byte) (1 << (7 - bitIndex));
                            }
                            bitIndex++;
                            if (bitIndex == 8) {
                                dos.writeByte(currentByte);
                                bitIndex = 0;
                                currentByte = 0;
                            }
                        }
                        if (bitIndex > 0) {
                            dos.writeByte(currentByte);
                        }
                    }
                }
                case BIG -> {
                    for (int[] row : mask) {
                        for (int val : row) {
                            dos.writeByte((byte) val);
                        }
                    }
                }
                case SMALL -> {
                    boolean halfByte = false;
                    byte currentByte = 0;
                    for (int[] row : mask) {
                        for (int val : row) {
                            if (halfByte) {
                                currentByte |= (byte) (val & 0xF);
                                dos.writeByte(currentByte);
                            } else {
                                currentByte = (byte) (val << 4);
                            }
                            halfByte = !halfByte;
                        }
                    }
                    if (halfByte) {
                        dos.writeByte(currentByte);
                    }
                }
                case NONE, ALL -> {
                }
            }

            dos.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            return new byte[0];
        }
    }

    private static int[][] createEmptyMask(int rows, int cols) {
        var mask = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mask[i][j] = 0;
            }
        }
        return mask;
    }

    private static int[][] createFullMask(int rows, int cols) {
        var mask = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mask[i][j] = 1;
            }
        }
        return mask;
    }

    public static int[][] deserializeMazeMask(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {

            int rows = dis.readShort();
            int cols = dis.readShort();
            int[][] mask = new int[rows][cols];

            int type = dis.readByte();
            MaskType maskType = MaskType.getByType(type);

            switch (maskType) {
                case NONE -> {
                    return createEmptyMask(rows, cols);
                }
                case ALL -> {
                    return createFullMask(rows, cols);
                }
                case BOOL -> {
                    for (int r = 0; r < rows; r++) {
                        int bitIndex = 0;
                        byte currentByte = 0;
                        for (int c = 0; c < cols; c++) {
                            if (bitIndex == 0) {
                                currentByte = dis.readByte();
                            }

                            boolean value = ((currentByte >> (7 - bitIndex)) & 1) == 1;
                            mask[r][c] = value ? 1 : 0;

                            bitIndex = (bitIndex + 1) % 8;
                        }
                    }
                }
                case BIG -> {
                    for (int r = 0; r < rows; r++) {
                        for (int c = 0; c < cols; c++) {
                            mask[r][c] = dis.readByte();
                        }
                    }
                }
                case SMALL -> {
                    boolean halfByte = false;
                    byte byteValue = 0;
                    for (int r = 0; r < rows; r++) {
                        for (int c = 0; c < cols; c++) {
                            int value;
                            if (!halfByte) {
                                byteValue = dis.readByte();
                                value = (byteValue >> 4) & 0x0F;
                            } else {
                                value = byteValue & 0x0F;
                            }
                            halfByte = !halfByte;
                            mask[r][c] = value;
                        }
                    }
                }
            }

            return mask;

        } catch (IOException e) {
            e.printStackTrace();
            return new int[0][0];
        }
    }

    public enum MaskType {
        NONE(0),
        BOOL(1),
        SMALL(2),
        BIG(3),
        ALL(4);

        private final int type;

        MaskType(int type) {
            this.type = type;
        }

        public static MaskType getByType(int type) {
            return switch (type) {
                case 0 -> NONE;
                case 1 -> BOOL;
                case 2 -> SMALL;
                case 3 -> BIG;
                case 4 -> ALL;
                default -> NONE;
            };

        }

        public int getType() {
            return type;
        }
    }
}
