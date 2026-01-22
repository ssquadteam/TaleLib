@file:JvmName("EntityBuilderKt")

package com.github.ssquadteam.talelib.entity

import com.hypixel.hytale.component.AddReason
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.asset.type.model.config.Model
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset
import com.hypixel.hytale.server.core.entity.UUIDComponent
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation
import com.hypixel.hytale.server.core.modules.entity.component.Interactable
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId
import com.hypixel.hytale.server.core.modules.interaction.Interactions
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * DSL builder for spawning entities with models.
 *
 * Example usage:
 * ```kotlin
 * world.spawnEntity {
 *     model("Minecart")
 *     position(100.0, 65.0, 200.0)
 *     scale(1.5f)
 *     interactable(true)
 * }
 *
 * world.spawnEntity {
 *     model("Kweebec")
 *     position(player.position)
 *     rotation(0f, 180f, 0f)
 * }
 * ```
 */
class EntityBuilder(private val world: World) {
    private var modelAssetId: String = ""
    private var x: Double = 0.0
    private var y: Double = 0.0
    private var z: Double = 0.0
    private var yaw: Float = 0f
    private var pitch: Float = 0f
    private var roll: Float = 0f
    private var scale: Float = 1.0f
    private var interactable: Boolean = false
    private var displayName: String? = null

    internal fun getModelAssetId(): String = modelAssetId
    internal fun getX(): Double = x
    internal fun getY(): Double = y
    internal fun getZ(): Double = z
    internal fun getYaw(): Float = yaw
    internal fun getPitch(): Float = pitch
    internal fun getRoll(): Float = roll
    internal fun getScale(): Float = scale
    internal fun isInteractable(): Boolean = interactable
    internal fun getDisplayName(): String? = displayName

    /**
     * Sets the model asset ID (e.g., "Minecart", "Kweebec", "Trork").
     */
    fun model(modelAssetId: String): EntityBuilder {
        this.modelAssetId = modelAssetId
        return this
    }

