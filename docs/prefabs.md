# Prefab System

TaleLib provides utilities for working with Hytale prefabs.

## Getting Prefab Configs

```kotlin
import com.github.ssquadteam.talelib.prefab.*

// Get prefab configuration by ID
val config = getPrefabConfig("hytale:tree")  // PrefabConfig?

// Check if prefab exists
if (prefabConfigExists("hytale:custom_structure")) {
    // Prefab is available
}

// Get all available prefab config IDs
val allPrefabs = getAllPrefabConfigIds()  // List<String>
```

## Rotation Conversion

Prefabs use `PrefabRotation` which needs to be converted for other uses:

```kotlin
// Convert PrefabRotation to Rotation
val rotation = toRotation(prefabRotation)  // Rotation

// Get yaw degrees from PrefabRotation
val yawDegrees = prefabRotation.toYawDegrees()  // Float
```

### Why Rotation Conversion?

The `PrefabRotation.rotation` field is private in the Hytale API. TaleLib provides these helper functions to extract rotation data:

```kotlin
// ❌ Wrong - rotation is private
val rot = prefabRotation.rotation  // Cannot access!

// ✅ Correct - use TaleLib helpers
val rot = toRotation(prefabRotation)
val yaw = prefabRotation.toYawDegrees()
```

## Notes

- Prefab configs define structure templates that can be placed in the world
- The rotation conversion uses `Rotation.ofDegrees()` internally
- Use `getAllPrefabConfigIds()` to discover available prefabs at runtime
