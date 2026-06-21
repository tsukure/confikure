package re.tsuku.confikure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static re.tsuku.confikure.ConfigFixtures.find;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import re.tsuku.confikure.ConfigFixtures.ChoiceConfig;
import re.tsuku.confikure.ConfigFixtures.ColorAlphaConfig;
import re.tsuku.confikure.ConfigFixtures.DefaultButtonLabelConfig;
import re.tsuku.confikure.ConfigFixtures.DuplicateCategoryConfig;
import re.tsuku.confikure.ConfigFixtures.DuplicateGroupConfig;
import re.tsuku.confikure.ConfigFixtures.DuplicateOptionConfig;
import re.tsuku.confikure.ConfigFixtures.ExampleConfig;
import re.tsuku.confikure.ConfigFixtures.InvalidButtonConfig;
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
        assertEquals(EditorType.TEXT, find(options, "label").type());
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
    public void infersEnumChoicesAndSearchTags() {
        ConfigDefinition definition = Confikure.scan(new ChoiceConfig());
        List<ConfigOption> options = definition.categories().get(0).options();

        assertEquals(EditorType.DROPDOWN, find(options, "enum-mode").type());
        assertEquals(Arrays.asList("FIRST", "SECOND"), find(options, "enum-mode").choices());
        assertEquals(Arrays.asList("alias", "lookup"), find(options, "tagged-value").searchTags());
    }

    @Test
    public void stableIdsCollapsePunctuationAndFallbackForBlankNames() {
        assertEquals("fancy-option-2", ConfigScanner.stableId("  Fancy Option #2!! "));
        assertEquals("option", ConfigScanner.stableId("!!!"));
        assertEquals("option", ConfigScanner.stableId(null));
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
        assertEquals("reset", action.buttonLabel());

        action.press();
        assertTrue(config.visuals.reset);
    }

    @Test
    public void buttonsDefaultToRunLabel() {
        ConfigOption action = Confikure.scan(new DefaultButtonLabelConfig()).option("refresh");

        assertEquals("run", action.buttonLabel());
    }

    @Test
    public void supportsDependencyPredicates() {
        ExampleConfig config = new ExampleConfig();
        ConfigDefinition definition = Confikure.scan(config);
        ConfigOption mode = find(definition.categories().get(1).options(), "mode");
        ConfigOption notes = find(definition.categories().get(1).options(), "notes");

        mode.enabledWhen(() -> !config.visuals.notes.equals("locked"));
        notes.visibleWhen(() -> config.visuals.primaryColor != 0);

        assertTrue(mode.enabled());
        assertTrue(notes.visible());

        config.visuals.notes = "locked";
        config.visuals.primaryColor = 0;

        assertFalse(mode.enabled());
        assertFalse(notes.visible());
    }

    @Test
    public void duplicateCategoryIdsFailScan() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Confikure.scan(new DuplicateCategoryConfig()));
        assertTrue(exception.getMessage().contains("duplicate category id"));
    }

    @Test
    public void duplicateGroupIdsFailScan() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Confikure.scan(new DuplicateGroupConfig()));
        assertTrue(exception.getMessage().contains("duplicate group id"));
    }

    @Test
    public void duplicateOptionIdsFailScan() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Confikure.scan(new DuplicateOptionConfig()));
        assertTrue(exception.getMessage().contains("duplicate option id"));
    }

    @Test
    public void buttonMethodsCannotHaveParameters() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Confikure.scan(new InvalidButtonConfig()));
        assertTrue(exception.getMessage().contains("@Button methods cannot have parameters"));
    }
}
