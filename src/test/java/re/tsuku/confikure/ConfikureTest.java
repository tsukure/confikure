package re.tsuku.confikure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import re.tsuku.confikure.annotations.Button;
import re.tsuku.confikure.annotations.Category;
import re.tsuku.confikure.annotations.Color;
import re.tsuku.confikure.annotations.Config;
import re.tsuku.confikure.annotations.Dropdown;
import re.tsuku.confikure.annotations.Group;
import re.tsuku.confikure.annotations.Info;
import re.tsuku.confikure.annotations.Keybind;
import re.tsuku.confikure.annotations.Multiline;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Range;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigGroup;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.EditorType;

public final class ConfikureTest {
    @Test
    public void scansGroupedConfig() {
        ExampleConfig config = new ExampleConfig();

        ConfigDefinition definition = Confikure.scan(config);
        ConfigGroup movement = definition.categories().get(0).groups().get(0);
        ConfigGroup visuals = definition.categories().get(1).groups().get(1);

        assertEquals("example", definition.id());
        assertEquals("movement", definition.categories().get(0).id());
        assertEquals("sprint", movement.id());
        assertEquals("enabled", movement.options().get(0).id());
        assertEquals(EditorType.BOOLEAN, movement.options().get(0).type());
        assertEquals("theme", visuals.id());
        assertEquals(EditorType.COLOR, visuals.options().get(0).type());
    }

    @Test
    public void supportsPropertyTypes() {
        ConfigDefinition definition = Confikure.scan(new ExampleConfig());
        List<ConfigOption> options = definition.categories().get(1).options();

        assertEquals(EditorType.KEYBIND, find(options, "open-gui").type());
        assertEquals(EditorType.DROPDOWN, find(options, "mode").type());
        assertEquals(EditorType.MULTILINE_TEXT, find(options, "notes").type());
        assertEquals(EditorType.INFO, find(options, "about").type());
        assertEquals(EditorType.DRAGGABLE_LIST, find(options, "order").type());
    }

    @Test
    public void coercesRangeAndTracksDirtyState() {
        ExampleConfig config = new ExampleConfig();
        ConfigOption speed = find(Confikure.scan(config).categories().get(0).options(), "speed");

        assertFalse(speed.dirty());
        speed.set(3.2D);

        assertEquals(2.0D, (Double) speed.get(), 0.0D);
        assertTrue(speed.dirty());

        speed.reset();
        assertEquals(1.0D, (Double) speed.get(), 0.0D);
    }

    @Test
    public void validatesDropdownsAndInvokesButtons() {
        ExampleConfig config = new ExampleConfig();
        ConfigDefinition definition = Confikure.scan(config);
        List<ConfigOption> options = definition.categories().get(1).options();
        ConfigOption mode = find(options, "mode");
        ConfigOption action = find(options, "reset-cache");

        assertEquals(Arrays.asList("simple", "fancy"), mode.choices());
        assertEquals("must be one of [simple, fancy]", mode.validate("missing"));

        action.press();
        assertTrue(config.visuals.reset);
    }

    private static ConfigOption find(List<ConfigOption> options, String id) {
        for (ConfigOption option : options) {
            if (option.id().equals(id)) {
                return option;
            }
        }
        throw new AssertionError(id);
    }

    @Config(name = "example")
    private static final class ExampleConfig {
        @Category(name = "visuals", order = 1)
        private final Visuals visuals = new Visuals();

        @Category(name = "movement", order = 0)
        private final Movement movement = new Movement();
    }

    private static final class Movement {
        @Group(name = "sprint")
        private final Sprint sprint = new Sprint();

        @Option(name = "speed", group = "sprint", order = 1)
        @Range(min = 0.0D, max = 2.0D, step = 0.5D)
        private double speed = 1.0D;
    }

    private static final class Sprint {
        @Option(name = "enabled")
        private boolean enabled = true;
    }

    private static final class Visuals {
        private boolean reset;

        @Option(name = "open gui")
        @Keybind
        private int openGui = 54;

        @Option(name = "mode")
        @Dropdown(values = {"simple", "fancy"})
        private String mode = "simple";

        @Option(name = "primary color", group = "theme")
        @Color
        private int primaryColor = 0xFF78A96B;

        @Option(name = "notes")
        @Multiline
        private String notes = "line one";

        @Option(name = "about")
        @Info
        private String about = "client settings";

        @Option(name = "order")
        private List<String> order = Arrays.asList("one", "two");

        @Button(name = "reset cache")
        private void resetCache() {
            reset = true;
        }
    }
}
