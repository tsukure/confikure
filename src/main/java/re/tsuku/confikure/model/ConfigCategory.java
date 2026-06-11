package re.tsuku.confikure.model;

import java.util.List;

public final class ConfigCategory {
    private final String name;
    private final String description;
    private final Object instance;
    private final List<ConfigOption> options;

    public ConfigCategory(String name, String description, Object instance, List<ConfigOption> options) {
        this.name = name;
        this.description = description;
        this.instance = instance;
        this.options = options;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Object instance() {
        return instance;
    }

    public List<ConfigOption> options() {
        return options;
    }
}
