package cz.logicgo.core.util.converters;

import jakarta.persistence.AttributeConverter;

import static cz.logicgo.core.util.boardConverters.MazeConverters.deserializeMazeMaskBoolean;
import static cz.logicgo.core.util.boardConverters.MazeConverters.serializeMazeMaskBoolean;

public class ShikakuMaskConverter implements AttributeConverter<boolean[][], byte[]> {
    @Override
    public byte[] convertToDatabaseColumn(boolean[][] attribute) {
        return serializeMazeMaskBoolean(attribute);
    }

    @Override
    public boolean[][] convertToEntityAttribute(byte[] dbData) {
        return deserializeMazeMaskBoolean(dbData);
    }
}
