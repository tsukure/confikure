package re.tsuku.confikure.dev;

import java.util.function.Consumer;
import java.util.function.Supplier;
import re.tsuku.confikure.gui.ConfigColorScheme;
import re.tsuku.confikure.gui.ConfigGui;
import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.OptionCondition;

final class DevGuiConfigurator implements Consumer<ConfigGui> {
    private static final String[] READ_ONLY_OPTIONS = {"read-only-switch", "read-only-slider",
            "read-only-dropdown", "read-only-mode", "read-only-keybind", "read-only-color"};

    private final ConfikureDevMod.DevConfig config;

    DevGuiConfigurator(ConfikureDevMod.DevConfig config) {
        this.config = config;
    }

    public void accept(ConfigGui gui) {
        gui.themeSupplier(new SchemeThemeSupplier(config));
        gui.sidebarHeader(new DevSidebarHeader());
        configureDevOptions(gui);
    }

    private void configureDevOptions(ConfigGui gui) {
        ConfigOption dependentDensity = gui.definition().option("dependent-density");
        if (dependentDensity != null) {
            dependentDensity.visibleWhen(new DependentDensityVisibleCondition(config));
        }
        for (int i = 0; i < READ_ONLY_OPTIONS.length; i++) {
            ConfigOption option = gui.definition().option(READ_ONLY_OPTIONS[i]);
            if (option != null) {
                option.enabledWhen(DisabledOptionCondition.INSTANCE);
            }
        }
    }

    private static final class SchemeThemeSupplier implements Supplier<ConfigTheme> {
        private final ConfikureDevMod.DevConfig config;

        private SchemeThemeSupplier(ConfikureDevMod.DevConfig config) {
            this.config = config;
        }

        public ConfigTheme get() {
            return ConfigColorScheme.byDisplayName(config.visuals.theme.scheme).theme();
        }
    }

    private static final class DevSidebarHeader implements ConfigGui.SidebarHeader {
        public void render(GuiRenderer renderer, GuiBounds bounds, ConfigTheme theme) {
            renderer.text("confikure", bounds.x, bounds.y, theme.text);
            renderer.text("dev preview", bounds.x, bounds.y + 12, theme.mutedText);
        }
    }

    private static final class DependentDensityVisibleCondition implements OptionCondition {
        private final ConfikureDevMod.DevConfig config;

        private DependentDensityVisibleCondition(ConfikureDevMod.DevConfig config) {
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
