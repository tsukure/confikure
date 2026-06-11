package re.tsuku.confikure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import re.tsuku.confikure.annotations.Category;
import re.tsuku.confikure.annotations.Config;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.EditorType;

public final class ConfikureTest {
    @Test
    public void scansAnnotatedConfig() {
        ExampleConfig config = new ExampleConfig();

        ConfigDefinition definition = Confikure.scan(config);
        ConfigOption option = definition.categories().get(0).options().get(0);

        assertEquals("example", definition.name());
        assertEquals("general", definition.categories().get(0).name());
        assertEquals("enabled", option.name());
        assertEquals(EditorType.BOOLEAN, option.type());
        assertTrue((Boolean) option.get());
    }

    @Config(name = "example")
    private static final class ExampleConfig {
        @Category(name = "general")
        private final General general = new General();
    }

    private static final class General {
        @Option(name = "enabled")
        private boolean enabled = true;
    }
}
