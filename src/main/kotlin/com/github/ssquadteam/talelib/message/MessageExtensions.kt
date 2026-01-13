@file:JvmName("MessageExtensions")

package com.github.ssquadteam.talelib.message

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.protocol.MaybeBool
import com.github.ssquadteam.hytaleminiformat.Colors

fun String.toMessage(): Message = Message.raw(this)
fun String.toTranslation(): Message = Message.translation(this)

fun String.success(): Message = this.toMessage().color(Colors.SUCCESS)
fun String.error(): Message = this.toMessage().color(Colors.ERROR)
fun String.warning(): Message = this.toMessage().color(Colors.WARNING)
fun String.info(): Message = this.toMessage().color(Colors.INFO)
fun String.muted(): Message = this.toMessage().color(Colors.MUTED)
fun String.highlight(): Message = this.toMessage().color(Colors.HIGHLIGHT)
fun String.primary(): Message = this.toMessage().color(Colors.PRIMARY)

fun Message.bold(): Message = this.bold(true)
fun Message.italic(): Message = this.italic(true)
fun Message.monospace(): Message = this.monospace(true)

fun messages(vararg messages: Message): Message = Message.join(*messages)
fun messages(vararg strings: String): Message = Message.join(*strings.map { it.toMessage() }.toTypedArray())

operator fun Message.plus(other: Message): Message = Message.join(this, other)
operator fun Message.plus(other: String): Message = Message.join(this, other.toMessage())

fun String.withTaleLibPrefix(): Message = Message.join(
    "[TaleLib] ".toMessage().color(Colors.TALELIB).bold(),
    this.toMessage()
)

fun String.withPrefix(prefix: String, prefixColor: String = Colors.GOLD): Message = Message.join(
    "[$prefix] ".toMessage().color(prefixColor).bold(),
    this.toMessage()
)

fun emptyMessage(): Message = Message.raw("")

fun separator(char: Char = '-', length: Int = 40, color: String = Colors.GRAY): Message =
    char.toString().repeat(length).toMessage().color(color)

fun Message.underlined(underlined: Boolean): Message {
    this.formattedMessage.underlined = if (underlined) MaybeBool.True else MaybeBool.False
    return this
}
