package re.tsuku.confikure.gui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class ListEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        Object value = option.get();
        int size = value instanceof List ? ((List<?>) value).size() : 0;
        renderer.text(size + " items", bounds.x + bounds.width - 66, bounds.y + 8, theme.mutedText);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
        Object value = option.get();
        if (!(value instanceof List) || ((List<?>) value).size() < 2) {
            return;
        }
        List<Object> copy = new ArrayList<Object>((List<?>) value);
        Collections.rotate(copy, -1);
        option.set(copy);
    }
}
