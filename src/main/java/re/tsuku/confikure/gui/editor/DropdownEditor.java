package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.layout.ControlLayout;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class DropdownEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        GuiBounds control = ControlLayout.editor(bounds);
        EditorDraw.frame(renderer, control.x, control.y, control.width, control.height, theme, context.hovered(option),
                context.dropdownOpen(option),
                option.enabled());
        renderer.text(String.valueOf(option.get()), control.x + 5, control.y + 5,
                option.enabled() ? theme.text : theme.disabledText);
        EditorDraw.chevron(renderer, control.x + control.width - 13, control.y + 7,
                context.dropdownOpen(option) ? EditorDraw.Direction.UP : EditorDraw.Direction.DOWN,
                !option.enabled()
                        ? theme.disabledText
                        : context.hovered(option) || context.dropdownOpen(option) ? theme.accent : theme.mutedText);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }
}
