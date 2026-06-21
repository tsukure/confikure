package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class KeybindEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        int x = bounds.x + bounds.width - 86;
        int y = bounds.y + (bounds.height - 18) / 2;
        EditorDraw.frame(renderer, x, y, 80, 18, theme, context.hovered(option), context.focused(option),
                option.enabled());
        int clearX = x + 64;
        boolean showClear = option.enabled() && option.keybindClearable() && (context.hovered(option)
                || context.focused(option));
        renderer.centeredText(context.focused(option) ? "press key" : context.displayValue(option), x, y + 5,
                showClear ? 62 : 80,
                !option.enabled() ? theme.disabledText : context.focused(option) ? theme.accent : theme.text);
        if (showClear) {
            boolean hovered = context.mouseX() >= clearX && context.mouseY() >= y && context.mouseX() < x + 80
                    && context.mouseY() < y + 18;
            EditorDraw.inlineButton(renderer, theme, clearX, y, 16, 18, hovered, true);
            renderer.centeredText("x", clearX, y + 5, 16, hovered ? theme.danger : theme.mutedText);
        }
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }
}
