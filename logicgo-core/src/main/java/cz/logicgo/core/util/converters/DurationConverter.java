package cz.logicgo.core.util.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter(autoApply = true)
public class DurationConverter implements AttributeConverter<Duration, Long> {

    @Override
    public Long convertToDatabaseColumn(Duration duration) {
        if (duration == null) {
            return null;
        }
        return duration.toMillis();
    }

    @Override
    public Duration convertToEntityAttribute(Long value) {
        if (value == null) {
            return null;
        }
        return Duration.ofMillis(value);
    }
}
