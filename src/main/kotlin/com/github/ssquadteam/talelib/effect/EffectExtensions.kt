package com.github.ssquadteam.talelib.effect

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior
import com.hypixel.hytale.server.core.entity.effect.ActiveEntityEffect
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.RemovalBehavior
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/**
 * Extension functions for effect management.
 * Source: com/hypixel/hytale/server/core/entity/effect/EffectControllerComponent.java
 */

// ============================================
// Player Ref Effect Extensions
// ============================================

fun PlayerRef.applyEffect(block: EffectBuilder.() -> Unit): Boolean {
    return EffectBuilder().apply(block).applyTo(this)
}

fun PlayerRef.applyEffect(effectId: String, duration: Float? = null): Boolean {
    return applyEffect {
        effect(effectId)
        duration?.let { duration(it) }
    }
}

fun PlayerRef.applyInfiniteEffect(effectId: String): Boolean {
    return applyEffect {
        effect(effectId)
        infinite()
    }
}

fun PlayerRef.removeEffect(effectId: String, behavior: RemovalBehavior = RemovalBehavior.COMPLETE): Boolean {
    return removeEffect {
        effect(effectId)
        behavior(behavior)
    }.removeFrom(this)
}

fun PlayerRef.clearEffects(): Boolean {
    return removeEffect { all() }.removeFrom(this)
}

fun PlayerRef.hasEffect(effectId: String): Boolean {
    val ref = this.reference ?: return false
    if (!ref.isValid) return false

    val store = ref.store
    val controller = store.getComponent(ref, EffectControllerComponent.getComponentType())
        ?: return false

    val effectIndex = try {
        val index = EntityEffect.getAssetMap().getIndex(effectId)
        if (index == Int.MIN_VALUE) return false
        index
    } catch (e: Exception) {
        return false
    }

    return controller.activeEffects.containsKey(effectIndex)
}

fun PlayerRef.getEffectDuration(effectId: String): Float? {
    val ref = this.reference ?: return null
    if (!ref.isValid) return null

    val store = ref.store
    val controller = store.getComponent(ref, EffectControllerComponent.getComponentType())
        ?: return null

    val effectIndex = try {
        val index = EntityEffect.getAssetMap().getIndex(effectId)
        if (index == Int.MIN_VALUE) return null
        index
    } catch (e: Exception) {
        return null
    }

    val activeEffect = controller.activeEffects[effectIndex] ?: return null
    return if (activeEffect.isInfinite) Float.POSITIVE_INFINITY else activeEffect.remainingDuration
}

fun PlayerRef.getActiveEffects(): List<TaleActiveEffect> {
    val ref = this.reference ?: return emptyList()
    if (!ref.isValid) return emptyList()

    val store = ref.store
    val controller = store.getComponent(ref, EffectControllerComponent.getComponentType())
        ?: return emptyList()

    return controller.activeEffects.values.map { TaleActiveEffect(it) }
}

fun PlayerRef.getActiveEffectCount(): Int {
    val ref = this.reference ?: return 0
    if (!ref.isValid) return 0

    val store = ref.store
    val controller = store.getComponent(ref, EffectControllerComponent.getComponentType())
        ?: return 0

    return controller.activeEffects.size
}

fun PlayerRef.hasBuffs(): Boolean {
    return getActiveEffects().any { !it.isDebuff }
}

fun PlayerRef.hasDebuffs(): Boolean {
    return getActiveEffects().any { it.isDebuff }
}

fun PlayerRef.isInvulnerableFromEffects(): Boolean {
    val ref = this.reference ?: return false
    if (!ref.isValid) return false

    val store = ref.store
    val controller = store.getComponent(ref, EffectControllerComponent.getComponentType())
        ?: return false

    return controller.isInvulnerable
}

fun PlayerRef.clearDebuffs(): Int {
    val debuffs = getActiveEffects().filter { it.isDebuff }
    var removed = 0
    debuffs.forEach { effect ->
        val ref = this.reference ?: return removed
        val store = ref.store
        val controller = store.getComponent(ref, EffectControllerComponent.getComponentType())
            ?: return removed
        controller.removeEffect(ref, effect.effectIndex, RemovalBehavior.COMPLETE, store)
        removed++
    }
    return removed
}

fun PlayerRef.clearBuffs(): Int {
    val buffs = getActiveEffects().filter { !it.isDebuff }
    var removed = 0
    buffs.forEach { effect ->
        val ref = this.reference ?: return removed
        val store = ref.store
        val controller = store.getComponent(ref, EffectControllerComponent.getComponentType())
            ?: return removed
        controller.removeEffect(ref, effect.effectIndex, RemovalBehavior.COMPLETE, store)
        removed++
    }
    return removed
}

// ============================================
// Entity Ref Effect Extensions
// ============================================

fun Ref<EntityStore>.applyEffect(block: EffectBuilder.() -> Unit): Boolean {
    return EffectBuilder().apply(block).applyTo(this)
}

fun Ref<EntityStore>.applyEffect(effectId: String, duration: Float? = null): Boolean {
    return applyEffect {
        effect(effectId)
        duration?.let { duration(it) }
    }
}

fun Ref<EntityStore>.removeEffect(effectId: String, behavior: RemovalBehavior = RemovalBehavior.COMPLETE): Boolean {
    return removeEffect {
        effect(effectId)
        behavior(behavior)
    }.removeFrom(this)
}

fun Ref<EntityStore>.clearEffects(): Boolean {
    return removeEffect { all() }.removeFrom(this)
}

fun Ref<EntityStore>.hasEffect(effectId: String): Boolean {
    if (!this.isValid) return false

    val store = this.store
    val controller = store.getComponent(this, EffectControllerComponent.getComponentType())
        ?: return false

    val effectIndex = try {
        val index = EntityEffect.getAssetMap().getIndex(effectId)
        if (index == Int.MIN_VALUE) return false
        index
    } catch (e: Exception) {
        return false
    }

    return controller.activeEffects.containsKey(effectIndex)
}

fun getEffect(effectId: String): EntityEffect? {
    return try {
        EntityEffect.getAssetMap().getAsset(effectId)
    } catch (e: Exception) {
        null
    }
}

fun getEffectIndex(effectId: String): Int? {
    return try {
        val index = EntityEffect.getAssetMap().getIndex(effectId)
        if (index == Int.MIN_VALUE) null else index
    } catch (e: Exception) {
        null
    }
}

fun effectExists(effectId: String): Boolean {
    return getEffect(effectId) != null
}

@Suppress("UNCHECKED_CAST")
fun getAllEffectIds(): List<String> {
    return try {
        val map = EntityEffect.getAssetMap().getAssetMap() as Map<Any, Any>
        map.keys.map { it.toString() }
    } catch (e: Exception) {
        emptyList()
    }
}

fun formatEffectDuration(duration: Float?): String {
    return when {
        duration == null -> "Not Active"
        duration == Float.POSITIVE_INFINITY -> "Infinite"
        duration < 60 -> String.format("%.1fs", duration)
        duration < 3600 -> String.format("%.1fm", duration / 60)
        else -> String.format("%.1fh", duration / 3600)
    }
}
