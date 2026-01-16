package com.github.ssquadteam.talelib.prefab

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation
import com.hypixel.hytale.math.vector.Vector3i
import com.hypixel.hytale.server.core.prefab.PrefabRotation
import com.hypixel.hytale.server.core.prefab.PrefabStore
import com.hypixel.hytale.server.core.prefab.PrefabWeights
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.PrefabUtil
import java.nio.file.Path
import java.util.Random

// ============================================
// Prefab Loading Extensions
// ============================================

fun getServerPrefab(relativePath: String): IPrefabBuffer? {
    return try {
        val fullPath = PrefabStore.get().serverPrefabsPath.resolve(relativePath)
        PrefabBufferUtil.getCached(fullPath)
    } catch (e: Exception) {
        null
    }
}

fun getAssetPrefab(relativePath: String): IPrefabBuffer? {
    return try {
        val fullPath = PrefabStore.get().assetPrefabsPath.resolve(relativePath)
        PrefabBufferUtil.getCached(fullPath)
    } catch (e: Exception) {
        null
    }
}

fun getWorldGenPrefab(relativePath: String): IPrefabBuffer? {
    return try {
        val fullPath = PrefabStore.get().worldGenPrefabsPath.resolve(relativePath)
        PrefabBufferUtil.getCached(fullPath)
    } catch (e: Exception) {
        null
    }
}

fun getPrefabFromPath(path: Path): IPrefabBuffer? {
    return try {
        PrefabBufferUtil.getCached(path)
    } catch (e: Exception) {
        null
    }
}

fun getPrefabFromAnyPack(relativePath: String): IPrefabBuffer? {
    return try {
        val selection = PrefabStore.get().getAssetPrefabFromAnyPack(relativePath)
        if (selection != null) {
            val fullPath = PrefabStore.get().assetPrefabsPath.resolve(relativePath)
            PrefabBufferUtil.getCached(fullPath)
        } else null
    } catch (e: Exception) {
        null
    }
}

// ============================================
// World Prefab Spawning Extensions
// ============================================

fun World.spawnPrefab(
    prefabPath: String,
    x: Int,
    y: Int,
    z: Int,
    rotation: Rotation = Rotation.None,
    force: Boolean = false
): Boolean {
    return spawnPrefab(prefabPath, Vector3i(x, y, z), rotation, force)
}

fun World.spawnPrefab(
    prefabPath: String,
    position: Vector3i,
    rotation: Rotation = Rotation.None,
    force: Boolean = false
): Boolean {
    val buffer = getAssetPrefab(prefabPath) ?: getServerPrefab(prefabPath) ?: return false
    return spawnPrefab(buffer, position, rotation, force)
}

fun World.spawnPrefab(
    buffer: IPrefabBuffer,
    position: Vector3i,
    rotation: Rotation = Rotation.None,
    force: Boolean = false
): Boolean {
    return try {
        PrefabUtil.paste(
            buffer,
            this,
            position,
            rotation,
            force,
            Random(),
            this.entityStore.store
        )
        true
    } catch (e: Exception) {
        false
    }
}

fun World.spawnPrefabAdvanced(
    buffer: IPrefabBuffer,
    position: Vector3i,
    rotation: Rotation = Rotation.None,
    force: Boolean = false,
    random: Random = Random(),
    technicalPaste: Boolean = false,
    loadEntities: Boolean = true
): Boolean {
    return try {
        PrefabUtil.paste(
            buffer,
            this,
            position,
            rotation,
            force,
            random,
            0, // setBlockSettings
            technicalPaste,
            false, // pasteAnchorAsBlock
            loadEntities,
            this.entityStore.store
        )
        true
    } catch (e: Exception) {
        false
    }
}

// ============================================
// Prefab Removal Extensions
// ============================================

fun World.removePrefab(
    prefabPath: String,
    position: Vector3i,
    rotation: Rotation = Rotation.None,
    force: Boolean = false
): Boolean {
    val buffer = getAssetPrefab(prefabPath) ?: getServerPrefab(prefabPath) ?: return false
    return removePrefab(buffer, position, rotation, force)
}

fun World.removePrefab(
    buffer: IPrefabBuffer,
    position: Vector3i,
    rotation: Rotation = Rotation.None,
    force: Boolean = false
): Boolean {
    return try {
        PrefabUtil.remove(
            buffer,
            this,
            position,
            rotation,
            force,
            Random(),
            0,
            1.0
        )
        true
    } catch (e: Exception) {
        false
    }
}

// ============================================
// Prefab Validation Extensions
// ============================================

fun World.canPlacePrefab(
    buffer: IPrefabBuffer,
    position: Vector3i,
    rotation: Rotation = Rotation.None
): Boolean {
    return try {
        PrefabUtil.canPlacePrefab(
            buffer,
            this,
            position,
            rotation,
            null,
            Random(),
            false
        )
    } catch (e: Exception) {
        false
    }
}

fun World.prefabMatchesAtPosition(
    buffer: IPrefabBuffer,
    position: Vector3i,
    rotation: Rotation = Rotation.None
): Boolean {
    return try {
        PrefabUtil.prefabMatchesAtPosition(
            buffer,
            this,
            position,
            rotation,
            Random()
        )
    } catch (e: Exception) {
        false
    }
}

// ============================================
// Player Prefab Extensions
// ============================================

