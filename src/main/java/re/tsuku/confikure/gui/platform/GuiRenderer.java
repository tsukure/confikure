package re.tsuku.confikure.gui.platform;

public interface GuiRenderer {
    void fill(int left, int top, int right, int bottom, int color);

    void text(String text, int x, int y, int color);

    void centeredText(String text, int x, int y, int width, int color);

    int textWidth(String text);

    int fontHeight();

    void pushClip(int x, int y, int width, int height);

    void popClip();
}
