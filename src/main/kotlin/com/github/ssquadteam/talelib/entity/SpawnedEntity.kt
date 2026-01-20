package com.github.ssquadteam.talelib.entity

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.asset.type.model.config.Model
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
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
    internal val entityRef: Ref<EntityStore>,
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
     */
    fun setMovementState(
        idle: Boolean = true,
        walking: Boolean = false,
        running: Boolean = false,
        sprinting: Boolean = false,
        jumping: Boolean = false,
        falling: Boolean = false,
        onGround: Boolean = true
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
