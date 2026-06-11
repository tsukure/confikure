package re.tsuku.confikure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import re.tsuku.confikure.model.EditorType;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {
    String name();

    String id() default "";

    String description() default "";

    String group() default "";

    int order() default 0;

    EditorType type() default EditorType.AUTO;
}
