package cz.logicgo.core.misc;

import org.reflections.Reflections;

public class ReflectionProvider {
    private static final Reflections reflections = new Reflections("cz.logicgo.core");

    public static Reflections getInstance() {
        return reflections;
    }
}
