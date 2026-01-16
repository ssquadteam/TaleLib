package com.github.ssquadteam.talelib.blockhealth

import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealth
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthChunk
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthModule
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkUtil
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.math.vector.Vector3i
import java.time.Instant

// ============================================
// Block Health Constants
// ============================================

object BlockHealthConstants {
    const val FULL_HEALTH = 1.0f
    const val DESTROYED = 0.0f
    const val LIGHT_DAMAGE_THRESHOLD = 0.75f
    const val MODERATE_DAMAGE_THRESHOLD = 0.50f
    const val HEAVY_DAMAGE_THRESHOLD = 0.25f
    const val CRITICAL_DAMAGE_THRESHOLD = 0.01f
    const val SECONDS_UNTIL_REGENERATION = 5L
    const val HEALING_PER_SECOND = 0.1f
}

// ============================================
// World Block Health Extensions
// ============================================

fun World.getBlockHealth(x: Int, y: Int, z: Int): Float {
    val blockPos = Vector3i(x, y, z)
    return getBlockHealth(blockPos)
}

fun World.getBlockHealth(blockPos: Vector3i): Float {
    val healthChunk = getBlockHealthChunk(blockPos) ?: return BlockHealthConstants.FULL_HEALTH
    return healthChunk.getBlockHealth(blockPos)
}

fun World.damageBlock(x: Int, y: Int, z: Int, damage: Float): BlockHealthResult {
    val blockPos = Vector3i(x, y, z)
    return damageBlock(blockPos, damage)
}

fun World.damageBlock(blockPos: Vector3i, damage: Float): BlockHealthResult {
    val healthChunk = getBlockHealthChunk(blockPos)
        ?: return BlockHealthResult(BlockHealthConstants.FULL_HEALTH, false, false)

    return try {
        val currentTime = this.getTimeResource()?.gameTime ?: Instant.now()
        val result = healthChunk.damageBlock(currentTime, this, blockPos, damage)
        BlockHealthResult(
            health = result.health,
            isDestroyed = result.isDestroyed,
            isFullHealth = result.isFullHealth
        )
    } catch (e: Exception) {
        BlockHealthResult(BlockHealthConstants.FULL_HEALTH, false, false)
    }
}

fun World.repairBlock(x: Int, y: Int, z: Int, amount: Float): BlockHealthResult {
    val blockPos = Vector3i(x, y, z)
    return repairBlock(blockPos, amount)
}

fun World.repairBlock(blockPos: Vector3i, amount: Float): BlockHealthResult {
    val healthChunk = getBlockHealthChunk(blockPos)
        ?: return BlockHealthResult(BlockHealthConstants.FULL_HEALTH, false, true)

    return try {
        val result = healthChunk.repairBlock(this, blockPos, amount)
        BlockHealthResult(
            health = result.health,
            isDestroyed = result.isDestroyed,
            isFullHealth = result.isFullHealth
        )
    } catch (e: Exception) {
        BlockHealthResult(BlockHealthConstants.FULL_HEALTH, false, true)
    }
}

fun World.removeBlockHealth(x: Int, y: Int, z: Int): Boolean {
    val blockPos = Vector3i(x, y, z)
    return removeBlockHealth(blockPos)
}

fun World.removeBlockHealth(blockPos: Vector3i): Boolean {
    val healthChunk = getBlockHealthChunk(blockPos) ?: return false
    return try {
        healthChunk.removeBlock(this, blockPos)
        true
    } catch (e: Exception) {
        false
    }
}

fun World.isBlockDestroyed(x: Int, y: Int, z: Int): Boolean {
    return getBlockHealth(x, y, z) <= BlockHealthConstants.DESTROYED
}

fun World.isBlockDestroyed(blockPos: Vector3i): Boolean {
    return getBlockHealth(blockPos) <= BlockHealthConstants.DESTROYED
}

fun World.isBlockDamaged(x: Int, y: Int, z: Int): Boolean {
    return getBlockHealth(x, y, z) < BlockHealthConstants.FULL_HEALTH
}

fun World.isBlockDamaged(blockPos: Vector3i): Boolean {
    return getBlockHealth(blockPos) < BlockHealthConstants.FULL_HEALTH
}

// ============================================
// Block Fragility Extensions
// ============================================

fun World.isBlockFragile(x: Int, y: Int, z: Int): Boolean {
    val blockPos = Vector3i(x, y, z)
    return isBlockFragile(blockPos)
}

fun World.isBlockFragile(blockPos: Vector3i): Boolean {
    val healthChunk = getBlockHealthChunk(blockPos) ?: return false
    return try {
        healthChunk.isBlockFragile(blockPos)
    } catch (e: Exception) {
        false
    }
}

fun World.makeBlockFragile(x: Int, y: Int, z: Int, durationSeconds: Float): Boolean {
    val blockPos = Vector3i(x, y, z)
    return makeBlockFragile(blockPos, durationSeconds)
}

