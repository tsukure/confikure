package re.tsuku.confikure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * exposes a zero-argument method as a clickable config action.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Button {
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
     * text shown inside the clickable button.
     */
    String label() default "run";

    /**
     * group name or id that should contain the action.
     */
    String group() default "";

    /**
     * sort position inside the group.
     */
    int order() default 0;
}
