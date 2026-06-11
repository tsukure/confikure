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

    public String displayName() {
        return displayName;
    }

    public ConfigTheme theme() {
        return theme;
    }
}
