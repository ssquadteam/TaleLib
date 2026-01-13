package com.github.ssquadteam.talelib.config

import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.nio.file.Files
import java.nio.file.Path

class ConfigManager<T : Any>(
    private val dataPath: Path,
    private val serializer: KSerializer<T>,
    private val defaultConfig: T,
    private val fileName: String = "config.json"
) {
    companion object {
        private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
        }
    }

    private var _config: T = defaultConfig
    val config: T get() = _config
    val filePath: Path by lazy { dataPath.resolve(fileName) }

    fun load(): T {
        try {
            if (!Files.exists(filePath)) {
                LOGGER.atInfo().log("Config not found, creating default: $filePath")
                save()
                return _config
            }
            _config = json.decodeFromString(serializer, Files.readString(filePath))
            LOGGER.atInfo().log("Loaded config from: $filePath")
        } catch (e: Exception) {
            LOGGER.atSevere().withCause(e).log("Failed to load config, using defaults")
            _config = defaultConfig
        }
        return _config
    }

    fun reload(): T = load()

    fun save(): Boolean = try {
        Files.createDirectories(dataPath)
        Files.writeString(filePath, json.encodeToString(serializer, _config))
        LOGGER.atFine().log("Saved config to: $filePath")
        true
    } catch (e: Exception) {
        LOGGER.atSevere().withCause(e).log("Failed to save config")
        false
    }

    fun save(newConfig: T): Boolean {
        _config = newConfig
        return save()
    }

    fun update(block: T.() -> T): T {
        _config = _config.block()
        save()
        return _config
    }

    fun reset(): T {
        _config = defaultConfig
        save()
        return _config
    }

    fun exists(): Boolean = Files.exists(filePath)
    fun delete(): Boolean = try { Files.deleteIfExists(filePath); true } catch (e: Exception) { false }
    fun toJson(): String = json.encodeToString(serializer, _config)
}

inline fun <reified T : Any> JavaPlugin.config(
    default: T,
    fileName: String = "config.json"
): ConfigManager<T> = ConfigManager(
    dataPath = this.dataDirectory,
    serializer = serializer(),
    defaultConfig = default,
    fileName = fileName
)

inline fun <reified T : Any> config(
    dataPath: Path,
    default: T,
    fileName: String = "config.json"
): ConfigManager<T> = ConfigManager(
    dataPath = dataPath,
    serializer = serializer(),
    defaultConfig = default,
    fileName = fileName
)
