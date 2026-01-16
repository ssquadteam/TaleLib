# Sound System

TaleLib provides extensions for playing 2D and 3D sounds.

## 2D Sounds (UI/Ambient)

```kotlin
import com.github.ssquadteam.talelib.sound.*

// Simple 2D sound
playerRef.playSound2d("hytale:ui/click")

// With options
playerRef.playSound2d(
    "hytale:ambient/wind",
    SoundCategory.AMBIENT,
    volume = 0.8f,
    pitch = 1.2f
)

// UI sounds shortcut
playerRef.playUISound("hytale:ui/success")
```

## 3D Positional Sounds

```kotlin
// Play at specific position
playerRef.playSound3d("hytale:explosion", x = 100.0, y = 64.0, z = 200.0)

// With full options
playerRef.playSound3d(
    "hytale:footstep",
    x, y, z,
    SoundCategory.SFX,
    volume = 1f,
    pitch = 1f
)

// Play at player's position
playerRef.playSoundAtSelf("hytale:levelup")
```

## Sound Builder DSL

```kotlin
playerRef.playSound {
    sound("hytale:magic/spell_cast")
    at(x, y, z)
    volume(0.9f)
    pitch(1.1f)
    sfx()  // SoundCategory.SFX
}

// With shortcuts
playerRef.playSound {
    sound("hytale:coin")
    atSelf()
    randomPitch(0.9f, 1.1f)
}
```

## Sound Categories

```kotlin
SoundCategory.MASTER
SoundCategory.MUSIC
SoundCategory.SFX
SoundCategory.AMBIENT
SoundCategory.UI
SoundCategory.VOICE
```

## Example: UI Feedback

```kotlin
fun onButtonClick(playerRef: PlayerRef, success: Boolean) {
    if (success) {
        playerRef.playUISound("hytale:ui/success")
    } else {
        playerRef.playUISound("hytale:ui/error")
    }
}
```

## Example: Combat Sounds

```kotlin
fun onPlayerHit(playerRef: PlayerRef, attacker: PlayerRef, damage: Int) {
    val pos = playerRef.position ?: return

    // Play hit sound at damage location
    attacker.playSound {
        sound("hytale:combat/hit")
        at(pos.x, pos.y, pos.z)
        volume(0.8f)
        randomPitch(0.9f, 1.1f)
        sfx()
    }

    // Play pain sound to victim
    playerRef.playSound {
        sound("hytale:player/pain")
        atSelf()
        volume(1.0f)
    }
}
```

## Example: Ambient Sound Loop

```kotlin
class AmbientPlugin(init: JavaPluginInit) : TalePlugin(init) {

    override fun onStart() {
        taleScheduler.repeat(interval = 30.seconds) {
            onlinePlayers.forEach { playerRef ->
                // Random ambient sound
                if (Random.nextFloat() < 0.3f) {
                    playerRef.playSound {
                        sound("hytale:ambient/wind")
                        atSelf()
                        volume(0.3f)
                        randomPitch(0.8f, 1.2f)
                    }
                }
            }
        }
    }
}
```

## Example: Level Up Effect

```kotlin
fun onLevelUp(playerRef: PlayerRef, newLevel: Int) {
    // Play ascending notes
    repeat(3) { i ->
        taleScheduler.delayTicks(i * 5L) {
            playerRef.playSound {
                sound("hytale:ui/note")
                atSelf()
                pitch(1.0f + (i * 0.2f))  // Ascending pitch
                volume(0.8f)
            }
        }
    }

    // Final fanfare
    taleScheduler.delayTicks(20) {
        playerRef.playSound {
            sound("hytale:ui/fanfare")
            atSelf()
            volume(1.0f)
        }
    }
}
```
