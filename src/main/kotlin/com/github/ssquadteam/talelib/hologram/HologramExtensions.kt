@file:JvmName("HologramExtensionsKt")

package com.github.ssquadteam.talelib.hologram

import com.hypixel.hytale.component.AddReason
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.entity.UUIDComponent
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate
import com.hypixel.hytale.server.core.modules.entity.component.Intangible
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Creates a hologram at the specified position.
 *
 * This creates an invisible entity with a Nameplate component that displays floating text.
 * The entity uses a ProjectileComponent as a shell and Intangible to make it non-collidable.
 *
 * @param text The text to display
 * @param x X coordinate
 * @param y Y coordinate
 * @param z Z coordinate
 * @return The created Hologram, or null if creation failed
 */
fun World.createHologram(text: String, x: Double, y: Double, z: Double): Hologram? {
    val future = CompletableFuture<Hologram?>()

    this.execute {
        val entityStore = this.entityStore ?: run {
            future.complete(null)
            return@execute
        }
        val store = entityStore.store

        // Create entity holder
        val holder = EntityStore.REGISTRY.newHolder()

        // Add ProjectileComponent as entity shell (required for valid entity)
        val projectileComponent = ProjectileComponent("Projectile")
        holder.putComponent(ProjectileComponent.getComponentType(), projectileComponent)

        // Add TransformComponent for position
        holder.putComponent(
            TransformComponent.getComponentType(),
            TransformComponent(Vector3d(x, y, z), Vector3f(0f, 0f, 0f))
        )

        // Ensure UUIDComponent and Intangible
        holder.ensureComponent(UUIDComponent.getComponentType())
        holder.ensureComponent(Intangible.getComponentType())

        // Initialize projectile (required for valid entity)
        if (projectileComponent.projectile == null) {
            projectileComponent.initialize()
            if (projectileComponent.projectile == null) {
                future.complete(null)
                return@execute
            }
        }

        // Add NetworkId for network sync
        holder.addComponent(
            NetworkId.getComponentType(),
            NetworkId(store.externalData.takeNextNetworkId())
        )

        // Add Nameplate for the hologram text
        holder.addComponent(
            Nameplate.getComponentType(),
            Nameplate(text)
        )

        // Spawn the entity
        val ref: Ref<EntityStore>? = store.addEntity(holder, AddReason.SPAWN)

        if (ref != null) {
            val hologram = Hologram(UUID.randomUUID(), ref, this)
            HologramManager.register(hologram)
            future.complete(hologram)
        } else {
            future.complete(null)
        }
    }

    return try {
        future.get()
    } catch (e: Exception) {
        null
    }
}

/**
 * Creates a hologram at the specified position.
 */
fun World.createHologram(text: String, position: Vector3d): Hologram? =
    createHologram(text, position.x, position.y, position.z)

/**
 * Creates multiple holograms for multi-line text.
 *
 * @param lines List of text lines
 * @param x X coordinate
 * @param y Y coordinate (for the bottom line)
 * @param z Z coordinate
 * @param lineSpacing Vertical spacing between lines (default 0.25)
 * @return List of created Holograms
 */
fun World.createMultiLineHologram(
    lines: List<String>,
    x: Double,
    y: Double,
    z: Double,
    lineSpacing: Double = 0.25
): List<Hologram> {
    return lines.mapIndexedNotNull { index, line ->
        // Stack lines from bottom to top
        createHologram(line, x, y + (lines.size - 1 - index) * lineSpacing, z)
    }
}

/**
 * Creates multiple holograms for multi-line text.
 */
fun World.createMultiLineHologram(
    lines: List<String>,
    position: Vector3d,
    lineSpacing: Double = 0.25
): List<Hologram> = createMultiLineHologram(lines, position.x, position.y, position.z, lineSpacing)
