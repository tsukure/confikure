package re.tsuku.confikure.gui.color;

import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

public interface ColorPickerHost {
    boolean interactive(ConfigOption option);

    GuiBounds optionBounds(GuiBounds panel, ConfigOption option);

    void focusColor(ConfigOption option, GuiBounds hexBounds, int mouseX);

    void activatePopup(ConfigOption option);

    void clearPopupActive();

    String displayValue(ConfigOption option);

    int textCursor(ConfigOption option);

    int textSelectionStart(ConfigOption option);

    int textSelectionEnd(ConfigOption option);

    int mouseX();

    int mouseY();

    void drawTextField(GuiRenderer renderer, GuiBounds bounds, String text, int cursor, int selectionStart,
            int selectionEnd, boolean hovered, boolean focused);

    boolean focused(ConfigOption option);
}
