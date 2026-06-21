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
        EditorDraw.frame(renderer, x, y, width, 18, theme, context.hovered(option), context.dropdownOpen(option),
                option.enabled());
        renderer.text(String.valueOf(option.get()), x + 5, y + 5, option.enabled() ? theme.text : theme.disabledText);
        EditorDraw.chevron(renderer, x + width - 13, y + 7,
                context.dropdownOpen(option) ? EditorDraw.Direction.UP : EditorDraw.Direction.DOWN,
                !option.enabled()
                        ? theme.disabledText
                        : context.hovered(option) || context.dropdownOpen(option) ? theme.accent : theme.mutedText);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }
}
