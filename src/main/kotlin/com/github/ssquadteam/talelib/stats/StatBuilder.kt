package com.github.ssquadteam.talelib.stats

import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.component.Ref

/**
 * DSL builder for manipulating entity stats.
 * Source: com/hypixel/hytale/server/core/modules/entitystats/EntityStatMap.java
 */
class StatBuilder {
    private var statIndex: Int? = null
    private var statId: String? = null
    private var operation: StatOperation = StatOperation.SET
    private var value: Float = 0f
    private var predictable: EntityStatMap.Predictable = EntityStatMap.Predictable.SELF

    fun stat(index: Int): StatBuilder {
        this.statIndex = index
        this.statId = null
        return this
    }

    fun stat(id: String): StatBuilder {
        this.statId = id
        this.statIndex = null
        return this
    }

    fun health(): StatBuilder {
        this.statIndex = DefaultEntityStatTypes.getHealth()
        this.statId = null
        return this
    }

    fun mana(): StatBuilder {
        this.statIndex = DefaultEntityStatTypes.getMana()
        this.statId = null
        return this
    }

    fun stamina(): StatBuilder {
        this.statIndex = DefaultEntityStatTypes.getStamina()
        this.statId = null
        return this
    }

    fun oxygen(): StatBuilder {
        this.statIndex = DefaultEntityStatTypes.getOxygen()
        this.statId = null
        return this
    }

    fun signatureEnergy(): StatBuilder {
        this.statIndex = DefaultEntityStatTypes.getSignatureEnergy()
        this.statId = null
        return this
    }

    fun ammo(): StatBuilder {
        this.statIndex = DefaultEntityStatTypes.getAmmo()
        this.statId = null
        return this
    }

    fun set(value: Float): StatBuilder {
        this.operation = StatOperation.SET
        this.value = value
        return this
    }

    fun add(value: Float): StatBuilder {
        this.operation = StatOperation.ADD
        this.value = value
        return this
    }

    fun subtract(value: Float): StatBuilder {
        this.operation = StatOperation.SUBTRACT
        this.value = value
        return this
    }

    fun minimize(): StatBuilder {
        this.operation = StatOperation.MINIMIZE
        return this
    }

    fun maximize(): StatBuilder {
        this.operation = StatOperation.MAXIMIZE
        return this
    }

    fun reset(): StatBuilder {
        this.operation = StatOperation.RESET
        return this
    }

    fun predictable(predictable: EntityStatMap.Predictable): StatBuilder {
        this.predictable = predictable
        return this
    }

    fun predictSelf(): StatBuilder {
        this.predictable = EntityStatMap.Predictable.SELF
        return this
    }

    fun noPredict(): StatBuilder {
        this.predictable = EntityStatMap.Predictable.NONE
        return this
    }

    fun applyTo(ref: Ref<EntityStore>): Float? {
        if (!ref.isValid) return null

        val store = ref.store
        val statMap = store.getComponent(ref, EntityStatMap.getComponentType()) ?: return null

        val index = resolveStatIndex() ?: return null

        return when (operation) {
            StatOperation.SET -> {
                statMap.setStatValue(predictable, index, value)
                value
            }
            StatOperation.ADD -> statMap.addStatValue(predictable, index, value)
            StatOperation.SUBTRACT -> statMap.subtractStatValue(predictable, index, value)
            StatOperation.MINIMIZE -> {
                statMap.minimizeStatValue(predictable, index)
                statMap.get(index)?.min
            }
            StatOperation.MAXIMIZE -> {
                statMap.maximizeStatValue(predictable, index)
                statMap.get(index)?.max
            }
            StatOperation.RESET -> {
                statMap.resetStatValue(predictable, index)
                statMap.get(index)?.get()
            }
        }
    }

    fun applyTo(player: PlayerRef): Float? {
        val ref = player.reference ?: return null
        return applyTo(ref)
    }

    private fun resolveStatIndex(): Int? {
        return statIndex ?: statId?.let {
            try {
                val index = EntityStatType.getAssetMap().getIndex(it)
                if (index == Int.MIN_VALUE) null else index
            } catch (e: Exception) {
                null
            }
        }
    }

    private enum class StatOperation {
        SET, ADD, SUBTRACT, MINIMIZE, MAXIMIZE, RESET
    }
}

fun modifyStat(block: StatBuilder.() -> Unit): StatBuilder {
    return StatBuilder().apply(block)
}

class StatModifierBuilder {
    private var statIndex: Int? = null
    private var statId: String? = null
    private var modifierKey: String = ""
    private var target: Modifier.ModifierTarget = Modifier.ModifierTarget.MAX
    private var calculationType: StaticModifier.CalculationType = StaticModifier.CalculationType.ADDITIVE
    private var amount: Float = 0f
    private var predictable: EntityStatMap.Predictable = EntityStatMap.Predictable.SELF

    fun stat(index: Int): StatModifierBuilder {
        this.statIndex = index
        this.statId = null
        return this
    }

    fun stat(id: String): StatModifierBuilder {
        this.statId = id
        this.statIndex = null
        return this
    }

    fun health(): StatModifierBuilder {
        this.statIndex = DefaultEntityStatTypes.getHealth()
        return this
    }

    fun mana(): StatModifierBuilder {
        this.statIndex = DefaultEntityStatTypes.getMana()
        return this
    }

    fun stamina(): StatModifierBuilder {
        this.statIndex = DefaultEntityStatTypes.getStamina()
        return this
    }

    fun key(key: String): StatModifierBuilder {
        this.modifierKey = key
        return this
    }

    fun modifyMax(): StatModifierBuilder {
        this.target = Modifier.ModifierTarget.MAX
        return this
    }

    fun modifyMin(): StatModifierBuilder {
        this.target = Modifier.ModifierTarget.MIN
        return this
    }

    fun additive(amount: Float): StatModifierBuilder {
        this.calculationType = StaticModifier.CalculationType.ADDITIVE
        this.amount = amount
        return this
    }

    fun multiplicative(amount: Float): StatModifierBuilder {
        this.calculationType = StaticModifier.CalculationType.MULTIPLICATIVE
        this.amount = amount
        return this
    }

    fun percentIncrease(percent: Float): StatModifierBuilder {
        this.calculationType = StaticModifier.CalculationType.MULTIPLICATIVE
        this.amount = 1f + percent
        return this
    }

    fun percentDecrease(percent: Float): StatModifierBuilder {
        this.calculationType = StaticModifier.CalculationType.MULTIPLICATIVE
        this.amount = 1f - percent
        return this
    }

    fun applyTo(ref: Ref<EntityStore>): Boolean {
        if (!ref.isValid || modifierKey.isEmpty()) return false

        val store = ref.store
        val statMap = store.getComponent(ref, EntityStatMap.getComponentType()) ?: return false

        val index = resolveStatIndex() ?: return false
        val modifier = StaticModifier(target, calculationType, amount)

        statMap.putModifier(predictable, index, modifierKey, modifier)
        return true
    }

    fun applyTo(player: PlayerRef): Boolean {
        val ref = player.reference ?: return false
        return applyTo(ref)
    }

    private fun resolveStatIndex(): Int? {
        return statIndex ?: statId?.let {
            try {
                val index = EntityStatType.getAssetMap().getIndex(it)
                if (index == Int.MIN_VALUE) null else index
            } catch (e: Exception) {
                null
            }
        }
    }
}

fun statModifier(block: StatModifierBuilder.() -> Unit): StatModifierBuilder {
    return StatModifierBuilder().apply(block)
}
