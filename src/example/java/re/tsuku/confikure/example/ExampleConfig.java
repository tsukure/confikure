package re.tsuku.confikure.example;

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

@Config(name = "confikure example", id = "confikure-example", description = "example mod config")
public final class ExampleConfig {
    @Category(name = "general", order = 0)
    public final General general = new General();

    @Category(name = "visuals", order = 1)
    public final Visuals visuals = new Visuals();

    public static final class General {
        @Group(name = "feature", description = "basic toggles and selectors", order = 0)
        public final Feature feature = new Feature();

        @Group(name = "movement", description = "numbers and actions", order = 1)
        public final Movement movement = new Movement();

        @Group(name = "advanced", description = "extra rows for scroll testing", order = 2)
        public final Advanced advanced = new Advanced();
    }

    public static final class Movement {
        @Option(name = "speed", description = "drag or type a value", order = 0)
        @Range(min = 0.0D, max = 3.0D, step = 0.25D)
        public double speed = 1.0D;

        @Button(name = "reset speed", description = "sets speed back to one", order = 1)
        public void resetSpeed() {
            speed = 1.0D;
        }
    }

    public static final class Advanced {
        @Option(name = "density", description = "small stepped number", order = 0)
        @Range(min = 0.0D, max = 10.0D, step = 1.0D)
        public int density = 4;

        @Option(name = "opacity", description = "percentage-style value", order = 1)
        @Range(min = 0.0D, max = 1.0D, step = 0.05D)
        public double opacity = 0.75D;

        @Option(name = "offset x", description = "signed test value", order = 2)
        @Range(min = -100.0D, max = 100.0D, step = 1.0D)
        public int offsetX = 12;

        @Option(name = "offset y", description = "signed test value", order = 3)
        @Range(min = -100.0D, max = 100.0D, step = 1.0D)
        public int offsetY = -8;

        @Option(name = "notifications", description = "another switch row", order = 4)
        public boolean notifications = true;

        @Option(name = "style", description = "another dropdown row", order = 5)
        @Dropdown(values = {"compact", "normal", "wide"})
        public String style = "normal";

        @Option(name = "show dependent", description = "reveals another setting", order = 6)
        public boolean showDependent = false;

        @Option(name = "dependent density", description = "hidden until enabled above", order = 7)
        @Range(min = 0.0D, max = 10.0D, step = 1.0D)
        public int dependentDensity = 5;
    }

    public static final class Feature {
        @Option(name = "enabled", description = "basic boolean switch", order = 0)
        public boolean enabled = true;

        @Option(name = "dropdown", description = "opens a real option list", order = 1)
        @Dropdown(values = {"simple", "detailed", "debug"})
        public String dropdown = "simple";

        @Option(name = "mode", description = "cycles on click", order = 2)
        @Mode(values = {"simple", "detailed", "debug"})
        public String mode = "simple";

        @Option(name = "hotkey", description = "click then press a key", order = 3)
        @Keybind(resetOnClear = true)
        public int hotkey = 54;

        @Option(name = "optional hotkey", description = "x clears to none", order = 4)
        @Keybind
        public int optionalHotkey = 0;
    }

    public static final class Visuals {
        @Group(name = "theme", description = "color and text controls", order = 0)
        public final Theme theme = new Theme();

        @Group(name = "preview", description = "read-only display values", order = 1)
        public final Preview preview = new Preview();
    }

    public static final class Theme {
        @Option(name = "theme scheme", description = "changes the gui colors", order = 0)
        @Dropdown(values = {"minecraft", "catppuccin mocha", "ayu mirage"})
        public String scheme = "minecraft";

        @Option(name = "accent", description = "opens a color picker", order = 1)
        @Color
        public int accent = 0xFF78A96B;

        @Option(name = "solid color", description = "color without alpha", order = 2)
        @Color(alpha = false)
        public int solidColor = 0xFF4F8BC9;

        @Option(name = "title", description = "simple text input", order = 3)
        public String title = "confikure";

        @Option(name = "notes", description = "multiline text input", order = 4)
        @Multiline
        public String notes = "hello from loom";
    }

    public static final class Preview {
        @Option(name = "description", order = 0)
        @Info
        public String description = "plain description text can live in the option list without a value control.";

        @Option(name = "read only switch", description = "disabled boolean example", order = 1)
        public boolean readOnlySwitch = true;

        @Option(name = "read only slider", description = "disabled slider example", order = 2)
        @Range(min = 0.0D, max = 1.0D, step = 0.1D)
        public double readOnlySlider = 0.7D;

        @Option(name = "read only dropdown", description = "disabled dropdown example", order = 3)
        @Dropdown(values = {"simple", "detailed", "debug"})
        public String readOnlyDropdown = "detailed";

        @Option(name = "read only mode", description = "disabled mode example", order = 4)
        @Mode(values = {"simple", "detailed", "debug"})
        public String readOnlyMode = "debug";

        @Option(name = "read only keybind", description = "disabled keybind example", order = 5)
        @Keybind
        public int readOnlyKeybind = 54;

        @Option(name = "read only color", description = "disabled color example", order = 6)
        @Color(alpha = false)
        public int readOnlyColor = 0xFFAA8844;
    }
}
