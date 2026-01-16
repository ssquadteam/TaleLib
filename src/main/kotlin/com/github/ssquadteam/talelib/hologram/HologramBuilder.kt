@file:JvmName("HologramBuilderKt")

package com.github.ssquadteam.talelib.hologram

import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.universe.world.World

/**
 * DSL builder for creating holograms.
 *
 * Example usage:
 * ```kotlin
 * world.hologram {
 *     text("Hello World")
 *     position(100.0, 65.0, 200.0)
 * }
 *
 * world.hologram {
 *     line("Line 1")
 *     line("Line 2")
 *     line("Line 3")
 *     position(100.0, 65.0, 200.0)
 *     spacing(0.3)
 * }
 * ```
 */
class HologramBuilder(private val world: World) {
    private var text: String = ""
    private val lines = mutableListOf<String>()
    private var x: Double = 0.0
    private var y: Double = 0.0
    private var z: Double = 0.0
    private var lineSpacing: Double = 0.25

    /**
     * Sets the hologram text (for single-line holograms).
     */
    fun text(text: String): HologramBuilder {
        this.text = text
        return this
    }

    /**
     * Adds a line of text (for multi-line holograms).
     */
    fun line(text: String): HologramBuilder {
        lines.add(text)
        return this
    }

    /**
     * Adds multiple lines of text.
     */
    fun lines(vararg texts: String): HologramBuilder {
        lines.addAll(texts)
        return this
    }

    /**
     * Sets the hologram position.
     */
    fun position(x: Double, y: Double, z: Double): HologramBuilder {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    /**
     * Sets the hologram position.
     */
    fun position(pos: Vector3d): HologramBuilder = position(pos.x, pos.y, pos.z)

    /**
     * Sets the vertical spacing between lines (for multi-line holograms).
     */
    fun spacing(value: Double): HologramBuilder {
        lineSpacing = value
        return this
    }

    /**
     * Creates the hologram(s) and returns them.
     */
    fun create(): List<Hologram> {
        return if (lines.isNotEmpty()) {
            world.createMultiLineHologram(lines, x, y, z, lineSpacing)
        } else if (text.isNotEmpty()) {
            listOfNotNull(world.createHologram(text, x, y, z))
        } else {
            emptyList()
        }
    }
}

/**
 * Creates hologram(s) using the DSL builder.
 *
 * @return List of created Holograms
 */
fun World.hologram(block: HologramBuilder.() -> Unit): List<Hologram> {
    return HologramBuilder(this).apply(block).create()
}

/**
 * Creates a single hologram using the DSL builder.
 *
 * @return The created Hologram, or null if creation failed
 */
fun World.hologramSingle(block: HologramBuilder.() -> Unit): Hologram? {
    return HologramBuilder(this).apply(block).create().firstOrNull()
}
