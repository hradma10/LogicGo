package cz.logicgo.core.util.boardConverters;


import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.HistorySudokuPlay;
import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.CompType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.ParityType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.misc.interfaces.PersistableEnum;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.*;
import static cz.logicgo.core.misc.Compression.compress;
import static cz.logicgo.core.misc.Compression.decompress;


public class SudokuConverters {

    public static byte[] sudokuCellCandidatesToBytes(HashMap<SudokuCell, HashSet<Integer>> candidates) {
        if (candidates == null || candidates.isEmpty()) return new byte[0];

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            for (Map.Entry<SudokuCell, HashSet<Integer>> entry : candidates.entrySet()) {
                SudokuCell cell = entry.getKey();
                Set<Integer> set = entry.getValue();

                if (cell == null || set == null || set.isEmpty()) continue;
                dos.writeByte(cell.getRow());
                dos.writeByte(cell.getCol());
                dos.writeByte(set.size());
                for (Integer val : set) {
                    dos.writeByte(val);
                }
            }

            return compress(baos.toByteArray());
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static HashMap<SudokuCell, HashSet<Integer>> bytesToSudokuCellCandidates(byte[] bytes, SudokuCell[][] board) {
        HashMap<SudokuCell, HashSet<Integer>> candidates = new HashMap<>();
        if (bytes == null || bytes.length == 0 || board == null || board.length == 0) return candidates;

        byte[] decompressed = decompress(bytes);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(decompressed);
             DataInputStream dis = new DataInputStream(bais)) {

            while (dis.available() > 0) {
                int row = dis.readByte() & 0xFF;
                int col = dis.readByte() & 0xFF;

                int size = dis.readByte() & 0xFF;
                HashSet<Integer> set = new HashSet<>(size, 1.0F);

                for (int i = 0; i < size; i++) {
                    set.add((int) dis.readByte() & 0xFF);
                }

                if (row < board.length && col < board[row].length && board[row][col] != null) {
                    candidates.put(board[row][col], set);
                }
            }

            return candidates;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public static byte[] serializeBoard(SudokuCell[][] board) {
        if (board == null || board.length == 0) return new byte[0];

        int size = board.length;
        if (size <= 3) return new byte[0];

        SudokuCell firstCell = null;
        findFirst:
        for (SudokuCell[] row : board) {
            for (SudokuCell cell : row) {
                if (cell != null) {
                    firstCell = cell;
                    break findFirst;
                }
            }
        }
        if (firstCell == null) return new byte[0];

        SudokuVariant variant = firstCell.getVariant();
        if (variant == null) return new byte[0];

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(size);
            dos.writeByte(variant.getId());

            for (SudokuCell[] row : board) {
                for (SudokuCell cell : row) {
                    if (cell == null) {
                        dos.writeByte(-1);
                    } else {
                        dos.writeByte(cell.getValue());
                    }
                }
            }

            return compress(baos.toByteArray());
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static SudokuCell[][] deserializeBoard(byte[] encodedBoard) {
        if (encodedBoard == null || encodedBoard.length == 0) return new SudokuCell[0][0];

        byte[] data = decompress(encodedBoard);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {

            int gridSize = dis.readByte();
            int variantId = dis.readByte();
            var variant = PersistableEnum.fromId(variantId, SudokuVariant.class);
            var sudokuCells = new SudokuCell[gridSize][gridSize];

            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    byte value = dis.readByte();
                    if (value == -1) {
                        sudokuCells[i][j] = null;
                    } else {
                        sudokuCells[i][j] = createCell(variant, i, j, value);
                    }
                }
            }

            return sudokuCells;
        } catch (IOException e) {
            throw new RuntimeException("Chyba při deserializaci", e);
        }
    }

    public static byte[] serializeSudokuPlayHistory(List<HistorySudokuPlay> history) {
        if (history == null || history.isEmpty()) return new byte[0];

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            for (HistorySudokuPlay historySudokuPlay : history) {
                buffer.write((byte) historySudokuPlay.row());
                buffer.write((byte) historySudokuPlay.col());
                buffer.write((byte) historySudokuPlay.num());
            }
            return compress(buffer.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static List<HistorySudokuPlay> deserializeSudokuPlayHistory(byte[] data) {
        data = decompress(data);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {

            ArrayList<HistorySudokuPlay> history = new ArrayList<>();
            while (dis.available() > 0) {
                int row = dis.readByte();
                int col = dis.readByte();
                int num = dis.readByte();
                history.add(new HistorySudokuPlay(row, col, num));
            }
            return history;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static byte[] sudokuCandidatesToBytes(Map<GridCell, Set<Integer>> candidates) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            int sudokuSize = candidates.keySet().stream().mapToInt(gridCell -> Math.max(gridCell.row(), gridCell.col())).max().orElse(0);


            for (Map.Entry<GridCell, Set<Integer>> entry : candidates.entrySet()) {
                GridCell cell = entry.getKey();
                Set<Integer> set = entry.getValue();

                if (set.isEmpty()) continue;

                if (sudokuSize <= 16) {
                    int packedRC = ((cell.row() & 0x0F) << 4) | (cell.col() & 0x0F);
                    dos.writeByte(packedRC);
                } else {
                    dos.writeByte(cell.row());
                    dos.writeByte(cell.col());
                }

                if (sudokuSize > 16) {
                    dos.writeByte(set.size());
                    for (Integer value : set) {
                        dos.writeByte(value);
                    }
                } else {
                    List<Integer> values = new ArrayList<>(set);
                    dos.writeByte(values.size());
                    for (int i = 0; i < values.size(); i += 2) {
                        int first = values.get(i) & 0x0F;
                        int second = (i + 1 < values.size()) ? (values.get(i + 1) & 0x0F) : 0;
                        int packed = (first << 4) | second;
                        dos.writeByte(packed);
                    }
                }
            }

            return compress(baos.toByteArray());
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static Map<GridCell, Set<Integer>> bytesToSudokuCandidates(byte[] bytes, SudokuCell[][] cells) {
        bytes = decompress(bytes);
        HashMap<GridCell, Set<Integer>> candidates = new HashMap<>();
        int sudokuSize = cells.length;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             DataInputStream dis = new DataInputStream(bais)) {

            while (dis.available() > 0) {
                int row, col;
                if (sudokuSize <= 16) {
                    int packedRC = dis.readByte() & 0xFF;
                    row = (packedRC >> 4) & 0x0F;
                    col = packedRC & 0x0F;
                } else {
                    row = dis.readByte();
                    col = dis.readByte();
                }

                GridCell cell = new GridCell(row, col);
                Set<Integer> set = new HashSet<>(sudokuSize, 1.0F);
                int size = dis.readByte();

                if (sudokuSize > 16) {
                    for (int i = 0; i < size; i++) {
                        int val = dis.readByte();
                        set.add(val);
                    }
                } else {
                    int count = 0;
                    int bytesToRead = (size + 1) / 2;
                    for (int i = 0; i < bytesToRead; i++) {
                        int packed = dis.readByte() & 0xFF;
                        int first = (packed >> 4) & 0x0F;
                        int second = packed & 0x0F;

                        if (count < size && first != 0) {
                            set.add(first);
                            count++;
                        }
                        if (count < size && second != 0) {
                            set.add(second);
                            count++;
                        }
                    }
                }

                candidates.put(cell, set);
            }

            return candidates;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public static byte[] sudokuRegionToBytes(SudokuRegionLayout layout) {
        if (layout == null) return new byte[0];

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            Integer[][] grid = layout.getRegions();
            int size = (grid != null && grid.length > 0) ? grid.length : 9;
            int type = layout.getType();

            dos.writeByte(size);

            if (type == SudokuRegionLayout.getCUSTOM_TYPE_ID()) {
                dos.writeBoolean(true);
                for (int r = 0; r < size; r++) {
                    for (int c = 0; c < size; c++) {
                        int regVal = (grid != null && grid[r][c] != null) ? grid[r][c] : 0;
                        dos.writeByte(regVal);
                    }
                }
            } else {
                dos.writeBoolean(false);
                dos.writeInt(type);
            }

            return compress(baos.toByteArray());
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static SudokuRegionLayout bytesToSudokuRegion(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;

        byte[] data = decompress(bytes);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {

            int size = dis.readByte() & 0xFF;
            boolean isCustom = dis.readBoolean();

            if (isCustom) {
                Integer[][] grid = new Integer[size][size];
                for (int r = 0; r < size; r++) {
                    for (int c = 0; c < size; c++) {
                        grid[r][c] = (int) dis.readByte();
                    }
                }

                return new SudokuRegionLayout(grid);
            } else {
                int type = dis.readInt();
                SudokuRegionLayout layout = new SudokuRegionLayout();
                layout.setType(type);
                return layout;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] sudokuPatternToBytes(SudokuPatternLayout sudokuPatternLayout) {
        if (sudokuPatternLayout == null || sudokuPatternLayout.getPattern() == null) {
            return new byte[0];
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            int size = sudokuPatternLayout.getPattern().length;
            dos.writeByte(size);

            Integer type = sudokuPatternLayout.getType();
            int typeVal = (type != null) ? type : -1;

            if (typeVal == SudokuPatternLayout.getCustomTypeId()) {
                dos.writeByte(SudokuPatternLayout.getCustomTypeId());
                byte[] patternBytes = serializeCustomPattern(sudokuPatternLayout.getPattern());
                dos.writeInt(patternBytes.length);
                dos.write(patternBytes);
            } else {
                dos.writeByte(typeVal);
            }

            return compress(baos.toByteArray());
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static SudokuPatternLayout bytesToSudokuPattern(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;

        byte[] data = decompress(bytes);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream dis = new DataInputStream(bais)) {

            int size = dis.readByte() & 0xFF;
            byte type = dis.readByte();

            if (type == -1) {
                return SudokuPatternLayout.createEmptyPattern(size);
            } else if (type == SudokuPatternLayout.getCustomTypeId()) {
                int length = dis.readInt();
                byte[] bytesPattern = dis.readNBytes(length);
                int[][] rawGrid = deserializeCustomPattern(bytesPattern);

                Integer[][] grid = new Integer[size][size];
                for (int r = 0; r < size; r++) {
                    for (int c = 0; c < size; c++) {
                        grid[r][c] = rawGrid[r][c];
                    }
                }

                return new SudokuPatternLayout(grid, true);
            } else {
                SudokuPatternLayout layout = new SudokuPatternLayout();
                layout.setType((int) type);
                return layout;
            }
        } catch (Exception e) {
            return null;
        }
    }


    private static ByteBuffer allocateSudokuBuffer(int size) {
        int totalCells = size * size;

        int neededCapacity;
        if (size >= 16) {
            neededCapacity = (size * size) + 1;
        } else {
            neededCapacity = (int) (Math.ceil((double) totalCells / 2)) + 1;
        }

        return ByteBuffer.allocate(neededCapacity);
    }


    public static byte[] serializeCustomPattern(Integer[][] board) {
        if (board == null) return null;

        int[][] converted = Arrays.stream(board)
                .map(row -> Arrays.stream(row)
                        .mapToInt(val -> (val != null && val >= 0) ? val : 0)
                        .toArray())
                .toArray(int[][]::new);

        return serializeCustomPattern(converted);
    }

    public static byte[] serializeCustomPattern(int[][] board) {
        if (board == null || board.length == 0) return new byte[0];

        int size = board.length;
        ByteBuffer buffer = allocateSudokuBuffer(size);

        int gridSize = board.length;
        SudokuSize type = SudokuSize.getTypeByGridSize(gridSize);
        if (type == null) return new byte[0];

        buffer.put((byte) type.getGridSize());

        switch (type) {
            case SIXTEEN -> {
                for (int[] row : board) {
                    for (int val : row) {
                        buffer.put((byte) val);
                    }
                }
            }
            default -> {
                boolean halfByte = false;
                byte currentByte = 0;
                for (int[] row : board) {
                    for (int val : row) {
                        if (halfByte) {
                            currentByte |= (byte) (val & 0xF);
                            buffer.put(currentByte);
                        } else {
                            currentByte = (byte) (val << 4);
                        }
                        halfByte = !halfByte;
                    }
                }
                if (halfByte) {
                    buffer.put(currentByte);
                }
            }
        }

        return compress(buffer.array());
    }

    public static int[][] deserializeCustomPattern(byte[] encodedBoard) {
        encodedBoard = decompress(encodedBoard);
        int gridSize = encodedBoard[0];
        SudokuSize type = SudokuSize.getTypeByGridSize(gridSize);
        int[][] board = new int[gridSize][gridSize];
        int currentByteIndex = 1;

        switch (type) {
            case SIXTEEN -> {
                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        board[i][j] = encodedBoard[currentByteIndex++];
                    }
                }
            }
            default -> {
                boolean halfByte = false;
                byte byteValue = 0;
                loop:
                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        int value;
                        if (!halfByte) {
                            if (currentByteIndex >= encodedBoard.length) break loop;
                            byteValue = encodedBoard[currentByteIndex];
                            value = (byteValue >> 4) & 0x0F;
                        } else {
                            value = byteValue & 0x0F;
                            currentByteIndex++;
                        }
                        halfByte = !halfByte;
                        board[i][j] = value;
                    }
                }
            }
        }

        return board;
    }

    public static SudokuCell createCell(SudokuVariant sudokuVariant, int row, int col, int value) {
        var cell = new SudokuCell(row, col, value);
        cell.setVariant(sudokuVariant);
        return cell;
    }

    public static byte[] compTypesToBytes(List<CompType[]> types) {
        if (types == null || types.isEmpty()) return new byte[0];

        int rows = types.size();
        int cols = types.getFirst().length;

        int totalBits = rows * cols;
        int byteCount = 2 + (totalBits + 7) / 8;

        byte[] result = new byte[byteCount];
        result[0] = (byte) rows;
        result[1] = (byte) cols;

        int globalBitIdx = 0;
        for (CompType[] region : types) {
            for (int c = 0; c < cols; c++) {
                int byteIndex = 2 + (globalBitIdx / 8);
                int bitInByte = 7 - (globalBitIdx % 8);

                int value = (region[c] != null) ? region[c].getValue() : 0;
                result[byteIndex] |= (byte) ((value & 1) << bitInByte);

                globalBitIdx++;
            }
        }

        return result;
    }

    public static List<CompType[]> bytesToCompTypeArray(byte[] bytes) {
        if (bytes == null || bytes.length < 2) return Collections.emptyList();

        int rows = bytes[0] & 0xFF;
        int cols = bytes[1] & 0xFF;

        List<CompType[]> result = new ArrayList<>(rows);
        int globalBitIdx = 0;

        for (int r = 0; r < rows; r++) {
            CompType[] region = new CompType[cols];
            for (int c = 0; c < cols; c++) {
                int byteIndex = 2 + (globalBitIdx / 8);
                int bitInByte = 7 - (globalBitIdx % 8);

                int value = (bytes[byteIndex] >> bitInByte) & 1;
                region[c] = CompType.fromValue(value);

                globalBitIdx++;
            }
            result.add(region);
        }

        return result;
    }

    public static List<Pair<GridCell, GridCell>> bytesToConsecutivePairs(byte[] bytes) {
        bytes = decompress(bytes);
        int count = bytes[0] & 0xFF;
        List<Pair<GridCell, GridCell>> pairs = new ArrayList<>(count);

        int index = 1;
        for (int i = 0; i < count; i++) {
            byte b1 = bytes[index++];
            byte b2 = bytes[index++];

            int[] first = {(b1 >> 4) & 0x0F, b1 & 0x0F};
            int[] second = {(b2 >> 4) & 0x0F, b2 & 0x0F};

            pairs.add(new Pair<>(new GridCell(first), new GridCell(second)));
        }
        return pairs;
    }

    public static byte[] consecutivePairsToBytes(List<Pair<GridCell, GridCell>> pairs) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + pairs.size() * 2);
        buffer.put((byte) pairs.size());

        pairs.forEach(pair -> {
            GridCell first = pair.getFirst();
            GridCell second = pair.getSecond();

            byte b1 = (byte) ((first.row() << 4) | (first.col() & 0x0F));
            byte b2 = (byte) ((second.row() << 4) | (second.col() & 0x0F));

            buffer.put(b1);
            buffer.put(b2);
        });
        return compress(buffer.array());
    }

    public static byte[] paritiesToBytes(ParityType[][] attribute) {
        if (attribute == null) return null;
        int size = attribute.length;
        int cellCount = size * size;
        int arraySize = 1 + (cellCount + 3) / 4;
        byte[] data = new byte[arraySize];
        data[0] = (byte) size;

        int index = 0;
        for (ParityType[] parityTypes : attribute) {
            for (int c = 0; c < size; c++) {
                int value = parityTypes[c].getValue() & 0b11;
                int byteIndex = 1 + index / 4;
                int shift = (index % 4) * 2;
                data[byteIndex] |= (byte) (value << shift);
                index++;
            }
        }
        return data;
    }

    public static ParityType[][] bytesToParities(byte[] dbData) {
        if (dbData == null || dbData.length == 0) return null;

        int size = dbData[0] & 0xFF;
        ParityType[][] constraints = new ParityType[size][size];

        int index = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int byteIndex = 1 + index / 4;
                int shift = (index % 4) * 2;
                int value = (dbData[byteIndex] >> shift) & 0b11;
                constraints[r][c] = ParityType.fromValue(value);
                index++;
            }
        }
        return constraints;
    }

    public static byte[] xvPairsToBytes(List<XVPair> marks) {
        if (marks == null || marks.isEmpty()) return new byte[0];
        ByteBuffer buffer = ByteBuffer.allocate(2 + marks.size() * 3);
        buffer.putShort((short) marks.size());

        for (XVPair mark : marks) {
            byte b1 = (byte) ((mark.first().row() << 4) | (mark.first().col() & 0x0F));
            byte b2 = (byte) ((mark.second().row() << 4) | (mark.second().col() & 0x0F));
            buffer.put(b1);
            buffer.put(b2);
            buffer.put((byte) (mark.markType() == MarkType.X ? 1 : 0));
        }
        return compress(buffer.array());
    }

    public static List<XVPair> bytesToXVPairs(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return new ArrayList<>();
        byte[] data = decompress(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int count = buffer.getShort() & 0xFFFF;
        List<XVPair> marks = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            byte b1 = buffer.get();
            byte b2 = buffer.get();
            byte type = buffer.get();

            GridCell first = new GridCell((b1 >> 4) & 0x0F, b1 & 0x0F);
            GridCell second = new GridCell((b2 >> 4) & 0x0F, b2 & 0x0F);
            MarkType markType = (type == 1) ? MarkType.X : MarkType.V;

            marks.add(new XVPair(first, second, markType));
        }
        return marks;
    }

    public static byte[] vudokuMarksToBytes(List<VudokuMark> marks) {
        if (marks == null || marks.isEmpty()) return new byte[0];
        ByteBuffer buffer = ByteBuffer.allocate(2 + marks.size() * 3);
        buffer.putShort((short) marks.size());

        for (VudokuMark mark : marks) {
            buffer.put((byte) ((mark.vertex().row() << 4) | (mark.vertex().col() & 0x0F)));
            buffer.put((byte) ((mark.arm1().row() << 4) | (mark.arm1().col() & 0x0F)));
            buffer.put((byte) ((mark.arm2().row() << 4) | (mark.arm2().col() & 0x0F)));
        }
        return compress(buffer.array());
    }

    public static List<VudokuMark> bytesToVudokuMarks(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return new ArrayList<>();
        byte[] data = decompress(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int count = buffer.getShort() & 0xFFFF;
        List<VudokuMark> marks = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            byte v = buffer.get();
            byte a1 = buffer.get();
            byte a2 = buffer.get();
            marks.add(new VudokuMark(
                    new GridCell((v >> 4) & 0x0F, v & 0x0F),
                    new GridCell((a1 >> 4) & 0x0F, a1 & 0x0F),
                    new GridCell((a2 >> 4) & 0x0F, a2 & 0x0F)
            ));
        }
        return marks;
    }

    public static byte[] serializeBetweenLines(List<BetweenLine> lines) {
        if (lines == null || lines.isEmpty()) return new byte[0];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            dos.writeShort(lines.size());
            for (BetweenLine line : lines) {

                dos.writeByte((byte) ((line.startCircle().row() << 4) | (line.startCircle().col() & 0x0F)));

                dos.writeByte((byte) ((line.endCircle().row() << 4) | (line.endCircle().col() & 0x0F)));

                dos.writeByte(line.lineCells().size());
                for (GridCell cell : line.lineCells()) {
                    dos.writeByte((byte) ((cell.row() << 4) | (cell.col() & 0x0F)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compress(baos.toByteArray());
    }

    public static List<BetweenLine> deserializeBetweenLines(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return new ArrayList<>();
        byte[] data = decompress(bytes);
        List<BetweenLine> lines = new ArrayList<>();

        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            int lineCount = dis.readShort() & 0xFFFF;
            for (int i = 0; i < lineCount; i++) {
                byte bStart = dis.readByte();
                GridCell start = new GridCell((bStart >> 4) & 0x0F, bStart & 0x0F);

                byte bEnd = dis.readByte();
                GridCell end = new GridCell((bEnd >> 4) & 0x0F, bEnd & 0x0F);
                int cellSize = dis.readByte() & 0xFF;
                List<GridCell> path = new ArrayList<>(cellSize);
                for (int j = 0; j < cellSize; j++) {
                    byte b = dis.readByte();
                    path.add(new GridCell((b >> 4) & 0x0F, b & 0x0F));
                }
                lines.add(new BetweenLine(start, end, path));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
