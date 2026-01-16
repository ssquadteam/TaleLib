package com.github.ssquadteam.talelib.hologram

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a text hologram in the world.
 *
 * Holograms are invisible entities with a Nameplate component that displays floating text.
 * They are created using a ProjectileComponent as an entity shell with Intangible component
 * to make them non-collidable.
 */
class Hologram internal constructor(
    val id: UUID,
    internal val entityRef: Ref<EntityStore>,
    private val world: World
) {
    /**
     * Gets or sets the hologram text.
     */
    var text: String
        get() {
            val entityStore = world.entityStore ?: return ""
            return entityStore.store.getComponent(entityRef, Nameplate.getComponentType())?.text ?: ""
        }
        set(value) {
            world.execute {
                val entityStore = world.entityStore ?: return@execute
                entityStore.store.getComponent(entityRef, Nameplate.getComponentType())?.text = value
            }
        }

    /**
     * Gets the hologram's current position.
     */
    val position: Vector3d?
        get() {
            val entityStore = world.entityStore ?: return null
            return entityStore.store.getComponent(entityRef, TransformComponent.getComponentType())?.position
        }

    /**
     * Sets the hologram's position.
     */
    fun setPosition(x: Double, y: Double, z: Double) {
        world.execute {
            val entityStore = world.entityStore ?: return@execute
            entityStore.store.getComponent(entityRef, TransformComponent.getComponentType())?.position?.assign(x, y, z)
        }
    }

    /**
     * Sets the hologram's position.
     */
    fun setPosition(position: Vector3d) {
        setPosition(position.x, position.y, position.z)
    }

    /**
     * Removes the hologram from the world.
     */
    fun remove() {
        world.execute {
            val entityStore = world.entityStore ?: return@execute
            entityStore.store.removeEntity(entityRef, EntityStore.REGISTRY.newHolder(), RemoveReason.REMOVE)
        }
        HologramManager.unregister(id)
    }

    /**
     * Checks if the hologram entity is still valid.
     */
    fun isValid(): Boolean = entityRef.isValid
}

/**
 * Manager for tracking holograms created by TaleLib.
 */
object HologramManager {
    private val holograms = ConcurrentHashMap<UUID, Hologram>()

    internal fun register(hologram: Hologram) {
        holograms[hologram.id] = hologram
    }

    internal fun unregister(id: UUID) {
        holograms.remove(id)
    }

    /**
     * Gets a hologram by its ID.
     */
    fun get(id: UUID): Hologram? = holograms[id]

    /**
     * Gets all registered holograms.
     */
    fun getAll(): Collection<Hologram> = holograms.values.toList()

    /**
     * Removes all registered holograms from the world.
     */
    fun removeAll() {
        holograms.values.toList().forEach { it.remove() }
    }

    /**
     * Gets the number of registered holograms.
     */
    fun count(): Int = holograms.size
}
