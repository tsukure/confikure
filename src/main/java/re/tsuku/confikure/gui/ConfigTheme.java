package re.tsuku.confikure.gui;

/**
 * colors and spacing used by the default config gui.
 */
public final class ConfigTheme {
    /**
     * full-screen backdrop color.
     */
    public final int background;
    /**
     * main panel fill color.
     */
    public final int panel;
    /**
     * raised row and header fill color.
     */
    public final int panelRaised;
    /**
     * light border color.
     */
    public final int border;
    /**
     * dark border color.
     */
    public final int borderDark;
    /**
     * primary accent color.
     */
    public final int accent;
    /**
     * darker accent color used for filled tracks.
     */
    public final int accentDark;
    /**
     * primary text color.
     */
    public final int text;
    /**
     * secondary text color.
     */
    public final int mutedText;
    /**
     * disabled text color.
     */
    public final int disabledText;
    /**
     * destructive action color.
     */
    public final int danger;
    /**
     * sidebar fill color.
     */
    public final int sidebar;
    /**
     * recessed control slot color.
     */
    public final int slot;

    /**
     * outer content padding.
     */
    public final int padding;
    /**
     * normal option row height.
     */
    public final int rowHeight;
    /**
     * spacing between groups.
     */
    public final int groupGap;

    /**
     * creates the default confikure theme.
     */
    public ConfigTheme() {
        this(0xEE1B1B1B, 0xFF282828, 0xFF333333, 0xFF555555, 0xFF121212, 0xFF78A96B, 0xFF4F7448,
                0xFFEFEFEF, 0xFFAAAAAA, 0xFF777777, 0xFFC86060, 0xFF242424, 0xFF151515, 8, 30, 5);
    }

    /**
     * creates a theme from raw argb colors and layout metrics.
     */
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

    /**
     * returns the default theme.
     */
    public static ConfigTheme minecraft() {
        return new ConfigTheme();
    }

    /**
     * returns a catppuccin mocha preset theme.
     */
    public static ConfigTheme catppuccinMocha() {
        return new ConfigTheme(0xEE1E1E2E, 0xFF313244, 0xFF45475A, 0xFF585B70, 0xFF11111B, 0xFFCBA6F7,
                0xFF8F6BB7, 0xFFCDD6F4, 0xFFBAC2DE, 0xFF7F849C, 0xFFF38BA8, 0xFF1E1E2E, 0xFF11111B, 8,
                30, 5);
    }

    /**
     * returns an ayu mirage preset theme.
     */
    public static ConfigTheme ayuMirage() {
        return new ConfigTheme(0xEE1F2430, 0xFF242936, 0xFF2F3545, 0xFF707A8C, 0xFF111722, 0xFFFFCC66,
                0xFFB98F3C, 0xFFCBCCC6, 0xFFA6A7A0, 0xFF707A8C, 0xFFF28779, 0xFF1B202B, 0xFF111722, 8,
                30, 5);
    }
}
