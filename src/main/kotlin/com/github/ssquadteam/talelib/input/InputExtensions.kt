@file:JvmName("InputExtensions")

package com.github.ssquadteam.talelib.input

import com.hypixel.hytale.math.vector.Vector3i
import com.hypixel.hytale.protocol.MouseButtonState
import com.hypixel.hytale.protocol.MouseButtonType
import com.hypixel.hytale.server.core.entity.Entity
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseMotionEvent

val PlayerMouseButtonEvent.buttonType: MouseButtonType
    get() = this.mouseButton.mouseButtonType

val PlayerMouseButtonEvent.buttonState: MouseButtonState
    get() = this.mouseButton.state

val PlayerMouseButtonEvent.clickCount: Int
    get() = this.mouseButton.clicks.toInt()

val PlayerMouseButtonEvent.isPressed: Boolean
    get() = this.mouseButton.state == MouseButtonState.Pressed

val PlayerMouseButtonEvent.isReleased: Boolean
    get() = this.mouseButton.state == MouseButtonState.Released

val PlayerMouseButtonEvent.isLeftClick: Boolean
    get() = this.mouseButton.mouseButtonType == MouseButtonType.Left

val PlayerMouseButtonEvent.isRightClick: Boolean
    get() = this.mouseButton.mouseButtonType == MouseButtonType.Right

val PlayerMouseButtonEvent.isMiddleClick: Boolean
    get() = this.mouseButton.mouseButtonType == MouseButtonType.Middle

val PlayerMouseButtonEvent.isDoubleClick: Boolean
    get() = this.mouseButton.clicks >= 2

val PlayerMouseButtonEvent.hasTargetBlock: Boolean
    get() = this.targetBlock != null

val PlayerMouseButtonEvent.hasTargetEntity: Boolean
    get() = this.targetEntity != null

val PlayerMouseButtonEvent.hasItemInHand: Boolean
    get() = this.itemInHand != null

fun PlayerMouseButtonEvent.isLeftPress(): Boolean = isLeftClick && isPressed

fun PlayerMouseButtonEvent.isRightPress(): Boolean = isRightClick && isPressed

fun PlayerMouseButtonEvent.isLeftRelease(): Boolean = isLeftClick && isReleased

fun PlayerMouseButtonEvent.isRightRelease(): Boolean = isRightClick && isReleased

val PlayerMouseButtonEvent.normalizedScreenPoint: Pair<Float, Float>?
    get() = this.screenPoint?.let { Pair(it.x, it.y) }

val PlayerMouseMotionEvent.deltaX: Int
    get() = this.mouseMotion?.relativeMotion?.x ?: 0

val PlayerMouseMotionEvent.deltaY: Int
    get() = this.mouseMotion?.relativeMotion?.y ?: 0

val PlayerMouseMotionEvent.motionDelta: Pair<Int, Int>?
    get() = this.mouseMotion?.relativeMotion?.let { Pair(it.x, it.y) }

val PlayerMouseMotionEvent.hasButtonsHeld: Boolean
    get() = this.mouseMotion?.mouseButtonType?.isNotEmpty() == true

val PlayerMouseMotionEvent.heldButtons: List<MouseButtonType>
    get() = this.mouseMotion?.mouseButtonType?.toList() ?: emptyList()

fun PlayerMouseMotionEvent.isButtonHeld(button: MouseButtonType): Boolean =
    this.mouseMotion?.mouseButtonType?.contains(button) == true

val PlayerMouseMotionEvent.isLeftHeld: Boolean
    get() = isButtonHeld(MouseButtonType.Left)

val PlayerMouseMotionEvent.isRightHeld: Boolean
    get() = isButtonHeld(MouseButtonType.Right)

val PlayerMouseMotionEvent.isDragging: Boolean
    get() = hasButtonsHeld && (deltaX != 0 || deltaY != 0)

val PlayerMouseMotionEvent.hasTargetBlock: Boolean
    get() = this.targetBlock != null

val PlayerMouseMotionEvent.hasTargetEntity: Boolean
    get() = this.targetEntity != null
