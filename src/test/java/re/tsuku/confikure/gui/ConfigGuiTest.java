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
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.OptionCondition;

public final class ConfigGuiTest {
    @Test
    public void guiCanEditInteractiveTypes() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        gui.click(800, 600, 670, 285);
        gui.keyTyped('!', 0);
        assertEquals("line one!", config.visuals.notes);

        gui.click(800, 600, 640, 181);
        gui.keyTyped('\0', 35);
        assertEquals(35, config.visuals.openGui);

        gui.click(800, 600, 670, 358);
        assertEquals(Arrays.asList("two", "one"), config.visuals.order);

        gui.click(800, 600, 660, 441);
        gui.click(800, 600, 600, 474);
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
        gui.click(800, 600, 670, 212);
        assertEquals("simple", config.visuals.mode);
        gui.click(800, 600, 670, 241);
        assertEquals("fancy", config.visuals.mode);

        assertEquals("one", config.visuals.cycleMode);
        gui.click(800, 600, 670, 243);
        assertEquals("two", config.visuals.cycleMode);
        gui.click(800, 600, 670, 243);
        assertEquals("one", config.visuals.cycleMode);
        gui.click(800, 600, 550, 243);
        assertEquals("two", config.visuals.cycleMode);
    }

    @Test
    public void guiOnlyInteractsWithComponents() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        gui.click(800, 600, 300, 212);
        assertEquals("simple", config.visuals.mode);

        gui.click(800, 600, 670, 212);
        gui.click(800, 600, 670, 241);
        assertEquals("fancy", config.visuals.mode);
    }

    @Test
    public void guiDragsSlidersAndEditsTextSelection() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(0);
        gui.click(800, 600, 535, 212);
        assertEquals(0.0D, config.movement.speed, 0.0D);

        gui.click(800, 600, 584, 212);
        gui.drag(800, 600, 670, 212);
        gui.release();
        assertEquals(2.0D, config.movement.speed, 0.0D);

        gui.selectedCategory(1);
        gui.click(800, 600, 670, 285);
        gui.keyTyped('\0', 30, false, true);
        gui.keyTyped('x', 0);
        assertEquals("x", config.visuals.notes);

        gui.selectedCategory(0);
        gui.click(800, 600, 670, 212);
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
        gui.click(800, 600, 670, 212);
        gui.click(800, 600, 584, 212);
        gui.drag(800, 600, 670, 212);
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

        gui.click(800, 600, 660, 206);
        gui.click(800, 600, 580, 322);
        gui.keyTyped('\0', 30, false, true);
        for (char character : "#11223380".toCharArray()) {
            gui.keyTyped(character, 0);
        }

        assertEquals(0xFF112233, config.general.withoutAlpha);
        assertEquals("#112233", gui.displayValue(option));
    }

    @Test
    public void staleDisabledPopupsAndDragsDoNotMutateOptions() {
        ExampleConfig config = new ExampleConfig();
        ConfigDefinition definition = Confikure.scan(config);
        ConfigGui gui = new ConfigGui(definition);

        gui.selectedCategory(1);
        ConfigOption dropdown = definition.option("mode");
        gui.click(800, 600, 670, 212);
        dropdown.enabledWhen(never());
        gui.click(800, 600, 670, 241);
        assertEquals("simple", config.visuals.mode);

        ConfigOption color = definition.option("primary-color");
        gui.click(800, 600, 660, 441);
        color.enabledWhen(never());
        gui.click(800, 600, 540, 390);
        gui.drag(800, 600, 560, 410);
        gui.release();
        assertEquals(0xFF78A96B, config.visuals.primaryColor);

        gui.selectedCategory(0);
        ConfigOption speed = definition.option("speed");
        gui.click(800, 600, 584, 216);
        double afterClick = config.movement.speed;
        speed.enabledWhen(never());
        gui.drag(800, 600, 670, 216);
        gui.release();
        assertEquals(afterClick, config.movement.speed, 0.0D);
    }

    @Test
    public void focusedComponentsConsumeKeyboardCommitAndCancel() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        assertFalse(gui.keyTyped('\0', 1));

        gui.selectedCategory(1);
        gui.click(800, 600, 670, 285);
        gui.keyTyped('\0', 30, false, true);
        gui.keyTyped('x', 0);
        assertTrue(gui.keyTyped('\0', 28));
        assertEquals("x", config.visuals.notes);
        assertFalse(gui.keyTyped('\0', 1));

        gui.click(800, 600, 660, 441);
        assertTrue(gui.keyTyped('\0', 1));
        assertFalse(gui.keyTyped('\0', 1));
    }

    @Test
    public void keybindClearButtonClearsOrResets() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        gui.click(800, 600, 672, 181);
        assertEquals(0, config.visuals.openGui);
    }

    private static OptionCondition never() {
        return new OptionCondition() {
            public boolean test() {
                return false;
            }
        };
    }
}
