package cz.logicgo.core.util.converters;

import cz.logicgo.core.gameClasses.viewers.CustomMask;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static cz.logicgo.core.misc.Compression.compress;
import static cz.logicgo.core.misc.Compression.decompress;
import static cz.logicgo.core.util.boardConverters.MazeConverters.deserializeMazeMaskBoolean;
import static cz.logicgo.core.util.boardConverters.MazeConverters.serializeMazeMaskBoolean;


@Converter
public class CustomMaskListConverter implements AttributeConverter<List<CustomMask>, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(List<CustomMask> attribute) {
        return serializeCustomMaskList(attribute);
    }

    @Override
    public List<CustomMask> convertToEntityAttribute(byte[] dbData) {
        return deserializeCustomMaskList(dbData);
    }

    public static byte[] serializeCustomMaskList(List<CustomMask> masks) {
        if (masks == null || masks.isEmpty()) return new byte[0];

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeShort(masks.size());

            for (CustomMask pojo : masks) {
                dos.writeByte(pojo.getGameType() != null ? pojo.getGameType().getId() : 0);

                byte[] maskBytes = serializeMazeMaskBoolean(pojo.getLayout());
                dos.writeInt(maskBytes.length);
                dos.write(maskBytes);
            }

            dos.flush();
            return compress(baos.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static List<CustomMask> deserializeCustomMaskList(byte[] dbData) {
        if (dbData == null || dbData.length == 0) return new ArrayList<>();

        byte[] decompressed = decompress(dbData);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(decompressed);
             DataInputStream dis = new DataInputStream(bais)) {

            int count = dis.readShort() & 0xFFFF;
            List<CustomMask> list = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                byte gameTypeId = dis.readByte();
                TypeGame gameType = PersistableEnum.fromId(gameTypeId, TypeGame.class);

                int length = dis.readInt();
                byte[] maskBytes = dis.readNBytes(length);
                boolean[][] layout = deserializeMazeMaskBoolean(maskBytes);

                CustomMask pojo = new CustomMask();
                pojo.setGameType(gameType);
                pojo.setLayout(layout);

                list.add(pojo);
            }

            return list;

        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
