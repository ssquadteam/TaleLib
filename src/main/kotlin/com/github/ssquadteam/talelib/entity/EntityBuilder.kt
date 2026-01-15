package com.github.ssquadteam.talelib.entity

import com.hypixel.hytale.component.AddReason
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class EntityBuilder(private val world: World, private val prefabId: String) {
    private var x: Double = 0.0
    private var y: Double = 0.0
    private var z: Double = 0.0
    private var yaw: Float = 0f
    private var pitch: Float = 0f
    private var roll: Float = 0f

    fun position(x: Double, y: Double, z: Double): EntityBuilder {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun position(pos: Vector3d): EntityBuilder = position(pos.x, pos.y, pos.z)

    fun rotation(yaw: Float, pitch: Float = 0f, roll: Float = 0f): EntityBuilder {
        this.yaw = yaw
        this.pitch = pitch
        this.roll = roll
        return this
    }

    fun spawn(): Ref<EntityStore>? {
        var result: Ref<EntityStore>? = null
        world.execute {
            val store = world.entityStore?.store ?: return@execute
            val holder = EntityStore.REGISTRY.newHolder(prefabId) ?: return@execute

            val transform = holder.getComponent(TransformComponent.getComponentType())
            transform?.position?.assign(x, y, z)
            transform?.rotation?.assign(yaw, pitch, roll)

            result = store.addEntity(holder, AddReason.SPAWN)
        }
        return result
    }
}

fun World.entity(prefabId: String, block: EntityBuilder.() -> Unit): Ref<EntityStore>? {
    return EntityBuilder(this, prefabId).apply(block).spawn()
}
