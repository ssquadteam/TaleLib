@file:JvmName("HologramExtensions")

package com.github.ssquadteam.talelib.hologram

import com.hypixel.hytale.component.AddReason
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.util.*

fun World.createHologram(text: String, x: Double, y: Double, z: Double): Hologram? {
    var hologram: Hologram? = null
    this.execute {
        val store = this.entityStore?.store ?: return@execute
        val holder = EntityStore.REGISTRY.newHolder() ?: return@execute

        val transform = holder.ensureComponent(TransformComponent.getComponentType())
        transform?.position?.assign(x, y, z)

        val nameplate = holder.ensureComponent(Nameplate.getComponentType())
        nameplate?.text = text

        val ref = store.addEntity(holder, AddReason.SPAWN) ?: return@execute
        hologram = Hologram(UUID.randomUUID(), ref, this)
        HologramManager.register(hologram!!)
    }
    return hologram
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
