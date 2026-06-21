package re.tsuku.confikure.gui.platform;

/**
 * immutable mouse input snapshot for gui integrations.
 */
public final class GuiInput {
    private final int mouseX;
    private final int mouseY;
    private final boolean mouseDown;

    public GuiInput(int mouseX, int mouseY, boolean mouseDown) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.mouseDown = mouseDown;
    }

    /**
     * returns the mouse x coordinate.
     */
    public int mouseX() {
        return mouseX;
    }

    /**
     * returns the mouse y coordinate.
     */
    public int mouseY() {
        return mouseY;
    }

    /**
     * returns whether the primary mouse button is down.
     */
    public boolean mouseDown() {
        return mouseDown;
    }
}
