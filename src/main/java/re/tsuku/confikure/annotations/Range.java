package re.tsuku.confikure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * constrains a numeric option and renders it as a slider.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Range {
    /**
     * minimum allowed value.
     */
    double min() default Double.NEGATIVE_INFINITY;

    /**
     * maximum allowed value.
     */
    double max() default Double.POSITIVE_INFINITY;

    /**
     * snap increment; zero disables snapping.
     */
    double step() default 0.0D;
}
