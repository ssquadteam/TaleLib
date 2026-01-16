# Permission System

TaleLib provides extensions for checking and managing permissions.

## Checking Permissions

```kotlin
import com.github.ssquadteam.talelib.permission.*

// Check single permission
playerRef.hasPermission("myplugin.admin")           // Boolean
playerRef.hasPermission("myplugin.command.fly")

// Check in command
class FlyCommand : TaleCommand("fly", "Toggle flight") {
    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef ?: return

        if (!playerRef.hasPermission("myplugin.fly")) {
            ctx.reply("No permission!".error())
            return
        }

        // Allow flight...
    }
}
```

## Granting/Revoking Permissions

```kotlin
// Grant permissions
playerRef.addPermission("myplugin.vip", "myplugin.fly")

// Revoke permissions
playerRef.removePermission("myplugin.temp")
```

## Group Management

```kotlin
// Add to group
playerRef.addToGroup("vip")

// Remove from group
playerRef.removeFromGroup("vip")

// Check group membership
playerRef.isInGroup("admin")                        // Boolean

// Get all groups
val groups = playerRef.groups                       // Set<String>
```

## Permission DSL

Batch operations with the DSL:

```kotlin
playerRef.permissions {
    grant("myplugin.feature1", "myplugin.feature2")
    revoke("myplugin.old")
    group("premium")
}
```

## Group-Level Permissions

```kotlin
// Add permission to a group
addGroupPermission("admin", "myplugin.*")

// Remove permission from a group
removeGroupPermission("default", "myplugin.restricted")
```

## Example: Rank System

```kotlin
class RankPlugin(init: JavaPluginInit) : TalePlugin(init) {

    fun setRank(playerRef: PlayerRef, rank: String) {
        // Remove from all rank groups
        playerRef.removeFromGroup("default")
        playerRef.removeFromGroup("vip")
        playerRef.removeFromGroup("admin")

        // Add to new rank
        when (rank) {
            "default" -> {
                playerRef.addToGroup("default")
            }
            "vip" -> {
                playerRef.permissions {
                    group("vip")
                    grant("myplugin.fly", "myplugin.home.multiple")
                }
            }
            "admin" -> {
                playerRef.permissions {
                    group("admin")
                    grant("myplugin.*")
                }
            }
        }

        playerRef.sendSuccess("Rank set to $rank")
    }
}
```

## Example: Permission Check in Commands

```kotlin
class AdminCommand : TaleCommand("admin", "Admin commands") {
    init {
        subCommand(KickCommand())
        subCommand(BanCommand())
    }

    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef
        if (playerRef != null && !playerRef.hasPermission("myplugin.admin")) {
            ctx.reply("No permission!".error())
            return
        }

        ctx.reply("Admin commands:".info())
        ctx.reply("  /admin kick <player>".muted())
        ctx.reply("  /admin ban <player>".muted())
    }
}

class KickCommand : TaleCommand("kick", "Kick a player") {
    private val targetArg = playerRefArg("player", "Player to kick")

    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef
        if (playerRef != null && !playerRef.hasPermission("myplugin.admin.kick")) {
            ctx.reply("No permission!".error())
            return
        }

        val target = ctx.get(targetArg)
        // Kick logic...
        ctx.reply("Kicked ${target.name}".success())
    }
}
```
