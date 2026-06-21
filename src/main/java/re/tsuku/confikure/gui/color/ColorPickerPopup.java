package re.tsuku.confikure.gui.color;

import java.util.HashMap;
import java.util.Map;
import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.layout.ControlLayout;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;

public final class ColorPickerPopup {
    private static final int PADDING = 8;
    private static final int SQUARE_WIDTH = 112;
    private static final int TRACK_WIDTH = 12;
    private static final int TRACK_GAP = 8;
    private static final int ALPHA_GAP = 6;
    private static final int HEIGHT = 72;
    private static final int ROW_GAP = 8;
    private static final int FIELD_HEIGHT = 18;
    private static final int PREVIEW_WIDTH = 28;

    private final Map<ConfigOption, State> states = new HashMap<ConfigOption, State>();
    private ConfigOption option;
    private Drag drag = Drag.NONE;

    public boolean isOpen() {
        return option != null;
    }

    public boolean isOpen(ConfigOption option) {
        return this.option == option;
    }

    public ConfigOption option() {
        return option;
    }

    public void open(ConfigOption option) {
        this.option = option;
        drag = Drag.NONE;
    }

    public void close() {
        option = null;
        drag = Drag.NONE;
    }

    public void stopDrag() {
        drag = Drag.NONE;
    }

    public void render(GuiRenderer renderer, ConfigTheme theme, GuiBounds panel, ColorPickerHost host) {
        if (option == null) {
            return;
        }
        if (!host.interactive(option)) {
            close();
            return;
        }
        GuiBounds picker = bounds(panel, option, host.optionBounds(panel, option), theme.padding);
        frame(renderer, theme, picker.x, picker.y, picker.width, picker.height);
        int color = color(option);
        State state = state(option);
        syncState(option, state);

        GuiBounds square = squareBounds(picker);
        for (int y = 0; y < HEIGHT; y += 2) {
            for (int x = 0; x < SQUARE_WIDTH; x += 2) {
                float saturation = x / (float) SQUARE_WIDTH;
                float value = 1.0F - y / (float) HEIGHT;
                renderer.fill(square.x + x, square.y + y, square.x + x + 2, square.y + y + 2,
                        0xFF000000 | hsvToRgb(state.hue, saturation, value));
            }
        }
        drawBorder(renderer, theme, square.x, square.y, square.width, square.height, theme.border);

        GuiBounds hue = hueBounds(picker);
        for (int y = 0; y < HEIGHT; y += 2) {
            renderer.fill(hue.x, hue.y + y, hue.x + hue.width, hue.y + y + 2,
                    0xFF000000 | hsvToRgb(y / (float) HEIGHT, 1.0F, 1.0F));
        }
        drawBorder(renderer, theme, hue.x, hue.y, hue.width, hue.height, theme.border);

        if (option.colorAlpha()) {
            GuiBounds alpha = alphaBounds(picker);
            drawAlphaTrack(renderer, theme, alpha.x, alpha.y, color);
            drawBorder(renderer, theme, alpha.x, alpha.y, alpha.width, alpha.height, theme.border);
        }

        drawPickerHandle(renderer, theme, square.x + Math.round(state.saturation * SQUARE_WIDTH),
                square.y + Math.round((1.0F - state.value) * HEIGHT));
        drawVerticalTrackHandle(renderer, theme, hue.x, hue.y + Math.round(state.hue * HEIGHT), true);
        if (option.colorAlpha()) {
            GuiBounds alpha = alphaBounds(picker);
            drawVerticalTrackHandle(renderer, theme, alpha.x,
                    alpha.y + Math.round(((color >>> 24) & 0xFF) / 255.0F * HEIGHT), true);
        }

        GuiBounds preview = previewBounds(picker);
        frame(renderer, theme, preview.x, preview.y, preview.width, preview.height);
        renderer.fill(preview.x + 3, preview.y + 3, preview.x + preview.width - 3, preview.y + preview.height - 3,
                theme.slot);
        renderer.fill(preview.x + 4, preview.y + 4, preview.x + preview.width - 4, preview.y + preview.height - 4,
                color);

        GuiBounds hex = hexBounds(picker);
        host.drawTextField(renderer, hex, host.displayValue(option), host.textCursor(option),
                host.textSelectionStart(option), host.textSelectionEnd(option),
                hex.contains(host.mouseX(), host.mouseY()),
                host.focused(option));
    }

