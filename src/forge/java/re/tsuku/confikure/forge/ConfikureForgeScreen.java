package re.tsuku.confikure.forge;

import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import re.tsuku.confikure.gui.ConfigGui;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.persistence.ConfigStore;

/**
 * forge 1.8.9 screen wrapper for a scanned confikure config.
 */
public final class ConfikureForgeScreen extends GuiScreen {
    private final ConfigDefinition definition;
    private final Path path;
    private final ConfigStore store;
    private ConfigGui gui;
    private ForgeGuiRenderer renderer;
    private boolean loaded;

    public ConfikureForgeScreen(ConfigDefinition definition) {
        this(definition, null, null);
    }

    public ConfikureForgeScreen(ConfigDefinition definition, Path path, ConfigStore store) {
        this.definition = definition;
        this.path = path;
        this.store = store == null ? new ConfigStore() : store;
    }

    public void initGui() {
        gui = new ConfigGui(definition);
        gui.keyNameProvider(new ConfigGui.KeyNameProvider() {
            public String name(int keyCode) {
                String name = Keyboard.getKeyName(keyCode);
                return name == null ? String.valueOf(keyCode) : name.toLowerCase();
            }
        });
        renderer = new ForgeGuiRenderer(mc);
        Keyboard.enableRepeatEvents(true);
        if (path != null && !loaded) {
            try {
                store.load(definition, path);
                loaded = true;
            } catch (IOException exception) {
                throw new IllegalStateException("unable to load config: " + path, exception);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ensureInitialized();
        if (Mouse.isButtonDown(0)) {
            gui.drag(width, height, rawMouseX(), rawMouseY());
        }
        drawDefaultBackground();
        gui.render(renderer, width, height, mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ensureInitialized();
        if (mouseButton == 0) {
            gui.click(width, height, mouseX, mouseY);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        ensureInitialized();
        if (clickedMouseButton == 0) {
            gui.drag(width, height, mouseX, mouseY);
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        ensureInitialized();
        if (state == 0) {
            gui.release();
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        ensureInitialized();
        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        boolean control = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        if (gui.keyTyped(typedChar, keyCode, shift, control)) {
            return;
        }
        if (keyCode == 1) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    public void handleMouseInput() throws IOException {
        ensureInitialized();
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            gui.scroll(wheel < 0 ? 18 : -18);
        }
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        if (path != null) {
            try {
                store.save(definition, path);
            } catch (IOException exception) {
                throw new IllegalStateException("unable to save config: " + path, exception);
            }
        }
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    private void ensureInitialized() {
        if (gui == null || renderer == null) {
            initGui();
        }
    }

    private int rawMouseX() {
        return Mouse.getX() * width / mc.displayWidth;
    }

    private int rawMouseY() {
        return height - Mouse.getY() * height / mc.displayHeight - 1;
    }
}
