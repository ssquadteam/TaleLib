# Camera System

TaleLib provides camera presets and a DSL for controlling the player's camera view.

## Camera Presets

Quick presets for common camera styles:

```kotlin
import com.github.ssquadteam.talelib.camera.*

// Apply preset
playerRef.camera(CameraPresets.TOP_DOWN)       // RTS/ARPG style
playerRef.camera(CameraPresets.SIDE_SCROLLER)  // 2D platformer style
playerRef.camera(CameraPresets.ISOMETRIC)      // Classic RPG style
playerRef.camera(CameraPresets.CINEMATIC)      // Smooth cinematic
playerRef.camera(CameraPresets.OVER_SHOULDER)  // TPS shooter style
playerRef.camera(CameraPresets.THIRD_PERSON)   // Standard third person
playerRef.camera(CameraPresets.FIRST_PERSON)   // First person
playerRef.camera(CameraPresets.FIXED)          // Fixed position
```

## Preset Functions with Distance

```kotlin
// Presets with custom distance
playerRef.topDownCamera(distance = 25f)
playerRef.sideScrollerCamera(distance = 20f)
playerRef.thirdPersonCamera(distance = 15f)

// Quick presets (default distances)
playerRef.firstPersonCamera()
playerRef.isometricCamera()
playerRef.cinematicCamera()
playerRef.overShoulderCamera()
```

## Custom Camera DSL

Build custom camera configurations:

```kotlin
playerRef.camera {
    distance = 20f
    firstPerson = false
    showCursor = true
    smoothing(position = 0.2f, rotation = 0.2f)
    lookAtGround()  // For top-down
    lock2D()        // Restrict movement to 2D plane
}
```

## CameraBuilder Methods

```kotlin
playerRef.camera {
    // Basic settings
    distance = 10f              // Camera distance from player
    firstPerson = true/false    // First person mode
    showCursor = true/false     // Display cursor
    eyeOffset = true/false      // Use eye offset

    // Smoothing
    positionSmoothing = 0.2f    // Position lerp speed
    rotationSmoothing = 0.2f    // Rotation lerp speed
    smoothing(position, rotation)  // Set both at once

    // Mode configurations
    thirdPerson(distance)       // Configure third person
    firstPerson()               // Configure first person

    // Rotation
    rotation(yaw, pitch, roll)  // Set custom rotation
    lookDown()                  // Look straight down
    lookAtGround()              // Look at ground plane
    lookAtSide()                // Look at side plane

    // Constraints
    lock2D()                    // Restrict to 2D movement
    enableWallCollision()       // Enable wall raycasting
}
```

## Lock Camera

Prevent player from changing camera:

```kotlin
// Lock with preset
playerRef.lockCamera(CameraPresets.SIDE_SCROLLER)

// Lock with custom settings
playerRef.lockCamera {
    distance = 10f
    firstPerson = false
}
```

## Unlock / Reset

```kotlin
// Allow player to change camera again
playerRef.unlockCamera()

// Reset to default camera
playerRef.resetCamera()
```

## Snap Camera

Apply camera instantly without smoothing:

```kotlin
playerRef.snapCamera {
    distance = 15f
    firstPerson = false
}
```

## Example: Game Mode Cameras

```kotlin
class GameModePlugin(init: JavaPluginInit) : TalePlugin(init) {

    fun setTopDownMode(playerRef: PlayerRef) {
        playerRef.lockCamera {
            distance = 30f
            firstPerson = false
            showCursor = true
            lookAtGround()
            smoothing(0.15f, 0.1f)
        }
    }

    fun setSideScrollerMode(playerRef: PlayerRef) {
        playerRef.lockCamera {
            distance = 20f
            firstPerson = false
            showCursor = false
            lookAtSide()
            lock2D()
        }
    }

    fun setFirstPersonMode(playerRef: PlayerRef) {
        playerRef.lockCamera {
            firstPerson = true
            showCursor = false
        }
    }

    fun resetToDefault(playerRef: PlayerRef) {
        playerRef.unlockCamera()
        playerRef.resetCamera()
    }
}
```

## Example: Cinematic Sequence

```kotlin
fun playCinematic(playerRef: PlayerRef) {
    // Start with dramatic angle
    playerRef.snapCamera {
        distance = 25f
        rotation(45f, -30f, 0f)
        firstPerson = false
    }

    taleScheduler.delay(3.seconds) {
        // Transition to close-up
        playerRef.camera {
            distance = 5f
            rotation(0f, 0f, 0f)
            smoothing(0.05f, 0.05f)  // Slow, cinematic
        }
    }

    taleScheduler.delay(6.seconds) {
        // Return control
        playerRef.unlockCamera()
    }
}
```