    public boolean handleMouse(GuiBounds panel, int mouseX, int mouseY, boolean pressed, ColorPickerHost host,
            int padding) {
        if (option == null) {
            return false;
        }
        if (!host.interactive(option)) {
            close();
            host.clearPopupActive();
            return true;
        }
        GuiBounds picker = bounds(panel, option, host.optionBounds(panel, option), padding);
        GuiBounds square = squareBounds(picker);
        GuiBounds hue = hueBounds(picker);
        GuiBounds alpha = alphaBounds(picker);
        if (pressed) {
            if (square.contains(mouseX, mouseY)) {
                drag = Drag.SQUARE;
                host.activatePopup(option);
            } else if (hue.contains(mouseX, mouseY)) {
                drag = Drag.HUE;
                host.activatePopup(option);
            } else if (option.colorAlpha() && alpha.contains(mouseX, mouseY)) {
                drag = Drag.ALPHA;
                host.activatePopup(option);
            } else if (hexBounds(picker).contains(mouseX, mouseY)) {
                host.focusColor(option, hexBounds(picker), mouseX);
                drag = Drag.NONE;
                return true;
            } else if (!picker.contains(mouseX, mouseY)) {
                close();
                return false;
            }
        }
        if (drag == Drag.NONE) {
            return picker.contains(mouseX, mouseY);
        }
        State state = state(option);
        syncState(option, state);
        if (drag == Drag.SQUARE) {
            applyState(option, state.hue, clamp((mouseX - square.x) / (float) SQUARE_WIDTH),
                    1.0F - clamp((mouseY - square.y) / (float) HEIGHT), color(option) >>> 24 & 0xFF);
        } else if (drag == Drag.HUE) {
            applyState(option, clamp((mouseY - square.y) / (float) HEIGHT), state.saturation, state.value,
                    color(option) >>> 24 & 0xFF);
        } else if (drag == Drag.ALPHA) {
            int alphaValue = Math.round(clamp((mouseY - square.y) / (float) HEIGHT) * 255.0F);
            option.set((alphaValue << 24) | (color(option) & 0x00FFFFFF));
            state.lastColor = color(option);
        }
        return true;
    }

    public GuiBounds hexBounds(GuiBounds panel, ConfigOption option, GuiBounds row, int padding) {
        return hexBounds(bounds(panel, option, row, padding));
    }

    public static String format(ConfigOption option) {
        return hex(color(option), option.colorAlpha());
    }

    public static void parse(ConfigOption option, String text) {
        String value = text == null ? "" : text.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        if (value.length() != 6 && (!option.colorAlpha() || value.length() != 8)) {
            return;
        }
        try {
            if (value.length() == 6) {
                option.set(0xFF000000 | (int) Long.parseLong(value, 16));
                return;
            }
            long rgba = Long.parseLong(value, 16);
            int rgb = (int) ((rgba >>> 8) & 0xFFFFFFL);
            int alpha = (int) (rgba & 0xFFL);
            option.set((alpha << 24) | rgb);
        } catch (NumberFormatException ignored) {
        }
    }

    private State state(ConfigOption option) {
        State state = states.get(option);
        if (state == null) {
            state = new State();
            int color = color(option);
            float[] hsv = rgbToHsv(color);
            state.hue = hsv[0];
            state.saturation = hsv[1];
            state.value = hsv[2];
            state.lastColor = color;
            states.put(option, state);
        }
        return state;
    }

    private void syncState(ConfigOption option, State state) {
        int current = color(option);
        if (current == state.lastColor) {
            return;
        }
        float[] hsv = rgbToHsv(current);
        if (hsv[1] > 0.0F && hsv[2] > 0.0F) {
            state.hue = hsv[0];
        }
        if (hsv[2] > 0.0F) {
            state.saturation = hsv[1];
        }
        state.value = hsv[2];
        state.lastColor = current;
    }

