package re.tsuku.confikure;

import re.tsuku.confikure.model.ConfigDefinition;

/**
 * entry point for scanning config objects.
 */
public final class Confikure {
    private Confikure() {
    }

    /**
     * scans an annotated config object into a renderable and persistable definition.
     *
     * @param config
     *            root config object
     * @return scanned config metadata and option accessors
     */
    public static ConfigDefinition scan(Object config) {
        return new ConfigScanner().scan(config);
    }
}
