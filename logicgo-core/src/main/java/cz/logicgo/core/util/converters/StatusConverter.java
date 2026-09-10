package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Status status) {
        if (status == null) {
            return null;
        }
        return status.getId();
    }

    @Override
    public Status convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return PersistableEnum.fromId(dbData, Status.class);
    }
}
