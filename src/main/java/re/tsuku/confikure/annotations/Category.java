package re.tsuku.confikure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * marks a field as a top-level category tab in the config gui.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Category {
    /**
     * label shown in the sidebar and header.
     */
    String name();

    /**
     * stable persisted id; generated from {@link #name()} when empty.
     */
    String id() default "";

    /**
     * category description kept in scanned metadata.
     */
    String description() default "";

    /**
     * sort position in the sidebar.
     */
    int order() default 0;
}
