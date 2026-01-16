# Player Extensions

TaleLib provides extensions for both `Player` (entity) and `PlayerRef` (network handle).

## Player vs PlayerRef

Hytale distinguishes between:

- **`Player`** - The entity in the ECS system (has inventory, game mode, etc.)
- **`PlayerRef`** - The network handle (for messages, camera, packets)

```kotlin
// Get PlayerRef from Player
val playerRef: PlayerRef = player.playerRef

// In events
taleEvents.on<PlayerReadyEvent> { event ->
    val player: Player = event.player
    val playerRef: PlayerRef = player.playerRef
}

taleEvents.on<PlayerChatEvent> { event ->
    val playerRef: PlayerRef = event.sender
}
```

## PlayerRef Extensions

### Messaging

```kotlin
import com.github.ssquadteam.talelib.player.*

// Basic messaging
playerRef.send("Hello!")
playerRef.send(message)

// Styled messages
playerRef.sendSuccess("Operation completed!")
playerRef.sendError("Something went wrong!")
playerRef.sendWarning("Be careful!")
playerRef.sendInfo("FYI...")

// With prefix
playerRef.sendPrefixed("MyPlugin", "Hello!", Colors.GOLD)
```

### Properties

```kotlin
playerRef.name         // String (username)
playerRef.uniqueId     // UUID
playerRef.packetHandler  // PacketHandler
playerRef.isOnline     // Boolean (checks channel active)
```

### Display Helpers

```kotlin
playerRef.displayString()              // Returns username
playerRef.displayMessage(Colors.AQUA)  // Returns colored Message
```

## Player Entity Extensions

### Game Mode

Hytale only has two game modes: Adventure and Creative.

```kotlin
player.isCreative   // Check if in creative mode
player.isAdventure  // Check if in adventure mode
```

### View Distance

```kotlin
player.setViewDistance(12)
```

### Position and World

```kotlin
val position = player.position  // Vector3d?
val world = player.world        // World
```

## Example: Welcome Message

```kotlin
taleEvents.on<PlayerReadyEvent> { event ->
    val player = event.player
    val playerRef = player.playerRef

    playerRef.sendSuccess("Welcome to the server, ${playerRef.name}!")

    if (player.isCreative) {
        playerRef.sendInfo("You are in Creative mode")
    }
}
```

## Example: Player Info Command

```kotlin
class PlayerInfoCommand : TaleCommand("playerinfo", "Show player info") {
    private val targetArg = optionalPlayerRef("player", "Target player")

    override fun onExecute(ctx: TaleContext) {
        val target = ctx.get(targetArg) ?: ctx.playerRef
        if (target == null) {
            ctx.reply("Specify a player or run as player".error())
            return
        }

        ctx.reply("Player: ${target.name}".info())
        ctx.reply("UUID: ${target.uniqueId}".muted())
        ctx.reply("Online: ${target.isOnline}".muted())
    }
}
```
