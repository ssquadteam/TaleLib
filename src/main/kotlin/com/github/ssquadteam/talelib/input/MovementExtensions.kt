@file:JvmName("MovementExtensions")

package com.github.ssquadteam.talelib.input

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.protocol.MovementStates
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent
import com.hypixel.hytale.server.core.modules.entity.EntityModule
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

// ============================================
// MovementStates Access
// ============================================

/**
 * Gets the MovementStates for this player.
 * Must be called from WorldThread context.
 */
fun PlayerRef.getMovementStates(): MovementStates? {
    val ref = this.reference ?: return null
    if (!ref.isValid) return null
    val store = ref.store
    val component = store.getComponent(ref, MovementStatesComponent.getComponentType()) ?: return null
    return component.movementStates
}

/**
 * Gets the MovementStates for this player entity.
 * Must be called from WorldThread context.
 */
fun Player.getMovementStates(ref: Ref<EntityStore>): MovementStates? {
    val store = ref.store
    val component = store.getComponent(ref, MovementStatesComponent.getComponentType()) ?: return null
    return component.movementStates
}

// ============================================
// Movement State Checks (PlayerRef)
// ============================================

val PlayerRef.isIdle: Boolean
    get() = getMovementStates()?.idle == true

val PlayerRef.isHorizontalIdle: Boolean
    get() = getMovementStates()?.horizontalIdle == true

val PlayerRef.isJumping: Boolean
    get() = getMovementStates()?.jumping == true

val PlayerRef.isFlying: Boolean
    get() = getMovementStates()?.flying == true

val PlayerRef.isWalking: Boolean
    get() = getMovementStates()?.walking == true

val PlayerRef.isRunning: Boolean
    get() = getMovementStates()?.running == true

val PlayerRef.isSprinting: Boolean
    get() = getMovementStates()?.sprinting == true

val PlayerRef.isCrouching: Boolean
    get() = getMovementStates()?.crouching == true

val PlayerRef.isForcedCrouching: Boolean
    get() = getMovementStates()?.forcedCrouching == true

val PlayerRef.isFalling: Boolean
    get() = getMovementStates()?.falling == true

val PlayerRef.isClimbing: Boolean
    get() = getMovementStates()?.climbing == true

val PlayerRef.isInFluid: Boolean
    get() = getMovementStates()?.inFluid == true

val PlayerRef.isSwimming: Boolean
    get() = getMovementStates()?.swimming == true

val PlayerRef.isSwimJumping: Boolean
    get() = getMovementStates()?.swimJumping == true

val PlayerRef.isOnGround: Boolean
    get() = getMovementStates()?.onGround == true

val PlayerRef.isMantling: Boolean
    get() = getMovementStates()?.mantling == true

val PlayerRef.isSliding: Boolean
    get() = getMovementStates()?.sliding == true

val PlayerRef.isMounting: Boolean
    get() = getMovementStates()?.mounting == true

val PlayerRef.isRolling: Boolean
    get() = getMovementStates()?.rolling == true

val PlayerRef.isSitting: Boolean
    get() = getMovementStates()?.sitting == true

val PlayerRef.isGliding: Boolean
    get() = getMovementStates()?.gliding == true

val PlayerRef.isSleeping: Boolean
    get() = getMovementStates()?.sleeping == true

// ============================================
// Compound Movement Checks
// ============================================

/**
 * Player is moving (walking, running, or sprinting)
 */
val PlayerRef.isMoving: Boolean
    get() {
        val states = getMovementStates() ?: return false
        return states.walking || states.running || states.sprinting
    }

/**
 * Player is in the air (jumping or falling, not on ground)
 */
val PlayerRef.isInAir: Boolean
    get() {
        val states = getMovementStates() ?: return false
        return !states.onGround || states.jumping || states.falling
    }

/**
 * Player is in water (swimming or in fluid)
 */
val PlayerRef.isInWater: Boolean
    get() {
        val states = getMovementStates() ?: return false
        return states.inFluid || states.swimming
    }

/**
 * Player is doing a special movement (mantling, sliding, rolling, gliding)
 */
val PlayerRef.isDoingSpecialMovement: Boolean
    get() {
        val states = getMovementStates() ?: return false
        return states.mantling || states.sliding || states.rolling || states.gliding
    }

/**
 * Player is stationary (idle and on ground)
 */
val PlayerRef.isStationary: Boolean
    get() {
        val states = getMovementStates() ?: return false
        return states.idle && states.onGround && !states.falling
    }

// ============================================
// MovementStates Extensions
// ============================================

/**
 * Check if player is moving in any direction
 */
val MovementStates.isMoving: Boolean
    get() = walking || running || sprinting

/**
 * Check if player is airborne
 */
val MovementStates.isAirborne: Boolean
    get() = !onGround || jumping || falling

/**
 * Check if player is in water
 */
val MovementStates.isInWater: Boolean
    get() = inFluid || swimming

/**
 * Check if player is doing any special movement
 */
val MovementStates.isSpecialMovement: Boolean
    get() = mantling || sliding || rolling || gliding

/**
 * Get a list of all active movement states (for debugging)
 */
fun MovementStates.getActiveStates(): List<String> {
    val active = mutableListOf<String>()
    if (idle) active.add("idle")
    if (horizontalIdle) active.add("horizontalIdle")
    if (jumping) active.add("jumping")
    if (flying) active.add("flying")
    if (walking) active.add("walking")
    if (running) active.add("running")
    if (sprinting) active.add("sprinting")
    if (crouching) active.add("crouching")
    if (forcedCrouching) active.add("forcedCrouching")
    if (falling) active.add("falling")
    if (climbing) active.add("climbing")
    if (inFluid) active.add("inFluid")
    if (swimming) active.add("swimming")
    if (swimJumping) active.add("swimJumping")
    if (onGround) active.add("onGround")
    if (mantling) active.add("mantling")
    if (sliding) active.add("sliding")
    if (mounting) active.add("mounting")
    if (rolling) active.add("rolling")
    if (sitting) active.add("sitting")
    if (gliding) active.add("gliding")
    if (sleeping) active.add("sleeping")
    return active
}
