<h1 align="center">confikure</h1>

<p align="center">
  <a href="https://github.com/tsukure/confikure/releases/latest"><img src="https://img.shields.io/github/v/release/tsukure/confikure?style=flat-square" alt="latest release"></a>
  <a href="https://maven.tsuku.re/releases/re/tsuku/confikure/"><img src="https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.tsuku.re%2Freleases%2Fre%2Ftsuku%2Fconfikure%2Fmaven-metadata.xml&style=flat-square&label=maven" alt="maven"></a>
  <a href="https://github.com/tsukure/confikure/actions/workflows/release.yml"><img src="https://img.shields.io/github/actions/workflow/status/tsukure/confikure/release.yml?branch=main&style=flat-square" alt="release workflow"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue?style=flat-square" alt="license"></a>
</p>

`confikure` is a small java config and gui library for legacy minecraft forge mods.
it targets forge 1.8.9 and java 8 bytecode.

configs are declared with annotations, scanned at runtime, persisted as json, and opened in-game.

## install

gradle kotlin dsl:

```kotlin
repositories {
    maven("https://maven.tsuku.re/releases")
}

dependencies {
    implementation("re.tsuku:confikure:1.0.0")
}
```

for snapshots, use:

```kotlin
repositories {
    maven("https://maven.tsuku.re/snapshots")
}
```

maven:

```xml
<repositories>
  <repository>
    <id>tsukure-releases</id>
    <url>https://maven.tsuku.re/releases</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>re.tsuku</groupId>
    <artifactId>confikure</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

for snapshots, use:

```xml
<repository>
  <id>tsukure-snapshots</id>
  <url>https://maven.tsuku.re/snapshots</url>
</repository>
```

## basic usage

create a config:

```java
import re.tsuku.confikure.annotations.Category;
import re.tsuku.confikure.annotations.Config;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Range;

@Config(name = "examplemod", id = "examplemod", description = "examplemod settings")
public final class ExampleConfig {
    @Category(name = "general")
    public final General general = new General();

    public static final class General {
        @Option(name = "enabled")
        public boolean enabled = true;

        @Option(name = "speed", description = "movement multiplier")
        @Range(min = 0.0D, max = 3.0D, step = 0.25D)
        public double speed = 1.0D;
    }
}
```

scan it when you need a definition:

```java
import re.tsuku.confikure.Confikure;
import re.tsuku.confikure.model.ConfigDefinition;

ConfigDefinition definition = Confikure.scan(new ExampleConfig());
```

## forge 1.8.9

open the built-in screen from a command or keybind:

```java
import java.io.File;
import net.minecraft.client.Minecraft;
import re.tsuku.confikure.forge.ForgeConfig;

File configFile = new File(new File(Minecraft.getMinecraft().mcDataDir, "config"), "examplemod.json");
ForgeConfig.open(config, configFile);
```

`open` delays screen creation by one client tick so chat can close before the gui opens.
`openNow` displays an already-created screen immediately.

to embed confikure inside your own `GuiScreen`, create a controller and forward lifecycle calls:

```java
import re.tsuku.confikure.forge.ForgeConfig;
import re.tsuku.confikure.forge.ForgeConfigGui;

ForgeConfigGui gui = ForgeConfig.gui(config, configFile, confikure -> {
    confikure.sidebarHeader((renderer, bounds, theme) -> {
        renderer.text("examplemod", bounds.x, bounds.y, theme.text);
        renderer.text("settings", bounds.x, bounds.y + 12, theme.mutedText);
    });
});

gui.init(Minecraft.getMinecraft());
gui.render(mc, width, height, mouseX, mouseY);
gui.click(width, height, mouseX, mouseY);
gui.keyTyped(typedChar, keyCode);
gui.close();
```

## annotations

supported config annotations:

- `@Config`, `@Category`, and `@Group` for structure
- `@Option` for editable values
- `@Range` for numeric sliders and typed numeric fields
- `@Dropdown` and `@Mode` for fixed string choices
- `@Color` for argb color values, with optional alpha
- `@Keybind`, `@Text`, `@Multiline`, `@Info`, `@SearchTag`, and `@Button`

option ids are generated from names by default.
set explicit ids when a setting has already shipped and should keep the same persisted key.

## themes

use the default theme:

```java
ConfigGui gui = new ConfigGui(definition);
```

or select a built-in color scheme:

```java
gui.themeSupplier(() -> ConfigColorScheme.byDisplayName(config.visuals.themeScheme).theme());
```

the built-in schemes are `minecraft`, `catppuccin mocha`, and `ayu mirage`.

## example

`src/forge/java` is the production forge adapter that is included in release jars.
`src/example/java` is a local example mod that shows command registration, config persistence, gui customization, read-only settings, conditional settings, and a small mod-owned event bus.

run the example mod:

```bash
./gradlew runClient
```

or use the generated `Minecraft Client` run configuration in intellij.

open the example gui with:

```text
/confikure
```

the example config is written to:

```text
run/config/confikure-example.json
```

## build

run tests:

```bash
./gradlew test
```

check formatting, tests, and the forge jar:

```bash
./gradlew spotlessCheck test assemble
```

the remapped jar is written to:

```text
build/libs/confikure-1.0.0-SNAPSHOT-forge.jar
```

## license

mit
