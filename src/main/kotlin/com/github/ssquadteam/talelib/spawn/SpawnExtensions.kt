package com.github.ssquadteam.talelib.spawn

import com.hypixel.hytale.math.Transform
import com.hypixel.hytale.server.core.entity.EntityStore
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider
import com.hypixel.hytale.server.core.universe.world.spawn.IndividualSpawnProvider
import com.hypixel.hytale.server.core.world.World
import com.hypixel.hytale.server.ecs.Ref
import org.joml.Vector3d
import org.joml.Vector3f
import java.util.UUID

// ============================================
// World Spawn Extensions
// ============================================

fun World.getSpawnProvider(): ISpawnProvider? {
    return try {
        this.worldConfig?.spawnProvider
    } catch (e: Exception) {
        null
    }
}

fun World.getSpawnPoint(playerUuid: UUID): Transform? {
    val provider = getSpawnProvider() ?: return null
    return try {
        provider.getSpawnPoint(this, playerUuid)
    } catch (e: Exception) {
        null
    }
}

fun World.getSpawnPosition(playerUuid: UUID): Vector3d? {
    return getSpawnPoint(playerUuid)?.position
}

fun World.getSpawnRotation(playerUuid: UUID): Vector3f? {
    return getSpawnPoint(playerUuid)?.rotation
}

fun World.setGlobalSpawn(x: Double, y: Double, z: Double, yaw: Float = 0f, pitch: Float = 0f): Boolean {
    return setGlobalSpawn(Vector3d(x, y, z), Vector3f(pitch, yaw, 0f))
}

fun World.setGlobalSpawn(position: Vector3d, rotation: Vector3f = Vector3f(0f, 0f, 0f)): Boolean {
    return try {
        val transform = Transform(position, rotation)
        val provider = GlobalSpawnProvider(transform)
        this.worldConfig?.spawnProvider = provider
        this.worldConfig?.markChanged()
        true
    } catch (e: Exception) {
        false
    }
}

fun World.setSpawnPoints(vararg transforms: Transform): Boolean {
    return setSpawnPoints(transforms.toList())
}

fun World.setSpawnPoints(transforms: List<Transform>): Boolean {
    return try {
        val provider = IndividualSpawnProvider(transforms.toTypedArray())
        this.worldConfig?.spawnProvider = provider
        this.worldConfig?.markChanged()
        true
    } catch (e: Exception) {
        false
    }
}

fun World.isNearSpawn(x: Double, y: Double, z: Double, distance: Double): Boolean {
    return isNearSpawn(Vector3d(x, y, z), distance)
}

fun World.isNearSpawn(position: Vector3d, distance: Double): Boolean {
    val provider = getSpawnProvider() ?: return false
    return try {
        provider.isWithinSpawnDistance(position, distance)
    } catch (e: Exception) {
        false
    }
}

// ============================================
// Player Spawn Extensions
// ============================================

fun PlayerRef.getSpawnPoint(): Transform? {
    val world = this.world ?: return null
    val uuid = this.uuid ?: return null
    return world.getSpawnPoint(uuid)
}

fun PlayerRef.getSpawnPosition(): Vector3d? {
    return getSpawnPoint()?.position
}

fun PlayerRef.teleportToSpawn(): Boolean {
    val world = this.world ?: return false
    val ref = this.ref ?: return false
    val spawnPoint = getSpawnPoint() ?: return false

    return try {
        val store = ref.store
        val transform = store.getComponent(ref, com.hypixel.hytale.server.core.entity.component.TransformComponent.getComponentType())
        if (transform != null) {
            transform.position.set(spawnPoint.position)
            transform.rotation.set(spawnPoint.rotation)
            true
        } else false
    } catch (e: Exception) {
        false
    }
}

fun PlayerRef.isNearSpawn(distance: Double): Boolean {
    val world = this.world ?: return false
    val pos = this.position ?: return false
    return world.isNearSpawn(pos.x, pos.y, pos.z, distance)
}

// ============================================
// SpawnPoint Builder
// ============================================

class SpawnPointBuilder {
    private var x: Double = 0.0
    private var y: Double = 64.0
    private var z: Double = 0.0
    private var yaw: Float = 0f
    private var pitch: Float = 0f
    private var roll: Float = 0f

