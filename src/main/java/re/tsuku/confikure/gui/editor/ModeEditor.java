package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

final class ModeEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        int width = 142;
        int x = bounds.x + bounds.width - width - 6;
        int y = bounds.y + (bounds.height - 18) / 2;
        EditorDraw.frame(renderer, x, y, width, 18, theme, context.hovered(option), false);
        boolean leftHover = contains(x, y, 20, 18, context.mouseX(), context.mouseY());
        boolean rightHover = contains(x + width - 20, y, 20, 18, context.mouseX(), context.mouseY());
        if (leftHover) {
            renderer.fill(x + 1, y + 1, x + 20, y + 17, theme.panelRaised);
        }
        if (rightHover) {
            renderer.fill(x + width - 20, y + 1, x + width - 1, y + 17, theme.panelRaised);
        }
        renderer.text("<", x + 6, y + 5, leftHover ? theme.accent : theme.mutedText);
        renderer.centeredText(String.valueOf(option.get()), x + 18, y + 5, width - 36, theme.text);
        renderer.text(">", x + width - 12, y + 5, rightHover ? theme.accent : theme.mutedText);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
    }

    private static boolean contains(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
