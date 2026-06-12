<h1 align="center">confikure</h1>

<p align="center">
  <a href="https://github.com/tsukure/confikure/releases/latest"><img src="https://img.shields.io/github/v/release/tsukure/confikure?style=flat-square" alt="latest release"></a>
  <a href="https://maven.tsuku.re/releases/re/tsuku/confikure/"><img src="https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.tsuku.re%2Freleases%2Fre%2Ftsuku%2Fconfikure%2Fmaven-metadata.xml&style=flat-square&label=maven" alt="maven"></a>
  <a href="https://github.com/tsukure/confikure/actions/workflows/release.yml"><img src="https://img.shields.io/github/actions/workflow/status/tsukure/confikure/release.yml?branch=main&style=flat-square" alt="release workflow"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue?style=flat-square" alt="license"></a>
</p>

`confikure` is a small java config and gui library for minecraft mods.

it currently targets legacy forge 1.8.9, java 8 bytecode, and a simple annotation-driven config model. the gui is intentionally minecraft-like without depending on vanilla container textures.

## status

this is an early proof of concept, but the main config and gui path is testable.

implemented:

- runtime annotations for configs, category tabs, groups, options, ranges, dropdowns, mode cycling, colors, keybinds, multiline text, info text, search tags, and buttons
- reflective scanning with stable ids, ordering, grouped options, defaults, reset support, dirty tracking, validation, listeners, and visible/enabled predicates
- fastjson2 persistence keyed by stable ids, with pretty json and atomic temp-file writes
- a minecraft-friendly gui with left category tabs, collapsible groups, boxed option rows, scrolling, hover/focus states, true dropdowns, mode cycling, typed sliders, text selection, keybind capture, clear/reset keybind actions, and a typed color picker
- built-in gui themes for the default minecraft-ish palette, catppuccin mocha, and ayu mirage
- a legacy forge 1.8.9 adapter and loom dev run
- a remapped forge jar with fastjson2 relocated to avoid mod shading conflicts
- focused jvm tests for scanning, persistence, option behavior, and gui interaction rules

still expected before a stable release:

- migrations and config schema version handling
- unknown-field preservation or backup behavior for hand-edited config files
- a cleaner artifact split if the core api grows beyond the legacy forge target
- more real mod integration
- a final visual pass after more in-game testing

## install

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

## shading

mods should shade and relocate both `confikure` and fastjson2:

```kotlin
shade("re.tsuku:confikure:1.0.0-SNAPSHOT")
shade("com.alibaba.fastjson2:fastjson2:2.0.62")

shadowJar {
    relocate("re.tsuku.confikure", "your.mod.lib.confikure")
    relocate("com.alibaba.fastjson2", "your.mod.lib.fastjson2")
}
```

the loom-built forge jar relocates fastjson2 to `re.tsuku.confikure.deps.fastjson2`.

## basic shape

```java
import re.tsuku.confikure.Confikure;
import re.tsuku.confikure.annotations.Category;
import re.tsuku.confikure.annotations.Config;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.model.ConfigDefinition;

@Config(name = "examplemod", description = "examplemod settings")
public final class ExampleModConfig {
    @Category(name = "general")
    public final General general = new General();

    public static final class General {
        @Option(name = "enabled")
        public boolean enabled = true;

        @Option(name = "hud text", group = "hud")
        public String hudText = "final kills";
    }
}

ConfigDefinition definition = Confikure.scan(new ExampleModConfig());
```

## forge 1.8.9

the forge bridge opens a config screen from a scanned definition and a config file path:

```java
File configFile = new File(new File(Minecraft.getMinecraft().mcDataDir, "config"), "examplemod.json");
ConfikureForge.open(config, configFile.toPath());
```

when opening from a command, confikure delays the gui by one client tick so chat can close first.

## themes

use the default theme:

```java
ConfigGui gui = new ConfigGui(definition);
```

or provide a theme directly:

```java
ConfigGui gui = new ConfigGui(definition, ConfigTheme.catppuccinMocha(), DefaultOptionEditors.create());
```

the built-in presets are also exposed through `ConfigColorScheme`.

## build

run tests:

```bash
./gradlew test
```

check formatting, tests, and the loom-backed forge build:

```bash
./gradlew spotlessCheck clean test assemble
```

the remapped jar is written to:

```text
build/libs/confikure-1.0.0-SNAPSHOT-forge.jar
```

run the local loom test mod and open the gui with `/confikure`:

```bash
./gradlew runClient
```

the dev config is stored under the minecraft config directory:

```text
run/config/confikure-dev.json
```

publish locally:

```bash
./gradlew publishToMavenLocal
```

release publishing is tag-based. tags like `v1.0.0` publish version `1.0.0`.

## license

mit
