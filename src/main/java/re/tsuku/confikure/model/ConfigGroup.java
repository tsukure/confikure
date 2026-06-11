package re.tsuku.confikure.model;

import java.util.List;

/**
 * visual section inside a category tab.
 */
public final class ConfigGroup {
    private final String id;
    private final String name;
    private final String description;
    private final int order;
    private final List<ConfigOption> options;

    public ConfigGroup(String id, String name, String description, int order, List<ConfigOption> options) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.order = order;
        this.options = options;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public int order() {
        return order;
    }

    public List<ConfigOption> options() {
        return options;
    }
}
