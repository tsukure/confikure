package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.layout.ControlLayout;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class ButtonEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        GuiBounds control = ControlLayout.button(bounds);
        EditorDraw.frame(renderer, control.x, control.y, control.width, control.height, theme, context.hovered(option),
                context.active(option), option.enabled());
        renderer.centeredText("run", control.x, control.y + 5, control.width,
                option.enabled() ? theme.text : theme.disabledText);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
        option.press();
    }
}
