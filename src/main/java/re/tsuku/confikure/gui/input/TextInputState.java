package re.tsuku.confikure.gui.input;

import re.tsuku.confikure.gui.TextLayout;
import re.tsuku.confikure.gui.platform.ClipboardAccess;
import re.tsuku.confikure.gui.platform.GuiRenderer;

/**
 * small platform-neutral text buffer for gui inputs.
 */
public final class TextInputState {
    private String text = "";
    private int cursor;
    private int selection;
    private int maxLength = 256;
    private CharacterFilter filter = CharacterFilter.ANY;

    /**
     * returns the current text.
     */
    public String text() {
        return text;
    }

    /**
     * replaces the current text and moves the cursor to the end.
     */
    public void text(String text) {
        this.text = text == null ? "" : text;
        this.cursor = this.text.length();
        this.selection = cursor;
    }

    /**
     * sets the maximum accepted text length.
     */
    public void maxLength(int maxLength) {
        this.maxLength = Math.max(0, maxLength);
        if (text.length() > this.maxLength) {
            text = text.substring(0, this.maxLength);
        }
        clamp();
    }

    /**
     * sets the character filter used for typed input.
     */
    public void filter(CharacterFilter filter) {
        this.filter = filter == null ? CharacterFilter.ANY : filter;
    }

    /**
     * returns the cursor index.
     */
    public int cursor() {
        return cursor;
    }

    /**
     * returns the lower selection bound.
     */
    public int selectionStart() {
        return Math.min(cursor, selection);
    }

    /**
     * returns the upper selection bound.
     */
    public int selectionEnd() {
        return Math.max(cursor, selection);
    }

    /**
     * moves the cursor to the character nearest a mouse x coordinate.
     */
    public void cursorAt(GuiRenderer renderer, String visibleText, int textX, int mouseX) {
        cursor = positionAt(renderer, visibleText, textX, mouseX);
        selection = cursor;
    }

    /**
     * moves the cursor to the character nearest a mouse x coordinate in a horizontally scrolled field.
     */
    public void cursorAtWindow(GuiRenderer renderer, int textX, int width, int mouseX, boolean focused,
            int viewCursor) {
        cursor = TextLayout.singleLinePositionAt(renderer, text, width, viewCursor, focused, textX, mouseX);
        selection = cursor;
    }

    /**
     * moves the cursor to the character nearest a mouse position in wrapped text.
     */
    public void cursorAtWrapped(GuiRenderer renderer, int textX, int textY, int width, int height, int mouseX,
            int mouseY, boolean focused) {
        cursorAtWrapped(renderer, textX, textY, width, height, mouseX, mouseY, focused, cursor);
    }

    /**
     * moves the cursor to the character nearest a mouse position using a separate cursor as the visible anchor.
     */
    public void cursorAtWrapped(GuiRenderer renderer, int textX, int textY, int width, int height, int mouseX,
            int mouseY, boolean focused, int viewCursor) {
        cursor = TextLayout.positionAt(renderer, text, width, height, viewCursor, focused, textX, textY, mouseX,
                mouseY);
        selection = cursor;
    }

    /**
     * extends selection to the character nearest a mouse x coordinate.
     */
    public void selectAt(GuiRenderer renderer, String visibleText, int textX, int mouseX) {
        cursor = positionAt(renderer, visibleText, textX, mouseX);
    }

    /**
     * extends selection to the character nearest a mouse x coordinate in a horizontally scrolled field.
     */
    public void selectAtWindow(GuiRenderer renderer, int textX, int width, int mouseX, boolean focused) {
        cursor = TextLayout.singleLinePositionAt(renderer, text, width, selection, focused, textX, mouseX);
    }

    /**
     * extends selection to the character nearest a mouse position in wrapped text.
     */
    public void selectAtWrapped(GuiRenderer renderer, int textX, int textY, int width, int height, int mouseX,
            int mouseY, boolean focused) {
        cursor = TextLayout.positionAt(renderer, text, width, height, cursor, focused, textX, textY, mouseX, mouseY);
    }

    /**
     * applies one typed key and returns how the caller should react.
     */
    public KeyResult keyTyped(char typedChar, int keyCode, boolean shift, boolean control, boolean multiline) {
        return keyTyped(typedChar, keyCode, shift, control, multiline, ClipboardAccess.NONE);
    }

    /**
     * applies one typed key and returns how the caller should react.
     */
    public KeyResult keyTyped(char typedChar, int keyCode, boolean shift, boolean control, boolean multiline,
            ClipboardAccess clipboard) {
        clamp();
        if (keyCode == 1) {
            return KeyResult.CANCEL;
        }
        if (keyCode == 28 || keyCode == 156) {
            if (multiline && !control) {
                insert("\n");
                return KeyResult.CHANGED;
            }
            return KeyResult.COMMIT;
        }
        if (control && keyCode == 30) {
            cursor = text.length();
            selection = 0;
            return KeyResult.USED;
        }
        if (control && keyCode == 46) {
            copy(clipboard);
            return KeyResult.USED;
        }
        if (control && keyCode == 45) {
            return cut(clipboard);
        }
        if (control && keyCode == 47) {
            return paste(clipboard, multiline);
        }
        if (keyCode == 203) {
            moveTo(control ? wordBoundaryLeft(cursor) : cursor - 1, shift);
            return KeyResult.USED;
        }
        if (keyCode == 205) {
            moveTo(control ? wordBoundaryRight(cursor) : cursor + 1, shift);
            return KeyResult.USED;
        }
        if (keyCode == 199) {
            cursor = 0;
            if (!shift) {
                selection = cursor;
            }
            return KeyResult.USED;
        }
        if (keyCode == 207) {
            cursor = text.length();
            if (!shift) {
                selection = cursor;
            }
            return KeyResult.USED;
        }
        if (keyCode == 14) {
            if (control) {
                deleteBackwardWord();
            } else {
                backspace();
            }
            return KeyResult.CHANGED;
        }
        if (keyCode == 211) {
            if (control) {
                deleteForwardWord();
            } else {
                deleteForward();
            }
            return KeyResult.CHANGED;
        }
        if (typedChar >= 32 && typedChar != 127 && filter.accept(typedChar)) {
            insert(String.valueOf(typedChar));
            return KeyResult.CHANGED;
        }
        return KeyResult.USED;
    }

