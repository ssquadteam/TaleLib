package com.github.ssquadteam.talelib.world

import com.hypixel.hytale.math.util.ChunkUtil
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.World
import java.util.concurrent.CompletableFuture

/**
 * World-related extension functions and utilities for TaleLib.
 */

/**
 * Get a unique identifier for this world.
 * Uses the world's name as the identifier since World doesn't have a UUID property.
 */
val World.worldId: String
    get() = this.name

/**
 * Find a world by its name/ID.
 * @param worldId The name of the world to find
 * @return The World if found, null otherwise
 */
fun findWorldByName(worldId: String): World? {
    return try {
        val worlds = Universe.get().worlds
        worlds.values.find { it.name == worldId }
    } catch (e: Throwable) {
        null
    }
}

/**
 * Get all currently loaded worlds.
 * @return A collection of all loaded worlds
 */
fun getAllWorlds(): Collection<World> {
    return try {
        Universe.get().worlds.values
    } catch (e: Throwable) {
        emptyList()
    }
}

/**
 * Get the default/main world.
 * Returns the first world in the universe or null if none exist.
 */
fun getDefaultWorld(): World? {
    return try {
        Universe.get().worlds.values.firstOrNull()
    } catch (e: Throwable) {
        null
    }
}

/**
 * Check if a world exists by name.
 * @param worldId The name of the world to check
 * @return true if the world exists, false otherwise
 */
fun worldExists(worldId: String): Boolean {
    return findWorldByName(worldId) != null
}

/**
 * Get the number of loaded worlds.
 */
fun worldCount(): Int {
    return try {
        Universe.get().worlds.size
    } catch (e: Throwable) {
        0
    }
}

/**
 * Check if the world thread is started and accepting tasks.
 * World extends TickingThread which provides isStarted.
 */
fun World.isWorldThreadStarted(): Boolean {
    return try {
        this.isStarted
    } catch (e: Throwable) {
        false
    }
}

/**
 * Start the world thread if not already started.
 * Returns a CompletableFuture that completes when the world is ready.
 * Safe to call if already started (will return completed future).
 */
fun World.ensureStarted(): CompletableFuture<Void> {
    return try {
        if (!this.isStarted) {
            this.start()
        } else {
            CompletableFuture.completedFuture(null)
        }
    } catch (e: IllegalStateException) {
        CompletableFuture.completedFuture(null)
    } catch (e: Throwable) {
        CompletableFuture.failedFuture(e)
    }
}

/**
 * Request a chunk to be loaded or generated.
 * @param chunkX Hytale chunk X coordinate (32-block chunks)
 * @param chunkZ Hytale chunk Z coordinate (32-block chunks)
 * @return CompletableFuture that completes when chunk is ready
 */
fun World.ensureChunkGenerated(chunkX: Int, chunkZ: Int): CompletableFuture<Boolean> {
    return try {
        val chunkStore = this.chunkStore ?: return CompletableFuture.completedFuture(false)
        val chunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ)
        chunkStore.getChunkReferenceAsync(chunkIndex, 0).thenApply { ref ->
            ref != null && ref.isValid
        }
    } catch (e: Throwable) {
        CompletableFuture.completedFuture(false)
    }
}

/**
 * Pre-generate chunks in a radius around a block position.
 * @param blockX World block X coordinate
 * @param blockZ World block Z coordinate
 * @param radiusChunks Radius in Hytale chunks (32-block chunks)
 * @return CompletableFuture that completes when all chunks are ready
 */
fun World.pregenerateChunksAround(blockX: Int, blockZ: Int, radiusChunks: Int): CompletableFuture<Int> {
    return try {
        val chunkStore = this.chunkStore ?: return CompletableFuture.completedFuture(0)
        val centerChunkX = blockX shr 5
        val centerChunkZ = blockZ shr 5

        val futures = mutableListOf<CompletableFuture<*>>()
        var count = 0

        for (dx in -radiusChunks..radiusChunks) {
            for (dz in -radiusChunks..radiusChunks) {
                val chunkIndex = ChunkUtil.indexChunk(centerChunkX + dx, centerChunkZ + dz)
                futures.add(chunkStore.getChunkReferenceAsync(chunkIndex, 0))
                count++
            }
        }

        CompletableFuture.allOf(*futures.toTypedArray()).thenApply { count }
    } catch (e: Throwable) {
        CompletableFuture.completedFuture(0)
    }
}
