package com.github.ssquadteam.talelib.collision

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.math.shape.Box
import com.hypixel.hytale.math.vector.Vector2d
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.protocol.CollisionType
import com.hypixel.hytale.server.core.modules.collision.*
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

// ============================================
// Collision Material Constants
// ============================================

object CollisionMaterials {
    const val EMPTY = CollisionMaterial.MATERIAL_EMPTY
    const val FLUID = CollisionMaterial.MATERIAL_FLUID
    const val SOLID = CollisionMaterial.MATERIAL_SOLID
    const val SUBMERGED = CollisionMaterial.MATERIAL_SUBMERGED
    const val DAMAGE = CollisionMaterial.MATERIAL_DAMAGE
    const val ANY = CollisionMaterial.MATERIAL_SET_ANY
    const val NONE = CollisionMaterial.MATERIAL_SET_NONE
}

// ============================================
// Raycast Extensions
// ============================================

fun World.raycast(
    origin: Vector3d,
    direction: Vector3d,
    maxDistance: Double,
    materials: Int = CollisionMaterials.SOLID
): RaycastResult? {
    val pointBox = Box(0.0, 0.0, 0.0, 0.01, 0.01, 0.01)
    val ray = Vector3d(direction).normalize().scale(maxDistance)

    val result = CollisionResult(false, false)
    result.setCollisionByMaterial(materials)

    return try {
        CollisionModule.findBlockCollisionsIterative(
            this, pointBox, origin, ray, true, result
        )

        val collision = result.firstBlockCollision
        if (collision != null) {
            RaycastResult(
                hit = true,
                position = Vector3d(collision.collisionPoint),
                normal = Vector3d(collision.collisionNormal),
                blockX = collision.x,
                blockY = collision.y,
                blockZ = collision.z,
                blockId = collision.blockId,
                distance = collision.collisionStart * maxDistance
            )
        } else null
    } catch (e: Exception) {
        null
    }
}

fun World.raycastBlocks(
    origin: Vector3d,
    direction: Vector3d,
    maxDistance: Double
): RaycastResult? {
    return raycast(origin, direction, maxDistance, CollisionMaterials.SOLID)
}

fun World.raycastFluids(
    origin: Vector3d,
    direction: Vector3d,
    maxDistance: Double
): RaycastResult? {
    return raycast(origin, direction, maxDistance, CollisionMaterials.FLUID)
}

fun World.raycastAll(
    origin: Vector3d,
    direction: Vector3d,
    maxDistance: Double
): RaycastResult? {
    return raycast(origin, direction, maxDistance, CollisionMaterials.ANY)
}

// ============================================
// Player Raycast Extensions
// ============================================

fun PlayerRef.raycastFromEyes(
    maxDistance: Double,
    materials: Int = CollisionMaterials.SOLID
): RaycastResult? {
    val ref = this.reference ?: return null
    if (!ref.isValid) return null
    val world = (ref.store.externalData as? EntityStore)?.world ?: return null
    val transform = this.transform
    val pos = transform.position
    val eyePos = Vector3d(pos.x, pos.y + 1.62, pos.z)
    val direction = getLookDirection() ?: return null
    return world.raycast(eyePos, direction, maxDistance, materials)
}

fun PlayerRef.getTargetBlock(maxDistance: Double = 5.0): RaycastResult? {
    return raycastFromEyes(maxDistance, CollisionMaterials.SOLID)
}

fun PlayerRef.getTargetFluid(maxDistance: Double = 5.0): RaycastResult? {
    return raycastFromEyes(maxDistance, CollisionMaterials.FLUID)
}

private fun PlayerRef.getLookDirection(): Vector3d? {
    val ref = this.reference ?: return null
    if (!ref.isValid) return null

    val headRot = this.headRotation
    val yaw = headRot.y
    val pitch = headRot.x

    val yawRad = Math.toRadians(yaw.toDouble())
    val pitchRad = Math.toRadians(pitch.toDouble())

    val x = -kotlin.math.sin(yawRad) * kotlin.math.cos(pitchRad)
    val y = -kotlin.math.sin(pitchRad)
    val z = kotlin.math.cos(yawRad) * kotlin.math.cos(pitchRad)

    return Vector3d(x, y, z).normalize()
}

