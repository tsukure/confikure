package re.tsuku.confikure.model;

import java.lang.reflect.Field;

public final class FieldOptionAccess implements OptionAccess {
    private final Field field;
    private final Object owner;

    public FieldOptionAccess(Field field, Object owner) {
        this.field = field;
        this.owner = owner;
    }

    public Class<?> valueType() {
        return field.getType();
    }

    public Object get() {
        try {
            return field.get(owner);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("unable to read option field: " + field, exception);
        }
    }

    public void set(Object value) {
        try {
            field.set(owner, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("unable to write option field: " + field, exception);
        }
    }
}
