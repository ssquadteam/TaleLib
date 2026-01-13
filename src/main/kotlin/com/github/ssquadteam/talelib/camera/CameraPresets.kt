package com.github.ssquadteam.talelib.camera

import com.hypixel.hytale.protocol.Direction
import com.hypixel.hytale.protocol.MouseInputType
import com.hypixel.hytale.protocol.MovementForceRotationType
import com.hypixel.hytale.protocol.PositionDistanceOffsetType
import com.hypixel.hytale.protocol.RotationType
import com.hypixel.hytale.protocol.ServerCameraSettings
import com.hypixel.hytale.protocol.Vector3f

object CameraPresets {

    val THIRD_PERSON: ServerCameraSettings
        get() = cameraSettings {
            distance = 10f
            firstPerson = false
            smoothing(0.2f, 0.2f)
        }

    val FIRST_PERSON: ServerCameraSettings
        get() = cameraSettings { firstPerson = true }

    val CINEMATIC: ServerCameraSettings
        get() = cameraSettings {
            distance = 12f
            firstPerson = false
            smoothing(0.05f, 0.05f)
        }

    val TOP_DOWN: ServerCameraSettings
        get() = ServerCameraSettings().apply {
            positionLerpSpeed = 0.2f
            rotationLerpSpeed = 0.2f
            distance = 20.0f
            displayCursor = true
            isFirstPerson = false
            movementForceRotationType = MovementForceRotationType.Custom
            eyeOffset = true
            positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset
            rotationType = RotationType.Custom
            rotation = Direction(0.0f, -1.5707964f, 0.0f)
            mouseInputType = MouseInputType.LookAtPlane
            planeNormal = Vector3f(0.0f, 1.0f, 0.0f)
        }

    val SIDE_SCROLLER: ServerCameraSettings
        get() = ServerCameraSettings().apply {
            positionLerpSpeed = 0.2f
            rotationLerpSpeed = 0.2f
            distance = 15.0f
            displayCursor = true
            isFirstPerson = false
            movementForceRotationType = MovementForceRotationType.Custom
            movementMultiplier = Vector3f(1.0f, 1.0f, 0.0f)
            eyeOffset = true
            positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset
            rotationType = RotationType.Custom
            mouseInputType = MouseInputType.LookAtPlane
            planeNormal = Vector3f(0.0f, 0.0f, 1.0f)
        }

    val ISOMETRIC: ServerCameraSettings
        get() = ServerCameraSettings().apply {
            positionLerpSpeed = 0.2f
            rotationLerpSpeed = 0.2f
            distance = 25.0f
            displayCursor = true
            isFirstPerson = false
            movementForceRotationType = MovementForceRotationType.Custom
            eyeOffset = true
            positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset
            rotationType = RotationType.Custom
            rotation = Direction(0.7854f, -0.6154f, 0.0f)
            mouseInputType = MouseInputType.LookAtPlane
            planeNormal = Vector3f(0.0f, 1.0f, 0.0f)
        }

    val OVER_SHOULDER: ServerCameraSettings
        get() = cameraSettings {
            distance = 3f
            firstPerson = false
            smoothing(0.15f, 0.1f)
            enableWallCollision()
        }

    val FIXED: ServerCameraSettings
        get() = ServerCameraSettings().apply {
            positionLerpSpeed = 0.0f
            rotationLerpSpeed = 0.0f
            isFirstPerson = false
            rotationType = RotationType.Custom
        }

    fun thirdPerson(distance: Float = 10f, smoothing: Float = 0.2f): ServerCameraSettings = cameraSettings {
        this.distance = distance
        firstPerson = false
        smoothing(smoothing, smoothing)
    }

    fun topDown(distance: Float = 20f): ServerCameraSettings = TOP_DOWN.apply { this.distance = distance }
    fun sideScroller(distance: Float = 15f): ServerCameraSettings = SIDE_SCROLLER.apply { this.distance = distance }

    fun orbital(distance: Float = 15f, yaw: Float = 0f, pitch: Float = -0.5f): ServerCameraSettings =
        ServerCameraSettings().apply {
            this.distance = distance
            isFirstPerson = false
            positionLerpSpeed = 0.2f
            rotationLerpSpeed = 0.2f
            rotationType = RotationType.Custom
            rotation = Direction(yaw, pitch, 0f)
            positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffsetRaycast
        }
}
