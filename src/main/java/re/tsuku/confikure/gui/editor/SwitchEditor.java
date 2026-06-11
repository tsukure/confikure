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
        EditorDraw.frame(renderer, x, y, width, 16, theme, context.hovered(option), context.focused(option));
        renderer.fill(x + 2, y + 2, x + width - 2, y + 14, enabled ? theme.accentDark : theme.slot);
        int knobX = enabled ? x + width - 13 : x + 4;
        renderer.fill(knobX, y + 4, knobX + 9, y + 12, enabled ? theme.accent : theme.border);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
        option.set(!Boolean.TRUE.equals(option.get()));
    }
}
