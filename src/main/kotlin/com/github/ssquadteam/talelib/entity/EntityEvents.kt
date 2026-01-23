package com.github.ssquadteam.talelib.entity

import com.hypixel.hytale.component.AddReason
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.inventory.ItemStack
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

data class ItemEntityAddedEvent(
    val entityRef: Ref<EntityStore>,
    val world: World,
    val networkId: Int,
    val itemStack: ItemStack,
    val position: Vector3d,
    val reason: AddReason
)

data class ItemEntityRemovedEvent(
    val entityRef: Ref<EntityStore>,
    val world: World,
    val networkId: Int,
    val itemStack: ItemStack?,
    val position: Vector3d?,
    val reason: RemoveReason
)

data class ItemEntityMovedEvent(
    val entityRef: Ref<EntityStore>,
    val world: World,
    val networkId: Int,
    val itemStack: ItemStack,
    val oldPosition: Vector3d,
    val newPosition: Vector3d
)

data class ItemQuantityChangedEvent(
    val entityRef: Ref<EntityStore>,
    val world: World,
    val networkId: Int,
    val itemStack: ItemStack,
    val oldQuantity: Int,
    val newQuantity: Int
)

interface ItemEntityListener {
    fun onItemEntityAdded(event: ItemEntityAddedEvent) {}
    fun onItemEntityRemoved(event: ItemEntityRemovedEvent) {}
    fun onItemEntityMoved(event: ItemEntityMovedEvent) {}
    fun onItemQuantityChanged(event: ItemQuantityChangedEvent) {}
}

fun interface ItemEntityAddedHandler {
    fun handle(event: ItemEntityAddedEvent)
}

fun interface ItemEntityRemovedHandler {
    fun handle(event: ItemEntityRemovedEvent)
}

fun interface ItemEntityMovedHandler {
    fun handle(event: ItemEntityMovedEvent)
}

fun interface ItemQuantityChangedHandler {
    fun handle(event: ItemQuantityChangedEvent)
}
