package re.tsuku.confikure.forge;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import re.tsuku.confikure.gui.platform.GuiRenderer;

/**
 * forge 1.8.9 implementation of the core gui renderer.
 */
public final class ForgeGuiRenderer implements GuiRenderer {
    private final Minecraft minecraft;
    private final Deque<int[]> clips = new ArrayDeque<int[]>();

    /**
     * creates a renderer backed by the given minecraft client.
     */
    public ForgeGuiRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void fill(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left, top, right, bottom, color);
    }

    public void blendedFill(int left, int top, int right, int bottom, int color) {
        boolean alphaTest = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        if (alphaTest) {
            GlStateManager.disableAlpha();
        }
        fill(left, top, right, bottom, color);
        if (alphaTest) {
            GlStateManager.enableAlpha();
        }
    }

    public void text(String text, int x, int y, int color) {
        font().drawStringWithShadow(text, x, y, color);
    }

    public void centeredText(String text, int x, int y, int width, int color) {
        text(text, x + (width - textWidth(text)) / 2, y, color);
    }

    public int textWidth(String text) {
        return font().getStringWidth(text);
    }

    public int fontHeight() {
        return font().FONT_HEIGHT;
    }

    public void pushClip(int x, int y, int width, int height) {
        clips.push(new int[]{x, y, width, height});
        applyClip(x, y, width, height);
    }

    public void popClip() {
        if (!clips.isEmpty()) {
            clips.pop();
        }
        if (clips.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            return;
        }
        int[] clip = clips.peek();
        applyClip(clip[0], clip[1], clip[2], clip[3]);
    }

    private FontRenderer font() {
        return minecraft.fontRendererObj;
    }

    private void applyClip(int x, int y, int width, int height) {
        ScaledResolution resolution = new ScaledResolution(minecraft);
        int scale = resolution.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, minecraft.displayHeight - (y + height) * scale, width * scale, height * scale);
    }
}
