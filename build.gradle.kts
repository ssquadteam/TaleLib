import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

plugins {
    java
    `maven-publish`
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
}

// Access properties from gradle.properties
val patchline: String = findProperty("patchline") as? String ?: "release"
val includesPack: String = findProperty("includes_pack") as? String ?: "false"
val loadUserMods: String = findProperty("load_user_mods") as? String ?: "false"
val javaVersionStr: String = findProperty("java_version") as? String ?: "21"
val mavenGroup: String = findProperty("maven_group") as? String ?: "com.github.ssquadteam"

val hytaleHome = "${System.getProperty("user.home")}/AppData/Roaming/Hytale"

group = mavenGroup
version = findProperty("version") as? String ?: "0.3.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersionStr))
    withSourcesJar()
    withJavadocJar()
}

// Quiet warnings about missing Javadocs
tasks.javadoc {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:-missing", "-quiet")
}

repositories {
    maven { url = uri("https://jitpack.io") }
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Hytale Server Dependency (Local)
    compileOnly(files("libs/HytaleServer.jar"))

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Reflection for annotation processing
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")

    // HytaleRequest
    api("com.github.ssquadteam:HytaleMiniFormat:main-SNAPSHOT")
}

// Server Run Directory
val serverRunDir = file("$projectDir/run")
if (!serverRunDir.exists()) {
    serverRunDir.mkdirs()
}

// Update manifest.json task
tasks.register("updatePluginManifest") {
    val manifestFile = file("src/main/resources/manifest.json")
    inputs.property("version", project.version)
    inputs.property("includesPack", includesPack)

    doLast {
        if (!manifestFile.exists()) {
            throw GradleException("Could not find manifest.json at ${manifestFile.path}!")
        }

        @Suppress("UNCHECKED_CAST")
        val jsonMap = JsonSlurper().parseText(manifestFile.readText()) as MutableMap<String, Any>
        jsonMap["Version"] = project.version
        jsonMap["IncludesAssetPack"] = includesPack.toBoolean()

        manifestFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(jsonMap)))
    }
}

tasks.named("processResources") {
    dependsOn("updatePluginManifest")
}

// IntelliJ Run Configuration
idea {
    project {
        settings {
            runConfigurations {
                register("HytaleServer", Application::class.java) {
                    mainClass = "com.hypixel.hytale.Main"
                    moduleName = "${project.name}.main"
                    programParameters = "--allow-op --assets=$hytaleHome/install/$patchline/package/game/latest/Assets.zip"

                    if (includesPack.toBoolean()) {
                        val sourceDir = sourceSets["main"].java.srcDirs.firstOrNull()?.parentFile?.absolutePath
                        if (sourceDir != null) {
                            programParameters += " --mods=$sourceDir"
                        }
                    }

                    if (loadUserMods.toBoolean()) {
                        programParameters += " --mods=$hytaleHome/UserData/Mods"
                    }

                    workingDirectory = serverRunDir.absolutePath
                }
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = javaVersionStr
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

// Source sets
sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/java", "src/main/kotlin"))
        }
        resources {
            setSrcDirs(listOf("src/main/resources"))
            exclude(".idea/**")
        }
    }
}

// Jar configuration - include Kotlin dependencies
tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(provider {
        configurations.runtimeClasspath.get().filter {
            it.name.startsWith("kotlin-stdlib") ||
            it.name.startsWith("kotlinx-coroutines") ||
            it.name.startsWith("kotlinx-serialization") ||
            it.name.startsWith("HytaleMiniFormat")
        }.map { if (it.isDirectory) it else zipTree(it) }
    })
    exclude(".idea/**")

    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
}

// Maven publishing for local use
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = mavenGroup
            artifactId = "talelib"
            version = project.version.toString()
        }
    }
}