    private void applyState(ConfigOption option, float hue, float saturation, float value, int alpha) {
        State state = state(option);
        state.hue = hue;
        state.saturation = saturation;
        state.value = value;
        int nextAlpha = option.colorAlpha() ? alpha : 0xFF;
        int next = (nextAlpha << 24) | hsvToRgb(hue, saturation, value);
        option.set(next);
        state.lastColor = color(option);
    }

    private GuiBounds bounds(GuiBounds panel, ConfigOption option, GuiBounds row, int padding) {
        int width = width(option);
        int height = height();
        int x = row.x + row.width - width - ControlLayout.RIGHT_PADDING;
        int y = Math.min(row.y + row.height - 4, panel.y + panel.height - height - padding);
        return new GuiBounds(x, y, width, height);
    }

    private GuiBounds squareBounds(GuiBounds picker) {
        return new GuiBounds(picker.x + PADDING, picker.y + PADDING, SQUARE_WIDTH, HEIGHT);
    }

    private GuiBounds hueBounds(GuiBounds picker) {
        GuiBounds square = squareBounds(picker);
        return new GuiBounds(square.x + SQUARE_WIDTH + TRACK_GAP, square.y, TRACK_WIDTH, HEIGHT);
    }

    private GuiBounds alphaBounds(GuiBounds picker) {
        GuiBounds hue = hueBounds(picker);
        return new GuiBounds(hue.x + TRACK_WIDTH + ALPHA_GAP, hue.y, TRACK_WIDTH, HEIGHT);
    }

    private GuiBounds hexBounds(GuiBounds picker) {
        return new GuiBounds(picker.x + PADDING + PREVIEW_WIDTH + TRACK_GAP, controlsY(picker),
                picker.width - PADDING * 2 - PREVIEW_WIDTH - TRACK_GAP, FIELD_HEIGHT);
    }

    private GuiBounds previewBounds(GuiBounds picker) {
        return new GuiBounds(picker.x + PADDING, controlsY(picker), PREVIEW_WIDTH, FIELD_HEIGHT);
    }

    private int controlsY(GuiBounds picker) {
        return picker.y + PADDING + HEIGHT + ROW_GAP;
    }

    private int width(ConfigOption option) {
        int tracks = TRACK_GAP + TRACK_WIDTH;
        if (option.colorAlpha()) {
            tracks += ALPHA_GAP + TRACK_WIDTH;
        }
        return PADDING * 2 + SQUARE_WIDTH + tracks;
    }

    private int height() {
        return PADDING * 2 + HEIGHT + ROW_GAP + FIELD_HEIGHT;
    }

    private void drawPickerHandle(GuiRenderer renderer, ConfigTheme theme, int centerX, int centerY) {
        GuiBounds handle = new GuiBounds(centerX - 3, centerY - 3, 7, 7);
        boxed(renderer, theme, handle, theme.panelRaised, theme.text);
        renderer.fill(handle.x + 2, handle.y + 2, handle.x + handle.width - 2, handle.y + handle.height - 2,
                theme.borderDark);
    }

    private void drawVerticalTrackHandle(GuiRenderer renderer, ConfigTheme theme, int trackX, int centerY,
            boolean enabled) {
        int x = trackX - 2;
        int y = centerY - 3;
        int fill = enabled ? theme.panelRaised : theme.panel;
        int border = enabled ? theme.border : theme.borderDark;
        boxed(renderer, theme, new GuiBounds(x, y, TRACK_WIDTH + 4, 7), fill, border);
    }

    private void drawAlphaTrack(GuiRenderer renderer, ConfigTheme theme, int x, int y, int color) {
        renderer.fill(x, y, x + TRACK_WIDTH, y + HEIGHT, theme.slot);
        int rgb = color & 0x00FFFFFF;
        for (int offset = 0; offset < HEIGHT; offset += 2) {
            int alpha = Math.round(offset / (float) HEIGHT * 255.0F);
            renderer.fill(x, y + offset, x + TRACK_WIDTH, y + offset + 2, (alpha << 24) | rgb);
        }
    }

