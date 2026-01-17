@file:JvmName("MapSettingsBuilderKt")

package com.github.ssquadteam.talelib.worldmap

import com.hypixel.hytale.protocol.packets.worldmap.BiomeData
import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMapSettings

/**
 * DSL builder for configuring world map settings.
 *
 * Example usage:
 * ```kotlin
 * world.updateMapSettings {
 *     enabled = true
 *     defaultZoom = 64f
 *     zoomRange(min = 8f, max = 128f, default = 64f)
 *     allowTeleportToMarkers = true
 *     allowTeleportToCoordinates = false
 *     biomeColor(1, 0, "Zone Alpha", "Forest", MapColors.FOREST_GREEN)
 * }
 * ```
 */
class MapSettingsBuilder {
    var enabled: Boolean = true

    var allowTeleportToCoordinates: Boolean = true

    var allowTeleportToMarkers: Boolean = true

    var defaultZoom: Float = 32f

    var minZoom: Float = 2f

    var maxZoom: Float = 256f

    private val biomeColors = mutableMapOf<Short, BiomeData>()

    fun zoomRange(min: Float, max: Float, default: Float): MapSettingsBuilder {
        this.minZoom = min
        this.maxZoom = max
        this.defaultZoom = default
        return this
    }

    fun disableTeleportation(): MapSettingsBuilder {
        allowTeleportToCoordinates = false
        allowTeleportToMarkers = false
        return this
    }

    fun enableTeleportation(): MapSettingsBuilder {
        allowTeleportToCoordinates = true
        allowTeleportToMarkers = true
        return this
    }

    fun biomeColor(
        biomeId: Short,
        zoneId: Int,
        zoneName: String,
        biomeName: String,
        color: Int
    ): MapSettingsBuilder {
        biomeColors[biomeId] = BiomeData(zoneId, zoneName, biomeName, color)
        return this
    }

    fun biomeColor(biomeId: Short, color: Int): MapSettingsBuilder {
        biomeColors[biomeId] = BiomeData(biomeId.toInt(), null, null, color)
        return this
    }

    fun clearBiomeColors(): MapSettingsBuilder {
        biomeColors.clear()
        return this
    }

    internal fun build(): UpdateWorldMapSettings {
        return UpdateWorldMapSettings(
            enabled,
            biomeColors.takeIf { it.isNotEmpty() },
            allowTeleportToCoordinates,
            allowTeleportToMarkers,
            defaultZoom,
            minZoom,
            maxZoom
        )
    }
}

fun mapSettings(block: MapSettingsBuilder.() -> Unit): UpdateWorldMapSettings {
    return MapSettingsBuilder().apply(block).build()
}
