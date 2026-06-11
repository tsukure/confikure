package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class TextEditor implements OptionEditor {
    private final boolean multiline;

    TextEditor(boolean multiline) {
        this.multiline = multiline;
    }

    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            boolean hovered) {
        int x = bounds.x + bounds.width - 104;
        int height = multiline ? 34 : 16;
        EditorDraw.frame(renderer, x, bounds.y + 4, 100, height, theme, hovered);
        String text = String.valueOf(option.get());
        renderer.text(clip(text, renderer, 92), x + 4, bounds.y + 8, theme.text);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }

    private static String clip(String text, GuiRenderer renderer, int width) {
        if (renderer.textWidth(text) <= width) {
            return text;
        }
        String value = text;
        while (value.length() > 0 && renderer.textWidth(value + "...") > width) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }
}
