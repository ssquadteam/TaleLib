# Inventory System

TaleLib provides extensions for managing player inventories.

## PlayerRef Extensions

```kotlin
import com.github.ssquadteam.talelib.inventory.*

// Give items
playerRef.giveItem(itemStack)
playerRef.giveItem("hytale:stone", 32)

// Check for items
playerRef.hasItem("hytale:iron_sword")        // Has at least 1
playerRef.hasItem("hytale:stone", 10)         // Has at least 10

// Remove items
playerRef.removeItem("hytale:stone", 5)

// Count items
playerRef.getItemCount("hytale:stone")        // Total count

// Clear inventory
playerRef.clearInventory()

// Access inventory directly
val inv = playerRef.inventory                 // Inventory?
```

## Player Entity Extensions

```kotlin
// Held item
val held = player.itemInHand                  // ItemStack?
val slot = player.activeSlot                  // Int (0-based)

// Switch hotbar slot
player.setHotbarSlot(3)

// Check space
player.hasInventorySpace()                    // Boolean
```

## Inventory Extensions

```kotlin
val inv = playerRef.inventory ?: return

inv.giveItem(itemStack)
inv.giveItem("hytale:stone", 32)
inv.hasItem("hytale:stone", 10)
inv.removeItem("hytale:stone", 5)
inv.countItem("hytale:stone")                 // Int
inv.hasSpace()                                // Boolean
```

## Example: Shop Purchase

```kotlin
fun buyItem(playerRef: PlayerRef, itemId: String, price: Int) {
    val coins = playerRef.getItemCount("hytale:coin")

    if (coins < price) {
        playerRef.sendError("Not enough coins! Need $price, have $coins")
        return
    }

    if (!playerRef.inventory?.hasSpace() == true) {
        playerRef.sendError("Inventory full!")
        return
    }

    playerRef.removeItem("hytale:coin", price)
    playerRef.giveItem(itemId, 1)
    playerRef.sendSuccess("Purchased $itemId for $price coins!")
}
```

## Example: Item Check Command

```kotlin
class HasItemCommand : TaleCommand("hasitem", "Check if you have an item") {
    private val itemArg = stringArg("item", "Item ID")
    private val countArg = optionalInt("count", "Required count")

    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef ?: return
        val item = ctx.get(itemArg)
        val count = ctx.get(countArg) ?: 1

        val has = playerRef.hasItem(item, count)
        val actual = playerRef.getItemCount(item)

        if (has) {
            ctx.reply("You have $actual x $item".success())
        } else {
            ctx.reply("You only have $actual x $item (need $count)".error())
        }
    }
}
```

## Example: Clear Inventory Command

```kotlin
class ClearCommand : TaleCommand("clear", "Clear your inventory") {
    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef
        if (playerRef == null) {
            ctx.reply("Must be a player".error())
            return
        }

        playerRef.clearInventory()
        ctx.reply("Inventory cleared!".success())
    }
}
```
