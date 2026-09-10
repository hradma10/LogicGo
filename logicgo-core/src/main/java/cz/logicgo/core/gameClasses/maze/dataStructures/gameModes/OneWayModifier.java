package cz.logicgo.core.gameClasses.maze.dataStructures.gameModes;


import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public record OneWayModifier(List<Pair<MazeCell, MazeCell>> oneWayPaths) implements MazeModifier {
    final private static MazeType mazeType = null;

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeInt(oneWayPaths.size());
            for (Pair<MazeCell, MazeCell> path : oneWayPaths) {
                dos.writeShort(path.getFirst().getRow());
                dos.writeShort(path.getFirst().getCol());
                dos.writeShort(path.getSecond().getRow());
                dos.writeShort(path.getSecond().getCol());
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
        List<Pair<MazeCell, MazeCell>> newPaths = new ArrayList<>();
        for (Pair<MazeCell, MazeCell> path : oneWayPaths) {
            MazeCell c1 = grid[path.getFirst().getRow()][path.getFirst().getCol()];
            MazeCell c2 = grid[path.getSecond().getRow()][path.getSecond().getCol()];
            newPaths.add(new Pair<>(c1, c2));
        }
        return new OneWayModifier(newPaths);
    }

    public static OneWayModifier deserialize(byte[] bytes, MazeGrid grid) {
        List<Pair<MazeCell, MazeCell>> paths = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (dis.available() <= 0) throw new IOException("Empty Data");

            int size = dis.readInt();
            for (int i = 0; i < size; i++) {
                int r1 = dis.readShort();
                int c1 = dis.readShort();
                int r2 = dis.readShort();
                int c2 = dis.readShort();

                MazeCell cell1 = grid.getCell(r1, c1);
                MazeCell cell2 = grid.getCell(r2, c2);
                if (cell1 != null && cell2 != null) {
                    paths.add(new Pair<>(cell1, cell2));
                }
            }
            return new OneWayModifier(paths);
        } catch (Exception e) {
            return null;
        }
    }
}
