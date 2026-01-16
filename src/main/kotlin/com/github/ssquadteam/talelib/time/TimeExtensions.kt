package com.github.ssquadteam.talelib.time

import com.hypixel.hytale.component.Store
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.time.Instant
import java.time.LocalDateTime

/**
 * Extension functions for time management.
 * Source: com/hypixel/hytale/server/core/modules/time/WorldTimeResource.java
 */

// ============================================
// PlayerRef Helper Extensions
// ============================================

private fun PlayerRef.getWorld(): World? {
    val ref = this.reference ?: return null
    return (ref.store.externalData as? EntityStore)?.world
}

// ============================================
// Time Constants
// ============================================

object TimeConstants {
    const val SECONDS_PER_DAY = 86400

    const val HOURS_PER_DAY = 24

    const val DAYTIME_PORTION = 0.6f

    const val DAYTIME_SECONDS = (SECONDS_PER_DAY * DAYTIME_PORTION).toInt()

    const val NIGHTTIME_SECONDS = SECONDS_PER_DAY - DAYTIME_SECONDS

    const val TIME_DILATION_MIN = 0.01f

    const val TIME_DILATION_MAX = 4.0f

    const val DAWN_PROGRESS = 0.25f

    const val NOON_PROGRESS = 0.5f

    const val DUSK_PROGRESS = 0.75f

    const val MIDNIGHT_PROGRESS = 0.0f
}
enum class TimeOfDay(val progress: Float, val hour: Int) {
    MIDNIGHT(0.0f, 0),
    DAWN(0.25f, 6),
    MORNING(0.333f, 8),
    NOON(0.5f, 12),
    AFTERNOON(0.625f, 15),
    DUSK(0.75f, 18),
    EVENING(0.833f, 20),
    NIGHT(0.916f, 22)
}

// ============================================
// World Time Extensions
// ============================================

fun World.getTimeResource(): WorldTimeResource? {
    return try {
        this.entityStore.store.getResource(WorldTimeResource.getResourceType())
    } catch (e: Exception) {
        null
    }
}

fun World.getGameTime(): Instant? {
    return getTimeResource()?.gameTime
}

fun World.getGameDateTime(): LocalDateTime? {
    return getTimeResource()?.gameDateTime
}

fun World.getCurrentHour(): Int? {
    return getTimeResource()?.currentHour
}

fun World.getDayProgress(): Float? {
    return getTimeResource()?.dayProgress
}

fun World.getSunlightFactor(): Double? {
    return getTimeResource()?.sunlightFactor
}

fun World.getMoonPhase(): Int? {
    return getTimeResource()?.moonPhase
}

fun World.getSunDirection(): Vector3f? {
    return getTimeResource()?.sunDirection
}

fun World.isDaytime(): Boolean {
    val sunlight = getSunlightFactor() ?: return false
    return sunlight > 0.5
}

fun World.isNighttime(): Boolean {
    return !isDaytime()
}

fun World.isTimeWithinRange(minProgress: Double, maxProgress: Double): Boolean {
    return getTimeResource()?.isDayTimeWithinRange(minProgress, maxProgress) ?: false
}

fun World.isMoonPhaseWithinRange(minPhase: Int, maxPhase: Int): Boolean {
    return getTimeResource()?.isMoonPhaseWithinRange(this, minPhase, maxPhase) ?: false
}

fun World.isFullMoon(): Boolean {
    return isMoonPhaseWithinRange(4, 4)
}

fun World.isNewMoon(): Boolean {
    return isMoonPhaseWithinRange(0, 0)
}

// ============================================
// Time Manipulation
// ============================================

fun World.setGameTime(instant: Instant): Boolean {
    val timeResource = getTimeResource() ?: return false
    return try {
        timeResource.setGameTime(instant, this, this.entityStore.store)
        true
    } catch (e: Exception) {
        false
    }
}

fun World.setDayProgress(progress: Double): Boolean {
    val timeResource = getTimeResource() ?: return false
    return try {
        timeResource.setDayTime(progress.coerceIn(0.0, 1.0), this, this.entityStore.store)
        true
    } catch (e: Exception) {
        false
    }
}

fun World.setHour(hour: Float): Boolean {
    val progress = (hour / 24f).coerceIn(0f, 1f).toDouble()
    return setDayProgress(progress)
}

fun World.setTimeOfDay(timeOfDay: TimeOfDay): Boolean {
    return setDayProgress(timeOfDay.progress.toDouble())
}

fun World.setDawn(): Boolean = setTimeOfDay(TimeOfDay.DAWN)

fun World.setNoon(): Boolean = setTimeOfDay(TimeOfDay.NOON)

fun World.setDusk(): Boolean = setTimeOfDay(TimeOfDay.DUSK)

fun World.setMidnight(): Boolean = setTimeOfDay(TimeOfDay.MIDNIGHT)

fun World.setTimeDilation(multiplier: Float): Boolean {
    return try {
        val clamped = multiplier.coerceIn(TimeConstants.TIME_DILATION_MIN, TimeConstants.TIME_DILATION_MAX)
        World.setTimeDilation(clamped, this.entityStore.store)
        true
    } catch (e: Exception) {
        false
    }
}

fun World.resetTimeDilation(): Boolean {
    return setTimeDilation(1.0f)
}

fun World.fastForward(): Boolean {
    return setTimeDilation(2.0f)
}

fun World.slowMotion(): Boolean {
    return setTimeDilation(0.5f)
}

// ============================================
// Player Time Extensions
// ============================================

fun PlayerRef.sendTime() {
    val world = getWorld() ?: return
    val timeResource = world.getTimeResource() ?: return
    timeResource.sendTimePackets(this)
}

// ============================================
// Utility Functions
// ============================================

fun hourToProgress(hour: Float): Float {
    return (hour / 24f).coerceIn(0f, 1f)
}

fun progressToHour(progress: Float): Float {
    return (progress * 24f).coerceIn(0f, 24f)
}

fun formatTime(hour: Int, minute: Int = 0): String {
    return String.format("%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))
}

fun formatDayProgress(progress: Float): String {
    val totalMinutes = (progress * 24 * 60).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return formatTime(hours, minutes)
}

fun getTimeOfDayDescription(progress: Float): String {
    return when {
        progress < 0.25f -> "Night"
        progress < 0.333f -> "Dawn"
        progress < 0.5f -> "Morning"
        progress < 0.625f -> "Noon"
        progress < 0.75f -> "Afternoon"
        progress < 0.833f -> "Dusk"
        progress < 0.916f -> "Evening"
        else -> "Night"
    }
}

fun getMoonPhaseName(phase: Int, totalPhases: Int = 8): String {
    return when (phase) {
        0 -> "New Moon"
        totalPhases / 4 -> "First Quarter"
        totalPhases / 2 -> "Full Moon"
        (totalPhases * 3) / 4 -> "Last Quarter"
        in 1 until totalPhases / 4 -> "Waxing Crescent"
        in (totalPhases / 4) + 1 until totalPhases / 2 -> "Waxing Gibbous"
        in (totalPhases / 2) + 1 until (totalPhases * 3) / 4 -> "Waning Gibbous"
        else -> "Waning Crescent"
    }
}
