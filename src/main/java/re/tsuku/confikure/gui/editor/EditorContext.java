package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.model.ConfigOption;

/**
 * read-only interaction state exposed to option editors.
 */
public interface EditorContext {
    boolean hovered(ConfigOption option);

    boolean focused(ConfigOption option);

    boolean active(ConfigOption option);

    boolean dropdownOpen(ConfigOption option);

    boolean colorPickerOpen(ConfigOption option);

    String displayValue(ConfigOption option);

    int mouseX();

    int mouseY();

    int textCursor(ConfigOption option);

    int textSelectionStart(ConfigOption option);

    int textSelectionEnd(ConfigOption option);
}
