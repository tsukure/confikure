package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.layout.ControlLayout;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class ColorEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        GuiBounds control = ControlLayout.colorSwatch(bounds);
        EditorDraw.frame(renderer, control.x, control.y, control.width, control.height, theme, context.hovered(option),
                context.colorPickerOpen(option), option.enabled());
        int color = option.get() instanceof Number ? ((Number) option.get()).intValue() : 0xFFFFFFFF;
        renderer.fill(control.x + 4, control.y + 4, control.x + 36, control.y + 14, theme.slot);
        renderer.fill(control.x + 5, control.y + 5, control.x + 35, control.y + 13, color);
        if (!option.enabled()) {
            renderer.fill(control.x + 5, control.y + 5, control.x + 35, control.y + 6, theme.borderDark);
            renderer.fill(control.x + 5, control.y + 12, control.x + 35, control.y + 13, theme.borderDark);
        }
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }

}
