@file:JvmName("PlayerExtensions")

package com.github.ssquadteam.talelib.player

import com.github.ssquadteam.talelib.message.Colors
import com.github.ssquadteam.talelib.message.error
import com.github.ssquadteam.talelib.message.info
import com.github.ssquadteam.talelib.message.success
import com.github.ssquadteam.talelib.message.toMessage
import com.github.ssquadteam.talelib.message.warning
import com.hypixel.hytale.protocol.GameMode
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.io.PacketHandler
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.UUID

fun PlayerRef.send(message: String) = this.sendMessage(message.toMessage())
fun PlayerRef.send(message: Message) = this.sendMessage(message)
fun PlayerRef.sendSuccess(message: String) = this.sendMessage(message.success())
fun PlayerRef.sendError(message: String) = this.sendMessage(message.error())
fun PlayerRef.sendWarning(message: String) = this.sendMessage(message.warning())
fun PlayerRef.sendInfo(message: String) = this.sendMessage(message.info())

fun PlayerRef.sendPrefixed(prefix: String, message: String, prefixColor: String = Colors.GOLD) {
    this.sendMessage(
        Message.join(
            "[$prefix] ".toMessage().color(prefixColor),
            message.toMessage()
        )
    )
}

val PlayerRef.name: String get() = this.username
val PlayerRef.uniqueId: UUID get() = this.uuid
val PlayerRef.packetHandler: PacketHandler get() = this.getPacketHandler()

val PlayerRef.isOnline: Boolean
    get() = try {
        this.packetHandler.channel?.isActive == true
    } catch (e: Exception) {
        false
    }

val Player.isCreative: Boolean get() = this.gameMode == GameMode.Creative
val Player.isAdventure: Boolean get() = this.gameMode == GameMode.Adventure

fun Player.setViewDistance(distance: Int) = this.setClientViewRadius(distance)

fun PlayerRef.displayString(): String = this.username
fun PlayerRef.displayMessage(color: String = Colors.AQUA): Message = this.username.toMessage().color(color)

fun Iterable<PlayerRef>.broadcast(message: String) {
    val msg = Message.raw(message)
    this.forEach { it.sendMessage(msg) }
}

fun Iterable<PlayerRef>.broadcast(message: Message) {
    this.forEach { it.sendMessage(message) }
}
