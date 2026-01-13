@file:JvmName("CameraExtensions")

package com.github.ssquadteam.talelib.camera

import com.hypixel.hytale.protocol.ClientCameraView
import com.hypixel.hytale.protocol.ServerCameraSettings
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera
import com.hypixel.hytale.server.core.universe.PlayerRef

fun PlayerRef.camera(settings: ServerCameraSettings, locked: Boolean = false) {
    val packet = SetServerCamera(ClientCameraView.Custom, locked, settings)
    this.getPacketHandler().writeNoCache(packet)
}

fun PlayerRef.camera(locked: Boolean = false, builder: CameraBuilder.() -> Unit) {
    camera(cameraSettings(builder), locked)
}

fun PlayerRef.applyPreset(preset: ServerCameraSettings, locked: Boolean = false) {
    camera(preset, locked)
}

fun PlayerRef.resetCamera() {
    val packet = SetServerCamera(ClientCameraView.Custom, false, null)
    this.getPacketHandler().writeNoCache(packet)
}

fun PlayerRef.firstPersonCamera() {
    val packet = SetServerCamera(ClientCameraView.FirstPerson, false, null)
    this.getPacketHandler().writeNoCache(packet)
}

fun PlayerRef.thirdPersonCamera(distance: Float = 10f) {
    camera(CameraPresets.thirdPerson(distance))
}

fun PlayerRef.lockCamera(settings: ServerCameraSettings) {
    camera(settings, locked = true)
}

fun PlayerRef.lockCamera(builder: CameraBuilder.() -> Unit) {
    camera(locked = true, builder = builder)
}

fun PlayerRef.unlockCamera() = resetCamera()

fun PlayerRef.topDownCamera(distance: Float = 20f) {
    camera(CameraPresets.topDown(distance))
}

fun PlayerRef.sideScrollerCamera(distance: Float = 15f) {
    camera(CameraPresets.sideScroller(distance))
}

fun PlayerRef.isometricCamera() {
    camera(CameraPresets.ISOMETRIC)
}

fun PlayerRef.cinematicCamera() {
    camera(CameraPresets.CINEMATIC)
}

fun PlayerRef.overShoulderCamera() {
    camera(CameraPresets.OVER_SHOULDER)
}

fun PlayerRef.transitionCamera(settings: ServerCameraSettings) {
    camera(settings)
}

fun PlayerRef.snapCamera(builder: CameraBuilder.() -> Unit) {
    camera {
        builder()
        positionSmoothing = 1f
        rotationSmoothing = 1f
    }
}
