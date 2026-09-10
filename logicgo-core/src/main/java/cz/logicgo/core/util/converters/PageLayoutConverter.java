package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.enums.PageLayout;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PageLayoutConverter implements AttributeConverter<PageLayout, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PageLayout previewType) {
        if (previewType == null) {
            return null;
        }
        return previewType.getId();
    }

    @Override
    public PageLayout convertToEntityAttribute(Integer value) {
        if (value == null) {
            return null;
        }
        return PersistableEnum.fromId(value, PageLayout.class);
    }
}
