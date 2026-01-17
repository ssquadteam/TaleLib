@file:JvmName("TaleMarkerProviderKt")

package com.github.ssquadteam.talelib.worldmap

import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.world.World

/**
 * Interface for custom map marker providers.
 *
 * Marker providers are called periodically to supply dynamic markers
 * that appear on the world map. Use this to create markers that
 * update based on game state (e.g., tracking enemies, quest objectives,
 * or dynamic waypoints).
 *
 * Example usage:
 * ```kotlin
 * world.registerMarkerProvider("quest_objectives") { world, player, radius, chunkX, chunkZ ->
 *     getActiveQuests(player).mapNotNull { quest ->
 *         quest.targetPosition?.let { pos ->
 *             MapMarkerBuilder().apply {
 *                 id = "quest_${quest.id}"
 *                 name = quest.name
 *                 icon = "Quest.png"
 *                 position(pos)
 *             }
 *         }
 *     }
 * }
 * ```
 */
fun interface TaleMarkerProvider {

    fun provideMarkers(
        world: World,
        player: Player,
        chunkViewRadius: Int,
        playerChunkX: Int,
        playerChunkZ: Int
    ): List<MapMarkerBuilder>
}

/**
 * Creates a TaleMarkerProvider from a lambda.
 *
 * Example:
 * ```kotlin
 * val provider = markerProvider { world, player, radius, x, z ->
 *     listOf(
 *         MapMarkerBuilder().apply {
 *             name = "Dynamic Marker"
 *             position(player.position)
 *         }
 *     )
 * }
 * ```
 */
inline fun markerProvider(
    crossinline block: (World, Player, Int, Int, Int) -> List<MapMarkerBuilder>
): TaleMarkerProvider {
    return TaleMarkerProvider { world, player, radius, x, z ->
        block(world, player, radius, x, z)
    }
}

fun staticMarkerProvider(vararg markers: MapMarkerBuilder.() -> Unit): TaleMarkerProvider {
    val builtMarkers = markers.map { MapMarkerBuilder().apply(it) }
    return TaleMarkerProvider { _, _, _, _, _ -> builtMarkers }
}
