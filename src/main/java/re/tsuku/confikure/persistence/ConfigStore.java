package re.tsuku.confikure.persistence;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import re.tsuku.confikure.model.ConfigCategory;
import re.tsuku.confikure.model.ConfigDefinition;
import re.tsuku.confikure.model.ConfigGroup;
import re.tsuku.confikure.model.ConfigOption;
import re.tsuku.confikure.model.EditorType;

/**
 * json persistence for scanned config definitions.
 */
public final class ConfigStore {
    private static final Type MAP_TYPE = new TypeReference<Map<String, Object>>() {
    }.getType();

    public void save(ConfigDefinition definition, Path path) throws IOException {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", definition.version());
        root.put("config", definition.id());

        Map<String, Object> categories = new LinkedHashMap<>();
        for (ConfigCategory category : definition.categories()) {
            Map<String, Object> groups = new LinkedHashMap<>();
            for (ConfigGroup group : category.groups()) {
                Map<String, Object> options = new LinkedHashMap<>();
                for (ConfigOption option : group.options()) {
                    if (option.type() != EditorType.BUTTON && option.type() != EditorType.INFO) {
                        options.put(option.id(), writeValue(option));
                    }
                }
                groups.put(group.id(), options);
            }
            categories.put(category.id(), groups);
        }
        root.put("categories", categories);

        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = path.resolveSibling(path.getFileName().toString() + ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            writer.write(JSON.toJSONString(root, JSONWriter.Feature.PrettyFormat));
        }
        try {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public boolean load(ConfigDefinition definition, Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        Map<String, Object> root;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            try {
                root = JSON.parseObject(reader, MAP_TYPE);
            } catch (RuntimeException exception) {
                return false;
            }
        }
        if (root == null) {
            return false;
        }
        Object configId = root.get("config");
        if (configId != null && !definition.id().equals(String.valueOf(configId))) {
            return false;
        }
        Object categoriesValue = root.get("categories");
        if (!(categoriesValue instanceof Map)) {
            return false;
        }
        readCategories(definition, castMap(categoriesValue));
        return true;
    }

    private void readCategories(ConfigDefinition definition, Map<String, Object> categories) {
        for (ConfigCategory category : definition.categories()) {
            Object groupsValue = categories.get(category.id());
            if (!(groupsValue instanceof Map)) {
                continue;
            }
            Map<String, Object> groups = castMap(groupsValue);
            for (ConfigGroup group : category.groups()) {
                Object optionsValue = groups.get(group.id());
                if (!(optionsValue instanceof Map)) {
                    continue;
                }
                Map<String, Object> options = castMap(optionsValue);
                for (ConfigOption option : group.options()) {
                    if (option.type() == EditorType.BUTTON || option.type() == EditorType.INFO) {
                        continue;
                    }
                    if (options.containsKey(option.id())) {
                        loadOption(option, options.get(option.id()));
                    }
                }
            }
        }
    }

    private static void loadOption(ConfigOption option, Object value) {
        try {
            option.set(readValue(option, value));
        } catch (RuntimeException ignored) {
        }
    }

    private static Object writeValue(ConfigOption option) {
        Object value = option.get();
        if (value instanceof Enum<?>) {
            return ((Enum<?>) value).name();
        }
        return value;
    }

    private static Object readValue(ConfigOption option, Object value) {
        if (value == null) {
            return null;
        }
        if (option.type() == EditorType.DROPDOWN && option.valueType().isEnum()) {
            return String.valueOf(value);
        }
        Class<?> type = option.valueType();
        if (value instanceof Number) {
            Number number = (Number) value;
            if (type == int.class || type == Integer.class) {
                return number.intValue();
            }
            if (type == long.class || type == Long.class) {
                return number.longValue();
            }
            if (type == float.class || type == Float.class) {
                return number.floatValue();
            }
            if (type == double.class || type == Double.class) {
                return number.doubleValue();
            }
        }
        if (List.class.isAssignableFrom(type) && value instanceof List) {
            return value;
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }
}
