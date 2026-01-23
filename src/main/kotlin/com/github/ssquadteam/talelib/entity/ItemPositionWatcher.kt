package com.github.ssquadteam.talelib.entity

import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.tick.EntityTickingSystem
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.util.concurrent.ConcurrentHashMap

/**
 * TickingSystem that monitors item entity positions and dispatches events when they move.
 * This is needed because physics-based item movement doesn't trigger standard entity events.
 */
class ItemPositionWatcher : EntityTickingSystem<EntityStore>() {

    companion object {
        private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()

        @Volatile
        private var registered = false

        private data class TrackedPosition(val x: Double, val y: Double, val z: Double)

        private val lastKnownPositions = ConcurrentHashMap<Int, TrackedPosition>()

        private const val MOVE_THRESHOLD = 0.01 // Minimum distance to count as movement

        @JvmStatic
        fun register() {
            if (registered) return

            try {
                val system = ItemPositionWatcher()
                EntityStore.REGISTRY.registerSystem(system)
                registered = true
                LOGGER.atInfo().log("TaleLib: ItemPositionWatcher system registered")
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("TaleLib: Failed to register ItemPositionWatcher system")
            }
        }

        @JvmStatic
        fun isRegistered(): Boolean = registered

        @JvmStatic
        fun trackPosition(networkId: Int, x: Double, y: Double, z: Double) {
            lastKnownPositions[networkId] = TrackedPosition(x, y, z)
        }

        @JvmStatic
        fun removeTracking(networkId: Int) {
            lastKnownPositions.remove(networkId)
        }

        @JvmStatic
        fun clear() {
            lastKnownPositions.clear()
        }
    }

    override fun getQuery(): Query<EntityStore> {
        return ItemComponent.getComponentType()
    }

    override fun tick(
        dt: Float,
        index: Int,
        archetypeChunk: ArchetypeChunk<EntityStore>,
        store: Store<EntityStore>,
        commandBuffer: CommandBuffer<EntityStore>
    ) {
        if (!EntityEventDispatcher.hasItemHandlers()) return

        try {
            val entityStore = store.externalData as? EntityStore ?: return
            val world = entityStore.world

            val ref = archetypeChunk.getReferenceTo(index)

            val itemComponent = archetypeChunk.getComponent(index, ItemComponent.getComponentType()) ?: return
            val itemStack = itemComponent.itemStack ?: return

            val networkIdComponent = commandBuffer.getComponent(ref, NetworkId.getComponentType())
            val networkId = networkIdComponent?.id ?: return

            val transform = commandBuffer.getComponent(ref, TransformComponent.getComponentType()) ?: return
            val position = transform.position ?: return

            val currentX = position.x
            val currentY = position.y
            val currentZ = position.z

            val lastPos = lastKnownPositions[networkId]

            if (lastPos != null) {
                val dx = currentX - lastPos.x
                val dy = currentY - lastPos.y
                val dz = currentZ - lastPos.z
                val distSq = dx * dx + dy * dy + dz * dz

                if (distSq > MOVE_THRESHOLD * MOVE_THRESHOLD) {
                    val event = ItemEntityMovedEvent(
                        entityRef = ref,
                        world = world,
                        networkId = networkId,
                        itemStack = itemStack,
                        oldPosition = Vector3d(lastPos.x, lastPos.y, lastPos.z),
                        newPosition = Vector3d(currentX, currentY, currentZ)
                    )

                    EntityEventDispatcher.dispatchItemMoved(event)
                    lastKnownPositions[networkId] = TrackedPosition(currentX, currentY, currentZ)
                }
            } else {
                // First time seeing this entity, just track it
                lastKnownPositions[networkId] = TrackedPosition(currentX, currentY, currentZ)
            }

        } catch (e: Exception) {
            LOGGER.atFine().withCause(e).log("Error checking item position")
        }
    }
}
