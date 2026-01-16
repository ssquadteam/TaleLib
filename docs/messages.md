# Message System

TaleLib provides message formatting with colors and styling.

## Quick Styling

```kotlin
import com.github.ssquadteam.talelib.message.*

"Success!".success()    // Green
"Error!".error()        // Red
"Warning!".warning()    // Yellow
"Info".info()           // Aqua
"Muted".muted()         // Gray
"Highlight".highlight() // Gold
```

## String to Message

```kotlin
"Hello".toMessage()
```

## Combining Messages

```kotlin
// Using messages() function
val msg = messages(
    "Hello, ".toMessage(),
    playerName.toMessage().color(Colors.AQUA)
)

// Using + operator
val msg2 = "Hello, ".toMessage() + playerName.toMessage().color(Colors.AQUA)
```

## Prefixed Messages

```kotlin
import com.github.ssquadteam.hytaleminiformat.Colors

"Server started!".withPrefix("MyPlugin", Colors.GOLD)
// Result: [MyPlugin] Server started!
```

## Utility Functions

```kotlin
separator()     // Creates "----------------------------------------" line
newline()       // Creates "\n"
emptyMessage()  // Empty message
```

## Colors

```kotlin
import com.github.ssquadteam.hytaleminiformat.Colors

// Standard colors
Colors.WHITE, Colors.BLACK, Colors.RED, Colors.GREEN, Colors.BLUE
Colors.YELLOW, Colors.AQUA, Colors.GRAY, Colors.DARK_GRAY
Colors.GOLD, Colors.PURPLE, Colors.ORANGE, Colors.PINK

// Semantic colors
Colors.SUCCESS   // Green
Colors.ERROR     // Red
Colors.WARNING   // Yellow
Colors.INFO      // Aqua
Colors.MUTED     // Gray
Colors.HIGHLIGHT // Gold
Colors.PRIMARY   // Blue

// TaleLib branding
Colors.TALELIB   // Purple
```

## Using Colors

```kotlin
"Hello".toMessage().color(Colors.GOLD)
```

## Rich Text with HytaleMiniFormat

TaleLib includes HytaleMiniFormat for advanced formatting:

```kotlin
import com.github.ssquadteam.hytaleminiformat.MiniFormat

// Gradients
val gradient = MiniFormat.parse("<gradient:#ff0000:#0000ff>Rainbow Text</gradient>")

// Hex colors
val hex = MiniFormat.parse("<#00FF00>Hex Color")

// Styled text
val styled = MiniFormat.parse("<bold><italic>Styled</italic></bold>")

// Combined
val fancy = MiniFormat.parse("<gradient:#ff0000:#ffff00><bold>Fancy Title</bold></gradient>")
```

## Sending Messages

```kotlin
// Using PlayerRef extensions
playerRef.send("Hello!")
playerRef.send(message)
playerRef.sendSuccess("Done!")
playerRef.sendError("Failed!")
playerRef.sendWarning("Careful!")
playerRef.sendInfo("Note:")

// Using command context
ctx.reply("Hello!")
ctx.reply(message)
```

## Example: Formatted Help Command

```kotlin
class HelpCommand : TaleCommand("help", "Show help") {
    override fun onExecute(ctx: TaleContext) {
        ctx.reply(separator())
        ctx.reply("Server Commands".highlight())
        ctx.reply(separator())
        ctx.reply("/spawn".info() + " - Teleport to spawn".muted())
        ctx.reply("/home".info() + " - Teleport home".muted())
        ctx.reply("/shop".info() + " - Open shop".muted())
        ctx.reply(separator())
    }
}
```

## Example: Status Display

```kotlin
fun showStatus(playerRef: PlayerRef, health: Int, coins: Int) {
    playerRef.send(separator())
    playerRef.send("Player Status".withPrefix("Server", Colors.GOLD))
    playerRef.send(
        "Health: ".muted() + "$health HP".success()
    )
    playerRef.send(
        "Coins: ".muted() + "$coins".highlight()
    )
    playerRef.send(separator())
}
```
