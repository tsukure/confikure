package re.tsuku.confikure;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import re.tsuku.confikure.annotations.Button;
import re.tsuku.confikure.annotations.Category;
import re.tsuku.confikure.annotations.Color;
import re.tsuku.confikure.annotations.Config;
import re.tsuku.confikure.annotations.Dropdown;
import re.tsuku.confikure.annotations.Group;
import re.tsuku.confikure.annotations.Info;
import re.tsuku.confikure.annotations.Keybind;
import re.tsuku.confikure.annotations.Mode;
import re.tsuku.confikure.annotations.Multiline;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Range;
import re.tsuku.confikure.annotations.SearchTag;
import re.tsuku.confikure.annotations.Text;
import re.tsuku.confikure.model.ButtonOptionAccess;
import re.tsuku.confikure.model.ConfigCategory;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigGroup;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.EditorType;
import re.tsuku.confikure.model.FieldOptionAccess;
import re.tsuku.confikure.model.NumberRange;

/**
 * scanner that converts annotated config objects into confikure model objects.
 */
public final class ConfigScanner {
    private static final String DEFAULT_GROUP_ID = "general";

    /**
     * scans one config root object.
     *
     * @param config
     *            annotated root object
     * @return scanned config definition
     */
    public ConfigDefinition scan(Object config) {
        if (config == null) {
            throw new NullPointerException("config");
        }

        Class<?> configType = config.getClass();
        Config metadata = configType.getAnnotation(Config.class);
        String name = metadata == null ? configType.getSimpleName() : metadata.name();
        String id = metadata == null || metadata.id().isEmpty() ? stableId(name) : metadata.id();
        String description = metadata == null ? "" : metadata.description();
        int version = metadata == null ? 1 : metadata.version();

        return new ConfigDefinition(config, id, name, description, version, findCategories(config));
    }

    private List<ConfigCategory> findCategories(Object config) {
        List<ConfigCategory> categories = new ArrayList<>();
        for (Field field : fields(config.getClass())) {
            Category category = field.getAnnotation(Category.class);
            if (category == null) {
                continue;
            }
            field.setAccessible(true);
            Object instance = read(field, config);
            String id = category.id().isEmpty() ? stableId(category.name()) : category.id();
            categories.add(new ConfigCategory(id, category.name(), category.description(), category.order(), instance,
                    findGroups(instance)));
        }
        requireUniqueCategoryIds(categories);
        categories.sort(Comparator.comparingInt(ConfigCategory::order).thenComparing(ConfigCategory::id));
        return Collections.unmodifiableList(categories);
    }

    private List<ConfigGroup> findGroups(Object category) {
        if (category == null) {
            return Collections.emptyList();
        }

        Map<String, GroupBuilder> groups = new LinkedHashMap<>();
        group(groups, DEFAULT_GROUP_ID, "general", "", 0);

        for (Field field : fields(category.getClass())) {
            Group group = field.getAnnotation(Group.class);
            if (group != null) {
                field.setAccessible(true);
                Object instance = read(field, category);
                String id = group.id().isEmpty() ? stableId(group.name()) : group.id();
                group(groups, id, group.name(), group.description(), group.order(), true).options
                        .addAll(findOptions(instance, id));
            }

            Option option = field.getAnnotation(Option.class);
            if (option != null) {
                field.setAccessible(true);
                ConfigOption scanned = option(field, category, option);
                group(groups, scanned.groupId(), displayName(scanned.groupId()), "",
                        scanned.groupId().equals(DEFAULT_GROUP_ID)
                                ? 0
                                : scanned.order()).options
                        .add(scanned);
            }
        }

        for (Method method : methods(category.getClass())) {
            Button button = method.getAnnotation(Button.class);
            if (button == null) {
                continue;
            }
            if (method.getParameterTypes().length != 0) {
                throw new IllegalArgumentException("@Button methods cannot have parameters: " + method);
            }
            method.setAccessible(true);
            ConfigOption scanned = button(method, category, button);
            group(groups, scanned.groupId(), displayName(scanned.groupId()), "",
                    scanned.groupId().equals(DEFAULT_GROUP_ID)
                            ? 0
                            : scanned.order()).options
                    .add(scanned);
        }

        List<ConfigGroup> result = new ArrayList<>();
        for (GroupBuilder builder : groups.values()) {
            if (builder.options.isEmpty()) {
                continue;
            }
            requireUniqueOptionIds(builder.id, builder.options);
            builder.options.sort(Comparator.comparingInt(ConfigOption::order).thenComparing(ConfigOption::id));
            result.add(new ConfigGroup(builder.id, builder.name, builder.description, builder.order,
                    Collections.unmodifiableList(new ArrayList<>(builder.options))));
        }
        result.sort(Comparator.comparingInt(ConfigGroup::order).thenComparing(ConfigGroup::id));
        return Collections.unmodifiableList(result);
    }