    private void drawBorder(GuiRenderer renderer, ConfigTheme theme, int x, int y, int width, int height, int color) {
        renderer.fill(x, y, x + width, y + 1, color);
        renderer.fill(x, y, x + 1, y + height, color);
        renderer.fill(x, y + height - 1, x + width, y + height, theme.borderDark);
        renderer.fill(x + width - 1, y, x + width, y + height, theme.borderDark);
    }

    private void frame(GuiRenderer renderer, ConfigTheme theme, int x, int y, int width, int height) {
        renderer.fill(x, y, x + width, y + height, theme.panel);
        renderer.fill(x, y, x + width - 1, y + 1, theme.border);
        renderer.fill(x, y, x + 1, y + height - 1, theme.border);
        renderer.fill(x + 1, y + height - 1, x + width, y + height, theme.borderDark);
        renderer.fill(x + width - 1, y + 1, x + width, y + height, theme.borderDark);
    }

    private void boxed(GuiRenderer renderer, ConfigTheme theme, GuiBounds bounds, int fill, int border) {
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, fill);
        renderer.fill(bounds.x, bounds.y, bounds.x + bounds.width - 1, bounds.y + 1, border);
        renderer.fill(bounds.x, bounds.y, bounds.x + 1, bounds.y + bounds.height - 1, border);
        renderer.fill(bounds.x + 1, bounds.y + bounds.height - 1, bounds.x + bounds.width,
                bounds.y + bounds.height, theme.borderDark);
        renderer.fill(bounds.x + bounds.width - 1, bounds.y + 1, bounds.x + bounds.width,
                bounds.y + bounds.height, theme.borderDark);
    }

    private static int color(ConfigOption option) {
        return option.get() instanceof Number ? ((Number) option.get()).intValue() : 0xFFFFFFFF;
    }

    private static String hex(int color, boolean alpha) {
        String rgb = Integer.toHexString(color & 0x00FFFFFF).toUpperCase();
        while (rgb.length() < 6) {
            rgb = "0" + rgb;
        }
        if (!alpha) {
            return "#" + rgb;
        }
        int alphaValue = color >>> 24 & 0xFF;
        String alphaHex = Integer.toHexString(alphaValue).toUpperCase();
        if (alphaHex.length() < 2) {
            alphaHex = "0" + alphaHex;
        }
        return "#" + rgb + alphaHex;
    }

    private static float[] rgbToHsv(int color) {
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = max - min;
        float hue;
        if (delta == 0.0F) {
            hue = 0.0F;
        } else if (max == red) {
            hue = ((green - blue) / delta) % 6.0F;
        } else if (max == green) {
            hue = (blue - red) / delta + 2.0F;
        } else {
            hue = (red - green) / delta + 4.0F;
        }
        hue /= 6.0F;
        if (hue < 0.0F) {
            hue += 1.0F;
        }
        float saturation = max == 0.0F ? 0.0F : delta / max;
        return new float[]{hue, saturation, max};
    }

    private static int hsvToRgb(float hue, float saturation, float value) {
        float scaled = hue * 6.0F;
        int sector = (int) Math.floor(scaled);
        float fraction = scaled - sector;
        float p = value * (1.0F - saturation);
        float q = value * (1.0F - fraction * saturation);
        float t = value * (1.0F - (1.0F - fraction) * saturation);
        float red;
        float green;
        float blue;
        switch (sector % 6) {
            case 0 :
                red = value;
                green = t;
                blue = p;
                break;
            case 1 :
                red = q;
                green = value;
                blue = p;
                break;
            case 2 :
                red = p;
                green = value;
                blue = t;
                break;
            case 3 :
                red = p;
                green = q;
                blue = value;
                break;
            case 4 :
                red = t;
                green = p;
                blue = value;
                break;
            default :
                red = value;
                green = p;
                blue = q;
                break;
        }
        return (Math.round(red * 255.0F) << 16) | (Math.round(green * 255.0F) << 8)
                | Math.round(blue * 255.0F);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private enum Drag {
        NONE, SQUARE, HUE, ALPHA
    }

    private static final class State {
        private float hue;
        private float saturation;
        private float value;
        private int lastColor;
    }
}
