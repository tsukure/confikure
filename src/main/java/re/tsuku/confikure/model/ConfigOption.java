package re.tsuku.confikure.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final boolean keybindClearable;
    private final boolean keybindResetOnClear;
    private final List<OptionListener> listeners = new ArrayList<>();
    private OptionCondition visibleWhen = always();
    private OptionCondition enabledWhen = always();

    public ConfigOption(String id, String name, String description, String groupId, int order, EditorType type,
            OptionAccess access, Object defaultValue, NumberRange range, List<String> choices,
            List<String> searchTags) {
        this(id, name, description, groupId, order, type, access, defaultValue, range, choices, searchTags, true,
                false);
    }

    public ConfigOption(String id, String name, String description, String groupId, int order, EditorType type,
            OptionAccess access, Object defaultValue, NumberRange range, List<String> choices, List<String> searchTags,
            boolean keybindClearable, boolean keybindResetOnClear) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.groupId = groupId;
        this.order = order;
        this.type = type;
        this.access = access;
        this.defaultValue = copyValue(defaultValue);
        this.range = range;
        this.choices = choices == null ? Collections.<String>emptyList() : choices;
        this.searchTags = searchTags == null ? Collections.<String>emptyList() : searchTags;
        this.keybindClearable = keybindClearable;
        this.keybindResetOnClear = keybindResetOnClear;
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
        return copyValue(defaultValue);
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

    public boolean keybindClearable() {
        return keybindClearable;
    }

    public boolean keybindResetOnClear() {
        return keybindResetOnClear;
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
        if (!valueEquals(oldValue, newValue)) {
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
        return !valueEquals(defaultValue, get());
    }

    public void reset() {
        if (type != EditorType.BUTTON && type != EditorType.INFO) {
            set(copyValue(defaultValue));
        }
    }

    public String validate(Object value) {
        if ((type == EditorType.DROPDOWN || type == EditorType.MODE) && !choices.isEmpty()
                && !choices.contains(String.valueOf(value))) {
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
        if (value == null) {
            if (valueType().isPrimitive()) {
                throw new IllegalArgumentException("null is not valid for " + id);
            }
            return null;
        }
        if (value instanceof Number && numericType(valueType())) {
            return coerceNumber((Number) value);
        }
        if ((type == EditorType.DROPDOWN || type == EditorType.MODE) && !choices.isEmpty()) {
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
        double coerced = range == null ? value.doubleValue() : range.coerce(value.doubleValue());
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
        if (valueType == byte.class || valueType == Byte.class) {
            return (byte) Math.round(coerced);
        }
        if (valueType == short.class || valueType == Short.class) {
            return (short) Math.round(coerced);
        }
        return coerced;
    }

    private static Object copyValue(Object value) {
        if (value instanceof List) {
            return new ArrayList<>((List<?>) value);
        }
        if (value instanceof Map) {
            return new java.util.LinkedHashMap<>((Map<?, ?>) value);
        }
        if (value instanceof Object[]) {
            return ((Object[]) value).clone();
        }
        if (value instanceof int[]) {
            return ((int[]) value).clone();
        }
        if (value instanceof long[]) {
            return ((long[]) value).clone();
        }
        if (value instanceof float[]) {
            return ((float[]) value).clone();
        }
        if (value instanceof double[]) {
            return ((double[]) value).clone();
        }
        if (value instanceof byte[]) {
            return ((byte[]) value).clone();
        }
        if (value instanceof short[]) {
            return ((short[]) value).clone();
        }
        if (value instanceof char[]) {
            return ((char[]) value).clone();
        }
        if (value instanceof boolean[]) {
            return ((boolean[]) value).clone();
        }
        return value;
    }

    private static boolean valueEquals(Object left, Object right) {
        if (left != null && right != null && left.getClass().isArray() && right.getClass().isArray()) {
            if (left instanceof Object[] && right instanceof Object[]) {
                return Arrays.deepEquals((Object[]) left, (Object[]) right);
            }
            if (left instanceof int[] && right instanceof int[]) {
                return Arrays.equals((int[]) left, (int[]) right);
            }
            if (left instanceof long[] && right instanceof long[]) {
                return Arrays.equals((long[]) left, (long[]) right);
            }
            if (left instanceof float[] && right instanceof float[]) {
                return Arrays.equals((float[]) left, (float[]) right);
            }
            if (left instanceof double[] && right instanceof double[]) {
                return Arrays.equals((double[]) left, (double[]) right);
            }
            if (left instanceof byte[] && right instanceof byte[]) {
                return Arrays.equals((byte[]) left, (byte[]) right);
            }
            if (left instanceof short[] && right instanceof short[]) {
                return Arrays.equals((short[]) left, (short[]) right);
            }
            if (left instanceof char[] && right instanceof char[]) {
                return Arrays.equals((char[]) left, (char[]) right);
            }
            if (left instanceof boolean[] && right instanceof boolean[]) {
                return Arrays.equals((boolean[]) left, (boolean[]) right);
            }
        }
        return Objects.equals(left, right);
    }

    private static boolean numericType(Class<?> type) {
        return type == byte.class || type == Byte.class || type == short.class || type == Short.class
                || type == int.class || type == Integer.class || type == long.class || type == Long.class
                || type == float.class || type == Float.class || type == double.class || type == Double.class;
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
