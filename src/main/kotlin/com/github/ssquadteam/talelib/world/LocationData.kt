package com.github.ssquadteam.talelib.world

import com.hypixel.hytale.protocol.Vector3f
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Location(
    val worldId: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val yaw: Float = 0f,
    val pitch: Float = 0f
) {
    constructor(worldUuid: UUID, x: Float, y: Float, z: Float, yaw: Float = 0f, pitch: Float = 0f)
            : this(worldUuid.toString(), x, y, z, yaw, pitch)

    fun toVector(): Vector3f = Vector3f(x, y, z)
    fun worldUuid(): UUID = UUID.fromString(worldId)

    fun distanceTo(other: Location): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun distanceSquaredTo(other: Location): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return dx * dx + dy * dy + dz * dz
    }

    fun isSameWorld(other: Location): Boolean = worldId == other.worldId

    fun blockX(): Int = kotlin.math.floor(x.toDouble()).toInt()
    fun blockY(): Int = kotlin.math.floor(y.toDouble()).toInt()
    fun blockZ(): Int = kotlin.math.floor(z.toDouble()).toInt()

    fun add(dx: Float, dy: Float, dz: Float): Location = copy(x = x + dx, y = y + dy, z = z + dz)
    fun add(offset: Vector3f): Location = add(offset.x, offset.y, offset.z)
    fun subtract(dx: Float, dy: Float, dz: Float): Location = copy(x = x - dx, y = y - dy, z = z - dz)

    fun isWithin(other: Location, range: Float): Boolean =
        isSameWorld(other) && distanceSquaredTo(other) <= range * range

    fun centerBlock(): Location = copy(
        x = blockX() + 0.5f,
        y = blockY().toFloat(),
        z = blockZ() + 0.5f
    )

    override fun toString(): String =
        "Location(world=$worldId, x=$x, y=$y, z=$z, yaw=$yaw, pitch=$pitch)"

    companion object {
        fun origin(worldId: String): Location = Location(worldId, 0f, 0f, 0f)

        fun fromVector(worldId: String, vector: Vector3f, yaw: Float = 0f, pitch: Float = 0f): Location =
            Location(worldId, vector.x, vector.y, vector.z, yaw, pitch)
    }
}

fun Vector3f.toLocation(worldId: String): Location =
    Location(worldId, this.x, this.y, this.z)

fun Vector3f.toLocation(worldId: String, yaw: Float, pitch: Float): Location =
    Location(worldId, this.x, this.y, this.z, yaw, pitch)
