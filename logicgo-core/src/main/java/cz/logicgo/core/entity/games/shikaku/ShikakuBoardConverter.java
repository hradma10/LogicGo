package cz.logicgo.core.entity.games.shikaku;

import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;
import cz.logicgo.core.util.boardConverters.ShikakuConverters;
import jakarta.persistence.AttributeConverter;

public class ShikakuBoardConverter implements AttributeConverter<ShikakuCell[][], byte[]> {
    @Override
    public byte[] convertToDatabaseColumn(ShikakuCell[][] attribute) {
        return ShikakuConverters.serializeBoard(attribute);
    }

    @Override
    public ShikakuCell[][] convertToEntityAttribute(byte[] dbData) {
        return ShikakuConverters.deserializeBoard(dbData);
    }
}
