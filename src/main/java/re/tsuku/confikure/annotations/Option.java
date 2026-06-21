package re.tsuku.confikure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import re.tsuku.confikure.model.EditorType;

/**
 * exposes a field or method as a config row.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {
    /**
     * label shown in the gui.
     */
    String name();

    /**
     * stable persisted id; generated from {@link #name()} when empty.
     */
    String id() default "";

    /**
     * short helper text shown under the label.
     */
    String description() default "";

    /**
     * group name or id that should contain the row.
     */
    String group() default "";

    /**
     * sort position inside the group.
     */
    int order() default 0;

    /**
     * explicit editor type; {@link EditorType#AUTO} infers from the field type and companion annotations.
     */
    EditorType type() default EditorType.AUTO;
}
