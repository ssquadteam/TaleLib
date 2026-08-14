@file:JvmName("MapSettingsBuilderKt")

package com.github.ssquadteam.talelib.worldmap

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
 * }
 * ```
 */
class MapSettingsBuilder {
    var enabled: Boolean = true

    var allowTeleportToCoordinates: Boolean = true

    var allowTeleportToMarkers: Boolean = true

    var allowShowOnMapToggle: Boolean = true

    var allowCompassTrackingToggle: Boolean = true

    var allowCreatingMapMarkers: Boolean = true

    var allowRemovingOtherPlayersMarkers: Boolean = false

    var defaultZoom: Float = 32f

    var minZoom: Float = 2f

    var maxZoom: Float = 256f

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

    internal fun build(): UpdateWorldMapSettings {
        return UpdateWorldMapSettings(
            enabled,
            allowTeleportToCoordinates,
            allowTeleportToMarkers,
            allowShowOnMapToggle,
            allowCompassTrackingToggle,
            allowCreatingMapMarkers,
            allowRemovingOtherPlayersMarkers,
            defaultZoom,
            minZoom,
            maxZoom
        )
    }
}

fun mapSettings(block: MapSettingsBuilder.() -> Unit): UpdateWorldMapSettings {
    return MapSettingsBuilder().apply(block).build()
}
