package com.github.ssquadteam.talelib.stats

import com.hypixel.hytale.server.core.entity.EntityStore
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.ecs.Ref

/**
 * Extension functions for entity stats management.
 * Source: com/hypixel/hytale/server/core/modules/entitystats/EntityStatMap.java
 */

// ============================================
// Player Stat Extensions
// ============================================

fun PlayerRef.getStatMap(): EntityStatMap? {
    val ref = this.ref ?: return null
    if (!ref.isValid) return null
    return ref.store.getComponent(ref, EntityStatMap.getComponentType())
}

fun PlayerRef.getStat(statIndex: Int): EntityStatValue? {
    return getStatMap()?.get(statIndex)
}

fun PlayerRef.getStat(statId: String): EntityStatValue? {
    val index = getStatIndex(statId) ?: return null
    return getStat(index)
}

fun PlayerRef.modifyStat(block: StatBuilder.() -> Unit): Float? {
    return StatBuilder().apply(block).applyTo(this)
}

fun PlayerRef.addStatModifier(block: StatModifierBuilder.() -> Unit): Boolean {
    return StatModifierBuilder().apply(block).applyTo(this)
}

fun PlayerRef.removeStatModifier(statIndex: Int, modifierKey: String): Modifier? {
    val statMap = getStatMap() ?: return null
    return statMap.removeModifier(EntityStatMap.Predictable.SELF, statIndex, modifierKey)
}

fun PlayerRef.removeStatModifier(statId: String, modifierKey: String): Modifier? {
    val index = getStatIndex(statId) ?: return null
    return removeStatModifier(index, modifierKey)
}

// ============================================
// Health Extensions
// ============================================

fun PlayerRef.getHealth(): Float? {
    return getStat(DefaultEntityStatTypes.getHealth())?.get()
}

fun PlayerRef.getHealthPercent(): Float? {
    return getStat(DefaultEntityStatTypes.getHealth())?.asPercentage()
}

fun PlayerRef.getMaxHealth(): Float? {
    return getStat(DefaultEntityStatTypes.getHealth())?.max
}

fun PlayerRef.setHealth(value: Float): Float? {
    return modifyStat {
        health()
        set(value)
    }
}

fun PlayerRef.heal(amount: Float): Float? {
    return modifyStat {
        health()
        add(amount)
    }
}

fun PlayerRef.fullHeal(): Float? {
    return modifyStat {
        health()
        maximize()
    }
}

fun PlayerRef.isFullHealth(): Boolean {
    val stat = getStat(DefaultEntityStatTypes.getHealth()) ?: return false
    return stat.get() >= stat.max
}

fun PlayerRef.isLowHealth(threshold: Float = 0.25f): Boolean {
    val percent = getHealthPercent() ?: return false
    return percent <= threshold
}

// ============================================
// Mana Extensions
// ============================================

fun PlayerRef.getMana(): Float? {
    return getStat(DefaultEntityStatTypes.getMana())?.get()
}

fun PlayerRef.getManaPercent(): Float? {
    return getStat(DefaultEntityStatTypes.getMana())?.asPercentage()
}

fun PlayerRef.getMaxMana(): Float? {
    return getStat(DefaultEntityStatTypes.getMana())?.max
}

fun PlayerRef.setMana(value: Float): Float? {
    return modifyStat {
        mana()
        set(value)
    }
}

fun PlayerRef.addMana(amount: Float): Float? {
    return modifyStat {
        mana()
        add(amount)
    }
}

fun PlayerRef.useMana(amount: Float): Float? {
    return modifyStat {
        mana()
        subtract(amount)
    }
}

fun PlayerRef.fullMana(): Float? {
    return modifyStat {
        mana()
        maximize()
    }
}

fun PlayerRef.hasMana(amount: Float): Boolean {
    val mana = getMana() ?: return false
    return mana >= amount
}

// ============================================
// Stamina Extensions
// ============================================

