@file:JvmName("InteractionTypeUtils")

package com.github.ssquadteam.talelib.input

import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.protocol.MouseButtonType

object InteractionTypes {
    val STANDARD_INPUTS: Set<InteractionType> = setOf(
        InteractionType.Primary,
        InteractionType.Secondary,
        InteractionType.Ability1,
        InteractionType.Ability2,
        InteractionType.Ability3,
        InteractionType.Use,
        InteractionType.Pick,
        InteractionType.Dodge
    )

    val COMBAT_TYPES: Set<InteractionType> = setOf(
        InteractionType.Primary,
        InteractionType.Secondary,
        InteractionType.Ability1,
        InteractionType.Ability2,
        InteractionType.Ability3
    )

    val PROJECTILE_TYPES: Set<InteractionType> = setOf(
        InteractionType.ProjectileSpawn,
        InteractionType.ProjectileBounce,
        InteractionType.ProjectileMiss,
        InteractionType.ProjectileHit
    )

    val COLLISION_TYPES: Set<InteractionType> = setOf(
        InteractionType.Collision,
        InteractionType.CollisionEnter,
        InteractionType.CollisionLeave
    )

    val EQUIPMENT_TYPES: Set<InteractionType> = setOf(
        InteractionType.Held,
        InteractionType.HeldOffhand,
        InteractionType.Equipped,
        InteractionType.SwapFrom,
        InteractionType.SwapTo,
        InteractionType.Wielding
    )
}

val InteractionType.isStandardInput: Boolean
    get() = this in InteractionTypes.STANDARD_INPUTS

val InteractionType.isCombatInteraction: Boolean
    get() = this in InteractionTypes.COMBAT_TYPES

val InteractionType.isProjectileInteraction: Boolean
    get() = this in InteractionTypes.PROJECTILE_TYPES

val InteractionType.isCollisionInteraction: Boolean
    get() = this == InteractionType.Collision ||
            this == InteractionType.CollisionEnter ||
            this == InteractionType.CollisionLeave

val InteractionType.isEquipmentInteraction: Boolean
    get() = this in InteractionTypes.EQUIPMENT_TYPES

val InteractionType.defaultCooldown: Float
    get() = 0.35f

fun MouseButtonType.toInteractionType(): InteractionType = when (this) {
    MouseButtonType.Left -> InteractionType.Primary
    MouseButtonType.Right -> InteractionType.Secondary
    MouseButtonType.Middle -> InteractionType.Pick
    else -> InteractionType.Use
}

val InteractionType.displayName: String
    get() = when (this) {
        InteractionType.Primary -> "Primary Attack"
        InteractionType.Secondary -> "Secondary Attack"
        InteractionType.Ability1 -> "Ability 1"
        InteractionType.Ability2 -> "Ability 2"
        InteractionType.Ability3 -> "Ability 3"
        InteractionType.Use -> "Use/Interact"
        InteractionType.Pick -> "Pick"
        InteractionType.Dodge -> "Dodge"
        InteractionType.Pickup -> "Pickup Item"
        InteractionType.Wielding -> "Wielding"
        else -> this.name
    }
