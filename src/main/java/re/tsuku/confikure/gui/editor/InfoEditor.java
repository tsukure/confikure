package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class InfoEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        int width = 120;
        int x = bounds.x + bounds.width - width - 6;
        renderer.fill(x, bounds.y + 7, x + width, bounds.y + 25, theme.panel);
        renderer.text(EditorDraw.clip(String.valueOf(option.get()), renderer, width - 10), x + 5, bounds.y + 12,
                theme.mutedText);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }
}
