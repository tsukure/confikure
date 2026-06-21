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

    /**
     * returns the stable category id.
     */
    public String id() {
        return id;
    }

    /**
     * returns the display name for the category tab.
     */
    public String name() {
        return name;
    }

    /**
     * returns the category description.
     */
    public String description() {
        return description;
    }

    /**
     * returns the configured sort position.
     */
    public int order() {
        return order;
    }

    /**
     * returns the scanned category object.
     */
    public Object instance() {
        return instance;
    }

    /**
     * returns the groups in display order.
     */
    public List<ConfigGroup> groups() {
        return groups;
    }

    /**
     * returns every option in this category.
     */
    public List<ConfigOption> options() {
        List<ConfigOption> options = new ArrayList<>();
        for (ConfigGroup group : groups) {
            options.addAll(group.options());
        }
        return Collections.unmodifiableList(options);
    }
}
