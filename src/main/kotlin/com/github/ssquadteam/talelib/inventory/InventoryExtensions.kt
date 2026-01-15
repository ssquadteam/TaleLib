@file:JvmName("InventoryExtensions")

package com.github.ssquadteam.talelib.inventory

import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.inventory.Inventory
import com.hypixel.hytale.server.core.inventory.ItemStack
import com.hypixel.hytale.server.core.universe.PlayerRef

val PlayerRef.inventory: Inventory?
    get() = this.player?.inventory

fun PlayerRef.giveItem(item: ItemStack): Boolean {
    val inv = inventory ?: return false
    return inv.combinedHotbarFirst.addItemStack(item) != null
}

fun PlayerRef.giveItem(itemId: String, quantity: Int = 1): Boolean =
    giveItem(ItemStack(itemId, quantity))

fun PlayerRef.hasItem(itemId: String, quantity: Int = 1): Boolean {
    val inv = inventory ?: return false
    return inv.combinedEverything.countItem(itemId) >= quantity
}

fun PlayerRef.removeItem(itemId: String, quantity: Int = 1): Boolean {
    val inv = inventory ?: return false
    return inv.combinedEverything.removeItem(itemId, quantity) > 0
}

fun PlayerRef.clearInventory() {
    inventory?.clear()
}

fun PlayerRef.getItemCount(itemId: String): Int {
    val inv = inventory ?: return 0
    return inv.combinedEverything.countItem(itemId)
}

val Player.itemInHand: ItemStack?
    get() = inventory?.itemInHand

val Player.activeSlot: Int
    get() = inventory?.activeHotbarSlot?.toInt() ?: 0

fun Player.setHotbarSlot(slot: Int) {
    inventory?.setActiveHotbarSlot(slot.toShort())
}

fun Player.hasInventorySpace(): Boolean {
    return inventory?.combinedHotbarFirst?.hasSpace() ?: false
}

fun Inventory.giveItem(item: ItemStack): Boolean =
    combinedHotbarFirst.addItemStack(item) != null

fun Inventory.giveItem(itemId: String, quantity: Int = 1): Boolean =
    giveItem(ItemStack(itemId, quantity))

fun Inventory.hasItem(itemId: String, quantity: Int = 1): Boolean =
    combinedEverything.countItem(itemId) >= quantity

fun Inventory.removeItem(itemId: String, quantity: Int = 1): Boolean =
    combinedEverything.removeItem(itemId, quantity) > 0

fun Inventory.countItem(itemId: String): Int =
    combinedEverything.countItem(itemId)

fun Inventory.hasSpace(): Boolean =
    combinedHotbarFirst.hasSpace()
