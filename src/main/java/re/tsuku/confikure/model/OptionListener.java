package re.tsuku.confikure.model;

/**
 * listener notified when a config option successfully changes value.
 */
public interface OptionListener {
    /**
     * called after the underlying config value changes.
     *
     * @param option
     *            changed option
     * @param oldValue
     *            previous value
     * @param newValue
     *            current value
     */
    void changed(ConfigOption option, Object oldValue, Object newValue);
}
