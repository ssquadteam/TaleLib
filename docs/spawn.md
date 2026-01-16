# Spawn System

TaleLib provides extensions for managing player spawn points.

## Getting Spawn Point

```kotlin
import com.github.ssquadteam.talelib.spawn.*

// Get player's current spawn point
val spawnPoint = playerRef.getSpawnPoint()  // Transform?

spawnPoint?.let {
    val position = it.position  // Vector3d
    val rotation = it.rotation  // Vector3f
}
```

## Setting Spawn Point

```kotlin
// Set spawn at coordinates
playerRef.setSpawnPoint(x = 100.0, y = 64.0, z = 200.0)

// Set spawn with rotation
playerRef.setSpawnPoint(
    x = 100.0, y = 64.0, z = 200.0,
    yaw = 90f, pitch = 0f
)

// Set spawn from Transform
playerRef.setSpawnPoint(transform)
```

## Teleporting to Spawn

```kotlin
// Teleport player to their spawn point
playerRef.teleportToSpawn()
```

## Distance Utilities

```kotlin
// Get distance from player to their spawn point
val distance = playerRef.distanceToSpawn()  // Double?

// Check if player is within radius of spawn
val atSpawn = playerRef.isAtSpawn(radius = 5.0)  // Boolean
```

## Notes

- Spawn points are stored per-player in the world
- `getSpawnPoint()` returns `null` if no spawn point is set
- `distanceToSpawn()` returns `null` if spawn point or player position is unavailable
- `isAtSpawn()` returns `false` if distance cannot be calculated
