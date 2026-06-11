package re.tsuku.confikure.model;

public interface OptionAccess {
    Class<?> valueType();

    Object get();

    void set(Object value);
}
