package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.layout.ControlLayout;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class SwitchEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        GuiBounds control = ControlLayout.switchControl(bounds);
        boolean enabled = Boolean.TRUE.equals(option.get());
        EditorDraw.frame(renderer, control.x, control.y, control.width, control.height, theme, context.hovered(option),
                context.focused(option), option.enabled());
        renderer.fill(control.x + 3, control.y + 3, control.x + control.width - 3, control.y + 13,
                !option.enabled() ? theme.borderDark : enabled ? theme.accentDark : theme.slot);
        int knobX = enabled ? control.x + control.width - 13 : control.x + 3;
        EditorDraw.frame(renderer, knobX, control.y + 3, 10, 10, theme, false, false, option.enabled());
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
        option.set(!Boolean.TRUE.equals(option.get()));
    }
}
