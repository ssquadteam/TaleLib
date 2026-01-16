# Projectile System

TaleLib provides extensions for spawning and managing projectiles.

## Shooting Projectiles

### From Player Look Direction

```kotlin
import com.github.ssquadteam.talelib.projectile.*

// Shoot projectile in player's look direction
val projectile = playerRef.shootProjectile(
    commandBuffer,
    configId = "hytale:arrow",
    velocityMultiplier = 1.5
)

// With custom configuration via DSL
playerRef.shootProjectile(commandBuffer) {
    config("hytale:magic_bolt")
    velocityMultiplier(2.0)
}
```

### At Specific Target

```kotlin
// Shoot at coordinates
playerRef.shootProjectileAt(
    commandBuffer,
    configId = "hytale:fireball",
    targetX = 100.0,
    targetY = 64.0,
    targetZ = 200.0,
    velocityMultiplier = 1.0
)

// Shoot at another player
playerRef.shootProjectileAt(
    commandBuffer,
    configId = "hytale:arrow",
    target = targetPlayerRef
)
```

## Direction Utilities

```kotlin
// Calculate direction between two points
val direction = directionTo(
    sourceX, sourceY, sourceZ,
    targetX, targetY, targetZ
)  // Returns normalized Vector3d

// Create direction from yaw/pitch angles
val direction = directionFromAngles(yaw = 45f, pitch = -10f)

// Add random spread to a direction
val spreadDirection = direction.withSpread(spreadDegrees = 5.0)
```

## Configuration Utilities

```kotlin
// Get projectile config by ID
val config = getProjectileConfig("hytale:arrow")  // ProjectileConfig?

// Check if config exists
if (projectileConfigExists("hytale:custom_projectile")) {
    // Config is available
}

// Get all available projectile config IDs
val allConfigs = getAllProjectileConfigIds()  // List<String>
```

## Player Utilities

```kotlin
// Get player's eye position (for projectile origin)
val eyePos = playerRef.getEyePosition()  // Vector3d?

// Get player's look direction
val lookDir = playerRef.getLookDirection()  // Vector3d?
```

## Notes

- Projectile spawning requires a `CommandBuffer<EntityStore>` from a system context
- The `velocityMultiplier` parameter scales the base projectile speed
- Direction vectors are automatically normalized
- Spread uses random yaw and pitch offsets within the specified degree range
