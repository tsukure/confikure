package re.tsuku.confikure.gui;

/**
 * immutable integer rectangle used by gui layout and render callbacks.
 */
public final class GuiBounds {
    /**
     * left edge in screen coordinates.
     */
    public final int x;
    /**
     * top edge in screen coordinates.
     */
    public final int y;
    /**
     * rectangle width.
     */
    public final int width;
    /**
     * rectangle height.
     */
    public final int height;

    /**
     * creates a bounds rectangle.
     */
    public GuiBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * returns whether a point is inside this rectangle.
     */
    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
