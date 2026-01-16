package com.github.ssquadteam.talelib.effect

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent
import com.hypixel.hytale.server.core.entity.effect.RemovalBehavior
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/**
 * DSL builder for applying effects to entities.
 * Source: com/hypixel/hytale/server/core/entity/effect/EffectControllerComponent.java
 */
class EffectBuilder {
    private var effectId: String? = null
    private var entityEffect: EntityEffect? = null
    private var duration: Float? = null
    private var infinite: Boolean = false
    private var overlapBehavior: OverlapBehavior = OverlapBehavior.OVERWRITE

    fun effect(effectId: String): EffectBuilder {
        this.effectId = effectId
        this.entityEffect = null
        return this
    }

    fun effect(effect: EntityEffect): EffectBuilder {
        this.entityEffect = effect
        this.effectId = null
        return this
    }

    fun duration(seconds: Float): EffectBuilder {
        this.duration = seconds
        this.infinite = false
        return this
    }

    fun durationTicks(ticks: Int): EffectBuilder {
        this.duration = ticks / 20f
        this.infinite = false
        return this
    }

    fun infinite(): EffectBuilder {
        this.infinite = true
        return this
    }

    fun permanent() = infinite()

    /**
     * Set the overlap behavior when effect is already active.
     * EXTEND: Add duration to existing
     * OVERWRITE: Replace existing effect
     * IGNORE: Keep existing, ignore new
     */
    fun overlap(behavior: OverlapBehavior): EffectBuilder {
        this.overlapBehavior = behavior
        return this
    }

    fun extend(): EffectBuilder {
        this.overlapBehavior = OverlapBehavior.EXTEND
        return this
    }

    fun overwrite(): EffectBuilder {
        this.overlapBehavior = OverlapBehavior.OVERWRITE
        return this
    }

    fun ignore(): EffectBuilder {
        this.overlapBehavior = OverlapBehavior.IGNORE
        return this
    }

    fun applyTo(ref: Ref<EntityStore>): Boolean {
        if (!ref.isValid) return false

        val store = ref.store
        val effect = resolveEffect() ?: return false

        val controller = store.getComponent(ref, EffectControllerComponent.getComponentType())
            ?: return false

        return when {
            infinite -> {
                val effectIndex = getEffectIndex() ?: return false
                controller.addInfiniteEffect(ref, effectIndex, effect, store)
            }
            duration != null -> {
                controller.addEffect(ref, effect, duration!!, overlapBehavior, store)
            }
            else -> {
                controller.addEffect(ref, effect, store)
            }
        }
    }

    fun applyTo(player: PlayerRef): Boolean {
        val ref = player.reference ?: return false
        return applyTo(ref)
    }

    private fun resolveEffect(): EntityEffect? {
        return entityEffect ?: effectId?.let {
            try {
                EntityEffect.getAssetMap().getAsset(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun getEffectIndex(): Int? {
        val id = effectId ?: entityEffect?.id ?: return null
        return try {
            val index = EntityEffect.getAssetMap().getIndex(id)
            if (index == Int.MIN_VALUE) null else index
        } catch (e: Exception) {
            null
        }
    }
}

fun applyEffect(block: EffectBuilder.() -> Unit): EffectBuilder {
    return EffectBuilder().apply(block)
}

class EffectRemovalBuilder {
    private var effectId: String? = null
    private var effectIndex: Int? = null
    private var removalBehavior: RemovalBehavior = RemovalBehavior.COMPLETE
    private var clearAll: Boolean = false

    fun effect(effectId: String): EffectRemovalBuilder {
        this.effectId = effectId
        this.effectIndex = null
        this.clearAll = false
        return this
    }

    fun effectIndex(index: Int): EffectRemovalBuilder {
        this.effectIndex = index
        this.effectId = null
        this.clearAll = false
        return this
    }

    fun all(): EffectRemovalBuilder {
        this.clearAll = true
        return this
    }

    /**
     * Set removal behavior.
     * COMPLETE: Fully remove effect
     * INFINITE: Only remove infinite flag
     * DURATION: Set remaining duration to zero
     */
    fun behavior(behavior: RemovalBehavior): EffectRemovalBuilder {
        this.removalBehavior = behavior
        return this
    }

    fun complete(): EffectRemovalBuilder {
        this.removalBehavior = RemovalBehavior.COMPLETE
        return this
    }

    fun removeFrom(ref: Ref<EntityStore>): Boolean {
        if (!ref.isValid) return false

        val store = ref.store
        val controller = store.getComponent(ref, EffectControllerComponent.getComponentType())
            ?: return false

        if (clearAll) {
            controller.clearEffects(ref, store)
            return true
        }

        val index = effectIndex ?: effectId?.let {
            try {
                val i = EntityEffect.getAssetMap().getIndex(it)
                if (i == Int.MIN_VALUE) null else i
            } catch (e: Exception) {
                null
            }
        } ?: return false

        controller.removeEffect(ref, index, removalBehavior, store)
        return true
    }

    fun removeFrom(player: PlayerRef): Boolean {
        val ref = player.reference ?: return false
        return removeFrom(ref)
    }
}

fun removeEffect(block: EffectRemovalBuilder.() -> Unit): EffectRemovalBuilder {
    return EffectRemovalBuilder().apply(block)
}
