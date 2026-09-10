package cz.logicgo.core.util.converters;

import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.viewers.SudokuPattern;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static cz.logicgo.core.misc.Compression.compress;
import static cz.logicgo.core.misc.Compression.decompress;
import static cz.logicgo.core.util.boardConverters.SudokuConverters.bytesToSudokuPattern;
import static cz.logicgo.core.util.boardConverters.SudokuConverters.sudokuPatternToBytes;


@Converter
public class SudokuPatternListConverter implements AttributeConverter<List<SudokuPattern>, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(List<SudokuPattern> attribute) {
        return serializeSudokuPatternList(attribute);
    }

    @Override
    public List<SudokuPattern> convertToEntityAttribute(byte[] dbData) {
        return deserializeSudokuPatternList(dbData);
    }

    public static byte[] serializeSudokuPatternList(List<SudokuPattern> patterns) {
        if (patterns == null || patterns.isEmpty()) return new byte[0];

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeShort(patterns.size());

            for (SudokuPattern pojo : patterns) {
                dos.writeBoolean(pojo.isCreated());

                byte[] patternBytes = sudokuPatternToBytes(pojo.getLayout());
                dos.writeInt(patternBytes.length);
                dos.write(patternBytes);
            }

            dos.flush();
            return compress(baos.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static List<SudokuPattern> deserializeSudokuPatternList(byte[] dbData) {
        if (dbData == null || dbData.length == 0) return new ArrayList<>();

        byte[] decompressed = decompress(dbData);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(decompressed);
             DataInputStream dis = new DataInputStream(bais)) {

            int count = dis.readShort() & 0xFFFF;
            List<SudokuPattern> list = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                boolean created = dis.readBoolean();

                int length = dis.readInt();
                byte[] patternBytes = dis.readNBytes(length);
                SudokuPatternLayout layout = bytesToSudokuPattern(patternBytes);

                SudokuPattern pojo = new SudokuPattern();
                pojo.setCreated(created);
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
