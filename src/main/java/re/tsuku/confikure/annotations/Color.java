package re.tsuku.confikure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * renders an integer option as an argb color picker.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Color {
    /**
     * whether the picker and hex field expose the alpha channel.
     */
    boolean alpha() default true;
}
