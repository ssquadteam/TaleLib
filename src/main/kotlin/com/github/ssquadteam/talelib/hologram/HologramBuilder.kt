package com.github.ssquadteam.talelib.hologram

import com.hypixel.hytale.math.vector.Vector3d
import java.util.UUID

// Note: This is a placeholder builder. The actual hologram creation
// requires integration with Hytale's NPC/prefab system.

class HologramBuilder {
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
        // Create placeholder holograms (not real entities)
        return if (lines.isNotEmpty()) {
            lines.mapIndexed { index, line ->
                val hologram = Hologram(
                    UUID.randomUUID(),
                    line,
                    x,
                    y + (lines.size - 1 - index) * lineSpacing,
                    z
                )
                HologramManager.register(hologram)
                hologram
            }
        } else if (text.isNotEmpty()) {
            val hologram = Hologram(UUID.randomUUID(), text, x, y, z)
            HologramManager.register(hologram)
            listOf(hologram)
        } else {
            emptyList()
        }
    }
}

fun hologram(block: HologramBuilder.() -> Unit): List<Hologram> {
    return HologramBuilder().apply(block).create()
}

fun hologramSingle(block: HologramBuilder.() -> Unit): Hologram? {
    return HologramBuilder().apply(block).create().firstOrNull()
}
