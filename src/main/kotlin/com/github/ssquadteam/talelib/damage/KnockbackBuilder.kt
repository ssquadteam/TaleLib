package com.github.ssquadteam.talelib.damage

import com.hypixel.hytale.protocol.ChangeVelocityType
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig
import com.hypixel.hytale.math.vector.Vector3d

/**
 * DSL builder for creating knockback configurations.
 * Source: com/hypixel/hytale/server/core/modules/entity/knockback/KnockbackComponent.java
 */
class KnockbackBuilder {
    private var velocity: Vector3d = Vector3d(0.0, 0.0, 0.0)
    private var velocityType: ChangeVelocityType = ChangeVelocityType.Add
    private var velocityConfig: VelocityConfig? = null
    private var duration: Float = 0f
    private val modifiers: MutableList<Double> = mutableListOf()

    fun velocity(x: Double, y: Double, z: Double): KnockbackBuilder {
        this.velocity = Vector3d(x, y, z)
        return this
    }
    fun velocity(velocity: Vector3d): KnockbackBuilder {
        this.velocity = Vector3d(velocity)
        return this
    }

    fun addVelocity(): KnockbackBuilder {
        this.velocityType = ChangeVelocityType.Add
        return this
    }

    fun setVelocity(): KnockbackBuilder {
        this.velocityType = ChangeVelocityType.Set
        return this
    }

    fun velocityType(type: ChangeVelocityType): KnockbackBuilder {
        this.velocityType = type
        return this
    }

    fun velocityConfig(config: VelocityConfig): KnockbackBuilder {
        this.velocityConfig = config
        return this
    }

    fun duration(duration: Float): KnockbackBuilder {
        this.duration = duration
        return this
    }

    fun addModifier(modifier: Double): KnockbackBuilder {
        this.modifiers.add(modifier)
        return this
    }

    fun verticalKnockback(y: Double): KnockbackBuilder {
        this.velocity = Vector3d(velocity.x, y, velocity.z)
        return this
    }

    fun horizontalKnockback(x: Double, z: Double): KnockbackBuilder {
        this.velocity = Vector3d(x, velocity.y, z)
        return this
    }

    fun awayFrom(sourceX: Double, sourceZ: Double, targetX: Double, targetZ: Double, force: Double, verticalForce: Double = 0.3): KnockbackBuilder {
        val dx = targetX - sourceX
        val dz = targetZ - sourceZ
        val distance = kotlin.math.sqrt(dx * dx + dz * dz)

        if (distance > 0) {
            val normalizedX = dx / distance
            val normalizedZ = dz / distance
            this.velocity = Vector3d(normalizedX * force, verticalForce, normalizedZ * force)
        } else {
            this.velocity = Vector3d(0.0, verticalForce, 0.0)
        }
        return this
    }

    fun inDirection(yaw: Float, force: Double, verticalForce: Double = 0.3): KnockbackBuilder {
        val radians = Math.toRadians(yaw.toDouble())
        val x = -kotlin.math.sin(radians) * force
        val z = kotlin.math.cos(radians) * force
        this.velocity = Vector3d(x, verticalForce, z)
        return this
    }

    fun build(): KnockbackComponent {
        val knockback = KnockbackComponent()
        knockback.velocity = velocity
        knockback.velocityType = velocityType
        knockback.duration = duration

        velocityConfig?.let { knockback.velocityConfig = it }

        modifiers.forEach { knockback.addModifier(it) }

        return knockback
    }
}

fun knockback(block: KnockbackBuilder.() -> Unit): KnockbackComponent {
    return KnockbackBuilder().apply(block).build()
}

object KnockbackPresets {

    fun light() = KnockbackBuilder()
        .velocity(0.0, 0.2, 0.0)
        .addVelocity()
        .duration(0f)

    fun medium() = KnockbackBuilder()
        .velocity(0.0, 0.4, 0.0)
        .addVelocity()
        .duration(0f)

    fun heavy() = KnockbackBuilder()
        .velocity(0.0, 0.6, 0.0)
        .addVelocity()
        .duration(0f)

    fun launch() = KnockbackBuilder()
        .velocity(0.0, 1.2, 0.0)
        .addVelocity()
        .duration(0f)

    fun explosive(force: Double = 1.5) = KnockbackBuilder()
        .velocity(0.0, force * 0.5, 0.0)
        .addVelocity()
        .duration(0f)
}