    /**
     * returns selected text, or an empty string when there is no active selection.
     */
    public String selectedText() {
        if (cursor == selection) {
            return "";
        }
        return text.substring(selectionStart(), selectionEnd());
    }

    private void insert(String value) {
        int start = selectionStart();
        int end = selectionEnd();
        int remaining = maxLength - (text.length() - (end - start));
        String inserted = value.length() > remaining ? value.substring(0, Math.max(0, remaining)) : value;
        text = text.substring(0, start) + inserted + text.substring(end);
        cursor = start + inserted.length();
        selection = cursor;
    }

    private void backspace() {
        if (cursor != selection) {
            replaceSelection("");
            return;
        }
        if (cursor > 0) {
            int next = cursor - 1;
            text = text.substring(0, next) + text.substring(cursor);
            cursor = next;
            selection = next;
        }
    }

    private void deleteForward() {
        if (cursor != selection) {
            replaceSelection("");
            return;
        }
        if (cursor < text.length()) {
            text = text.substring(0, cursor) + text.substring(cursor + 1);
        }
        selection = cursor;
    }

    private void deleteBackwardWord() {
        if (cursor != selection) {
            replaceSelection("");
            return;
        }
        int next = wordBoundaryLeft(cursor);
        text = text.substring(0, next) + text.substring(cursor);
        cursor = next;
        selection = next;
    }

    private void deleteForwardWord() {
        if (cursor != selection) {
            replaceSelection("");
            return;
        }
        int next = wordBoundaryRight(cursor);
        text = text.substring(0, cursor) + text.substring(next);
        selection = cursor;
    }

    private void replaceSelection(String replacement) {
        int start = selectionStart();
        int end = selectionEnd();
        text = text.substring(0, start) + replacement + text.substring(end);
        cursor = start + replacement.length();
        selection = cursor;
    }

    private void copy(ClipboardAccess clipboard) {
        String selected = selectedText();
        if (!selected.isEmpty()) {
            clipboard.set(selected);
        }
    }

    private KeyResult cut(ClipboardAccess clipboard) {
        String selected = selectedText();
        if (selected.isEmpty()) {
            return KeyResult.USED;
        }
        clipboard.set(selected);
        replaceSelection("");
        return KeyResult.CHANGED;
    }

    private KeyResult paste(ClipboardAccess clipboard, boolean multiline) {
        String pasted = clipboard.get();
        if (pasted == null || pasted.isEmpty()) {
            return KeyResult.USED;
        }
        String sanitized = sanitizedPaste(pasted, multiline);
        if (sanitized.isEmpty()) {
            return KeyResult.USED;
        }
        insert(sanitized);
        return KeyResult.CHANGED;
    }

    private String sanitizedPaste(String value, boolean multiline) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character == '\r') {
                if (i + 1 < value.length() && value.charAt(i + 1) == '\n') {
                    continue;
                }
                character = multiline ? '\n' : ' ';
            } else if (character == '\n') {
                character = multiline ? '\n' : ' ';
            }
            if (character == '\n' || character >= 32 && character != 127 && filter.accept(character)) {
                builder.append(character);
            }
        }
        return builder.toString();
    }

    private void moveTo(int next, boolean selecting) {
        cursor = Math.max(0, Math.min(text.length(), next));
        if (!selecting) {
            selection = cursor;
        }
    }

    private int positionAt(GuiRenderer renderer, String visibleText, int textX, int mouseX) {
        String value = visibleText == null ? text : visibleText;
        int next = value.length();
        for (int i = 0; i <= value.length(); i++) {
            int left = renderer.textWidth(value.substring(0, i));
            int right = i == value.length() ? left : renderer.textWidth(value.substring(0, i + 1));
            if (mouseX < textX + (left + right) / 2) {
                next = i;
                break;
            }
        }
        return Math.max(0, Math.min(text.length(), next));
    }

    private int wordBoundaryLeft(int from) {
        int index = Math.max(0, Math.min(text.length(), from));
        while (index > 0 && isWhitespace(text.charAt(index - 1))) {
            index--;
        }
        while (index > 0 && !isWhitespace(text.charAt(index - 1))) {
            index--;
        }
        return index;
    }

    private int wordBoundaryRight(int from) {
        int index = Math.max(0, Math.min(text.length(), from));
        while (index < text.length() && isWhitespace(text.charAt(index))) {
            index++;
        }
        while (index < text.length() && !isWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
    }

    private static boolean isWhitespace(char character) {
        return character <= ' ';
    }

    private void clamp() {
        cursor = Math.max(0, Math.min(text.length(), cursor));
        selection = Math.max(0, Math.min(text.length(), selection));
    }

    /**
     * result of applying a key to a text input.
     */
    public enum KeyResult {
        USED, CHANGED, COMMIT, CANCEL
    }

    /**
     * predicate for accepted typed characters.
     */
    public interface CharacterFilter {
        CharacterFilter ANY = AcceptAllCharacterFilter.INSTANCE;

        /**
         * returns whether a typed character should be inserted.
         */
        boolean accept(char character);
    }
}
