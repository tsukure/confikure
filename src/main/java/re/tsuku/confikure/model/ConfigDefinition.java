package re.tsuku.confikure.model;

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

    public Object instance() {
        return instance;
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

    public int version() {
        return version;
    }

    public List<ConfigCategory> categories() {
        return categories;
    }
}
