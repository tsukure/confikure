package re.tsuku.confikure.gui.editor;

import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.NumberRange;

final class SliderEditor implements OptionEditor {
    public void render(ConfigOption option, GuiBounds bounds, GuiRenderer renderer, ConfigTheme theme,
            EditorContext context) {
        int fieldWidth = 42;
        int width = 94;
        int x = bounds.x + bounds.width - width - fieldWidth - 12;
        int fieldX = bounds.x + bounds.width - fieldWidth - 6;
        int y = bounds.y + 12;
        NumberRange range = option.range();
        double value = option.get() instanceof Number ? ((Number) option.get()).doubleValue() : 0.0D;
        double min = range == null ? 0.0D : range.min();
        double max = range == null ? 1.0D : range.max();
        double progress = max <= min ? 0.0D : (value - min) / (max - min);
        int filled = (int) Math.round(width * Math.max(0.0D, Math.min(1.0D, progress)));
        renderer.fill(x, y, x + width, y + 6, theme.slot);
        renderer.fill(x, y, x + filled, y + 6, context.active(option) ? theme.accent : theme.accentDark);
        renderer.fill(x + filled - 2, y - 3, x + filled + 2, y + 9, theme.accent);
        EditorDraw.textField(renderer, theme, fieldX, bounds.y + 7, fieldWidth, 18, context.displayValue(option),
                context.textCursor(option), context.textSelectionStart(option), context.textSelectionEnd(option),
                contains(fieldX, bounds.y + 7, fieldWidth, 18, context.mouseX(), context.mouseY()),
                context.focused(option));
    }

    public void click(ConfigOption option, GuiBounds bounds, int mouseX, int mouseY) {
        NumberRange range = option.range();
        if (range == null) {
            return;
        }
        int width = 94;
        int x = bounds.x + bounds.width - width - 42 - 12;
        double progress = Math.max(0.0D, Math.min(1.0D, (mouseX - x) / (double) width));
        option.set(range.min() + (range.max() - range.min()) * progress);
    }

    private static boolean contains(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
