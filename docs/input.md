# Input System

TaleLib provides extensions for handling mouse and keyboard input events.

## Mouse Button Events

```kotlin
import com.github.ssquadteam.talelib.input.*

taleEvents.on<PlayerMouseButtonEvent> { event ->
    // Quick checks
    if (event.isLeftPress()) {
        // Left mouse button pressed
    }
    if (event.isRightClick && event.isPressed) {
        // Right click pressed
    }
    if (event.isDoubleClick) {
        // Double click detected
    }

    // Target information
    if (event.hasTargetBlock) {
        val block = event.targetBlock
    }
    if (event.hasTargetEntity) {
        val entity = event.targetEntity
    }

    // Raw data
    val clicks = event.clickCount
    val button = event.buttonType
    val state = event.buttonState
}
```

## Mouse Motion Events

```kotlin
taleEvents.on<PlayerMouseMotionEvent> { event ->
    val dx = event.deltaX
    val dy = event.deltaY

    if (event.isDragging) {
        // Mouse moved while button held
    }
    if (event.isLeftHeld) {
        // Left button is held during motion
    }

    val heldButtons = event.heldButtons
}
```

## Interaction Types

```kotlin
val interaction: InteractionType = ...

// Check categories
interaction.isStandardInput          // Primary, Secondary, abilities, etc.
interaction.isCombatInteraction      // Primary, Secondary, Ability1-3
interaction.isProjectileInteraction  // ProjectileSpawn, Hit, etc.

// Display name
val name = interaction.displayName   // "Primary Attack", "Use/Interact", etc.

// Convert mouse button to interaction
val interactionType = MouseButtonType.Left.toInteractionType()  // Primary
```

## Builder Tool Helpers

For creative mode tools:

```kotlin
if (player.canUseBuilderTools) {
    // Player is in creative mode with builder tools permission
}

player.hasBuilderToolsPermission()
player.hasBuilderToolsPermission(BuilderToolPermissions.BRUSH_USE)
```

## Example: Custom Click Handler

```kotlin
class ClickPlugin(init: JavaPluginInit) : TalePlugin(init) {

    override fun onStart() {
        taleEvents.on<PlayerMouseButtonEvent> { event ->
            val playerRef = event.player

            when {
                event.isLeftPress() && event.hasTargetEntity -> {
                    handleEntityClick(playerRef, event.targetEntity)
                }
                event.isRightClick && event.isPressed && event.hasTargetBlock -> {
                    handleBlockInteract(playerRef, event.targetBlock)
                }
            }
        }
    }

    private fun handleEntityClick(playerRef: PlayerRef, entity: EntityRef) {
        playerRef.send("You clicked an entity!".info())
    }

    private fun handleBlockInteract(playerRef: PlayerRef, block: BlockPosition) {
        playerRef.send("You interacted with a block at ${block.x}, ${block.y}, ${block.z}".info())
    }
}
```

## Example: Double Click Detection

```kotlin
taleEvents.on<PlayerMouseButtonEvent> { event ->
    if (event.isDoubleClick && event.isLeftClick) {
        val playerRef = event.player

        if (event.hasTargetEntity) {
            // Double-clicked an entity - open info panel
            openEntityInfoPanel(playerRef, event.targetEntity)
        }
    }
}
```

## Example: Drag Handler

```kotlin
class DragPlugin(init: JavaPluginInit) : TalePlugin(init) {

    private val dragStartPositions = mutableMapOf<UUID, Pair<Double, Double>>()

    override fun onStart() {
        taleEvents.on<PlayerMouseButtonEvent> { event ->
            if (event.isLeftPress()) {
                // Record drag start
                dragStartPositions[event.player.uniqueId] = Pair(0.0, 0.0)
            }
            if (event.isLeftRelease()) {
                // End drag
                dragStartPositions.remove(event.player.uniqueId)
            }
        }

        taleEvents.on<PlayerMouseMotionEvent> { event ->
            if (event.isDragging && event.isLeftHeld) {
                val playerRef = event.player

                // Handle drag motion
                val dx = event.deltaX
                val dy = event.deltaY

                handleDrag(playerRef, dx, dy)
            }
        }
    }

    private fun handleDrag(playerRef: PlayerRef, dx: Double, dy: Double) {
        // Implement drag behavior (e.g., rotate camera, move object)
    }
}
```
