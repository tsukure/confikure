package re.tsuku.confikure.model;

final class AlwaysOptionCondition implements OptionCondition {
    static final AlwaysOptionCondition INSTANCE = new AlwaysOptionCondition();

    private AlwaysOptionCondition() {
    }

    public boolean test() {
        return true;
    }
}
