# TaleLib

A flexible, expandable Kotlin library for Hytale plugin development.

## Features

- **Command System** - Declarative command building with argument parsing and subcommands
- **Event System** - Type-safe event handling with priorities and keyed events
- **Camera System** - Presets and DSL for camera control (Top-Down, Isometric, Side-Scroller, etc.)
- **UI System** - HUDs and interactive pages with element bindings
- **Hologram System** - Floating text holograms with multi-line support
- **Entity System** - Model-based entity spawning and management
- **Projectile System** - Projectile spawning with direction and spread utilities
- **Spawn System** - Spawn point management and teleportation
- **Stat System** - Stat modifier management (health, mana, etc.)
- **Time System** - World time and time dilation control
- **Prefab System** - Prefab config access and rotation utilities
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
- [Input](docs/input.md) - Mouse, keyboard-mapped actions, movement states, hold detection
- [Camera](docs/camera.md) - Camera presets and custom camera configuration
- [UI System](docs/ui.md) - HUDs and interactive pages
- [Holograms](docs/holograms.md) - Floating text holograms
- [Entity](docs/entity.md) - Entity spawning and management
- [Projectiles](docs/projectiles.md) - Projectile spawning and direction utilities
- [Spawn](docs/spawn.md) - Spawn point management
- [Stats](docs/stats.md) - Stat modifier system
- [Time](docs/time.md) - World time and dilation
- [Prefabs](docs/prefabs.md) - Prefab utilities
- [Player](docs/player.md) - Player and PlayerRef extensions
- [Configuration](docs/configuration.md) - JSON config management
- [Scheduler](docs/scheduler.md) - Coroutine-based task scheduling
- [Messages](docs/messages.md) - Message formatting and colors
- [Notifications](docs/notifications.md) - Player notifications
- [Inventory](docs/inventory.md) - Inventory management
- [Permissions](docs/permissions.md) - Permission checking and groups
- [Sound](docs/sound.md) - 2D and 3D sound playback
- [Teleportation](docs/teleportation.md) - Player teleportation
- [World](docs/world.md) - World utilities
- [Vectors](docs/vectors.md) - Vector utilities and method names

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

### PlayerRef Property Access

**Important**: `PlayerRef` does NOT have direct `world` or `position` properties:

```kotlin
// ❌ Wrong - these don't exist!
val world = playerRef.world
val pos = playerRef.position

// ✅ Correct
val ref = playerRef.reference              // Ref<EntityStore>? (not .ref!)
val pos = playerRef.transform.position     // Vector3d?

// Getting World from PlayerRef
private fun PlayerRef.getWorld(): World? {
    val ref = this.reference ?: return null
    return (ref.store.externalData as? EntityStore)?.world
}
```

### Vector Method Names

Hytale vectors use specific method names:

```kotlin
val dist = pos1.distanceTo(pos2)     // ✅ (not .distance())
position.assign(newPosition)          // ✅ (not .set())
val scaled = direction.scale(2.0)    // ✅ (not .mul())
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
├── spawn/         # Spawn point management
├── projectile/    # Projectile spawning and direction utilities
├── prefab/        # Prefab config access and rotation
├── stat/          # Stat modifier management
├── time/          # World time and dilation
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
