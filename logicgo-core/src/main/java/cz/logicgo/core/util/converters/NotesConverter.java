package cz.logicgo.core.util.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NotesConverter implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String content) {
        return (content == null || content.isEmpty()) ? null : content;
    }

    @Override
    public String convertToEntityAttribute(String content) {
        return content;
    }
}
