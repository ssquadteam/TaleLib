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
    val isOnWorldThread = Thread.currentThread().name.contains("WorldThread")

    if (isOnWorldThread) {
        return createHologramInternal(text, x, y, z)
    } else {
        val future = CompletableFuture<Hologram?>()
        this.execute {
            future.complete(createHologramInternal(text, x, y, z))
        }
        return try {
            future.get()
        } catch (e: Exception) {
            null
        }
    }
}

private fun World.createHologramInternal(text: String, x: Double, y: Double, z: Double): Hologram? {
    val entityStore = this.entityStore ?: return null
    val store = entityStore.store

    val holder = EntityStore.REGISTRY.newHolder()

    val projectileComponent = ProjectileComponent("Projectile")
    holder.putComponent(ProjectileComponent.getComponentType(), projectileComponent)

    holder.putComponent(
        TransformComponent.getComponentType(),
        TransformComponent(Vector3d(x, y, z), Vector3f(0f, 0f, 0f))
    )

    holder.ensureComponent(UUIDComponent.getComponentType())
    holder.ensureComponent(Intangible.getComponentType())

    if (projectileComponent.projectile == null) {
        projectileComponent.initialize()
        if (projectileComponent.projectile == null) {
            return null
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

    return if (ref != null) {
        val hologram = Hologram(UUID.randomUUID(), ref, this)
        HologramManager.register(hologram)
        hologram
    } else {
        null
    }
}

fun World.createHologram(text: String, position: Vector3d): Hologram? =
    createHologram(text, position.x, position.y, position.z)

fun World.createMultiLineHologram(
    lines: List<String>,
    x: Double,
    y: Double,
    z: Double,
    lineSpacing: Double = 0.25
): List<Hologram> {
    return lines.mapIndexedNotNull { index, line ->
        createHologram(line, x, y + (lines.size - 1 - index) * lineSpacing, z)
    }
}

fun World.createMultiLineHologram(
    lines: List<String>,
    position: Vector3d,
    lineSpacing: Double = 0.25
): List<Hologram> = createMultiLineHologram(lines, position.x, position.y, position.z, lineSpacing)