// ============================================
// Collision Check Extensions
// ============================================

fun World.findCollisions(
    collider: Box,
    position: Vector3d,
    movement: Vector3d,
    materials: Int = CollisionMaterials.SOLID
): CollisionQueryResult {
    val result = CollisionResult(true, false)
    result.setCollisionByMaterial(materials)

    return try {
        val farDistance = CollisionModule.findCollisions(
            collider, position, movement, result, this.entityStore.store
        )

        val blockCollisions = mutableListOf<BlockCollisionInfo>()
        for (i in 0 until result.blockCollisionCount) {
            val collision = result.getBlockCollision(i)
            blockCollisions.add(BlockCollisionInfo.from(collision))
        }

        CollisionQueryResult(
            hasCollision = result.blockCollisionCount > 0,
            blockCollisions = blockCollisions,
            farDistance = farDistance
        )
    } catch (e: Exception) {
        CollisionQueryResult(
            hasCollision = false,
            blockCollisions = emptyList(),
            farDistance = false
        )
    }
}

fun World.checkCollision(
    collider: Box,
    position: Vector3d,
    movement: Vector3d
): Boolean {
    return findCollisions(collider, position, movement).hasCollision
}

fun World.isPositionValid(collider: Box, position: Vector3d): Boolean {
    val result = CollisionResult()
    return try {
        val validation = CollisionModule.get().validatePosition(this, collider, position, result)
        validation != CollisionModule.VALIDATE_INVALID
    } catch (e: Exception) {
        false
    }
}

fun World.isOnGround(collider: Box, position: Vector3d): Boolean {
    val result = CollisionResult()
    return try {
        val validation = CollisionModule.get().validatePosition(this, collider, position, result)
        (validation and CollisionModule.VALIDATE_ON_GROUND) != 0
    } catch (e: Exception) {
        false
    }
}

fun World.isTouchingCeiling(collider: Box, position: Vector3d): Boolean {
    val result = CollisionResult()
    return try {
        val validation = CollisionModule.get().validatePosition(this, collider, position, result)
        (validation and CollisionModule.VALIDATE_TOUCH_CEIL) != 0
    } catch (e: Exception) {
        false
    }
}

// ============================================
// AABB Intersection Utilities
// ============================================

object CollisionUtils {
    fun intersectRayAABB(
        rayOrigin: Vector3d,
        rayDirection: Vector3d,
        boxPosition: Vector3d,
        box: Box
    ): Double? {
        val minMax = Vector2d()
        val hit = CollisionMath.intersectRayAABB(
            rayOrigin, rayDirection,
            boxPosition.x, boxPosition.y, boxPosition.z,
            box, minMax
        )
        return if (hit) minMax.x else null
    }

    fun intersectVectorAABB(
        origin: Vector3d,
        vector: Vector3d,
        boxPosition: Vector3d,
        box: Box
    ): Double? {
        val minMax = Vector2d()
        val hit = CollisionMath.intersectVectorAABB(
            origin, vector,
            boxPosition.x, boxPosition.y, boxPosition.z,
            box, minMax
        )
        return if (hit) minMax.x else null
    }

    fun intersectSweptAABBs(
        movingBoxPos: Vector3d,
        movement: Vector3d,
        movingBox: Box,
        staticBoxPos: Vector3d,
        staticBox: Box
    ): Double? {
        val minMax = Vector2d()
        val temp = Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
        val hit = CollisionMath.intersectSweptAABBs(
            movingBoxPos, movement, movingBox,
            staticBoxPos, staticBox,
            minMax, temp
        )
        return if (hit) minMax.x else null
    }

