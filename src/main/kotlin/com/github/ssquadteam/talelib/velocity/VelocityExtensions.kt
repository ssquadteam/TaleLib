package com.github.ssquadteam.talelib.velocity

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.protocol.ChangeVelocityType
import com.hypixel.hytale.protocol.packets.entities.ChangeVelocity
import com.hypixel.hytale.server.core.modules.physics.component.Velocity
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import kotlin.math.sqrt

/**
 * Extension functions for applying velocity to entities.
 * For players, sends ChangeVelocity packet directly to the client.
 * For non-player entities, uses the instruction system.
 */

fun Ref<EntityStore>.applyVelocity(
    world: World,
    x: Double,
    y: Double,
    z: Double,
    type: ChangeVelocityType = ChangeVelocityType.Add,
    config: VelocityConfig? = null
) {
    val velocityComponent = this.store.getComponent(this, Velocity.getComponentType())
        ?: world.entityStore.store.getComponent(this, Velocity.getComponentType())
    if (velocityComponent == null) {
        return
    }
    velocityComponent.addInstruction(Vector3d(x, y, z), config, type)
}

fun Ref<EntityStore>.applyVelocity(
    world: World,
    velocity: Vector3d,
    type: ChangeVelocityType = ChangeVelocityType.Add,
    config: VelocityConfig? = null
) {
    val velocityComponent = world.entityStore.store.getComponent(this, Velocity.getComponentType()) ?: return
    velocityComponent.addInstruction(velocity, config, type)
}

fun Ref<EntityStore>.applyKnockback(
    world: World,
    sourceX: Double,
    sourceZ: Double,
    targetX: Double,
    targetZ: Double,
    horizontalForce: Double,
    verticalForce: Double = 0.36
) {
    val dx = targetX - sourceX
    val dz = targetZ - sourceZ
    val distance = sqrt(dx * dx + dz * dz)

    val (vx, vz) = if (distance > 0.001) {
        val normalizedX = dx / distance
        val normalizedZ = dz / distance
        Pair(normalizedX * horizontalForce, normalizedZ * horizontalForce)
    } else {
        Pair(0.0, 0.0)
    }

    applyVelocity(world, vx, verticalForce, vz, ChangeVelocityType.Add)
}

fun PlayerRef.sendVelocityPacket(
    x: Float,
    y: Float,
    z: Float,
    type: ChangeVelocityType = ChangeVelocityType.Add,
    config: VelocityConfig? = null
): Boolean {
    val handler = this.packetHandler
    if (handler == null) {
        return false
    }
    val packet = ChangeVelocity(x, y, z, type, config?.toPacket())
    handler.writeNoCache(packet)
    return true
}

fun PlayerRef.applyVelocity(
    x: Double,
    y: Double,
    z: Double,
    type: ChangeVelocityType = ChangeVelocityType.Add,
    config: VelocityConfig? = null
) {
    sendVelocityPacket(x.toFloat(), y.toFloat(), z.toFloat(), type, config)
}

fun PlayerRef.applyVelocity(
    velocity: Vector3d,
    type: ChangeVelocityType = ChangeVelocityType.Add,
    config: VelocityConfig? = null
) {
    sendVelocityPacket(velocity.x.toFloat(), velocity.y.toFloat(), velocity.z.toFloat(), type, config)
}

fun PlayerRef.applyKnockback(
    sourceX: Double,
    sourceZ: Double,
    horizontalForce: Double = 0.4,
    verticalForce: Double = 0.36
): Boolean {
    val targetPos = this.transform?.position
    if (targetPos == null) {
        return false
    }

    val dx = targetPos.x - sourceX
    val dz = targetPos.z - sourceZ
    val distance = sqrt(dx * dx + dz * dz)

    val (vx, vz) = if (distance > 0.001) {
        val normalizedX = dx / distance
        val normalizedZ = dz / distance
        Pair(normalizedX * horizontalForce, normalizedZ * horizontalForce)
    } else {
        Pair(0.0, 0.0)
    }

    return sendVelocityPacket(vx.toFloat(), verticalForce.toFloat(), vz.toFloat(), ChangeVelocityType.Add, null)
}

fun PlayerRef.applyKnockbackFrom(attacker: PlayerRef, horizontalForce: Double = 0.4, verticalForce: Double = 0.36) {
    val attackerPos = attacker.transform?.position ?: return
    applyKnockback(attackerPos.x, attackerPos.z, horizontalForce, verticalForce)
}
