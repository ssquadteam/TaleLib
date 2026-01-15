package com.github.ssquadteam.talelib.hologram

import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.universe.world.World

class HologramBuilder(private val world: World) {
    private var text: String = ""
    private val lines = mutableListOf<String>()
    private var x: Double = 0.0
    private var y: Double = 0.0
    private var z: Double = 0.0
    private var lineSpacing: Double = 0.25

    fun text(text: String): HologramBuilder {
        this.text = text
        return this
    }

    fun line(text: String): HologramBuilder {
        lines.add(text)
        return this
    }

    fun lines(vararg texts: String): HologramBuilder {
        lines.addAll(texts)
        return this
    }

    fun position(x: Double, y: Double, z: Double): HologramBuilder {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun position(pos: Vector3d): HologramBuilder = position(pos.x, pos.y, pos.z)

    fun spacing(value: Double): HologramBuilder {
        lineSpacing = value
        return this
    }

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

fun World.hologram(block: HologramBuilder.() -> Unit): List<Hologram> {
    return HologramBuilder(this).apply(block).create()
}

fun World.hologramSingle(block: HologramBuilder.() -> Unit): Hologram? {
    return HologramBuilder(this).apply(block).create().firstOrNull()
}
