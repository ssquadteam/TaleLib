package com.github.ssquadteam.talelib.entity

import com.hypixel.hytale.component.AddReason
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.RefSystem
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/**
 * RefSystem that watches for item entities (entities with ItemComponent) being added/removed.
 * Dispatches events to EntityEventDispatcher for plugins to consume.
 */
class ItemEntityWatcher : RefSystem<EntityStore>() {

    companion object {
        private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()

        @Volatile
        private var registered = false

        @JvmStatic
        fun register() {
            if (registered) return

            try {
                val system = ItemEntityWatcher()
                EntityStore.REGISTRY.registerSystem(system)
                registered = true
                LOGGER.atInfo().log("TaleLib: ItemEntityWatcher system registered")
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("TaleLib: Failed to register ItemEntityWatcher system")
            }
        }

        @JvmStatic
        fun isRegistered(): Boolean = registered
    }

    override fun getQuery(): Query<EntityStore> {
        return ItemComponent.getComponentType()
    }

    override fun onEntityAdded(
        ref: Ref<EntityStore>,
        reason: AddReason,
        store: Store<EntityStore>,
        commandBuffer: CommandBuffer<EntityStore>
    ) {
        if (!EntityEventDispatcher.hasItemHandlers()) return

        try {
            val entityStore = store.externalData as? EntityStore ?: return
            val world = entityStore.world

            val itemComponent = commandBuffer.getComponent(ref, ItemComponent.getComponentType()) ?: return
            val itemStack = itemComponent.itemStack ?: return

            val networkIdComponent = commandBuffer.getComponent(ref, NetworkId.getComponentType())
            val networkId = networkIdComponent?.id ?: return

            val transform = commandBuffer.getComponent(ref, TransformComponent.getComponentType())
            val position = transform?.position ?: Vector3d(0.0, 0.0, 0.0)

            val event = ItemEntityAddedEvent(
                entityRef = ref,
                world = world,
                networkId = networkId,
                itemStack = itemStack,
                position = Vector3d(position.x, position.y, position.z),
                reason = reason
            )

            ItemQuantityWatcher.trackQuantity(networkId, itemStack.quantity)

            EntityEventDispatcher.dispatchItemAdded(event)

        } catch (e: Exception) {
            LOGGER.atFine().withCause(e).log("Error dispatching item entity added event")
        }
    }

    override fun onEntityRemove(
        ref: Ref<EntityStore>,
        reason: RemoveReason,
        store: Store<EntityStore>,
        commandBuffer: CommandBuffer<EntityStore>
    ) {
        if (!EntityEventDispatcher.hasItemHandlers()) return

        try {
            val entityStore = store.externalData as? EntityStore ?: return
            val world = entityStore.world

            val itemComponent = commandBuffer.getComponent(ref, ItemComponent.getComponentType())
            val itemStack = itemComponent?.itemStack

            val networkIdComponent = commandBuffer.getComponent(ref, NetworkId.getComponentType())
            val networkId = networkIdComponent?.id ?: return

            val transform = commandBuffer.getComponent(ref, TransformComponent.getComponentType())
            val position = transform?.position?.let { Vector3d(it.x, it.y, it.z) }

            val event = ItemEntityRemovedEvent(
                entityRef = ref,
                world = world,
                networkId = networkId,
                itemStack = itemStack,
                position = position,
                reason = reason
            )

            ItemQuantityWatcher.removeTracking(networkId)

            EntityEventDispatcher.dispatchItemRemoved(event)

        } catch (e: Exception) {
            LOGGER.atFine().withCause(e).log("Error dispatching item entity removed event")
        }
    }
}
