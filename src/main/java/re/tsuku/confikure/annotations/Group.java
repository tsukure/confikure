package re.tsuku.confikure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * marks a nested object as a collapsible group inside a category.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Group {
    /**
     * label shown in the group header.
     */
    String name();

    /**
     * stable group id; generated from {@link #name()} when empty.
     */
    String id() default "";

    /**
     * group description kept in scanned metadata.
     */
    String description() default "";

    /**
     * sort position inside the category.
     */
    int order() default 0;
}
