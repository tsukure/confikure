package re.tsuku.confikure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * renders a string option as a larger text field.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Multiline {
    /**
     * requested visible line count; kept as annotation metadata by the current scanner.
     */
    int lines() default 3;
}
