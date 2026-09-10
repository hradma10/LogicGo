package cz.logicgo.core.util.boardConverters;


import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.gameClasses.bridge.BoardSize;
import cz.logicgo.core.gameClasses.bridge.Island;
import cz.logicgo.core.gameClasses.bridge.IslandBridge;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cz.logicgo.core.misc.Compression.compress;
import static cz.logicgo.core.misc.Compression.decompress;


public class BridgeConverters {
    public static byte[] serializeIslands(List<Island> islands) {
        if (islands == null || islands.isEmpty()) return new byte[0];

        BoardSize boardSize = islands.getFirst().getBoardSize();

        int boardHeight = boardSize.height();
        int boardWidth = boardSize.width();
        int size = Stream.of(boardHeight, boardWidth, islands.size()).mapToInt(Integer::intValue).max().orElse(0);
        int neededCapacity;

        if (size >= 16) {
            neededCapacity = 3 + islands.size() * 4;
        } else {
            neededCapacity = 3 + islands.size() * 2;
        }

        ByteBuffer buffer = ByteBuffer.allocate(neededCapacity);

        buffer.put((byte) boardHeight);
        buffer.put((byte) boardWidth);
        buffer.put((byte) size);

        if (size >= 16) {
            islands.forEach(island -> {
                buffer.put((byte) island.getId());
                buffer.put(island.getRow().byteValue());
                buffer.put(island.getCol().byteValue());
                buffer.put((byte) island.getBridgeCount());
            });
        } else {
            islands.forEach(island -> {
                int id = island.getId() & 0xF;
                int row = island.getRow() & 0xF;
                int col = island.getCol() & 0xF;
                int val = island.getBridgeCount() & 0xF;
                int packed = (id << 12) | (row << 8) | (col << 4) | val;

                buffer.putShort((short) packed);
            });

        }
        try {
            return compress(buffer.array());
        } catch (Exception e) {
            return buffer.array();
        }

    }

    public static ArrayList<Island> deserializeIslands(byte[] encodedBoard) {
        encodedBoard = decompress(encodedBoard);
        if (encodedBoard == null || encodedBoard.length < 3) {
            return new ArrayList<>();
        }

        int boardHeight = encodedBoard[0] & 0xFF;
        int boardWidth = encodedBoard[1] & 0xFF;
        int size = encodedBoard[2] & 0xFF;

        BoardSize boardSize = new BoardSize(boardWidth, boardHeight);
        ArrayList<Island> islands = new ArrayList<>();
        int currentByteIndex = 3;

        if (size >= 16) {
            while (currentByteIndex + 3 < encodedBoard.length) {
                int id = encodedBoard[currentByteIndex++] & 0xFF;
                int row = encodedBoard[currentByteIndex++] & 0xFF;
                int col = encodedBoard[currentByteIndex++] & 0xFF;
                int val = encodedBoard[currentByteIndex++] & 0xFF;
                islands.add(new Island(id, row, col, val, boardSize));
            }
        } else {
            while (currentByteIndex + 1 < encodedBoard.length) {
                int packed = ((encodedBoard[currentByteIndex++] & 0xFF) << 8)
                        | (encodedBoard[currentByteIndex++] & 0xFF);

                int id = (packed >> 12) & 0xF;
                int row = (packed >> 8) & 0xF;
                int col = (packed >> 4) & 0xF;
                int val = packed & 0xF;

                islands.add(new Island(id, row, col, val, boardSize));
            }
        }

        return islands;
    }


    public static byte[] serializeIslandBridges(List<IslandBridge> bridgeIslands) {
        if (bridgeIslands == null || bridgeIslands.isEmpty()) return new byte[0];

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeShort(bridgeIslands.size());

            for (IslandBridge islandBridge : bridgeIslands) {
                dos.writeInt(islandBridge.getStartIsland().getId());
                dos.writeInt(islandBridge.getEndIsland().getId());
                dos.writeByte(islandBridge.getBridgeCount());
            }

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Chyba při serializaci", e);
        }
    }

    public static List<IslandBridge> deserializeIslandBridges(byte[] encoded) {
        List<IslandBridge> bridges = new ArrayList<>();
        if (encoded == null || encoded.length == 0) return bridges;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(encoded);
             DataInputStream dis = new DataInputStream(bais)) {

            int size = dis.readShort();
            for (int i = 0; i < size; i++) {
                int idStart = dis.readInt();
                int idEnd = dis.readInt();
                int bridgeCount = dis.readByte();

                IslandBridge bridge = new IslandBridge(new Island(idStart), new Island(idEnd));
                bridge.setBridgeCount(bridgeCount);
                bridges.add(bridge);
            }

            return bridges;
        } catch (IOException e) {
            throw new RuntimeException("Chyba při deserializaci", e);
        }
    }

    public static byte[] serializeConnectedIslands(HashMap<Bridge.ConnectedIslands, Integer> connectedIslands) {
        if (connectedIslands == null || connectedIslands.isEmpty()) return new byte[0];

        int sizeOfMap = connectedIslands.size();

        ByteBuffer buffer = ByteBuffer.allocate(sizeOfMap * 3);

        for (Map.Entry<Bridge.ConnectedIslands, Integer> val : connectedIslands.entrySet()) {
            buffer.put((byte) val.getKey().start().getId());
            buffer.put((byte) val.getKey().end().getId());
            buffer.put(val.getValue().byteValue());
        }

        return buffer.array();
    }

    public static HashMap<Bridge.ConnectedIslands, Integer> deserializeConnectedIslands(HashMap<Integer, Island> islandMap, byte[] data) {
        if (data == null) return new HashMap<>();
        HashMap<Bridge.ConnectedIslands, Integer> connectedIslands = new HashMap<>();

        int i = 0;
        while (i + 2 < data.length) {
            Island start = islandMap.get(data[i] & 0xFF);
            Island end = islandMap.get(data[i + 1] & 0xFF);
            int count = data[i + 2] & 0xFF;

            connectedIslands.put(new Bridge.ConnectedIslands(start, end), count);
            i += 3;
        }

        return connectedIslands;
    }
}
