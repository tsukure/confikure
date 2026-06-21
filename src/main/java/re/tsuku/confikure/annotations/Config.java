package re.tsuku.confikure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * marks a root object that can be scanned into a config definition.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
    /**
     * display name for the config screen.
     */
    String name();

    /**
     * stable persisted id; generated from {@link #name()} when empty.
     */
    String id() default "";

    /**
     * short description kept in scanned metadata.
     */
    String description() default "";

    /**
     * config schema version written to persisted json.
     */
    int version() default 1;
}
