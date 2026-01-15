package com.github.ssquadteam.talelib.hologram

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Hologram internal constructor(
    val id: UUID,
    internal val entityRef: Ref<EntityStore>,
    private val world: World
) {
    private val store: Store<EntityStore>?
        get() = world.entityStore?.store

    var text: String
        get() = store?.getComponent(entityRef, Nameplate.getComponentType())?.text ?: ""
        set(value) {
            val s = store ?: return
            world.execute {
                s.getComponent(entityRef, Nameplate.getComponentType())?.text = value
            }
        }

    var position: Vector3d?
        get() = store?.getComponent(entityRef, TransformComponent.getComponentType())?.position
        set(value) {
            val s = store ?: return
            val pos = value ?: return
            world.execute {
                s.getComponent(entityRef, TransformComponent.getComponentType())?.position?.assign(pos)
            }
        }

    fun setPosition(x: Double, y: Double, z: Double) {
        val s = store ?: return
        world.execute {
            s.getComponent(entityRef, TransformComponent.getComponentType())?.position?.assign(x, y, z)
        }
    }

    fun remove() {
        val s = store ?: return
        world.execute {
            s.removeEntity(entityRef)
        }
        HologramManager.unregister(id)
    }

    fun isValid(): Boolean = entityRef.isValid
}

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
