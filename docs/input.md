# Input System

TaleLib provides extensions for handling player input events.

## Important: No Keyboard Events

**Hytale servers do NOT receive raw keyboard input.** The client translates keyboard presses into packets:

- **Mouse events** - clicks with target info (full access)
- **Interaction types** - what action the player is performing (Q, E, R, F, etc.)
- **Movement states** - walking, jumping, etc. (via `MovementStates` component)

### What You CAN Do:
- Full mouse control (all 5 buttons, press/release, drag, double-click)
- Intercept the 3 ability slots (`Ability1`, `Ability2`, `Ability3`)
- Make abilities item-dependent (wand = fireball, sword = slash)
- Detect hold vs tap, double-click, toggle via timing
- Intercept and repurpose other packets (see Workaround below)

### What You CANNOT Do:
- Add new keybinds (keyboard mapping is client-side)
- Remap existing keys
- Add more than 3 ability slots

### Fixed Input Slots

| Action | Default Key | Server Receives |
|--------|-------------|-----------------|
| Left Mouse | LMB | `InteractionType.Primary` |
| Right Mouse | RMB | `InteractionType.Secondary` |
| Weapon Ability | Q | `InteractionType.Ability1` |
| Ability Slot 2 | E | `InteractionType.Ability2` |
| Ability Slot 3 | R | `InteractionType.Ability3` |
| Interact | F | `InteractionType.Use` |
| Pick Block | MMB | `InteractionType.Pick` |
| Dodge | LCTRL | `InteractionType.Dodge` |

**Note:** Only `Ability1` (Q) is actively used for weapon abilities. `Ability2` and `Ability3` exist but are reserved for future use.

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

## Hold Detection

Both `PlayerMouseButtonEvent` and `PlayerInteractEvent` support hold detection via `clientUseTime`:

```kotlin
taleEvents.on<PlayerMouseButtonEvent> { event ->
    // Built-in thresholds
    if (event.isTap) {
        // Held for less than 200ms - quick tap
    }
    if (event.isHold) {
        // Held for 200ms or more
    }
    if (event.isLongHold) {
        // Held for 500ms or more - charged attack
    }

    // Custom duration check
    if (event.isHeldFor(1000)) {
        // Held for 1 second
    }

    // Raw values
    val holdMs = event.holdTimeMs        // Long (milliseconds)
    val holdSec = event.holdTimeSeconds  // Float (seconds)
}
```

## Event Cancellation

Cancel events to prevent default behavior:

```kotlin
taleEvents.on<PlayerMouseButtonEvent> { event ->
    // Simple cancel
    event.cancel()

    // Conditional cancel
    event.cancelIf { someCondition() }
}

taleEvents.on<PlayerInteractEvent> { event ->
    // Cancel specific interaction types
    event.cancelIfType(InteractionType.Ability1)

    // Or check and cancel
    if (event.isAbility1 && !playerCanUseAbility()) {
        event.cancel()
    }
}
```

## Player Interact Events

For keyboard-mapped actions (Q, E, R, F, etc.):

```kotlin
taleEvents.on<PlayerInteractEvent> { event ->
    // Quick type checks
    if (event.isPrimaryAction) { }      // LMB
    if (event.isSecondaryAction) { }    // RMB
    if (event.isAbility1) { }           // Q key
    if (event.isAbility2) { }           // E key
    if (event.isAbility3) { }           // R key
    if (event.isUseAction) { }          // F key
    if (event.isDodgeAction) { }        // LCTRL

    // Category checks
    if (event.isAbilityAction) {
        // Any of Ability1, Ability2, or Ability3
    }
    if (event.isCombatInteraction) {
        // Primary, Secondary, or any ability
    }
    if (event.isStandardInput) {
        // Any normal player input
    }

    // Hold detection (same as mouse events)
    if (event.isHold) {
        // Player is holding the key
    }

    // Target info
    if (event.hasTarget) {
        // Has either block or entity target
    }
    if (event.hasItemInHand) {
        val item = event.itemInHand
    }

    // Display name for UI
    val actionName = event.actionDisplayName  // "Ability 1", "Use/Interact", etc.
}
```

