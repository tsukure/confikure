package re.tsuku.confikure.gui;

final class DefaultKeyNameProvider implements ConfigGui.KeyNameProvider {
    static final DefaultKeyNameProvider INSTANCE = new DefaultKeyNameProvider();

    private DefaultKeyNameProvider() {
    }

    public String name(int keyCode) {
        return String.valueOf(keyCode);
    }
}
