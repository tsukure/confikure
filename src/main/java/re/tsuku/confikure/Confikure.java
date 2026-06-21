package re.tsuku.confikure;

import re.tsuku.confikure.model.ConfigDefinition;

/**
 * entry point for scanning config objects.
 */
public final class Confikure {
    private Confikure() {
    }

    public static ConfigDefinition scan(Object config) {
        return new ConfigScanner().scan(config);
    }
}
