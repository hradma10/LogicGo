package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import jakarta.persistence.AttributeConverter;

public class MazeAlgorithmConverter implements AttributeConverter<MazeAlgorithm, Integer> {


    @Override
    public Integer convertToDatabaseColumn(MazeAlgorithm attribute) {
        return attribute.getId();
    }


    @Override
    public MazeAlgorithm convertToEntityAttribute(Integer dbData) {
        return PersistableEnum.fromId(dbData, MazeAlgorithm.class);
    }
}
