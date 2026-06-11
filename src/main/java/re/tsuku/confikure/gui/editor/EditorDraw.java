package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.platform.GuiRenderer;

final class EditorDraw {
    private EditorDraw() {
    }

    static void frame(GuiRenderer renderer, int x, int y, int width, int height, ConfigTheme theme, boolean hovered) {
        renderer.fill(x, y, x + width, y + height, hovered ? theme.panelRaised : theme.panel);
        renderer.fill(x, y, x + width, y + 1, theme.border);
        renderer.fill(x, y + height - 1, x + width, y + height, theme.borderDark);
        renderer.fill(x, y, x + 1, y + height, theme.border);
        renderer.fill(x + width - 1, y, x + width, y + height, theme.borderDark);
    }
}
