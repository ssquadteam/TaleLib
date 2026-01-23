package com.github.ssquadteam.talelib.block

import com.hypixel.hytale.builtin.blockphysics.BlockPhysicsSystems
import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.dependency.Dependency
import com.hypixel.hytale.component.dependency.Order
import com.hypixel.hytale.component.dependency.SystemDependency
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.tick.EntityTickingSystem
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.math.util.ChunkUtil
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import javax.annotation.Nonnull

/**
 * ECS system that detects ALL block changes after physics and dispatches events.
 * Runs AFTER BlockPhysicsSystems.Ticking to capture physics-caused block changes.
 */
class BlockChangeSyncSystem : EntityTickingSystem<ChunkStore>() {

    companion object {
        private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()

        @Suppress("UNCHECKED_CAST")
        private val QUERY: Query<ChunkStore> = Query.and(
            ChunkSection.getComponentType() as Query<ChunkStore>,
            BlockSection.getComponentType() as Query<ChunkStore>
        )

        private val DEPENDENCIES: Set<Dependency<ChunkStore>> = setOf(
            SystemDependency(Order.AFTER, BlockPhysicsSystems.Ticking::class.java)
        )

        @Volatile
        private var registered = false

        @JvmStatic
        fun register() {
            if (registered) return

            try {
                val system = BlockChangeSyncSystem()
                ChunkStore.REGISTRY.registerSystem(system)
                registered = true
                LOGGER.atInfo().log("TaleLib: BlockChangeSyncSystem registered")
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("TaleLib: Failed to register BlockChangeSyncSystem")
            }
        }

        @JvmStatic
        fun isRegistered(): Boolean = registered
    }

    @Nonnull
    override fun getQuery(): Query<ChunkStore> = QUERY

    @Nonnull
    override fun getDependencies(): Set<Dependency<ChunkStore>> = DEPENDENCIES

    override fun tick(
        dt: Float,
        index: Int,
        archetypeChunk: ArchetypeChunk<ChunkStore>,
        store: Store<ChunkStore>,
        commandBuffer: CommandBuffer<ChunkStore>
    ) {
        // Skip processing if no handlers are registered
        if (!BlockEventDispatcher.hasHandlers()) return

        try {
            val blockSection = archetypeChunk.getComponent(index, BlockSection.getComponentType()) ?: return
            val chunkSection = archetypeChunk.getComponent(index, ChunkSection.getComponentType()) ?: return

            // Get all changed positions since last tick (atomically clears the set)
            val changedPositions: IntOpenHashSet = blockSection.getAndClearChangedPositions()
            if (changedPositions.isEmpty()) return

            val chunkStore = store.externalData as? ChunkStore ?: return
            val world = chunkStore.world

            // Calculate base world coordinates for this chunk section
            val baseX = ChunkUtil.minBlock(chunkSection.x)
            val baseY = ChunkUtil.minBlock(chunkSection.y)
            val baseZ = ChunkUtil.minBlock(chunkSection.z)

            // Process each changed block
            val iter = changedPositions.intIterator()
            while (iter.hasNext()) {
                val blockIndex = iter.nextInt()

                // Extract local coordinates from packed index
                val localX = ChunkUtil.xFromIndex(blockIndex)
                val localY = ChunkUtil.yFromIndex(blockIndex)
                val localZ = ChunkUtil.zFromIndex(blockIndex)

                // Convert to world coordinates
                val worldX = baseX + localX
                val worldY = baseY + localY
                val worldZ = baseZ + localZ

                // Get current block state
                val blockId = blockSection.get(blockIndex)
                val rotationIndex = blockSection.getRotationIndex(blockIndex)

                // Dispatch event
                val event = BlockChangedEvent(
                    world = world,
                    x = worldX,
                    y = worldY,
                    z = worldZ,
                    blockId = blockId,
                    rotationIndex = rotationIndex
                )

                BlockEventDispatcher.dispatchBlockChanged(event)
            }

        } catch (e: Exception) {
            LOGGER.atFine().withCause(e).log("Error processing block changes")
        }
    }
}
