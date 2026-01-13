package com.github.ssquadteam.talelib.message

import com.hypixel.hytale.server.core.Message
import com.github.ssquadteam.hytaleminiformat.Colors

class MessageBuilder {
    private val parts = mutableListOf<Message>()

    operator fun String.unaryPlus() {
        parts.add(this.toMessage())
    }

    operator fun String.invoke(block: MessagePartBuilder.() -> Unit) {
        val builder = MessagePartBuilder(this)
        builder.block()
        parts.add(builder.build())
    }

    operator fun Message.unaryPlus() {
        parts.add(this)
    }

    fun part(text: String, block: MessagePartBuilder.() -> Unit = {}): MessageBuilder {
        val builder = MessagePartBuilder(text)
        builder.block()
        parts.add(builder.build())
        return this
    }

    fun black(text: String) = parts.add(text.toMessage().color(Colors.BLACK))
    fun darkBlue(text: String) = parts.add(text.toMessage().color(Colors.DARK_BLUE))
    fun darkGreen(text: String) = parts.add(text.toMessage().color(Colors.DARK_GREEN))
    fun darkAqua(text: String) = parts.add(text.toMessage().color(Colors.DARK_AQUA))
    fun darkRed(text: String) = parts.add(text.toMessage().color(Colors.DARK_RED))
    fun darkPurple(text: String) = parts.add(text.toMessage().color(Colors.DARK_PURPLE))
    fun gold(text: String) = parts.add(text.toMessage().color(Colors.GOLD))
    fun gray(text: String) = parts.add(text.toMessage().color(Colors.GRAY))
    fun darkGray(text: String) = parts.add(text.toMessage().color(Colors.DARK_GRAY))
    fun blue(text: String) = parts.add(text.toMessage().color(Colors.BLUE))
    fun green(text: String) = parts.add(text.toMessage().color(Colors.GREEN))
    fun aqua(text: String) = parts.add(text.toMessage().color(Colors.AQUA))
    fun red(text: String) = parts.add(text.toMessage().color(Colors.RED))
    fun lightPurple(text: String) = parts.add(text.toMessage().color(Colors.LIGHT_PURPLE))
    fun yellow(text: String) = parts.add(text.toMessage().color(Colors.YELLOW))
    fun white(text: String) = parts.add(text.toMessage().color(Colors.WHITE))

    fun success(text: String) = parts.add(text.success())
    fun error(text: String) = parts.add(text.error())
    fun warning(text: String) = parts.add(text.warning())
    fun info(text: String) = parts.add(text.info())

    fun newline() = parts.add(Message.raw("\n"))
    fun space() = parts.add(Message.raw(" "))

    fun separator(char: Char = '-', length: Int = 40, color: String = Colors.GRAY) {
        parts.add(char.toString().repeat(length).toMessage().color(color))
    }

    fun build(): Message = when (parts.size) {
        0 -> Message.raw("")
        1 -> parts[0]
        else -> Message.join(*parts.toTypedArray())
    }
}

class MessagePartBuilder(private val text: String) {
    var color: String? = null
    var bold: Boolean = false
    var italic: Boolean = false
    var monospace: Boolean = false
    var linkUrl: String? = null

    fun build(): Message {
        var msg = text.toMessage()
        color?.let { msg = msg.color(it) }
        if (bold) msg = msg.bold(true)
        if (italic) msg = msg.italic(true)
        if (monospace) msg = msg.monospace(true)
        linkUrl?.let { msg = msg.link(it) }
        return msg
    }
}

fun message(block: MessageBuilder.() -> Unit): Message {
    val builder = MessageBuilder()
    builder.block()
    return builder.build()
}

fun format(template: String, block: PlaceholderBuilder.() -> Unit): Message {
    val builder = PlaceholderBuilder()
    builder.block()
    var result = template
    builder.placeholders.forEach { (key, value) ->
        result = result.replace("%$key%", value)
    }
    return result.toMessage()
}

class PlaceholderBuilder {
    internal val placeholders = mutableMapOf<String, String>()

    infix fun String.to(value: String) {
        placeholders[this] = value
    }

    infix fun String.to(value: Any) {
        placeholders[this] = value.toString()
    }
}
