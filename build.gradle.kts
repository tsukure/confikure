import dev.architectury.pack200.java.Pack200Adapter
import net.fabricmc.loom.task.RemapJarTask
import org.apache.commons.lang3.SystemUtils
import java.io.ByteArrayOutputStream

plugins {
    idea
    java
    `maven-publish`
    id("gg.essential.loom") version "0.10.0.5"
    id("com.diffplug.spotless") version "8.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("dev.architectury.architectury-pack200") version "0.1.3"
}

group = "re.tsuku"
version = releaseVersion()

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    withSourcesJar()
}

loom {
    launchConfigs {
        "client" {
            property("mixin.debug", "true")
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
        }
    }
    runConfigs {
        "client" {
            if (SystemUtils.IS_OS_MAC_OSX) {
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(Pack200Adapter())
        mixinConfig("mixins.confikure.json")
    }
    mixin {
        defaultRefmapName.set("mixins.confikure.refmap.json")
    }
}

sourceSets {
    main {
        java.srcDir("src/forge/java")
        java.srcDir("src/dev/java")
        output.setResourcesDir(java.classesDirectory)
    }
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.tsuku.re/releases")
}

val shade: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    shade("com.alibaba.fastjson2:fastjson2:2.0.62")
    shade("re.tsuku:fastbus:1.1.1")
    shade("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        isTransitive = false
    }
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")

    testImplementation("junit:junit:4.13.2")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<Test> {
        useJUnit()
    }

    jar {
        archiveBaseName.set("confikure")
        archiveClassifier.set("without-deps")
        destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
        exclude("re/tsuku/confikure/dev/**")
        manifest.attributes(
            "FMLCorePluginContainsFMLMod" to "true",
            "ForceLoadAsMod" to "true",
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "MixinConfigs" to "mixins.confikure.json",
        )
    }

    shadowJar {
        destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
        archiveClassifier.set("non-obfuscated-with-deps")
        configurations = listOf(shade)
        exclude("re/tsuku/confikure/dev/**")
        exclude("META-INF/maven/com.alibaba.fastjson2/**")
        exclude("META-INF/native-image/com.alibaba.fastjson2/**")
        exclude("META-INF/proguard/fastjson2.pro")
        exclude("META-INF/scm/com.alibaba.fastjson2/**")

        relocate("com.alibaba.fastjson2", "re.tsuku.confikure.deps.fastjson2")
        relocate("re.tsuku.fastbus", "re.tsuku.confikure.deps.fastbus")
        manifest.attributes(
            "FMLCorePluginContainsFMLMod" to "true",
            "ForceLoadAsMod" to "true",
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "MixinConfigs" to "mixins.confikure.json",
        )
    }

    val remapJar = named<RemapJarTask>("remapJar") {
        archiveClassifier.set("forge")
        from(shadowJar)
        input.set(shadowJar.get().archiveFile)
    }

    val releaseSourcesJar = register("releaseSourcesJar") {
        group = LifecycleBasePlugin.BUILD_GROUP
        description = "Copies the source jar without dev-only sources into the release artifact name."
        val input = layout.buildDirectory.file("libs/confikure-${project.version}-sources-dev.jar")
        val output = layout.buildDirectory.file("libs/confikure-${project.version}-sources.jar")
        dependsOn(named("sourcesJar"))
        inputs.file(input)
        outputs.file(output)
        doLast {
            copy {
                from(input)
                into(output.get().asFile.parentFile)
                rename { output.get().asFile.name }
            }
        }
    }

    assemble {
        dependsOn(remapJar)
        dependsOn(releaseSourcesJar)
    }
}

tasks.named<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    exclude("re/tsuku/confikure/dev/**")
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        eclipse().configFile("config/formatter/eclipse-java.xml")
        formatAnnotations()

        target("src/**/*.java")
    }
}

fun releaseVersion(): String {
    val ref = System.getenv("GITHUB_REF_NAME")
    if (!ref.isNullOrBlank()) {
        return ref.removePrefix("v")
    }

    val output = ByteArrayOutputStream()
    val result = exec {
        commandLine("git", "describe", "--tags", "--exact-match", "HEAD")
        standardOutput = output
        errorOutput = ByteArrayOutputStream()
        isIgnoreExitValue = true
    }
    if (result.exitValue == 0) {
        return output.toString().trim().removePrefix("v")
    }
    return "1.0.0-SNAPSHOT"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.named("remapJar")) {
                classifier = ""
            }
            artifact(tasks.named("sourcesJar")) {
                classifier = "sources"
            }

            pom {
                name.set("confikure")
                description.set("a small java config and gui library for legacy minecraft forge mods.")
                url.set("https://github.com/tsukure/confikure")

                licenses {
                    license {
                        name.set("mit license")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("tsukure")
                        name.set("tsukure")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/tsukure/confikure.git")
                    developerConnection.set("scm:git:ssh://git@github.com/tsukure/confikure.git")
                    url.set("https://github.com/tsukure/confikure")
                }
            }
        }
    }
    repositories {
        maven {
            name = "tsukure"
            url = uri(if (version.toString().endsWith("-SNAPSHOT")) {
                "https://maven.tsuku.re/snapshots"
            } else {
                "https://maven.tsuku.re/releases"
            })
            credentials {
                username = publishUsername()
                password = publishPassword()
            }
        }
    }
}

fun publishUsername(): String? {
    return System.getenv("REPOSILITE_USERNAME")
}

fun publishPassword(): String? {
    return System.getenv("REPOSILITE_PASSWORD")
}
