package re.tsuku.confikure.gui;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import re.tsuku.confikure.gui.platform.GuiRenderer;

public final class GuiPrimitivesTest {
    @Test
    public void multilineTextFieldWrapsLongLines() {
        TestRenderer renderer = new TestRenderer();

        GuiPrimitives.textField(renderer, new ConfigTheme(), new GuiBounds(0, 0, 28, 42), "abcdefg", 0, 0, 0,
                false, false, true, true);

        assertEquals(3, renderer.text.size());
        assertEquals("abc", renderer.text.get(0));
        assertEquals("def", renderer.text.get(1));
        assertEquals("g", renderer.text.get(2));
    }

    @Test
    public void multilineTextFieldPreservesExplicitNewlines() {
        TestRenderer renderer = new TestRenderer();

        GuiPrimitives.textField(renderer, new ConfigTheme(), new GuiBounds(0, 0, 80, 42), "alpha\nbeta", 0, 0, 0,
                false, false, true, true);

        assertEquals(2, renderer.text.size());
        assertEquals("alpha", renderer.text.get(0));
        assertEquals("beta", renderer.text.get(1));
    }

    @Test
    public void focusedSingleLineTextFieldScrollsToCursor() {
        TestRenderer renderer = new TestRenderer();

        GuiPrimitives.textField(renderer, new ConfigTheme(), new GuiBounds(0, 0, 28, 18), "abcdef", 6, 6, 6, false,
                true, true, false);

        assertEquals(1, renderer.text.size());
        assertEquals("def", renderer.text.get(0));
    }

    private static final class TestRenderer implements GuiRenderer {
        private final List<String> text = new ArrayList<String>();

        public void fill(int left, int top, int right, int bottom, int color) {
        }

        public void text(String text, int x, int y, int color) {
            this.text.add(text);
        }

        public void centeredText(String text, int x, int y, int width, int color) {
        }

        public int textWidth(String text) {
            return text.length() * 6;
        }

        public int fontHeight() {
            return 9;
        }

        public void pushClip(int x, int y, int width, int height) {
        }

        public void popClip() {
        }
    }
}
