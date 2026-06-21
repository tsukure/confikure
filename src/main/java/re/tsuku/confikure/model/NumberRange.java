package re.tsuku.confikure.model;

/**
 * numeric bounds and optional snap step for number options.
 */
public final class NumberRange {
    private final double min;
    private final double max;
    private final double step;

    public NumberRange(double min, double max, double step) {
        if (max < min) {
            throw new IllegalArgumentException("range max cannot be less than min");
        }
        if (step < 0.0D) {
            throw new IllegalArgumentException("range step cannot be negative");
        }
        this.min = min;
        this.max = max;
        this.step = step;
    }

    /**
     * returns the minimum allowed value.
     */
    public double min() {
        return min;
    }

    /**
     * returns the maximum allowed value.
     */
    public double max() {
        return max;
    }

    /**
     * returns the snap increment, or zero when snapping is disabled.
     */
    public double step() {
        return step;
    }

    /**
     * clamps and snaps a value to this range.
     *
     * @param value
     *            candidate value
     * @return coerced value
     */
    public double coerce(double value) {
        double coerced = Math.max(min, Math.min(max, value));
        if (step > 0.0D && Double.isFinite(min)) {
            coerced = min + Math.round((coerced - min) / step) * step;
        }
        return Math.max(min, Math.min(max, coerced));
    }
}