    /**
     * Sets the spawn position.
     */
    fun position(x: Double, y: Double, z: Double): EntityBuilder {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    /**
     * Sets the spawn position.
     */
    fun position(pos: Vector3d): EntityBuilder = position(pos.x, pos.y, pos.z)

    /**
     * Sets the rotation (yaw, pitch, roll).
     */
    fun rotation(yaw: Float, pitch: Float, roll: Float = 0f): EntityBuilder {
        this.yaw = yaw
        this.pitch = pitch
        this.roll = roll
        return this
    }

    /**
     * Sets the rotation.
     */
    fun rotation(rot: Vector3f): EntityBuilder = rotation(rot.x, rot.y, rot.z)

    /**
     * Sets the model scale (default 1.0).
     */
    fun scale(scale: Float): EntityBuilder {
        this.scale = scale
        return this
    }

    /**
     * Sets whether the entity should be interactable.
     */
    fun interactable(value: Boolean): EntityBuilder {
        this.interactable = value
        return this
    }

    /**
     * Sets the display name (name tag) for the entity.
     */
    fun displayName(name: String): EntityBuilder {
        this.displayName = name
        return this
    }

    /**
     * Spawns the entity and returns it.
     */
    fun spawn(): SpawnedEntity? {
        if (modelAssetId.isEmpty()) {
            return null
        }

        val future = CompletableFuture<SpawnedEntity?>()

        world.execute {
            val entityStore = world.entityStore ?: run {
                future.complete(null)
                return@execute
            }
            val store = entityStore.store

            // Get the model asset
            val modelAsset = ModelAsset.getAssetMap().getAsset(modelAssetId)
            if (modelAsset == null) {
                future.complete(null)
                return@execute
            }

            // Create scaled model
            val model = Model.createScaledModel(modelAsset, scale)

            // Create entity holder
            val holder = EntityStore.REGISTRY.newHolder()

            // Add TransformComponent for position and rotation
            holder.addComponent(
                TransformComponent.getComponentType(),
                TransformComponent(Vector3d(x, y, z), Vector3f(yaw, pitch, roll))
            )

            // Add PersistentModel for model reference
            val modelReference = Model.ModelReference(modelAssetId, scale, null, false)
            holder.addComponent(
                PersistentModel.getComponentType(),
                PersistentModel(modelReference)
            )

            // Add ModelComponent for the model itself
            holder.addComponent(
                ModelComponent.getComponentType(),
                ModelComponent(model)
            )

            // Add BoundingBox for collision
            val boundingBox = model.boundingBox
            if (boundingBox != null) {
                holder.addComponent(
                    BoundingBox.getComponentType(),
                    BoundingBox(boundingBox)
                )
            }

            // Add NetworkId for network sync
            holder.addComponent(
                NetworkId.getComponentType(),
                NetworkId(store.externalData.takeNextNetworkId())
            )

            // Ensure UUIDComponent
            holder.ensureComponent(UUIDComponent.getComponentType())

            // Add DisplayNameComponent and Nameplate if name is set
            val nameToDisplay = displayName
            if (nameToDisplay != null) {
                holder.addComponent(
                    DisplayNameComponent.getComponentType(),
                    DisplayNameComponent(Message.raw(nameToDisplay))
                )
                // Add Nameplate for visible floating text above the entity
                holder.addComponent(
                    Nameplate.getComponentType(),
                    Nameplate(nameToDisplay)
                )
            }

            // Add Interactions if interactable
            if (interactable) {
                holder.addComponent(Interactions.getComponentType(), Interactions())
                holder.ensureComponent(Interactable.getComponentType())
            }

            // Add HeadRotation component for independent head movement
            holder.addComponent(
                HeadRotation.getComponentType(),
                HeadRotation()
            )

            // Add MovementStatesComponent for crouching/movement state sync
            holder.addComponent(
                MovementStatesComponent.getComponentType(),
                MovementStatesComponent()
            )

            // Add ActiveAnimationComponent for animation tracking
            holder.addComponent(
                ActiveAnimationComponent.getComponentType(),
                ActiveAnimationComponent()
            )

            // Spawn the entity
            val ref: Ref<EntityStore>? = store.addEntity(holder, AddReason.SPAWN)

            if (ref != null) {
                val entity = SpawnedEntity(UUID.randomUUID(), ref, world, modelAssetId)
                SpawnedEntityManager.register(entity)
                future.complete(entity)
            } else {
                future.complete(null)
            }
        }

        return try {
            future.get()
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Spawns an entity using the DSL builder.
 *
 * @return The spawned entity, or null if spawning failed
 */
fun World.spawnEntity(block: EntityBuilder.() -> Unit): SpawnedEntity? {
    return EntityBuilder(this).apply(block).spawn()
}

/**
 * Spawns an entity with the specified model at the given position.
 *
 * @param modelAssetId The model asset ID (e.g., "Minecart", "Kweebec")
 * @param x X coordinate
 * @param y Y coordinate
 * @param z Z coordinate
 * @param scale Model scale (default 1.0)
 * @param interactable Whether the entity should be interactable
 * @return The spawned entity, or null if spawning failed
 */
fun World.spawnEntity(
    modelAssetId: String,
    x: Double,
    y: Double,
    z: Double,
    scale: Float = 1.0f,
    interactable: Boolean = false
): SpawnedEntity? {
    return spawnEntity {
        model(modelAssetId)
        position(x, y, z)
        scale(scale)
        interactable(interactable)
    }
}

/**
 * Spawns an entity with the specified model at the given position.
 */
fun World.spawnEntity(
    modelAssetId: String,
    position: Vector3d,
    scale: Float = 1.0f,
    interactable: Boolean = false
): SpawnedEntity? = spawnEntity(modelAssetId, position.x, position.y, position.z, scale, interactable)

/**
 * Spawns an entity with cleanup of an orphaned entity.
 * The cleanup happens synchronously on the world thread BEFORE the new entity is spawned.
 * This prevents duplicate entities from accumulating.
 *
 * @param orphanedEntity Optional entity to clean up before spawning
 * @param onCleanup Optional callback when cleanup occurs
 * @param block Entity configuration DSL
 * @return The spawned entity, or null if spawning failed
 */
fun World.spawnEntityWithCleanup(
    orphanedEntity: SpawnedEntity?,
    onCleanup: (() -> Unit)? = null,
    block: EntityBuilder.() -> Unit
): SpawnedEntity? {
    val builder = EntityBuilder(this).apply(block)

    val future = java.util.concurrent.CompletableFuture<SpawnedEntity?>()

    this.execute {
        if (orphanedEntity != null && orphanedEntity.isValid()) {
            try {
                val entityStore = this.entityStore
                if (entityStore != null && orphanedEntity.entityRef.isValid) {
                    entityStore.store.removeEntity(
                        orphanedEntity.entityRef,
                        EntityStore.REGISTRY.newHolder(),
                        com.hypixel.hytale.component.RemoveReason.REMOVE
                    )
                    SpawnedEntityManager.unregister(orphanedEntity.id)
                    onCleanup?.invoke()
                }
            } catch (e: Exception) {
            }
        }

        try {
            val entityStore = this.entityStore
            if (entityStore == null) {
                future.complete(null)
                return@execute
            }
            val store = entityStore.store

            val modelAssetId = builder.getModelAssetId()
            if (modelAssetId.isEmpty()) {
                future.complete(null)
                return@execute
            }

            val modelAsset = ModelAsset.getAssetMap().getAsset(modelAssetId)
            if (modelAsset == null) {
                future.complete(null)
                return@execute
            }

            val model = Model.createScaledModel(modelAsset, builder.getScale())
            val holder = EntityStore.REGISTRY.newHolder()

            holder.addComponent(
                TransformComponent.getComponentType(),
                TransformComponent(
                    Vector3d(builder.getX(), builder.getY(), builder.getZ()),
                    Vector3f(builder.getYaw(), builder.getPitch(), builder.getRoll())
                )
            )

            val modelReference = Model.ModelReference(modelAssetId, builder.getScale(), null, false)
            holder.addComponent(PersistentModel.getComponentType(), PersistentModel(modelReference))
            holder.addComponent(ModelComponent.getComponentType(), ModelComponent(model))

            val boundingBox = model.boundingBox
            if (boundingBox != null) {
                holder.addComponent(BoundingBox.getComponentType(), BoundingBox(boundingBox))
            }

            holder.addComponent(
                NetworkId.getComponentType(),
                NetworkId(store.externalData.takeNextNetworkId())
            )

            holder.ensureComponent(UUIDComponent.getComponentType())

            val displayName = builder.getDisplayName()
            if (displayName != null) {
                holder.addComponent(
                    DisplayNameComponent.getComponentType(),
                    DisplayNameComponent(Message.raw(displayName))
                )
                holder.addComponent(Nameplate.getComponentType(), Nameplate(displayName))
            }

            if (builder.isInteractable()) {
                holder.addComponent(Interactions.getComponentType(), Interactions())
                holder.ensureComponent(Interactable.getComponentType())
            }

            holder.addComponent(HeadRotation.getComponentType(), HeadRotation())
            holder.addComponent(MovementStatesComponent.getComponentType(), MovementStatesComponent())
            holder.addComponent(ActiveAnimationComponent.getComponentType(), ActiveAnimationComponent())

            val ref = store.addEntity(holder, AddReason.SPAWN)
            if (ref != null) {
                val entity = SpawnedEntity(java.util.UUID.randomUUID(), ref, this, modelAssetId)
                SpawnedEntityManager.register(entity)
                future.complete(entity)
            } else {
                future.complete(null)
            }
        } catch (e: Exception) {
            future.complete(null)
        }
    }

    return try {
        future.get()
    } catch (e: Exception) {
        null
    }
}
