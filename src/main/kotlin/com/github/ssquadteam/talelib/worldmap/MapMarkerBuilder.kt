@file:JvmName("MapMarkerBuilderKt")

package com.github.ssquadteam.talelib.worldmap

import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.protocol.Direction
import com.hypixel.hytale.protocol.Position
import com.hypixel.hytale.protocol.Transform
import com.hypixel.hytale.protocol.packets.worldmap.ContextMenuItem
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker
import java.util.UUID

/**
 * DSL builder for creating world map markers.
 *
 * Example usage:
 * ```kotlin
 * world.addMapMarker {
 *     id = "my_base"
 *     name = "My Base"
 *     icon = "Home.png"
 *     position(100.0, 65.0, 200.0)
 *     contextAction("Teleport", "/tp @s 100 65 200")
 * }
 * ```
 */
class MapMarkerBuilder {
    var id: String = UUID.randomUUID().toString()

    var name: String = ""

    var icon: String = ""

    private var x: Double = 0.0
    private var y: Double = 0.0
    private var z: Double = 0.0
    private var yaw: Float = 0f
    private var pitch: Float = 0f
    private var roll: Float = 0f
    private val contextMenuItems = mutableListOf<Pair<String, String>>()

    fun position(x: Double, y: Double, z: Double): MapMarkerBuilder {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun position(pos: Vector3d): MapMarkerBuilder = position(pos.x, pos.y, pos.z)

    fun rotation(yaw: Float, pitch: Float = 0f, roll: Float = 0f): MapMarkerBuilder {
        this.yaw = yaw
        this.pitch = pitch
        this.roll = roll
        return this
    }

    fun contextAction(name: String, command: String): MapMarkerBuilder {
        contextMenuItems.add(name to command)
        return this
    }

    internal fun build(): MapMarker {
        val pos = Position(x, y, z)
        val dir = Direction(yaw, pitch, roll)
        val transform = Transform(pos, dir)

        val menuItems = if (contextMenuItems.isNotEmpty()) {
            contextMenuItems.map { (n, c) -> ContextMenuItem(n, c) }.toTypedArray()
        } else null

        return MapMarker(id, name.ifEmpty { null }, icon.ifEmpty { null }, transform, menuItems)
    }
}

/**
 * Creates a MapMarker using the DSL builder.
 *
 * Example:
 * ```kotlin
 * val marker = mapMarker {
 *     name = "Waypoint"
 *     icon = "Star.png"
 *     position(100.0, 65.0, 200.0)
 * }
 * ```
 */
fun mapMarker(block: MapMarkerBuilder.() -> Unit): MapMarker {
    return MapMarkerBuilder().apply(block).build()
}
