@file:JvmName("EntityExtensions")

package com.github.ssquadteam.talelib.entity

import com.hypixel.hytale.component.ComponentAccessor
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.entity.Entity
import com.hypixel.hytale.server.core.entity.EntityUtils
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

fun Ref<EntityStore>.getEntity(store: Store<EntityStore>): Entity? {
    return EntityUtils.getEntity(this, store)
}

fun Ref<EntityStore>.remove(store: Store<EntityStore>, reason: RemoveReason = RemoveReason.REMOVE) {
    store.removeEntity(this, EntityStore.REGISTRY.newHolder(), reason)
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
