package re.tsuku.confikure.dev;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import re.tsuku.confikure.forge.ConfikureForge;

final class ConfikureDevCommand extends CommandBase {
    private final ConfikureDevMod.DevConfig config;

    ConfikureDevCommand(ConfikureDevMod.DevConfig config) {
        this.config = config;
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
        File file = new File(new File(Minecraft.getMinecraft().mcDataDir, "config"), "confikure-dev.json");
        ConfikureForge.open(config, file.toPath(), new DevGuiConfigurator(config));
    }
}
