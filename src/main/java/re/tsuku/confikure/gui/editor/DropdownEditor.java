package re.tsuku.confikure.gui.editor;

import java.util.List;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class DropdownEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            boolean hovered) {
        int x = bounds.x + bounds.width - 104;
        EditorDraw.frame(renderer, x, bounds.y + 4, 100, 16, theme, hovered);
        renderer.text(String.valueOf(option.get()), x + 4, bounds.y + 8, theme.text);
        renderer.text("v", x + 90, bounds.y + 8, theme.mutedText);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
        List<String> choices = option.choices();
        if (choices.isEmpty()) {
            return;
        }
        String current = option.valueType().isEnum() ? ((Enum<?>) option.get()).name() : String.valueOf(option.get());
        int index = choices.indexOf(current);
        option.set(choices.get((index + 1) % choices.size()));
    }
}
