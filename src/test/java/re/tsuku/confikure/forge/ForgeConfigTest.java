package re.tsuku.confikure.forge;

import static org.junit.Assert.assertThrows;

import java.nio.file.Path;
import org.junit.Test;
import re.tsuku.confikure.ConfigFixtures;
import re.tsuku.confikure.Confikure;
import re.tsuku.confikure.model.ConfigDefinition;

public final class ForgeConfigTest {
    @Test
    public void rejectsNullScreenInputsAtCallSite() {
        ConfigFixtures.ExampleConfig config = new ConfigFixtures.ExampleConfig();

        assertThrows(NullPointerException.class, () -> ForgeConfig.screen(null));
        assertThrows(NullPointerException.class, () -> ForgeConfig.screen(config, (Path) null));
        assertThrows(NullPointerException.class, () -> ForgeConfig.gui(config, (Path) null, null));
        assertThrows(NullPointerException.class, () -> ForgeConfig.open(null));
        assertThrows(NullPointerException.class, () -> ForgeConfig.open(config, (Path) null));
        assertThrows(NullPointerException.class, () -> ForgeConfig.open((ForgeConfigScreen) null));
        assertThrows(NullPointerException.class, () -> ForgeConfig.openNow(null));
    }

    @Test
    public void rejectsNullEmbeddedScreenDependencies() {
        ConfigDefinition definition = Confikure.scan(new ConfigFixtures.ExampleConfig());

        assertThrows(NullPointerException.class, () -> new ForgeConfigGui(null, null, null));
        assertThrows(NullPointerException.class, () -> new ForgeConfigScreen(null));
        assertThrows(NullPointerException.class, () -> new ForgeConfigGui(definition, null, null).init(null));
    }
}
