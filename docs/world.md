# World Utilities

TaleLib provides utilities for working with worlds.

## Finding Worlds

```kotlin
import com.github.ssquadteam.talelib.world.*

// Find by name
val world = findWorldByName("overworld")       // World?

// Get default world (first loaded)
val defaultWorld = getDefaultWorld()           // World?

// Get all worlds
val allWorlds = getAllWorlds()                 // Collection<World>
```

## Check World Existence

```kotlin
if (worldExists("nether")) {
    // World is loaded
}
```

## World Count

```kotlin
val count = worldCount()                       // Int
```

## World Identifier

```kotlin
val worldName = world.worldId                  // String (world's name)
```

## Important Notes

- Hytale worlds don't have UUID properties like some other game APIs
- TaleLib uses `world.name` as the unique identifier
- When storing world references in config files, use the world name string

## Example: World List Command

```kotlin
class WorldsCommand : TaleCommand("worlds", "List all worlds") {
    override fun onExecute(ctx: TaleContext) {
        val worlds = getAllWorlds()

        ctx.reply("Loaded Worlds (${worlds.size}):".info())
        worlds.forEach { world ->
            ctx.reply("  - ${world.worldId}".muted())
        }
    }
}
```

## Example: World Teleport Command

```kotlin
class WorldTpCommand : TaleCommand("worldtp", "Teleport to another world") {
    private val worldArg = stringArg("world", "World name")

    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef ?: return
        val worldName = ctx.get(worldArg)

        val world = findWorldByName(worldName)
        if (world == null) {
            ctx.reply("World '$worldName' not found".error())
            return
        }

        // Teleport to world spawn (0, 65, 0)
        playerRef.teleportToWorld(world, 0.0, 65.0, 0.0)
        ctx.reply("Teleported to world '$worldName'".success())
    }
}
```

## Example: Per-World Settings

```kotlin
@Serializable
data class WorldSettings(
    val worlds: Map<String, WorldConfig> = emptyMap()
)

@Serializable
data class WorldConfig(
    val pvpEnabled: Boolean = true,
    val mobSpawning: Boolean = true
)

class WorldPlugin(init: JavaPluginInit) : TalePlugin(init) {

    private val settingsManager = config(WorldSettings())
    val settings: WorldSettings get() = settingsManager.config

    fun getWorldConfig(world: World): WorldConfig {
        return settings.worlds[world.worldId] ?: WorldConfig()
    }

    fun isPvpEnabled(world: World): Boolean {
        return getWorldConfig(world).pvpEnabled
    }
}
```

## Example: World-Specific Spawn Points

```kotlin
@Serializable
data class SpawnPoints(
    val spawns: Map<String, SpawnLocation> = emptyMap()
)

@Serializable
data class SpawnLocation(
    val x: Double,
    val y: Double,
    val z: Double
)

class SpawnPlugin(init: JavaPluginInit) : TalePlugin(init) {

    private val spawnManager = config(SpawnPoints(), "spawns.json")

    fun getWorldSpawn(world: World): SpawnLocation {
        return spawnManager.config.spawns[world.worldId]
            ?: SpawnLocation(0.0, 65.0, 0.0)
    }

    fun setWorldSpawn(world: World, x: Double, y: Double, z: Double) {
        spawnManager.update {
            copy(spawns = spawns + (world.worldId to SpawnLocation(x, y, z)))
        }
    }
}
```
