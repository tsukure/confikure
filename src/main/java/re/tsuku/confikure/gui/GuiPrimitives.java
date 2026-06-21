package re.tsuku.confikure.gui;

import re.tsuku.confikure.gui.platform.GuiRenderer;

public final class GuiPrimitives {
    public enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    private GuiPrimitives() {
    }

    public static void frame(GuiRenderer renderer, ConfigTheme theme, GuiBounds bounds) {
        frame(renderer, theme, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public static void frame(GuiRenderer renderer, ConfigTheme theme, int x, int y, int width, int height) {
        renderer.fill(x, y, x + width, y + height, theme.panel);
        renderer.fill(x, y, x + width - 1, y + 1, theme.border);
        renderer.fill(x, y, x + 1, y + height - 1, theme.border);
        renderer.fill(x + 1, y + height - 1, x + width, y + height, theme.borderDark);
        renderer.fill(x + width - 1, y + 1, x + width, y + height, theme.borderDark);
    }

    public static void frame(GuiRenderer renderer, ConfigTheme theme, int x, int y, int width, int height,
            boolean hovered, boolean focused, boolean enabled) {
        renderer.fill(x, y, x + width, y + height, enabled && (hovered || focused) ? theme.panelRaised : theme.panel);
        int light = enabled && focused ? theme.accent : theme.border;
        renderer.fill(x, y, x + width - 1, y + 1, light);
        renderer.fill(x, y, x + 1, y + height - 1, light);
        renderer.fill(x + 1, y + height - 1, x + width, y + height, theme.borderDark);
        renderer.fill(x + width - 1, y + 1, x + width, y + height, theme.borderDark);
    }

    public static void box(GuiRenderer renderer, ConfigTheme theme, GuiBounds bounds, int fill, int border) {
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, fill);
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width - 1, bounds.y + 1, border);
        renderer.fill(bounds.x, bounds.y, bounds.x + 1, bounds.y + bounds.height - 1, border);
        renderer.fill(bounds.x + 1, bounds.y + bounds.height - 1, bounds.x + bounds.width,
                bounds.y + bounds.height, theme.borderDark);
        renderer.fill(bounds.x + bounds.width - 1, bounds.y + 1, bounds.x + bounds.width,
                bounds.y + bounds.height, theme.borderDark);
    }

    public static void border(GuiRenderer renderer, ConfigTheme theme, GuiBounds bounds, int color) {
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + 1, color);
        renderer.fill(bounds.x, bounds.y, bounds.x + 1, bounds.y + bounds.height, color);
        renderer.fill(bounds.x, bounds.y + bounds.height - 1, bounds.x + bounds.width, bounds.y + bounds.height,
                theme.borderDark);
        renderer.fill(bounds.x + bounds.width - 1, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
                theme.borderDark);
    }

    public static void textField(GuiRenderer renderer, ConfigTheme theme, GuiBounds bounds, String text, int cursor,
            int selectionStart, int selectionEnd, boolean hovered, boolean focused, boolean enabled,
            boolean topAligned) {
        frame(renderer, theme, bounds.x, bounds.y, bounds.width, bounds.height, hovered, focused, enabled);
        int textX = bounds.x + 5;
        int textY = topAligned ? bounds.y + 5 : bounds.y + Math.max(2, (bounds.height - renderer.fontHeight()) / 2);
        String clipped = clip(text, renderer, bounds.width - 10);
        if (enabled && selectionStart != selectionEnd) {
            int start = Math.max(0, Math.min(Math.min(selectionStart, selectionEnd), clipped.length()));
            int end = Math.max(0, Math.min(Math.max(selectionStart, selectionEnd), clipped.length()));
            int left = textX + renderer.textWidth(clipped.substring(0, start));
            int right = textX + renderer.textWidth(clipped.substring(0, end));
            renderer.fill(left, textY - 1, Math.max(left + 1, right), textY + renderer.fontHeight(),
                    theme.accentDark);
        }
        renderer.text(clipped, textX, textY, enabled ? theme.text : theme.disabledText);
        if (enabled && focused && (System.currentTimeMillis() / 450L) % 2L == 0L) {
            int shownCursor = Math.max(0, Math.min(cursor, clipped.length()));
            int cursorX = textX + renderer.textWidth(clipped.substring(0, shownCursor));
            renderer.fill(cursorX, textY - 1, cursorX + 1, textY + renderer.fontHeight(), theme.text);
        }
    }

    public static void sliderHandle(GuiRenderer renderer, ConfigTheme theme, int centerX, int y, int height,
            boolean active, boolean enabled) {
        int fill = !enabled ? theme.panel : active ? theme.accentDark : theme.panelRaised;
        int border = enabled && active ? theme.accent : theme.border;
        renderer.fill(centerX - 3, y, centerX + 4, y + height, fill);
        renderer.fill(centerX - 3, y, centerX + 4, y + 1, border);
        renderer.fill(centerX - 3, y, centerX - 2, y + height - 1, border);
        renderer.fill(centerX + 3, y + 1, centerX + 4, y + height, theme.borderDark);
        renderer.fill(centerX - 2, y + height - 1, centerX + 4, y + height, theme.borderDark);
    }

    public static void inlineButton(GuiRenderer renderer, ConfigTheme theme, int x, int y, int width, int height,
            boolean hovered, boolean enabled) {
        int fill = !enabled ? theme.panel : hovered ? theme.panelRaised : theme.panel;
        frame(renderer, theme, x, y, width, height, hovered && enabled, false, enabled);
        renderer.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
    }

    public static String clip(String text, GuiRenderer renderer, int width) {
        if (renderer.textWidth(text) <= width) {
            return text;
        }
        String value = text;
        while (value.length() > 0 && renderer.textWidth(value + "...") > width) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "...";
    }

    public static void chevron(GuiRenderer renderer, int x, int y, Direction direction, int color) {
        if (direction == Direction.LEFT) {
            renderer.fill(x + 3, y, x + 4, y + 7, color);
            renderer.fill(x + 2, y + 1, x + 3, y + 6, color);
            renderer.fill(x + 1, y + 2, x + 2, y + 5, color);
            renderer.fill(x, y + 3, x + 1, y + 4, color);
            return;
        }
        if (direction == Direction.RIGHT) {
            renderer.fill(x, y, x + 1, y + 7, color);
            renderer.fill(x + 1, y + 1, x + 2, y + 6, color);
            renderer.fill(x + 2, y + 2, x + 3, y + 5, color);
            renderer.fill(x + 3, y + 3, x + 4, y + 4, color);
            return;
        }
        if (direction == Direction.UP) {
            renderer.fill(x + 3, y, x + 4, y + 1, color);
            renderer.fill(x + 2, y + 1, x + 5, y + 2, color);
            renderer.fill(x + 1, y + 2, x + 6, y + 3, color);
            renderer.fill(x, y + 3, x + 7, y + 4, color);
            return;
        }
        renderer.fill(x, y, x + 7, y + 1, color);
        renderer.fill(x + 1, y + 1, x + 6, y + 2, color);
        renderer.fill(x + 2, y + 2, x + 5, y + 3, color);
        renderer.fill(x + 3, y + 3, x + 4, y + 4, color);
    }
}
