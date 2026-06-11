package re.tsuku.confikure.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * one editable value or action in a config group.
 */
public final class ConfigOption {
    private final String id;
    private final String name;
    private final String description;
    private final String groupId;
    private final int order;
    private final EditorType type;
    private final OptionAccess access;
    private final Object defaultValue;
    private final NumberRange range;
    private final List<String> choices;
    private final List<String> searchTags;
    private final List<OptionListener> listeners = new ArrayList<>();
    private OptionCondition visibleWhen = always();
    private OptionCondition enabledWhen = always();

    public ConfigOption(String id, String name, String description, String groupId, int order, EditorType type,
            OptionAccess access, Object defaultValue, NumberRange range, List<String> choices,
            List<String> searchTags) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.groupId = groupId;
        this.order = order;
        this.type = type;
        this.access = access;
        this.defaultValue = defaultValue;
        this.range = range;
        this.choices = choices == null ? Collections.<String>emptyList() : choices;
        this.searchTags = searchTags == null ? Collections.<String>emptyList() : searchTags;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String groupId() {
        return groupId;
    }

    public int order() {
        return order;
    }

    public EditorType type() {
        return type;
    }

    public Class<?> valueType() {
        return access.valueType();
    }

    public Object defaultValue() {
        return defaultValue;
    }

    public NumberRange range() {
        return range;
    }

    public List<String> choices() {
        return choices;
    }

    public List<String> searchTags() {
        return searchTags;
    }

    public Object get() {
        return access.get();
    }

    public boolean visible() {
        return visibleWhen.test();
    }

    public boolean enabled() {
        return enabledWhen.test();
    }

    public ConfigOption visibleWhen(OptionCondition condition) {
        if (condition == null) {
            throw new NullPointerException("condition");
        }
        this.visibleWhen = condition;
        return this;
    }

    public ConfigOption enabledWhen(OptionCondition condition) {
        if (condition == null) {
            throw new NullPointerException("condition");
        }
        this.enabledWhen = condition;
        return this;
    }

    public void set(Object value) {
        Object oldValue = get();
        Object nextValue = coerce(value);
        access.set(nextValue);
        Object newValue = get();
        if (!Objects.equals(oldValue, newValue)) {
            notifyListeners(oldValue, newValue);
        }
    }

    public void press() {
        if (type != EditorType.BUTTON) {
            throw new IllegalStateException("option is not a button: " + id);
        }
        access.set(null);
    }

    public boolean dirty() {
        return !Objects.equals(defaultValue, get());
    }

    public void reset() {
        if (type != EditorType.BUTTON && type != EditorType.INFO) {
            set(defaultValue);
        }
    }

    public String validate(Object value) {
        if (type == EditorType.DROPDOWN && !choices.isEmpty() && !choices.contains(String.valueOf(value))) {
            return "must be one of " + choices;
        }
        if (range != null && value instanceof Number) {
            double number = ((Number) value).doubleValue();
            if (number < range.min() || number > range.max()) {
                return "must be between " + range.min() + " and " + range.max();
            }
        }
        return null;
    }

    public void addListener(OptionListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        listeners.add(listener);
    }

    private Object coerce(Object value) {
        if (range != null && value instanceof Number) {
            return coerceNumber((Number) value);
        }
        if (type == EditorType.DROPDOWN && !choices.isEmpty()) {
            String next = String.valueOf(value);
            if (!choices.contains(next)) {
                throw new IllegalArgumentException("invalid value for " + id + ": " + next);
            }
            if (valueType().isEnum()) {
                return enumValue(next);
            }
            return next;
        }
        return value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object enumValue(String name) {
        return Enum.valueOf((Class<? extends Enum>) valueType(), name);
    }

    private Object coerceNumber(Number value) {
        double coerced = range.coerce(value.doubleValue());
        Class<?> valueType = valueType();
        if (valueType == int.class || valueType == Integer.class) {
            return (int) Math.round(coerced);
        }
        if (valueType == long.class || valueType == Long.class) {
            return Math.round(coerced);
        }
        if (valueType == float.class || valueType == Float.class) {
            return (float) coerced;
        }
        return coerced;
    }

    private void notifyListeners(Object oldValue, Object newValue) {
        for (OptionListener listener : listeners) {
            listener.changed(this, oldValue, newValue);
        }
    }

    private static OptionCondition always() {
        return new OptionCondition() {
            public boolean test() {
                return true;
            }
        };
    }
}
