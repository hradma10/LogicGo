package cz.logicgo.core.misc.enums.hints;


import cz.logicgo.core.misc.interfaces.Translatable;

public enum ShikakuHintType implements Translatable {
    CHECK_VALIDITY("shikaku.hint_type.check_validity"),
    INSERT_RECTANGLE_TO_NUMBER("shikaku.hint_type.insert_rectangle_number"),
    INSERT_RANDOM_RECTANGLE("shikaku.hint_type.insert_random_rectangle"),

    ;

    final String name;

    ShikakuHintType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
