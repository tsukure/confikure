package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.layout.ControlLayout;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class ModeEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        GuiBounds control = ControlLayout.mode(bounds);
        EditorDraw.frame(renderer, control.x, control.y, control.width, control.height, theme, false, false,
                option.enabled());
        GuiBounds left = new GuiBounds(control.x, control.y, 20, control.height);
        GuiBounds right = new GuiBounds(control.x + control.width - 20, control.y, 20, control.height);
        boolean leftHover = option.enabled() && left.contains(context.mouseX(), context.mouseY());
        boolean rightHover = option.enabled()
                && right.contains(context.mouseX(), context.mouseY());
        EditorDraw.inlineButton(renderer, theme, left.x, left.y, left.width, left.height, leftHover, option.enabled());
        EditorDraw.inlineButton(renderer, theme, right.x, right.y, right.width, right.height, rightHover,
                option.enabled());
        EditorDraw.chevron(renderer, control.x + 8, control.y + 6, EditorDraw.Direction.LEFT,
                !option.enabled() ? theme.disabledText : leftHover ? theme.accent : theme.mutedText);
        renderer.centeredText(String.valueOf(option.get()), control.x + 20, control.y + 5, control.width - 40,
                option.enabled() ? theme.text : theme.disabledText);
        EditorDraw.chevron(renderer, control.x + control.width - 12, control.y + 6, EditorDraw.Direction.RIGHT,
                !option.enabled() ? theme.disabledText : rightHover ? theme.accent : theme.mutedText);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }
}
