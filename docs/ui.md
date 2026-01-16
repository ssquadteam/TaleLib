# UI System

TaleLib provides a comprehensive UI system for HUDs (non-interactive overlays) and interactive pages.

## HUDs (Non-Interactive)

HUDs are overlays that display information without player interaction.

### Creating a HUD

```kotlin
import com.github.ssquadteam.talelib.ui.*
import com.github.ssquadteam.talelib.ui.hud.*

// Create HUD using DSL
val statsHud = taleHud("stats", "ui/stats.json") {
    onShow { player ->
        info("Showing stats HUD to ${player.name}")
    }
    onHide { player ->
        info("Hiding stats HUD from ${player.name}")
    }
    onBuild { player, builder ->
        // Customize UICommandBuilder when HUD is built
    }
}

// Register with plugin
val hud = registerHud(statsHud)

// Or create and register in one call
val hud2 = createHud("health", "ui/health.json")
```

### Show/Hide HUD

```kotlin
playerRef.showHud(hud)
playerRef.hideHud(hud)
hud.toggle(playerRef)
hud.isShownTo(playerRef)  // Boolean
```

### Update HUD Elements

```kotlin
hud.update(playerRef) {
    text("score_label", "Score: 100")
    visible("bonus_icon", true)
    progress("health_bar", 75, 100)
}

// Update for all players showing this HUD
hud.updateAll {
    text("server_time", "12:00")
}
```

### HUD Shortcuts

```kotlin
hud.setText(playerRef, "score", "1000")
hud.setVisible(playerRef, "icon", true)
hud.setProgress(playerRef, "xp_bar", currentXp, maxXp)
```

### Bulk Operations

```kotlin
hud.showTo(listOf(player1, player2, player3))
hud.hideFrom(listOf(player1, player2))
hud.hideFromAll()
hud.getActivePlayers()  // Set<PlayerRef>
```

## Interactive Pages

Pages are interactive UIs that respond to player input.

### Creating a Page

```kotlin
import com.github.ssquadteam.talelib.ui.page.*

val shopPage = talePage("shop", "ui/shop.json") {
    onOpen { player ->
        info("${player.name} opened shop")
    }
    onClose { player ->
        info("${player.name} closed shop")
    }
    onEvent { player, eventData ->
        // Handle UI events (button clicks, etc.)
        when {
            eventData.contains("buyButton") -> handleBuy(player)
            eventData.contains("sellButton") -> handleSell(player)
        }
    }
}

val page = registerPage(shopPage)
```

### Open/Close Pages

```kotlin
playerRef.openPage(page)
page.open(playerRef)
page.close(playerRef)
playerRef.closePage(page)
```

### Update Page Elements

```kotlin
page.update(playerRef) {
    text("coins", "500")
    visible("sale_banner", true)
}

// Page state
page.isOpenFor(playerRef)      // Boolean
page.getActivePlayers()        // Set<PlayerRef>
page.closeAll()                // Close for all players
```

## Element References

Type-safe element references:

```kotlin
import com.github.ssquadteam.talelib.ui.element.*

val scoreElement = element("score_display")
val healthBar = element("hud", "health_bar")  // Nested: "hud.health_bar"

// Property paths
scoreElement.text          // "score_display.text"
scoreElement.visible       // "score_display.visible"
scoreElement.enabled       // "score_display.enabled"
scoreElement.progress      // "score_display.progress"
scoreElement.maxProgress   // "score_display.maxProgress"
scoreElement.color         // "score_display.color"
scoreElement.alpha         // "score_display.alpha"

// Custom properties
scoreElement.property("custom")  // "score_display.custom"
scoreElement.child("sub")        // ElementRef("score_display.sub")

// Built-in elements
Elements.ROOT                    // ElementRef("root")
Elements.list("items", 0)        // ElementRef("items[0]")
```

## UI Command DSL

The update DSL handles Hytale's UI selector format automatically:

```kotlin
hud.update(playerRef) {
    // Text - auto-adds # prefix and .Text property
    text("title", "Welcome!")           // → #title.Text = "Welcome!"

    // Visibility
    visible("panel", true)              // → #panel.Visible = true
    show("successIcon")                 // → #successIcon.Visible = true
    hide("errorIcon")                   // → #errorIcon.Visible = false

    // Enabled state
    enabled("button", true)             // → #button.Enabled = true
    enable("submitBtn")
    disable("lockedBtn")

    // Progress bars
    progress("health", 75, 100)         // Current and max
    progressFloat("xp", 0.75f, 1.0f)

    // Colors (RGBA)
    color("highlight", 255, 200, 0, 255)
    alpha("overlay", 0.5f)

    // Images/Icons
    icon("itemIcon", "items/sword")
    image("background", "textures/bg")

    // Input values
    value("inputField", "default text")

    // Container operations
    clear("itemList")                   // Clear children
    appendTo("container", "template.ui")

    // Generic property setting
    set("#custom.CustomProperty", "value")
    set("property.path", 42)
    set("property.path", true)
}
```

## UI Registry

Access registered HUDs and pages:

```kotlin
taleUI.getHud("stats")           // TaleHud?
taleUI.getPage("shop")           // TalePage?
taleUI.getAllHuds()              // Collection<TaleHud>
taleUI.getAllPages()             // Collection<TalePage>
taleUI.unregisterHud("stats")
taleUI.unregisterPage("shop")
```

## Example: Stats HUD

```kotlin
class StatsPlugin(init: JavaPluginInit) : TalePlugin(init) {

    private lateinit var statsHud: TaleHud

    override fun onStart() {
        statsHud = createHud("stats", "ui/stats_hud.json")

        taleEvents.on<PlayerReadyEvent> { event ->
            val playerRef = event.player.playerRef
            playerRef.showHud(statsHud)
            updateStats(playerRef)
        }
    }

    fun updateStats(playerRef: PlayerRef) {
        statsHud.update(playerRef) {
            text("health", "100/100")
            progress("health_bar", 100, 100)
            text("coins", "500")
        }
    }

    fun onPlayerDamage(playerRef: PlayerRef, health: Int, maxHealth: Int) {
        statsHud.update(playerRef) {
            text("health", "$health/$maxHealth")
            progress("health_bar", health, maxHealth)
        }
    }
}
```

## Example: Shop Page

```kotlin
class ShopPlugin(init: JavaPluginInit) : TalePlugin(init) {

    private lateinit var shopPage: TalePage

    override fun onStart() {
        shopPage = registerPage(talePage("shop", "ui/shop.json") {
            onOpen { player ->
                updateShopItems(player)
            }
            onEvent { player, eventData ->
                handleShopEvent(player, eventData)
            }
        })

        taleCommands.register(ShopCommand())
    }

    inner class ShopCommand : TaleCommand("shop", "Open the shop") {
        override fun onExecute(ctx: TaleContext) {
            val playerRef = ctx.playerRef ?: return
            playerRef.openPage(shopPage)
        }
    }

    private fun updateShopItems(playerRef: PlayerRef) {
        shopPage.update(playerRef) {
            text("coins", "${getPlayerCoins(playerRef)}")
            // Update item list...
        }
    }

    private fun handleShopEvent(playerRef: PlayerRef, eventData: String) {
        // Parse event and handle purchases
    }
}
```
