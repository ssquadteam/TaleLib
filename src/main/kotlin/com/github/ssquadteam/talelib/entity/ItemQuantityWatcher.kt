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
 * TickingSystem that monitors item entity quantities and dispatches events when they change.
 * This is needed because Hytale's ItemMergeSystem changes quantities without firing events.
 */
class ItemQuantityWatcher : EntityTickingSystem<EntityStore>() {

    companion object {
        private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()

        @Volatile
        private var registered = false

        private val lastKnownQuantities = ConcurrentHashMap<Int, Int>()

        @JvmStatic
        fun register() {
            if (registered) return

            try {
                val system = ItemQuantityWatcher()
                EntityStore.REGISTRY.registerSystem(system)
                registered = true
                LOGGER.atInfo().log("TaleLib: ItemQuantityWatcher system registered")
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("TaleLib: Failed to register ItemQuantityWatcher system")
            }
        }

        @JvmStatic
        fun isRegistered(): Boolean = registered

        @JvmStatic
        fun trackQuantity(networkId: Int, quantity: Int) {
            lastKnownQuantities[networkId] = quantity
        }

        @JvmStatic
        fun removeTracking(networkId: Int) {
            lastKnownQuantities.remove(networkId)
        }

        @JvmStatic
        fun clear() {
            lastKnownQuantities.clear()
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

            val currentQuantity = itemStack.quantity
            val lastQuantity = lastKnownQuantities[networkId]

            if (lastQuantity != null && lastQuantity != currentQuantity) {
                val transform = commandBuffer.getComponent(ref, TransformComponent.getComponentType())
                val position = transform?.position ?: Vector3d(0.0, 0.0, 0.0)

                val event = ItemQuantityChangedEvent(
                    entityRef = ref,
                    world = world,
                    networkId = networkId,
                    itemStack = itemStack,
                    oldQuantity = lastQuantity,
                    newQuantity = currentQuantity
                )

                EntityEventDispatcher.dispatchItemQuantityChanged(event)
                lastKnownQuantities[networkId] = currentQuantity
            } else if (lastQuantity == null) {
                lastKnownQuantities[networkId] = currentQuantity
            }

        } catch (e: Exception) {
            LOGGER.atFine().withCause(e).log("Error checking item quantity")
        }
    }
}
