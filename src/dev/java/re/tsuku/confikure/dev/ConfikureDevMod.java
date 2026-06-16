package re.tsuku.confikure.dev;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
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
import re.tsuku.confikure.forge.ConfikureForge;
import re.tsuku.confikure.gui.ConfigGui;
import re.tsuku.confikure.gui.ConfigTheme;
import re.tsuku.confikure.gui.GuiBounds;
import re.tsuku.confikure.gui.platform.GuiRenderer;

@Mod(modid = "confikure-dev", name = "confikure dev", version = "dev", clientSideOnly = true)
public final class ConfikureDevMod {
    private static final DevConfig CONFIG = new DevConfig();

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new CommandBase() {
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
                ConfikureForge.open(CONFIG, file.toPath(), new java.util.function.Consumer<ConfigGui>() {
                    public void accept(ConfigGui gui) {
                        gui.themePreviews(true);
                        gui.sidebarHeader(new ConfigGui.SidebarHeader() {
                            public void render(GuiRenderer renderer, GuiBounds bounds, ConfigTheme theme) {
                                renderer.fill(bounds.x, bounds.y, bounds.x + 16, bounds.y + 16, theme.accentDark);
                                renderer.fill(bounds.x + 3, bounds.y + 3, bounds.x + 13, bounds.y + 13, theme.accent);
                                renderer.text("confikure", bounds.x + 22, bounds.y, theme.text);
                                renderer.text("dev preview", bounds.x + 22, bounds.y + 12, theme.mutedText);
                            }
                        });
                    }
                });
            }
        });
    }

    @Config(name = "confikure dev", id = "confikure-dev", description = "local development config")
    public static final class DevConfig {
        @Category(name = "general", order = 0)
        public final General general = new General();

        @Category(name = "visuals", order = 1)
        public final Visuals visuals = new Visuals();
    }

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
        @Option(name = "accent", description = "opens a color picker", order = 0)
        @Color
        public int accent = 0xFF78A96B;

        @Option(name = "title", description = "simple text input", order = 1)
        public String title = "confikure";

        @Option(name = "notes", description = "multiline text input", order = 2)
        @Multiline
        public String notes = "hello from loom";
    }

    public static final class Preview {
        @Option(name = "info", description = "read-only value example", order = 0)
        @Info
        public String info = "read-only example";
    }
}
