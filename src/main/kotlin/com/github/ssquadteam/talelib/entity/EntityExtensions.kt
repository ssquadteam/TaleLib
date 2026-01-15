@file:JvmName("EntityExtensions")

package com.github.ssquadteam.talelib.entity

import com.hypixel.hytale.component.AddReason
import com.hypixel.hytale.component.Holder
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.modules.entity.EntityModule
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

fun World.spawnEntity(prefabId: String, x: Double, y: Double, z: Double): Ref<EntityStore>? {
    var result: Ref<EntityStore>? = null
    this.execute {
        val store = this.entityStore?.store ?: return@execute
        val holder = EntityStore.REGISTRY.newHolder(prefabId) ?: return@execute

        val transform = holder.getComponent(TransformComponent.getComponentType())
        transform?.position?.assign(x, y, z)

        result = store.addEntity(holder, AddReason.SPAWN)
    }
    return result
}

fun World.spawnEntity(prefabId: String, position: Vector3d): Ref<EntityStore>? =
    spawnEntity(prefabId, position.x, position.y, position.z)

fun World.spawn(prefabId: String, x: Double, y: Double, z: Double): Ref<EntityStore>? =
    spawnEntity(prefabId, x, y, z)

fun World.spawn(prefabId: String, position: Vector3d): Ref<EntityStore>? =
    spawnEntity(prefabId, position.x, position.y, position.z)

fun Ref<EntityStore>.remove(store: Store<EntityStore>) {
    store.removeEntity(this)
}

fun Ref<EntityStore>.getPosition(store: Store<EntityStore>): Vector3d? {
    val transform = store.getComponent(this, TransformComponent.getComponentType())
    return transform?.position
}

fun Ref<EntityStore>.setPosition(store: Store<EntityStore>, x: Double, y: Double, z: Double) {
    val transform = store.getComponent(this, TransformComponent.getComponentType())
    transform?.position?.assign(x, y, z)
}

fun Ref<EntityStore>.setPosition(store: Store<EntityStore>, position: Vector3d) =
    setPosition(store, position.x, position.y, position.z)

fun Ref<EntityStore>.getRotation(store: Store<EntityStore>): Vector3f? {
    val transform = store.getComponent(this, TransformComponent.getComponentType())
    return transform?.rotation
}

fun Ref<EntityStore>.setRotation(store: Store<EntityStore>, yaw: Float, pitch: Float, roll: Float = 0f) {
    val transform = store.getComponent(this, TransformComponent.getComponentType())
    transform?.rotation?.assign(yaw, pitch, roll)
}
