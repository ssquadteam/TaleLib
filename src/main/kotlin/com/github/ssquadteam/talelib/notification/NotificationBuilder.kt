package com.github.ssquadteam.talelib.notification

import com.github.ssquadteam.talelib.message.toMessage
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.inventory.ItemStack
import com.hypixel.hytale.server.core.universe.PlayerRef

class NotificationBuilder {
    private var message: Message = Message.raw("")
    private var secondary: Message? = null
    private var icon: String? = null
    private var item: ItemStack? = null
    private var style: NotificationStyle = NotificationStyle.Default

    fun message(text: String): NotificationBuilder {
        message = text.toMessage()
        return this
    }

    fun message(msg: Message): NotificationBuilder {
        message = msg
        return this
    }

    fun secondary(text: String): NotificationBuilder {
        secondary = text.toMessage()
        return this
    }

    fun secondary(msg: Message): NotificationBuilder {
        secondary = msg
        return this
    }

    fun icon(iconPath: String): NotificationBuilder {
        icon = iconPath
        return this
    }

    fun itemIcon(itemId: String, quantity: Int = 1): NotificationBuilder {
        item = ItemStack(itemId, quantity)
        return this
    }

    fun itemIcon(itemStack: ItemStack): NotificationBuilder {
        item = itemStack
        return this
    }

    fun style(notificationStyle: NotificationStyle): NotificationBuilder {
        style = notificationStyle
        return this
    }

    fun success(): NotificationBuilder {
        style = NotificationStyle.Success
        return this
    }

    fun warning(): NotificationBuilder {
        style = NotificationStyle.Warning
        return this
    }

    fun error(): NotificationBuilder {
        style = NotificationStyle.Error
        return this
    }

    internal fun send(player: PlayerRef) {
        player.notify(message, secondary, icon, item, style)
    }
}

fun PlayerRef.notification(block: NotificationBuilder.() -> Unit) {
    NotificationBuilder().apply(block).send(this)
}
