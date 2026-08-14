@file:JvmName("WorldMapExtensionsKt")

package com.github.ssquadteam.talelib.worldmap

import com.hypixel.hytale.protocol.packets.worldmap.MapMarker
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker
import it.unimi.dsi.fastutil.longs.LongSet

// ============================================================================
// World Extensions
// ============================================================================

val World.mapManager: WorldMapManager?
    get() = this.worldMapManager

val World.isMapEnabled: Boolean
    get() = mapManager?.isWorldMapEnabled ?: false

/**
 * Adds a map marker to the world's points of interest.
 * These markers are visible to all players.
 *
 * Example:
 * ```kotlin
 * world.addMapMarker {
 *     id = "shop_marker"
 *     name = "Village Shop"
 *     icon = "Home.png"
 *     position(100.0, 65.0, 200.0)
 *     contextAction("Teleport", "/tp @s 100 65 200")
 * }
 * ```
 *
 * @param block Builder configuration
 * @return The created MapMarker, or null if map manager is unavailable
 */
fun World.addMapMarker(block: MapMarkerBuilder.() -> Unit): MapMarker? {
    val marker = MapMarkerBuilder().apply(block).build()
    mapManager?.pointsOfInterest?.put(marker.id, marker)
    return marker
}

fun World.removeMapMarker(markerId: String): Boolean {
    return mapManager?.pointsOfInterest?.remove(markerId) != null
}

fun World.clearMapMarkers() {
    mapManager?.pointsOfInterest?.clear()
}

fun World.getMapMarkers(): Map<String, MapMarker> {
    return mapManager?.pointsOfInterest?.toMap() ?: emptyMap()
}

/**
 * Registers a custom marker provider for dynamic markers.
 *
 * Marker providers are called periodically to supply dynamic markers
 * that appear on the world map.
 *
 * Example:
 * ```kotlin
 * world.registerMarkerProvider("quest_markers") { world, player, radius, chunkX, chunkZ ->
 *     getActiveQuests(player).map { quest ->
 *         MapMarkerBuilder().apply {
 *             id = "quest_${quest.id}"
 *             name = quest.name
 *             icon = "Quest.png"
 *             position(quest.targetPosition)
 *         }
 *     }
 * }
 * ```
 *
 * @param key Unique key for this provider (used for unregistration)
 * @param provider The marker provider implementation
 */
fun World.registerMarkerProvider(key: String, provider: TaleMarkerProvider) {
    val wrapped = WorldMapManager.MarkerProvider { providerWorld, player, collector: MarkersCollector ->
        provider.provideMarkers(providerWorld, player).forEach { collector.add(it.build()) }
    }
    mapManager?.addMarkerProvider(key, wrapped)
}

fun World.unregisterMarkerProvider(key: String): Boolean {
    return mapManager?.markerProviders?.remove(key) != null
}

fun World.getMarkerProviderKeys(): Set<String> {
    return mapManager?.markerProviders?.keys ?: emptySet()
}

fun World.setMapEnabled(enabled: Boolean) {
    val settings = mapManager?.worldMapSettings?.settingsPacket ?: return
    settings.enabled = enabled
    mapManager?.sendSettings()
}

fun World.refreshMapImages() {
    mapManager?.clearImages()
    for (player in players) {
        player.worldMapTracker.clear()
    }
}

fun World.refreshMapImagesInChunks(chunkIndices: LongSet) {
    mapManager?.clearImagesInChunks(chunkIndices)
    for (player in players) {
        player.worldMapTracker.clearChunks(chunkIndices)
    }
}

fun World.broadcastMapSettings() {
    mapManager?.sendSettings()
}

// ============================================================================
// Player Extensions
// ============================================================================

val Player.mapTracker: WorldMapTracker
    get() = this.worldMapTracker

