package re.tsuku.confikure.forge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import re.tsuku.confikure.gui.ConfigGui;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.persistence.ConfigStore;

/**
 * forge 1.8.9 screen wrapper for a scanned confikure config.
 */
public final class ForgeConfigScreen extends GuiScreen {
    private final ForgeConfigGui gui;

    public ForgeConfigScreen(ConfigDefinition definition) {
        this(definition, null, null);
    }

    public ForgeConfigScreen(ConfigDefinition definition, Path path, ConfigStore store) {
        this(definition, path, store, null);
    }

    public ForgeConfigScreen(ConfigDefinition definition, Path path, ConfigStore store,
            Consumer<ConfigGui> configurator) {
        this.gui = new ForgeConfigGui(definition, path, store, configurator);
    }

    public void initGui() {
        gui.init(mc);
        gui.gui().closeHandler(new Runnable() {
            public void run() {
                mc.displayGuiScreen(null);
            }
        });
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ensureInitialized();
        drawDefaultBackground();
        gui.render(mc, width, height, mouseX, mouseY);
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
        if (gui.keyTyped(typedChar, keyCode)) {
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
        gui.mouseWheel(Mouse.getEventDWheel());
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        gui.close();
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    private void ensureInitialized() {
        if (gui.gui() == null) {
            initGui();
        }
    }
}
