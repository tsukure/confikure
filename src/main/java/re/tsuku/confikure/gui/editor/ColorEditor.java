package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class ColorEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        int x = bounds.x + bounds.width - 46;
        int y = bounds.y + (bounds.height - 18) / 2;
        EditorDraw.frame(renderer, x, y, 40, 18, theme, context.hovered(option), context.focused(option));
        int color = option.get() instanceof Number ? ((Number) option.get()).intValue() : 0xFFFFFFFF;
        renderer.fill(x + 4, y + 4, x + 36, y + 14, theme.slot);
        renderer.fill(x + 5, y + 5, x + 35, y + 13, color);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }

}
