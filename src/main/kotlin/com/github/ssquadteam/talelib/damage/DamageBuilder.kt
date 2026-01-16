package com.github.ssquadteam.talelib.damage

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.math.vector.Vector4d
import com.hypixel.hytale.server.core.modules.entity.damage.Damage
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/**
 * DSL builder for creating and applying damage.
 * Source: com/hypixel/hytale/server/core/modules/entity/damage/Damage.java
 */
class DamageBuilder {
    private var source: Damage.Source = Damage.NULL_SOURCE
    private var cause: DamageCause? = null
    private var causeId: String? = null
    private var amount: Float = 0f
    private var knockback: KnockbackBuilder? = null
    private var hitLocation: Vector4d? = null
    private var hitAngle: Float? = null
    private var impactSoundId: String? = null
    private var playerImpactSoundId: String? = null
    private var blocked: Boolean = false
    private var staminaDrainMultiplier: Float = 1f
    private var canBePredicted: Boolean = true

    fun amount(amount: Float): DamageBuilder {
        this.amount = amount
        return this
    }

    fun damage(amount: Float) = amount(amount)

    fun cause(cause: DamageCause): DamageBuilder {
        this.cause = cause
        this.causeId = null
        return this
    }

    fun cause(causeId: String): DamageBuilder {
        this.causeId = causeId
        this.cause = null
        return this
    }

    fun commandCause(): DamageBuilder {
        this.cause = DamageCause.COMMAND
        this.causeId = null
        return this
    }

    fun fallCause(): DamageBuilder {
        this.cause = DamageCause.FALL
        this.causeId = null
        return this
    }

    fun drowningCause(): DamageBuilder {
        this.cause = DamageCause.DROWNING
        this.causeId = null
        return this
    }

    fun suffocationCause(): DamageBuilder {
        this.cause = DamageCause.SUFFOCATION
        this.causeId = null
        return this
    }

    fun nullSource(): DamageBuilder {
        this.source = Damage.NULL_SOURCE
        return this
    }

    fun fromEntity(entityRef: Ref<EntityStore>): DamageBuilder {
        this.source = Damage.EntitySource(entityRef)
        return this
    }

    fun fromPlayer(player: PlayerRef): DamageBuilder {
        val ref = player.reference ?: return this
        this.source = Damage.EntitySource(ref)
        return this
    }

    fun fromProjectile(projectileRef: Ref<EntityStore>, shooterRef: Ref<EntityStore>): DamageBuilder {
        this.source = Damage.ProjectileSource(shooterRef, projectileRef)
        return this
    }

    fun fromEnvironment(type: String = "environment"): DamageBuilder {
        this.source = Damage.EnvironmentSource(type)
        return this
    }

    fun knockback(block: KnockbackBuilder.() -> Unit): DamageBuilder {
        this.knockback = KnockbackBuilder().apply(block)
        return this
    }

    fun knockback(x: Double, y: Double, z: Double): DamageBuilder {
        this.knockback = KnockbackBuilder().velocity(x, y, z)
        return this
    }

    fun hitLocation(x: Double, y: Double, z: Double, w: Double = 1.0): DamageBuilder {
        this.hitLocation = Vector4d(x, y, z, w)
        return this
    }

    fun hitAngle(angle: Float): DamageBuilder {
        this.hitAngle = angle
        return this
    }

    fun impactSound(soundId: String): DamageBuilder {
        this.impactSoundId = soundId
        return this
    }

    fun playerImpactSound(soundId: String): DamageBuilder {
        this.playerImpactSoundId = soundId
        return this
    }

    fun blocked(blocked: Boolean = true): DamageBuilder {
        this.blocked = blocked
        return this
    }

    fun staminaDrain(multiplier: Float): DamageBuilder {
        this.staminaDrainMultiplier = multiplier
        return this
    }

    fun canBePredicted(canPredict: Boolean): DamageBuilder {
        this.canBePredicted = canPredict
        return this
    }

    fun build(): Damage {
        val defaultCause = DamageCause.COMMAND
            ?: DamageCause.getAssetMap().getAsset("Command")
            ?: throw IllegalStateException("DamageCause.COMMAND not initialized")

        val damageCause = when {
            cause != null -> cause!!
            causeId != null -> DamageCause.getAssetMap().getAsset(causeId) ?: defaultCause
            else -> defaultCause
        }

        val damage = Damage(source, damageCause, amount)

        hitLocation?.let { damage.putMetaObject(Damage.HIT_LOCATION, it) }
        hitAngle?.let { damage.putMetaObject(Damage.HIT_ANGLE, it) }

        if (impactSoundId != null) {
            val soundIndex = com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent.getAssetMap().getIndex(impactSoundId)
            if (soundIndex >= 0) {
                damage.putMetaObject(Damage.IMPACT_SOUND_EFFECT, Damage.SoundEffect(soundIndex))
            }
        }

        if (playerImpactSoundId != null) {
            val soundIndex = com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent.getAssetMap().getIndex(playerImpactSoundId)
            if (soundIndex >= 0) {
                damage.putMetaObject(Damage.PLAYER_IMPACT_SOUND_EFFECT, Damage.SoundEffect(soundIndex))
            }
        }

        if (blocked) {
            damage.putMetaObject(Damage.BLOCKED, true)
        }

        damage.putMetaObject(Damage.STAMINA_DRAIN_MULTIPLIER, staminaDrainMultiplier)
        damage.putMetaObject(Damage.CAN_BE_PREDICTED, canBePredicted)

        knockback?.build()?.let {
            damage.putMetaObject(Damage.KNOCKBACK_COMPONENT, it)
        }

        return damage
    }

    fun execute(targetRef: Ref<EntityStore>, store: Store<EntityStore>) {
        val damage = build()
        DamageSystems.executeDamage(targetRef, store, damage)
    }

    fun execute(targetRef: Ref<EntityStore>, world: World) {
        val damage = build()
        DamageSystems.executeDamage(targetRef, world.entityStore.store, damage)
    }
}

fun damage(block: DamageBuilder.() -> Unit): Damage {
    return DamageBuilder().apply(block).build()
}

fun damageBuilder(block: DamageBuilder.() -> Unit): DamageBuilder {
    return DamageBuilder().apply(block)
}
