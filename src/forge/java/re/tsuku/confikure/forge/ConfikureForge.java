package re.tsuku.confikure.forge;

import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import re.tsuku.confikure.Confikure;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.persistence.ConfigStore;

/**
 * forge 1.8.9 entry points for opening confikure screens.
 */
public final class ConfikureForge {
    private static final TickOpener TICK_OPENER = new TickOpener();
    private static boolean registered;

    private ConfikureForge() {
    }

    public static ConfikureForgeScreen screen(Object config) {
        return new ConfikureForgeScreen(Confikure.scan(config));
    }

    public static ConfikureForgeScreen screen(Object config, Path path) {
        ConfigDefinition definition = Confikure.scan(config);
        return new ConfikureForgeScreen(definition, path, new ConfigStore());
    }

    public static void open(Object config) {
        open(screen(config));
    }

    public static void open(Object config, Path path) {
        open(screen(config, path));
    }

    public static void open(ConfikureForgeScreen screen) {
        ensureRegistered();
        TICK_OPENER.screen = screen;
    }

    public static void openNow(ConfikureForgeScreen screen) {
        Minecraft.getMinecraft().displayGuiScreen(screen);
    }

    private static void ensureRegistered() {
        if (registered) {
            return;
        }
        MinecraftForge.EVENT_BUS.register(TICK_OPENER);
        registered = true;
    }

    private static final class TickOpener {
        private GuiScreen screen;

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END || screen == null) {
                return;
            }
            Minecraft.getMinecraft().displayGuiScreen(screen);
            screen = null;
        }
    }
}
