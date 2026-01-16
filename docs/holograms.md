# Hologram System

TaleLib provides floating text holograms using invisible entities with Nameplate components.

## Creating Holograms

### Single Line Hologram

```kotlin
import com.github.ssquadteam.talelib.hologram.*

// Create at coordinates
val hologram = world.createHologram("Hello World!", x, y, z)

// Create at position vector
val hologram2 = world.createHologram("Welcome!", position)  // Vector3d
```

### Multi-Line Hologram

```kotlin
val holograms = world.createMultiLineHologram(
    listOf("Line 1", "Line 2", "Line 3"),
    x, y, z,
    lineSpacing = 0.25  // Vertical spacing between lines
)
```

### DSL Builder (Single Line)

```kotlin
val holo = world.hologramSingle {
    text("Welcome!")
    position(100.0, 65.0, 200.0)
}
```

### DSL Builder (Multi-Line)

```kotlin
val holos = world.hologram {
    line("=== Server Info ===")
    line("Players: 42")
    line("TPS: 20")
    position(x, y, z)
    spacing(0.3)
}

// Or use lines() for multiple at once
val holos2 = world.hologram {
    lines("Line 1", "Line 2", "Line 3")
    position(x, y, z)
}
```

## Manipulating Holograms

```kotlin
hologram?.let { h ->
    // Update text
    h.text = "Updated Text"

    // Get/set position
    val pos = h.position  // Vector3d?
    h.setPosition(newX, newY, newZ)
    h.setPosition(newPosition)  // Vector3d

    // Check validity and remove
    h.isValid()  // Boolean
    h.remove()   // Despawn from world
}
```

## HologramManager

Track and manage all holograms:

```kotlin
HologramManager.get(hologramId)   // Hologram?
HologramManager.getAll()          // Collection<Hologram>
HologramManager.removeAll()       // Cleanup all holograms
HologramManager.count()           // Int
```

## Entity Persistence

Hytale automatically persists entities with `UUIDComponent`. This affects holograms across server restarts.

### The Problem

If you spawn holograms on startup without tracking, you'll create duplicates:

```kotlin
// WRONG - causes duplicates on every restart!
override fun onStart() {
    myHolograms.forEach { data ->
        world.createHologram(data.text, data.x, data.y, data.z)
    }
}
```

### The Solution - UUID-Based Tracking

Store entity UUIDs when spawning and trust persistence on restart:

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class MyHologramData(
    val name: String,
    val lines: List<String>,
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val entityUuids: List<String> = emptyList()  // Track spawned entity UUIDs
)

fun spawnHologram(data: MyHologramData) {
    val world = findWorldByName(data.worldName) ?: return

    // If UUIDs exist and match line count - DON'T spawn!
    // Entities are already persisted in the world
    if (data.entityUuids.isNotEmpty() && data.entityUuids.size == data.lines.size) {
        logger.info("Hologram '${data.name}' using ${data.entityUuids.size} persisted entities")
        return
    }

    // No UUIDs or count mismatch - spawn fresh entities
    world.execute {
        val newUuids = mutableListOf<String>()

        data.lines.forEachIndexed { index, line ->
            val lineY = data.y + (data.lines.size - 1 - index) * 0.25

            val holograms = world.hologram {
                text(line)
                position(data.x, lineY, data.z)
            }

            holograms.firstOrNull()?.entityUuid?.let { uuid ->
                newUuids.add(uuid.toString())
            }
        }

        // Save UUIDs for next restart
        if (newUuids.isNotEmpty()) {
            saveHologramUuids(data.name, newUuids)
        }
    }
}
```

### Updating Content

When content changes, remove old entities and spawn new ones:

```kotlin
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

fun updateHologramLines(name: String, newLines: List<String>) {
    val data = getHologramData(name) ?: return
    val world = findWorldByName(data.worldName) ?: return

    // Remove old persisted entities from world
    if (data.entityUuids.isNotEmpty()) {
        world.execute {
            val entityStore = world.entityStore ?: return@execute
            val store = entityStore.store

            data.entityUuids.forEach { uuidStr ->
                try {
                    val uuid = UUID.fromString(uuidStr)
                    val entityRef = entityStore.getRefFromUUID(uuid)
                    if (entityRef != null && entityRef.isValid) {
                        store.removeEntity(
                            entityRef,
                            EntityStore.REGISTRY.newHolder(),
                            RemoveReason.REMOVE
                        )
                    }
                } catch (e: Exception) {
                    // Entity may already be removed
                }
            }
        }
    }

    // Save with cleared UUIDs to force respawn
    val updated = data.copy(
        lines = newLines,
        entityUuids = emptyList()
    )
    saveHologramData(updated)

    // Spawn with new content
    spawnHologram(updated)
}
```

### Quick Reference

| Scenario | Action |
|----------|--------|
| Server startup with stored UUIDs | DON'T spawn - trust persistence |
| Server startup with no UUIDs | Spawn new entities, save UUIDs |
| Content changed (lines edited) | Remove old entities, clear UUIDs, spawn new |
| Hologram deleted | Remove entities from world by UUID |
| Hologram cloned | Clone with empty UUIDs (forces new spawn) |
| Position changed | Remove old entities, clear UUIDs, spawn at new position |

## How Holograms Work Internally

Holograms are invisible entities with a visible nameplate:

- Creates an entity with `ProjectileComponent("Projectile")` as a valid entity shell
- Adds `TransformComponent` for position
- Adds `UUIDComponent` and `Intangible` (non-collidable)
- Adds `NetworkId` for network synchronization
- Adds `Nameplate` component with the hologram text

## Important Notes

1. **Plain Text Only**: Hytale's `Nameplate` component only supports plain text. MiniMessage tags and colors will display as raw text.

2. **WorldThread Requirement**: Entity operations must run on the WorldThread. Use `world.execute { }` for spawning and removing entities.

3. **Entity UUID**: The `Hologram` class provides `entityUuid` which is the UUID from `UUIDComponent`. This is what Hytale uses for persistence.

## Example: Welcome Hologram

```kotlin
class WelcomePlugin(init: JavaPluginInit) : TalePlugin(init) {

    override fun onStart() {
        val world = getDefaultWorld() ?: return

        world.execute {
            world.hologram {
                line("Welcome to the Server!")
                line("Use /help for commands")
                position(0.0, 70.0, 0.0)
                spacing(0.3)
            }
        }
    }
}
```
