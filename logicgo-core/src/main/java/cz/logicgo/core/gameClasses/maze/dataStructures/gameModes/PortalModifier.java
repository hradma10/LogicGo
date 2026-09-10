package cz.logicgo.core.gameClasses.maze.dataStructures.gameModes;


import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.HexagonalDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public non-sealed class PortalModifier implements MazeModifier {
    final private static MazeType mazeType = MazeType.PORTAL;
    final private List<MazeCell> cellsWithPortals;
    final private HashMap<MazeCell, MazeDirection> directionsOfPortals;
    final private List<Pair<MazeCell, MazeCell>> portals;
    final private MazeShape mazeShape;

    public PortalModifier(List<MazeCell> cellsWithPortals, HashMap<MazeCell, MazeDirection> directionsOfPortals, List<Pair<MazeCell, MazeCell>> portals, MazeShape mazeShape) {
        this.cellsWithPortals = cellsWithPortals;
        this.directionsOfPortals = directionsOfPortals;
        this.portals = portals;
        this.mazeShape = mazeShape;
    }

    @Override
    public MazeModifier copy(MazeCell[][] grid) {
        List<MazeCell> newCellsWithPortals = new ArrayList<>();
        for (MazeCell cell : this.cellsWithPortals()) {
            int row = cell.getRow();
            int col = cell.getCol();
            MazeCell realCell = grid[row][col];
            newCellsWithPortals.add(realCell);
        }

        HashMap<MazeCell, MazeDirection> newDirectionsOfPortals = new HashMap<>();
        for (Map.Entry<MazeCell, MazeDirection> cell : this.directionsOfPortals().entrySet()) {
            int row = cell.getKey().getRow();
            int col = cell.getKey().getCol();

            MazeDirection direction = cell.getValue();

            MazeCell realCell = grid[row][col];
            newDirectionsOfPortals.put(realCell, direction);
        }

        List<Pair<MazeCell, MazeCell>> portals = new ArrayList<>();

        for (Pair<MazeCell, MazeCell> pair : this.portals()) {
            MazeCell firstFake = pair.getFirst();
            MazeCell secondFake = pair.getSecond();

            MazeCell firstReal = grid[firstFake.getRow()][firstFake.getCol()];
            MazeCell secondReal = grid[secondFake.getRow()][secondFake.getCol()];

            portals.add(new Pair<>(firstReal, secondReal));

        }

        return new PortalModifier(newCellsWithPortals, newDirectionsOfPortals, portals, mazeShape);

    }


    public static PortalModifier deserialize(byte[] bytes, MazeGrid grid) {
        List<MazeCell> cellsWithAddedWall = new ArrayList<>();
        HashMap<MazeCell, MazeDirection> directionsOfAddedWalls = new HashMap<>();
        List<Pair<MazeCell, MazeCell>> portals = new ArrayList<>();

        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (dis.available() <= 0) {
                throw new IOException("Empty Data");
            }
            MazeShape mazeShape = MazeShape.fromId(dis.readByte());
            int directionsLength = dis.readInt();
            byte[] directionBytes = dis.readNBytes(directionsLength);

            try (DataInputStream disDirections = new DataInputStream(new ByteArrayInputStream(directionBytes))) {
                while (disDirections.available() > 0) {
                    int row = dis.readShort();
                    int col = dis.readShort();
                    byte directionByte = dis.readByte();

                    MazeDirection mazeDirection = switch (mazeShape) {
                        case RECTANGULAR -> RectangularDirection.getById(directionByte);
                        case HEXAGONAL -> HexagonalDirection.getById(directionByte);
                    };
                    MazeCell cell = grid.getCell(row, col);

                    cellsWithAddedWall.add(cell);
                    directionsOfAddedWalls.put(cell, mazeDirection);
                }
            }

            int portalsLength = dis.readInt();
            byte[] portalsBytes = dis.readNBytes(portalsLength);
            try (DataInputStream disPortals = new DataInputStream(new ByteArrayInputStream(portalsBytes))) {
                int rowEnter = disPortals.readShort();
                int colEnter = disPortals.readShort();

                int rowExit = disPortals.readShort();
                int colExit = disPortals.readShort();

                MazeCell enterCell = grid.getCell(rowEnter, colEnter);
                MazeCell exitCell = grid.getCell(rowExit, colExit);

                portals.add(new Pair<>(enterCell, exitCell));
            }

            return new PortalModifier(cellsWithAddedWall, directionsOfAddedWalls, portals, mazeShape);
        } catch (Exception e) {
            return new PortalModifier(new ArrayList<>(), new HashMap<>(), new ArrayList<>(), MazeShape.RECTANGULAR);
        }

    }

    public List<MazeCell> cellsWithPortals() {
        return cellsWithPortals;
    }

    public HashMap<MazeCell, MazeDirection> directionsOfPortals() {
        return directionsOfPortals;
    }

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            MazeShape mazeShape = this.mazeShape();
            dos.writeByte(mazeShape.getId());

            byte[] directionsBytes = getDirectionBytes();
            dos.writeInt(directionsBytes.length);
            dos.write(directionsBytes);

            byte[] portalBytes = getPortalBytes();

            dos.writeInt(portals.size());
            dos.write(portalBytes);

            return baos.toByteArray();

        } catch (Exception e) {
            return new byte[0];
        }
    }

    private byte[] getPortalBytes() throws IOException {
        byte[] portalBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            for (var entry : portals) {
                MazeCell enterPortal = entry.getFirst();
                MazeCell exitPortal = entry.getSecond();

                int rowEnter = enterPortal.getRow();
                int colEnter = enterPortal.getCol();
                int rowExit = exitPortal.getRow();
                int colExit = exitPortal.getCol();

                dos.writeShort(rowEnter);
                dos.writeShort(colEnter);

                dos.writeShort(rowExit);
                dos.writeShort(colExit);
            }
            portalBytes = baos.toByteArray();
        }
        return portalBytes;
    }

    private byte[] getDirectionBytes() throws IOException {
        byte[] directionsBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            for (var entry : directionsOfPortals.entrySet()) {
                MazeCell cell = entry.getKey();
                MazeDirection direction = entry.getValue();
                int row = cell.getRow();
                int col = cell.getCol();
                int directionByte = direction.getId();
                dos.writeShort(row);
                dos.writeShort(col);
                dos.writeByte(directionByte);
            }
            directionsBytes = baos.toByteArray();
        }
        return directionsBytes;
    }

    @Override
    public int getId() {
        return mazeType.getId();
    }

    public MazeShape mazeShape() {
        return mazeShape;
    }

    public List<Pair<MazeCell, MazeCell>> portals() {
        return portals;
    }
}
