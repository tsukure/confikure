package re.tsuku.confikure.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * left-side tab in the config gui.
 */
public final class ConfigCategory {
    private final String id;
    private final String name;
    private final String description;
    private final int order;
    private final Object instance;
    private final List<ConfigGroup> groups;

    public ConfigCategory(String id, String name, String description, int order, Object instance,
            List<ConfigGroup> groups) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.order = order;
        this.instance = instance;
        this.groups = groups;
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

    public Object instance() {
        return instance;
    }

    public List<ConfigGroup> groups() {
        return groups;
    }

    public List<ConfigOption> options() {
        List<ConfigOption> options = new ArrayList<>();
        for (ConfigGroup group : groups) {
            options.addAll(group.options());
        }
        return Collections.unmodifiableList(options);
    }
}
