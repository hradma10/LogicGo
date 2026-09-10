package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.enums.gameTypes.maze.directions.HexagonalDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.RectangularDirection;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MazeDirectionConverter implements AttributeConverter<MazeDirection, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(MazeDirection attribute) {
        if (attribute == null) {
            return null;
        }
        byte[] data = new byte[2];
        data[0] = (byte) (attribute instanceof RectangularDirection ? 0 : 1);
        data[1] = (byte) attribute.getId();
        return data;
    }

    @Override
    public MazeDirection convertToEntityAttribute(byte[] dbData) {
        if (dbData == null || dbData.length < 2) {
            return null;
        }

        int type = dbData[0];
        int id = dbData[1];

        if (type == 0) {
            return PersistableEnum.fromId(id, RectangularDirection.class);
        } else if (type == 1) {
            return PersistableEnum.fromId(id, HexagonalDirection.class);
        }

        throw new IllegalArgumentException("Unknown MazeDirection type identifier: " + type);
    }
}
