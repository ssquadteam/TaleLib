package com.github.ssquadteam.talelib.damage

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.server.core.modules.entity.damage.Damage
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/**
 * Extension functions for applying damage to entities.
 * Source: com/hypixel/hytale/server/core/modules/entity/damage/DamageSystems.java
 */

fun Ref<EntityStore>.damage(world: World, block: DamageBuilder.() -> Unit) {
    val builder = DamageBuilder().apply(block)
    builder.execute(this, world)
}

fun Ref<EntityStore>.damage(world: World, amount: Float, cause: DamageCause? = null) {
    val actualCause = cause ?: DamageCause.COMMAND ?: return
    val damage = Damage(Damage.NULL_SOURCE, actualCause, amount)
    DamageSystems.executeDamage(this, world.entityStore.store, damage)
}

fun Ref<EntityStore>.damageFrom(world: World, attacker: Ref<EntityStore>, amount: Float, cause: DamageCause? = null) {
    val actualCause = cause ?: DamageCause.COMMAND ?: return
    val source = Damage.EntitySource(attacker)
    val damage = Damage(source, actualCause, amount)
    DamageSystems.executeDamage(this, world.entityStore.store, damage)
}

fun PlayerRef.damage(block: DamageBuilder.() -> Unit) {
    val ref = this.reference ?: return
    val world = (ref.store.externalData as? EntityStore)?.world ?: return
    world.execute {
        ref.damage(world, block)
    }
}

fun PlayerRef.damage(amount: Float, cause: DamageCause? = null) {
    val ref = this.reference ?: return
    val world = (ref.store.externalData as? EntityStore)?.world ?: return
    world.execute {
        ref.damage(world, amount, cause)
    }
}

fun PlayerRef.damageFrom(attacker: Ref<EntityStore>, amount: Float, cause: DamageCause? = null) {
    val ref = this.reference ?: return
    val world = (ref.store.externalData as? EntityStore)?.world ?: return
    world.execute {
        ref.damageFrom(world, attacker, amount, cause)
    }
}

fun PlayerRef.damageFrom(attacker: PlayerRef, amount: Float, cause: DamageCause? = null) {
    val ref = this.reference ?: return
    val attackerRef = attacker.reference ?: return
    val world = (ref.store.externalData as? EntityStore)?.world ?: return
    world.execute {
        ref.damageFrom(world, attackerRef, amount, cause)
    }
}

fun PlayerRef.damageWithKnockback(
    amount: Float,
    knockbackX: Double,
    knockbackY: Double,
    knockbackZ: Double,
    cause: DamageCause? = null
) {
    val actualCause = cause ?: DamageCause.COMMAND ?: return
    damage {
        amount(amount)
        cause(actualCause)
        knockback(knockbackX, knockbackY, knockbackZ)
    }
}

fun PlayerRef.damageWithKnockbackFrom(
    attacker: PlayerRef,
    amount: Float,
    knockbackForce: Double = 0.5,
    verticalForce: Double = 0.3,
    cause: DamageCause? = null
) {
    val ref = this.reference ?: return
    val world = (ref.store.externalData as? EntityStore)?.world ?: return
    val actualCause = cause ?: DamageCause.COMMAND ?: return
    val attackerPos = attacker.transform.position
    val targetPos = this.transform.position

    world.execute {
        ref.damage(world) {
            amount(amount)
            cause(actualCause)
            fromPlayer(attacker)
            knockback {
                awayFrom(attackerPos.x, attackerPos.z, targetPos.x, targetPos.z, knockbackForce, verticalForce)
            }
        }
    }
}

fun PlayerRef.kill(cause: DamageCause? = null) {
    damage(Float.MAX_VALUE, cause)
}

fun Ref<EntityStore>.kill(world: World, cause: DamageCause? = null) {
    damage(world, Float.MAX_VALUE, cause)
}

fun PlayerRef.applyFallDamage(amount: Float) {
    val cause = DamageCause.FALL ?: return
    damage(amount, cause)
}

fun PlayerRef.applyDrowningDamage(amount: Float = 2f) {
    val cause = DamageCause.DROWNING ?: return
    damage(amount, cause)
}

fun PlayerRef.applySuffocationDamage(amount: Float = 1f) {
    val cause = DamageCause.SUFFOCATION ?: return
    damage(amount, cause)
}

fun PlayerRef.applyOutOfWorldDamage(amount: Float = Float.MAX_VALUE) {
    val cause = DamageCause.OUT_OF_WORLD ?: return
    damage(amount, cause)
}

fun getDamageCause(id: String): DamageCause? {
    return try {
        DamageCause.getAssetMap().getAsset(id)
    } catch (e: Exception) {
        null
    }
}

fun getDamageCauseIndex(id: String): Int {
    return try {
        DamageCause.getAssetMap().getIndex(id)
    } catch (e: Exception) {
        -1
    }
}

fun damageCauseExists(id: String): Boolean {
    return getDamageCause(id) != null
}
