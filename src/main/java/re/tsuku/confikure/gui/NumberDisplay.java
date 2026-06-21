package re.tsuku.confikure.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.NumberRange;

final class NumberDisplay {
    private NumberDisplay() {
    }

    static String format(ConfigOption option) {
        Object value = option.get();
        if (!(value instanceof Number)) {
            return String.valueOf(value);
        }
        double number = ((Number) value).doubleValue();
        if (Math.rint(number) == number) {
            return String.valueOf((long) number);
        }
        NumberRange range = option.range();
        if (range != null && range.step() > 0.0D && Double.isFinite(range.step())) {
            int scale = Math.max(decimalScale(range.min()), decimalScale(range.step()));
            BigDecimal rounded = BigDecimal.valueOf(number).setScale(scale, RoundingMode.HALF_UP);
            return rounded.stripTrailingZeros().toPlainString();
        }
        return trim(String.format(Locale.ROOT, "%.4f", number));
    }

    private static int decimalScale(double number) {
        return Math.max(0, BigDecimal.valueOf(number).stripTrailingZeros().scale());
    }

    private static String trim(String text) {
        while (text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        return text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
    }
}
