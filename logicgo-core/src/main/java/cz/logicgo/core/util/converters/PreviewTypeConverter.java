package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.enums.PreviewType;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PreviewTypeConverter implements AttributeConverter<PreviewType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PreviewType previewType) {
        if (previewType == null) {
            return null;
        }
        return previewType.getId();
    }

    @Override
    public PreviewType convertToEntityAttribute(Integer value) {
        if (value == null) {
            return null;
        }
        return PersistableEnum.fromId(value, PreviewType.class);
    }
}
