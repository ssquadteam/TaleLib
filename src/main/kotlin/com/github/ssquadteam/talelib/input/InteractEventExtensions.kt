@file:JvmName("InteractEventExtensions")

package com.github.ssquadteam.talelib.input

import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent

// ============================================
// InteractionType Quick Checks
// ============================================

val PlayerInteractEvent.isPrimaryAction: Boolean
    get() = actionType == InteractionType.Primary

val PlayerInteractEvent.isSecondaryAction: Boolean
    get() = actionType == InteractionType.Secondary

val PlayerInteractEvent.isAbility1: Boolean
    get() = actionType == InteractionType.Ability1

val PlayerInteractEvent.isAbility2: Boolean
    get() = actionType == InteractionType.Ability2

val PlayerInteractEvent.isAbility3: Boolean
    get() = actionType == InteractionType.Ability3

val PlayerInteractEvent.isUseAction: Boolean
    get() = actionType == InteractionType.Use

val PlayerInteractEvent.isPickAction: Boolean
    get() = actionType == InteractionType.Pick

val PlayerInteractEvent.isDodgeAction: Boolean
    get() = actionType == InteractionType.Dodge

// ============================================
// Category Checks
// ============================================

/**
 * True if this is a standard player input (Primary, Secondary, abilities, Use, Pick, Dodge)
 */
val PlayerInteractEvent.isStandardInput: Boolean
    get() = actionType.isStandardInput

/**
 * True if this is a combat interaction (Primary, Secondary, Ability1-3)
 */
val PlayerInteractEvent.isCombatInteraction: Boolean
    get() = actionType.isCombatInteraction

/**
 * True if this is any ability action (Ability1, Ability2, or Ability3)
 */
val PlayerInteractEvent.isAbilityAction: Boolean
    get() = actionType == InteractionType.Ability1 ||
            actionType == InteractionType.Ability2 ||
            actionType == InteractionType.Ability3

// ============================================
// Target Checks
// ============================================

val PlayerInteractEvent.hasTargetBlock: Boolean
    get() = targetBlock != null

val PlayerInteractEvent.hasTargetEntity: Boolean
    get() = targetEntity != null

val PlayerInteractEvent.hasItemInHand: Boolean
    get() = itemInHand != null

val PlayerInteractEvent.hasTarget: Boolean
    get() = hasTargetBlock || hasTargetEntity

// ============================================
// Hold Detection (clientUseTime)
// ============================================

/**
 * Milliseconds the client has been holding this action
 */
val PlayerInteractEvent.holdTimeMs: Long
    get() = clientUseTime

/**
 * Seconds the client has been holding this action
 */
val PlayerInteractEvent.holdTimeSeconds: Float
    get() = clientUseTime / 1000f

/**
 * True if this is a tap (held for less than 200ms)
 */
val PlayerInteractEvent.isTap: Boolean
    get() = clientUseTime < 200

/**
 * True if this is being held (held for 200ms or more)
 */
val PlayerInteractEvent.isHold: Boolean
    get() = clientUseTime >= 200

/**
 * True if this is a long hold (held for 500ms or more)
 */
val PlayerInteractEvent.isLongHold: Boolean
    get() = clientUseTime >= 500

/**
 * Check if held for at least the specified duration
 */
fun PlayerInteractEvent.isHeldFor(millis: Long): Boolean = clientUseTime >= millis

/**
 * Check if held for at least the specified duration in seconds
 */
fun PlayerInteractEvent.isHeldForSeconds(seconds: Float): Boolean =
    clientUseTime >= (seconds * 1000).toLong()

// ============================================
// Cancellation Helpers
// ============================================

/**
 * Cancel this event (prevents default behavior)
 */
fun PlayerInteractEvent.cancel() {
    isCancelled = true
}

/**
 * Cancel this event if the condition is true
 */
inline fun PlayerInteractEvent.cancelIf(condition: () -> Boolean) {
    if (condition()) {
        isCancelled = true
    }
}

/**
 * Cancel this event if it matches the given interaction type
 */
fun PlayerInteractEvent.cancelIfType(type: InteractionType) {
    if (actionType == type) {
        isCancelled = true
    }
}

// ============================================
// Display Name
// ============================================

/**
 * Human-readable name for the action type
 */
val PlayerInteractEvent.actionDisplayName: String
    get() = actionType.displayName
