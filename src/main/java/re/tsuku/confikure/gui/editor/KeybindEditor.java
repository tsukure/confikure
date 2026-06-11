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
        EditorDraw.frame(renderer, x, y, 80, 18, theme, context.hovered(option), context.focused(option));
        int clearX = x + 64;
        renderer.centeredText(context.focused(option) ? "press key" : context.displayValue(option), x, y + 5,
                option.keybindClearable() ? 62 : 80,
                context.focused(option) ? theme.accent : theme.text);
        if (option.keybindClearable()) {
            boolean hovered = context.mouseX() >= clearX && context.mouseY() >= y && context.mouseX() < x + 80
                    && context.mouseY() < y + 18;
            renderer.fill(clearX, y + 1, x + 79, y + 17, hovered ? theme.danger : theme.panelSunken);
            renderer.centeredText("x", clearX, y + 5, 16, hovered ? theme.text : theme.mutedText);
        }
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }
}
