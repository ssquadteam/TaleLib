@file:JvmName("TeleportExtensions")

package com.github.ssquadteam.talelib.teleport

import com.github.ssquadteam.talelib.world.Location
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World

fun PlayerRef.teleport(x: Double, y: Double, z: Double, yaw: Float = 0f, pitch: Float = 0f) {
    val player = this.player ?: return
    val world = player.world ?: return
    world.execute {
        val store = world.entityStore?.store ?: return@execute
        val teleportComponent = Teleport(Vector3d(x, y, z), Vector3f(yaw, pitch, 0f))
        store.addComponent(player.reference, Teleport.getComponentType(), teleportComponent)
    }
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
    val otherPlayer = other.player ?: return
    val world = otherPlayer.world ?: return
    world.execute {
        val store = world.entityStore?.store ?: return@execute
        val transform = store.getComponent(
            otherPlayer.reference,
            com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType()
        ) ?: return@execute
        teleport(transform.position, transform.rotation)
    }
}

fun PlayerRef.teleportToWorld(targetWorld: World, x: Double, y: Double, z: Double, yaw: Float = 0f, pitch: Float = 0f) {
    val player = this.player ?: return
    targetWorld.execute {
        val store = targetWorld.entityStore?.store ?: return@execute
        val teleportComponent = Teleport(targetWorld, Vector3d(x, y, z), Vector3f(yaw, pitch, 0f))
        store.addComponent(player.reference, Teleport.getComponentType(), teleportComponent)
    }
}

fun PlayerRef.teleportRelative(dx: Double = 0.0, dy: Double = 0.0, dz: Double = 0.0) {
    val player = this.player ?: return
    val world = player.world ?: return
    world.execute {
        val store = world.entityStore?.store ?: return@execute
        val transform = store.getComponent(
            player.reference,
            com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType()
        ) ?: return@execute
        val pos = transform.position
        teleport(pos.x + dx, pos.y + dy, pos.z + dz)
    }
}
