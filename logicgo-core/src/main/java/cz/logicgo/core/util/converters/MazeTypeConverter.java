package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import jakarta.persistence.AttributeConverter;

public class MazeTypeConverter implements AttributeConverter<MazeType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(MazeType attribute) {
        return attribute.getId();
    }

    @Override
    public MazeType convertToEntityAttribute(Integer dbData) {
        return PersistableEnum.fromId(dbData, MazeType.class);
    }
}
