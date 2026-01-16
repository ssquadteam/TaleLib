# Command System

TaleLib provides a declarative command system with argument parsing, subcommands, and tab completion.

## Creating Commands

Extend `TaleCommand` to create a command:

```kotlin
import com.github.ssquadteam.talelib.command.TaleCommand
import com.github.ssquadteam.talelib.command.TaleContext

class TeleportCommand : TaleCommand("teleport", "Teleport to coordinates") {

    // Define arguments
    private val xArg = floatArg("x", "X coordinate")
    private val yArg = floatArg("y", "Y coordinate")
    private val zArg = floatArg("z", "Z coordinate")

    init {
        // Add aliases
        aliases("tp", "goto")

        // Add subcommands
        subCommand(SpawnCommand())
    }

    override fun onExecute(ctx: TaleContext) {
        val player = ctx.requirePlayer() ?: return

        val x = ctx.get(xArg)
        val y = ctx.get(yArg)
        val z = ctx.get(zArg)

        ctx.reply("Teleporting to ($x, $y, $z)...".info())
    }
}

class SpawnCommand : TaleCommand("spawn", "Teleport to spawn") {
    override fun onExecute(ctx: TaleContext) {
        ctx.reply("Teleporting to spawn!".success())
    }
}
```

## Registering Commands

Register commands in your plugin's `onStart()`:

```kotlin
override fun onStart() {
    taleCommands.register(TeleportCommand())
    taleCommands.register(MyOtherCommand())
}
```

## Argument Types

### Required Arguments

```kotlin
stringArg("name", "description")
intArg("count", "description")
floatArg("distance", "description")
doubleArg("value", "description")
boolArg("enabled", "description")
playerRefArg("target", "description")
```

### Optional Arguments

```kotlin
optionalString("name", "description")
optionalInt("count", "description")
optionalFloat("distance", "description")
optionalDouble("value", "description")
optionalPlayerRef("target", "description")
```

### Flags

```kotlin
flag("verbose", "Enable verbose output")
```

## TaleContext

The context object provides access to the command sender and arguments:

```kotlin
override fun onExecute(ctx: TaleContext) {
    // Sender information
    ctx.sender        // CommandSender
    ctx.senderName    // String (display name)
    ctx.isPlayer      // Boolean
    ctx.player        // Player? (the entity)
    ctx.playerRef     // PlayerRef? (network handle)
    ctx.raw           // CommandContext (underlying API)

    // Getting argument values
    val name = ctx.get(requiredArg)     // Returns T
    val optional = ctx.get(optionalArg) // Returns T?
    val hasFlag = ctx.has(flagArg)      // Returns Boolean

    // Require player (returns null and sends error if not player)
    val player = ctx.requirePlayer() ?: return

    // Sending messages
    ctx.reply("Hello!")
    ctx.reply(message)
}
```

## Subcommands

Add subcommands in the `init` block:

```kotlin
class AdminCommand : TaleCommand("admin", "Admin commands") {
    init {
        subCommand(KickCommand())
        subCommand(BanCommand())
        subCommand(MuteCommand())
    }

    override fun onExecute(ctx: TaleContext) {
        // Called when /admin is run without subcommand
        ctx.reply("Use /admin <kick|ban|mute>".info())
    }
}
```

## Aliases

Add command aliases:

```kotlin
init {
    aliases("tp", "teleport", "warp")
}
```

## Example: Complete Command

```kotlin
class GiveCommand : TaleCommand("give", "Give items to a player") {
    private val targetArg = playerRefArg("player", "Target player")
    private val itemArg = stringArg("item", "Item ID")
    private val amountArg = optionalInt("amount", "Amount to give")
    private val silentFlag = flag("silent", "Don't notify player")

    override fun onExecute(ctx: TaleContext) {
        val target = ctx.get(targetArg)
        val item = ctx.get(itemArg)
        val amount = ctx.get(amountArg) ?: 1
        val silent = ctx.has(silentFlag)

        target.giveItem(item, amount)

        ctx.reply("Gave $amount x $item to ${target.name}".success())

        if (!silent) {
            target.send("You received $amount x $item".info())
        }
    }
}
```
