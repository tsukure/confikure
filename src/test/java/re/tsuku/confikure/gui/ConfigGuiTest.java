package re.tsuku.confikure.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;
import re.tsuku.confikure.ConfigFixtures.ExampleConfig;
import re.tsuku.confikure.Confikure;

public final class ConfigGuiTest {
    @Test
    public void guiCanEditInteractiveTypes() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        gui.click(800, 600, 690, 313);
        gui.keyTyped('!', 0);
        assertEquals("line one!", config.visuals.notes);

        gui.click(800, 600, 640, 185);
        gui.keyTyped('\0', 35);
        assertEquals(35, config.visuals.openGui);

        gui.click(800, 600, 690, 410);
        assertEquals(Arrays.asList("two", "one"), config.visuals.order);

        gui.click(800, 600, 670, 529);
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
        gui.click(800, 600, 690, 221);
        assertEquals("simple", config.visuals.mode);
        gui.click(800, 600, 690, 258);
        assertEquals("fancy", config.visuals.mode);

        assertEquals("one", config.visuals.cycleMode);
        gui.click(800, 600, 690, 267);
        assertEquals("two", config.visuals.cycleMode);
        gui.click(800, 600, 690, 267);
        assertEquals("one", config.visuals.cycleMode);
        gui.click(800, 600, 560, 267);
        assertEquals("two", config.visuals.cycleMode);
    }

    @Test
    public void guiOnlyInteractsWithComponents() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        gui.click(800, 600, 300, 221);
        assertEquals("simple", config.visuals.mode);

        gui.click(800, 600, 690, 221);
        gui.click(800, 600, 690, 258);
        assertEquals("fancy", config.visuals.mode);
    }

    @Test
    public void guiDragsSlidersAndEditsTextSelection() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(0);
        gui.click(800, 600, 584, 221);
        gui.drag(800, 600, 694, 221);
        gui.release();
        assertEquals(2.0D, config.movement.speed, 0.0D);

        gui.selectedCategory(1);
        gui.click(800, 600, 690, 313);
        gui.keyTyped('\0', 30, false, true);
        gui.keyTyped('x', 0);
        assertEquals("x", config.visuals.notes);

        gui.selectedCategory(0);
        gui.click(800, 600, 690, 221);
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
        gui.click(800, 600, 690, 313);
        gui.keyTyped('\0', 30, false, true);
        gui.keyTyped('x', 0);
        assertTrue(gui.keyTyped('\0', 28));
        assertEquals("x", config.visuals.notes);
        assertFalse(gui.keyTyped('\0', 1));

        gui.click(800, 600, 690, 529);
        assertTrue(gui.keyTyped('\0', 1));
        assertFalse(gui.keyTyped('\0', 1));
    }

    @Test
    public void keybindClearButtonClearsOrResets() {
        ExampleConfig config = new ExampleConfig();
        ConfigGui gui = new ConfigGui(Confikure.scan(config));

        gui.selectedCategory(1);
        gui.click(800, 600, 690, 185);
        assertEquals(0, config.visuals.openGui);
    }
}