    private List<ConfigOption> findOptions(Object group, String groupId) {
        if (group == null) {
            return Collections.emptyList();
        }

        List<ConfigOption> options = new ArrayList<>();
        for (Field field : fields(group.getClass())) {
            Option option = field.getAnnotation(Option.class);
            if (option == null) {
                continue;
            }
            field.setAccessible(true);
            options.add(option(field, group, option, groupId));
        }
        return options;
    }

    private ConfigOption option(Field field, Object owner, Option option) {
        String groupId = option.group().isEmpty() ? DEFAULT_GROUP_ID : stableId(option.group());
        return option(field, owner, option, groupId);
    }

    private ConfigOption option(Field field, Object owner, Option option, String groupId) {
        EditorType editorType = editorType(field, option.type());
        String id = option.id().isEmpty() ? stableId(option.name()) : option.id();
        Keybind keybind = field.getAnnotation(Keybind.class);
        Color color = field.getAnnotation(Color.class);
        return new ConfigOption(id, option.name(), option.description(), groupId, option.order(), editorType,
                new FieldOptionAccess(field, owner), read(field, owner), range(field), choices(field),
                searchTags(field.getAnnotation(SearchTag.class)), keybind == null || keybind.clearable(),
                keybind != null && keybind.resetOnClear(), color == null || color.alpha());
    }

    private ConfigOption button(Method method, Object owner, Button button) {
        String id = button.id().isEmpty() ? stableId(button.name()) : button.id();
        String groupId = button.group().isEmpty() ? DEFAULT_GROUP_ID : stableId(button.group());
        return new ConfigOption(id, button.name(), button.description(), groupId, button.order(), EditorType.BUTTON,
                new ButtonOptionAccess(method, owner), null, null, Collections.<String>emptyList(),
                searchTags(method.getAnnotation(SearchTag.class)));
    }

    private static EditorType editorType(Field field, EditorType requested) {
        if (requested != EditorType.AUTO) {
            return requested;
        }
        if (field.getAnnotation(Info.class) != null) {
            return EditorType.INFO;
        }
        if (field.getAnnotation(Keybind.class) != null) {
            return EditorType.KEYBIND;
        }
        if (field.getAnnotation(Color.class) != null) {
            return EditorType.COLOR;
        }
        if (field.getAnnotation(Multiline.class) != null) {
            return EditorType.MULTILINE_TEXT;
        }
        if (field.getAnnotation(Text.class) != null) {
            return EditorType.TEXT;
        }
        if (field.getAnnotation(Mode.class) != null) {
            return EditorType.MODE;
        }
        if (field.getAnnotation(Dropdown.class) != null) {
            return EditorType.DROPDOWN;
        }
        return inferEditorType(field.getType());
    }

    private static NumberRange range(Field field) {
        Range range = field.getAnnotation(Range.class);
        return range == null ? null : new NumberRange(range.min(), range.max(), range.step());
    }

    private static List<String> choices(Field field) {
        Mode mode = field.getAnnotation(Mode.class);
        if (mode != null && mode.values().length > 0) {
            return Collections.unmodifiableList(Arrays.asList(mode.values()));
        }
        Dropdown dropdown = field.getAnnotation(Dropdown.class);
        if (dropdown != null && dropdown.values().length > 0) {
            return Collections.unmodifiableList(Arrays.asList(dropdown.values()));
        }
        if (!field.getType().isEnum()) {
            return Collections.emptyList();
        }
        Object[] values = field.getType().getEnumConstants();
        List<String> choices = new ArrayList<>();
        for (Object value : values) {
            choices.add(((Enum<?>) value).name());
        }
        return Collections.unmodifiableList(choices);
    }

