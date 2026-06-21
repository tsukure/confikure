package re.tsuku.confikure.gui;

import re.tsuku.confikure.gui.color.ColorPickerHost;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class ConfigGuiColorPickerHost implements ColorPickerHost {
    private final ConfigGui gui;

    ConfigGuiColorPickerHost(ConfigGui gui) {
        this.gui = gui;
    }

    public boolean interactive(ConfigOption option) {
        return gui.interactive(option);
    }

    public GuiBounds optionBounds(GuiBounds panel, ConfigOption option) {
        return gui.optionBounds(panel, option);
    }

    public void focusColor(ConfigOption option, GuiBounds hexBounds, int mouseX) {
        gui.focusColor(option, hexBounds, mouseX);
    }

    public void activatePopup(ConfigOption option) {
        gui.activatePopup(option);
    }

    public void clearPopupActive() {
        gui.clearPopupActive();
    }

    public String displayValue(ConfigOption option) {
        return gui.displayValue(option);
    }

    public int textCursor(ConfigOption option) {
        return gui.textCursor(option);
    }

    public int textSelectionStart(ConfigOption option) {
        return gui.textSelectionStart(option);
    }

    public int textSelectionEnd(ConfigOption option) {
        return gui.textSelectionEnd(option);
    }

    public int mouseX() {
        return gui.mouseX();
    }

    public int mouseY() {
        return gui.mouseY();
    }

    public void drawTextField(GuiRenderer renderer, GuiBounds bounds, String text, int cursor, int selectionStart,
            int selectionEnd, boolean hovered, boolean focused) {
        gui.drawTextField(renderer, bounds, text, cursor, selectionStart, selectionEnd, hovered, focused);
    }

    public boolean focused(ConfigOption option) {
        return gui.focused(option);
    }
}
