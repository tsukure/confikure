package re.tsuku.confikure;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import re.tsuku.confikure.annotations.Category;
import re.tsuku.confikure.annotations.Config;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.model.ConfigCategory;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.EditorType;

public final class ConfigScanner {
    public ConfigDefinition scan(Object config) {
        if (config == null) {
            throw new NullPointerException("config");
        }

        Class<?> configType = config.getClass();
        Config metadata = configType.getAnnotation(Config.class);
        String name = metadata == null ? configType.getSimpleName() : metadata.name();
        String description = metadata == null ? "" : metadata.description();
        List<ConfigCategory> categories = findCategories(config);

        return new ConfigDefinition(config, name, description, categories);
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
            categories
                    .add(new ConfigCategory(category.name(), category.description(), instance, findOptions(instance)));
        }
        return Collections.unmodifiableList(categories);
    }

    private List<ConfigOption> findOptions(Object category) {
        if (category == null) {
            return Collections.emptyList();
        }

        List<ConfigOption> options = new ArrayList<>();
        for (Field field : fields(category.getClass())) {
            Option option = field.getAnnotation(Option.class);
            if (option == null) {
                continue;
            }
            field.setAccessible(true);
            options.add(new ConfigOption(option.name(), option.description(), option.type(), field, category));
        }
        return Collections.unmodifiableList(options);
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

    private static Object read(Field field, Object owner) {
        try {
            return field.get(owner);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("unable to read config field: " + field, exception);
        }
    }

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
        return EditorType.CUSTOM;
    }
}
