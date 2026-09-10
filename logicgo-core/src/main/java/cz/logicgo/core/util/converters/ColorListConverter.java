package cz.logicgo.core.util.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class ColorListConverter implements AttributeConverter<List<String>, String> {

    private static final String DELIMITER = ";";

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {

        StringBuilder builder = new StringBuilder();
        for (String s : attribute) {
            builder.append(s);
            builder.append(DELIMITER);
        }
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(dbData.split(DELIMITER))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
