package re.tsuku.confikure.gui.layout;

import re.tsuku.confikure.gui.GuiBounds;

/**
 * shared control geometry for option editors and input hit testing.
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

    /**
     * returns a rectangle aligned to the right side of an option row.
     */
    public static GuiBounds rightBounds(GuiBounds row, int width, int height) {
        return new GuiBounds(row.x + row.width - width - RIGHT_PADDING, row.y + (row.height - height) / 2, width,
                height);
    }

    /**
     * returns the standard editor bounds.
     */
    public static GuiBounds editor(GuiBounds row) {
        return rightBounds(row, EDITOR_WIDTH, FIELD_HEIGHT);
    }

    /**
     * returns text field bounds.
     */
    public static GuiBounds text(GuiBounds row, boolean multiline) {
        return rightBounds(row, EDITOR_WIDTH, multiline ? 42 : FIELD_HEIGHT);
    }

    /**
     * returns switch control bounds.
     */
    public static GuiBounds switchControl(GuiBounds row) {
        return rightBounds(row, 34, 16);
    }

    /**
     * returns button control bounds.
     */
    public static GuiBounds button(GuiBounds row) {
        return rightBounds(row, 64, FIELD_HEIGHT);
    }

    /**
     * returns list control bounds.
     */
    public static GuiBounds list(GuiBounds row) {
        return rightBounds(row, 64, FIELD_HEIGHT);
    }

    /**
     * returns color swatch bounds.
     */
    public static GuiBounds colorSwatch(GuiBounds row) {
        return rightBounds(row, 40, FIELD_HEIGHT);
    }

    /**
     * returns keybind control bounds.
     */
    public static GuiBounds keybind(GuiBounds row) {
        return rightBounds(row, 80, FIELD_HEIGHT);
    }

    /**
     * returns keybind clear button bounds.
     */
    public static GuiBounds keybindClear(GuiBounds row) {
        GuiBounds keybind = keybind(row);
        return new GuiBounds(keybind.x + 64, keybind.y, 16, FIELD_HEIGHT);
    }

    /**
     * returns mode control bounds.
     */
    public static GuiBounds mode(GuiBounds row) {
        return editor(row);
    }

    /**
     * returns the full number control bounds including slider hit padding.
     */
    public static GuiBounds number(GuiBounds row) {
        GuiBounds bounds = rightBounds(row, SLIDER_TRACK_WIDTH + SLIDER_FIELD_WIDTH + 6, 22);
        return new GuiBounds(bounds.x - SLIDER_HANDLE_PAD, bounds.y, bounds.width + SLIDER_HANDLE_PAD * 2,
                bounds.height);
    }

    /**
     * returns the visible slider track bounds.
     */
    public static GuiBounds sliderTrack(GuiBounds row) {
        int x = row.x + row.width - SLIDER_TRACK_WIDTH - SLIDER_FIELD_WIDTH - 6 - RIGHT_PADDING;
        return new GuiBounds(x, row.y + 7, SLIDER_TRACK_WIDTH, FIELD_HEIGHT);
    }

    /**
     * returns the slider hitbox including handle padding.
     */
    public static GuiBounds sliderHit(GuiBounds row) {
        GuiBounds track = sliderTrack(row);
        return new GuiBounds(track.x - SLIDER_HANDLE_PAD, track.y - SLIDER_HANDLE_PAD,
                track.width + SLIDER_HANDLE_PAD * 2, track.height + SLIDER_HANDLE_PAD * 2);
    }

    /**
     * returns the numeric text field bounds.
     */
    public static GuiBounds sliderField(GuiBounds row) {
        int x = row.x + row.width - SLIDER_FIELD_WIDTH - RIGHT_PADDING;
        return new GuiBounds(x, row.y + 7, SLIDER_FIELD_WIDTH, FIELD_HEIGHT);
    }
}
