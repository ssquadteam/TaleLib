# Vector Utilities

TaleLib provides extensions and utilities for working with Hytale vectors.

## Important: Hytale Vector Method Names

Hytale vectors use specific method names that differ from other libraries:

| Common Name | Hytale Method | Description |
|-------------|---------------|-------------|
| `distance()` | `distanceTo()` | Distance between two vectors |
| `mul()` | `scale()` | Multiply by scalar |
| `set()` | `assign()` | Copy values from another vector |

```kotlin
// ❌ Wrong - these methods don't exist!
val dist = pos1.distance(pos2)
position.set(newPos)
val scaled = direction.mul(2.0)

// ✅ Correct
val dist = pos1.distanceTo(pos2)
position.assign(newPos)
val scaled = direction.scale(2.0)
```

## Creating Vectors

```kotlin
import com.github.ssquadteam.talelib.util.*

// Create Vector3f
val v1 = vec(1f, 2f, 3f)
val v2 = vec(4, 5, 6)  // From ints

// Create Vector3d
val v3 = Vector3d(1.0, 2.0, 3.0)
```

## Arithmetic Operations

```kotlin
val sum = v1 + v2
val diff = v1 - v2
val scaled = v1 * 2f
val divided = v1 / 2f
```

## Vector Operations

```kotlin
val length = v1.length()
val lengthSq = v1.lengthSquared()
val normalized = v1.normalized()
val dot = v1.dot(v2)
val cross = v1.cross(v2)
val lerped = v1.lerp(v2, 0.5f)
val distance = v1.distanceTo(v2)
val distanceSq = v1.distanceSquaredTo(v2)
```

## Constants

```kotlin
ZERO     // (0, 0, 0)
ONE      // (1, 1, 1)
UP       // (0, 1, 0)
DOWN     // (0, -1, 0)
FORWARD  // (0, 0, 1)
BACK     // (0, 0, -1)
LEFT     // (-1, 0, 0)
RIGHT    // (1, 0, 0)
```

## Copying Values

When you need to copy values from one vector to another:

```kotlin
// Copy all values
position.assign(newPosition)

// Copy individual components
position.assign(x, y, z)
```

**Note**: The `assign()` method modifies the vector in place, while most other operations return new vectors.
