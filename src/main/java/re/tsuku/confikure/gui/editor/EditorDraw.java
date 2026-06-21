package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.GuiPrimitives;
import re.tsuku.confikure.gui.platform.GuiRenderer;

final class EditorDraw {
    enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    private EditorDraw() {
    }

    static void frame(GuiRenderer renderer, int x, int y, int width, int height, ConfigTheme theme, boolean hovered,
            boolean focused) {
        frame(renderer, x, y, width, height, theme, hovered, focused, true);
    }

    static void frame(GuiRenderer renderer, int x, int y, int width, int height, ConfigTheme theme, boolean hovered,
            boolean focused, boolean enabled) {
        GuiPrimitives.frame(renderer, theme, x, y, width, height, hovered, focused, enabled);
    }

    static void textField(GuiRenderer renderer, ConfigTheme theme, int x, int y, int width, int height, String text,
            int cursor, int selectionStart, int selectionEnd, boolean hovered, boolean focused) {
        textField(renderer, theme, x, y, width, height, text, cursor, selectionStart, selectionEnd, hovered, focused,
                true);
    }

    static void textField(GuiRenderer renderer, ConfigTheme theme, int x, int y, int width, int height, String text,
            int cursor, int selectionStart, int selectionEnd, boolean hovered, boolean focused, boolean enabled) {
        textField(renderer, theme, x, y, width, height, text, cursor, selectionStart, selectionEnd, hovered, focused,
                enabled, false);
    }

    static void textField(GuiRenderer renderer, ConfigTheme theme, int x, int y, int width, int height, String text,
            int cursor, int selectionStart, int selectionEnd, boolean hovered, boolean focused, boolean enabled,
            boolean topAligned) {
        GuiPrimitives.textField(renderer, theme, new GuiBounds(x, y, width, height), text, cursor, selectionStart,
                selectionEnd, hovered, focused, enabled, topAligned);
    }

    static void sliderHandle(GuiRenderer renderer, ConfigTheme theme, int centerX, int y, int height, boolean active,
            boolean enabled) {
        GuiPrimitives.sliderHandle(renderer, theme, centerX, y, height, active, enabled);
    }

    static void inlineButton(GuiRenderer renderer, ConfigTheme theme, int x, int y, int width, int height,
            boolean hovered, boolean enabled) {
        GuiPrimitives.inlineButton(renderer, theme, x, y, width, height, hovered, enabled);
    }

    static String clip(String text, GuiRenderer renderer, int width) {
        return GuiPrimitives.clip(text, renderer, width);
    }

    static void chevron(GuiRenderer renderer, int x, int y, Direction direction, int color) {
        if (direction == Direction.LEFT) {
            GuiPrimitives.chevron(renderer, x, y, GuiPrimitives.Direction.LEFT, color);
            return;
        }
        if (direction == Direction.RIGHT) {
            GuiPrimitives.chevron(renderer, x, y, GuiPrimitives.Direction.RIGHT, color);
            return;
        }
        if (direction == Direction.UP) {
            GuiPrimitives.chevron(renderer, x, y, GuiPrimitives.Direction.UP, color);
            return;
        }
        GuiPrimitives.chevron(renderer, x, y, GuiPrimitives.Direction.DOWN, color);
    }
}
