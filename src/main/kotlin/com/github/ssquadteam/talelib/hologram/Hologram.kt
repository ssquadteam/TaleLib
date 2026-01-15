package com.github.ssquadteam.talelib.hologram

import java.util.*
import java.util.concurrent.ConcurrentHashMap

// Note: The hologram system is a placeholder. Creating holograms (entities with nameplate
// components) requires complex entity/prefab system integration. For text displays,
// consider using Hytale's native NPC system or custom prefabs with nameplate components.

/**
 * Placeholder hologram class.
 * In a full implementation, this would wrap an entity with a Nameplate component.
 */
class Hologram internal constructor(
    val id: UUID,
    var text: String,
    var x: Double,
    var y: Double,
    var z: Double
) {
    fun setPosition(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun remove() {
        HologramManager.unregister(id)
    }

    fun isValid(): Boolean = HologramManager.get(id) != null
}

/**
 * Manager for tracking placeholder holograms.
 */
object HologramManager {
    private val holograms = ConcurrentHashMap<UUID, Hologram>()

    internal fun register(hologram: Hologram) {
        holograms[hologram.id] = hologram
    }

    internal fun unregister(id: UUID) {
        holograms.remove(id)
    }

    fun get(id: UUID): Hologram? = holograms[id]

    fun getAll(): Collection<Hologram> = holograms.values.toList()

    fun removeAll() {
        holograms.values.toList().forEach { it.remove() }
    }

    fun count(): Int = holograms.size
}
