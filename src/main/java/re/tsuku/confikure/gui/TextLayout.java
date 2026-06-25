package re.tsuku.confikure.gui;

import java.util.ArrayList;
import java.util.List;
import re.tsuku.confikure.gui.platform.GuiRenderer;

/**
 * shared text layout for wrapped text fields.
 */
public final class TextLayout {
    public static final int LINE_GAP = 2;

    private TextLayout() {
    }

    /**
     * wraps a text value into visual lines constrained to a pixel width.
     */
    public static List<Line> wrappedLines(GuiRenderer renderer, String text, int width) {
        List<Line> lines = new ArrayList<Line>();
        String value = text == null ? "" : text;
        if (value.isEmpty()) {
            lines.add(new Line("", 0, 0));
            return lines;
        }
        int start = 0;
        while (start <= value.length()) {
            int newline = value.indexOf('\n', start);
            int end = newline < 0 ? value.length() : newline;
            wrapLogicalLine(renderer, value, start, end, width, lines);
            if (newline < 0) {
                break;
            }
            start = newline + 1;
            if (start == value.length()) {
                lines.add(new Line("", start, start));
                break;
            }
        }
        return lines;
    }

    /**
     * returns a horizontally scrolled single-line window that keeps the cursor visible.
     */
    public static Line singleLineWindow(GuiRenderer renderer, String text, int width, int cursor) {
        String value = text == null ? "" : text;
        int safeCursor = Math.max(0, Math.min(cursor, value.length()));
        if (renderer.textWidth(value) <= width) {
            return new Line(value, 0, value.length());
        }

        int start = safeCursor;
        while (start > 0 && renderer.textWidth(value.substring(start - 1, safeCursor)) <= width) {
            start--;
        }
        if (start < safeCursor && renderer.textWidth(value.substring(start, safeCursor)) > width) {
            start++;
        }

        int end = safeCursor;
        while (end < value.length() && renderer.textWidth(value.substring(start, end + 1)) <= width) {
            end++;
        }
        if (start == end && end < value.length()) {
            end++;
        }

        return new Line(value.substring(start, end), start, end);
    }

    /**
     * returns the visual line index containing the cursor.
     */
    public static int cursorLine(List<Line> lines, int cursor) {
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            if (cursor < line.end() || cursor == line.start() && cursor == line.end()) {
                return i;
            }
            if (cursor == line.end()) {
                if (i + 1 < lines.size() && lines.get(i + 1).start() == line.end()) {
                    continue;
                }
                return i;
            }
            if (i == lines.size() - 1) {
                return i;
            }
        }
        return 0;
    }

    /**
     * returns how many wrapped lines fit in a text field.
     */
    public static int visibleLineCount(GuiRenderer renderer, int fieldHeight) {
        return Math.max(1, (fieldHeight - 10 + LINE_GAP) / lineStep(renderer));
    }

    /**
     * returns the first visible wrapped line for a field.
     */
    public static int firstVisibleLine(GuiRenderer renderer, List<Line> lines, int cursor, int fieldHeight,
            boolean focused) {
        if (!focused) {
            return 0;
        }
        int cursorLine = cursorLine(lines, cursor);
        int visibleLines = visibleLineCount(renderer, fieldHeight);
        return Math.max(0, cursorLine - visibleLines + 1);
    }

    /**
     * returns the vertical step between wrapped text baselines.
     */
    public static int lineStep(GuiRenderer renderer) {
        return renderer.fontHeight() + LINE_GAP;
    }

    /**
     * returns the text index nearest a mouse position in a wrapped field.
     */
    public static int positionAt(GuiRenderer renderer, String text, int width, int fieldHeight, int cursor,
            boolean focused, int textX, int textY, int mouseX, int mouseY) {
        String value = text == null ? "" : text;
        List<Line> lines = wrappedLines(renderer, value, width);
        int safeCursor = Math.max(0, Math.min(cursor, value.length()));
        int firstLine = firstVisibleLine(renderer, lines, safeCursor, fieldHeight, focused);
        int lineOffset = Math.max(0, (mouseY - textY) / lineStep(renderer));
        int lineIndex = Math.max(0, Math.min(lines.size() - 1, firstLine + lineOffset));
        Line line = lines.get(lineIndex);
        return positionInLine(renderer, line, textX, mouseX, value.length());
    }

    /**
     * returns the text index nearest a mouse position in a horizontally scrolled single-line field.
     */
    public static int singleLinePositionAt(GuiRenderer renderer, String text, int width, int cursor, boolean focused,
            int textX, int mouseX) {
        String value = text == null ? "" : text;
        Line line = focused ? singleLineWindow(renderer, value, width, cursor) : new Line(value, 0, value.length());
        return positionInLine(renderer, line, textX, mouseX, value.length());
    }

    private static int positionInLine(GuiRenderer renderer, Line line, int textX, int mouseX, int textLength) {
        int next = line.end();
        for (int i = 0; i <= line.text().length(); i++) {
            int left = renderer.textWidth(line.text().substring(0, i));
            int right = i == line.text().length() ? left : renderer.textWidth(line.text().substring(0, i + 1));
            if (mouseX < textX + (left + right) / 2) {
                next = line.start() + i;
                break;
            }
        }
        return Math.max(0, Math.min(textLength, next));
    }

    private static void wrapLogicalLine(GuiRenderer renderer, String text, int start, int end, int width,
            List<Line> lines) {
        if (start == end) {
            lines.add(new Line("", start, end));
            return;
        }
        int lineStart = start;
        while (lineStart < end) {
            int lineEnd = fittingLineEnd(renderer, text, lineStart, end, width);
            lines.add(new Line(text.substring(lineStart, lineEnd), lineStart, lineEnd));
            lineStart = lineEnd;
        }
    }

    private static int fittingLineEnd(GuiRenderer renderer, String text, int start, int end, int width) {
        int lastFit = start;
        int lastBreak = -1;
        for (int i = start; i < end; i++) {
            String candidate = text.substring(start, i + 1);
            if (renderer.textWidth(candidate) > width) {
                break;
            }
            lastFit = i + 1;
            if (Character.isWhitespace(text.charAt(i))) {
                lastBreak = i + 1;
            }
        }
        if (lastFit == start) {
            return start + 1;
        }
        if (lastFit < end && lastBreak > start) {
            return lastBreak;
        }
        return lastFit;
    }

    /**
     * one visual text line and its source string bounds.
     */
    public static final class Line {
        private final String text;
        private final int start;
        private final int end;

        private Line(String text, int start, int end) {
            this.text = text;
            this.start = start;
            this.end = end;
        }

        public String text() {
            return text;
        }

        public int start() {
            return start;
        }

        public int end() {
            return end;
        }
    }
}
