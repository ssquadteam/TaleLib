@file:JvmName("TeleportExtensions")

package com.github.ssquadteam.talelib.teleport

import com.github.ssquadteam.talelib.world.Location
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

fun PlayerRef.teleport(x: Double, y: Double, z: Double, yaw: Float = 0f, pitch: Float = 0f) {
    val ref = this.reference ?: return
    val store = ref.store
    val teleport = Teleport(Vector3d(x, y, z), Vector3f(yaw, pitch, 0f))
    store.putComponent(ref, Teleport.getComponentType(), teleport)
}

fun PlayerRef.teleport(position: Vector3d, rotation: Vector3f? = null) {
    teleport(
        position.x,
        position.y,
        position.z,
        rotation?.yaw ?: 0f,
        rotation?.pitch ?: 0f
    )
}

fun PlayerRef.teleport(location: Location) {
    teleport(
        location.x.toDouble(),
        location.y.toDouble(),
        location.z.toDouble(),
        location.yaw,
        location.pitch
    )
}

fun PlayerRef.teleportTo(other: PlayerRef) {
    val otherRef = other.reference ?: return
    val store = otherRef.store
    val transform = store.getComponent(otherRef, TransformComponent.getComponentType()) ?: return
    teleport(transform.position, transform.rotation)
}

fun PlayerRef.teleportToWorld(targetWorld: World, x: Double, y: Double, z: Double, yaw: Float = 0f, pitch: Float = 0f) {
    val ref = this.reference ?: return
    val store = ref.store
    val teleport = Teleport(targetWorld, Vector3d(x, y, z), Vector3f(yaw, pitch, 0f))
    store.putComponent(ref, Teleport.getComponentType(), teleport)
}

fun PlayerRef.teleportRelative(dx: Double = 0.0, dy: Double = 0.0, dz: Double = 0.0) {
    val ref = this.reference ?: return
    val store = ref.store
    val transform = store.getComponent(ref, TransformComponent.getComponentType()) ?: return
    val pos = transform.position
    teleport(pos.x + dx, pos.y + dy, pos.z + dz)
}
