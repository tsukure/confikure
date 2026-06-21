package re.tsuku.confikure.example;

import java.util.function.Consumer;
import java.util.function.Supplier;
import re.tsuku.confikure.gui.ConfigColorScheme;
import re.tsuku.confikure.gui.ConfigGui;
import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.OptionCondition;
import re.tsuku.fastbus.FastBus;

final class ExampleGui implements Consumer<ConfigGui> {
    private static final String[] READ_ONLY_OPTIONS = {"read-only-switch", "read-only-slider",
            "read-only-dropdown", "read-only-mode", "read-only-keybind", "read-only-color"};

    private final ExampleConfig config;
    private final FastBus events;

    ExampleGui(ExampleConfig config, FastBus events) {
        this.config = config;
        this.events = events;
    }

    public void accept(ConfigGui gui) {
        gui.themeSupplier(new SchemeThemeSupplier(config));
        gui.sidebarHeader(new ExampleSidebarHeader());
        configureVisibility(gui);
        configureReadOnlyOptions(gui);
        configureEvents(gui);
    }

    private void configureVisibility(ConfigGui gui) {
        ConfigOption dependentDensity = gui.definition().option("dependent-density");
        if (dependentDensity != null) {
            dependentDensity.visibleWhen(new DependentDensityVisibleCondition(config));
        }
    }

    private void configureReadOnlyOptions(ConfigGui gui) {
        for (int i = 0; i < READ_ONLY_OPTIONS.length; i++) {
            ConfigOption option = gui.definition().option(READ_ONLY_OPTIONS[i]);
            if (option != null) {
                option.enabledWhen(DisabledOptionCondition.INSTANCE);
            }
        }
    }

    private void configureEvents(ConfigGui gui) {
        ConfigOption scheme = gui.definition().option("theme-scheme");
        if (scheme != null) {
            scheme.addListener((option, oldValue, newValue) -> events.post(new ThemeChangedEvent(oldValue, newValue)));
        }
    }

    private static final class SchemeThemeSupplier implements Supplier<ConfigTheme> {
        private final ExampleConfig config;

        private SchemeThemeSupplier(ExampleConfig config) {
            this.config = config;
        }

        public ConfigTheme get() {
            return ConfigColorScheme.byDisplayName(config.visuals.theme.scheme).theme();
        }
    }

    private static final class ExampleSidebarHeader implements ConfigGui.SidebarHeader {
        public void render(GuiRenderer renderer, GuiBounds bounds, ConfigTheme theme) {
            renderer.text("confikure", bounds.x, bounds.y, theme.text);
            renderer.text("example mod", bounds.x, bounds.y + 12, theme.mutedText);
        }
    }

    private static final class DependentDensityVisibleCondition implements OptionCondition {
        private final ExampleConfig config;

        private DependentDensityVisibleCondition(ExampleConfig config) {
            this.config = config;
        }

        public boolean test() {
            return config.general.advanced.showDependent;
        }
    }

    private static final class DisabledOptionCondition implements OptionCondition {
        private static final DisabledOptionCondition INSTANCE = new DisabledOptionCondition();

        public boolean test() {
            return false;
        }
    }
}
