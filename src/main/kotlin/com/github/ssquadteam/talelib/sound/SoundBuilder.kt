package com.github.ssquadteam.talelib.sound

import com.hypixel.hytale.protocol.SoundCategory
import com.hypixel.hytale.server.core.universe.PlayerRef
import kotlin.random.Random

class SoundBuilder {
    var soundId: String = ""
    var category: SoundCategory = SoundCategory.SFX
    var volume: Float = 1f
    var pitch: Float = 1f
    private var x: Double? = null
    private var y: Double? = null
    private var z: Double? = null
    private var atSelf: Boolean = false

    fun at(x: Double, y: Double, z: Double): SoundBuilder {
        this.x = x
        this.y = y
        this.z = z
        this.atSelf = false
        return this
    }

    fun position(x: Double, y: Double, z: Double) = at(x, y, z)

    fun atSelf(): SoundBuilder {
        this.atSelf = true
        this.x = null
        this.y = null
        this.z = null
        return this
    }

    fun randomPitch(min: Float = 0.9f, max: Float = 1.1f): SoundBuilder {
        pitch = min + (Random.nextFloat() * (max - min))
        return this
    }

    fun sfx(): SoundBuilder {
        category = SoundCategory.SFX
        return this
    }

    fun ui(): SoundBuilder {
        category = SoundCategory.UI
        return this
    }

    fun music(): SoundBuilder {
        category = SoundCategory.Music
        return this
    }

    fun ambient(): SoundBuilder {
        category = SoundCategory.Ambient
        return this
    }

    internal fun play(player: PlayerRef) {
        if (soundId.isEmpty()) return

        when {
            atSelf -> player.playSoundAtSelf(soundId, category, volume, pitch)
            x != null && y != null && z != null -> player.playSound3d(soundId, x!!, y!!, z!!, category, volume, pitch)
            else -> player.playSound2d(soundId, category, volume, pitch)
        }
    }
}

fun PlayerRef.playSound(block: SoundBuilder.() -> Unit) {
    SoundBuilder().apply(block).play(this)
}
