package com.github.ssquadteam.talelib.projectile

import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/**
 * Extension functions for projectile spawning.
 * Source: com/hypixel/hytale/server/core/modules/projectile/ProjectileModule.java
 */

// ============================================
// Player Projectile Extensions
// ============================================

/**
 * Shoots a projectile from the player's eye position in their look direction.
 * Must be called from within a system context where a CommandBuffer is available.
 */
fun PlayerRef.shootProjectile(
    commandBuffer: CommandBuffer<EntityStore>,
    configId: String,
    velocityMultiplier: Double = 1.0
): Ref<EntityStore>? {
    val ref = this.reference ?: return null
    val pos = getEyePosition() ?: return null
    val dir = getLookDirection() ?: return null

    return commandBuffer.spawnProjectile {
        config(configId)
        position(pos)
        direction(dir)
        creator(ref)
        velocityMultiplier(velocityMultiplier)
    }
}

/**
 * Shoots a projectile with custom configuration from the player.
 * Must be called from within a system context where a CommandBuffer is available.
 */
fun PlayerRef.shootProjectile(
    commandBuffer: CommandBuffer<EntityStore>,
    block: ProjectileBuilder.() -> Unit
): Ref<EntityStore>? {
    val ref = this.reference ?: return null
    val pos = getEyePosition() ?: return null
    val dir = getLookDirection() ?: return null

    return commandBuffer.spawnProjectile {
        position(pos)
        direction(dir)
        creator(ref)
        block()
    }
}

/**
 * Shoots a projectile at a specific target position.
 * Must be called from within a system context where a CommandBuffer is available.
 */
fun PlayerRef.shootProjectileAt(
    commandBuffer: CommandBuffer<EntityStore>,
    configId: String,
    targetX: Double,
    targetY: Double,
    targetZ: Double,
    velocityMultiplier: Double = 1.0
): Ref<EntityStore>? {
    val ref = this.reference ?: return null
    val pos = getEyePosition() ?: return null

    val direction = Vector3d(targetX - pos.x, targetY - pos.y, targetZ - pos.z).normalize()

    return commandBuffer.spawnProjectile {
        config(configId)
        position(pos)
        direction(direction)
        creator(ref)
        velocityMultiplier(velocityMultiplier)
    }
}

/**
 * Shoots a projectile at another player.
 * Must be called from within a system context where a CommandBuffer is available.
 */
fun PlayerRef.shootProjectileAt(
    commandBuffer: CommandBuffer<EntityStore>,
    configId: String,
    target: PlayerRef,
    velocityMultiplier: Double = 1.0
): Ref<EntityStore>? {
    val targetPos = target.transform.position ?: return null
    return shootProjectileAt(commandBuffer, configId, targetPos.x, targetPos.y + 1.0, targetPos.z, velocityMultiplier)
}

fun PlayerRef.getEyePosition(): Vector3d? {
    val pos = this.transform.position ?: return null
    return Vector3d(pos.x, pos.y + 1.62, pos.z)
}

fun PlayerRef.getLookDirection(): Vector3d? {
    val ref = this.reference ?: return null
    if (!ref.isValid) return null

    val store = ref.store
    val transform = store.getComponent(ref, TransformComponent.getComponentType()) ?: return null
    val rotation = transform.rotation ?: return null

    val yaw = rotation.y
    val pitch = rotation.x

    val yawRad = Math.toRadians(yaw.toDouble())
    val pitchRad = Math.toRadians(pitch.toDouble())

    val x = -kotlin.math.sin(yawRad) * kotlin.math.cos(pitchRad)
    val y = -kotlin.math.sin(pitchRad)
    val z = kotlin.math.cos(yawRad) * kotlin.math.cos(pitchRad)

    return Vector3d(x, y, z).normalize()
}

// ============================================
// Projectile Utility Functions
// ============================================

fun getProjectileConfig(configId: String): ProjectileConfig? {
    return try {
        ProjectileConfig.getAssetMap().getAsset(configId)
    } catch (e: Exception) {
        null
    }
}

fun projectileConfigExists(configId: String): Boolean {
    return getProjectileConfig(configId) != null
}

@Suppress("UNCHECKED_CAST")
fun getAllProjectileConfigIds(): List<String> {
    return try {
        val map = ProjectileConfig.getAssetMap().getAssetMap() as Map<String, Any>
        map.keys.toList()
    } catch (e: Exception) {
        emptyList()
    }
}

fun directionTo(
    sourceX: Double, sourceY: Double, sourceZ: Double,
    targetX: Double, targetY: Double, targetZ: Double
): Vector3d {
    return Vector3d(
        targetX - sourceX,
        targetY - sourceY,
        targetZ - sourceZ
    ).normalize()
}

fun directionFromAngles(yaw: Float, pitch: Float): Vector3d {
    val yawRad = Math.toRadians(yaw.toDouble())
    val pitchRad = Math.toRadians(pitch.toDouble())

    val x = -kotlin.math.sin(yawRad) * kotlin.math.cos(pitchRad)
    val y = -kotlin.math.sin(pitchRad)
    val z = kotlin.math.cos(yawRad) * kotlin.math.cos(pitchRad)

    return Vector3d(x, y, z).normalize()
}

fun Vector3d.withSpread(spreadDegrees: Double): Vector3d {
    val spreadRad = Math.toRadians(spreadDegrees)
    val randomYaw = (Math.random() - 0.5) * 2 * spreadRad
    val randomPitch = (Math.random() - 0.5) * 2 * spreadRad

    val cosYaw = kotlin.math.cos(randomYaw)
    val sinYaw = kotlin.math.sin(randomYaw)
    val cosPitch = kotlin.math.cos(randomPitch)
    val sinPitch = kotlin.math.sin(randomPitch)

    val newX = x * cosYaw - z * sinYaw
    val newZ = x * sinYaw + z * cosYaw
    val newY = y * cosPitch + kotlin.math.sqrt(newX * newX + newZ * newZ) * sinPitch

    return Vector3d(newX, newY, newZ).normalize()
}
