package cz.logicgo.core.gameClasses.maze.dataStructures.gameModes;


import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public final class PatternModifier implements MazeModifier {
    final private static MazeType mazeType = MazeType.PATTERN;
    final private MazeShape mazeShape;
    final private Map<MazeCell, Integer> cellPatterns;
    final private int patternCount;

    public PatternModifier(MazeShape mazeShape, Map<MazeCell, Integer> cellPatterns) {
        this.mazeShape = mazeShape;
        this.cellPatterns = cellPatterns;
        this.patternCount = cellPatterns.values().stream().mapToInt(Integer::intValue).max().orElse(-1) + 1;
    }

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(mazeShape.getId());
            dos.writeShort(cellPatterns.size());
            for (Map.Entry<MazeCell, Integer> entry : cellPatterns.entrySet()) {
                dos.writeShort(entry.getKey().getRow());
                dos.writeShort(entry.getKey().getCol());
                dos.writeByte(entry.getValue());
            }
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @Override
    public int getId() {
        return mazeType.getId();
    }

    @Override
    public MazeModifier copy(MazeCell[][] grid) {
        Map<MazeCell, Integer> newPatterns = new HashMap<>();
        for (Map.Entry<MazeCell, Integer> entry : cellPatterns.entrySet()) {
            MazeCell oldCell = entry.getKey();
            newPatterns.put(grid[oldCell.getRow()][oldCell.getCol()], entry.getValue());
        }
        return new PatternModifier(mazeShape, newPatterns);
    }

    public static PatternModifier deserialize(byte[] bytes, MazeGrid grid) {
        Map<MazeCell, Integer> loadedPatterns = new HashMap<>();
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (dis.available() <= 0) throw new IOException("Empty Data");

            MazeShape loadedShape = MazeShape.fromId(dis.readByte());
            int count = dis.readShort();

            for (int i = 0; i < count; i++) {
                int row = dis.readShort();
                int col = dis.readShort();
                int patternId = dis.readByte();
                MazeCell cell = grid.getCell(row, col);
                if (cell != null) loadedPatterns.put(cell, patternId);
            }
            return new PatternModifier(loadedShape, loadedPatterns);
        } catch (Exception e) {
            return null;
        }
    }

    public MazeShape getMazeShape() {
        return mazeShape;
    }

    public Map<MazeCell, Integer> getCellPatterns() {
        return cellPatterns;
    }

    public int getPatternCount() {
        return patternCount;
    }
}
