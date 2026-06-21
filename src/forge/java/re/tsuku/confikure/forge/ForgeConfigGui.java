package re.tsuku.confikure.forge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import re.tsuku.confikure.gui.ConfigGui;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.persistence.ConfigStore;

/**
 * forge 1.8.9 adapter for embedding a {@link ConfigGui} inside any screen.
 */
public final class ForgeConfigGui {
    private final ConfigDefinition definition;
    private final Path path;
    private final ConfigStore store;
    private final Consumer<ConfigGui> configurator;
    private ConfigGui gui;
    private ForgeGuiRenderer renderer;
    private boolean loaded;

    public ForgeConfigGui(ConfigDefinition definition, Path path, ConfigStore store) {
        this(definition, path, store, null);
    }

    public ForgeConfigGui(ConfigDefinition definition, Path path, ConfigStore store,
            Consumer<ConfigGui> configurator) {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.path = path;
        this.store = store == null ? new ConfigStore() : store;
        this.configurator = configurator;
    }

    public ConfigGui gui() {
        return gui;
    }

    public void init(Minecraft minecraft) {
        Objects.requireNonNull(minecraft, "minecraft");
        gui = new ConfigGui(definition);
        gui.keyNameProvider(new ConfigGui.KeyNameProvider() {
            public String name(int keyCode) {
                String name = Keyboard.getKeyName(keyCode);
                return name == null ? String.valueOf(keyCode) : name.toLowerCase();
            }
        });
        if (configurator != null) {
            configurator.accept(gui);
        }
        renderer = new ForgeGuiRenderer(minecraft);
        Keyboard.enableRepeatEvents(true);
        loadOnce();
    }

    public void render(Minecraft minecraft, int width, int height, int mouseX, int mouseY) {
        ensureInitialized(minecraft);
        if (Mouse.isButtonDown(0)) {
            gui.drag(width, height, rawMouseX(minecraft, width), rawMouseY(minecraft, height));
        }
        gui.render(renderer, width, height, mouseX, mouseY);
    }

    public void click(int width, int height, int mouseX, int mouseY) {
        gui.click(width, height, mouseX, mouseY);
    }

    public void drag(int width, int height, int mouseX, int mouseY) {
        gui.drag(width, height, mouseX, mouseY);
    }

    public void release() {
        gui.release();
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        boolean control = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        return gui.keyTyped(typedChar, keyCode, shift, control);
    }

    public void mouseWheel(int wheel) {
        if (wheel != 0) {
            gui.scroll(wheel < 0 ? 18 : -18);
        }
    }

    public void close() {
        Keyboard.enableRepeatEvents(false);
        if (path != null) {
            try {
                store.save(definition, path);
            } catch (IOException exception) {
                throw new IllegalStateException("unable to save config: " + path, exception);
            }
        }
    }

    private void loadOnce() {
        if (path == null || loaded) {
            return;
        }
        try {
            store.load(definition, path);
            loaded = true;
        } catch (IOException exception) {
            throw new IllegalStateException("unable to load config: " + path, exception);
        }
    }

    private void ensureInitialized(Minecraft minecraft) {
        if (gui == null || renderer == null) {
            init(minecraft);
        }
    }

    private static int rawMouseX(Minecraft minecraft, int width) {
        return Mouse.getX() * width / minecraft.displayWidth;
    }

    private static int rawMouseY(Minecraft minecraft, int height) {
        return height - Mouse.getY() * height / minecraft.displayHeight - 1;
    }
}
