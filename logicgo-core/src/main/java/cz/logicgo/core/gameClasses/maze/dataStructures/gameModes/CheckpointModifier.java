package cz.logicgo.core.gameClasses.maze.dataStructures.gameModes;


import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CheckpointModifier implements MazeModifier {
    final private static MazeType mazeType = MazeType.CHECKPOINT;
    private final Map<Integer, MazeCell> orderedCheckpoints;
    private final List<Pair<MazeCell, MazeCell>> oneWayTraps;

    public CheckpointModifier(Map<Integer, MazeCell> orderedCheckpoints, List<Pair<MazeCell, MazeCell>> oneWayTraps) {
        this.orderedCheckpoints = orderedCheckpoints;
        this.oneWayTraps = oneWayTraps != null ? oneWayTraps : new ArrayList<>();
    }

    public Map<Integer, MazeCell> getCheckpoints() {
        return orderedCheckpoints;
    }

    public List<Pair<MazeCell, MazeCell>> getOneWays() {
        return oneWayTraps;
    }

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeInt(orderedCheckpoints.size());
            for (var entry : orderedCheckpoints.entrySet()) {
                dos.writeInt(entry.getKey());
                dos.writeShort(entry.getValue().getRow());
                dos.writeShort(entry.getValue().getCol());
            }

            dos.writeInt(oneWayTraps.size());
            for (Pair<MazeCell, MazeCell> trap : oneWayTraps) {
                dos.writeShort(trap.getFirst().getRow());
                dos.writeShort(trap.getFirst().getCol());
                dos.writeShort(trap.getSecond().getRow());
                dos.writeShort(trap.getSecond().getCol());
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
        Map<Integer, MazeCell> newOrderedCheckpoints = new HashMap<>();
        for (var entry : orderedCheckpoints.entrySet()) {
            MazeCell oldCell = entry.getValue();
            newOrderedCheckpoints.put(entry.getKey(), grid[oldCell.getRow()][oldCell.getCol()]);
        }

        List<Pair<MazeCell, MazeCell>> newTraps = new ArrayList<>();
        for (Pair<MazeCell, MazeCell> trap : oneWayTraps) {
            MazeCell from = grid[trap.getFirst().getRow()][trap.getFirst().getCol()];
            MazeCell to = grid[trap.getSecond().getRow()][trap.getSecond().getCol()];
            newTraps.add(new Pair<>(from, to));
        }

        return new CheckpointModifier(newOrderedCheckpoints, newTraps);
    }

    public static CheckpointModifier deserialize(byte[] bytes, MazeGrid grid) {
        Map<Integer, MazeCell> map = new HashMap<>();
        List<Pair<MazeCell, MazeCell>> traps = new ArrayList<>();

        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (dis.available() <= 0) throw new IOException("Empty Data");

            int size = dis.readInt();
            for (int i = 0; i < size; i++) {
                int order = dis.readInt();
                int row = dis.readShort();
                int col = dis.readShort();

                MazeCell cell = grid.getCell(row, col);
                if (cell != null) {
                    map.put(order, cell);
                }
            }

            if (dis.available() > 0) {
                int trapSize = dis.readInt();
                for (int i = 0; i < trapSize; i++) {
                    int r1 = dis.readShort();
                    int c1 = dis.readShort();
                    int r2 = dis.readShort();
                    int c2 = dis.readShort();

                    MazeCell cell1 = grid.getCell(r1, c1);
                    MazeCell cell2 = grid.getCell(r2, c2);

                    if (cell1 != null && cell2 != null) {
                        traps.add(new Pair<>(cell1, cell2));
                    }
                }
            }

            return new CheckpointModifier(map, traps);
        } catch (Exception e) {
            return null;
        }
    }
}
