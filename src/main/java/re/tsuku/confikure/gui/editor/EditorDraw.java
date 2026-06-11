package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.platform.GuiRenderer;

final class EditorDraw {
    private EditorDraw() {
    }

    static void frame(GuiRenderer renderer, int x, int y, int width, int height, ConfigTheme theme, boolean hovered,
            boolean focused) {
        renderer.fill(x, y, x + width, y + height, hovered || focused ? theme.panelRaised : theme.panelSunken);
        int light = focused ? theme.accent : theme.border;
        renderer.fill(x, y, x + width, y + 1, light);
        renderer.fill(x, y + height - 1, x + width, y + height, theme.borderDark);
        renderer.fill(x, y, x + 1, y + height, light);
        renderer.fill(x + width - 1, y, x + width, y + height, theme.borderDark);
    }

    static void textField(GuiRenderer renderer, ConfigTheme theme, int x, int y, int width, int height, String text,
            int cursor, int selectionStart, int selectionEnd, boolean hovered, boolean focused) {
        frame(renderer, x, y, width, height, theme, hovered, focused);
        int textX = x + 5;
        int textY = y + 5;
        String clipped = clip(text, renderer, width - 10);
        if (selectionStart != selectionEnd) {
            int start = Math.max(0, Math.min(Math.min(selectionStart, selectionEnd), clipped.length()));
            int end = Math.max(0, Math.min(Math.max(selectionStart, selectionEnd), clipped.length()));
            int left = textX + renderer.textWidth(clipped.substring(0, start));
            int right = textX + renderer.textWidth(clipped.substring(0, end));
            renderer.fill(left, textY - 1, Math.max(left + 1, right), textY + renderer.fontHeight(),
                    theme.accentDark);
        }
        renderer.text(clipped, textX, textY, theme.text);
        if (focused && (System.currentTimeMillis() / 450L) % 2L == 0L) {
            int shownCursor = Math.max(0, Math.min(cursor, clipped.length()));
            int cursorX = textX + renderer.textWidth(clipped.substring(0, shownCursor));
            renderer.fill(cursorX, textY - 1, cursorX + 1, textY + renderer.fontHeight(), theme.text);
        }
    }

    static String clip(String text, GuiRenderer renderer, int width) {
        if (renderer.textWidth(text) <= width) {
            return text;
        }
        String value = text;
        while (value.length() > 0 && renderer.textWidth(value + "...") > width) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }
}
