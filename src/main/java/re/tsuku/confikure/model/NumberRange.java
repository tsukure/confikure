package re.tsuku.confikure.model;

public final class NumberRange {
    private final double min;
    private final double max;
    private final double step;

    public NumberRange(double min, double max, double step) {
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public double step() {
        return step;
    }

    public double coerce(double value) {
        double coerced = Math.max(min, Math.min(max, value));
        if (step > 0.0D) {
            coerced = Math.round(coerced / step) * step;
        }
        return Math.max(min, Math.min(max, coerced));
    }
}
