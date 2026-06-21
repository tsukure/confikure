package re.tsuku.confikure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import re.tsuku.confikure.annotations.Button;
import re.tsuku.confikure.annotations.Category;
import re.tsuku.confikure.annotations.Color;
import re.tsuku.confikure.annotations.Config;
import re.tsuku.confikure.annotations.Dropdown;
import re.tsuku.confikure.annotations.Group;
import re.tsuku.confikure.annotations.Info;
import re.tsuku.confikure.annotations.Keybind;
import re.tsuku.confikure.annotations.Mode;
import re.tsuku.confikure.annotations.Multiline;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Range;
import re.tsuku.confikure.annotations.SearchTag;
import re.tsuku.confikure.annotations.Text;
import re.tsuku.confikure.model.ConfigOption;

public final class ConfigFixtures {
    private ConfigFixtures() {
    }

    public static ConfigOption find(List<ConfigOption> options, String id) {
        for (ConfigOption option : options) {
            if (option.id().equals(id)) {
                return option;
            }
        }
        throw new AssertionError(id);
    }

    @Config(name = "example")
    public static final class ExampleConfig {
        @Category(name = "visuals", order = 1)
        public final Visuals visuals = new Visuals();

        @Category(name = "movement", order = 0)
        public final Movement movement = new Movement();
    }

    public static final class Movement {
        @Group(name = "sprint")
        public final Sprint sprint = new Sprint();

        @Option(name = "speed", group = "sprint", order = 1)
        @Range(min = 0.0D, max = 2.0D, step = 0.5D)
        public double speed = 1.0D;
    }

    public static final class Sprint {
        @Option(name = "enabled")
        public boolean enabled = true;
    }

    public static final class Visuals {
        public boolean reset;

        @Option(name = "open gui", order = 0)
        @Keybind
        public int openGui = 54;

        @Option(name = "mode", order = 1)
        @Dropdown(values = {"simple", "fancy"})
        public String mode = "simple";

        @Option(name = "cycle mode", order = 2)
        @Mode(values = {"one", "two"})
        public String cycleMode = "one";

        @Option(name = "primary color", group = "theme", order = 0)
        @Color
        public int primaryColor = 0xFF78A96B;

        @Option(name = "notes", order = 3)
        @Multiline
        public String notes = "line one";

        @Option(name = "label", order = 4)
        @Text
        public String label = "visuals";

        @Option(name = "about", order = 5)
        @Info
        public String about = "client settings";

        @Option(name = "order", order = 6)
        public List<String> order = Arrays.asList("one", "two");

        @Button(name = "reset cache", label = "reset", order = 7)
        public void resetCache() {
            reset = true;
        }
    }

    @Config(name = "keybind policy")
    public static final class KeybindPolicyConfig {
        @Category(name = "general")
        public final Keybinds keybinds = new Keybinds();
    }

    @Config(name = "choice config")
    public static final class ChoiceConfig {
        @Category(name = "general")
        public final Choices general = new Choices();
    }

    public enum ChoiceMode {
        FIRST, SECOND
    }

    public static final class Choices {
        @Option(name = "enum mode")
        public ChoiceMode enumMode = ChoiceMode.FIRST;

        @Option(name = "tagged value")
        @SearchTag({"alias", "lookup"})
        public String taggedValue = "value";
    }

    @Config(name = "invalid button")
    public static final class InvalidButtonConfig {
        @Category(name = "general")
        public final InvalidButtons general = new InvalidButtons();
    }

    @Config(name = "default button label")
    public static final class DefaultButtonLabelConfig {
        @Category(name = "general")
        public final DefaultButtonLabels general = new DefaultButtonLabels();
    }

    public static final class DefaultButtonLabels {
        @Button(name = "refresh")
        public void refresh() {
        }
    }

    public static final class InvalidButtons {
        @Button(name = "bad")
        public void bad(String value) {
        }
    }

    public static final class Keybinds {
        @Option(name = "locked key")
        @Keybind(clearable = false)
        public int lockedKey = 34;

        @Option(name = "reset key")
        @Keybind(resetOnClear = true)
        public int resetKey = 54;
    }

    @Config(name = "color alpha")
    public static final class ColorAlphaConfig {
        @Category(name = "general")
        public final ColorAlpha general = new ColorAlpha();
    }

    public static final class ColorAlpha {
        @Option(name = "with alpha")
        @Color
        public int withAlpha = 0x8078A96B;

        @Option(name = "without alpha")
        @Color(alpha = false)
        public int withoutAlpha = 0xFF78A96B;
    }

    @Config(name = "offset range")
    public static final class OffsetRangeConfig {
        @Category(name = "general")
        public final OffsetRange general = new OffsetRange();
    }

    public static final class OffsetRange {
        @Option(name = "offset")
        @Range(min = 0.1D, max = 1.0D, step = 0.2D)
        public double offset = 0.1D;
    }

    @Config(name = "numeric")
    public static final class NumericConfig {
        @Category(name = "general")
        public final Numeric general = new Numeric();
    }

    public static final class Numeric {
        @Option(name = "int value")
        public int intValue = 1;

        @Option(name = "long value")
        public long longValue = 1L;

        @Option(name = "float value")
        public float floatValue = 1.0F;

        @Option(name = "double value")
        public double doubleValue = 1.0D;
    }

    @Config(name = "mutable default")
    public static final class MutableDefaultConfig {
        @Category(name = "general")
        public final MutableDefault general = new MutableDefault();
    }

    public static final class MutableDefault {
        @Option(name = "entries")
        public List<String> entries = new ArrayList<String>(Arrays.asList("one"));
    }

    @Config(name = "duplicate category")
    public static final class DuplicateCategoryConfig {
        @Category(name = "general")
        public final Object first = new Empty();

        @Category(name = "general!")
        public final Object second = new Empty();
    }

    @Config(name = "duplicate group")
    public static final class DuplicateGroupConfig {
        @Category(name = "general")
        public final DuplicateGroups general = new DuplicateGroups();
    }

    public static final class DuplicateGroups {
        @Group(name = "feature")
        public final SingleOption first = new SingleOption();

        @Group(name = "feature!")
        public final SingleOption second = new SingleOption();
    }

    @Config(name = "duplicate option")
    public static final class DuplicateOptionConfig {
        @Category(name = "general")
        public final DuplicateOptions general = new DuplicateOptions();
    }

    public static final class DuplicateOptions {
        @Option(name = "enabled")
        public boolean first = true;

        @Option(name = "enabled!")
        public boolean second = false;
    }

    public static final class SingleOption {
        @Option(name = "enabled")
        public boolean enabled = true;
    }

    public static final class Empty {
        @Option(name = "enabled")
        public boolean enabled = true;
    }
}
