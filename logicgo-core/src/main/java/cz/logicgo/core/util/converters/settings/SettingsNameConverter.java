package cz.logicgo.core.util.converters.settings;

import cz.logicgo.core.misc.ReflectionProvider;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Converter(autoApply = true)
public class SettingsNameConverter implements AttributeConverter<SettingKey, String> {

    private static final Map<String, SettingKey> map = new HashMap<>();

    static {
        Reflections reflections = ReflectionProvider.getInstance();

        Set<Class<? extends SettingKey>> subTypes = reflections.getSubTypesOf(SettingKey.class);

        for (Class<? extends SettingKey> type : subTypes) {
            if (type.isEnum()) {
                for (SettingKey key : type.getEnumConstants()) {
                    map.put(key.getQualifiedName(), key);
                }
            }
        }
    }

    @Override
    public String convertToDatabaseColumn(SettingKey settingKey) {
        if (settingKey == null) {
            return null;
        }
        return settingKey.getQualifiedName();
    }

    @Override
    public SettingKey convertToEntityAttribute(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return map.get(name);
    }
}
