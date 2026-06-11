package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class DropdownEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        int width = 142;
        int x = bounds.x + bounds.width - width - 6;
        int y = bounds.y + (bounds.height - 18) / 2;
        EditorDraw.frame(renderer, x, y, width, 18, theme, context.hovered(option), context.dropdownOpen(option));
        renderer.text(String.valueOf(option.get()), x + 5, y + 5, theme.text);
        renderer.text(context.dropdownOpen(option) ? "^" : "v", x + width - 12, y + 5, theme.mutedText);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }
}
