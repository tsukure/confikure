package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.NumberRange;

final class SliderEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            boolean hovered) {
        int x = bounds.x + bounds.width - 92;
        int y = bounds.y + 7;
        int width = 88;
        NumberRange range = option.range();
        double value = option.get() instanceof Number ? ((Number) option.get()).doubleValue() : 0.0D;
        double min = range == null ? 0.0D : range.min();
        double max = range == null ? 1.0D : range.max();
        double progress = max <= min ? 0.0D : (value - min) / (max - min);
        int filled = (int) Math.round(width * Math.max(0.0D, Math.min(1.0D, progress)));
        renderer.fill(x, y, x + width, y + 6, theme.slot);
        renderer.fill(x, y, x + filled, y + 6, theme.accentDark);
        renderer.fill(x + filled - 2, y - 3, x + filled + 2, y + 9, theme.accent);
        renderer.text(String.valueOf(option.get()), x, bounds.y + 16, theme.mutedText);
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
        NumberRange range = option.range();
        if (range == null) {
            return;
        }
        int x = bounds.x + bounds.width - 92;
        double progress = Math.max(0.0D, Math.min(1.0D, (mouseX - x) / 88.0D));
        option.set(range.min() + (range.max() - range.min()) * progress);
    }
}
