# Time System

TaleLib provides extensions for managing world time and time dilation.

## World Time

```kotlin
import com.github.ssquadteam.talelib.time.*

// Set world time (0-24000 ticks)
world.setTime(0)      // Midnight
world.setTime(6000)   // Morning/Sunrise
world.setTime(12000)  // Noon
world.setTime(18000)  // Evening/Sunset
```

## Time Dilation

Time dilation controls how fast time passes in the world.

```kotlin
// Set time dilation (speed multiplier)
world.setTimeDilation(1.0f)   // Normal speed
world.setTimeDilation(2.0f)   // 2x speed (day passes twice as fast)
world.setTimeDilation(0.5f)   // Half speed (day passes twice as slow)
world.setTimeDilation(4.0f)   // Maximum speed (4x)
```

### Dilation Limits

- Minimum: `0.01f`
- Maximum: `4.0f`

## Sending Time Updates

```kotlin
// Send current time to a specific player
playerRef.sendTime()
```

This is useful when you need to synchronize a player's client with the server time after they join or after time changes.

## Time Values Reference

| Time | Ticks | Description |
|------|-------|-------------|
| Midnight | 0 | Start of day cycle |
| Sunrise | 6000 | Morning begins |
| Noon | 12000 | Middle of day |
| Sunset | 18000 | Evening begins |
| Full cycle | 24000 | Complete day/night cycle |

## Notes

- Time is measured in ticks (20 ticks = 1 real second at normal speed)
- Time dilation affects all time-based mechanics in the world
- Players receive time updates automatically, but `sendTime()` can force an update
