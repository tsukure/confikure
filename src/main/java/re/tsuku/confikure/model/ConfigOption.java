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
    private final boolean colorAlpha;
    private final String buttonLabel;
    private final List<OptionListener> listeners = new ArrayList<>();
    private OptionCondition visibleWhen = always();
    private OptionCondition enabledWhen = always();

    public ConfigOption(String id, String name, String description, String groupId, int order, EditorType type,
            OptionAccess access, Object defaultValue, NumberRange range, List<String> choices,
            List<String> searchTags) {
        this(id, name, description, groupId, order, type, access, defaultValue, range, choices, searchTags, true,
                false, true);
    }

    public ConfigOption(String id, String name, String description, String groupId, int order, EditorType type,
            OptionAccess access, Object defaultValue, NumberRange range, List<String> choices, List<String> searchTags,
            boolean keybindClearable, boolean keybindResetOnClear) {
        this(id, name, description, groupId, order, type, access, defaultValue, range, choices, searchTags,
                keybindClearable, keybindResetOnClear, true);
    }

    public ConfigOption(String id, String name, String description, String groupId, int order, EditorType type,
            OptionAccess access, Object defaultValue, NumberRange range, List<String> choices, List<String> searchTags,
            boolean keybindClearable, boolean keybindResetOnClear, boolean colorAlpha) {
        this(id, name, description, groupId, order, type, access, defaultValue, range, choices, searchTags,
                keybindClearable, keybindResetOnClear, colorAlpha, "run");
    }

    public ConfigOption(String id, String name, String description, String groupId, int order, EditorType type,
            OptionAccess access, Object defaultValue, NumberRange range, List<String> choices, List<String> searchTags,
            boolean keybindClearable, boolean keybindResetOnClear, boolean colorAlpha, String buttonLabel) {
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
        this.colorAlpha = colorAlpha;
        this.buttonLabel = buttonLabel == null || buttonLabel.isEmpty() ? "run" : buttonLabel;
    }

    /**
     * returns the stable option id used by persistence.
     */
    public String id() {
        return id;
    }

    /**
     * returns the display label.
     */
    public String name() {
        return name;
    }

    /**
     * returns the helper text shown under the label.
     */
    public String description() {
        return description;
    }

    /**
     * returns the stable id of the containing group.
     */
    public String groupId() {
        return groupId;
    }

    /**
     * returns the configured sort position.
     */
    public int order() {
        return order;
    }

    /**
     * returns the editor type used by the default gui.
     */
    public EditorType type() {
        return type;
    }

    /**
     * returns the underlying value type.
     */
    public Class<?> valueType() {
        return access.valueType();
    }

    /**
     * returns a defensive copy of the value captured during scanning.
     */
    public Object defaultValue() {
        return copyValue(defaultValue);
    }

    /**
     * returns the numeric range, or {@code null} when this option is not ranged.
     */
    public NumberRange range() {
        return range;
    }

    /**
     * returns the allowed choice values for dropdown and mode editors.
     */
    public List<String> choices() {
        return choices;
    }

    /**
     * returns additional search aliases associated with this option.
     */
    public List<String> searchTags() {
        return searchTags;
    }

    /**
     * returns whether a keybind option can be cleared from the gui.
     */
    public boolean keybindClearable() {
        return keybindClearable;
    }

    /**
     * returns whether clearing a keybind restores the default value.
     */
    public boolean keybindResetOnClear() {
        return keybindResetOnClear;
    }

    /**
     * returns whether a color option exposes alpha.
     */
    public boolean colorAlpha() {
        return colorAlpha;
    }

    /**
     * returns the text shown inside a button option's control.
     */
    public String buttonLabel() {
        return buttonLabel;
    }

    /**
     * reads the current value from the underlying config object.
     */
    public Object get() {
        return access.get();
    }

    /**
     * returns whether the option should currently be shown.
     */
    public boolean visible() {
        return visibleWhen.test();
    }

    /**
     * returns whether the option should currently accept input.
     */
    public boolean enabled() {
        return enabledWhen.test();
    }

    /**
     * sets the condition used to decide whether the option is visible.
     *
     * @param condition
     *            visibility predicate
     * @return this option
     */
    public ConfigOption visibleWhen(OptionCondition condition) {
        if (condition == null) {
            throw new NullPointerException("condition");
        }
        this.visibleWhen = condition;
        return this;
    }

    /**
     * sets the condition used to decide whether the option is enabled.
     *
     * @param condition
     *            enabled predicate
     * @return this option
     */
    public ConfigOption enabledWhen(OptionCondition condition) {
        if (condition == null) {
            throw new NullPointerException("condition");
        }
        this.enabledWhen = condition;
        return this;
    }

    /**
     * coerces and writes a new value to the underlying config object.
     *
     * @param value
     *            new value
     */
    public void set(Object value) {
        Object oldValue = get();
        Object nextValue = coerce(value);
        access.set(nextValue);
        Object newValue = get();
        if (!valueEquals(oldValue, newValue)) {
            notifyListeners(oldValue, newValue);
        }
    }

    /**
     * invokes a button option.
     */
    public void press() {
        if (type != EditorType.BUTTON) {
            throw new IllegalStateException("option is not a button: " + id);
        }
        access.set(null);
    }

    /**
     * returns whether the current value differs from the scanned default.
     */
    public boolean dirty() {
        return !valueEquals(defaultValue, get());
    }

    /**
     * restores the scanned default value for editable options.
     */
    public void reset() {
        if (type != EditorType.BUTTON && type != EditorType.INFO) {
            set(copyValue(defaultValue));
        }
    }

    /**
     * validates a value against choice and range metadata.
     *
     * @param value
     *            candidate value
     * @return error text, or {@code null} when valid
     */
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

    /**
     * registers a listener that is called after a successful value change.
     *
     * @param listener
     *            listener to add
     */
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
        return AlwaysOptionCondition.INSTANCE;
    }
}
