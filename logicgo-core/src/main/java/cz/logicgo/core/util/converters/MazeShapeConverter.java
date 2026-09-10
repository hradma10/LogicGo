package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import jakarta.persistence.AttributeConverter;

public class MazeShapeConverter implements AttributeConverter<MazeShape, Integer> {

    @Override
    public Integer convertToDatabaseColumn(MazeShape attribute) {
        return attribute.getId();
    }

    @Override
    public MazeShape convertToEntityAttribute(Integer dbData) {
        return PersistableEnum.fromId(dbData, MazeShape.class);
    }


}
