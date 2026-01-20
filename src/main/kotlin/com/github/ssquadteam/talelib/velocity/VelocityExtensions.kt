package com.github.ssquadteam.talelib.velocity

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.protocol.ChangeVelocityType
import com.hypixel.hytale.protocol.Position
import com.hypixel.hytale.protocol.packets.entities.ApplyKnockback
import com.hypixel.hytale.protocol.packets.entities.ChangeVelocity
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent
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

fun PlayerRef.sendKnockbackPacket(
    hitPosition: Position?,
    x: Float,
    y: Float,
    z: Float,
    type: ChangeVelocityType = ChangeVelocityType.Add
): Boolean {
    val handler = this.packetHandler
    if (handler == null) {
        return false
    }
    val packet = ApplyKnockback(hitPosition, x, y, z, type)
    handler.write(packet)
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

    val hitPosition = Position(targetPos.x, targetPos.y, targetPos.z)
    return sendKnockbackPacket(hitPosition, vx.toFloat(), verticalForce.toFloat(), vz.toFloat(), ChangeVelocityType.Add)
}

fun PlayerRef.applyKnockbackFrom(attacker: PlayerRef, horizontalForce: Double = 0.4, verticalForce: Double = 0.36) {
    val attackerPos = attacker.transform?.position ?: return
    applyKnockback(attackerPos.x, attackerPos.z, horizontalForce, verticalForce)
}

/**
 * Applies knockback to a player via the ECS system by adding KnockbackComponent directly to the entity.
 * This is the proper way to apply knockback that works with Hytale's ApplyPlayerKnockback system.
 *
 * @param sourceX The X coordinate of the knockback source
 * @param sourceZ The Z coordinate of the knockback source
 * @param horizontalForce The horizontal knockback force
 * @param verticalForce The vertical knockback force
 * @return true if knockback was applied, false otherwise
 */
fun PlayerRef.applyKnockbackECS(
    sourceX: Double,
    sourceZ: Double,
    horizontalForce: Double = 0.4,
    verticalForce: Double = 0.36
): Boolean {
    val ref = this.reference ?: return false
    val world = (ref.store.externalData as? EntityStore)?.world ?: return false
    val targetPos = this.transform?.position ?: return false

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

    return ref.applyKnockbackComponentECS(world, vx, verticalForce, vz)
}

/**
 * Applies knockback to a player entity via the ECS by adding KnockbackComponent directly.
 * This ensures the ApplyPlayerKnockback system processes the knockback properly.
 *
 * @param world The world containing the entity
 * @param vx The X velocity component
 * @param vy The Y velocity component
 * @param vz The Z velocity component
 * @param type The velocity change type (Add or Set)
 * @param duration The duration of the knockback (0 = instant)
 * @return true if the component was added/updated, false otherwise
 */
fun Ref<EntityStore>.applyKnockbackComponentECS(
    world: World,
    vx: Double,
    vy: Double,
    vz: Double,
    type: ChangeVelocityType = ChangeVelocityType.Add,
    duration: Float = 0f
): Boolean {
    val store = world.entityStore?.store ?: return false

    try {
        // Use ensureAndGetComponent to get or create the KnockbackComponent
        // This is the same pattern used by ExplosionUtils and DamageEntityInteraction
        val knockbackComponent = store.ensureAndGetComponent(this, KnockbackComponent.getComponentType())
        if (knockbackComponent == null) {
            return false
        }

        // Set the velocity and properties
        knockbackComponent.velocity = Vector3d(vx, vy, vz)
        knockbackComponent.velocityType = type
        knockbackComponent.duration = duration
        knockbackComponent.setTimer(0f)

        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

/**
 * Applies knockback away from a source position via ECS.
 *
 * @param world The world
 * @param sourceX The X position of the knockback source
 * @param sourceZ The Z position of the knockback source
 * @param targetX The X position of the target
 * @param targetZ The Z position of the target
 * @param horizontalForce The horizontal knockback force
 * @param verticalForce The vertical knockback force
 * @return true if knockback was applied
 */
fun Ref<EntityStore>.applyKnockbackAwayFromECS(
    world: World,
    sourceX: Double,
    sourceZ: Double,
    targetX: Double,
    targetZ: Double,
    horizontalForce: Double,
    verticalForce: Double = 0.36
): Boolean {
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

    return applyKnockbackComponentECS(world, vx, verticalForce, vz)
}

/**
 * Applies knockback to a PlayerRef away from a source position via ECS.
 */
fun PlayerRef.applyKnockbackAwayFromECS(
    sourceX: Double,
    sourceZ: Double,
    horizontalForce: Double = 0.4,
    verticalForce: Double = 0.36
): Boolean {
    val ref = this.reference ?: return false
    val world = (ref.store.externalData as? EntityStore)?.world ?: return false
    val targetPos = this.transform?.position ?: return false

    return ref.applyKnockbackAwayFromECS(world, sourceX, sourceZ, targetPos.x, targetPos.z, horizontalForce, verticalForce)
}
