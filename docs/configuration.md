# Configuration System

TaleLib provides JSON configuration management using kotlinx-serialization.

## Creating a Config Class

Define your config as a serializable data class:

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class MyConfig(
    val debug: Boolean = false,
    val maxPlayers: Int = 100,
    val messages: Messages = Messages()
)

@Serializable
data class Messages(
    val welcome: String = "Welcome to the server!",
    val goodbye: String = "See you later!"
)
```

## Using ConfigManager

```kotlin
import com.github.ssquadteam.talelib.config.config

class MyPlugin(init: JavaPluginInit) : TalePlugin(init) {

    // Create ConfigManager using extension function
    private val configManager = config(MyConfig())

    // Convenient accessor
    val config: MyConfig get() = configManager.config

    override fun onStart() {
        // Load config (creates default if not exists)
        configManager.load()

        if (config.debug) {
            info("Debug mode enabled!")
        }

        info(config.messages.welcome)
    }
}
```

## ConfigManager Methods

### Loading and Saving

```kotlin
configManager.load()      // Load from file (creates default if missing)
configManager.reload()    // Reload from file
configManager.save()      // Save current config to file
```

### Modifying Config

```kotlin
// Save new config
configManager.save(config.copy(debug = true))

// Update with block
configManager.update { copy(maxPlayers = 200) }

// Reset to defaults
configManager.reset()
```

### Utilities

```kotlin
configManager.exists()    // Check if file exists
configManager.delete()    // Delete config file
configManager.toJson()    // Get config as JSON string
configManager.filePath    // Get Path to config file
```

## Custom File Name

```kotlin
// Default: config.json
private val configManager = config(MyConfig())

// Custom filename
private val configManager = config(
    default = MyConfig(),
    fileName = "settings.json"
)
```

## Multiple Config Files

```kotlin
class MyPlugin(init: JavaPluginInit) : TalePlugin(init) {

    // Main settings
    private val settingsManager = config(Settings(), "settings.json")
    val settings: Settings get() = settingsManager.config

    // Data storage
    private val dataManager = config(PlayerData(), "data.json")
    val data: PlayerData get() = dataManager.config

    override fun onStart() {
        settingsManager.load()
        dataManager.load()
    }

    override fun onShutdown() {
        dataManager.save()
    }
}
```

## Example: Full Config Usage

```kotlin
@Serializable
data class ShopConfig(
    val enabled: Boolean = true,
    val currency: String = "coins",
    val items: List<ShopItem> = listOf(
        ShopItem("hytale:stone", 10),
        ShopItem("hytale:iron_ore", 50)
    )
)

@Serializable
data class ShopItem(
    val itemId: String,
    val price: Int
)

class ShopPlugin(init: JavaPluginInit) : TalePlugin(init) {

    private val configManager = config(ShopConfig())
    val config: ShopConfig get() = configManager.config

    override fun onStart() {
        configManager.load()

        if (!config.enabled) {
            info("Shop is disabled in config")
            return
        }

        info("Loaded ${config.items.size} shop items")
        config.items.forEach { item ->
            info("  ${item.itemId}: ${item.price} ${config.currency}")
        }
    }

    fun addItem(itemId: String, price: Int) {
        configManager.update {
            copy(items = items + ShopItem(itemId, price))
        }
    }
}
```

## Config File Location

Config files are stored in your plugin's data folder:
```
plugins/YourPlugin/config.json
plugins/YourPlugin/settings.json
```
