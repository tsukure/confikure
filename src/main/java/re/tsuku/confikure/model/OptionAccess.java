package re.tsuku.confikure.model;

/**
 * adapter used by a config option to read and write an underlying value.
 */
public interface OptionAccess {
    /**
     * returns the java type accepted by this access adapter.
     */
    Class<?> valueType();

    /**
     * reads the current value.
     */
    Object get();

    /**
     * writes a new value.
     *
     * @param value
     *            new value
     */
    void set(Object value);
}
