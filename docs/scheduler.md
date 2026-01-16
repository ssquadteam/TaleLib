# Scheduler System

TaleLib provides a coroutine-based scheduler for asynchronous and timed operations.

## Basic Operations

### Async Execution

```kotlin
val taskId = taleScheduler.async {
    // Runs on async thread
    val data = fetchFromDatabase()

    sync {
        // Return to default dispatcher
        player.applyData(data)
    }

    delay(2.seconds)  // Suspend within async context
}
```

### IO Operations

```kotlin
taleScheduler.io {
    // Uses Dispatchers.IO for heavy I/O
    val content = Files.readString(path)
    processContent(content)
}
```

## Delayed Execution

### By Duration

```kotlin
import kotlin.time.Duration.Companion.seconds

taleScheduler.delay(5.seconds) {
    playerRef.sendMessage("5 seconds passed!".toMessage())
}
```

### By Ticks

```kotlin
import com.github.ssquadteam.talelib.scheduler.ticks

// 20 ticks = 1 second
taleScheduler.delayTicks(40) {  // 2 seconds
    playerRef.sendMessage("40 ticks passed!".toMessage())
}
```

## Repeating Tasks

### By Duration

```kotlin
val taskId = taleScheduler.repeat(
    interval = 1.seconds,
    initialDelay = 0.seconds,
    times = 10  // Optional, defaults to infinite
) { iteration ->
    playerRef.sendMessage("Tick #$iteration".toMessage())
}
```

### By Ticks

```kotlin
taleScheduler.repeatTicks(
    intervalTicks = 20,
    initialDelayTicks = 0,
    times = 5
) { iteration ->
    // Every second for 5 iterations
}
```

### Infinite Repeat

```kotlin
// Omit 'times' for infinite repeat
val taskId = taleScheduler.repeat(interval = 1.seconds) {
    updateScoreboard()
}
```

## Task Management

### Cancel Tasks

```kotlin
// Cancel specific task
taleScheduler.cancel(taskId)

// Cancel all tasks
taleScheduler.cancelAll()
```

### Check Task Status

```kotlin
taleScheduler.isRunning(taskId)    // Boolean
taleScheduler.runningTaskCount()   // Int
```

## Duration Extensions

```kotlin
import com.github.ssquadteam.talelib.scheduler.ticks

val twoSeconds = 40.ticks    // 2 seconds as Duration
val fiveSeconds = 100L.ticks // From Long
```

## Example: Countdown

```kotlin
fun startCountdown(playerRef: PlayerRef, seconds: Int) {
    var remaining = seconds

    taleScheduler.repeat(
        interval = 1.seconds,
        times = seconds
    ) { _ ->
        playerRef.send("$remaining...".warning())
        remaining--
    }

    taleScheduler.delay((seconds + 1).seconds) {
        playerRef.send("GO!".success())
    }
}
```

## Example: Auto-Save

```kotlin
class SavePlugin(init: JavaPluginInit) : TalePlugin(init) {

    private var autoSaveTask: Long = -1

    override fun onStart() {
        // Auto-save every 5 minutes
        autoSaveTask = taleScheduler.repeat(interval = 5.minutes) {
            info("Auto-saving...")
            saveAllData()
        }
    }

    override fun onShutdown() {
        taleScheduler.cancel(autoSaveTask)
        saveAllData()
    }

    private fun saveAllData() {
        taleScheduler.io {
            // Save to disk on IO thread
            dataManager.save()
        }
    }
}
```

## Example: Async Database Query

```kotlin
fun loadPlayerData(playerRef: PlayerRef) {
    taleScheduler.async {
        // Query database on async thread
        val data = database.query("SELECT * FROM players WHERE uuid = ?", playerRef.uniqueId)

        sync {
            // Apply data on main thread
            applyPlayerData(playerRef, data)
            playerRef.sendSuccess("Data loaded!")
        }
    }
}
```

## Notes

- `taleScheduler.cancelAll()` is called automatically in `onShutdown()`
- Use `io { }` for file I/O and database operations
- Use `async { }` for general async work
- Use `sync { }` inside async blocks to return to the main dispatcher
- Task IDs are returned from `async`, `delay`, and `repeat` methods
