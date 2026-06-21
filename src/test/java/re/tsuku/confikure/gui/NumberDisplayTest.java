package re.tsuku.confikure.gui;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import org.junit.Test;
import re.tsuku.confikure.gui.format.NumberDisplay;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.EditorType;
import re.tsuku.confikure.model.NumberRange;
import re.tsuku.confikure.model.OptionAccess;

public final class NumberDisplayTest {
    @Test
    public void hidesSteppedFloatingPointArtifacts() {
        ConfigOption option = option(new NumberRange(0.1D, 1.0D, 0.2D), 0.1D);

        option.set(0.7000000000000001D);

        assertEquals("0.7", NumberDisplay.format(option));
    }

    @Test
    public void keepsPrecisionNeededByRangeMinimum() {
        ConfigOption option = option(new NumberRange(0.05D, 1.0D, 0.1D), 0.05D);

        option.set(0.05D);

        assertEquals("0.05", NumberDisplay.format(option));
    }

    @Test
    public void preservesQuarterStepValues() {
        ConfigOption option = option(new NumberRange(0.0D, 3.0D, 0.25D), 0.0D);

        option.set(1.25D);

        assertEquals("1.25", NumberDisplay.format(option));
    }

    @Test
    public void trimsUnsteppedDecimalsToReadablePrecision() {
        ConfigOption option = option(null, 0.0D);

        option.set(0.7000000000000001D);

        assertEquals("0.7", NumberDisplay.format(option));
    }

    @Test
    public void formatsIntegersAndNegativeSteppedValuesCleanly() {
        ConfigOption option = option(new NumberRange(-1.0D, 1.0D, 0.25D), 0.0D);

        option.set(-0.5D);
        assertEquals("-0.5", NumberDisplay.format(option));

        option.set(1.0D);
        assertEquals("1", NumberDisplay.format(option));
    }

    private static ConfigOption option(NumberRange range, double initialValue) {
        MutableDoubleAccess access = new MutableDoubleAccess(initialValue);
        return new ConfigOption("number", "number", "", "", 0, EditorType.NUMBER, access, initialValue, range,
                Collections.<String>emptyList(), Collections.<String>emptyList());
    }

    private static final class MutableDoubleAccess implements OptionAccess {
        private double value;

        private MutableDoubleAccess(double value) {
            this.value = value;
        }

        public Class<?> valueType() {
            return double.class;
        }

        public Object get() {
            return value;
        }

        public void set(Object value) {
            this.value = ((Number) value).doubleValue();
        }
    }
}
