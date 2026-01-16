# Entity System

TaleLib provides a system for spawning and managing entities with models.

## Spawning Entities

### DSL Builder (Recommended)

```kotlin
import com.github.ssquadteam.talelib.entity.*

val entity = world.spawnEntity {
    model("Minecart")                     // Model asset ID
    position(100.0, 64.0, 200.0)
    rotation(0f, 180f, 0f)                // yaw, pitch, roll
    scale(1.5f)                           // Model scale
    interactable(true)                    // Enable interactions
}
```

### Simple Spawn

```kotlin
// Basic spawn
val minecart = world.spawnEntity("Minecart", x, y, z)

// With scale
val scaledEntity = world.spawnEntity("Kweebec", x, y, z, scale = 2.0f)

// With position vector
val npc = world.spawnEntity("Trork", position, interactable = true)
```

## Manipulating Entities

```kotlin
entity?.let { e ->
    // Position
    e.setPosition(newX, newY, newZ)
    e.setPosition(newPosition)            // Vector3d
    val pos = e.position                  // Vector3d?

    // Rotation
    e.setRotation(yaw, pitch, roll)
    e.setRotation(newRotation)            // Vector3f
    val rot = e.rotation                  // Vector3f?

    // Model info
    val model = e.getModel()              // Model?
    val box = e.getBoundingBox()          // Box?

    // State
    e.isValid()                           // Boolean
    e.remove()                            // Despawn
}
```

## SpawnedEntityManager

Track all spawned entities:

```kotlin
SpawnedEntityManager.get(entityId)        // SpawnedEntity?
SpawnedEntityManager.getAll()             // Collection<SpawnedEntity>
SpawnedEntityManager.getByModel("Minecart")  // List<SpawnedEntity>
SpawnedEntityManager.removeAll()          // Cleanup all
SpawnedEntityManager.count()              // Int
```

## Low-Level Entity Utilities

For working with any entity reference:

```kotlin
entityRef.getEntity(store)                // Entity?
entityRef.getPosition(store)              // Vector3d?
entityRef.setPosition(store, x, y, z)
entityRef.getRotation(store)              // Vector3f?
entityRef.setRotation(store, yaw, pitch, roll)
entityRef.remove(store)                   // Despawn
```

## Components Added Automatically

When spawning entities, these components are added:

- `TransformComponent` - position and rotation
- `PersistentModel` - model reference for persistence
- `ModelComponent` - actual model rendering
- `BoundingBox` - collision detection
- `NetworkId` - network synchronization
- `UUIDComponent` - unique entity identity
- `Interactions` + `Interactable` (if `interactable = true`)

## Example: NPC Spawner

```kotlin
class NPCPlugin(init: JavaPluginInit) : TalePlugin(init) {

    private val npcs = mutableListOf<SpawnedEntity>()

    override fun onStart() {
        taleCommands.register(SpawnNPCCommand())
    }

    inner class SpawnNPCCommand : TaleCommand("spawnnpc", "Spawn an NPC") {
        private val modelArg = stringArg("model", "Model name")

        override fun onExecute(ctx: TaleContext) {
            val player = ctx.requirePlayer() ?: return
            val model = ctx.get(modelArg)
            val world = player.world

            world.execute {
                val entity = world.spawnEntity {
                    model(model)
                    position(player.position)
                    interactable(true)
                }

                entity?.let {
                    npcs.add(it)
                    ctx.reply("Spawned $model NPC".success())
                } ?: ctx.reply("Failed to spawn NPC".error())
            }
        }
    }

    override fun onShutdown() {
        npcs.forEach { it.remove() }
        npcs.clear()
    }
}
```

## Example: Moving Platform

```kotlin
class PlatformPlugin(init: JavaPluginInit) : TalePlugin(init) {

    private var platform: SpawnedEntity? = null
    private var baseY = 60.0
    private var direction = 1

    override fun onStart() {
        val world = getDefaultWorld() ?: return

        world.execute {
            platform = world.spawnEntity {
                model("Minecart")  // Or custom platform model
                position(0.0, baseY, 0.0)
                scale(3.0f)
            }
        }

        // Move platform up and down
        taleScheduler.repeatTicks(10) {
            platform?.let { p ->
                val pos = p.position ?: return@repeatTicks
                val newY = pos.y + (0.1 * direction)

                if (newY > baseY + 5) direction = -1
                if (newY < baseY) direction = 1

                p.setPosition(pos.x, newY, pos.z)
            }
        }
    }

    override fun onShutdown() {
        platform?.remove()
    }
}
```

## WorldThread Requirement

Entity operations must run on the WorldThread:

```kotlin
// Correct - using world.execute
world.execute {
    val entity = world.spawnEntity("Kweebec", x, y, z)
    entity?.setPosition(newX, newY, newZ)
}

// Wrong - will cause thread assertion error
val entity = world.spawnEntity("Kweebec", x, y, z)  // May fail
```