    private static List<String> searchTags(SearchTag tag) {
        if (tag == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(Arrays.asList(tag.value()));
    }

    private static GroupBuilder group(Map<String, GroupBuilder> groups, String id, String name, String description,
            int order) {
        GroupBuilder existing = groups.get(id);
        if (existing != null) {
            return existing;
        }
        GroupBuilder builder = new GroupBuilder(id, name, description, order);
        groups.put(id, builder);
        return builder;
    }

    private static GroupBuilder group(Map<String, GroupBuilder> groups, String id, String name, String description,
            int order, boolean explicit) {
        GroupBuilder existing = groups.get(id);
        if (existing != null) {
            if (explicit && existing.explicit) {
                throw new IllegalArgumentException("duplicate group id: " + id);
            }
            if (explicit) {
                existing.name = name;
                existing.description = description;
                existing.order = order;
                existing.explicit = true;
            }
            return existing;
        }
        GroupBuilder builder = new GroupBuilder(id, name, description, order);
        builder.explicit = explicit;
        groups.put(id, builder);
        return builder;
    }

    private static void requireUniqueCategoryIds(List<ConfigCategory> categories) {
        Map<String, ConfigCategory> seen = new LinkedHashMap<>();
        for (ConfigCategory category : categories) {
            if (seen.put(category.id(), category) != null) {
                throw new IllegalArgumentException("duplicate category id: " + category.id());
            }
        }
    }

    private static void requireUniqueOptionIds(String groupId, List<ConfigOption> options) {
        Map<String, ConfigOption> seen = new LinkedHashMap<>();
        for (ConfigOption option : options) {
            if (seen.put(option.id(), option) != null) {
                throw new IllegalArgumentException("duplicate option id in group " + groupId + ": " + option.id());
            }
        }
    }

    private static List<Field> fields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            Collections.addAll(fields, current.getDeclaredFields());
            current = current.getSuperclass();
        }
        return fields;
    }

    private static List<Method> methods(Class<?> type) {
        List<Method> methods = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            Collections.addAll(methods, current.getDeclaredMethods());
            current = current.getSuperclass();
        }
        return methods;
    }

    private static Object read(Field field, Object owner) {
        try {
            return field.get(owner);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("unable to read config field: " + field, exception);
        }
    }

    /**
     * chooses the default editor type for an option value type.
     *
     * @param type
     *            java field type
     * @return inferred editor type
     */
    public static EditorType inferEditorType(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) {
            return EditorType.BOOLEAN;
        }
        if (type == int.class || type == Integer.class || type == long.class || type == Long.class
                || type == float.class || type == Float.class || type == double.class || type == Double.class) {
            return EditorType.NUMBER;
        }
        if (type == String.class) {
            return EditorType.TEXT;
        }
        if (type.isEnum()) {
            return EditorType.DROPDOWN;
        }
        if (List.class.isAssignableFrom(type)) {
            return EditorType.DRAGGABLE_LIST;
        }
        return EditorType.CUSTOM;
    }

    /**
     * converts display text into the stable lowercase id format used by generated ids.
     *
     * @param name
     *            display text
     * @return stable id using lowercase letters, digits, and dashes
     */
    public static String stableId(String name) {
        StringBuilder builder = new StringBuilder();
        String text = name == null ? "" : name.trim().toLowerCase();
        boolean dash = false;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if ((character >= 'a' && character <= 'z') || (character >= '0' && character <= '9')) {
                builder.append(character);
                dash = false;
            } else if (!dash && builder.length() > 0) {
                builder.append('-');
                dash = true;
            }
        }
        int length = builder.length();
        if (length > 0 && builder.charAt(length - 1) == '-') {
            builder.deleteCharAt(length - 1);
        }
        return builder.length() == 0 ? "option" : builder.toString();
    }

    private static String displayName(String id) {
        String[] parts = id.split("-");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.length() == 0 ? "General" : builder.toString();
    }

    private static final class GroupBuilder {
        private final String id;
        private String name;
        private String description;
        private int order;
        private boolean explicit;
        private final List<ConfigOption> options = new ArrayList<>();

        private GroupBuilder(String id, String name, String description, int order) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.order = order;
        }
    }
}
