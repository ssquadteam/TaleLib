@file:JvmName("InventoryExtensions")

package com.github.ssquadteam.talelib.inventory

import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.inventory.Inventory
import com.hypixel.hytale.server.core.inventory.ItemStack
import com.hypixel.hytale.server.core.universe.PlayerRef

fun PlayerRef.getPlayerComponent(): Player? {
    val ref = this.reference ?: return null
    return ref.store.getComponent(ref, Player.getComponentType())
}

val PlayerRef.inventory: Inventory?
    get() = getPlayerComponent()?.inventory

fun PlayerRef.giveItem(item: ItemStack): Boolean {
    val inv = inventory ?: return false
    return inv.combinedHotbarFirst.addItemStack(item).succeeded()
}

fun PlayerRef.giveItem(itemId: String, quantity: Int = 1): Boolean =
    giveItem(ItemStack(itemId, quantity))

fun PlayerRef.hasItem(itemId: String, quantity: Int = 1): Boolean {
    val inv = inventory ?: return false
    val count = inv.combinedEverything.countItemStacks { it.itemId == itemId }
    return count >= quantity
}

fun PlayerRef.removeItem(itemId: String, quantity: Int = 1): Boolean {
    val inv = inventory ?: return false
    return inv.combinedEverything.removeItemStack(ItemStack(itemId, quantity)).succeeded()
}

fun PlayerRef.clearInventory() {
    inventory?.clear()
}

fun PlayerRef.getItemCount(itemId: String): Int {
    val inv = inventory ?: return 0
    return inv.combinedEverything.countItemStacks { it.itemId == itemId }
}

val Player.itemInHand: ItemStack?
    get() = inventory?.itemInHand

val Player.activeSlot: Int
    get() = inventory?.activeHotbarSlot?.toInt() ?: 0

fun Player.setHotbarSlot(slot: Int) {
    inventory?.setActiveHotbarSlot(slot.toByte())
}

fun Player.hasInventorySpace(): Boolean {
    val inv = inventory ?: return false
    return !inv.combinedHotbarFirst.isEmpty
}

fun Inventory.giveItem(item: ItemStack): Boolean =
    combinedHotbarFirst.addItemStack(item).succeeded()

fun Inventory.giveItem(itemId: String, quantity: Int = 1): Boolean =
    giveItem(ItemStack(itemId, quantity))

fun Inventory.hasItem(itemId: String, quantity: Int = 1): Boolean {
    val count = combinedEverything.countItemStacks { it.itemId == itemId }
    return count >= quantity
}

fun Inventory.removeItem(itemId: String, quantity: Int = 1): Boolean =
    combinedEverything.removeItemStack(ItemStack(itemId, quantity)).succeeded()

fun Inventory.countItem(itemId: String): Int =
    combinedEverything.countItemStacks { it.itemId == itemId }

fun Inventory.hasSpace(): Boolean =
    !combinedHotbarFirst.isEmpty
