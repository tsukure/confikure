package re.tsuku.confikure.gui.input;

final class AcceptAllCharacterFilter implements TextInputState.CharacterFilter {
    static final AcceptAllCharacterFilter INSTANCE = new AcceptAllCharacterFilter();

    private AcceptAllCharacterFilter() {
    }

    public boolean accept(char character) {
        return true;
    }
}