fun PlayerRef.spawnPrefabAtLocation(
    prefabPath: String,
    offsetX: Int = 0,
    offsetY: Int = 0,
    offsetZ: Int = 0,
    rotation: Rotation = Rotation.None,
    force: Boolean = false
): Boolean {
    val world = this.world ?: return false
    val pos = this.position ?: return false

    val targetPos = Vector3i(
        pos.x.toInt() + offsetX,
        pos.y.toInt() + offsetY,
        pos.z.toInt() + offsetZ
    )

    return world.spawnPrefab(prefabPath, targetPos, rotation, force)
}

fun PlayerRef.spawnPrefabAtLookTarget(
    prefabPath: String,
    maxDistance: Double = 10.0,
    rotation: Rotation = Rotation.None,
    force: Boolean = false
): Boolean {
    val world = this.world ?: return false

    // Get raycast target using collision extensions if available
    // For now, use position-based placement
    val pos = this.position ?: return false

    // Simple forward placement based on facing
    val ref = this.ref ?: return false
    val store = ref.store
    val transform = store.getComponent(ref, com.hypixel.hytale.server.core.entity.component.TransformComponent.getComponentType())
        ?: return false
    val rot = transform.rotation ?: return false

    val yaw = Math.toRadians(rot.y.toDouble())
    val targetX = (pos.x - kotlin.math.sin(yaw) * maxDistance).toInt()
    val targetZ = (pos.z + kotlin.math.cos(yaw) * maxDistance).toInt()
    val targetPos = Vector3i(targetX, pos.y.toInt(), targetZ)

    return world.spawnPrefab(prefabPath, targetPos, rotation, force)
}

// ============================================
// Prefab Rotation Utilities
// ============================================

object PrefabRotations {
    val NONE = PrefabRotation.ROTATION_0
    val DEG_90 = PrefabRotation.ROTATION_90
    val DEG_180 = PrefabRotation.ROTATION_180
    val DEG_270 = PrefabRotation.ROTATION_270

    fun random(): PrefabRotation {
        return PrefabRotation.VALUES[Random().nextInt(4)]
    }

    fun random(rng: Random): PrefabRotation {
        return PrefabRotation.VALUES[rng.nextInt(4)]
    }

    fun fromRotation(rotation: Rotation): PrefabRotation {
        return PrefabRotation.fromRotation(rotation)
    }

    fun toRotation(prefabRotation: PrefabRotation): Rotation {
        return prefabRotation.rotation
    }
}

// ============================================
// Prefab Weights Builder
// ============================================

class PrefabWeightsBuilder {
    private var defaultWeight: Double = 1.0
    private val weights = mutableMapOf<String, Double>()

    fun default(weight: Double): PrefabWeightsBuilder {
        this.defaultWeight = weight
        return this
    }

    fun weight(prefabName: String, weight: Double): PrefabWeightsBuilder {
        weights[prefabName] = weight
        return this
    }

    fun rare(prefabName: String): PrefabWeightsBuilder {
        return weight(prefabName, 0.1)
    }

    fun uncommon(prefabName: String): PrefabWeightsBuilder {
        return weight(prefabName, 0.5)
    }

    fun common(prefabName: String): PrefabWeightsBuilder {
        return weight(prefabName, 2.0)
    }

    fun veryCommon(prefabName: String): PrefabWeightsBuilder {
        return weight(prefabName, 5.0)
    }

    fun build(): PrefabWeights {
        val result = PrefabWeights()
        result.defaultWeight = defaultWeight
        weights.forEach { (name, w) -> result.setWeight(name, w) }
        return result
    }
}

fun prefabWeights(block: PrefabWeightsBuilder.() -> Unit): PrefabWeights {
    return PrefabWeightsBuilder().apply(block).build()
}

// ============================================
// Prefab Info Data Class
// ============================================

data class PrefabInfo(
    val path: Path,
    val anchorX: Int,
    val anchorY: Int,
    val anchorZ: Int,
    val width: Int,
    val height: Int,
    val depth: Int,
    val columnCount: Int,
    val hasChildPrefabs: Boolean
) {
    companion object {
        fun from(buffer: IPrefabBuffer, path: Path): PrefabInfo {
            val rotation = PrefabRotation.ROTATION_0
            return PrefabInfo(
                path = path,
                anchorX = buffer.anchorX,
                anchorY = buffer.anchorY,
                anchorZ = buffer.anchorZ,
                width = buffer.getMaxX(rotation) - buffer.getMinX(rotation),
                height = buffer.maxY - buffer.minY,
                depth = buffer.getMaxZ(rotation) - buffer.getMinZ(rotation),
                columnCount = buffer.columnCount,
                hasChildPrefabs = buffer.childPrefabs?.isNotEmpty() == true
            )
        }
    }
}

fun IPrefabBuffer.getInfo(path: Path): PrefabInfo {
    return PrefabInfo.from(this, path)
}

// ============================================
// Prefab Store Utilities
// ============================================

fun prefabExists(prefabPath: String): Boolean {
    return getAssetPrefab(prefabPath) != null || getServerPrefab(prefabPath) != null
}

fun getServerPrefabsDirectory(): Path {
    return PrefabStore.get().serverPrefabsPath
}

fun getAssetPrefabsDirectory(): Path {
    return PrefabStore.get().assetPrefabsPath
}

fun getWorldGenPrefabsDirectory(): Path {
    return PrefabStore.get().worldGenPrefabsPath
}
