package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter(autoApply = true)
public class GameTypeConverter implements AttributeConverter<TypeGame, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TypeGame typeGame) {
        if (typeGame == null) {
            return null;
        }
        return typeGame.getId();
    }

    @Override
    public TypeGame convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return PersistableEnum.fromId(dbData, TypeGame.class);
    }
}
