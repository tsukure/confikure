package re.tsuku.confikure.gui.platform;

public final class GuiInput {
    private final int mouseX;
    private final int mouseY;
    private final boolean mouseDown;

    public GuiInput(int mouseX, int mouseY, boolean mouseDown) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.mouseDown = mouseDown;
    }

    public int mouseX() {
        return mouseX;
    }

    public int mouseY() {
        return mouseY;
    }

    public boolean mouseDown() {
        return mouseDown;
    }
}
