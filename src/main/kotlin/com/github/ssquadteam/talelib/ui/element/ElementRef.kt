@file:JvmName("ElementRef")

package com.github.ssquadteam.talelib.ui.element

@JvmInline
value class ElementRef(val id: String) {
    val text: String get() = "$id.text"
    val value: String get() = "$id.value"
    val visible: String get() = "$id.visible"
    val enabled: String get() = "$id.enabled"
    val progress: String get() = "$id.progress"
    val maxProgress: String get() = "$id.maxProgress"
    val color: String get() = "$id.color"
    val alpha: String get() = "$id.alpha"
    val icon: String get() = "$id.icon"
    val image: String get() = "$id.image"

    fun property(name: String): String = "$id.$name"

    fun child(childId: String): ElementRef = ElementRef("$id.$childId")

    override fun toString(): String = id
}

fun element(id: String): ElementRef = ElementRef(id)

fun element(parent: String, child: String): ElementRef = ElementRef("$parent.$child")

object Elements {
    val ROOT = ElementRef("root")

    fun list(id: String, index: Int): ElementRef = ElementRef("$id[$index]")

    fun dynamicList(id: String): String = "$id[]"
}
