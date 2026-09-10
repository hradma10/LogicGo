package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import jakarta.persistence.AttributeConverter;

public class ShikakuTypeConverter implements AttributeConverter<ShikakuType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ShikakuType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getId();
    }

    @Override
    public ShikakuType convertToEntityAttribute(Integer dbData) {
        return PersistableEnum.fromId(dbData, ShikakuType.class);
    }
}
