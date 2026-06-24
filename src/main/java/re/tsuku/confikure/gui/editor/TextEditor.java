package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.layout.ControlLayout;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class TextEditor implements OptionEditor {
    private final boolean multiline;

    TextEditor(boolean multiline) {
        this.multiline = multiline;
    }

    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        GuiBounds control = ControlLayout.text(bounds, multiline);
        String text = String.valueOf(option.get());
        int cursor = Math.max(0, Math.min(context.textCursor(option), text.length()));
        EditorDraw.textField(renderer, theme, control.x, control.y, control.width, control.height, text, cursor,
                context.textSelectionStart(option), context.textSelectionEnd(option), context.hovered(option),
                context.focused(option),
                option.enabled(), multiline);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }
}
