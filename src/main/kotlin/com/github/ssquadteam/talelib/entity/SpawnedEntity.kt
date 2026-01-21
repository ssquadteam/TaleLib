package com.github.ssquadteam.talelib.entity

import com.github.ssquadteam.talelib.damage.damage
import com.github.ssquadteam.talelib.damage.getDamageCause
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.protocol.AnimationSlot
import com.hypixel.hytale.protocol.ComponentUpdate
import com.hypixel.hytale.protocol.ComponentUpdateType
import com.hypixel.hytale.protocol.Equipment
import com.hypixel.hytale.protocol.packets.entities.PlayAnimation
import com.hypixel.hytale.server.core.asset.type.model.config.Model
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems
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
        if (!entityRef.isValid) return
        world.execute {
            if (!entityRef.isValid) return@execute
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
        if (!entityRef.isValid) return
        world.execute {
            if (!entityRef.isValid) return@execute
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
        if (!entityRef.isValid) return
        world.execute {
            if (!entityRef.isValid) return@execute
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
        if (!entityRef.isValid) return
        world.execute {
            if (!entityRef.isValid) return@execute
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
        if (!entityRef.isValid) return
        world.execute {
            if (!entityRef.isValid) return@execute
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
        if (!entityRef.isValid) return
        val actualCause = cause ?: getDamageCause("Physical") ?: DamageCause.COMMAND ?: return
        world.execute {
            if (!entityRef.isValid) return@execute
            entityRef.damage(world, amount, actualCause)
        }
    }

    /**
     * Applies damage to the entity with a specific cause name.
     * @param amount The amount of damage to apply
     * @param causeName The name of the damage cause (e.g., "Physical", "Fall", "Fire")
     */
    fun damage(amount: Float, causeName: String) {
        if (!entityRef.isValid) return
        val cause = getDamageCause(causeName) ?: return
        world.execute {
            if (!entityRef.isValid) return@execute
            entityRef.damage(world, amount, cause)
        }
    }

    /**
     * Kills the entity by applying maximum damage.
     * @param cause The damage cause (defaults to COMMAND)
     */
    fun kill(cause: DamageCause? = null) {
        if (!entityRef.isValid) return
        val actualCause = cause ?: DamageCause.COMMAND ?: return
        world.execute {
            if (!entityRef.isValid) return@execute
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
     * Plays an animation on this entity for nearby Hytale players (within 100 blocks).
     * Use broadcastAnimation() instead if you want to send to ALL players regardless of distance.
     *
     * @param animationId The animation ID to play (e.g., "Hurt", "SwingLeft", "SwingRight")
     * @param slot The animation slot (default: Action)
     * @param itemAnimationsId Optional item animations ID (usually null for model animations)
     */
    fun playAnimation(animationId: String?, slot: AnimationSlot = AnimationSlot.Action, itemAnimationsId: String? = null) {
        if (!entityRef.isValid) return
        world.execute {
            if (!entityRef.isValid) return@execute
            val store = world.entityStore?.store ?: return@execute
            val networkIdComponent = store.getComponent(entityRef, NetworkId.getComponentType()) ?: return@execute

            val activeAnimComp = store.getComponent(entityRef, com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent.getComponentType())
            activeAnimComp?.setPlayingAnimation(slot, animationId)

            val networkId = networkIdComponent.id
            val packet = PlayAnimation(networkId, itemAnimationsId, animationId, slot)

            val entityPos = position ?: return@execute
            val playerRefs = world.playerRefs ?: return@execute

            playerRefs.forEach { playerRef ->
                val playerPos = playerRef.transform?.position
                if (playerPos != null) {
                    val dx = playerPos.x - entityPos.x
                    val dy = playerPos.y - entityPos.y
                    val dz = playerPos.z - entityPos.z
                    val distSq = dx * dx + dy * dy + dz * dz
                    if (distSq < 10000) { // 100 block radius
                        playerRef.packetHandler?.writeNoCache(packet)
                    }
                }
            }
        }
    }

    /**
     * Broadcasts an animation to ALL players in the world using PlayerUtil.
     * This bypasses visibility checks and sends to everyone.
     */
    fun broadcastAnimation(animationId: String?, slot: AnimationSlot = AnimationSlot.Action, itemAnimationsId: String? = null) {
        if (!entityRef.isValid) return
        world.execute {
            if (!entityRef.isValid) return@execute
            val store = world.entityStore?.store ?: return@execute
            val networkIdComponent = store.getComponent(entityRef, NetworkId.getComponentType()) ?: return@execute

            val networkId = networkIdComponent.id
            val packet = PlayAnimation(networkId, itemAnimationsId, animationId, slot)

            PlayerUtil.broadcastPacketToPlayersNoCache(store, packet)
        }
    }

    /**
     * Plays a model animation (no itemAnimationsId).
     * @param animationId The animation ID (e.g., "Hurt", "SwingLeft", "SwingRight")
     * @param slot The animation slot (default: Action)
     */
    fun playModelAnimation(animationId: String, slot: AnimationSlot = AnimationSlot.Action) {
        broadcastAnimation(animationId, slot, null)
    }

    /**
     * Plays the hurt animation on this entity.
     */
    fun playHurtAnimation() {
        broadcastAnimation("Hurt", AnimationSlot.Action, null)
    }

    /**
     * Stops the animation in the specified slot.
     */
    fun stopAnimation(slot: AnimationSlot = AnimationSlot.Action) {
        broadcastAnimation(null, slot, null)
    }

    /**
     * Sets the entity's held item (main hand) for display purposes.
     * This broadcasts an equipment update to all viewers (Hytale players who can see this entity).
     */
    fun setHeldItem(rightHandItemId: String = "Empty", leftHandItemId: String = "Empty") {
        if (!entityRef.isValid) return
        world.execute {
            if (!entityRef.isValid) return@execute
            val entityStore = world.entityStore ?: return@execute
            val store = entityStore.store

            val visibleComponent = store.getComponent(entityRef, EntityTrackerSystems.Visible.getComponentType())
                ?: return@execute

            val equipment = Equipment()
            equipment.rightHandItemId = rightHandItemId
            equipment.leftHandItemId = leftHandItemId
            equipment.armorIds = arrayOf("", "", "", "")

            val update = ComponentUpdate()
            update.type = ComponentUpdateType.Equipment
            update.equipment = equipment

            for (viewer in visibleComponent.visibleTo.values) {
                try {
                    viewer.queueUpdate(entityRef, update)
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * Removes the entity from the world.
     */
    fun remove() {
        SpawnedEntityManager.unregister(id)
        if (!entityRef.isValid) return
        world.execute {
            if (!entityRef.isValid) return@execute
            val entityStore = world.entityStore ?: return@execute
            entityStore.store.removeEntity(entityRef, EntityStore.REGISTRY.newHolder(), RemoveReason.REMOVE)
        }
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
