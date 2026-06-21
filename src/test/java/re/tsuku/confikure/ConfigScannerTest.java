package re.tsuku.confikure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static re.tsuku.confikure.ConfigFixtures.find;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import re.tsuku.confikure.ConfigFixtures.ColorAlphaConfig;
import re.tsuku.confikure.ConfigFixtures.DuplicateCategoryConfig;
import re.tsuku.confikure.ConfigFixtures.DuplicateGroupConfig;
import re.tsuku.confikure.ConfigFixtures.DuplicateOptionConfig;
import re.tsuku.confikure.ConfigFixtures.ExampleConfig;
import re.tsuku.confikure.ConfigFixtures.KeybindPolicyConfig;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigGroup;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.EditorType;

public final class ConfigScannerTest {
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
        assertTrue(find(options, "open-gui").keybindClearable());
        assertFalse(find(options, "open-gui").keybindResetOnClear());
        assertEquals(EditorType.DROPDOWN, find(options, "mode").type());
        assertEquals(EditorType.MODE, find(options, "cycle-mode").type());
        assertEquals(EditorType.MULTILINE_TEXT, find(options, "notes").type());
        assertEquals(EditorType.INFO, find(options, "about").type());
        assertEquals(EditorType.DRAGGABLE_LIST, find(options, "order").type());
    }

    @Test
    public void scansKeybindClearPolicies() {
        ConfigDefinition definition = Confikure.scan(new KeybindPolicyConfig());
        List<ConfigOption> options = definition.categories().get(0).options();

        assertFalse(find(options, "locked-key").keybindClearable());
        assertTrue(find(options, "reset-key").keybindClearable());
        assertTrue(find(options, "reset-key").keybindResetOnClear());
    }

    @Test
    public void scansColorAlphaPolicy() {
        ConfigDefinition definition = Confikure.scan(new ColorAlphaConfig());
        List<ConfigOption> options = definition.categories().get(0).options();

        assertTrue(find(options, "with-alpha").colorAlpha());
        assertFalse(find(options, "without-alpha").colorAlpha());
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

    @Test
    public void supportsDependencyPredicates() {
        ExampleConfig config = new ExampleConfig();
        ConfigDefinition definition = Confikure.scan(config);
        ConfigOption mode = find(definition.categories().get(1).options(), "mode");
        ConfigOption notes = find(definition.categories().get(1).options(), "notes");

        mode.enabledWhen(new re.tsuku.confikure.model.OptionCondition() {
            public boolean test() {
                return !config.visuals.notes.equals("locked");
            }
        });
        notes.visibleWhen(new re.tsuku.confikure.model.OptionCondition() {
            public boolean test() {
                return config.visuals.primaryColor != 0;
            }
        });

        assertTrue(mode.enabled());
        assertTrue(notes.visible());

        config.visuals.notes = "locked";
        config.visuals.primaryColor = 0;

        assertFalse(mode.enabled());
        assertFalse(notes.visible());
    }

    @Test
    public void duplicateCategoryIdsFailScan() {
        try {
            Confikure.scan(new DuplicateCategoryConfig());
            fail("expected duplicate category id to fail");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("duplicate category id"));
        }
    }

    @Test
    public void duplicateGroupIdsFailScan() {
        try {
            Confikure.scan(new DuplicateGroupConfig());
            fail("expected duplicate group id to fail");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("duplicate group id"));
        }
    }

    @Test
    public void duplicateOptionIdsFailScan() {
        try {
            Confikure.scan(new DuplicateOptionConfig());
            fail("expected duplicate option id to fail");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("duplicate option id"));
        }
    }
}
