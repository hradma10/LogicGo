package cz.logicgo.core.misc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PreloadCategory {
    String folder();

    boolean byName() default true;

    String[] extra() default {};
}
