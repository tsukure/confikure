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
            EditorContext context) {
        int width = 142;
        int x = bounds.x + bounds.width - width - 6;
        int height = multiline ? 42 : 18;
        int y = bounds.y + (bounds.height - height) / 2;
        String text = String.valueOf(option.get());
        String line = firstLine(text);
        int cursor = Math.max(0, Math.min(context.textCursor(option), text.length()));
        EditorDraw.textField(renderer, theme, x, y, width, height, line, cursor, context.textSelectionStart(option),
                context.textSelectionEnd(option), context.hovered(option), context.focused(option),
                option.enabled(), multiline);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }

    private static String firstLine(String text) {
        int index = text.indexOf('\n');
        return index < 0 ? text : text.substring(0, index);
    }
}
