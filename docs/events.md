# Event System

TaleLib provides a type-safe event system with priorities and keyed event support.

## Basic Event Handling

Register events using the `on` extension function:

```kotlin
import com.github.ssquadteam.talelib.event.on
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent

override fun onStart() {
    // Simple event handler
    taleEvents.on<PlayerReadyEvent> { event ->
        event.player.sendMessage("Welcome!".success())
    }
}
```

## Event Priority

Control the order handlers execute with priority:

```kotlin
import com.hypixel.hytale.server.core.event.EventPriority

// Using EventPriority enum
taleEvents.on<PlayerChatEvent>(EventPriority.HIGH) { event ->
    // Runs before normal priority handlers
}

taleEvents.on<PlayerChatEvent>(EventPriority.LOW) { event ->
    // Runs after normal priority handlers
}

// Using numeric priority (higher = earlier)
taleEvents.on<PlayerChatEvent>(100.toShort()) { event ->
    // Custom priority value
}
```

## Keyed Events

Some events have a key type for filtering:

```kotlin
// Only fires for a specific key
taleEvents.onKeyed<SomeKeyedEvent>("specific_key") { event ->
    // Handle event for this key
}

// Fires for all keys (global handler)
taleEvents.onGlobal<SomeKeyedEvent> { event ->
    // Handle all instances
}

// Fires if no keyed handler matched
taleEvents.onUnhandled<SomeKeyedEvent> { event ->
    // Fallback handler
}
```

## Common Events

### Player Events

```kotlin
// Player joins and is ready
taleEvents.on<PlayerReadyEvent> { event ->
    val player: Player = event.player
    val playerRef: PlayerRef = player.playerRef
}

// Player chat
taleEvents.on<PlayerChatEvent> { event ->
    val sender: PlayerRef = event.sender
    val message: String = event.message
}

// Player disconnect
taleEvents.on<PlayerDisconnectEvent> { event ->
    val player: PlayerRef = event.player
}
```

### Mouse/Input Events

```kotlin
taleEvents.on<PlayerMouseButtonEvent> { event ->
    if (event.isLeftPress()) {
        // Left click
    }
}

taleEvents.on<PlayerMouseMotionEvent> { event ->
    val dx = event.deltaX
    val dy = event.deltaY
}
```

## Example: Chat Filter

```kotlin
class ChatPlugin(init: JavaPluginInit) : TalePlugin(init) {

    private val bannedWords = listOf("spam", "badword")

    override fun onStart() {
        taleEvents.on<PlayerChatEvent>(EventPriority.HIGH) { event ->
            val message = event.message.lowercase()

            for (word in bannedWords) {
                if (message.contains(word)) {
                    event.sender.sendError("Message blocked: contains banned word")
                    // Cancel the event if supported
                    return@on
                }
            }
        }
    }
}
```

## Example: Join/Leave Messages

```kotlin
override fun onStart() {
    taleEvents.on<PlayerReadyEvent> { event ->
        val name = event.player.playerRef.name
        broadcastMessage("$name joined the server!".success())
    }

    taleEvents.on<PlayerDisconnectEvent> { event ->
        val name = event.player.name
        broadcastMessage("$name left the server".muted())
    }
}
```
