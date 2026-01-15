@file:JvmName("NotificationExtensions")

package com.github.ssquadteam.talelib.notification

import com.github.ssquadteam.talelib.message.toMessage
import com.hypixel.hytale.protocol.ItemWithAllMetadata
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.inventory.ItemStack
import com.hypixel.hytale.server.core.util.NotificationUtil
import com.hypixel.hytale.server.core.universe.PlayerRef

fun PlayerRef.notify(message: String) {
    NotificationUtil.sendNotification(packetHandler, message)
}

fun PlayerRef.notify(message: Message) {
    NotificationUtil.sendNotification(packetHandler, message)
}

fun PlayerRef.notify(message: String, style: NotificationStyle) {
    NotificationUtil.sendNotification(packetHandler, message, style)
}

fun PlayerRef.notify(message: Message, style: NotificationStyle) {
    NotificationUtil.sendNotification(packetHandler, message, style)
}

fun PlayerRef.notify(message: String, secondary: String) {
    NotificationUtil.sendNotification(packetHandler, message, secondary)
}

fun PlayerRef.notify(message: Message, secondary: Message) {
    NotificationUtil.sendNotification(packetHandler, message, secondary)
}

fun PlayerRef.notify(message: Message, secondary: Message, style: NotificationStyle) {
    NotificationUtil.sendNotification(packetHandler, message, secondary, style)
}

fun PlayerRef.notifyWithIcon(message: String, icon: String) {
    NotificationUtil.sendNotification(packetHandler, message.toMessage(), icon)
}

fun PlayerRef.notify(message: Message, icon: String) {
    NotificationUtil.sendNotification(packetHandler, message, icon)
}

fun PlayerRef.notify(message: Message, icon: String, style: NotificationStyle) {
    NotificationUtil.sendNotification(packetHandler, message, icon, style)
}

fun PlayerRef.notify(message: Message, item: ItemStack) {
    NotificationUtil.sendNotification(
        packetHandler,
        message,
        null as Message?,
        item.toPacket() as ItemWithAllMetadata
    )
}

fun PlayerRef.notify(message: Message, item: ItemStack, style: NotificationStyle) {
    NotificationUtil.sendNotification(
        packetHandler,
        message,
        null as Message?,
        item.toPacket() as ItemWithAllMetadata,
        style
    )
}

fun PlayerRef.notify(message: Message, secondary: Message?, icon: String?, item: ItemStack?, style: NotificationStyle) {
    NotificationUtil.sendNotification(
        packetHandler,
        message,
        secondary,
        icon,
        item?.toPacket() as? ItemWithAllMetadata,
        style
    )
}

fun PlayerRef.notifySuccess(message: String) =
    notify(message.toMessage(), NotificationStyle.Success)

fun PlayerRef.notifyWarning(message: String) =
    notify(message.toMessage(), NotificationStyle.Warning)

fun PlayerRef.notifyError(message: String) =
    notify(message.toMessage(), NotificationStyle.Error)

fun PlayerRef.notifyInfo(message: String) =
    notify(message.toMessage(), NotificationStyle.Default)
