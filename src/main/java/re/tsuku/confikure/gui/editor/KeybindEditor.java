package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.layout.ControlLayout;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class KeybindEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        GuiBounds control = ControlLayout.keybind(bounds);
        GuiBounds clear = ControlLayout.keybindClear(bounds);
        EditorDraw.frame(renderer, control.x, control.y, control.width, control.height, theme, context.hovered(option),
                context.focused(option), option.enabled());
        boolean showClear = option.enabled() && option.keybindClearable() && (context.hovered(option)
                || context.focused(option));
        renderer.centeredText(context.focused(option) ? "press key" : context.displayValue(option), control.x,
                control.y + 5,
                showClear ? 62 : 80,
                !option.enabled() ? theme.disabledText : context.focused(option) ? theme.accent : theme.text);
        if (showClear) {
            boolean hovered = clear.contains(context.mouseX(), context.mouseY());
            EditorDraw.inlineButton(renderer, theme, clear.x, clear.y, clear.width, clear.height, hovered, true);
            renderer.centeredText("x", clear.x, clear.y + 5, clear.width, hovered ? theme.danger : theme.mutedText);
        }
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }
}
