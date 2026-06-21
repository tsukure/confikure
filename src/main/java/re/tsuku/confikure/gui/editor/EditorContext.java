package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.model.ConfigOption;

/**
 * read-only interaction state exposed to option editors.
 */
public interface EditorContext {
    /**
     * returns whether the option is hovered.
     */
    boolean hovered(ConfigOption option);

    /**
     * returns whether the option has keyboard focus.
     */
    boolean focused(ConfigOption option);

    /**
     * returns whether the option is actively being interacted with.
     */
    boolean active(ConfigOption option);

    /**
     * returns whether the option's dropdown is open.
     */
    boolean dropdownOpen(ConfigOption option);

    /**
     * returns whether the option's color picker is open.
     */
    boolean colorPickerOpen(ConfigOption option);

    /**
     * returns the text the editor should display for the option value.
     */
    String displayValue(ConfigOption option);

    /**
     * returns the latest mouse x coordinate.
     */
    int mouseX();

    /**
     * returns the latest mouse y coordinate.
     */
    int mouseY();

    /**
     * returns text cursor position for text-like editors.
     */
    int textCursor(ConfigOption option);

    /**
     * returns text selection start for text-like editors.
     */
    int textSelectionStart(ConfigOption option);

    /**
     * returns text selection end for text-like editors.
     */
    int textSelectionEnd(ConfigOption option);
}
