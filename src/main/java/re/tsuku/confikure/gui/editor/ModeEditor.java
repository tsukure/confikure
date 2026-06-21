package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class ModeEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        int width = 142;
        int x = bounds.x + bounds.width - width - 6;
        int y = bounds.y + (bounds.height - 18) / 2;
        EditorDraw.frame(renderer, x, y, width, 18, theme, false, false, option.enabled());
        boolean leftHover = option.enabled() && contains(x, y, 20, 18, context.mouseX(), context.mouseY());
        boolean rightHover = option.enabled()
                && contains(x + width - 20, y, 20, 18, context.mouseX(), context.mouseY());
        EditorDraw.inlineButton(renderer, theme, x, y, 20, 18, leftHover, option.enabled());
        EditorDraw.inlineButton(renderer, theme, x + width - 20, y, 20, 18, rightHover, option.enabled());
        EditorDraw.chevron(renderer, x + 8, y + 6, EditorDraw.Direction.LEFT,
                !option.enabled() ? theme.disabledText : leftHover ? theme.accent : theme.mutedText);
        renderer.centeredText(String.valueOf(option.get()), x + 20, y + 5, width - 40,
                option.enabled() ? theme.text : theme.disabledText);
        EditorDraw.chevron(renderer, x + width - 12, y + 6, EditorDraw.Direction.RIGHT,
                !option.enabled() ? theme.disabledText : rightHover ? theme.accent : theme.mutedText);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }

    private static boolean contains(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
