package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class ColorEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            boolean hovered) {
        int x = bounds.x + bounds.width - 42;
        EditorDraw.frame(renderer, x, bounds.y + 4, 38, 16, theme, hovered);
        int color = option.get() instanceof Number ? ((Number) option.get()).intValue() : 0xFFFFFFFF;
        renderer.fill(x + 3, bounds.y + 7, x + 35, bounds.y + 17, color);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }
}
