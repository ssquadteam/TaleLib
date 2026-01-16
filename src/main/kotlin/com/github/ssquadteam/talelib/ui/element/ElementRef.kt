@file:JvmName("ElementRefKt")

package com.github.ssquadteam.talelib.ui.element

/**
 * Reference to a UI element for property access.
 *
 * Hytale UI selectors:
 * - Element IDs use # prefix: #title, #myButton
 * - Properties use capital letters: Text, Value, Visible
 * - Full selector: #elementId.PropertyName
 */
@JvmInline
value class ElementRef(val selector: String) {
    // Common properties (capital letters to match Hytale UI)
    val Text: String get() = "$selector.Text"
    val Value: String get() = "$selector.Value"
    val Visible: String get() = "$selector.Visible"
    val Enabled: String get() = "$selector.Enabled"
    val Progress: String get() = "$selector.Progress"
    val MaxProgress: String get() = "$selector.MaxProgress"
    val Color: String get() = "$selector.Color"
    val Alpha: String get() = "$selector.Alpha"
    val Icon: String get() = "$selector.Icon"
    val Image: String get() = "$selector.Image"

    /**
     * Access a custom property by name
     */
    fun property(name: String): String = "$selector.$name"

    /**
     * Get a child element reference
     */
    fun child(childId: String): ElementRef = ElementRef("$selector.$childId")

    /**
     * Get element at index (for list containers)
     */
    operator fun get(index: Int): ElementRef = ElementRef("$selector[$index]")

    override fun toString(): String = selector
}

/**
 * Create an element reference with # prefix
 */
fun element(id: String): ElementRef = ElementRef("#$id")

/**
 * Create a nested element reference
 */
fun element(parent: String, child: String): ElementRef = ElementRef("#$parent.$child")

/**
 * Create an element reference from raw selector (no # prefix added)
 */
fun rawElement(selector: String): ElementRef = ElementRef(selector)

object Elements {
    val ROOT = ElementRef("root")

    /**
     * Get element at index in a list container
     */
    fun list(id: String, index: Int): ElementRef = ElementRef("#$id[$index]")

    /**
     * Dynamic list selector for appending
     */
    fun dynamicList(id: String): String = "#$id[]"
}
