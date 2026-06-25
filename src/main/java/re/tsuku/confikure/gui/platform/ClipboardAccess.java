package re.tsuku.confikure.gui.platform;

/**
 * platform clipboard bridge for text keyboard shortcuts.
 */
public interface ClipboardAccess {
    ClipboardAccess NONE = new ClipboardAccess() {
        public String get() {
            return "";
        }

        public void set(String text) {
        }
    };

    /**
     * returns current clipboard text, or an empty string when unavailable.
     */
    String get();

    /**
     * replaces current clipboard text.
     */
    void set(String text);
}
