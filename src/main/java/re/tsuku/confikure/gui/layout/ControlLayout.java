package re.tsuku.confikure.gui.layout;

import re.tsuku.confikure.gui.GuiBounds;

/**
 * Shared control geometry for option editors and input hit testing.
 */
public final class ControlLayout {
    public static final int EDITOR_WIDTH = 142;
    public static final int RIGHT_PADDING = 6;
    public static final int FIELD_HEIGHT = 18;
    public static final int SLIDER_TRACK_WIDTH = 94;
    public static final int SLIDER_FIELD_WIDTH = 42;
    public static final int SLIDER_HANDLE_PAD = 4;

    private ControlLayout() {
    }

    public static GuiBounds rightBounds(GuiBounds row, int width, int height) {
        return new GuiBounds(row.x + row.width - width - RIGHT_PADDING, row.y + (row.height - height) / 2, width,
                height);
    }

    public static GuiBounds editor(GuiBounds row) {
        return rightBounds(row, EDITOR_WIDTH, FIELD_HEIGHT);
    }

    public static GuiBounds text(GuiBounds row, boolean multiline) {
        return rightBounds(row, EDITOR_WIDTH, multiline ? 42 : FIELD_HEIGHT);
    }

    public static GuiBounds switchControl(GuiBounds row) {
        return rightBounds(row, 34, 16);
    }

    public static GuiBounds button(GuiBounds row) {
        return rightBounds(row, 64, FIELD_HEIGHT);
    }

    public static GuiBounds list(GuiBounds row) {
        return rightBounds(row, 64, FIELD_HEIGHT);
    }

    public static GuiBounds colorSwatch(GuiBounds row) {
        return rightBounds(row, 40, FIELD_HEIGHT);
    }

    public static GuiBounds keybind(GuiBounds row) {
        return rightBounds(row, 80, FIELD_HEIGHT);
    }

    public static GuiBounds keybindClear(GuiBounds row) {
        GuiBounds keybind = keybind(row);
        return new GuiBounds(keybind.x + 64, keybind.y, 16, FIELD_HEIGHT);
    }

    public static GuiBounds mode(GuiBounds row) {
        return editor(row);
    }

    public static GuiBounds number(GuiBounds row) {
        GuiBounds bounds = rightBounds(row, SLIDER_TRACK_WIDTH + SLIDER_FIELD_WIDTH + 6, 22);
        return new GuiBounds(bounds.x - SLIDER_HANDLE_PAD, bounds.y, bounds.width + SLIDER_HANDLE_PAD * 2,
                bounds.height);
    }

    public static GuiBounds sliderTrack(GuiBounds row) {
        int x = row.x + row.width - SLIDER_TRACK_WIDTH - SLIDER_FIELD_WIDTH - 6 - RIGHT_PADDING;
        return new GuiBounds(x, row.y + 7, SLIDER_TRACK_WIDTH, FIELD_HEIGHT);
    }

    public static GuiBounds sliderHit(GuiBounds row) {
        GuiBounds track = sliderTrack(row);
        return new GuiBounds(track.x - SLIDER_HANDLE_PAD, track.y - SLIDER_HANDLE_PAD,
                track.width + SLIDER_HANDLE_PAD * 2, track.height + SLIDER_HANDLE_PAD * 2);
    }

    public static GuiBounds sliderField(GuiBounds row) {
        int x = row.x + row.width - SLIDER_FIELD_WIDTH - RIGHT_PADDING;
        return new GuiBounds(x, row.y + 7, SLIDER_FIELD_WIDTH, FIELD_HEIGHT);
    }
}
