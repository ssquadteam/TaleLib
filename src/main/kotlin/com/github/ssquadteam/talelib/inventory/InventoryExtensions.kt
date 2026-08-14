@file:JvmName("InventoryExtensions")

package com.github.ssquadteam.talelib.inventory

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.inventory.InventoryComponent
import com.hypixel.hytale.server.core.inventory.InventoryUtils
import com.hypixel.hytale.server.core.inventory.ItemStack
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

fun PlayerRef.getPlayerComponent(): Player? {
    val ref = this.reference ?: return null
    return ref.store.getComponent(ref, Player.getComponentType())
}

val Ref<EntityStore>.everything: CombinedItemContainer
    get() = InventoryComponent.getCombined(store, this, *InventoryComponent.EVERYTHING)

val Ref<EntityStore>.hotbarFirst: CombinedItemContainer
    get() = InventoryComponent.getCombined(store, this, *InventoryComponent.HOTBAR_FIRST)

fun Ref<EntityStore>.giveItem(item: ItemStack): Boolean = hotbarFirst.addItemStack(item).succeeded()

fun Ref<EntityStore>.giveItem(itemId: String, quantity: Int = 1): Boolean = giveItem(ItemStack(itemId, quantity))

fun Ref<EntityStore>.countItem(itemId: String): Int = everything.countItemStacks { it.itemId == itemId }

fun Ref<EntityStore>.hasItem(itemId: String, quantity: Int = 1): Boolean = countItem(itemId) >= quantity

fun Ref<EntityStore>.removeItem(itemId: String, quantity: Int = 1): Boolean =
    everything.removeItemStack(ItemStack(itemId, quantity)).succeeded()

fun Ref<EntityStore>.clearInventory() = InventoryUtils.clear(this, store)

fun Ref<EntityStore>.hasInventorySpace(): Boolean = !hotbarFirst.isEmpty

val Ref<EntityStore>.itemInHand: ItemStack?
    get() = InventoryComponent.getItemInHand(store, this)

var Ref<EntityStore>.activeHotbarSlot: Int
    get() = InventoryUtils.getActiveSlot(this, InventoryComponent.HOTBAR_SECTION_ID, store).toInt()
    set(slot) {
        store.getComponent(this, InventoryComponent.Hotbar.getComponentType())?.setActiveSlot(slot.toByte(), this, store)
    }

fun PlayerRef.giveItem(item: ItemStack): Boolean = reference?.giveItem(item) ?: false

fun PlayerRef.giveItem(itemId: String, quantity: Int = 1): Boolean = giveItem(ItemStack(itemId, quantity))

fun PlayerRef.hasItem(itemId: String, quantity: Int = 1): Boolean = reference?.hasItem(itemId, quantity) ?: false

fun PlayerRef.removeItem(itemId: String, quantity: Int = 1): Boolean = reference?.removeItem(itemId, quantity) ?: false

fun PlayerRef.clearInventory() {
    reference?.clearInventory()
}

fun PlayerRef.getItemCount(itemId: String): Int = reference?.countItem(itemId) ?: 0

val Player.itemInHand: ItemStack?
    get() = reference?.itemInHand

val Player.activeSlot: Int
    get() = reference?.activeHotbarSlot ?: 0

fun Player.setHotbarSlot(slot: Int) {
    reference?.activeHotbarSlot = slot
}

fun Player.hasInventorySpace(): Boolean = reference?.hasInventorySpace() ?: false
