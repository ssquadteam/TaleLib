# Stat System

TaleLib provides extensions for managing entity stat modifiers.

## Adding Modifiers

```kotlin
import com.github.ssquadteam.talelib.stat.*
import com.hypixel.hytale.protocol.ModifierTarget

// Add a stat modifier to reduce max health
playerRef.addStatModifier(
    statId = "health",
    modifierId = "poison_effect",
    value = -5.0f,
    target = ModifierTarget.MAX
)

// Add a modifier that affects current value
playerRef.addStatModifier(
    statId = "stamina",
    modifierId = "exhaustion",
    value = -10.0f,
    target = ModifierTarget.MIN
)
```

## Removing Modifiers

```kotlin
// Remove a specific modifier
playerRef.removeStatModifier(
    statId = "health",
    modifierId = "poison_effect"
)
```

## Checking Modifiers

```kotlin
// Check if a modifier exists
val hasPoison = playerRef.hasStatModifier("health", "poison_effect")  // Boolean
```

## ModifierTarget

The `ModifierTarget` enum determines how the modifier affects the stat:

- `ModifierTarget.MAX` - Affects the maximum value of the stat
- `ModifierTarget.MIN` - Affects the minimum value of the stat

## Common Use Cases

### Temporary Debuffs

```kotlin
// Apply poison that reduces max health
playerRef.addStatModifier("health", "poison_$uniqueId", -20.0f, ModifierTarget.MAX)

// Remove after duration
taleScheduler.delay(10.seconds) {
    playerRef.removeStatModifier("health", "poison_$uniqueId")
}
```

### Equipment Bonuses

```kotlin
// Add armor bonus
playerRef.addStatModifier("defense", "iron_armor", 15.0f, ModifierTarget.MAX)

// Remove when unequipped
playerRef.removeStatModifier("defense", "iron_armor")
```

## Notes

- Modifier IDs should be unique per stat to avoid conflicts
- Use descriptive modifier IDs that include context (e.g., `"potion_strength_boost"`)
- Modifiers persist until explicitly removed
- Negative values reduce the stat, positive values increase it
