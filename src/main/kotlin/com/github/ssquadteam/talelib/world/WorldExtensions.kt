package com.github.ssquadteam.talelib.world

import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.World

/**
 * World-related extension functions and utilities for TaleLib.
 */

/**
 * Get a unique identifier for this world.
 * Uses the world's name as the identifier since World doesn't have a UUID property.
 */
val World.worldId: String
    get() = this.name

/**
 * Find a world by its name/ID.
 * @param worldId The name of the world to find
 * @return The World if found, null otherwise
 */
fun findWorldByName(worldId: String): World? {
    return try {
        val worlds = Universe.get().worlds
        worlds.values.find { it.name == worldId }
    } catch (e: Throwable) {
        null
    }
}

/**
 * Get all currently loaded worlds.
 * @return A collection of all loaded worlds
 */
fun getAllWorlds(): Collection<World> {
    return try {
        Universe.get().worlds.values
    } catch (e: Throwable) {
        emptyList()
    }
}

/**
 * Get the default/main world.
 * Returns the first world in the universe or null if none exist.
 */
fun getDefaultWorld(): World? {
    return try {
        Universe.get().worlds.values.firstOrNull()
    } catch (e: Throwable) {
        null
    }
}

/**
 * Check if a world exists by name.
 * @param worldId The name of the world to check
 * @return true if the world exists, false otherwise
 */
fun worldExists(worldId: String): Boolean {
    return findWorldByName(worldId) != null
}

/**
 * Get the number of loaded worlds.
 */
fun worldCount(): Int {
    return try {
        Universe.get().worlds.size
    } catch (e: Throwable) {
        0
    }
}
