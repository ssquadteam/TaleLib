package com.github.ssquadteam.talelib.teleport

import com.github.ssquadteam.talelib.world.Location
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World

class TeleportBuilder(private val player: PlayerRef) {
    private var x: Double = 0.0
    private var y: Double = 0.0
    private var z: Double = 0.0
    private var yaw: Float = 0f
    private var pitch: Float = 0f
    private var targetWorld: World? = null
    private var resetVelocity: Boolean = true
    private var resetRoll: Boolean = false

    fun to(x: Double, y: Double, z: Double): TeleportBuilder {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun to(position: Vector3d): TeleportBuilder = to(position.x, position.y, position.z)

    fun to(location: Location): TeleportBuilder {
        to(location.x.toDouble(), location.y.toDouble(), location.z.toDouble())
        facing(location.yaw, location.pitch)
        return this
    }

    fun to(target: PlayerRef): TeleportBuilder {
        val targetPlayer = target.player ?: return this
        val world = targetPlayer.world ?: return this
        world.execute {
            val store = world.entityStore?.store ?: return@execute
            val transform = store.getComponent(
                targetPlayer.reference,
                com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType()
            ) ?: return@execute
            to(transform.position)
            facing(transform.rotation.yaw, transform.rotation.pitch)
        }
        return this
    }

    fun facing(yaw: Float, pitch: Float = 0f): TeleportBuilder {
        this.yaw = yaw
        this.pitch = pitch
        return this
    }

    fun world(world: World): TeleportBuilder {
        this.targetWorld = world
        return this
    }

    fun keepVelocity(): TeleportBuilder {
        resetVelocity = false
        return this
    }

    fun resetRoll(): TeleportBuilder {
        resetRoll = true
        return this
    }

    fun execute() {
        val playerEntity = player.player ?: return
        val world = targetWorld ?: playerEntity.world ?: return

        world.execute {
            val store = world.entityStore?.store ?: return@execute

            var teleport = if (targetWorld != null) {
                Teleport(targetWorld, Vector3d(x, y, z), Vector3f(yaw, pitch, 0f))
            } else {
                Teleport(Vector3d(x, y, z), Vector3f(yaw, pitch, 0f))
            }

            if (!resetVelocity) {
                teleport = teleport.withoutVelocityReset()
            }
            if (resetRoll) {
                teleport = teleport.withResetRoll()
            }

            store.addComponent(playerEntity.reference, Teleport.getComponentType(), teleport)
        }
    }
}

fun PlayerRef.teleport(block: TeleportBuilder.() -> Unit) {
    TeleportBuilder(this).apply(block).execute()
}
