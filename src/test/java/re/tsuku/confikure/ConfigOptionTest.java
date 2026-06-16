package re.tsuku.confikure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static re.tsuku.confikure.ConfigFixtures.find;

import java.util.Arrays;
import org.junit.Test;
import re.tsuku.confikure.ConfigFixtures.ExampleConfig;
import re.tsuku.confikure.ConfigFixtures.MutableDefaultConfig;
import re.tsuku.confikure.ConfigFixtures.NumericConfig;
import re.tsuku.confikure.ConfigFixtures.OffsetRangeConfig;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigOption;

public final class ConfigOptionTest {
    @Test
    public void coercesRangeAndTracksDirtyState() {
        ExampleConfig config = new ExampleConfig();
        ConfigOption speed = find(Confikure.scan(config).categories().get(0).options(), "speed");

        assertFalse(speed.dirty());
        speed.set(3.2D);

        assertEquals(2.0D, (Double) speed.get(), 0.0D);
        assertTrue(speed.dirty());

        speed.reset();
        assertEquals(1.0D, (Double) speed.get(), 0.0D);
    }

    @Test
    public void rangeStepsAreAnchoredToMinimum() {
        OffsetRangeConfig config = new OffsetRangeConfig();
        ConfigOption value = Confikure.scan(config).option("offset");

        value.set(0.24D);
        assertEquals(0.3D, config.general.offset, 0.0000001D);

        value.set(0.19D);
        assertEquals(0.1D, config.general.offset, 0.0000001D);
    }

    @Test
    public void numericFieldsCoerceWithoutRange() {
        NumericConfig config = new NumericConfig();
        ConfigDefinition definition = Confikure.scan(config);

        definition.option("int-value").set(2.8D);
        definition.option("long-value").set(3.2D);
        definition.option("float-value").set(1.25D);
        definition.option("double-value").set(2);

        assertEquals(3, config.general.intValue);
        assertEquals(3L, config.general.longValue);
        assertEquals(1.25F, config.general.floatValue, 0.0F);
        assertEquals(2.0D, config.general.doubleValue, 0.0D);
    }

    @Test
    public void mutableListDefaultsAreSnapshotted() {
        MutableDefaultConfig config = new MutableDefaultConfig();
        ConfigOption list = Confikure.scan(config).option("entries");

        config.general.entries.add("two");

        assertTrue(list.dirty());
        list.reset();
        assertEquals(Arrays.asList("one"), config.general.entries);
    }
}
