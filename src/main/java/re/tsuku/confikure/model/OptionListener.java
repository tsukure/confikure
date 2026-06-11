package re.tsuku.confikure.model;

public interface OptionListener {
    void changed(ConfigOption option, Object oldValue, Object newValue);
}
