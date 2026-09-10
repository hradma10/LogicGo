package cz.logicgo.core.util.boardConverters;


import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;
import cz.logicgo.core.gameClasses.shikaku.ShikakuRectangle;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import static cz.logicgo.core.misc.Compression.compress;
import static cz.logicgo.core.misc.Compression.decompress;


public class ShikakuConverters {

    public static byte[] serializeBoard(ShikakuCell[][] board) {
        if (board == null || board.length == 0) return new byte[0];

        int height = board.length;
        int width = board[0].length;
        ShikakuType type = ShikakuType.CLASSIC;

        if (type == null) return new byte[0];

        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(buffer)) {
            dos.writeByte(height);
            dos.writeByte(width);
            dos.writeByte(type.getType());
            for (ShikakuCell[] row : board) {
                for (ShikakuCell cell : row) {
                    if (cell == null) {
                        dos.writeBoolean(false);
                        continue;
                    }
                    dos.writeBoolean(true);
                    dos.writeInt(cell.getRegionId());
                    dos.writeByte(cell.getClue());
                }
            }

            return compress(buffer.toByteArray());
        } catch (Exception e) {
            return new byte[0];
        }


    }

    public static byte[] shikakuRectangleListToBytes(List<ShikakuRectangle> rectangles) {
        if (rectangles == null || rectangles.isEmpty()) return new byte[0];
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(buffer)) {
            int size = rectangles.size();
            dos.writeShort(size);
            for (ShikakuRectangle rectangle : rectangles) {
                dos.writeByte(rectangle.getMinRow());
                dos.writeByte(rectangle.getMinCol());
                dos.writeByte(rectangle.getMaxRow());
                dos.writeByte(rectangle.getMaxCol());
                dos.writeInt(rectangle.getId());
            }
            return compress(buffer.toByteArray());
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static List<ShikakuRectangle> bytesToRectangleList(byte[] encodedRectangles) {
        encodedRectangles = decompress(encodedRectangles);
        try {
            try (ByteArrayInputStream buffer = new ByteArrayInputStream(encodedRectangles);
                 DataInputStream dis = new DataInputStream(buffer)) {

                int size = dis.readShort();
                List<ShikakuRectangle> rectangles = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    int minRow = dis.readByte();
                    int minCol = dis.readByte();
                    int maxRow = dis.readByte();
                    int maxCol = dis.readByte();
                    int id = dis.readInt();
                    rectangles.add(new ShikakuRectangle(id, minRow, maxRow, minCol, maxCol));
                }
                return rectangles;

            } catch (Exception e) {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }

    }

    public static ShikakuCell[][] deserializeBoard(byte[] encodedBoard) {
        if (encodedBoard == null || encodedBoard.length == 0) return new ShikakuCell[0][0];

        try {
            byte[] decompressed = decompress(encodedBoard);
            try (ByteArrayInputStream buffer = new ByteArrayInputStream(decompressed);
                 DataInputStream dis = new DataInputStream(buffer)) {

                int height = dis.readUnsignedByte();
                int width = dis.readUnsignedByte();
                byte typeByte = dis.readByte();

                ShikakuCell[][] shikakuCells = new ShikakuCell[height][width];

                for (int r = 0; r < height; r++) {
                    for (int c = 0; c < width; c++) {
                        if (!dis.readBoolean()) continue;
                        int regionId = dis.readInt();
                        byte clue = dis.readByte();
                        shikakuCells[r][c] = new ShikakuCell(r, c, clue, regionId);
                    }
                }
                return shikakuCells;
            }
        } catch (Exception e) {
            return new ShikakuCell[0][0];
        }
    }
}
