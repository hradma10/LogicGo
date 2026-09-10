package cz.logicgo.core.misc.interfaces;

public interface Parseable {
    static <T extends Enum<T> & Parseable> T parse(Class<T> enumClass, String value) {
        for (T constant : enumClass.getEnumConstants()) {
            if (constant.matches(value)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("No matching for value: " + value);
    }

    boolean matches(String input);
}
