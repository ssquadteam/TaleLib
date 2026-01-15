@file:JvmName("UICommandDslKt")

package com.github.ssquadteam.talelib.ui.command

import com.github.ssquadteam.talelib.ui.element.ElementRef
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder

class UICommandDsl(private val builder: UICommandBuilder) {

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

    fun text(elementId: String, value: String): UICommandDsl {
        builder.set("$elementId.text", value)
        return this
    }

    fun text(element: ElementRef, value: String): UICommandDsl {
        builder.set(element.text, value)
        return this
    }

    fun visible(elementId: String, visible: Boolean): UICommandDsl {
        builder.set("$elementId.visible", visible)
        return this
    }

    fun visible(element: ElementRef, visible: Boolean): UICommandDsl {
        builder.set(element.visible, visible)
        return this
    }

    fun enabled(elementId: String, enabled: Boolean): UICommandDsl {
        builder.set("$elementId.enabled", enabled)
        return this
    }

    fun enabled(element: ElementRef, enabled: Boolean): UICommandDsl {
        builder.set(element.enabled, enabled)
        return this
    }

    fun progress(elementId: String, current: Int, max: Int): UICommandDsl {
        builder.set("$elementId.progress", current)
        builder.set("$elementId.maxProgress", max)
        return this
    }

    fun progress(element: ElementRef, current: Int, max: Int): UICommandDsl {
        builder.set(element.progress, current)
        builder.set(element.maxProgress, max)
        return this
    }

    fun progressFloat(elementId: String, current: Float, max: Float): UICommandDsl {
        builder.set("$elementId.progress", current)
        builder.set("$elementId.maxProgress", max)
        return this
    }

    fun progressFloat(element: ElementRef, current: Float, max: Float): UICommandDsl {
        builder.set(element.progress, current)
        builder.set(element.maxProgress, max)
        return this
    }

    fun color(elementId: String, r: Int, g: Int, b: Int, a: Int = 255): UICommandDsl {
        builder.set("$elementId.color", arrayOf(r, g, b, a))
        return this
    }

    fun color(element: ElementRef, r: Int, g: Int, b: Int, a: Int = 255): UICommandDsl {
        builder.set(element.color, arrayOf(r, g, b, a))
        return this
    }

    fun alpha(elementId: String, alpha: Float): UICommandDsl {
        builder.set("$elementId.alpha", alpha)
        return this
    }

    fun alpha(element: ElementRef, alpha: Float): UICommandDsl {
        builder.set(element.alpha, alpha)
        return this
    }

    fun icon(elementId: String, iconPath: String): UICommandDsl {
        builder.set("$elementId.icon", iconPath)
        return this
    }

    fun icon(element: ElementRef, iconPath: String): UICommandDsl {
        builder.set(element.icon, iconPath)
        return this
    }

    fun image(elementId: String, imagePath: String): UICommandDsl {
        builder.set("$elementId.image", imagePath)
        return this
    }

    fun image(element: ElementRef, imagePath: String): UICommandDsl {
        builder.set(element.image, imagePath)
        return this
    }

    fun show(elementId: String): UICommandDsl = visible(elementId, true)

    fun hide(elementId: String): UICommandDsl = visible(elementId, false)

    fun show(element: ElementRef): UICommandDsl = visible(element, true)

    fun hide(element: ElementRef): UICommandDsl = visible(element, false)

    fun enable(elementId: String): UICommandDsl = enabled(elementId, true)

    fun disable(elementId: String): UICommandDsl = enabled(elementId, false)

    fun enable(element: ElementRef): UICommandDsl = enabled(element, true)

    fun disable(element: ElementRef): UICommandDsl = enabled(element, false)

    fun build(): UICommandBuilder = builder
}

fun UICommandBuilder.dsl(block: UICommandDsl.() -> Unit): UICommandBuilder {
    UICommandDsl(this).apply(block)
    return this
}

fun uiCommand(builder: UICommandBuilder, block: UICommandDsl.() -> Unit): UICommandBuilder {
    return builder.dsl(block)
}
