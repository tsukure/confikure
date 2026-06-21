package re.tsuku.confikure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * renders an integer option as a keybind recorder.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Keybind {
    /**
     * whether the gui shows a clear button for this keybind.
     */
    boolean clearable() default true;

    /**
     * whether clearing restores the default value instead of setting zero.
     */
    boolean resetOnClear() default false;
}
