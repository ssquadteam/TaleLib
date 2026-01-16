# Notification System

TaleLib provides a system for displaying toast-style notifications to players.

## Simple Notifications

```kotlin
import com.github.ssquadteam.talelib.notification.*

// Default notification
playerRef.notifyDefault("Hello!")
playerRef.notifyDefault(message)  // Message object

// With secondary text
playerRef.notifyDefault("Quest Complete!", "Earned 100 XP")
```

## Notification Builder DSL

```kotlin
playerRef.notification {
    message("Achievement Unlocked!")
    secondary("First Blood")
    defaultStyle()  // NotificationStyle.Default
}
```

## Important Note

Only `NotificationStyle.Default` is currently available in the Hytale API. Success/Warning/Error styles may be added in future updates.

## Example: Achievement Notification

```kotlin
fun unlockAchievement(playerRef: PlayerRef, name: String, description: String) {
    playerRef.notification {
        message(name)
        secondary(description)
        defaultStyle()
    }

    // Also play a sound
    playerRef.playUISound("hytale:ui/achievement")
}
```

## Example: Quest Completion

```kotlin
fun completeQuest(playerRef: PlayerRef, questName: String, reward: Int) {
    playerRef.notifyDefault(
        "Quest Complete!",
        "$questName - Earned $reward coins"
    )
}
```

## Example: Level Up

```kotlin
fun onLevelUp(playerRef: PlayerRef, newLevel: Int) {
    playerRef.notification {
        message("Level Up!")
        secondary("You are now level $newLevel")
        defaultStyle()
    }
}
```

## Example: System Messages

```kotlin
class NotificationPlugin(init: JavaPluginInit) : TalePlugin(init) {

    fun notifyServerRestart(minutes: Int) {
        onlinePlayers.forEach { playerRef ->
            playerRef.notifyDefault(
                "Server Restart",
                "Server will restart in $minutes minutes"
            )
        }
    }

    fun notifyMaintenanceMode() {
        onlinePlayers.forEach { playerRef ->
            playerRef.notifyDefault(
                "Maintenance Mode",
                "Server entering maintenance"
            )
        }
    }
}
```
