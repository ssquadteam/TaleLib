@file:JvmName("SoundExtensions")

package com.github.ssquadteam.talelib.sound

import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.protocol.SoundCategory
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent
import com.hypixel.hytale.server.core.modules.entity.EntityModule
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.SoundUtil

fun soundIndex(soundId: String): Int = SoundEvent.getAssetMap().getIndex(soundId)

fun PlayerRef.playSound2d(
    soundId: String,
    category: SoundCategory = SoundCategory.SFX,
    volume: Float = 1f,
    pitch: Float = 1f
) {
    val index = soundIndex(soundId)
    if (index != 0) {
        SoundUtil.playSoundEvent2dToPlayer(this, index, category, volume, pitch)
    }
}

fun PlayerRef.playSound3d(
    soundId: String,
    x: Double,
    y: Double,
    z: Double,
    category: SoundCategory = SoundCategory.SFX,
    volume: Float = 1f,
    pitch: Float = 1f
) {
    val index = soundIndex(soundId)
    if (index == 0) return
    val player = this.player ?: return
    val world = player.world ?: return
    val store = world.entityStore?.store ?: return
    SoundUtil.playSoundEvent3dToPlayer(
        player.reference,
        index,
        category,
        x,
        y,
        z,
        volume,
        pitch,
        store
    )
}

fun PlayerRef.playSound3d(
    soundId: String,
    position: Vector3d,
    category: SoundCategory = SoundCategory.SFX,
    volume: Float = 1f,
    pitch: Float = 1f
) = playSound3d(soundId, position.x, position.y, position.z, category, volume, pitch)

fun PlayerRef.playSoundAtSelf(
    soundId: String,
    category: SoundCategory = SoundCategory.SFX,
    volume: Float = 1f,
    pitch: Float = 1f
) {
    val player = this.player ?: return
    val world = player.world ?: return
    val store = world.entityStore?.store ?: return
    val transform = store.getComponent(player.reference, TransformComponent.getComponentType()) ?: return
    val pos = transform.position
    playSound3d(soundId, pos.x, pos.y, pos.z, category, volume, pitch)
}

fun PlayerRef.playUISound(soundId: String, volume: Float = 1f, pitch: Float = 1f) =
    playSound2d(soundId, SoundCategory.UI, volume, pitch)

fun PlayerRef.playMusicSound(soundId: String, volume: Float = 1f, pitch: Float = 1f) =
    playSound2d(soundId, SoundCategory.Music, volume, pitch)

fun PlayerRef.playAmbientSound(soundId: String, volume: Float = 1f, pitch: Float = 1f) =
    playSound2d(soundId, SoundCategory.Ambient, volume, pitch)

fun PlayerRef.playSFXSound(soundId: String, volume: Float = 1f, pitch: Float = 1f) =
    playSound2d(soundId, SoundCategory.SFX, volume, pitch)
