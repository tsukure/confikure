package re.tsuku.confikure.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;
import re.tsuku.confikure.ConfigFixtures.ColorAlphaConfig;
import re.tsuku.confikure.ConfigFixtures.ExampleConfig;
import re.tsuku.confikure.ConfigFixtures.OffsetRangeConfig;
import re.tsuku.confikure.Confikure;
import re.tsuku.confikure.gui.layout.ControlLayout;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.OptionCondition;

public final class ConfigGuiTest {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    @Test
    public void guiCanEditInteractiveTypes() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        clickControl(gui, "notes");
        gui.keyTyped('!', 0);
        assertEquals("line one!", config.visuals.notes);

        clickControl(gui, "open-gui");
        gui.keyTyped('\0', 35);
        assertEquals(35, config.visuals.openGui);

        clickControl(gui, "order");
        assertEquals(Arrays.asList("two", "one"), config.visuals.order);

        clickControl(gui, "primary-color");
        clickHexField(gui, "primary-color");
        gui.keyTyped('\0', 30, false, true);
        for (char character : "112233".toCharArray()) {
            gui.keyTyped(character, 0);
        }
        assertEquals(0xFF112233, config.visuals.primaryColor);
        gui.keyTyped('\0', 30, false, true);
        for (char character : "#44556680".toCharArray()) {
            gui.keyTyped(character, 0);
        }
        assertEquals(0x80445566, config.visuals.primaryColor);
    }

    @Test
    public void guiSeparatesDropdownsFromModeCycling() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        clickControl(gui, "mode");
        assertEquals("simple", config.visuals.mode);
        clickDropdownChoice(gui, "mode", 1);
        assertEquals("fancy", config.visuals.mode);

        assertEquals("one", config.visuals.cycleMode);
        clickMode(gui, "cycle-mode", 1);
        assertEquals("two", config.visuals.cycleMode);
        clickMode(gui, "cycle-mode", 1);
        assertEquals("one", config.visuals.cycleMode);
        clickMode(gui, "cycle-mode", -1);
        assertEquals("two", config.visuals.cycleMode);
    }

    @Test
    public void guiOnlyInteractsWithComponents() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        clickRowLabel(gui, "mode");
        assertEquals("simple", config.visuals.mode);

        clickControl(gui, "mode");
        clickDropdownChoice(gui, "mode", 1);
        assertEquals("fancy", config.visuals.mode);
    }

    @Test
    public void guiDragsSlidersAndEditsTextSelection() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(0);
        clickSlider(gui, "speed", 0.0D);
        assertEquals(0.0D, config.movement.speed, 0.0D);

        clickSlider(gui, "speed", 0.5D);
        dragSlider(gui, "speed", 1.0D);
        gui.release();
        assertEquals(2.0D, config.movement.speed, 0.0D);

        gui.selectedCategory(1);
        clickControl(gui, "notes");
        gui.keyTyped('\0', 30, false, true);
        gui.keyTyped('x', 0);
        assertEquals("x", config.visuals.notes);

        gui.selectedCategory(0);
        clickNumberField(gui, "speed");
        gui.keyTyped('\0', 30, false, true);
        gui.keyTyped('1', 0);
        gui.keyTyped('.', 0);
        gui.keyTyped('5', 0);
        assertEquals(1.5D, config.movement.speed, 0.0D);
    }

    @Test
    public void sliderDisplayHidesFloatingPointArtifacts() {
        OffsetRangeConfig config = new OffsetRangeConfig();
        ConfigDefinition definition = Confikure.scan(config);
        ConfigOption option = definition.option("offset");
        ConfigGui gui = new ConfigGui(definition);

        option.set(0.7000000000000001D);

        assertEquals(0.7D, config.general.offset, 0.000000000000001D);
        assertEquals("0.7", gui.displayValue(option));
    }

    @Test
    public void focusedNumberFieldDoesNotBlockSliderDragging() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(0);
        clickNumberField(gui, "speed");
        clickSlider(gui, "speed", 0.5D);
        dragSlider(gui, "speed", 1.0D);
        gui.release();

        assertEquals(2.0D, config.movement.speed, 0.0D);
        assertFalse(gui.keyTyped('\0', 28));
    }

    @Test
    public void colorDisplayUsesHexText() {
        ExampleConfig config = new ExampleConfig();
        ConfigDefinition definition = Confikure.scan(config);
        ConfigOption option = definition.option("primary-color");
        ConfigGui gui = new ConfigGui(definition);

        assertEquals("#78A96BFF", gui.displayValue(option));

        option.set(0x80112233);

        assertEquals("#11223380", gui.displayValue(option));
    }

    @Test
    public void nonAlphaColorDisplayAndInputUseRgbHex() {
        ColorAlphaConfig config = new ColorAlphaConfig();
        ConfigDefinition definition = Confikure.scan(config);
        ConfigOption option = definition.option("without-alpha");
        ConfigGui gui = new ConfigGui(definition);

        assertEquals("#78A96B", gui.displayValue(option));

        clickControl(gui, "without-alpha");
        clickHexField(gui, "without-alpha");
        gui.keyTyped('\0', 30, false, true);
        for (char character : "#11223380".toCharArray()) {
            gui.keyTyped(character, 0);
        }

        assertEquals(0xFF112233, config.general.withoutAlpha);
        assertEquals("#112233", gui.displayValue(option));
    }

    @Test
    public void invalidColorTextKeepsPreviousValue() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        clickControl(gui, "primary-color");
        clickHexField(gui, "primary-color");
        gui.keyTyped('\0', 30, false, true);
        for (char character : "#NOPE".toCharArray()) {
            gui.keyTyped(character, 0);
        }
        gui.keyTyped('\0', 28);

        assertEquals(0xFF78A96B, config.visuals.primaryColor);
    }

    @Test
    public void staleDisabledPopupsAndDragsDoNotMutateOptions() {
        ExampleConfig config = new ExampleConfig();
        ConfigDefinition definition = Confikure.scan(config);
        ConfigGui gui = new ConfigGui(definition);

        gui.selectedCategory(1);
        ConfigOption dropdown = definition.option("mode");
        clickControl(gui, "mode");
        dropdown.enabledWhen(never());
        clickDropdownChoice(gui, "mode", 1);
        assertEquals("simple", config.visuals.mode);

        ConfigOption color = definition.option("primary-color");
        clickControl(gui, "primary-color");
        color.enabledWhen(never());
        GuiBounds hex = gui.colorHexBounds(WIDTH, HEIGHT, color);
        gui.click(WIDTH, HEIGHT, hex.x - 20, hex.y - 80);
        gui.drag(WIDTH, HEIGHT, hex.x, hex.y - 60);
        gui.release();
        assertEquals(0xFF78A96B, config.visuals.primaryColor);

        gui.selectedCategory(0);
        ConfigOption speed = definition.option("speed");
        clickSlider(gui, "speed", 0.5D);
        double afterClick = config.movement.speed;
        speed.enabledWhen(never());
        dragSlider(gui, "speed", 1.0D);
        gui.release();
        assertEquals(afterClick, config.movement.speed, 0.0D);
    }

    @Test
    public void focusedComponentsConsumeKeyboardCommitAndCancel() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        assertFalse(gui.keyTyped('\0', 1));

        gui.selectedCategory(1);
        clickControl(gui, "notes");
        gui.keyTyped('\0', 30, false, true);
        gui.keyTyped('x', 0);
        assertTrue(gui.keyTyped('\0', 28));
        assertEquals("x", config.visuals.notes);
        assertFalse(gui.keyTyped('\0', 1));

        clickControl(gui, "primary-color");
        assertTrue(gui.keyTyped('\0', 1));
        assertFalse(gui.keyTyped('\0', 1));
    }

    @Test
    public void keybindClearButtonClearsOrResets() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        clickKeybindClear(gui, "open-gui");
        assertEquals(0, config.visuals.openGui);
    }

    @Test
    public void guiStateRemembersSelectedCategoryAndCollapsedGroups() {
        ConfigGui gui = new ConfigGui(Confikure.scan(new ExampleConfig()));
        gui.selectedCategory("visuals");
        gui.groupCollapsed("visuals", "theme", true);

        ConfigGuiState state = gui.state();
        ConfigGui restored = new ConfigGui(gui.definition());
        restored.state(state);

        assertEquals("visuals", restored.selectedCategoryId());
        assertTrue(restored.groupCollapsed("visuals", "theme"));
        assertFalse(restored.groupCollapsed("movement", "sprint"));
    }

    private static void clickControl(ConfigGui gui, String optionId) {
        click(gui, gui.controlBounds(WIDTH, HEIGHT, option(gui, optionId)));
    }

    private static void clickRowLabel(ConfigGui gui, String optionId) {
        GuiBounds row = gui.optionBounds(WIDTH, HEIGHT, option(gui, optionId));
        gui.click(WIDTH, HEIGHT, row.x + 16, centerY(row));
    }

    private static void clickDropdownChoice(ConfigGui gui, String optionId, int index) {
        GuiBounds row = gui.optionBounds(WIDTH, HEIGHT, option(gui, optionId));
        GuiBounds control = ControlLayout.editor(row);
        int y = row.y + row.height - 7 + index * 18 + 9;
        gui.click(WIDTH, HEIGHT, centerX(control), y);
    }

    private static void clickMode(ConfigGui gui, String optionId, int direction) {
        GuiBounds mode = ControlLayout.mode(gui.optionBounds(WIDTH, HEIGHT, option(gui, optionId)));
        int x = direction < 0 ? mode.x + 8 : mode.x + mode.width - 8;
        gui.click(WIDTH, HEIGHT, x, centerY(mode));
    }

    private static void clickSlider(ConfigGui gui, String optionId, double progress) {
        GuiBounds track = ControlLayout.sliderTrack(gui.optionBounds(WIDTH, HEIGHT, option(gui, optionId)));
        gui.click(WIDTH, HEIGHT, track.x + (int) Math.round(track.width * progress), centerY(track));
    }

    private static void dragSlider(ConfigGui gui, String optionId, double progress) {
        GuiBounds track = ControlLayout.sliderTrack(gui.optionBounds(WIDTH, HEIGHT, option(gui, optionId)));
        gui.drag(WIDTH, HEIGHT, track.x + (int) Math.round(track.width * progress), centerY(track));
    }

    private static void clickNumberField(ConfigGui gui, String optionId) {
        click(gui, ControlLayout.sliderField(gui.optionBounds(WIDTH, HEIGHT, option(gui, optionId))));
    }

    private static void clickHexField(ConfigGui gui, String optionId) {
        click(gui, gui.colorHexBounds(WIDTH, HEIGHT, option(gui, optionId)));
    }

    private static void clickKeybindClear(ConfigGui gui, String optionId) {
        click(gui, ControlLayout.keybindClear(gui.optionBounds(WIDTH, HEIGHT, option(gui, optionId))));
    }

    private static void click(ConfigGui gui, GuiBounds bounds) {
        gui.click(WIDTH, HEIGHT, centerX(bounds), centerY(bounds));
    }

    private static ConfigOption option(ConfigGui gui, String id) {
        return gui.definition().option(id);
    }

    private static int centerX(GuiBounds bounds) {
        return bounds.x + bounds.width / 2;
    }

    private static int centerY(GuiBounds bounds) {
        return bounds.y + bounds.height / 2;
    }

    private static OptionCondition never() {
        return () -> false;
    }
}