    fun position(x: Double, y: Double, z: Double): SpawnPointBuilder {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun position(pos: Vector3d): SpawnPointBuilder {
        this.x = pos.x
        this.y = pos.y
        this.z = pos.z
        return this
    }

    fun rotation(yaw: Float, pitch: Float = 0f): SpawnPointBuilder {
        this.yaw = yaw
        this.pitch = pitch
        return this
    }

    fun facing(direction: SpawnDirection): SpawnPointBuilder {
        this.yaw = direction.yaw
        return this
    }

    fun build(): Transform {
        return Transform(
            Vector3d(x, y, z),
            Vector3f(pitch, yaw, roll)
        )
    }
}

fun spawnPoint(block: SpawnPointBuilder.() -> Unit): Transform {
    return SpawnPointBuilder().apply(block).build()
}

// ============================================
// Spawn Direction Enum
// ============================================

enum class SpawnDirection(val yaw: Float) {
    NORTH(180f),
    SOUTH(0f),
    EAST(-90f),
    WEST(90f),
    NORTHEAST(135f),
    NORTHWEST(-135f),
    SOUTHEAST(45f),
    SOUTHWEST(-45f)
}

// ============================================
// Spawn Configuration Builder
// ============================================

class SpawnConfigBuilder {
    private val spawnPoints = mutableListOf<Transform>()
    private var isGlobal = true

    fun addSpawnPoint(x: Double, y: Double, z: Double, yaw: Float = 0f): SpawnConfigBuilder {
        spawnPoints.add(Transform(Vector3d(x, y, z), Vector3f(0f, yaw, 0f)))
        isGlobal = spawnPoints.size == 1
        return this
    }

    fun addSpawnPoint(transform: Transform): SpawnConfigBuilder {
        spawnPoints.add(transform)
        isGlobal = spawnPoints.size == 1
        return this
    }

    fun addSpawnPoints(vararg transforms: Transform): SpawnConfigBuilder {
        spawnPoints.addAll(transforms)
        isGlobal = false
        return this
    }

    fun global(): SpawnConfigBuilder {
        isGlobal = true
        return this
    }

    fun individual(): SpawnConfigBuilder {
        isGlobal = false
        return this
    }

    fun applyTo(world: World): Boolean {
        if (spawnPoints.isEmpty()) return false

        return try {
            val provider: ISpawnProvider = if (isGlobal || spawnPoints.size == 1) {
                GlobalSpawnProvider(spawnPoints.first())
            } else {
                IndividualSpawnProvider(spawnPoints.toTypedArray())
            }

            world.worldConfig?.spawnProvider = provider
            world.worldConfig?.markChanged()
            true
        } catch (e: Exception) {
            false
        }
    }
}

fun configureSpawn(block: SpawnConfigBuilder.() -> Unit): SpawnConfigBuilder {
    return SpawnConfigBuilder().apply(block)
}

// ============================================
// Spawn Info Data Class
// ============================================

data class SpawnInfo(
    val position: Vector3d,
    val rotation: Vector3f,
    val isGlobal: Boolean,
    val spawnCount: Int
) {
    val yaw: Float get() = rotation.y
    val pitch: Float get() = rotation.x

    companion object {
        fun from(world: World, playerUuid: UUID): SpawnInfo? {
            val provider = world.getSpawnProvider() ?: return null
            val spawnPoint = world.getSpawnPoint(playerUuid) ?: return null
            val allSpawns = try {
                @Suppress("DEPRECATION")
                provider.spawnPoints?.size ?: 1
            } catch (e: Exception) { 1 }

            return SpawnInfo(
                position = spawnPoint.position,
                rotation = spawnPoint.rotation,
                isGlobal = provider is GlobalSpawnProvider,
                spawnCount = allSpawns
            )
        }
    }
}

fun World.getSpawnInfo(playerUuid: UUID): SpawnInfo? {
    return SpawnInfo.from(this, playerUuid)
}

fun PlayerRef.getSpawnInfo(): SpawnInfo? {
    val world = this.world ?: return null
    val uuid = this.uuid ?: return null
    return world.getSpawnInfo(uuid)
}

// ============================================
// Utility Functions
// ============================================

fun createTransform(x: Double, y: Double, z: Double, yaw: Float = 0f, pitch: Float = 0f): Transform {
    return Transform(Vector3d(x, y, z), Vector3f(pitch, yaw, 0f))
}

fun distanceToSpawn(world: World, playerUuid: UUID, position: Vector3d): Double? {
    val spawnPos = world.getSpawnPosition(playerUuid) ?: return null
    return spawnPos.distance(position)
}
