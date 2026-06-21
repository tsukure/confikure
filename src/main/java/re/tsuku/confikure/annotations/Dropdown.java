package re.tsuku.confikure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * renders an option as a pop-up list of choices.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dropdown {
    /**
     * allowed values; enum fields use their enum constants when empty.
     */
    String[] values() default {};
}
