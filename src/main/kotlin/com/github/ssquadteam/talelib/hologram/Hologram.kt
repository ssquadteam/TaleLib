package com.github.ssquadteam.talelib.hologram

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.entity.UUIDComponent
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
     * Gets the entity UUID from the UUIDComponent.
     * This is the UUID used for entity persistence and lookup.
     */
    val entityUuid: UUID?
        get() {
            val entityStore = world.entityStore ?: return null
            return entityStore.store.getComponent(entityRef, UUIDComponent.getComponentType())?.uuid
        }

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
        // Always unregister first to prevent double-removal attempts
        HologramManager.unregister(id)

        // Skip if entity is already invalid (already removed)
        if (!entityRef.isValid) return

        val isOnWorldThread = Thread.currentThread().name.contains("WorldThread")

        if (isOnWorldThread) {
            // Execute synchronously if already on world thread
            try {
                val entityStore = world.entityStore
                if (entityStore != null && entityRef.isValid) {
                    entityStore.store.removeEntity(entityRef, EntityStore.REGISTRY.newHolder(), RemoveReason.REMOVE)
                }
            } catch (_: IllegalStateException) {
            }
        } else {
            // Schedule for world thread
            world.execute {
                try {
                    if (!entityRef.isValid) return@execute
                    val entityStore = world.entityStore ?: return@execute
                    entityStore.store.removeEntity(entityRef, EntityStore.REGISTRY.newHolder(), RemoveReason.REMOVE)
                } catch (_: IllegalStateException) {
                }
            }
        }
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

    /**
     * Creates a Hologram wrapper for an existing entity reference.
     * Used when restoring holograms from persisted entity UUIDs.
     */
    fun wrapExisting(entityRef: Ref<EntityStore>, world: World): Hologram {
        val hologram = Hologram(UUID.randomUUID(), entityRef, world)
        register(hologram)
        return hologram
    }
}
