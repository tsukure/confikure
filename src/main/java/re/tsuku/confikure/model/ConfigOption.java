package re.tsuku.confikure.model;

import java.lang.reflect.Field;

import re.tsuku.confikure.ConfigScanner;

public final class ConfigOption {
    private final String name;
    private final String description;
    private final EditorType type;
    private final Field field;
    private final Object owner;

    public ConfigOption(String name, String description, EditorType type, Field field, Object owner) {
        this.name = name;
        this.description = description;
        this.type = type == EditorType.AUTO ? ConfigScanner.inferEditorType(field.getType()) : type;
        this.field = field;
        this.owner = owner;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public EditorType type() {
        return type;
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
