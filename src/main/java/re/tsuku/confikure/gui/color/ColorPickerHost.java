package re.tsuku.confikure.gui.color;

import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

/**
 * host callbacks used by the color picker popup.
 */
public interface ColorPickerHost {
    /**
     * returns whether the option can still be interacted with.
     */
    boolean interactive(ConfigOption option);

    /**
     * returns the option row bounds for positioning the popup.
     */
    GuiBounds optionBounds(GuiBounds panel, ConfigOption option);

    /**
     * focuses the hex text field.
     */
    void focusColor(ConfigOption option, GuiBounds hexBounds, int mouseX);

    /**
     * marks the popup as the active interaction target.
     */
    void activatePopup(ConfigOption option);

    /**
     * clears active popup interaction state.
     */
    void clearPopupActive();

    /**
     * returns formatted color text.
     */
    String displayValue(ConfigOption option);

    /**
     * returns hex field cursor position.
     */
    int textCursor(ConfigOption option);

    /**
     * returns hex field selection start.
     */
    int textSelectionStart(ConfigOption option);

    /**
     * returns hex field selection end.
     */
    int textSelectionEnd(ConfigOption option);

    /**
     * returns current mouse x coordinate.
     */
    int mouseX();

    /**
     * returns current mouse y coordinate.
     */
    int mouseY();

    /**
     * draws the popup hex text field.
     */
    void drawTextField(GuiRenderer renderer, GuiBounds bounds, String text, int cursor, int selectionStart,
            int selectionEnd, boolean hovered, boolean focused);

    /**
     * returns whether the option's hex field is focused.
     */
    boolean focused(ConfigOption option);
}
