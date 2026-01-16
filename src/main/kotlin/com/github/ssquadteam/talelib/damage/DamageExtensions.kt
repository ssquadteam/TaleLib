package com.github.ssquadteam.talelib.damage

import com.hypixel.hytale.server.core.entity.EntityStore
import com.hypixel.hytale.server.core.modules.entity.damage.Damage
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.world.World
import com.hypixel.hytale.server.ecs.Ref

/**
 * Extension functions for applying damage to entities.
 * Source: com/hypixel/hytale/server/core/modules/entity/damage/DamageSystems.java
 */

fun Ref<EntityStore>.damage(world: World, block: DamageBuilder.() -> Unit) {
    val builder = DamageBuilder().apply(block)
    builder.execute(this, world)
}

fun Ref<EntityStore>.damage(world: World, amount: Float, cause: DamageCause = DamageCause.COMMAND) {
    val damage = Damage(Damage.NULL_SOURCE, cause, amount)
    world.commandBuffer.use { commandBuffer ->
        DamageSystems.executeDamage(this, commandBuffer, damage)
    }
}

fun Ref<EntityStore>.damageFrom(world: World, attacker: Ref<EntityStore>, amount: Float, cause: DamageCause = DamageCause.COMMAND) {
    val source = Damage.EntitySource(attacker)
    val damage = Damage(source, cause, amount)
    world.commandBuffer.use { commandBuffer ->
        DamageSystems.executeDamage(this, commandBuffer, damage)
    }
}

fun PlayerRef.damage(block: DamageBuilder.() -> Unit) {
    val world = this.world ?: return
    this.ref.damage(world, block)
}

fun PlayerRef.damage(amount: Float, cause: DamageCause = DamageCause.COMMAND) {
    val world = this.world ?: return
    this.ref.damage(world, amount, cause)
}

fun PlayerRef.damageFrom(attacker: Ref<EntityStore>, amount: Float, cause: DamageCause = DamageCause.COMMAND) {
    val world = this.world ?: return
    this.ref.damageFrom(world, attacker, amount, cause)
}

fun PlayerRef.damageFrom(attacker: PlayerRef, amount: Float, cause: DamageCause = DamageCause.COMMAND) {
    val world = this.world ?: return
    this.ref.damageFrom(world, attacker.ref, amount, cause)
}

fun PlayerRef.damageWithKnockback(
    amount: Float,
    knockbackX: Double,
    knockbackY: Double,
    knockbackZ: Double,
    cause: DamageCause = DamageCause.COMMAND
) {
    damage {
        amount(amount)
        cause(cause)
        knockback(knockbackX, knockbackY, knockbackZ)
    }
}

fun PlayerRef.damageWithKnockbackFrom(
    attacker: PlayerRef,
    amount: Float,
    knockbackForce: Double = 0.5,
    verticalForce: Double = 0.3,
    cause: DamageCause = DamageCause.COMMAND
) {
    val world = this.world ?: return
    val attackerPos = attacker.position ?: return
    val targetPos = this.position ?: return

    this.ref.damage(world) {
        amount(amount)
        cause(cause)
        fromPlayer(attacker)
        knockback {
            awayFrom(attackerPos.x, attackerPos.z, targetPos.x, targetPos.z, knockbackForce, verticalForce)
        }
    }
}

fun PlayerRef.kill(cause: DamageCause = DamageCause.COMMAND) {
    damage(Float.MAX_VALUE, cause)
}

fun Ref<EntityStore>.kill(world: World, cause: DamageCause = DamageCause.COMMAND) {
    damage(world, Float.MAX_VALUE, cause)
}

fun PlayerRef.applyFallDamage(amount: Float) {
    damage(amount, DamageCause.FALL)
}

fun PlayerRef.applyDrowningDamage(amount: Float = 2f) {
    damage(amount, DamageCause.DROWNING)
}

fun PlayerRef.applySuffocationDamage(amount: Float = 1f) {
    damage(amount, DamageCause.SUFFOCATION)
}

fun PlayerRef.applyOutOfWorldDamage(amount: Float = Float.MAX_VALUE) {
    damage(amount, DamageCause.OUT_OF_WORLD)
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
