package re.tsuku.confikure.gui.input;

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

    public String text() {
        return text;
    }

    public void text(String text) {
        this.text = text == null ? "" : text;
        this.cursor = this.text.length();
        this.selection = cursor;
    }

    public void maxLength(int maxLength) {
        this.maxLength = Math.max(0, maxLength);
        if (text.length() > this.maxLength) {
            text = text.substring(0, this.maxLength);
        }
        clamp();
    }

    public void filter(CharacterFilter filter) {
        this.filter = filter == null ? CharacterFilter.ANY : filter;
    }

    public int cursor() {
        return cursor;
    }

    public int selectionStart() {
        return Math.min(cursor, selection);
    }

    public int selectionEnd() {
        return Math.max(cursor, selection);
    }

    public void cursorAt(GuiRenderer renderer, String visibleText, int textX, int mouseX) {
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
        cursor = Math.max(0, Math.min(text.length(), next));
        selection = cursor;
    }

    public KeyResult keyTyped(char typedChar, int keyCode, boolean shift, boolean control, boolean multiline) {
        clamp();
        if (keyCode == 1) {
            return KeyResult.CANCEL;
        }
        if (keyCode == 28 || keyCode == 156) {
            if (multiline && shift && !control) {
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
        if (keyCode == 203) {
            move(-1, shift);
            return KeyResult.USED;
        }
        if (keyCode == 205) {
            move(1, shift);
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
            backspace();
            return KeyResult.CHANGED;
        }
        if (keyCode == 211) {
            deleteForward();
            return KeyResult.CHANGED;
        }
        if (typedChar >= 32 && typedChar != 127 && filter.accept(typedChar)) {
            insert(String.valueOf(typedChar));
            return KeyResult.CHANGED;
        }
        return KeyResult.USED;
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
    }

    private void replaceSelection(String replacement) {
        int start = selectionStart();
        int end = selectionEnd();
        text = text.substring(0, start) + replacement + text.substring(end);
        cursor = start + replacement.length();
        selection = cursor;
    }

    private void move(int delta, boolean selecting) {
        cursor = Math.max(0, Math.min(text.length(), cursor + delta));
        if (!selecting) {
            selection = cursor;
        }
    }

    private void clamp() {
        cursor = Math.max(0, Math.min(text.length(), cursor));
        selection = Math.max(0, Math.min(text.length(), selection));
    }

    public enum KeyResult {
        USED, CHANGED, COMMIT, CANCEL
    }

    public interface CharacterFilter {
        CharacterFilter ANY = new CharacterFilter() {
            public boolean accept(char character) {
                return true;
            }
        };

        boolean accept(char character);
    }
}
