package com.github.ssquadteam.talelib.stamina

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.protocol.MovementStates
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

// ============================================
// Movement State Extensions
// ============================================

fun PlayerRef.getMovementStates(): MovementStates? {
    val ref = this.reference ?: return null
    return ref.getMovementStates()
}

fun Ref<EntityStore>.getMovementStates(): MovementStates? {
    if (!this.isValid) return null
    val component = this.store.getComponent(this, MovementStatesComponent.getComponentType())
    return component?.movementStates
}

fun PlayerRef.isSprinting(): Boolean {
    return getMovementStates()?.sprinting == true
}

fun PlayerRef.isRunning(): Boolean {
    return getMovementStates()?.running == true
}

fun PlayerRef.isWalking(): Boolean {
    return getMovementStates()?.walking == true
}

fun PlayerRef.isJumping(): Boolean {
    return getMovementStates()?.jumping == true
}

fun PlayerRef.isRolling(): Boolean {
    return getMovementStates()?.rolling == true
}

fun PlayerRef.isSwimming(): Boolean {
    return getMovementStates()?.swimming == true
}

fun PlayerRef.isClimbing(): Boolean {
    return getMovementStates()?.climbing == true
}

fun PlayerRef.isGliding(): Boolean {
    return getMovementStates()?.gliding == true
}

fun PlayerRef.isCrouching(): Boolean {
    return getMovementStates()?.crouching == true
}

fun PlayerRef.isSliding(): Boolean {
    return getMovementStates()?.sliding == true
}

fun PlayerRef.isMantling(): Boolean {
    return getMovementStates()?.mantling == true
}

// ============================================
// Ref Movement State Extensions
// ============================================

fun Ref<EntityStore>.isSprinting(): Boolean {
    return getMovementStates()?.sprinting == true
}

fun Ref<EntityStore>.isRunning(): Boolean {
    return getMovementStates()?.running == true
}

fun Ref<EntityStore>.isWalking(): Boolean {
    return getMovementStates()?.walking == true
}

fun Ref<EntityStore>.isJumping(): Boolean {
    return getMovementStates()?.jumping == true
}

fun Ref<EntityStore>.isRolling(): Boolean {
    return getMovementStates()?.rolling == true
}

fun Ref<EntityStore>.isSwimming(): Boolean {
    return getMovementStates()?.swimming == true
}

fun Ref<EntityStore>.isClimbing(): Boolean {
    return getMovementStates()?.climbing == true
}

fun Ref<EntityStore>.isGliding(): Boolean {
    return getMovementStates()?.gliding == true
}

fun Ref<EntityStore>.isCrouching(): Boolean {
    return getMovementStates()?.crouching == true
}

fun Ref<EntityStore>.isSliding(): Boolean {
    return getMovementStates()?.sliding == true
}

fun Ref<EntityStore>.isMantling(): Boolean {
    return getMovementStates()?.mantling == true
}

// ============================================
// Stamina State Checks
// ============================================

fun PlayerRef.isStaminaDepleted(): Boolean {
    val ref = this.reference ?: return false
    return ref.isStaminaDepleted()
}

fun Ref<EntityStore>.isStaminaDepleted(): Boolean {
    if (!this.isValid) return false
    val statMap = this.store.getComponent(this, EntityStatMap.getComponentType()) ?: return false
    val stamina = statMap.get(DefaultEntityStatTypes.getStamina()) ?: return false
    return stamina.get() <= stamina.min
}

fun PlayerRef.isStaminaFull(): Boolean {
    val ref = this.reference ?: return false
    return ref.isStaminaFull()
}

fun Ref<EntityStore>.isStaminaFull(): Boolean {
    if (!this.isValid) return false
    val statMap = this.store.getComponent(this, EntityStatMap.getComponentType()) ?: return false
    val stamina = statMap.get(DefaultEntityStatTypes.getStamina()) ?: return false
    return stamina.get() >= stamina.max
}

fun PlayerRef.isStaminaCritical(threshold: Float = 0.1f): Boolean {
    val ref = this.reference ?: return false
    return ref.isStaminaCritical(threshold)
}

fun Ref<EntityStore>.isStaminaCritical(threshold: Float = 0.1f): Boolean {
    if (!this.isValid) return false
    val statMap = this.store.getComponent(this, EntityStatMap.getComponentType()) ?: return false
    val stamina = statMap.get(DefaultEntityStatTypes.getStamina()) ?: return false
    return stamina.asPercentage() <= threshold
}

// ============================================
// Stamina Cost Calculation Utilities
// ============================================

object StaminaCost {
    const val SPRINT_COST_PER_SECOND = 5f
    const val JUMP_COST = 10f
    const val ROLL_COST = 15f
    const val CLIMB_COST_PER_SECOND = 3f
    const val SWIM_COST_PER_SECOND = 2f
    const val GLIDE_COST_PER_SECOND = 1f
    const val MANTLE_COST = 8f

