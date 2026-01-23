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
import com.hypixel.hytale.server.core.universe.world.chunk.systems.ChunkSystems
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import java.lang.reflect.Field
import javax.annotation.Nonnull

/**
 * ECS system that detects ALL block changes after physics and dispatches events.
 * Runs AFTER BlockPhysicsSystems.Ticking but BEFORE ChunkSystems.ReplicateChanges.
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
            SystemDependency(Order.AFTER, BlockPhysicsSystems.Ticking::class.java),
            SystemDependency(Order.BEFORE, ChunkSystems.ReplicateChanges::class.java)
        )

        private var changedPositionsField: Field? = null
        private var chunkSectionLockField: Field? = null

        @Volatile
        private var registered = false

        @JvmStatic
        fun register() {
            if (registered) return

            try {
                changedPositionsField = BlockSection::class.java.getDeclaredField("changedPositions").apply {
                    isAccessible = true
                }
                chunkSectionLockField = BlockSection::class.java.getDeclaredField("chunkSectionLock").apply {
                    isAccessible = true
                }

                val system = BlockChangeSyncSystem()
                ChunkStore.REGISTRY.registerSystem(system)
                registered = true
                LOGGER.atInfo().log("TaleLib: BlockChangeSyncSystem registered (non-destructive read mode)")
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("TaleLib: Failed to register BlockChangeSyncSystem")
            }
        }

        @JvmStatic
        fun isRegistered(): Boolean = registered

        @Suppress("UNCHECKED_CAST")
        private fun readChangedPositions(blockSection: BlockSection): IntOpenHashSet? {
            val field = changedPositionsField ?: return null
            val lockField = chunkSectionLockField ?: return null

            return try {
                val lock = lockField.get(blockSection) as java.util.concurrent.locks.StampedLock
                val stamp = lock.tryOptimisticRead()
                val positions = field.get(blockSection) as? IntOpenHashSet

                if (positions == null || positions.isEmpty()) {
                    return null
                }

                val copy = if (lock.validate(stamp)) {
                    IntOpenHashSet(positions)
                } else {
                    val readStamp = lock.readLock()
                    try {
                        IntOpenHashSet(field.get(blockSection) as IntOpenHashSet)
                    } finally {
                        lock.unlockRead(readStamp)
                    }
                }

                if (copy.isEmpty()) null else copy
            } catch (e: Exception) {
                null
            }
        }
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

            val changedPositions = readChangedPositions(blockSection) ?: return

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
