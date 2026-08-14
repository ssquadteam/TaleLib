@file:JvmName("VectorExtensions")

package com.github.ssquadteam.talelib.util

import com.hypixel.hytale.protocol.Direction
import org.joml.Vector3f
import kotlin.math.floor

fun vec(x: Float, y: Float, z: Float): Vector3f = Vector3f(x, y, z)
fun vec(x: Int, y: Int, z: Int): Vector3f = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
fun vec(x: Double, y: Double, z: Double): Vector3f = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())

val ZERO: Vector3f get() = Vector3f(0f, 0f, 0f)
val ONE: Vector3f get() = Vector3f(1f, 1f, 1f)
val UP: Vector3f get() = Vector3f(0f, 1f, 0f)
val DOWN: Vector3f get() = Vector3f(0f, -1f, 0f)
val FORWARD: Vector3f get() = Vector3f(0f, 0f, 1f)
val BACK: Vector3f get() = Vector3f(0f, 0f, -1f)
val RIGHT: Vector3f get() = Vector3f(1f, 0f, 0f)
val LEFT: Vector3f get() = Vector3f(-1f, 0f, 0f)

operator fun Vector3f.plus(other: Vector3f): Vector3f = Vector3f(this).add(other)

operator fun Vector3f.minus(other: Vector3f): Vector3f = Vector3f(this).sub(other)

operator fun Vector3f.times(scalar: Float): Vector3f = Vector3f(this).mul(scalar)

operator fun Vector3f.times(scalar: Int): Vector3f = this * scalar.toFloat()

operator fun Vector3f.div(scalar: Float): Vector3f = Vector3f(this).div(scalar)

operator fun Vector3f.unaryMinus(): Vector3f = Vector3f(this).negate()

fun Vector3f.multiply(other: Vector3f): Vector3f = Vector3f(this).mul(other)

val Vector3f.length: Float get() = length()

val Vector3f.lengthSquared: Float get() = lengthSquared()

fun Vector3f.normalized(): Vector3f {
    val len = length
    return if (len > 0) this / len else ZERO
}

fun Vector3f.isZero(epsilon: Float = DEFAULT_EPSILON): Boolean = lengthSquared < epsilon * epsilon

fun Vector3f.distanceTo(other: Vector3f): Float = distance(other)

fun Vector3f.distanceSquaredTo(other: Vector3f): Float = distanceSquared(other)

fun Vector3f.clamp(min: Float, max: Float): Vector3f = Vector3f(
    x.coerceIn(min, max),
    y.coerceIn(min, max),
    z.coerceIn(min, max)
)

fun direction(yaw: Float, pitch: Float, roll: Float = 0f): Direction = Direction(yaw, pitch, roll)

fun Vector3f.toDisplayString(): String = "($x, $y, $z)"

fun Vector3f.toBlockCoords(): Triple<Int, Int, Int> = Triple(
    floor(x).toInt(),
    floor(y).toInt(),
    floor(z).toInt()
)

fun Vector3f.copy(x: Float = this.x, y: Float = this.y, z: Float = this.z): Vector3f = Vector3f(x, y, z)

fun Direction.copy(yaw: Float = this.yaw, pitch: Float = this.pitch, roll: Float = this.roll): Direction =
    Direction(yaw, pitch, roll)

private const val DEFAULT_EPSILON = 0.0001f
