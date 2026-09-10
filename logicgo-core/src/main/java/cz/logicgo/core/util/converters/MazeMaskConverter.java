package cz.logicgo.core.util.converters;

import cz.logicgo.core.util.boardConverters.MazeConverters;
import jakarta.persistence.AttributeConverter;

public class MazeMaskConverter implements AttributeConverter<boolean[][], byte[]> {
    @Override
    public byte[] convertToDatabaseColumn(boolean[][] attribute) {
        if (attribute == null) {
            return null;
        }
        return MazeConverters.serializeMazeMaskBoolean(attribute);
    }

    @Override
    public boolean[][] convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) {
            return null;
        }

        return MazeConverters.deserializeMazeMaskBoolean(dbData);
    }
}
