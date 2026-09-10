package cz.logicgo.core.misc;


import cz.logicgo.core.misc.dataStructures.SimpleBiMap;
import cz.logicgo.core.misc.enums.keys.HotkeyEvent;
import cz.logicgo.core.misc.interfaces.Translatable;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TranslationLoader {

    private static final SimpleBiMap<String, String> translations = new SimpleBiMap<>();
    private static final Map<String, Translatable> instances = new HashMap<>();
    private static final ArrayList<Class<?>> classes = new ArrayList<>();

    static {
        scanForTranslatable();
    }

    public static void scanForTranslatable() {
        Reflections reflections = ReflectionProvider.getInstance();

        Set<Class<? extends Translatable>> classes = reflections.getSubTypesOf(Translatable.class);

        for (Class<? extends Translatable> clazz : classes) {
            if (clazz.isEnum()) {
                Translatable[] constants = clazz.getEnumConstants();
                setUpTranslations(constants);
            }
        }
    }

    public static <T extends Translatable> void setUpTranslations(T[] values) {
        classes.add(values[0].getClass());
        for (T value : values) {
            if (value instanceof HotkeyEvent event) {
                translations.put(event.getTranslationName(), Messages.getFormatted(event.getTranslationName()));
                instances.put(event.getTranslationName(), value);
            } else {
                translations.put(value.getName(), Messages.getFormatted(value.getName()));
                instances.put(value.getName(), value);
            }

        }

    }

    @SuppressWarnings("unchecked")
    public static <T extends Translatable> T getInstanceFromName(String name) {
        return (T) instances.get(name);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Translatable> T getInstanceFromTranslation(String translation) {
        String name = translations.getBackward(translation);
        return (T) instances.get(name);
    }

    public static SimpleBiMap<String, String> getTranslations() {
        return translations;
    }

    public static String getTranslation(String name) {
        String translation = translations.getForward(name);
        if (translation == null || translation.isBlank()) {

            return name;
        }
        return translation;
    }
}
