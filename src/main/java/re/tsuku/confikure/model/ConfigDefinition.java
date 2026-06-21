package re.tsuku.confikure.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * scanned metadata for one config root object.
 */
public final class ConfigDefinition {
    private final Object instance;
    private final String id;
    private final String name;
    private final String description;
    private final int version;
    private final List<ConfigCategory> categories;

    public ConfigDefinition(Object instance, String id, String name, String description, int version,
            List<ConfigCategory> categories) {
        this.instance = instance;
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.categories = categories;
    }

    /**
     * returns the root config object that was scanned.
     */
    public Object instance() {
        return instance;
    }

    /**
     * returns the stable config id used by persistence.
     */
    public String id() {
        return id;
    }

    /**
     * returns the display name for the config.
     */
    public String name() {
        return name;
    }

    /**
     * returns the config description.
     */
    public String description() {
        return description;
    }

    /**
     * returns the schema version written to persisted json.
     */
    public int version() {
        return version;
    }

    /**
     * returns the scanned category tabs in display order.
     */
    public List<ConfigCategory> categories() {
        return categories;
    }

    /**
     * returns every option from every category in display order.
     */
    public List<ConfigOption> options() {
        List<ConfigOption> options = new ArrayList<>();
        for (ConfigCategory category : categories) {
            options.addAll(category.options());
        }
        return Collections.unmodifiableList(options);
    }

    /**
     * finds the first option with the given stable id.
     *
     * @param id
     *            option id
     * @return matching option, or {@code null} when none exists
     */
    public ConfigOption option(String id) {
        for (ConfigOption option : options()) {
            if (option.id().equals(id)) {
                return option;
            }
        }
        return null;
    }
}
