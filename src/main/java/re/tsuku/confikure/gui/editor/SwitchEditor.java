package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class SwitchEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        int width = 34;
        int x = bounds.x + bounds.width - width - 6;
        int y = bounds.y + (bounds.height - 16) / 2;
        boolean enabled = Boolean.TRUE.equals(option.get());
        EditorDraw.frame(renderer, x, y, width, 16, theme, context.hovered(option), context.focused(option),
                option.enabled());
        renderer.fill(x + 3, y + 3, x + width - 3, y + 13,
                !option.enabled() ? theme.borderDark : enabled ? theme.accentDark : theme.slot);
        int knobX = enabled ? x + width - 13 : x + 3;
        EditorDraw.frame(renderer, knobX, y + 3, 10, 10, theme, false, false, option.enabled());
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
        option.set(!Boolean.TRUE.equals(option.get()));
    }
}
