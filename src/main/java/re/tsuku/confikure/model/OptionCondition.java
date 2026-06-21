package re.tsuku.confikure.model;

/**
 * predicate used to compute dynamic option visibility or enabled state.
 */
public interface OptionCondition {
    /**
     * evaluates the condition.
     *
     * @return {@code true} when the condition passes
     */
    boolean test();
}
