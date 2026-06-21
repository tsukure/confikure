package re.tsuku.confikure.gui.input;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import re.tsuku.confikure.gui.input.TextInputState.KeyResult;
import re.tsuku.confikure.gui.platform.GuiRenderer;

public final class TextInputStateTest {
    @Test
    public void controlBackspaceDeletesPreviousWord() {
        TextInputState state = new TextInputState();
        state.text("hello brave world");

        assertEquals(KeyResult.CHANGED, state.keyTyped('\0', 14, false, true, false));

        assertEquals("hello brave ", state.text());
        assertEquals(state.text().length(), state.cursor());
    }

    @Test
    public void controlDeleteDeletesNextWord() {
        TextInputState state = new TextInputState();
        state.text("hello brave world");
        state.keyTyped('\0', 199, false, false, false);

        assertEquals(KeyResult.CHANGED, state.keyTyped('\0', 211, false, true, false));

        assertEquals(" brave world", state.text());
        assertEquals(0, state.cursor());
    }

    @Test
    public void controlShiftArrowSelectsByWord() {
        TextInputState state = new TextInputState();
        state.text("hello brave world");
        state.keyTyped('\0', 199, false, false, false);

        assertEquals(KeyResult.USED, state.keyTyped('\0', 205, true, true, false));

        assertEquals(0, state.selectionStart());
        assertEquals(5, state.selectionEnd());
    }

    @Test
    public void controlDeleteReplacesActiveSelection() {
        TextInputState state = new TextInputState();
        state.text("hello brave world");
        state.keyTyped('\0', 199, false, false, false);
        state.keyTyped('\0', 205, true, true, false);

        assertEquals(KeyResult.CHANGED, state.keyTyped('\0', 211, false, true, false));

        assertEquals(" brave world", state.text());
        assertEquals(0, state.cursor());
        assertEquals(0, state.selectionStart());
        assertEquals(0, state.selectionEnd());
    }

    @Test
    public void controlArrowCrossesWhitespaceAndClampsAtBoundaries() {
        TextInputState state = new TextInputState();
        state.text("hello   brave");
        state.keyTyped('\0', 199, false, false, false);

        state.keyTyped('\0', 205, false, true, false);
        assertEquals(5, state.cursor());
        state.keyTyped('\0', 205, false, true, false);
        assertEquals(13, state.cursor());
        state.keyTyped('\0', 205, false, true, false);
        assertEquals(13, state.cursor());

        state.keyTyped('\0', 203, false, true, false);
        assertEquals(8, state.cursor());
        state.keyTyped('\0', 203, false, true, false);
        assertEquals(0, state.cursor());
        state.keyTyped('\0', 203, false, true, false);
        assertEquals(0, state.cursor());
    }

    @Test
    public void selectAtExtendsSelectionFromExistingAnchor() {
        TextInputState state = new TextInputState();
        state.text("hello world");
        TestRenderer renderer = new TestRenderer();

        state.cursorAt(renderer, state.text(), 0, 0);
        state.selectAt(renderer, state.text(), 0, 60);

        assertEquals(0, state.selectionStart());
        assertEquals(10, state.selectionEnd());
    }

    private static final class TestRenderer implements GuiRenderer {
        public void fill(int left, int top, int right, int bottom, int color) {
        }

        public void text(String text, int x, int y, int color) {
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