fun World.makeBlockFragile(blockPos: Vector3i, durationSeconds: Float): Boolean {
    val healthChunk = getBlockHealthChunk(blockPos) ?: return false
    return try {
        healthChunk.makeBlockFragile(blockPos, durationSeconds)
        true
    } catch (e: Exception) {
        false
    }
}

// ============================================
// Block Damage State Extensions
// ============================================

fun World.getBlockDamageState(x: Int, y: Int, z: Int): BlockDamageState {
    return getBlockDamageState(getBlockHealth(x, y, z))
}

fun World.getBlockDamageState(blockPos: Vector3i): BlockDamageState {
    return getBlockDamageState(getBlockHealth(blockPos))
}

fun getBlockDamageState(health: Float): BlockDamageState {
    return when {
        health >= BlockHealthConstants.FULL_HEALTH -> BlockDamageState.NONE
        health >= BlockHealthConstants.LIGHT_DAMAGE_THRESHOLD -> BlockDamageState.LIGHT
        health >= BlockHealthConstants.MODERATE_DAMAGE_THRESHOLD -> BlockDamageState.MODERATE
        health >= BlockHealthConstants.HEAVY_DAMAGE_THRESHOLD -> BlockDamageState.HEAVY
        health > BlockHealthConstants.DESTROYED -> BlockDamageState.CRITICAL
        else -> BlockDamageState.DESTROYED
    }
}

// ============================================
// Bulk Operations
// ============================================

fun World.getDamagedBlocksInArea(
    minX: Int, minY: Int, minZ: Int,
    maxX: Int, maxY: Int, maxZ: Int
): List<DamagedBlockInfo> {
    val results = mutableListOf<DamagedBlockInfo>()

    for (x in minX..maxX) {
        for (y in minY..maxY) {
            for (z in minZ..maxZ) {
                val health = getBlockHealth(x, y, z)
                if (health < BlockHealthConstants.FULL_HEALTH) {
                    results.add(DamagedBlockInfo(
                        position = Vector3i(x, y, z),
                        health = health,
                        state = getBlockDamageState(health),
                        isFragile = isBlockFragile(x, y, z)
                    ))
                }
            }
        }
    }

    return results
}

fun World.repairAllBlocksInArea(
    minX: Int, minY: Int, minZ: Int,
    maxX: Int, maxY: Int, maxZ: Int
): Int {
    var repairedCount = 0

    for (x in minX..maxX) {
        for (y in minY..maxY) {
            for (z in minZ..maxZ) {
                if (isBlockDamaged(x, y, z)) {
                    removeBlockHealth(x, y, z)
                    repairedCount++
                }
            }
        }
    }

    return repairedCount
}

// ============================================
// Internal Helper Functions
// ============================================

private fun World.getBlockHealthChunk(blockPos: Vector3i): BlockHealthChunk? {
    return try {
        val chunkStore = this.chunkStore
        val chunkIndex = ChunkUtil.indexChunkFromBlock(blockPos.x, blockPos.z)
        val chunkRef = chunkStore.getChunkReference(chunkIndex) ?: return null

        if (!chunkRef.isValid) return null

        val componentType = BlockHealthModule.get().blockHealthChunkComponentType
        chunkStore.store.getComponent(chunkRef, componentType)
    } catch (e: Exception) {
        null
    }
}

private fun World.getTimeResource(): com.hypixel.hytale.server.core.modules.time.WorldTimeResource? {
    return try {
        this.entityStore.getResource(com.hypixel.hytale.server.core.modules.time.WorldTimeResource.getResourceType())
    } catch (e: Exception) {
        null
    }
}

// ============================================
// Data Classes and Enums
// ============================================

enum class BlockDamageState {
    NONE,
    LIGHT,
    MODERATE,
    HEAVY,
    CRITICAL,
    DESTROYED
}

data class BlockHealthResult(
    val health: Float,
    val isDestroyed: Boolean,
    val isFullHealth: Boolean
)

data class DamagedBlockInfo(
    val position: Vector3i,
    val health: Float,
    val state: BlockDamageState,
    val isFragile: Boolean
) {
    val healthPercent: Float
        get() = health * 100f

    val damagePercent: Float
        get() = (1f - health) * 100f
}

// ============================================
// BlockHealth Extensions
// ============================================

fun BlockHealth.getDamageState(): BlockDamageState {
    return getBlockDamageState(this.health)
}

fun BlockHealth.getHealthPercent(): Float {
    return this.health * 100f
}

fun BlockHealth.getDamagePercent(): Float {
    return (1f - this.health) * 100f
}

// ============================================
// Utility Functions
// ============================================

fun calculateDamageForHealth(currentHealth: Float, targetHealth: Float): Float {
    return (currentHealth - targetHealth).coerceAtLeast(0f)
}

fun calculateHitsToDestroy(currentHealth: Float, damagePerHit: Float): Int {
    if (damagePerHit <= 0) return Int.MAX_VALUE
    return kotlin.math.ceil(currentHealth / damagePerHit).toInt()
}

fun healthToPercent(health: Float): Float {
    return (health * 100f).coerceIn(0f, 100f)
}

fun percentToHealth(percent: Float): Float {
    return (percent / 100f).coerceIn(0f, 1f)
}
