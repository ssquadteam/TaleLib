@file:JvmName("TalePage")

package com.github.ssquadteam.talelib.ui.page

import com.github.ssquadteam.talelib.ui.command.UICommandDsl
import com.github.ssquadteam.talelib.ui.command.dsl
import com.github.ssquadteam.talelib.ui.element.ElementRef
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.ui.UICommandBuilder
import com.hypixel.hytale.server.core.ui.interactive.InteractiveUI
import com.hypixel.hytale.server.core.ui.interactive.UIInteraction
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.ConcurrentHashMap

abstract class TalePage<T : Any>(
    val id: String,
    val uiPath: String,
    val dataClass: Class<T>
) {
    private val activePages = ConcurrentHashMap<PlayerRef, InteractiveUI<T>>()

    open fun onOpen(player: PlayerRef, data: T) {}

    open fun onClose(player: PlayerRef) {}

    open fun onInteraction(player: PlayerRef, interaction: UIInteraction, data: T): T = data

    fun open(player: PlayerRef, data: T) {
        player.player?.let { p ->
            val ui = p.openInteractiveUI(uiPath, data, dataClass) { interaction, currentData ->
                onInteraction(player, interaction, currentData)
            }
            if (ui != null) {
                activePages[player] = ui
                onOpen(player, data)
            }
        }
    }

    fun close(player: PlayerRef) {
        activePages.remove(player)?.close()
        onClose(player)
    }

    fun isOpenFor(player: PlayerRef): Boolean = activePages.containsKey(player)

    fun update(player: PlayerRef, block: UICommandDsl.() -> Unit) {
        activePages[player]?.let { ui ->
            val builder = UICommandBuilder()
            builder.dsl(block)
            ui.update(builder)
        }
    }

    fun updateData(player: PlayerRef, updater: (T) -> T) {
        activePages[player]?.updateData(updater)
    }

    fun getData(player: PlayerRef): T? = activePages[player]?.data

    fun getActivePlayers(): Set<PlayerRef> = activePages.keys.toSet()

    fun closeAll() {
        activePages.keys.toList().forEach { close(it) }
    }

    fun setText(player: PlayerRef, element: ElementRef, text: String) {
        update(player) { text(element, text) }
    }

    fun setVisible(player: PlayerRef, element: ElementRef, visible: Boolean) {
        update(player) { visible(element, visible) }
    }

    fun setEnabled(player: PlayerRef, element: ElementRef, enabled: Boolean) {
        update(player) { enabled(element, enabled) }
    }
}

inline fun <reified T : Any> Player.openPage(page: TalePage<T>, data: T) {
    page.open(this.playerRef, data)
}

inline fun <reified T : Any> PlayerRef.openPage(page: TalePage<T>, data: T) {
    page.open(this, data)
}

fun <T : Any> Player.closePage(page: TalePage<T>) {
    page.close(this.playerRef)
}

fun <T : Any> PlayerRef.closePage(page: TalePage<T>) {
    page.close(this)
}
