package cz.logicgo.core.gameClasses.maze.dataStructures.gameModes;


import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public record TollModifier(MazeShape mazeShape, Map<MazeCell, Integer> tolls) implements MazeModifier {
    final private static MazeType mazeType = null;

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(mazeShape.getId());
            dos.writeShort(tolls.size());
            for (Map.Entry<MazeCell, Integer> entry : tolls.entrySet()) {
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
        Map<MazeCell, Integer> newTolls = new HashMap<>();
        for (Map.Entry<MazeCell, Integer> entry : tolls.entrySet()) {
            MazeCell oldCell = entry.getKey();
            newTolls.put(grid[oldCell.getRow()][oldCell.getCol()], entry.getValue());
        }
        return new TollModifier(mazeShape, newTolls);
    }

    public static TollModifier deserialize(byte[] bytes, MazeGrid grid) {
        Map<MazeCell, Integer> loadedTolls = new HashMap<>();
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (dis.available() <= 0) throw new IOException("Empty Data");

            MazeShape shape = MazeShape.fromId(dis.readByte());
            int count = dis.readShort();

            for (int i = 0; i < count; i++) {
                int row = dis.readShort();
                int col = dis.readShort();
                int tollValue = dis.readByte();
                MazeCell cell = grid.getCell(row, col);
                if (cell != null) loadedTolls.put(cell, tollValue);
            }
            return new TollModifier(shape, loadedTolls);
        } catch (Exception e) {
            return null;
        }
    }
}
