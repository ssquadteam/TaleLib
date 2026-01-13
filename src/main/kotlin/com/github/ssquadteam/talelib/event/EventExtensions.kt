@file:JvmName("EventExtensions")

package com.github.ssquadteam.talelib.event

import com.hypixel.hytale.event.IBaseEvent
import com.hypixel.hytale.event.ICancellable
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent

fun IBaseEvent<*>.cancel() {
    if (this is ICancellable) {
        this.isCancelled = true
    }
}

fun IBaseEvent<*>.uncancel() {
    if (this is ICancellable) {
        this.isCancelled = false
    }
}

val IBaseEvent<*>.cancelled: Boolean
    get() = (this as? ICancellable)?.isCancelled ?: false

val PlayerReadyEvent.playerEntity: Player
    get() = this.player

fun PlayerReadyEvent.welcome(message: Message) = this.player.sendMessage(message)
fun PlayerReadyEvent.welcome(text: String) = this.player.sendMessage(Message.raw(text))

var PlayerChatEvent.message: String
    get() = this.content
    set(value) { this.content = value }

val PlayerChatEvent.senderRef: PlayerRef
    get() = this.sender

fun PlayerChatEvent.containsIgnoreCase(text: String): Boolean =
    this.content.contains(text, ignoreCase = true)

fun PlayerChatEvent.matches(regex: Regex): Boolean = regex.matches(this.content)

fun PlayerChatEvent.replaceContent(old: String, new: String, ignoreCase: Boolean = false) {
    this.content = this.content.replace(old, new, ignoreCase)
}

fun PlayerChatEvent.formatWith(
    prefix: String,
    prefixColor: String,
    nameColor: String,
    messageColor: String
) {
    this.formatter = PlayerChatEvent.Formatter { playerRef, msg ->
        Message.join(
            Message.raw("[$prefix] ").color(prefixColor),
            Message.raw(playerRef.username).color(nameColor),
            Message.raw(": $msg").color(messageColor)
        )
    }
}

fun PlayerChatEvent.cancelWith(response: Message? = null) {
    this.isCancelled = true
    response?.let { this.sender.sendMessage(it) }
}

fun PlayerChatEvent.cancelWith(response: String) = cancelWith(Message.raw(response))
