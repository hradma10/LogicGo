package cz.logicgo.core.util.converters;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.logicgo.core.gameClasses.export.gameTypes.GameMode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class GameModeConverter implements AttributeConverter<GameMode, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
    }

    @Override
    public String convertToDatabaseColumn(GameMode attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameMode convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, GameMode.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
