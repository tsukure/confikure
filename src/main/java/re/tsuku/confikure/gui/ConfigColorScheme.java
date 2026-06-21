package re.tsuku.confikure.gui;

/**
 * built-in color schemes for the default gui theme.
 */
public enum ConfigColorScheme {
    MINECRAFT("minecraft", ConfigTheme.minecraft()), CATPPUCCIN_MOCHA("catppuccin mocha",
            ConfigTheme.catppuccinMocha()), AYU_MIRAGE("ayu mirage", ConfigTheme.ayuMirage());

    private final String displayName;
    private final ConfigTheme theme;

    ConfigColorScheme(String displayName, ConfigTheme theme) {
        this.displayName = displayName;
        this.theme = theme;
    }

    /**
     * returns the display name used in config examples and dropdowns.
     */
    public String displayName() {
        return displayName;
    }

    /**
     * returns the theme represented by this scheme.
     */
    public ConfigTheme theme() {
        return theme;
    }

    /**
     * finds a scheme by display name.
     *
     * @param displayName
     *            display name to resolve
     * @return matching scheme, or {@link #MINECRAFT} when none matches
     */
    public static ConfigColorScheme byDisplayName(String displayName) {
        for (ConfigColorScheme scheme : values()) {
            if (scheme.displayName.equals(displayName)) {
                return scheme;
            }
        }
        return MINECRAFT;
    }
}