## Movement States

Check what the player is physically doing:

```kotlin
import com.github.ssquadteam.talelib.input.*

// Individual state checks (on PlayerRef)
if (playerRef.isJumping) { }
if (playerRef.isSprinting) { }
if (playerRef.isCrouching) { }
if (playerRef.isSwimming) { }
if (playerRef.isOnGround) { }
if (playerRef.isGliding) { }
// ... and 16 more individual states

// Compound checks
if (playerRef.isMoving) {
    // Walking, running, or sprinting
}
if (playerRef.isInAir) {
    // Jumping, falling, or not on ground
}
if (playerRef.isInWater) {
    // In fluid or swimming
}
if (playerRef.isDoingSpecialMovement) {
    // Mantling, sliding, rolling, or gliding
}
if (playerRef.isStationary) {
    // Idle, on ground, not falling
}

// Get raw MovementStates for advanced checks
val states = playerRef.getMovementStates()
if (states != null) {
    val activeStates = states.getActiveStates()  // List<String> for debugging
}
```

### All Movement State Properties

| Property | Description |
|----------|-------------|
| `isIdle` | Not moving at all |
| `isHorizontalIdle` | Not moving horizontally |
| `isJumping` | Currently jumping |
| `isFlying` | Flying (creative mode) |
| `isWalking` | Walking speed |
| `isRunning` | Running speed |
| `isSprinting` | Sprinting speed |
| `isCrouching` | Crouching/sneaking |
| `isForcedCrouching` | Forced crouch (low ceiling) |
| `isFalling` | Falling downward |
| `isClimbing` | On a ladder/climbable |
| `isInFluid` | In water/lava |
| `isSwimming` | Actively swimming |
| `isSwimJumping` | Jumping while swimming |
| `isOnGround` | Touching ground |
| `isMantling` | Climbing over ledge |
| `isSliding` | Sliding on slope |
| `isMounting` | Getting on mount |
| `isRolling` | Dodge rolling |
| `isSitting` | Sitting on mount/chair |
| `isGliding` | Using glider |
| `isSleeping` | In bed |

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

## Workaround: Custom Keybinds via Builder Tools

Since you can't add new keybinds, you can **intercept existing packets** and repurpose them. Builder Tool packets are great for this because they're unused during normal gameplay.

**Caveat:** This overrides the default functionality. If you intercept `Ability1`, weapon abilities won't work unless you redirect them.

### Example: Builder Tools → Combat Abilities

```kotlin
// 1. Grant builder tools permission on join
taleEvents.on<PlayerReadyEvent> { event ->
    val playerRef = event.player.playerRef
    PermissionsModule.get().addUserPermission(
        playerRef.uniqueId,
        setOf(BuilderToolPermissions.BASE)
    )
}

// 2. Intercept builder tool actions in a packet handler
// When player is NOT in Creative mode, repurpose the keys:
// - B key (ActivateToolMode) → Cast Fireball
// - Ctrl+Z (HistoryUndo) → Cast Rewind
// - Ctrl+Y (HistoryRedo) → Cast Haste
// - Ctrl+C (SelectionCopy) → Cast Clone

// In Creative mode, normal builder tool behavior works
```

### Available Builder Tool Actions

| Action | Default Key | Potential Use |
|--------|-------------|---------------|
| `ActivateToolMode` | B | Toggle ability |
| `DeactivateToolMode` | B (release) | End ability |
| `HistoryUndo` | Ctrl+Z | Rewind spell |
| `HistoryRedo` | Ctrl+Y | Buff spell |
| `SelectionCopy` | Ctrl+C | Clone spell |
| `SelectionCut` | Ctrl+X | Available |
| `SelectionPaste` | Ctrl+V | Available |

**Warning:** If the player remaps their keybinds client-side, your documentation won't match. Always reference "Default Hytale Controls" in your guides.
