plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.3.1"
}

group = findProperty("maven_group") as? String ?: "com.github.ssquadteam"
version = findProperty("version") as? String ?: "0.3.0"

val asmVersion = findProperty("asm_version") as? String ?: "9.9"
val mixinVersion = findProperty("mixin_version") as? String ?: "0.16.5+mixin.0.8.7"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

val shade: Configuration by configurations.creating

configurations {
    implementation {
        extendsFrom(shade)
    }
}

repositories {
    maven { url = uri("https://jitpack.io") }
    mavenCentral()
    mavenLocal()
    maven("https://maven.fabricmc.net")
    maven("https://repo.spongepowered.org/maven")
    maven("https://maven.hytale.com/release")
    maven("https://maven.hytale.com/pre-release")
}

dependencies {
    compileOnly("com.hypixel.hytale:Server:0.6.0-pre.12.1")
    compileOnly(project(":"))

    shade("net.fabricmc:sponge-mixin:$mixinVersion")
    shade("org.ow2.asm:asm:$asmVersion")
    shade("org.ow2.asm:asm-analysis:$asmVersion")
    shade("org.ow2.asm:asm-commons:$asmVersion")
    shade("org.ow2.asm:asm-tree:$asmVersion")
    shade("org.ow2.asm:asm-util:$asmVersion")
    shade("org.ow2.sat4j:org.ow2.sat4j.core:2.3.6")
    shade("org.ow2.sat4j:org.ow2.sat4j.pb:2.3.6")
}

tasks.jar {
    enabled = false
}

tasks.processResources {
    filesMatching("manifest.json") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    archiveBaseName.set("TaleLibMixins")
    archiveClassifier.set("")
    configurations = listOf(shade)
    mergeServiceFiles()
    exclude("about.html", "fabric.mod.json", "LICENSE.txt", "LICENSE_MixinExtras", "sat4j.version")
    exclude("META-INF/maven/**")
    exclude("META-INF/services/cpw.**", "META-INF/services/org.spongepowered.tools.obfuscation.service.IObfuscationService")
    exclude("com/google/gson/**", "org/sat4j/**")

    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
