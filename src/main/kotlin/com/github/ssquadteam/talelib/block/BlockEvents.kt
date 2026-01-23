package com.github.ssquadteam.talelib.block

import com.hypixel.hytale.server.core.universe.world.World

/**
 * Event dispatched when a block changes in the world.
 * This includes both player-initiated changes AND physics-caused changes.
 */
data class BlockChangedEvent(
    val world: World,
    val x: Int,
    val y: Int,
    val z: Int,
    val blockId: Int,      // 0 = air (block was removed)
    val rotationIndex: Int
)

/**
 * Functional interface for handling block change events.
 */
fun interface BlockChangedHandler {
    fun handle(event: BlockChangedEvent)
}

/**
 * Listener interface for block change events.
 * Provides default empty implementation for optional override.
 */
interface BlockChangeListener {
    fun onBlockChanged(event: BlockChangedEvent) {}
}
