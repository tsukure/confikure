package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class KeybindEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            boolean hovered) {
        int x = bounds.x + bounds.width - 58;
        EditorDraw.frame(renderer, x, bounds.y + 4, 54, 16, theme, hovered);
        renderer.centeredText(String.valueOf(option.get()), x, bounds.y + 8, 54, theme.text);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }
}
