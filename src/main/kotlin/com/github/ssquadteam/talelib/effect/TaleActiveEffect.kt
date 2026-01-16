package com.github.ssquadteam.talelib.effect

import com.hypixel.hytale.server.core.entity.effect.ActiveEntityEffect

/**
 * Wrapper class for active entity effects.
 * Source: com/hypixel/hytale/server/core/entity/effect/ActiveEntityEffect.java
 */
class TaleActiveEffect(private val activeEffect: ActiveEntityEffect) {

    val effectIndex: Int
        get() = activeEffect.entityEffectIndex

    val initialDuration: Float
        get() = activeEffect.initialDuration

    val remainingDuration: Float
        get() = if (activeEffect.isInfinite) Float.POSITIVE_INFINITY else activeEffect.remainingDuration

    val isInfinite: Boolean
        get() = activeEffect.isInfinite

    val isDebuff: Boolean
        get() = activeEffect.isDebuff

    val isBuff: Boolean
        get() = !activeEffect.isDebuff

    val isInvulnerable: Boolean
        get() = activeEffect.isInvulnerable

    val progress: Float
        get() = when {
            isInfinite -> 1.0f
            initialDuration <= 0 -> 0.0f
            else -> (remainingDuration / initialDuration).coerceIn(0.0f, 1.0f)
        }

    val elapsedTime: Float
        get() = if (isInfinite) 0f else (initialDuration - remainingDuration).coerceAtLeast(0f)

    fun isExpiringSoon(thresholdSeconds: Float = 5f): Boolean {
        return !isInfinite && remainingDuration <= thresholdSeconds
    }

    fun hasMoreThan(seconds: Float): Boolean {
        return isInfinite || remainingDuration > seconds
    }

    fun formatDuration(): String {
        return formatEffectDuration(remainingDuration)
    }

    override fun toString(): String {
        val type = if (isDebuff) "Debuff" else "Buff"
        val duration = formatDuration()
        return "TaleActiveEffect[index=$effectIndex, $type, Duration: $duration]"
    }

    fun unwrap(): ActiveEntityEffect = activeEffect
}

fun ActiveEntityEffect.toTaleEffect(): TaleActiveEffect {
    return TaleActiveEffect(this)
}
