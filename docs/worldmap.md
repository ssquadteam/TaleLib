# World Map API

TaleLib provides a comprehensive API for manipulating Hytale's world map system (accessed via the `M` key). You can add custom markers, register dynamic marker providers, control map settings, and even create custom map images.

## Table of Contents

- [Quick Start](#quick-start)
- [Map Markers](#map-markers)
  - [World Markers](#world-markers)
  - [Personal Markers](#personal-markers)
  - [Dynamic Marker Providers](#dynamic-marker-providers)
- [Map Images](#map-images)
- [Map Settings](#map-settings)
- [Map Colors](#map-colors)
- [Zone Discovery](#zone-discovery)
- [API Reference](#api-reference)

## Quick Start

```kotlin
import com.github.ssquadteam.talelib.worldmap.*

// Add a simple marker
world.addMapMarker {
    id = "my_base"
    name = "My Base"
    icon = "Home.png"
    position(100.0, 65.0, 200.0)
}

// Add a marker with context menu actions
world.addMapMarker {
    id = "shop"
    name = "Village Shop"
    icon = "Prefab.png"
    position(50.0, 64.0, -30.0)
    contextAction("Teleport", "/tp @s 50 64 -30")
    contextAction("View Items", "/shop view")
}
```

## Map Markers

### World Markers

World markers are visible to all players and are stored in the world's points of interest.

```kotlin
// Add a marker
val marker = world.addMapMarker {
    id = "unique_id"           // Optional, auto-generated if not set
    name = "Display Name"       // Shown on hover
    icon = "Spawn.png"          // Icon asset path
    position(x, y, z)           // World coordinates
    rotation(yaw)               // Optional rotation
    contextAction("Action", "/command")  // Right-click menu
}

// Remove a marker
world.removeMapMarker("unique_id")

// Clear all markers
world.clearMapMarkers()

// Get all markers
val markers = world.getMapMarkers()
```

**Built-in Icon Assets:**
- `Spawn.png` - Spawn point marker
- `Home.png` - Home/respawn point
- `Death.png` - Death location
- `Player.png` - Player position
- `Prefab.png` - Point of interest

### Personal Markers

Personal markers are only visible to a specific player and persist in their player data.

```kotlin
// Add a personal marker
player.addPersonalMapMarker {
    name = "My Secret Spot"
    icon = "Star.png"
    position(player.position)
}

// Remove a personal marker
player.removePersonalMapMarker("marker_id")

// Clear all personal markers
player.clearPersonalMapMarkers()

// Get all personal markers
val myMarkers = player.getPersonalMapMarkers()
```

### Dynamic Marker Providers

Marker providers supply dynamic markers that update as the player moves through the world.

```kotlin
// Register a provider for tracking enemies
world.registerMarkerProvider("enemies") { world, player, radius, chunkX, chunkZ ->
    findNearbyEnemies(player, radius * 32).map { enemy ->
        MapMarkerBuilder().apply {
            id = "enemy_${enemy.uuid}"
            name = enemy.name
            icon = "Enemy.png"
            position(enemy.position)
        }
    }
}

// Register a provider for quest objectives
world.registerMarkerProvider("quests") { world, player, _, _, _ ->
    getActiveQuests(player).mapNotNull { quest ->
        quest.targetPosition?.let { pos ->
            MapMarkerBuilder().apply {
                id = "quest_${quest.id}"
                name = quest.objectiveName
                icon = "Quest.png"
                position(pos)
                contextAction("Track", "/quest track ${quest.id}")
            }
        }
    }
}

// Unregister a provider
world.unregisterMarkerProvider("enemies")

// Get all provider keys
val providers = world.getMarkerProviderKeys()
```

**Provider Helper Functions:**

```kotlin
// Create a provider with a lambda
val provider = markerProvider { world, player, radius, x, z ->
    // Return list of MapMarkerBuilder
    listOf(...)
}

// Create a static provider (always returns same markers)
val staticProvider = staticMarkerProvider(
    { id = "marker1"; name = "Static 1"; position(0.0, 64.0, 0.0) },
    { id = "marker2"; name = "Static 2"; position(100.0, 64.0, 100.0) }
)
```

## Map Images

Create custom map images for overlays or replacements.

```kotlin
// Create a simple colored image
val image = mapImage(32, 32) {
    fill(MapColors.TRANSPARENT)
    drawRect(0, 0, 16, 16, MapColors.RED)
    drawRect(16, 0, 16, 16, MapColors.BLUE)
    drawRect(0, 16, 16, 16, MapColors.GREEN)
    drawRect(16, 16, 16, 16, MapColors.YELLOW)
}

// Draw shapes
val patternImage = mapImage {
    fill(MapColors.GRASS_GREEN)
    drawRectOutline(4, 4, 24, 24, MapColors.DARK_GREEN)
    drawHLine(0, 16, 32, MapColors.WHITE)
    drawVLine(16, 0, 32, MapColors.WHITE)
}

// Overlay images with alpha blending
val combined = mapImageBuilder {
    fill(MapColors.WATER)
}.apply {
    overlay(patternImage, alpha = 0.5f)
}.build()
```

**MapImageBuilder Methods:**

| Method | Description |
|--------|-------------|
| `fill(color)` | Fill entire image with color |
| `setPixel(x, y, color)` | Set single pixel |
| `getPixel(x, y)` | Get pixel color |
| `drawRect(x, y, w, h, color)` | Draw filled rectangle |
| `drawRectOutline(x, y, w, h, color)` | Draw rectangle outline |
| `drawHLine(x, y, length, color)` | Draw horizontal line |
| `drawVLine(x, y, length, color)` | Draw vertical line |
| `drawImage(pixels, width, offsetX, offsetY)` | Copy pixel array |
| `overlay(other, alpha, offsetX, offsetY)` | Blend another image |
| `clear()` | Clear to transparent |

## Map Settings

Configure world map behavior and appearance.

```kotlin
// Using the settings builder (for reference)
val settings = mapSettings {
    enabled = true
    defaultZoom = 64f
    zoomRange(min = 8f, max = 128f, default = 64f)
    allowTeleportToMarkers = true
    allowTeleportToCoordinates = false

    // Custom biome colors
    biomeColor(1, 0, "Zone Alpha", "Forest", MapColors.FOREST_GREEN)
    biomeColor(2, 0, "Zone Alpha", "Desert", MapColors.DESERT)
}

// World-level controls
world.setMapEnabled(true)
world.broadcastMapSettings()
world.refreshMapImages()

// Player-level controls
player.setMapTeleportToCoordinates(false)
player.setMapTeleportToMarkers(true)
player.setMapViewRadius(128)  // Override view radius
player.refreshMap()
```

## Map Colors

Use the `MapColors` object for predefined colors and color utilities.

```kotlin
import com.github.ssquadteam.talelib.worldmap.MapColors

// Predefined colors
MapColors.RED
MapColors.GREEN
MapColors.BLUE
MapColors.TRANSPARENT
MapColors.FOREST_GREEN
MapColors.WATER
MapColors.SAND
// ... many more

// Create custom colors
val custom = MapColors.rgb(128, 64, 255)
val withAlpha = MapColors.rgba(255, 0, 0, 128)  // Semi-transparent red
val fromFloat = MapColors.rgbf(0.5f, 0.25f, 1.0f)

// Modify colors
val transparent = MapColors.withAlpha(MapColors.BLUE, 0.5f)
val darker = MapColors.darken(MapColors.GREEN, 0.7f)
val lighter = MapColors.lighten(MapColors.RED, 0.3f)
val blended = MapColors.lerp(MapColors.BLUE, MapColors.RED, 0.5f)

// Parse/format hex
val fromHex = MapColors.fromHex("#FF5500")
val toHex = MapColors.toHex(MapColors.PURPLE)  // "#8000FF"

// Extract components
val r = MapColors.red(color)
val g = MapColors.green(color)
val b = MapColors.blue(color)
val a = MapColors.alpha(color)
```

## Zone Discovery

Control zone discovery for players.

```kotlin
// Discover a zone
player.discoverZone("forest_region")

// Undiscover a zone
player.undiscoverZone("forest_region")

// Get discovered zones
val discovered = player.getDiscoveredZones()

// Get current zone/biome
val zoneName = player.getCurrentZoneName()
val biomeName = player.getCurrentBiomeName()
```

## API Reference

### World Extensions

| Function | Description |
|----------|-------------|
| `world.mapManager` | Get the WorldMapManager |
| `world.isMapEnabled` | Check if map is enabled |
| `world.addMapMarker { }` | Add a world marker |
| `world.removeMapMarker(id)` | Remove a marker by ID |
| `world.clearMapMarkers()` | Clear all markers |
| `world.getMapMarkers()` | Get all markers |
| `world.registerMarkerProvider(key, provider)` | Register dynamic provider |
| `world.unregisterMarkerProvider(key)` | Unregister provider |
| `world.setMapEnabled(enabled)` | Enable/disable map |
| `world.refreshMapImages()` | Force image regeneration |
| `world.broadcastMapSettings()` | Send settings to players |

### Player Extensions

| Function | Description |
|----------|-------------|
| `player.mapTracker` | Get the WorldMapTracker |
| `player.addPersonalMapMarker { }` | Add personal marker |
| `player.removePersonalMapMarker(id)` | Remove personal marker |
| `player.clearPersonalMapMarkers()` | Clear personal markers |
| `player.getPersonalMapMarkers()` | Get personal markers |
| `player.refreshMap()` | Refresh map view |
| `player.setMapViewRadius(radius)` | Set view radius |
| `player.getMapViewRadius()` | Get effective view radius |
| `player.setMapTeleportToCoordinates(allow)` | Control coordinate teleport |
| `player.setMapTeleportToMarkers(allow)` | Control marker teleport |
| `player.discoverZone(name)` | Discover a zone |
| `player.undiscoverZone(name)` | Undiscover a zone |
| `player.getDiscoveredZones()` | Get discovered zones |
| `player.getCurrentZoneName()` | Get current zone |
| `player.getCurrentBiomeName()` | Get current biome |

### Builder Classes

- `MapMarkerBuilder` - Build map markers with DSL
- `MapImageBuilder` - Create custom map images
- `MapSettingsBuilder` - Configure map settings
- `TaleMarkerProvider` - Interface for dynamic markers

### Top-Level Functions

| Function | Description |
|----------|-------------|
| `mapMarker { }` | Create a standalone MapMarker |
| `mapImage(w, h) { }` | Create a MapImage |
| `mapImageBuilder(w, h) { }` | Create a MapImageBuilder |
| `mapSettings { }` | Create UpdateWorldMapSettings |
| `markerProvider { }` | Create a TaleMarkerProvider |
| `staticMarkerProvider(...)` | Create static marker provider |
