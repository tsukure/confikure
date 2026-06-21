package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.layout.ControlLayout;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.NumberRange;

final class SliderEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        GuiBounds track = ControlLayout.sliderTrack(bounds);
        GuiBounds field = ControlLayout.sliderField(bounds);
        int y = track.y + 5;
        NumberRange range = option.range();
        double value = option.get() instanceof Number ? ((Number) option.get()).doubleValue() : 0.0D;
        double min = range == null ? 0.0D : range.min();
        double max = range == null ? 1.0D : range.max();
        double progress = max <= min ? 0.0D : (value - min) / (max - min);
        int filled = (int) Math.round(track.width * Math.max(0.0D, Math.min(1.0D, progress)));
        renderer.fill(track.x, y - 1, track.x + track.width, y + 8, theme.panel);
        renderer.fill(track.x + 1, y, track.x + track.width - 1, y + 7, theme.slot);
        int fillColor = !option.enabled() ? theme.borderDark : context.active(option) ? theme.accent : theme.accentDark;
        renderer.fill(track.x + 1, y, track.x + Math.max(1, filled), y + 7,
                fillColor);
        renderer.fill(track.x + 1, y, track.x + Math.max(1, filled), y + 1,
                option.enabled() ? theme.accent : theme.border);
        int knobX = track.x + filled;
        EditorDraw.sliderHandle(renderer, theme, knobX, y - 4, 15, context.active(option), option.enabled());
        EditorDraw.textField(renderer, theme, field.x, field.y, field.width, field.height, context.displayValue(option),
                context.textCursor(option), context.textSelectionStart(option), context.textSelectionEnd(option),
                field.contains(context.mouseX(), context.mouseY()),
                context.focused(option), option.enabled());
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
        NumberRange range = option.range();
        if (range == null) {
            return;
        }
        GuiBounds track = ControlLayout.sliderTrack(bounds);
        double progress = Math.max(0.0D, Math.min(1.0D, (mouseX - track.x) / (double) track.width));
        option.set(range.min() + (range.max() - range.min()) * progress);
    }
}
