package re.tsuku.confikure.forge;

import java.nio.file.Path;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import re.tsuku.confikure.Confikure;
import re.tsuku.confikure.forge.event.ConfikureScreenOpenEvent;
import re.tsuku.confikure.forge.task.DelayedTaskHandler;
import re.tsuku.confikure.gui.ConfigGui;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.persistence.ConfigStore;

/**
 * forge 1.8.9 entry points for opening confikure screens.
 */
public final class ConfikureForge {
    private static boolean initialized;

    private ConfikureForge() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        Confikure.eventBus().subscribe(DelayedTaskHandler.get());
        initialized = true;
    }

    public static ConfikureForgeScreen screen(Object config) {
        return new ConfikureForgeScreen(Confikure.scan(config));
    }

    public static ConfikureForgeScreen screen(Object config, Path path) {
        return screen(config, path, null);
    }

    public static ConfikureForgeScreen screen(Object config, Path path, Consumer<ConfigGui> configurator) {
        ConfigDefinition definition = Confikure.scan(config);
        return new ConfikureForgeScreen(definition, path, new ConfigStore(), configurator);
    }

    public static ConfikureForgeGui gui(Object config, Path path, Consumer<ConfigGui> configurator) {
        return new ConfikureForgeGui(Confikure.scan(config), path, new ConfigStore(), configurator);
    }

    public static void open(Object config) {
        init();
        DelayedTaskHandler.schedule(1, () -> openNow(screen(config)));
    }

    public static void open(Object config, Path path) {
        init();
        DelayedTaskHandler.schedule(1, () -> openNow(screen(config, path)));
    }

    public static void open(Object config, Path path, Consumer<ConfigGui> configurator) {
        init();
        DelayedTaskHandler.schedule(1, () -> openNow(screen(config, path, configurator)));
    }

    public static void open(ConfikureForgeScreen screen) {
        init();
        DelayedTaskHandler.schedule(1, () -> openNow(screen));
    }

    public static void openNow(ConfikureForgeScreen screen) {
        init();
        Confikure.eventBus().post(new ConfikureScreenOpenEvent(screen));
        Minecraft.getMinecraft().displayGuiScreen(screen);
    }
}
