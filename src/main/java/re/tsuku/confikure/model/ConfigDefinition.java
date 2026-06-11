package re.tsuku.confikure.model;

import java.util.List;

public final class ConfigDefinition {
    private final Object instance;
    private final String name;
    private final String description;
    private final List<ConfigCategory> categories;

    public ConfigDefinition(Object instance, String name, String description, List<ConfigCategory> categories) {
        this.instance = instance;
        this.name = name;
        this.description = description;
        this.categories = categories;
    }

    public Object instance() {
        return instance;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public List<ConfigCategory> categories() {
        return categories;
    }
}
