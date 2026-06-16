package re.tsuku.confikure.gui;

/**
 * colors and spacing used by the default config gui.
 */
public final class ConfigTheme {
    public final int background;
    public final int panel;
    public final int panelRaised;
    public final int border;
    public final int borderDark;
    public final int accent;
    public final int accentDark;
    public final int text;
    public final int mutedText;
    public final int disabledText;
    public final int danger;
    public final int sidebar;
    public final int slot;

    public final int padding;
    public final int rowHeight;
    public final int groupGap;

    public ConfigTheme() {
        this(0xEE1B1B1B, 0xFF282828, 0xFF333333, 0xFF555555, 0xFF121212, 0xFF78A96B, 0xFF4F7448,
                0xFFEFEFEF, 0xFFAAAAAA, 0xFF777777, 0xFFC86060, 0xFF242424, 0xFF151515, 8, 30, 5);
    }

    public ConfigTheme(int background, int panel, int panelRaised, int border, int borderDark, int accent,
            int accentDark, int text, int mutedText, int disabledText, int danger, int sidebar, int slot, int padding,
            int rowHeight, int groupGap) {
        this.background = background;
        this.panel = panel;
        this.panelRaised = panelRaised;
        this.border = border;
        this.borderDark = borderDark;
        this.accent = accent;
        this.accentDark = accentDark;
        this.text = text;
        this.mutedText = mutedText;
        this.disabledText = disabledText;
        this.danger = danger;
        this.sidebar = sidebar;
        this.slot = slot;
        this.padding = padding;
        this.rowHeight = rowHeight;
        this.groupGap = groupGap;
    }

    public static ConfigTheme minecraft() {
        return new ConfigTheme();
    }

    public static ConfigTheme catppuccinMocha() {
        return new ConfigTheme(0xEE1E1E2E, 0xFF313244, 0xFF45475A, 0xFF585B70, 0xFF11111B, 0xFFCBA6F7,
                0xFF8F6BB7, 0xFFCDD6F4, 0xFFBAC2DE, 0xFF7F849C, 0xFFF38BA8, 0xFF1E1E2E, 0xFF11111B, 8,
                30, 5);
    }

    public static ConfigTheme ayuMirage() {
        return new ConfigTheme(0xEE1F2430, 0xFF242936, 0xFF2F3545, 0xFF707A8C, 0xFF111722, 0xFFFFCC66,
                0xFFB98F3C, 0xFFCBCCC6, 0xFFA6A7A0, 0xFF707A8C, 0xFFF28779, 0xFF1B202B, 0xFF111722, 8,
                30, 5);
    }
}
