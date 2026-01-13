package com.github.ssquadteam.talelib.camera

import com.hypixel.hytale.protocol.Direction
import com.hypixel.hytale.protocol.MouseInputType
import com.hypixel.hytale.protocol.MovementForceRotationType
import com.hypixel.hytale.protocol.PositionDistanceOffsetType
import com.hypixel.hytale.protocol.RotationType
import com.hypixel.hytale.protocol.ServerCameraSettings
import com.hypixel.hytale.protocol.Vector3f

class CameraBuilder {
    private val settings = ServerCameraSettings()

    var distance: Float
        get() = settings.distance
        set(value) { settings.distance = value }

    var firstPerson: Boolean
        get() = settings.isFirstPerson
        set(value) { settings.isFirstPerson = value }

    var showCursor: Boolean
        get() = settings.displayCursor
        set(value) { settings.displayCursor = value }

    var positionSmoothing: Float
        get() = settings.positionLerpSpeed
        set(value) { settings.positionLerpSpeed = value }

    var rotationSmoothing: Float
        get() = settings.rotationLerpSpeed
        set(value) { settings.rotationLerpSpeed = value }

    var eyeOffset: Boolean
        get() = settings.eyeOffset
        set(value) { settings.eyeOffset = value }

    fun smoothing(position: Float = 0.2f, rotation: Float = 0.2f) {
        settings.positionLerpSpeed = position
        settings.rotationLerpSpeed = rotation
    }

    fun thirdPerson(distance: Float = 10f) {
        settings.isFirstPerson = false
        settings.distance = distance
    }

    fun firstPerson() {
        settings.isFirstPerson = true
    }

    fun rotationType(type: RotationType) {
        settings.rotationType = type
    }

    fun rotation(yaw: Float, pitch: Float, roll: Float = 0f) {
        settings.rotationType = RotationType.Custom
        settings.rotation = Direction(yaw, pitch, roll)
    }

    fun lookDown() {
        rotation(0f, -1.5707964f, 0f)
    }

    fun lookFromSide() {
        settings.rotationType = RotationType.Custom
    }

    fun movementRotationType(type: MovementForceRotationType) {
        settings.movementForceRotationType = type
    }

    fun movementMultiplier(x: Float = 1f, y: Float = 1f, z: Float = 1f) {
        settings.movementMultiplier = Vector3f(x, y, z)
    }

    fun lock2D() {
        movementMultiplier(1f, 1f, 0f)
    }

    fun mouseInput(type: MouseInputType) {
        settings.mouseInputType = type
    }

    fun lookAtPlane(normal: Vector3f) {
        settings.mouseInputType = MouseInputType.LookAtPlane
        settings.planeNormal = normal
    }

    fun lookAtGround() = lookAtPlane(Vector3f(0f, 1f, 0f))
    fun lookAtSide() = lookAtPlane(Vector3f(0f, 0f, 1f))

    fun distanceOffsetType(type: PositionDistanceOffsetType) {
        settings.positionDistanceOffsetType = type
    }

    fun enableWallCollision() {
        settings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffsetRaycast
    }

    fun build(): ServerCameraSettings = settings
}

fun cameraSettings(block: CameraBuilder.() -> Unit): ServerCameraSettings {
    val builder = CameraBuilder()
    builder.block()
    return builder.build()
}
