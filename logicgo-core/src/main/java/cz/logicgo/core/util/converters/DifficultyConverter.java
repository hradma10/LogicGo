package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DifficultyConverter implements AttributeConverter<Difficulty, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Difficulty difficulty) {
        if (difficulty == null) return null;
        return difficulty.getId();
    }

    @Override
    public Difficulty convertToEntityAttribute(Integer value) {
        return PersistableEnum.fromId(value, Difficulty.class);
    }
}
