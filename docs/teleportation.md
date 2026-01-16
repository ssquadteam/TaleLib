# Teleportation System

TaleLib provides extensions for teleporting players.

## Basic Teleportation

```kotlin
import com.github.ssquadteam.talelib.teleport.*

// Teleport to coordinates
playerRef.teleport(x, y, z)

// With rotation
playerRef.teleport(x, y, z, yaw = 90f, pitch = 0f)

// Teleport to Vector3d
playerRef.teleport(position)

// With position and rotation vectors
playerRef.teleport(position, rotation)  // Vector3d, Vector3f
```

## Teleport to Player

```kotlin
playerRef.teleportTo(targetPlayerRef)
```

## Cross-World Teleport

```kotlin
playerRef.teleportToWorld(targetWorld, x, y, z)
```

## Relative Teleport

```kotlin
// Move relative to current position
playerRef.teleportRelative(dx = 0.0, dy = 10.0, dz = 0.0)  // Move up 10 blocks
playerRef.teleportRelative(dx = 5.0, dy = 0.0, dz = 0.0)   // Move 5 blocks in X
```

## Teleport Builder DSL

```kotlin
playerRef.teleport {
    position(x, y, z)
    rotation(yaw, pitch)
    keepVelocity()  // Preserve momentum
}
```

## Example: Spawn Command

```kotlin
class SpawnCommand : TaleCommand("spawn", "Teleport to spawn") {
    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef ?: return

        playerRef.teleport(0.0, 65.0, 0.0, yaw = 0f, pitch = 0f)
        ctx.reply("Teleported to spawn!".success())
    }
}
```

## Example: Teleport to Player Command

```kotlin
class TpCommand : TaleCommand("tp", "Teleport to a player") {
    private val targetArg = playerRefArg("player", "Target player")

    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef ?: return
        val target = ctx.get(targetArg)

        playerRef.teleportTo(target)
        ctx.reply("Teleported to ${target.name}".success())
    }
}
```

## Example: Warp System

```kotlin
@Serializable
data class WarpData(
    val warps: Map<String, WarpLocation> = emptyMap()
)

@Serializable
data class WarpLocation(
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f
)

class WarpPlugin(init: JavaPluginInit) : TalePlugin(init) {

    private val dataManager = config(WarpData(), "warps.json")
    val data: WarpData get() = dataManager.config

    override fun onStart() {
        dataManager.load()
        taleCommands.register(WarpCommand())
        taleCommands.register(SetWarpCommand())
    }

    inner class WarpCommand : TaleCommand("warp", "Teleport to a warp") {
        private val warpArg = stringArg("name", "Warp name")

        override fun onExecute(ctx: TaleContext) {
            val playerRef = ctx.playerRef ?: return
            val warpName = ctx.get(warpArg)

            val warp = data.warps[warpName]
            if (warp == null) {
                ctx.reply("Warp '$warpName' not found".error())
                return
            }

            val world = findWorldByName(warp.worldName)
            if (world == null) {
                ctx.reply("World not found".error())
                return
            }

            playerRef.teleportToWorld(world, warp.x, warp.y, warp.z)
            playerRef.teleport {
                rotation(warp.yaw, warp.pitch)
            }
            ctx.reply("Warped to $warpName".success())
        }
    }

    inner class SetWarpCommand : TaleCommand("setwarp", "Create a warp") {
        private val nameArg = stringArg("name", "Warp name")

        override fun onExecute(ctx: TaleContext) {
            val player = ctx.requirePlayer() ?: return
            val playerRef = ctx.playerRef ?: return
            val warpName = ctx.get(nameArg)

            val pos = player.position ?: return
            val rot = player.rotation

            val warp = WarpLocation(
                worldName = player.world.worldId,
                x = pos.x,
                y = pos.y,
                z = pos.z,
                yaw = rot?.x ?: 0f,
                pitch = rot?.y ?: 0f
            )

            dataManager.update {
                copy(warps = warps + (warpName to warp))
            }

            ctx.reply("Created warp '$warpName'".success())
        }
    }
}
```

## Example: Random Teleport

```kotlin
class RTPCommand : TaleCommand("rtp", "Random teleport") {
    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef ?: return

        val randomX = Random.nextDouble(-1000.0, 1000.0)
        val randomZ = Random.nextDouble(-1000.0, 1000.0)

        // Teleport to Y=100 initially, then let player fall
        playerRef.teleport(randomX, 100.0, randomZ)
        ctx.reply("Randomly teleported!".success())
    }
}
```
