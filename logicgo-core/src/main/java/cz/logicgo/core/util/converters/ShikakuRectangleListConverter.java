package cz.logicgo.core.util.converters;

import cz.logicgo.core.gameClasses.shikaku.ShikakuRectangle;
import cz.logicgo.core.util.boardConverters.ShikakuConverters;
import jakarta.persistence.AttributeConverter;

import java.util.List;

public class ShikakuRectangleListConverter implements AttributeConverter<List<ShikakuRectangle>, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(List<ShikakuRectangle> attribute) {
        return ShikakuConverters.shikakuRectangleListToBytes(attribute);
    }

    @Override
    public List<ShikakuRectangle> convertToEntityAttribute(byte[] dbData) {
        return ShikakuConverters.bytesToRectangleList(dbData);
    }
}