    fun calculateBlockStaminaCost(damageAmount: Float, maxHealth: Float, costPercent: Float = 0.04f): Float {
        return damageAmount / (costPercent * maxHealth)
    }

    fun calculateAttackStaminaCost(baseCost: Float, multiplier: Float = 1f): Float {
        return baseCost * multiplier
    }
}

// ============================================
// Activity-Based Stamina Operations
// ============================================

fun PlayerRef.canSprint(): Boolean {
    val ref = this.reference ?: return false
    return ref.canSprint()
}

fun Ref<EntityStore>.canSprint(): Boolean {
    if (!this.isValid) return false
    val statMap = this.store.getComponent(this, EntityStatMap.getComponentType()) ?: return false
    val stamina = statMap.get(DefaultEntityStatTypes.getStamina()) ?: return false
    return stamina.get() > StaminaCost.SPRINT_COST_PER_SECOND
}

fun PlayerRef.canJump(): Boolean {
    val ref = this.reference ?: return false
    return ref.canJump()
}

fun Ref<EntityStore>.canJump(): Boolean {
    if (!this.isValid) return false
    val statMap = this.store.getComponent(this, EntityStatMap.getComponentType()) ?: return false
    val stamina = statMap.get(DefaultEntityStatTypes.getStamina()) ?: return false
    return stamina.get() >= StaminaCost.JUMP_COST
}

fun PlayerRef.canRoll(): Boolean {
    val ref = this.reference ?: return false
    return ref.canRoll()
}

fun Ref<EntityStore>.canRoll(): Boolean {
    if (!this.isValid) return false
    val statMap = this.store.getComponent(this, EntityStatMap.getComponentType()) ?: return false
    val stamina = statMap.get(DefaultEntityStatTypes.getStamina()) ?: return false
    return stamina.get() >= StaminaCost.ROLL_COST
}

// ============================================
// Stamina Drain/Restore Operations
// ============================================

fun PlayerRef.drainStaminaForSprint(deltaTime: Float): Boolean {
    val ref = this.reference ?: return false
    return ref.drainStamina(StaminaCost.SPRINT_COST_PER_SECOND * deltaTime)
}

fun PlayerRef.drainStaminaForJump(): Boolean {
    val ref = this.reference ?: return false
    return ref.drainStamina(StaminaCost.JUMP_COST)
}

fun PlayerRef.drainStaminaForRoll(): Boolean {
    val ref = this.reference ?: return false
    return ref.drainStamina(StaminaCost.ROLL_COST)
}

fun Ref<EntityStore>.drainStamina(amount: Float): Boolean {
    if (!this.isValid || amount <= 0) return false
    val statMap = this.store.getComponent(this, EntityStatMap.getComponentType()) ?: return false
    val stamina = statMap.get(DefaultEntityStatTypes.getStamina()) ?: return false
    if (stamina.get() < amount) return false
    statMap.subtractStatValue(DefaultEntityStatTypes.getStamina(), amount)
    return true
}

fun PlayerRef.drainStamina(amount: Float): Boolean {
    val ref = this.reference ?: return false
    return ref.drainStamina(amount)
}

fun Ref<EntityStore>.restoreStamina(amount: Float): Boolean {
    if (!this.isValid || amount <= 0) return false
    val statMap = this.store.getComponent(this, EntityStatMap.getComponentType()) ?: return false
    statMap.addStatValue(DefaultEntityStatTypes.getStamina(), amount)
    return true
}

fun PlayerRef.restoreStamina(amount: Float): Boolean {
    val ref = this.reference ?: return false
    return ref.restoreStamina(amount)
}

// ============================================
// Movement Activity Data Class
// ============================================

data class MovementActivity(
    val sprinting: Boolean,
    val running: Boolean,
    val walking: Boolean,
    val jumping: Boolean,
    val rolling: Boolean,
    val swimming: Boolean,
    val climbing: Boolean,
    val gliding: Boolean,
    val crouching: Boolean,
    val sliding: Boolean,
    val mantling: Boolean
) {
    val isMoving: Boolean
        get() = sprinting || running || walking || swimming || climbing || gliding || sliding

    val isHighStaminaActivity: Boolean
        get() = sprinting || jumping || rolling || mantling || climbing

    companion object {
        fun from(states: MovementStates?): MovementActivity {
            return MovementActivity(
                sprinting = states?.sprinting == true,
                running = states?.running == true,
                walking = states?.walking == true,
                jumping = states?.jumping == true,
                rolling = states?.rolling == true,
                swimming = states?.swimming == true,
                climbing = states?.climbing == true,
                gliding = states?.gliding == true,
                crouching = states?.crouching == true,
                sliding = states?.sliding == true,
                mantling = states?.mantling == true
            )
        }
    }
}

fun PlayerRef.getMovementActivity(): MovementActivity {
    return MovementActivity.from(getMovementStates())
}

fun Ref<EntityStore>.getMovementActivity(): MovementActivity {
    return MovementActivity.from(getMovementStates())
}
