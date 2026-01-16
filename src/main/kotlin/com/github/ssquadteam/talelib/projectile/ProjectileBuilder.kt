package com.github.ssquadteam.talelib.projectile

import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.util.UUID

/**
 * DSL builder for spawning projectiles.
 * Source: com/hypixel/hytale/server/core/modules/projectile/ProjectileModule.java
 */
class ProjectileBuilder {
    private var configId: String? = null
    private var config: ProjectileConfig? = null
    private var position: Vector3d? = null
    private var direction: Vector3d? = null
    private var creatorRef: Ref<EntityStore>? = null
    private var predictionId: UUID? = null
    private var velocityMultiplier: Double = 1.0

    fun config(configId: String): ProjectileBuilder {
        this.configId = configId
        this.config = null
        return this
    }

    fun config(config: ProjectileConfig): ProjectileBuilder {
        this.config = config
        this.configId = null
        return this
    }

    fun position(x: Double, y: Double, z: Double): ProjectileBuilder {
        this.position = Vector3d(x, y, z)
        return this
    }

    fun position(pos: Vector3d): ProjectileBuilder {
        this.position = Vector3d(pos)
        return this
    }

    fun direction(x: Double, y: Double, z: Double): ProjectileBuilder {
        this.direction = Vector3d(x, y, z).normalize()
        return this
    }

    fun direction(dir: Vector3d): ProjectileBuilder {
        this.direction = Vector3d(dir).normalize()
        return this
    }

    fun directionFromAngles(yaw: Float, pitch: Float): ProjectileBuilder {
        val yawRad = Math.toRadians(yaw.toDouble())
        val pitchRad = Math.toRadians(pitch.toDouble())

        val x = -kotlin.math.sin(yawRad) * kotlin.math.cos(pitchRad)
        val y = -kotlin.math.sin(pitchRad)
        val z = kotlin.math.cos(yawRad) * kotlin.math.cos(pitchRad)

        this.direction = Vector3d(x, y, z).normalize()
        return this
    }

    fun creator(ref: Ref<EntityStore>): ProjectileBuilder {
        this.creatorRef = ref
        return this
    }

    fun creator(player: PlayerRef): ProjectileBuilder {
        this.creatorRef = player.reference
        return this
    }

    fun prediction(uuid: UUID): ProjectileBuilder {
        this.predictionId = uuid
        return this
    }

    fun withPrediction(): ProjectileBuilder {
        this.predictionId = UUID.randomUUID()
        return this
    }

    fun velocityMultiplier(multiplier: Double): ProjectileBuilder {
        this.velocityMultiplier = multiplier
        return this
    }

    fun fastProjectile(): ProjectileBuilder {
        this.velocityMultiplier = 2.0
        return this
    }

    fun slowProjectile(): ProjectileBuilder {
        this.velocityMultiplier = 0.5
        return this
    }

    /**
     * Spawns the projectile using the provided CommandBuffer.
     * Must be called from within a system context where a CommandBuffer is available.
     */
    fun spawn(commandBuffer: CommandBuffer<EntityStore>): Ref<EntityStore>? {
        val projectileConfig = resolveConfig() ?: return null
        val pos = position ?: return null
        val dir = direction ?: return null
        val creator = creatorRef ?: return null

        val scaledDirection = Vector3d(dir).scale(velocityMultiplier)

        return try {
            if (predictionId != null) {
                ProjectileModule.get().spawnProjectile(
                    predictionId,
                    creator,
                    commandBuffer,
                    projectileConfig,
                    pos,
                    scaledDirection
                )
            } else {
                ProjectileModule.get().spawnProjectile(
                    creator,
                    commandBuffer,
                    projectileConfig,
                    pos,
                    scaledDirection
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun resolveConfig(): ProjectileConfig? {
        return config ?: configId?.let {
            try {
                ProjectileConfig.getAssetMap().getAsset(it)
            } catch (e: Exception) {
                null
            }
        }
    }
}

fun projectile(block: ProjectileBuilder.() -> Unit): ProjectileBuilder {
    return ProjectileBuilder().apply(block)
}

/**
 * Spawns a projectile using the provided CommandBuffer.
 * Must be called from within a system context where a CommandBuffer is available.
 */
fun CommandBuffer<EntityStore>.spawnProjectile(block: ProjectileBuilder.() -> Unit): Ref<EntityStore>? {
    return ProjectileBuilder().apply(block).spawn(this)
}
