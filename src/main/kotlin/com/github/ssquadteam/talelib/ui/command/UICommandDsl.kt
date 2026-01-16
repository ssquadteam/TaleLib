@file:JvmName("UICommandDslKt")

package com.github.ssquadteam.talelib.ui.command

import com.github.ssquadteam.talelib.ui.element.ElementRef
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder

/**
 * DSL for building UI commands.
 *
 * Hytale UI selectors use:
 * - # prefix for element IDs: #title, #myButton
 * - Capital property names: Text, Value, Visible
 * - Full format: #elementId.PropertyName
 */
class UICommandDsl(private val builder: UICommandBuilder) {

    /**
     * Ensure element ID has # prefix
     */
    private fun ensureSelector(elementId: String): String =
        if (elementId.startsWith("#")) elementId else "#$elementId"

    fun append(uiPath: String): UICommandDsl {
        builder.append(uiPath)
        return this
    }

    fun set(property: String, value: String): UICommandDsl {
        builder.set(property, value)
        return this
    }

    fun set(property: String, value: Int): UICommandDsl {
        builder.set(property, value)
        return this
    }

    fun set(property: String, value: Float): UICommandDsl {
        builder.set(property, value)
        return this
    }

    fun set(property: String, value: Double): UICommandDsl {
        builder.set(property, value)
        return this
    }

    fun set(property: String, value: Boolean): UICommandDsl {
        builder.set(property, value)
        return this
    }

    // Text property
    fun text(elementId: String, value: String): UICommandDsl {
        builder.set("${ensureSelector(elementId)}.Text", value)
        return this
    }

    fun text(element: ElementRef, value: String): UICommandDsl {
        builder.set(element.Text, value)
        return this
    }

    // Visible property
    fun visible(elementId: String, visible: Boolean): UICommandDsl {
        builder.set("${ensureSelector(elementId)}.Visible", visible)
        return this
    }

    fun visible(element: ElementRef, visible: Boolean): UICommandDsl {
        builder.set(element.Visible, visible)
        return this
    }

    // Enabled property
    fun enabled(elementId: String, enabled: Boolean): UICommandDsl {
        builder.set("${ensureSelector(elementId)}.Enabled", enabled)
        return this
    }

    fun enabled(element: ElementRef, enabled: Boolean): UICommandDsl {
        builder.set(element.Enabled, enabled)
        return this
    }

    // Progress properties
    fun progress(elementId: String, current: Int, max: Int): UICommandDsl {
        val selector = ensureSelector(elementId)
        builder.set("$selector.Progress", current)
        builder.set("$selector.MaxProgress", max)
        return this
    }

    fun progress(element: ElementRef, current: Int, max: Int): UICommandDsl {
        builder.set(element.Progress, current)
        builder.set(element.MaxProgress, max)
        return this
    }

    fun progressFloat(elementId: String, current: Float, max: Float): UICommandDsl {
        val selector = ensureSelector(elementId)
        builder.set("$selector.Progress", current)
        builder.set("$selector.MaxProgress", max)
        return this
    }

    fun progressFloat(element: ElementRef, current: Float, max: Float): UICommandDsl {
        builder.set(element.Progress, current)
        builder.set(element.MaxProgress, max)
        return this
    }

    // Color property
    fun color(elementId: String, r: Int, g: Int, b: Int, a: Int = 255): UICommandDsl {
        builder.set("${ensureSelector(elementId)}.Color", arrayOf(r, g, b, a))
        return this
    }

    fun color(element: ElementRef, r: Int, g: Int, b: Int, a: Int = 255): UICommandDsl {
        builder.set(element.Color, arrayOf(r, g, b, a))
        return this
    }

    // Alpha property
    fun alpha(elementId: String, alpha: Float): UICommandDsl {
        builder.set("${ensureSelector(elementId)}.Alpha", alpha)
        return this
    }

    fun alpha(element: ElementRef, alpha: Float): UICommandDsl {
        builder.set(element.Alpha, alpha)
        return this
    }

    // Icon property
    fun icon(elementId: String, iconPath: String): UICommandDsl {
        builder.set("${ensureSelector(elementId)}.Icon", iconPath)
        return this
    }

    fun icon(element: ElementRef, iconPath: String): UICommandDsl {
        builder.set(element.Icon, iconPath)
        return this
    }

    // Image property
    fun image(elementId: String, imagePath: String): UICommandDsl {
        builder.set("${ensureSelector(elementId)}.Image", imagePath)
        return this
    }

    fun image(element: ElementRef, imagePath: String): UICommandDsl {
        builder.set(element.Image, imagePath)
        return this
    }

    // Value property (for inputs)
    fun value(elementId: String, value: String): UICommandDsl {
        builder.set("${ensureSelector(elementId)}.Value", value)
        return this
    }

    fun value(element: ElementRef, value: String): UICommandDsl {
        builder.set(element.Value, value)
        return this
    }

    fun value(elementId: String, value: Int): UICommandDsl {
        builder.set("${ensureSelector(elementId)}.Value", value)
        return this
    }

    fun value(elementId: String, value: Float): UICommandDsl {
        builder.set("${ensureSelector(elementId)}.Value", value)
        return this
    }

    // Convenience methods
    fun show(elementId: String): UICommandDsl = visible(elementId, true)
    fun hide(elementId: String): UICommandDsl = visible(elementId, false)
    fun show(element: ElementRef): UICommandDsl = visible(element, true)
    fun hide(element: ElementRef): UICommandDsl = visible(element, false)

    fun enable(elementId: String): UICommandDsl = enabled(elementId, true)
    fun disable(elementId: String): UICommandDsl = enabled(elementId, false)
    fun enable(element: ElementRef): UICommandDsl = enabled(element, true)
    fun disable(element: ElementRef): UICommandDsl = enabled(element, false)

    /**
     * Clear all children from a container element
     */
    fun clear(elementId: String): UICommandDsl {
        builder.clear(ensureSelector(elementId))
        return this
    }

    fun clear(element: ElementRef): UICommandDsl {
        builder.clear(element.selector)
        return this
    }

    /**
     * Append a template to a container element
     */
    fun appendTo(containerId: String, templatePath: String): UICommandDsl {
        builder.append(ensureSelector(containerId), templatePath)
        return this
    }

    fun appendTo(container: ElementRef, templatePath: String): UICommandDsl {
        builder.append(container.selector, templatePath)
        return this
    }

    fun build(): UICommandBuilder = builder
}

fun UICommandBuilder.dsl(block: UICommandDsl.() -> Unit): UICommandBuilder {
    UICommandDsl(this).apply(block)
    return this
}

fun uiCommand(builder: UICommandBuilder, block: UICommandDsl.() -> Unit): UICommandBuilder {
    return builder.dsl(block)
}
