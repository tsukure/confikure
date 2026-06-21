package re.tsuku.confikure.forge;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import re.tsuku.confikure.Confikure;
import re.tsuku.confikure.forge.internal.ClientTickScheduler;
import re.tsuku.confikure.gui.ConfigGui;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.persistence.ConfigStore;

/**
 * forge 1.8.9 entry points for opening confikure screens.
 */
public final class ForgeConfig {
    private ForgeConfig() {
    }

    /**
     * creates a config screen without persistence.
     */
    public static ForgeConfigScreen screen(Object config) {
        Objects.requireNonNull(config, "config");
        return new ForgeConfigScreen(Confikure.scan(config));
    }

    /**
     * creates a config screen persisted to a file.
     */
    public static ForgeConfigScreen screen(Object config, File file) {
        Objects.requireNonNull(file, "file");
        return screen(config, file.toPath());
    }

    /**
     * creates a config screen persisted to a path.
     */
    public static ForgeConfigScreen screen(Object config, Path path) {
        return screen(config, path, null);
    }

    /**
     * creates a persisted config screen with gui customization.
     */
    public static ForgeConfigScreen screen(Object config, File file, Consumer<ConfigGui> configurator) {
        Objects.requireNonNull(file, "file");
        return screen(config, file.toPath(), configurator);
    }

    /**
     * creates a persisted config screen with gui customization.
     */
    public static ForgeConfigScreen screen(Object config, Path path, Consumer<ConfigGui> configurator) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(path, "path");
        ConfigDefinition definition = Confikure.scan(config);
        return new ForgeConfigScreen(definition, path, new ConfigStore(), configurator);
    }

    /**
     * creates an embeddable config gui adapter persisted to a file.
     */
    public static ForgeConfigGui gui(Object config, File file, Consumer<ConfigGui> configurator) {
        Objects.requireNonNull(file, "file");
        return gui(config, file.toPath(), configurator);
    }

    /**
     * creates an embeddable config gui adapter persisted to a path.
     */
    public static ForgeConfigGui gui(Object config, Path path, Consumer<ConfigGui> configurator) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(path, "path");
        return new ForgeConfigGui(Confikure.scan(config), path, new ConfigStore(), configurator);
    }

    /**
     * opens a config screen without persistence.
     */
    public static void open(Object config) {
        Objects.requireNonNull(config, "config");
        ClientTickScheduler.schedule(() -> openNow(screen(config)));
    }

    /**
     * opens a persisted config screen.
     */
    public static void open(Object config, File file) {
        Objects.requireNonNull(file, "file");
        open(config, file.toPath());
    }

    /**
     * opens a persisted config screen.
     */
    public static void open(Object config, Path path) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(path, "path");
        ClientTickScheduler.schedule(() -> openNow(screen(config, path)));
    }

    /**
     * opens a persisted config screen with gui customization.
     */
    public static void open(Object config, File file, Consumer<ConfigGui> configurator) {
        Objects.requireNonNull(file, "file");
        open(config, file.toPath(), configurator);
    }

    /**
     * opens a persisted config screen with gui customization.
     */
    public static void open(Object config, Path path, Consumer<ConfigGui> configurator) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(path, "path");
        ClientTickScheduler.schedule(() -> openNow(screen(config, path, configurator)));
    }

    /**
     * opens an already-created config screen.
     */
    public static void open(ForgeConfigScreen screen) {
        Objects.requireNonNull(screen, "screen");
        ClientTickScheduler.schedule(() -> openNow(screen));
    }

    /**
     * displays a config screen immediately on the current client thread.
     */
    public static void openNow(ForgeConfigScreen screen) {
        Objects.requireNonNull(screen, "screen");
        Minecraft.getMinecraft().displayGuiScreen(screen);
    }
}
