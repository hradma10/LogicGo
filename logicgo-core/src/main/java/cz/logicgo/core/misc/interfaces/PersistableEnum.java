package cz.logicgo.core.misc.interfaces;

public interface PersistableEnum {
    static <E extends Enum<E> & PersistableEnum> E fromId(int id, Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.getId() == id) return e;
        }
        throw new IllegalArgumentException("Unknown ID " + id + " for enum " + enumClass.getSimpleName());
    }

    int getId();
}