fun PlayerRef.getStamina(): Float? {
    return getStat(DefaultEntityStatTypes.getStamina())?.get()
}

fun PlayerRef.getStaminaPercent(): Float? {
    return getStat(DefaultEntityStatTypes.getStamina())?.asPercentage()
}

fun PlayerRef.getMaxStamina(): Float? {
    return getStat(DefaultEntityStatTypes.getStamina())?.max
}

fun PlayerRef.setStamina(value: Float): Float? {
    return modifyStat {
        stamina()
        set(value)
    }
}

fun PlayerRef.addStamina(amount: Float): Float? {
    return modifyStat {
        stamina()
        add(amount)
    }
}

fun PlayerRef.useStamina(amount: Float): Float? {
    return modifyStat {
        stamina()
        subtract(amount)
    }
}

fun PlayerRef.fullStamina(): Float? {
    return modifyStat {
        stamina()
        maximize()
    }
}

fun PlayerRef.hasStamina(amount: Float): Boolean {
    val stamina = getStamina() ?: return false
    return stamina >= amount
}

// ============================================
// Oxygen Extensions
// ============================================

fun PlayerRef.getOxygen(): Float? {
    return getStat(DefaultEntityStatTypes.getOxygen())?.get()
}

fun PlayerRef.getOxygenPercent(): Float? {
    return getStat(DefaultEntityStatTypes.getOxygen())?.asPercentage()
}

fun PlayerRef.setOxygen(value: Float): Float? {
    return modifyStat {
        oxygen()
        set(value)
    }
}

fun PlayerRef.fullOxygen(): Float? {
    return modifyStat {
        oxygen()
        maximize()
    }
}

// ============================================
// Entity Ref Stat Extensions
// ============================================

fun Ref<EntityStore>.getStatMap(): EntityStatMap? {
    if (!this.isValid) return null
    return this.store.getComponent(this, EntityStatMap.getComponentType())
}

fun Ref<EntityStore>.getStat(statIndex: Int): EntityStatValue? {
    return getStatMap()?.get(statIndex)
}

fun Ref<EntityStore>.modifyStat(block: StatBuilder.() -> Unit): Float? {
    return StatBuilder().apply(block).applyTo(this)
}

fun Ref<EntityStore>.getHealth(): Float? {
    return getStat(DefaultEntityStatTypes.getHealth())?.get()
}

fun Ref<EntityStore>.getMaxHealth(): Float? {
    return getStat(DefaultEntityStatTypes.getHealth())?.max
}

fun Ref<EntityStore>.getHealthPercent(): Float? {
    return getStat(DefaultEntityStatTypes.getHealth())?.asPercentage()
}

// ============================================
// Utility Functions
// ============================================

fun getStatIndex(statId: String): Int? {
    return try {
        val index = EntityStatType.getAssetMap().getIndex(statId)
        if (index == Int.MIN_VALUE) null else index
    } catch (e: Exception) {
        null
    }
}

fun getStatType(statId: String): EntityStatType? {
    return try {
        EntityStatType.getAssetMap().getAsset(statId)
    } catch (e: Exception) {
        null
    }
}

fun statExists(statId: String): Boolean {
    return getStatType(statId) != null
}

fun getAllStatIds(): List<String> {
    return try {
        EntityStatType.getAssetMap().keys.toList()
    } catch (e: Exception) {
        emptyList()
    }
}

object Stats {
    val HEALTH: Int get() = DefaultEntityStatTypes.getHealth()
    val MANA: Int get() = DefaultEntityStatTypes.getMana()
    val STAMINA: Int get() = DefaultEntityStatTypes.getStamina()
    val OXYGEN: Int get() = DefaultEntityStatTypes.getOxygen()
    val SIGNATURE_ENERGY: Int get() = DefaultEntityStatTypes.getSignatureEnergy()
    val AMMO: Int get() = DefaultEntityStatTypes.getAmmo()
}
