# TaleLib

A flexible, expandable Kotlin library for Hytale plugin development.

## Features

- **Command System** - Declarative command building with argument parsing and subcommands
- **Event System** - Type-safe event handling with priorities and keyed events
- **Camera System** - Presets and DSL for camera control (Top-Down, Isometric, Side-Scroller, etc.)
- **UI System** - HUDs and interactive pages with element bindings
- **Hologram System** - Floating text holograms with multi-line support
- **Entity System** - Model-based entity spawning and management
- **Scheduler** - Coroutine-based task scheduling
- **Config System** - JSON configuration with kotlinx-serialization
- **And more:** Inventory, Permissions, Sound, Notifications, Teleportation, Input handling

## Quick Start

### Creating a Plugin

```kotlin
import com.github.ssquadteam.talelib.TalePlugin
import com.github.ssquadteam.talelib.command.TaleCommand
import com.github.ssquadteam.talelib.command.TaleContext
import com.github.ssquadteam.talelib.message.*
import com.github.ssquadteam.talelib.event.on
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent

class MyPlugin(init: JavaPluginInit) : TalePlugin(init) {

    override fun onSetup() {
        info("MyPlugin setting up...")
    }

    override fun onStart() {
        info("MyPlugin started!")

        // Register commands
        taleCommands.register(HelloCommand())

        // Register events
        taleEvents.on<PlayerReadyEvent> { event ->
            event.player.sendMessage("Welcome to the server!".success())
        }
    }

    override fun onShutdown() {
        info("MyPlugin disabled!")
    }
}

class HelloCommand : TaleCommand("hello", "Say hello") {
    private val nameArg = optionalString("name", "Player name")

    override fun onExecute(ctx: TaleContext) {
        val name = ctx.get(nameArg) ?: "World"
        ctx.reply("Hello, $name!".success())
    }
}
```

## Installation

Add TaleLib as a dependency in your `build.gradle.kts`:

```kotlin
dependencies {
    compileOnly("com.github.ssquadteam:TaleLib")
}
```

Or with local JAR:

```kotlin
dependencies {
    compileOnly(files("libs/TaleLib.jar"))
}
```

## Documentation

Detailed documentation for each system is available in the [docs/](docs/) folder:

- [Commands](docs/commands.md) - Command system with arguments and subcommands
- [Events](docs/events.md) - Event handling with priorities
- [Camera](docs/camera.md) - Camera presets and custom camera configuration
- [UI System](docs/ui.md) - HUDs and interactive pages
- [Holograms](docs/holograms.md) - Floating text holograms
- [Entity](docs/entity.md) - Entity spawning and management
- [Player](docs/player.md) - Player and PlayerRef extensions
- [Configuration](docs/configuration.md) - JSON config management
- [Scheduler](docs/scheduler.md) - Coroutine-based task scheduling
- [Messages](docs/messages.md) - Message formatting and colors
- [Inventory](docs/inventory.md) - Inventory management
- [Permissions](docs/permissions.md) - Permission checking and groups
- [Sound](docs/sound.md) - 2D and 3D sound playback
- [Teleportation](docs/teleportation.md) - Player teleportation
- [World](docs/world.md) - World utilities

## Key Concepts

### Player vs PlayerRef

Hytale distinguishes between:

- **`Player`** - The entity in the ECS system (has inventory, game mode, etc.)
- **`PlayerRef`** - The network handle (for messages, camera, packets)

```kotlin
// Get PlayerRef from Player
val playerRef: PlayerRef = player.playerRef

// Get Player from PlayerRef (in world context)
val player: Player? = playerRef.player
```

### Game Modes

Hytale only has **two game modes**:
- `GameMode.Adventure` - Standard gameplay
- `GameMode.Creative` - Building/editing mode

There is no Survival or Spectator mode.

### TalePlugin Lifecycle

```kotlin
class MyPlugin(init: JavaPluginInit) : TalePlugin(init) {

    override fun onSetup() {
        // Called during plugin setup phase
    }

    override fun onStart() {
        // Called when plugin starts
        // Register commands, events, load configs
    }

    override fun onShutdown() {
        // Called when plugin shuts down
        // Cleanup resources, save data
    }
}
```

## Project Structure

```
TaleLib/
├── command/       # TaleCommand and registry
├── event/         # TaleEventRegistry
├── message/       # Message extensions and colors
├── camera/        # CameraBuilder, presets, extensions
├── player/        # Player/PlayerRef extensions
├── scheduler/     # Coroutine-based scheduler
├── config/        # ConfigManager
├── util/          # Vector extensions, Logger
├── world/         # World utilities
├── server/        # Server extensions
├── inventory/     # Inventory extensions
├── permission/    # Permission extensions
├── sound/         # Sound extensions
├── notification/  # Notification extensions
├── entity/        # Entity spawning system
├── teleport/      # Teleportation extensions
├── hologram/      # Hologram system
├── input/         # Input handling
├── lang/          # Localization support
└── ui/            # UI system
    ├── hud/       # HUD management
    ├── page/      # Interactive pages
    ├── element/   # Element references
    └── command/   # UI command DSL
```

## Dependencies

TaleLib includes and uses:
- [HytaleMiniFormat](https://github.com/ssquadteam/HytaleMiniFormat) - MiniMessage-style text formatting
- [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization) - JSON serialization

## License

MIT License - See LICENSE file for details.

## Credits

Developed by [SSQuadTeam](https://github.com/ssquadteam)