    fun isDisjoint(code: Int): Boolean = CollisionMath.isDisjoint(code)
    fun isOverlapping(code: Int): Boolean = CollisionMath.isOverlapping(code)
    fun isTouching(code: Int): Boolean = CollisionMath.isTouching(code)
}

// ============================================
// Entity Collision Extensions
// ============================================

fun World.findEntityCollisions(
    position: Vector3d,
    movement: Vector3d,
    ignoreSelf: Ref<EntityStore>? = null
): List<EntityCollisionInfo> {
    val result = CollisionResult(false, true)

    return try {
        CollisionModule.findCharacterCollisions(position, movement, result, this.entityStore.store)

        val collisions = mutableListOf<EntityCollisionInfo>()
        var collision = result.firstCharacterCollision
        while (collision != null) {
            if (ignoreSelf == null || collision.entityReference != ignoreSelf) {
                collisions.add(EntityCollisionInfo.from(collision))
            }
            result.forgetFirstCharacterCollision()
            collision = result.firstCharacterCollision
        }
        collisions
    } catch (e: Exception) {
        emptyList()
    }
}

// ============================================
// Data Classes
// ============================================

data class RaycastResult(
    val hit: Boolean,
    val position: Vector3d,
    val normal: Vector3d,
    val blockX: Int,
    val blockY: Int,
    val blockZ: Int,
    val blockId: Int,
    val distance: Double
) {
    val blockPosition: Vector3d
        get() = Vector3d(blockX.toDouble(), blockY.toDouble(), blockZ.toDouble())
}

data class BlockCollisionInfo(
    val x: Int,
    val y: Int,
    val z: Int,
    val blockId: Int,
    val rotation: Int,
    val collisionPoint: Vector3d,
    val collisionNormal: Vector3d,
    val collisionScale: Double,
    val touching: Boolean,
    val overlapping: Boolean,
    val willDamage: Boolean,
    val fluidId: Int
) {
    companion object {
        fun from(data: BlockCollisionData): BlockCollisionInfo {
            return BlockCollisionInfo(
                x = data.x,
                y = data.y,
                z = data.z,
                blockId = data.blockId,
                rotation = data.rotation,
                collisionPoint = Vector3d(data.collisionPoint),
                collisionNormal = Vector3d(data.collisionNormal),
                collisionScale = data.collisionStart,
                touching = data.touching,
                overlapping = data.overlapping,
                willDamage = data.willDamage,
                fluidId = data.fluidId
            )
        }
    }
}

data class EntityCollisionInfo(
    val entityRef: Ref<EntityStore>,
    val isPlayer: Boolean,
    val collisionPoint: Vector3d,
    val collisionScale: Double
) {
    companion object {
        fun from(data: CharacterCollisionData): EntityCollisionInfo {
            return EntityCollisionInfo(
                entityRef = data.entityReference,
                isPlayer = data.isPlayer,
                collisionPoint = Vector3d(data.collisionPoint),
                collisionScale = data.collisionStart
            )
        }
    }
}

data class CollisionQueryResult(
    val hasCollision: Boolean,
    val blockCollisions: List<BlockCollisionInfo>,
    val farDistance: Boolean
) {
    val firstCollision: BlockCollisionInfo?
        get() = blockCollisions.firstOrNull()

    val collisionCount: Int
        get() = blockCollisions.size
}

// ============================================
// Box Builder Extensions
// ============================================

fun box(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): Box {
    return Box(minX, minY, minZ, maxX, maxY, maxZ)
}

fun boxCentered(centerX: Double, centerY: Double, centerZ: Double, width: Double, height: Double, depth: Double): Box {
    val halfW = width / 2
    val halfD = depth / 2
    return Box(
        centerX - halfW, centerY, centerZ - halfD,
        centerX + halfW, centerY + height, centerZ + halfD
    )
}

fun playerBox(): Box {
    return Box(-0.3, 0.0, -0.3, 0.3, 1.8, 0.3)
}

fun pointBox(): Box {
    return Box(0.0, 0.0, 0.0, 0.01, 0.01, 0.01)
}
