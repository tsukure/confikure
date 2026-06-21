package re.tsuku.confikure.forge;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import re.tsuku.confikure.Confikure;
import re.tsuku.confikure.forge.internal.ForgeEventBus;
import re.tsuku.confikure.forge.internal.task.DelayedTaskHandler;
import re.tsuku.confikure.gui.ConfigGui;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.persistence.ConfigStore;

/**
 * forge 1.8.9 entry points for opening confikure screens.
 */
public final class ForgeConfig {
    private static boolean initialized;

    private ForgeConfig() {
    }

    static void init() {
        if (initialized) {
            return;
        }
        ForgeEventBus.subscribe(DelayedTaskHandler.get());
        initialized = true;
    }

    public static ForgeConfigScreen screen(Object config) {
        return new ForgeConfigScreen(Confikure.scan(config));
    }

    public static ForgeConfigScreen screen(Object config, File file) {
        return screen(config, file.toPath());
    }

    public static ForgeConfigScreen screen(Object config, Path path) {
        return screen(config, path, null);
    }

    public static ForgeConfigScreen screen(Object config, File file, Consumer<ConfigGui> configurator) {
        return screen(config, file.toPath(), configurator);
    }

    public static ForgeConfigScreen screen(Object config, Path path, Consumer<ConfigGui> configurator) {
        ConfigDefinition definition = Confikure.scan(config);
        return new ForgeConfigScreen(definition, path, new ConfigStore(), configurator);
    }

    public static ForgeConfigGui gui(Object config, File file, Consumer<ConfigGui> configurator) {
        return gui(config, file.toPath(), configurator);
    }

    public static ForgeConfigGui gui(Object config, Path path, Consumer<ConfigGui> configurator) {
        return new ForgeConfigGui(Confikure.scan(config), path, new ConfigStore(), configurator);
    }

    public static void open(Object config) {
        init();
        DelayedTaskHandler.schedule(0, () -> openNow(screen(config)));
    }

    public static void open(Object config, File file) {
        open(config, file.toPath());
    }

    public static void open(Object config, Path path) {
        init();
        DelayedTaskHandler.schedule(0, () -> openNow(screen(config, path)));
    }

    public static void open(Object config, File file, Consumer<ConfigGui> configurator) {
        open(config, file.toPath(), configurator);
    }

    public static void open(Object config, Path path, Consumer<ConfigGui> configurator) {
        init();
        DelayedTaskHandler.schedule(0, () -> openNow(screen(config, path, configurator)));
    }

    public static void open(ForgeConfigScreen screen) {
        init();
        DelayedTaskHandler.schedule(0, () -> openNow(screen));
    }

    public static void openNow(ForgeConfigScreen screen) {
        init();
        ForgeEventBus.postScreenOpen(screen);
        Minecraft.getMinecraft().displayGuiScreen(screen);
    }
}
