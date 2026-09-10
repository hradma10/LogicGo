package cz.logicgo.core.gameClasses.maze.dataStructures.gameModes;

import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static cz.logicgo.core.gameClasses.maze.MazeUtils.RegionEdge;


public non-sealed class WallModifier implements MazeModifier {
    final private static MazeType mazeType = MazeType.WALLS;
    final private List<RegionEdge> regionEdges;
    final private int trueWayWallCount;
    final private MazeShape mazeShape;

    public WallModifier(List<RegionEdge> regionEdges, int trueWayWallCount, MazeShape mazeShape) {
        this.regionEdges = regionEdges;
        this.trueWayWallCount = trueWayWallCount;
        this.mazeShape = mazeShape;
    }

    public List<RegionEdge> regionEdges() {
        return regionEdges;
    }

    public int getTrueWayWallCount() {
        return trueWayWallCount;
    }

    public List<RegionEdge> getRegionEdges() {
        return regionEdges;
    }


    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            MazeShape mazeShape = this.mazeShape();
            dos.writeByte(mazeShape.getId());
            dos.writeByte(trueWayWallCount);
            for (var entry : regionEdges) {
                int regionIdA = entry.regionA();
                int regionIdB = entry.regionB();

                dos.writeByte(regionIdA);
                dos.writeByte(regionIdB);

                MazeCell start = entry.cellA();
                MazeCell end = entry.cellB();

                int rowStart = start.getRow();
                int colStart = start.getCol();
                dos.writeShort(rowStart);
                dos.writeShort(colStart);

                int rowEnd = end.getRow();
                int colEnd = end.getCol();
                dos.writeShort(rowEnd);
                dos.writeShort(colEnd);

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
        List<RegionEdge> newRegionEdges = new ArrayList<>();

        for (var entry : this.regionEdges()) {
            int regionA = entry.regionA();
            int regionB = entry.regionB();
            MazeCell oldStart = entry.cellA();
            MazeCell oldEnd = entry.cellB();

            MazeCell newStart = grid[oldStart.getRow()][oldStart.getCol()];
            MazeCell newEnd = grid[oldEnd.getRow()][oldEnd.getCol()];

            RegionEdge newEdge = new RegionEdge(regionA, newStart, regionB, newEnd);
            newRegionEdges.add(newEdge);
        }

        return new WallModifier(newRegionEdges, trueWayWallCount, mazeShape);
    }


    public static WallModifier deserialize(byte[] bytes, MazeGrid grid) {
        List<RegionEdge> edges = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (dis.available() <= 0) {
                throw new IOException("Empty Data");
            }
            MazeShape mazeShape = MazeShape.fromId(dis.readByte());
            int trueWayWallCount = dis.readByte();
            while (dis.available() > 0) {
                int regionAId = dis.readByte();
                int regionBId = dis.readByte();

                int rowStart = dis.readShort();
                int colStart = dis.readShort();

                int rowEnd = dis.readShort();
                int colEnd = dis.readShort();

                MazeCell start = grid.getCell(rowStart, colStart);
                MazeCell end = grid.getCell(rowEnd, colEnd);

                edges.add(new RegionEdge(regionAId, start, regionBId, end));
            }
            return new WallModifier(edges, trueWayWallCount, mazeShape);
        } catch (Exception e) {
            return new WallModifier(new ArrayList<>(), 0, MazeShape.RECTANGULAR);
        }


    }

    public int trueWayWallCount() {
        return trueWayWallCount;
    }

    public MazeShape mazeShape() {
        return mazeShape;
    }
}
