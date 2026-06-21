package re.tsuku.confikure.example;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import re.tsuku.confikure.forge.ForgeConfig;
import re.tsuku.fastbus.FastBus;

final class OpenConfigCommand extends CommandBase {
    private final ExampleConfig config;
    private final FastBus events;

    OpenConfigCommand(ExampleConfig config, FastBus events) {
        this.config = config;
        this.events = events;
    }

    public String getCommandName() {
        return "confikure";
    }

    public String getCommandUsage(ICommandSender sender) {
        return "/confikure";
    }

    public int getRequiredPermissionLevel() {
        return 0;
    }

    public void processCommand(ICommandSender sender, String[] args) {
        File file = new File(new File(Minecraft.getMinecraft().mcDataDir, "config"), "confikure-example.json");
        ForgeConfig.open(config, file, new ExampleGui(config, events));
    }
}
