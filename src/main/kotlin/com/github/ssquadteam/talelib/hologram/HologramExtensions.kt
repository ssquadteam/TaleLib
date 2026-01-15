@file:JvmName("HologramExtensions")

package com.github.ssquadteam.talelib.hologram

import com.hypixel.hytale.math.vector.Vector3d

// Note: Creating holograms (entities with nameplate components) requires complex entity creation
// which depends on the NPC/prefab system. For now, this file provides placeholder functions.
// To create holograms, consider using Hytale's native NPC system or custom prefabs.

// Placeholder for future hologram creation when proper API is available
fun createHologramText(text: String, x: Double, y: Double, z: Double): String? {
    // TODO: Implement using proper entity creation API when available
    return null
}

fun createHologramText(text: String, position: Vector3d): String? =
    createHologramText(text, position.x, position.y, position.z)
