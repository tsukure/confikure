package re.tsuku.confikure.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import re.tsuku.confikure.ConfigFixtures.ExampleConfig;
import re.tsuku.confikure.Confikure;
import re.tsuku.confikure.model.ConfigDefinition;

public final class ConfigStoreTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void persistsJsonByStableIds() throws Exception {
        ExampleConfig config = new ExampleConfig();
        ConfigDefinition definition = Confikure.scan(config);
        ConfigStore store = new ConfigStore();
        Path path = temporaryFolder.newFile("example.json").toPath();

        config.movement.speed = 1.5D;
        config.visuals.mode = "fancy";
        config.visuals.order = Arrays.asList("two", "one");
        store.save(definition, path);

        ExampleConfig loaded = new ExampleConfig();
        store.load(Confikure.scan(loaded), path);

        assertEquals(1.5D, loaded.movement.speed, 0.0D);
        assertEquals("fancy", loaded.visuals.mode);
        assertEquals(Arrays.asList("two", "one"), loaded.visuals.order);
    }

    @Test
    public void saveCreatesParentsAndOmitsActionOnlyRows() throws Exception {
        ExampleConfig config = new ExampleConfig();
        ConfigStore store = new ConfigStore();
        Path path = temporaryFolder.getRoot().toPath().resolve("nested/example.json");

        store.save(Confikure.scan(config), path);

        String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        assertTrue(Files.isRegularFile(path));
        assertFalse(json.contains("reset-cache"));
        assertFalse(json.contains("about"));
    }

    @Test
    public void loadSkipsMismatchedConfigId() throws Exception {
        ConfigStore store = new ConfigStore();
        Path path = temporaryFolder.newFile("wrong.json").toPath();
        Files.write(path, Arrays.asList("{", "  \"config\": \"other\",", "  \"categories\": {}", "}"),
                StandardCharsets.UTF_8);

        ExampleConfig config = new ExampleConfig();

        assertFalse(store.load(Confikure.scan(config), path));
        assertEquals(1.0D, config.movement.speed, 0.0D);
    }

    @Test
    public void loadKeepsDefaultsForInvalidValues() throws Exception {
        ConfigStore store = new ConfigStore();
        Path path = temporaryFolder.newFile("invalid.json").toPath();
        Files.write(path,
                Arrays.asList("{", "  \"config\": \"example\",", "  \"categories\": {",
                        "    \"visuals\": {\"general\": {\"mode\": \"missing\"}},",
                        "    \"movement\": {\"sprint\": {\"speed\": \"fast\"}}", "  }", "}"),
                StandardCharsets.UTF_8);

        ExampleConfig config = new ExampleConfig();

        assertTrue(store.load(Confikure.scan(config), path));
        assertEquals("simple", config.visuals.mode);
        assertEquals(1.0D, config.movement.speed, 0.0D);
    }

    @Test
    public void loadReturnsFalseForCorruptJson() throws Exception {
        ConfigStore store = new ConfigStore();
        Path path = temporaryFolder.newFile("corrupt.json").toPath();
        Files.write(path, Arrays.asList("{nope"), StandardCharsets.UTF_8);

        assertFalse(store.load(Confikure.scan(new ExampleConfig()), path));
    }

    @Test
    public void loadReturnsFalseWhenCategoriesAreMissing() throws Exception {
        ConfigStore store = new ConfigStore();
        Path path = temporaryFolder.newFile("no-categories.json").toPath();
        Files.write(path, Arrays.asList("{", "  \"config\": \"example\"", "}"), StandardCharsets.UTF_8);

        assertFalse(store.load(Confikure.scan(new ExampleConfig()), path));
    }

    @Test
    public void loadSkipsNullPrimitiveValues() throws Exception {
        ConfigStore store = new ConfigStore();
        Path path = temporaryFolder.newFile("null-primitive.json").toPath();
        Files.write(path,
                Arrays.asList("{", "  \"config\": \"example\",",
                        "  \"categories\": {\"movement\": {\"sprint\": {\"speed\": null}}}", "}"),
                StandardCharsets.UTF_8);

        ExampleConfig config = new ExampleConfig();

        assertTrue(store.load(Confikure.scan(config), path));
        assertEquals(1.0D, config.movement.speed, 0.0D);
    }
}
