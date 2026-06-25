package re.tsuku.confikure.gui;

import java.util.List;
import re.tsuku.confikure.gui.platform.GuiRenderer;

/**
 * small drawing primitives shared by the default gui and custom editors.
 */
public final class GuiPrimitives {
    /**
     * directions supported by the chevron primitive.
     */
    public enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    private GuiPrimitives() {
    }

    /**
     * draws a framed rectangle.
     */
    public static void frame(GuiRenderer renderer, ConfigTheme theme, GuiBounds bounds) {
        frame(renderer, theme, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * draws a framed rectangle.
     */
    public static void frame(GuiRenderer renderer, ConfigTheme theme, int x, int y, int width, int height) {
        renderer.fill(x, y, x + width, y + height, theme.panel);
        renderer.fill(x, y, x + width - 1, y + 1, theme.border);
        renderer.fill(x, y, x + 1, y + height - 1, theme.border);
        renderer.fill(x + 1, y + height - 1, x + width, y + height, theme.borderDark);
        renderer.fill(x + width - 1, y + 1, x + width, y + height, theme.borderDark);
    }

    /**
     * draws a framed rectangle with interaction-aware colors.
     */
    public static void frame(GuiRenderer renderer, ConfigTheme theme, int x, int y, int width, int height,
            boolean hovered, boolean focused, boolean enabled) {
        renderer.fill(x, y, x + width, y + height, enabled && (hovered || focused) ? theme.panelRaised : theme.panel);
        int light = enabled && focused ? theme.accent : theme.border;
        renderer.fill(x, y, x + width - 1, y + 1, light);
        renderer.fill(x, y, x + 1, y + height - 1, light);
        renderer.fill(x + 1, y + height - 1, x + width, y + height, theme.borderDark);
        renderer.fill(x + width - 1, y + 1, x + width, y + height, theme.borderDark);
    }

    /**
     * draws a filled box with a border.
     */
    public static void box(GuiRenderer renderer, ConfigTheme theme, GuiBounds bounds, int fill, int border) {
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, fill);
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width - 1, bounds.y + 1, border);
        renderer.fill(bounds.x, bounds.y, bounds.x + 1, bounds.y + bounds.height - 1, border);
        renderer.fill(bounds.x + 1, bounds.y + bounds.height - 1, bounds.x + bounds.width,
                bounds.y + bounds.height, theme.borderDark);
        renderer.fill(bounds.x + bounds.width - 1, bounds.y + 1, bounds.x + bounds.width,
                bounds.y + bounds.height, theme.borderDark);
    }