/**
 * Adds a personal map marker for this player only.
 * Personal markers are stored in the player's world data and persist.
 *
 * Example:
 * ```kotlin
 * player.addPersonalMapMarker {
 *     name = "My Secret Base"
 *     icon = "Star.png"
 *     position(player.position)
 * }
 * ```
 *
 * @param block Builder configuration
 * @return The created MapMarker
 */
fun Player.addPersonalMapMarker(block: MapMarkerBuilder.() -> Unit): UserMapMarker {
    val marker = MapMarkerBuilder().apply(block).buildUserMarker()
    personalMapMarkerStore()?.addUserMapMarker(marker)
    return marker
}

fun Player.removePersonalMapMarker(markerId: String): Boolean {
    val store = personalMapMarkerStore() ?: return false
    if (store.getUserMapMarker(markerId) == null) return false
    store.removeUserMapMarker(markerId)
    return true
}

fun Player.clearPersonalMapMarkers() {
    personalMapMarkerStore()?.setUserMapMarkers(null)
}

fun Player.getPersonalMapMarkers(): List<UserMapMarker> =
    personalMapMarkerStore()?.userMapMarkers?.toList() ?: emptyList()

private fun Player.personalMapMarkerStore() =
    this.world?.let { this.playerConfigData.getPerWorldData(it.name) }

fun Player.refreshMap() {
    this.worldMapTracker.clear()
}

fun Player.setMapViewRadius(radius: Int?) {
    this.worldMapTracker.viewRadiusOverride = radius
}

fun Player.getMapViewRadius(): Int {
    val world = this.world ?: return 0
    return this.worldMapTracker.getEffectiveViewRadius(world)
}

fun Player.isMapTeleportToCoordinatesAllowed(): Boolean =
    WorldMapTracker.isAllowTeleportToCoordinates(this.playerRef, this)

fun Player.isMapTeleportToMarkersAllowed(): Boolean =
    this.worldMapTracker.isAllowTeleportToMarkers(this.playerRef, this)

fun Player.discoverZone(zoneName: String): Boolean {
    val world = this.world ?: return false
    return this.worldMapTracker.discoverZone(world, zoneName)
}

fun Player.undiscoverZone(zoneName: String): Boolean {
    val world = this.world ?: return false
    return this.worldMapTracker.undiscoverZone(world, zoneName)
}

fun Player.getDiscoveredZones(): Set<String> {
    return this.playerConfigData.discoveredZones
}

fun Player.getCurrentZoneName(): String? {
    return this.worldMapTracker.currentZone?.zoneName()
}

fun Player.getCurrentBiomeName(): String? {
    return this.worldMapTracker.currentBiomeName
}

// ============================================================================
// DSL Helpers
// ============================================================================

/**
 * Creates and adds a map marker in one call.
 *
 * Example:
 * ```kotlin
 * world.marker("spawn") {
 *     name = "Spawn Point"
 *     icon = "Spawn.png"
 *     position(0.0, 64.0, 0.0)
 * }
 * ```
 */
fun World.marker(id: String, block: MapMarkerBuilder.() -> Unit): MapMarker? {
    return addMapMarker {
        this.id = id
        block()
    }
}

/**
 * Batch adds multiple markers to the world.
 *
 * Example:
 * ```kotlin
 * world.addMapMarkers {
 *     marker {
 *         id = "shop"
 *         name = "Shop"
 *         position(100.0, 64.0, 100.0)
 *     }
 *     marker {
 *         id = "arena"
 *         name = "Arena"
 *         position(-50.0, 64.0, 200.0)
 *     }
 * }
 * ```
 */
fun World.addMapMarkers(block: MarkerListBuilder.() -> Unit): List<MapMarker> {
    val builder = MarkerListBuilder()
    builder.block()
    return builder.markers.mapNotNull { addMapMarker(it) }
}

class MarkerListBuilder {
    internal val markers = mutableListOf<MapMarkerBuilder.() -> Unit>()

    fun marker(block: MapMarkerBuilder.() -> Unit) {
        markers.add(block)
    }
}
