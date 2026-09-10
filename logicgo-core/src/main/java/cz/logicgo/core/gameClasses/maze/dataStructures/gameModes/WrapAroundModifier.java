package cz.logicgo.core.gameClasses.maze.dataStructures.gameModes;


import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public record WrapAroundModifier(List<Pair<MazeCell, MazeCell>> wrapLinks) implements MazeModifier {
    final private static MazeType mazeType = MazeType.WRAP_AROUND;

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeInt(wrapLinks.size());
            for (Pair<MazeCell, MazeCell> link : wrapLinks) {
                dos.writeShort(link.getFirst().getRow());
                dos.writeShort(link.getFirst().getCol());
                dos.writeShort(link.getSecond().getRow());
                dos.writeShort(link.getSecond().getCol());
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
        List<Pair<MazeCell, MazeCell>> newLinks = new ArrayList<>();
        for (Pair<MazeCell, MazeCell> link : wrapLinks) {
            MazeCell c1 = grid[link.getFirst().getRow()][link.getFirst().getCol()];
            MazeCell c2 = grid[link.getSecond().getRow()][link.getSecond().getCol()];
            newLinks.add(new Pair<>(c1, c2));
        }
        return new WrapAroundModifier(newLinks);
    }

    public static WrapAroundModifier deserialize(byte[] bytes, MazeGrid grid) {
        List<Pair<MazeCell, MazeCell>> links = new ArrayList<>();
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
                    links.add(new Pair<>(cell1, cell2));
                }
            }
            return new WrapAroundModifier(links);
        } catch (Exception e) {
            return null;
        }
    }
}
