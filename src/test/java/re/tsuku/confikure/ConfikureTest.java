package re.tsuku.confikure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import re.tsuku.confikure.annotations.Button;
import re.tsuku.confikure.annotations.Category;
import re.tsuku.confikure.annotations.Color;
import re.tsuku.confikure.annotations.Config;
import re.tsuku.confikure.annotations.Dropdown;
import re.tsuku.confikure.annotations.Group;
import re.tsuku.confikure.annotations.Info;
import re.tsuku.confikure.annotations.Keybind;
import re.tsuku.confikure.annotations.Mode;
import re.tsuku.confikure.annotations.Multiline;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Range;
import re.tsuku.confikure.gui.ConfigGui;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigGroup;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.EditorType;
import re.tsuku.confikure.persistence.ConfigStore;

public final class ConfikureTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

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
    public void persistsJsonByStableIds() throws Exception {
        ExampleConfig config = new ExampleConfig();
        ConfigDefinition definition = Confikure.scan(config);
        ConfigStore store = new ConfigStore();
        Path path = temporaryFolder.newFile("example.json").toPath();

        config.movement.speed = 1.5D;
        config.visuals.mode = "fancy";
        config.visuals.order = Arrays.asList("two", "one");
        store.save(definition, path);

        ExampleConfig loaded = new ExampleConfig();
        store.load(Confikure.scan(loaded), path);

        assertEquals(1.5D, loaded.movement.speed, 0.0D);
        assertEquals("fancy", loaded.visuals.mode);
        assertEquals(Arrays.asList("two", "one"), loaded.visuals.order);
    }

    @Test
    public void loadSkipsMismatchedConfigId() throws Exception {
        ConfigStore store = new ConfigStore();
        Path path = temporaryFolder.newFile("wrong.json").toPath();
        Files.write(path, Arrays.asList("{", "  \"config\": \"other\",", "  \"categories\": {}", "}"),
                StandardCharsets.UTF_8);

        ExampleConfig config = new ExampleConfig();

        assertFalse(store.load(Confikure.scan(config), path));
        assertEquals(1.0D, config.movement.speed, 0.0D);
    }

    @Test
    public void loadKeepsDefaultsForInvalidValues() throws Exception {
        ConfigStore store = new ConfigStore();
        Path path = temporaryFolder.newFile("invalid.json").toPath();
        Files.write(path,
                Arrays.asList("{", "  \"config\": \"example\",", "  \"categories\": {",
                        "    \"visuals\": {\"general\": {\"mode\": \"missing\"}},",
                        "    \"movement\": {\"sprint\": {\"speed\": \"fast\"}}", "  }", "}"),
                StandardCharsets.UTF_8);

        ExampleConfig config = new ExampleConfig();

        assertTrue(store.load(Confikure.scan(config), path));
        assertEquals("simple", config.visuals.mode);
        assertEquals(1.0D, config.movement.speed, 0.0D);
    }

    @Test
    public void loadReturnsFalseForCorruptJson() throws Exception {
        ConfigStore store = new ConfigStore();
        Path path = temporaryFolder.newFile("corrupt.json").toPath();
        Files.write(path, Arrays.asList("{nope"), StandardCharsets.UTF_8);

        assertFalse(store.load(Confikure.scan(new ExampleConfig()), path));
    }

    @Test
    public void loadSkipsNullPrimitiveValues() throws Exception {
        ConfigStore store = new ConfigStore();
        Path path = temporaryFolder.newFile("null-primitive.json").toPath();
        Files.write(path,
                Arrays.asList("{", "  \"config\": \"example\",",
                        "  \"categories\": {\"movement\": {\"sprint\": {\"speed\": null}}}", "}"),
                StandardCharsets.UTF_8);

        ExampleConfig config = new ExampleConfig();

        assertTrue(store.load(Confikure.scan(config), path));
        assertEquals(1.0D, config.movement.speed, 0.0D);
    }

    @Test
    public void rangeStepsAreAnchoredToMinimum() {
        OffsetRangeConfig config = new OffsetRangeConfig();
        ConfigOption value = Confikure.scan(config).option("offset");

        value.set(0.24D);
        assertEquals(0.3D, config.general.offset, 0.0000001D);

        value.set(0.19D);
        assertEquals(0.1D, config.general.offset, 0.0000001D);
    }

    @Test
    public void numericFieldsCoerceWithoutRange() {
        NumericConfig config = new NumericConfig();
        ConfigDefinition definition = Confikure.scan(config);

        definition.option("int-value").set(2.8D);
        definition.option("long-value").set(3.2D);
        definition.option("float-value").set(1.25D);
        definition.option("double-value").set(2);

        assertEquals(3, config.general.intValue);
        assertEquals(3L, config.general.longValue);
        assertEquals(1.25F, config.general.floatValue, 0.0F);
        assertEquals(2.0D, config.general.doubleValue, 0.0D);
    }

    @Test
    public void mutableListDefaultsAreSnapshotted() {
        MutableDefaultConfig config = new MutableDefaultConfig();
        ConfigOption list = Confikure.scan(config).option("entries");

        config.general.entries.add("two");

        assertTrue(list.dirty());
        list.reset();
        assertEquals(Arrays.asList("one"), config.general.entries);
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

    @Test
    public void guiCanEditInteractiveTypes() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        gui.click(800, 600, 690, 285);
        gui.keyTyped('!', 0);
        assertEquals("line one!", config.visuals.notes);

        gui.click(800, 600, 640, 157);
        gui.keyTyped('\0', 35);
        assertEquals(35, config.visuals.openGui);

        gui.click(800, 600, 690, 381);
        assertEquals(Arrays.asList("two", "one"), config.visuals.order);

        gui.click(800, 600, 670, 490);
        gui.click(800, 600, 600, 474);
        gui.keyTyped('\0', 30, false, true);
        for (char character : "112233".toCharArray()) {
            gui.keyTyped(character, 0);
        }
        assertEquals(0xFF112233, config.visuals.primaryColor);
    }

    @Test
    public void guiSeparatesDropdownsFromModeCycling() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        gui.click(800, 600, 690, 193);
        assertEquals("simple", config.visuals.mode);
        gui.click(800, 600, 690, 230);
        assertEquals("fancy", config.visuals.mode);

        assertEquals("one", config.visuals.cycleMode);
        gui.click(800, 600, 690, 239);
        assertEquals("two", config.visuals.cycleMode);
        gui.click(800, 600, 690, 239);
        assertEquals("one", config.visuals.cycleMode);
        gui.click(800, 600, 560, 239);
        assertEquals("two", config.visuals.cycleMode);
    }

    @Test
    public void guiOnlyInteractsWithComponents() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        gui.click(800, 600, 300, 193);
        assertEquals("simple", config.visuals.mode);

        gui.click(800, 600, 690, 193);
        gui.click(800, 600, 690, 230);
        assertEquals("fancy", config.visuals.mode);
    }

    @Test
    public void guiDragsSlidersAndEditsTextSelection() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(0);
        gui.click(800, 600, 584, 193);
        gui.drag(800, 600, 694, 193);
        gui.release();
        assertEquals(2.0D, config.movement.speed, 0.0D);

        gui.selectedCategory(1);
        gui.click(800, 600, 690, 285);
        gui.keyTyped('\0', 30, false, true);
        gui.keyTyped('x', 0);
        assertEquals("x", config.visuals.notes);

        gui.selectedCategory(0);
        gui.click(800, 600, 690, 193);
        gui.keyTyped('\0', 30, false, true);
        gui.keyTyped('1', 0);
        gui.keyTyped('.', 0);
        gui.keyTyped('5', 0);
        assertEquals(1.5D, config.movement.speed, 0.0D);
    }

    @Test
    public void focusedComponentsConsumeKeyboardCommitAndCancel() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        assertFalse(gui.keyTyped('\0', 1));

        gui.selectedCategory(1);
        gui.click(800, 600, 690, 285);
        gui.keyTyped('\0', 30, false, true);
        gui.keyTyped('x', 0);
        assertTrue(gui.keyTyped('\0', 28));
        assertEquals("x", config.visuals.notes);
        assertFalse(gui.keyTyped('\0', 1));

        gui.click(800, 600, 690, 490);
        assertTrue(gui.keyTyped('\0', 1));
        assertFalse(gui.keyTyped('\0', 1));
    }

    @Test
    public void keybindClearButtonClearsOrResets() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        gui.click(800, 600, 690, 157);
        assertEquals(0, config.visuals.openGui);
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

        @Option(name = "open gui", order = 0)
        @Keybind
        private int openGui = 54;

        @Option(name = "mode", order = 1)
        @Dropdown(values = {"simple", "fancy"})
        private String mode = "simple";

        @Option(name = "cycle mode", order = 2)
        @Mode(values = {"one", "two"})
        private String cycleMode = "one";

        @Option(name = "primary color", group = "theme", order = 0)
        @Color
        private int primaryColor = 0xFF78A96B;

        @Option(name = "notes", order = 3)
        @Multiline
        private String notes = "line one";

        @Option(name = "about", order = 4)
        @Info
        private String about = "client settings";

        @Option(name = "order", order = 5)
        private List<String> order = Arrays.asList("one", "two");

        @Button(name = "reset cache", order = 6)
        private void resetCache() {
            reset = true;
        }
    }

    @Config(name = "keybind policy")
    private static final class KeybindPolicyConfig {
        @Category(name = "general")
        private final Keybinds keybinds = new Keybinds();
    }

    private static final class Keybinds {
        @Option(name = "locked key")
        @Keybind(clearable = false)
        private int lockedKey = 34;

        @Option(name = "reset key")
        @Keybind(resetOnClear = true)
        private int resetKey = 54;
    }

    @Config(name = "offset range")
    private static final class OffsetRangeConfig {
        @Category(name = "general")
        private final OffsetRange general = new OffsetRange();
    }

    private static final class OffsetRange {
        @Option(name = "offset")
        @Range(min = 0.1D, max = 1.0D, step = 0.2D)
        private double offset = 0.1D;
    }

    @Config(name = "numeric")
    private static final class NumericConfig {
        @Category(name = "general")
        private final Numeric general = new Numeric();
    }

    private static final class Numeric {
        @Option(name = "int value")
        private int intValue = 1;

        @Option(name = "long value")
        private long longValue = 1L;

        @Option(name = "float value")
        private float floatValue = 1.0F;

        @Option(name = "double value")
        private double doubleValue = 1.0D;
    }

    @Config(name = "mutable default")
    private static final class MutableDefaultConfig {
        @Category(name = "general")
        private final MutableDefault general = new MutableDefault();
    }

    private static final class MutableDefault {
        @Option(name = "entries")
        private List<String> entries = new ArrayList<String>(Arrays.asList("one"));
    }

    @Config(name = "duplicate category")
    private static final class DuplicateCategoryConfig {
        @Category(name = "general")
        private final Object first = new Empty();

        @Category(name = "general!")
        private final Object second = new Empty();
    }

    @Config(name = "duplicate group")
    private static final class DuplicateGroupConfig {
        @Category(name = "general")
        private final DuplicateGroups general = new DuplicateGroups();
    }

    private static final class DuplicateGroups {
        @Group(name = "feature")
        private final SingleOption first = new SingleOption();

        @Group(name = "feature!")
        private final SingleOption second = new SingleOption();
    }

    @Config(name = "duplicate option")
    private static final class DuplicateOptionConfig {
        @Category(name = "general")
        private final DuplicateOptions general = new DuplicateOptions();
    }

    private static final class DuplicateOptions {
        @Option(name = "enabled")
        private boolean first = true;

        @Option(name = "enabled!")
        private boolean second = false;
    }

    private static final class SingleOption {
        @Option(name = "enabled")
        private boolean enabled = true;
    }

    private static final class Empty {
        @Option(name = "enabled")
        private boolean enabled = true;
    }
}