    /**
     * draws a border around a rectangle.
     */
    public static void border(GuiRenderer renderer, ConfigTheme theme, GuiBounds bounds, int color) {
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + 1, color);
        renderer.fill(bounds.x, bounds.y, bounds.x + 1, bounds.y + bounds.height, color);
        renderer.fill(bounds.x, bounds.y + bounds.height - 1, bounds.x + bounds.width, bounds.y + bounds.height,
                theme.borderDark);
        renderer.fill(bounds.x + bounds.width - 1, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
                theme.borderDark);
    }

    /**
     * draws a text field frame, clipped text, selection, and cursor.
     */
    public static void textField(GuiRenderer renderer, ConfigTheme theme, GuiBounds bounds, String text, int cursor,
            int selectionStart, int selectionEnd, boolean hovered, boolean focused, boolean enabled,
            boolean topAligned) {
        frame(renderer, theme, bounds.x, bounds.y, bounds.width, bounds.height, hovered, focused, enabled);
        int textX = bounds.x + 5;
        int textY = topAligned ? bounds.y + 5 : bounds.y + Math.max(2, (bounds.height - renderer.fontHeight()) / 2);
        if (topAligned) {
            multilineText(renderer, theme, bounds, text, cursor, selectionStart, selectionEnd, focused, enabled, textX,
                    textY);
            return;
        }
        if (focused) {
            singleLineText(renderer, theme, bounds, text, cursor, selectionStart, selectionEnd, enabled, textX, textY);
            return;
        }
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

    private static void singleLineText(GuiRenderer renderer, ConfigTheme theme, GuiBounds bounds, String text,
            int cursor, int selectionStart, int selectionEnd, boolean enabled, int textX, int textY) {
        String value = text == null ? "" : text;
        int safeCursor = Math.max(0, Math.min(cursor, value.length()));
        TextLayout.Line line = TextLayout.singleLineWindow(renderer, value, Math.max(1, bounds.width - 10),
                safeCursor);

        renderer.pushClip(bounds.x + 1, bounds.y + 1, Math.max(0, bounds.width - 2), Math.max(0, bounds.height - 2));
        drawSelection(renderer, theme, line, textX, textY, renderer.fontHeight(), selectionStart, selectionEnd,
                enabled);
        renderer.text(line.text(), textX, textY, enabled ? theme.text : theme.disabledText);
        drawCursor(renderer, theme, line, textX, textY, renderer.fontHeight(), safeCursor, true, enabled);
        renderer.popClip();
    }

    private static void multilineText(GuiRenderer renderer, ConfigTheme theme, GuiBounds bounds, String text,
            int cursor,
            int selectionStart, int selectionEnd, boolean focused, boolean enabled, int textX, int textY) {
        String value = text == null ? "" : text;
        int safeCursor = Math.max(0, Math.min(cursor, value.length()));
        int textWidth = Math.max(1, bounds.width - 10);
        List<TextLayout.Line> lines = TextLayout.wrappedLines(renderer, value, textWidth);
        int lineHeight = renderer.fontHeight();
        int lineStep = TextLayout.lineStep(renderer);
        int visibleLines = TextLayout.visibleLineCount(renderer, bounds.height);
        int cursorLine = TextLayout.cursorLine(lines, safeCursor);
        int firstLine = TextLayout.firstVisibleLine(renderer, lines, safeCursor, bounds.height, focused);
        int lastLine = Math.min(lines.size(), firstLine + visibleLines);

        renderer.pushClip(bounds.x + 1, bounds.y + 1, Math.max(0, bounds.width - 2), Math.max(0, bounds.height - 2));
        for (int i = firstLine; i < lastLine; i++) {
            TextLayout.Line line = lines.get(i);
            int y = textY + (i - firstLine) * lineStep;
            drawSelection(renderer, theme, line, textX, y, lineHeight, selectionStart, selectionEnd, enabled);
            renderer.text(line.text(), textX, y, enabled ? theme.text : theme.disabledText);
            if (i == cursorLine) {
                drawCursor(renderer, theme, line, textX, y, lineHeight, safeCursor, focused, enabled);
            }
        }
        renderer.popClip();
    }

    private static void drawSelection(GuiRenderer renderer, ConfigTheme theme, TextLayout.Line line, int textX, int y,
            int lineHeight, int selectionStart, int selectionEnd, boolean enabled) {
        if (!enabled || selectionStart == selectionEnd) {
            return;
        }
        int start = Math.max(Math.min(selectionStart, selectionEnd), line.start());
        int end = Math.min(Math.max(selectionStart, selectionEnd), line.end());
        if (end <= start) {
            return;
        }
        int left = textX + renderer.textWidth(line.text().substring(0, start - line.start()));
        int right = textX + renderer.textWidth(line.text().substring(0, end - line.start()));
        renderer.fill(left, y - 1, Math.max(left + 1, right), y + lineHeight, theme.accentDark);
    }

    private static void drawCursor(GuiRenderer renderer, ConfigTheme theme, TextLayout.Line line, int textX, int y,
            int lineHeight, int cursor, boolean focused, boolean enabled) {
        if (!enabled || !focused || (System.currentTimeMillis() / 450L) % 2L != 0L) {
            return;
        }
        if (cursor < line.start() || cursor > line.end()) {
            return;
        }
        int shownCursor = Math.max(0, Math.min(cursor - line.start(), line.text().length()));
        int cursorX = textX + renderer.textWidth(line.text().substring(0, shownCursor));
        renderer.fill(cursorX, y - 1, cursorX + 1, y + lineHeight, theme.text);
    }

    /**
     * draws the standard slider handle.
     */
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

    /**
     * draws the standard inline button frame.
     */
    public static void inlineButton(GuiRenderer renderer, ConfigTheme theme, int x, int y, int width, int height,
            boolean hovered, boolean enabled) {
        int fill = !enabled ? theme.panel : hovered ? theme.panelRaised : theme.panel;
        frame(renderer, theme, x, y, width, height, hovered && enabled, false, enabled);
        renderer.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
    }

    /**
     * clips text to a maximum pixel width and appends ellipsis when needed.
     */
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

    /**
     * draws a pixel chevron.
     */
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
