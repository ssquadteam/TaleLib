@file:JvmName("UIEventDslKt")

package com.github.ssquadteam.talelib.ui.event

import com.github.ssquadteam.talelib.ui.element.ElementRef
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
import com.hypixel.hytale.server.core.ui.builder.EventData
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder

/**
 * DSL for building UI event bindings.
 *
 * Hytale UI event binding requires:
 * - Element selector with # prefix: #myButton
 * - Event type: Activating, ValueChanged, etc.
 * - EventData with key-value pairs
 */
class UIEventDsl(private val builder: UIEventBuilder) {

    /**
     * Ensure element ID has # prefix
     */
    private fun ensureSelector(elementId: String): String =
        if (elementId.startsWith("#")) elementId else "#$elementId"

    /**
     * Bind a button click event (Activating)
     */
    fun onClick(elementId: String, action: String, locksInterface: Boolean = false) {
        builder.addEventBinding(
            CustomUIEventBindingType.Activating,
            ensureSelector(elementId),
            EventData().append("Action", action),
            locksInterface
        )
    }

    /**
     * Bind a button click event using ElementRef
     */
    fun onClick(element: ElementRef, action: String, locksInterface: Boolean = false) {
        builder.addEventBinding(
            CustomUIEventBindingType.Activating,
            element.selector,
            EventData().append("Action", action),
            locksInterface
        )
    }

    /**
     * Bind a button click with element ID as action
     */
    fun onClickButton(elementId: String, locksInterface: Boolean = false) {
        onClick(elementId, elementId, locksInterface)
    }

    /**
     * Bind multiple buttons at once
     */
    fun onClickButtons(vararg elementIds: String, locksInterface: Boolean = false) {
        elementIds.forEach { onClickButton(it, locksInterface) }
    }

    /**
     * Bind a value change event (for text inputs, sliders, etc.)
     */
    fun onValueChanged(elementId: String, action: String, captureValue: Boolean = true, locksInterface: Boolean = false) {
        val eventData = EventData().append("Action", action)
        if (captureValue) {
            // Capture the element's value with @ prefix
            eventData.append("@Value", "${ensureSelector(elementId)}.Value")
        }
        builder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            ensureSelector(elementId),
            eventData,
            locksInterface
        )
    }

    /**
     * Bind a right-click event
     */
    fun onRightClick(elementId: String, action: String, locksInterface: Boolean = false) {
        builder.addEventBinding(
            CustomUIEventBindingType.RightClicking,
            ensureSelector(elementId),
            EventData().append("Action", action),
            locksInterface
        )
    }

    /**
     * Bind a double-click event
     */
    fun onDoubleClick(elementId: String, action: String, locksInterface: Boolean = false) {
        builder.addEventBinding(
            CustomUIEventBindingType.DoubleClicking,
            ensureSelector(elementId),
            EventData().append("Action", action),
            locksInterface
        )
    }

    /**
     * Bind a mouse enter event
     */
    fun onMouseEnter(elementId: String, action: String, locksInterface: Boolean = false) {
        builder.addEventBinding(
            CustomUIEventBindingType.MouseEntered,
            ensureSelector(elementId),
            EventData().append("Action", action),
            locksInterface
        )
    }

    /**
     * Bind a mouse exit event
     */
    fun onMouseExit(elementId: String, action: String, locksInterface: Boolean = false) {
        builder.addEventBinding(
            CustomUIEventBindingType.MouseExited,
            ensureSelector(elementId),
            EventData().append("Action", action),
            locksInterface
        )
    }

    /**
     * Bind a focus gained event
     */
    fun onFocusGained(elementId: String, action: String, locksInterface: Boolean = false) {
        builder.addEventBinding(
            CustomUIEventBindingType.FocusGained,
            ensureSelector(elementId),
            EventData().append("Action", action),
            locksInterface
        )
    }

    /**
     * Bind a focus lost event
     */
    fun onFocusLost(elementId: String, action: String, locksInterface: Boolean = false) {
        builder.addEventBinding(
            CustomUIEventBindingType.FocusLost,
            ensureSelector(elementId),
            EventData().append("Action", action),
            locksInterface
        )
    }

    /**
     * Generic event binding with custom EventData
     */
    fun bind(
        type: CustomUIEventBindingType,
        elementId: String,
        eventData: EventData,
        locksInterface: Boolean = false
    ) {
        builder.addEventBinding(type, ensureSelector(elementId), eventData, locksInterface)
    }

    fun build(): UIEventBuilder = builder
}

/**
 * Extension function for UIEventBuilder DSL
 */
fun UIEventBuilder.dsl(block: UIEventDsl.() -> Unit): UIEventBuilder {
    UIEventDsl(this).apply(block)
    return this
}

/**
 * Create event bindings using DSL
 */
fun uiEvents(builder: UIEventBuilder, block: UIEventDsl.() -> Unit): UIEventBuilder {
    return builder.dsl(block)
}
