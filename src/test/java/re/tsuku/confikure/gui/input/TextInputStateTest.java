package re.tsuku.confikure.gui.input;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import re.tsuku.confikure.gui.input.TextInputState.KeyResult;
import re.tsuku.confikure.gui.platform.ClipboardAccess;
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

    @Test
    public void insertReplacesSelectionWithinMaxLength() {
        TextInputState state = new TextInputState();
        state.text("abcdef");
        state.maxLength(6);
        state.cursorAt(new TestRenderer(), state.text(), 0, 0);
        state.keyTyped('\0', 205, true, false, false);
        state.keyTyped('\0', 205, true, false, false);

        assertEquals(KeyResult.CHANGED, state.keyTyped('x', 0, false, false, false));

        assertEquals("xcdef", state.text());
        assertEquals(1, state.cursor());
    }

    @Test
    public void copyCutAndPasteUseSelection() {
        TextInputState state = new TextInputState();
        MemoryClipboard clipboard = new MemoryClipboard();
        state.text("hello");

        assertEquals(KeyResult.USED, state.keyTyped('\0', 30, false, true, false, clipboard));
        assertEquals(KeyResult.USED, state.keyTyped('\0', 46, false, true, false, clipboard));
        assertEquals("hello", clipboard.text);

        assertEquals(KeyResult.CHANGED, state.keyTyped('\0', 45, false, true, false, clipboard));
        assertEquals("", state.text());

        clipboard.text = "world";
        assertEquals(KeyResult.CHANGED, state.keyTyped('\0', 47, false, true, false, clipboard));
        assertEquals("world", state.text());
    }

    @Test
    public void pasteFiltersNewlinesForSingleLineInputs() {
        TextInputState state = new TextInputState();
        MemoryClipboard clipboard = new MemoryClipboard();
        clipboard.text = "hello\nworld";

        assertEquals(KeyResult.CHANGED, state.keyTyped('\0', 47, false, true, false, clipboard));

        assertEquals("hello world", state.text());
    }

    @Test
    public void pastePreservesNewlinesForMultilineInputs() {
        TextInputState state = new TextInputState();
        MemoryClipboard clipboard = new MemoryClipboard();
        clipboard.text = "hello\nworld";

        assertEquals(KeyResult.CHANGED, state.keyTyped('\0', 47, false, true, true, clipboard));

        assertEquals("hello\nworld", state.text());
    }

    @Test
    public void pasteTreatsCarriageReturnsAsNewlines() {
        TextInputState singleLine = new TextInputState();
        TextInputState multiline = new TextInputState();
        MemoryClipboard clipboard = new MemoryClipboard();
        clipboard.text = "hello\rworld";

        assertEquals(KeyResult.CHANGED, singleLine.keyTyped('\0', 47, false, true, false, clipboard));
        assertEquals(KeyResult.CHANGED, multiline.keyTyped('\0', 47, false, true, true, clipboard));

        assertEquals("hello world", singleLine.text());
        assertEquals("hello\nworld", multiline.text());
    }

    @Test
    public void cursorAtWindowUsesVisibleSingleLineWindow() {
        TextInputState state = new TextInputState();
        state.text("abcdef");

        state.cursorAtWindow(new TestRenderer(), 0, 18, 0, true, state.cursor());

        assertEquals(3, state.cursor());
    }

    @Test
    public void selectAtWindowUsesSelectionAnchorAsVisibleWindow() {
        TextInputState state = new TextInputState();
        state.text("abcdef");
        state.cursorAtWindow(new TestRenderer(), 0, 18, 0, true, state.cursor());
        state.keyTyped('\0', 205, true, false, false);

        state.selectAtWindow(new TestRenderer(), 0, 18, 0, true);

        assertEquals(0, state.selectionStart());
        assertEquals(3, state.selectionEnd());
    }

    @Test
    public void cursorAtWrappedUsesMouseYForExplicitLines() {
        TextInputState state = new TextInputState();
        state.text("abc\ndef");

        state.cursorAtWrapped(new TestRenderer(), 0, 0, 100, 42, 0, 11, false);

        assertEquals(4, state.cursor());
    }

    @Test
    public void cursorAtWrappedUsesMouseYForSoftWrappedLines() {
        TextInputState state = new TextInputState();
        state.text("abcdef");

        state.cursorAtWrapped(new TestRenderer(), 0, 0, 18, 42, 0, 11, false);

        assertEquals(3, state.cursor());
    }

    @Test
    public void enterInsertsNewlineForMultilineFields() {
        TextInputState state = new TextInputState();
        state.text("hello");

        assertEquals(KeyResult.COMMIT, state.keyTyped('\0', 28, false, false, false));
        assertEquals("hello", state.text());

        assertEquals(KeyResult.CHANGED, state.keyTyped('\0', 28, false, false, true));
        assertEquals("hello\n", state.text());

        assertEquals(KeyResult.CHANGED, state.keyTyped('\0', 28, true, false, true));
        assertEquals("hello\n\n", state.text());
    }

    @Test
    public void controlEnterCommitsMultilineFields() {
        TextInputState state = new TextInputState();
        state.text("hello");

        assertEquals(KeyResult.COMMIT, state.keyTyped('\0', 28, false, true, true));
        assertEquals("hello", state.text());
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

    private static final class MemoryClipboard implements ClipboardAccess {
        private String text = "";

        public String get() {
            return text;
        }

        public void set(String text) {
            this.text = text;
        }
    }
}
