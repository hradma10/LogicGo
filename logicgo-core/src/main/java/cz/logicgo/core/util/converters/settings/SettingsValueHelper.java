package cz.logicgo.core.util.converters.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.logicgo.core.misc.interfaces.PersistableEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SettingsValueHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private SettingsValueHelper() {}

    public static String serialize(Object value) {
        switch (value) {
            case null -> {
                return null;
            }
            case List<?> list when !list.isEmpty() && list.getFirst() instanceof String s && s.startsWith("#") -> {
                @SuppressWarnings("unchecked")
                List<String> hexList = (List<String>) list;
                return String.join(";", hexList);
            }
            case PersistableEnum persistable -> {
                return String.valueOf(persistable.getId());
            }
            case Enum<?> e -> {
                return e.name();
            }
            case String str -> {
                return str;
            }
            default -> {
            }
        }

        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert setting to string: " + value, e);
        }
    }

    public static List<String> deserializeColors(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(storedValue.split(";"))
                .filter(s -> !s.isBlank())
                .toList();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object deserialize(String value, Class<?> targetType) {
        if (value == null || targetType == null) {
            return null;
        }

        try {
            if (targetType.isEnum()) {
                String cleanValue = value.replace("\"", "").trim();
                if (PersistableEnum.class.isAssignableFrom(targetType)) {
                    try {
                        return PersistableEnum.fromId(Integer.parseInt(cleanValue), (Class) targetType);
                    } catch (NumberFormatException _) {
                        return Enum.valueOf((Class<Enum>) targetType, cleanValue);
                    }
                }
                return Enum.valueOf((Class<Enum>) targetType, cleanValue);
            }

            if (List.class.isAssignableFrom(targetType) && value.startsWith("#")) {
                return deserializeColors(value);
            }

            if (targetType == String.class) {
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                    return value.substring(1, value.length() - 1);
                }
                return value;
            }

            if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.parseBoolean(value.replace("\"", "").trim());
            }
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(value.replace("\"", "").trim());
            }
            if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(value.replace("\"", "").trim());
            }
            if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(value.replace("\"", "").trim());
            }
            return MAPPER.readValue(value, targetType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
