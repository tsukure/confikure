package re.tsuku.confikure.gui.editor;

import java.util.List;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class ListEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            boolean hovered) {
        Object value = option.get();
        int size = value instanceof List ? ((List<?>) value).size() : 0;
        renderer.text(size + " items", bounds.x + bounds.width - 54, bounds.y + 8, theme.mutedText);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }
}
