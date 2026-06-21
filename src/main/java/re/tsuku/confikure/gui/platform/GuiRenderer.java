package re.tsuku.confikure.gui.platform;

/**
 * minimal renderer contract used by the platform-independent gui.
 */
public interface GuiRenderer {
    /**
     * fills a rectangle with an argb color.
     */
    void fill(int left, int top, int right, int bottom, int color);

    /**
     * fills a rectangle that may require alpha blending support from the platform renderer.
     */
    default void blendedFill(int left, int top, int right, int bottom, int color) {
        fill(left, top, right, bottom, color);
    }

    /**
     * draws left-aligned text.
     */
    void text(String text, int x, int y, int color);

    /**
     * draws centered text inside a fixed-width region.
     */
    void centeredText(String text, int x, int y, int width, int color);

    /**
     * returns rendered text width in pixels.
     */
    int textWidth(String text);

    /**
     * returns font height in pixels.
     */
    int fontHeight();

    /**
     * pushes a clipping rectangle.
     */
    void pushClip(int x, int y, int width, int height);

    /**
     * pops the active clipping rectangle.
     */
    void popClip();
}
