package com.github.ssquadteam.talelib.entity

import com.github.ssquadteam.talelib.damage.damage
import com.github.ssquadteam.talelib.damage.getDamageCause
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.protocol.AnimationSlot
import com.hypixel.hytale.protocol.packets.entities.PlayAnimation
import com.hypixel.hytale.server.core.asset.type.model.config.Model
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId
import com.hypixel.hytale.server.core.universe.world.PlayerUtil
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a spawned entity in the world.
 *
 * Entities are created with a model from ModelAsset and include components for
 * transform (position/rotation), model rendering, bounding box, and optionally
 * interactions.
 */
class SpawnedEntity internal constructor(
    val id: UUID,
    val entityRef: Ref<EntityStore>,
    private val world: World,
    val modelAssetId: String
) {
    /**
     * Gets the entity's current position.
     */
    val position: Vector3d?
        get() {
            val entityStore = world.entityStore ?: return null
            return entityStore.store.getComponent(entityRef, TransformComponent.getComponentType())?.position
        }

    /**
     * Gets the entity's current rotation.
     */
    val rotation: Vector3f?
        get() {
            val entityStore = world.entityStore ?: return null
            return entityStore.store.getComponent(entityRef, TransformComponent.getComponentType())?.rotation
        }

    /**
     * Sets the entity's position.
     */
    fun setPosition(x: Double, y: Double, z: Double) {
        world.execute {
            val entityStore = world.entityStore ?: return@execute
            entityStore.store.getComponent(entityRef, TransformComponent.getComponentType())?.position?.assign(x, y, z)
        }
    }

    /**
     * Sets the entity's position.
     */
    fun setPosition(position: Vector3d) {
        setPosition(position.x, position.y, position.z)
    }

    /**
     * Sets the entity's rotation (yaw, pitch, roll).
     */
    fun setRotation(yaw: Float, pitch: Float, roll: Float = 0f) {
        world.execute {
            val entityStore = world.entityStore ?: return@execute
            entityStore.store.getComponent(entityRef, TransformComponent.getComponentType())?.rotation?.assign(yaw, pitch, roll)
        }
    }

    /**
     * Sets the entity's rotation.
     */
    fun setRotation(rotation: Vector3f) {
        setRotation(rotation.x, rotation.y, rotation.z)
    }

    /**
     * Sets the entity's head rotation (pitch, yaw, roll).
     * This controls where the head looks, independent of body rotation.
     */
    fun setHeadRotation(pitch: Float, yaw: Float, roll: Float = 0f) {
        world.execute {
            val entityStore = world.entityStore ?: return@execute
            entityStore.store.getComponent(entityRef, HeadRotation.getComponentType())?.rotation?.assign(pitch, yaw, roll)
        }
    }

    /**
     * Sets the entity's head rotation.
     */
    fun setHeadRotation(rotation: Vector3f) {
        setHeadRotation(rotation.x, rotation.y, rotation.z)
    }

    /**
     * Sets the entity's crouching state.
     */
    fun setCrouching(crouching: Boolean) {
        world.execute {
            val entityStore = world.entityStore ?: return@execute
            entityStore.store.getComponent(entityRef, MovementStatesComponent.getComponentType())?.movementStates?.crouching = crouching
        }
    }

    /**
     * Updates the entity's movement states for animation.
     * @param idle True if the entity is standing still
     * @param walking True if the entity is walking
     * @param running True if the entity is running
     * @param sprinting True if the entity is sprinting
     * @param jumping True if the entity is jumping
     * @param falling True if the entity is falling
     * @param onGround True if the entity is on the ground
     * @param sliding True if the entity is sliding (can be used for backwards movement)
     * @param swimming True if the entity is swimming
     * @param climbing True if the entity is climbing
     * @param flying True if the entity is flying
     * @param gliding True if the entity is gliding
     */
    fun setMovementState(
        idle: Boolean = true,
        walking: Boolean = false,
        running: Boolean = false,
        sprinting: Boolean = false,
        jumping: Boolean = false,
        falling: Boolean = false,
        onGround: Boolean = true,
        sliding: Boolean = false,
        swimming: Boolean = false,
        climbing: Boolean = false,
        flying: Boolean = false,
        gliding: Boolean = false
    ) {
        world.execute {
            val entityStore = world.entityStore ?: return@execute
            val states = entityStore.store.getComponent(entityRef, MovementStatesComponent.getComponentType())?.movementStates ?: return@execute
            states.idle = idle
            states.horizontalIdle = idle
            states.walking = walking
            states.running = running
            states.sprinting = sprinting
            states.jumping = jumping
            states.falling = falling
            states.onGround = onGround
            states.sliding = sliding
            states.swimming = swimming
            states.climbing = climbing
            states.flying = flying
            states.gliding = gliding
        }
    }

    /**
     * Applies damage to the entity.
     * @param amount The amount of damage to apply
     * @param cause The damage cause (defaults to "Physical" or COMMAND)
     */
    fun damage(amount: Float, cause: DamageCause? = null) {
        val actualCause = cause ?: getDamageCause("Physical") ?: DamageCause.COMMAND ?: return
        world.execute {
            entityRef.damage(world, amount, actualCause)
        }
    }

    /**
     * Applies damage to the entity with a specific cause name.
     * @param amount The amount of damage to apply
     * @param causeName The name of the damage cause (e.g., "Physical", "Fall", "Fire")
     */
    fun damage(amount: Float, causeName: String) {
        val cause = getDamageCause(causeName) ?: return
        world.execute {
            entityRef.damage(world, amount, cause)
        }
    }

    /**
     * Kills the entity by applying maximum damage.
     * @param cause The damage cause (defaults to COMMAND)
     */
    fun kill(cause: DamageCause? = null) {
        val actualCause = cause ?: DamageCause.COMMAND ?: return
        world.execute {
            entityRef.damage(world, Float.MAX_VALUE, actualCause)
        }
    }

    /**
     * Gets the entity's model component.
     */
    fun getModel(): Model? {
        val entityStore = world.entityStore ?: return null
        return entityStore.store.getComponent(entityRef, ModelComponent.getComponentType())?.model
    }

    /**
     * Gets the entity's bounding box.
     */
    fun getBoundingBox(): com.hypixel.hytale.math.shape.Box? {
        val entityStore = world.entityStore ?: return null
        return entityStore.store.getComponent(entityRef, BoundingBox.getComponentType())?.boundingBox
    }

    fun getNetworkId(): Int? {
        val entityStore = world.entityStore ?: return null
        return entityStore.store.getComponent(entityRef, NetworkId.getComponentType())?.id
    }

    /**
     * Plays an animation on this entity for all Hytale players who can see it.
     * Uses AnimationSlot.Action by default which doesn't require the animation to exist in the model.
     *
     * @param animationId The animation ID to play (e.g., "Attack", "Punch")
     * @param slot The animation slot (default: Action)
     * @param itemAnimationsId Optional item animations ID
     */
    fun playAnimation(animationId: String?, slot: AnimationSlot = AnimationSlot.Action, itemAnimationsId: String? = null) {
        world.execute {
            val store = world.entityStore?.store ?: return@execute
            val networkIdComponent = store.getComponent(entityRef, NetworkId.getComponentType())
            if (networkIdComponent == null) {
                return@execute
            }
            val networkId = networkIdComponent.id
            val packet = PlayAnimation(networkId, itemAnimationsId, animationId, slot)

            // Track how many players we sent to for debugging
            var playerCount = 0
            PlayerUtil.forEachPlayerThatCanSeeEntity(
                entityRef,
                { _, playerRefComponent, _ ->
                    val handler = playerRefComponent.packetHandler
                    if (handler != null) {
                        handler.writeNoCache(packet)
                        playerCount++
                    }
                },
                store
            )
        }
    }

    /**
     * Plays an animation using AnimationUtils (the standard Hytale way).
     * This may be more reliable than the custom implementation.
     */
    fun playAnimationStandard(animationId: String?, slot: AnimationSlot = AnimationSlot.Action, itemAnimationsId: String? = null, sendToSelf: Boolean = true) {
        world.execute {
            val store = world.entityStore?.store ?: return@execute
            com.hypixel.hytale.server.core.entity.AnimationUtils.playAnimation(
                entityRef,
                slot,
                itemAnimationsId,
                animationId,
                sendToSelf,
                store
            )
        }
    }

    fun stopAnimation(slot: AnimationSlot = AnimationSlot.Action) {
        playAnimation(null, slot, null)
    }

    /**
     * Removes the entity from the world.
     */
    fun remove() {
        world.execute {
            val entityStore = world.entityStore ?: return@execute
            entityStore.store.removeEntity(entityRef, EntityStore.REGISTRY.newHolder(), RemoveReason.REMOVE)
        }
        SpawnedEntityManager.unregister(id)
    }

    /**
     * Checks if the entity is still valid.
     */
    fun isValid(): Boolean = entityRef.isValid
}

/**
 * Manager for tracking spawned entities created by TaleLib.
 */
object SpawnedEntityManager {
    private val entities = ConcurrentHashMap<UUID, SpawnedEntity>()

    internal fun register(entity: SpawnedEntity) {
        entities[entity.id] = entity
    }

    internal fun unregister(id: UUID) {
        entities.remove(id)
    }

    /**
     * Gets a spawned entity by its ID.
     */
    fun get(id: UUID): SpawnedEntity? = entities[id]

    /**
     * Gets all registered spawned entities.
     */
    fun getAll(): Collection<SpawnedEntity> = entities.values.toList()

    /**
     * Gets all spawned entities with the specified model asset ID.
     */
    fun getByModel(modelAssetId: String): List<SpawnedEntity> =
        entities.values.filter { it.modelAssetId == modelAssetId }

    /**
     * Removes all registered spawned entities from the world.
     */
    fun removeAll() {
        entities.values.toList().forEach { it.remove() }
    }

    /**
     * Gets the number of registered spawned entities.
     */
    fun count(): Int = entities.size
}
