package re.tsuku.confikure.example;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import re.tsuku.fastbus.FastBus;

@Mod(modid = "confikure-example", name = "confikure example", version = "example", clientSideOnly = true)
public final class ExampleMod {
    static final Logger LOGGER = LogManager.getLogger("confikure-example");
    private static final ExampleConfig CONFIG = new ExampleConfig();
    private static final FastBus EVENTS = new FastBus();

    @EventHandler
    public void init(FMLInitializationEvent event) {
        EVENTS.subscribe(new ExampleEvents());
        ClientCommandHandler.instance.registerCommand(new OpenConfigCommand(CONFIG, EVENTS));
    }
}
