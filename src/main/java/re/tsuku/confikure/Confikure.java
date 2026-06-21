package re.tsuku.confikure;

import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.fastbus.FastBus;

/**
 * entry point for scanning config objects.
 */
public final class Confikure {
    private static final FastBus EVENT_BUS = new FastBus();

    private Confikure() {
    }

    public static ConfigDefinition scan(Object config) {
        return new ConfigScanner().scan(config);
    }

    public static FastBus eventBus() {
        return EVENT_BUS;
    }
}
