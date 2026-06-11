package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class ButtonEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            boolean hovered) {
        int x = bounds.x + bounds.width - 68;
        EditorDraw.frame(renderer, x, bounds.y + 4, 64, 16, theme, hovered);
        renderer.centeredText("run", x, bounds.y + 8, 64, theme.text);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
        option.press();
    }
}
