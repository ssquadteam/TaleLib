@file:JvmName("TaleHud")

package com.github.ssquadteam.talelib.ui.hud

import com.github.ssquadteam.talelib.ui.command.UICommandDsl
import com.github.ssquadteam.talelib.ui.command.dsl
import com.github.ssquadteam.talelib.ui.element.ElementRef
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.ui.HudLayer
import com.hypixel.hytale.server.core.ui.UICommandBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.ConcurrentHashMap

abstract class TaleHud(
    val id: String,
    val uiPath: String,
    val layer: HudLayer = HudLayer.DEFAULT
) {
    private val activeForPlayers = ConcurrentHashMap.newKeySet<PlayerRef>()

    open fun onShow(player: PlayerRef) {}

    open fun onHide(player: PlayerRef) {}

    open fun onUpdate(player: PlayerRef, builder: UICommandBuilder) {}

    fun show(player: PlayerRef) {
        player.player?.let { p ->
            p.hud.add(uiPath, layer)
            activeForPlayers.add(player)
            onShow(player)
        }
    }

    fun hide(player: PlayerRef) {
        player.player?.let { p ->
            p.hud.remove(uiPath)
            activeForPlayers.remove(player)
            onHide(player)
        }
    }

    fun toggle(player: PlayerRef) {
        if (isShownTo(player)) {
            hide(player)
        } else {
            show(player)
        }
    }

    fun isShownTo(player: PlayerRef): Boolean = activeForPlayers.contains(player)

    fun update(player: PlayerRef, block: UICommandDsl.() -> Unit) {
        player.player?.let { p ->
            val builder = UICommandBuilder()
            builder.dsl(block)
            p.hud.update(builder)
        }
    }

    fun updateAll(block: UICommandDsl.() -> Unit) {
        activeForPlayers.forEach { playerRef ->
            update(playerRef, block)
        }
    }

    fun showTo(players: Collection<PlayerRef>) {
        players.forEach { show(it) }
    }

    fun hideFrom(players: Collection<PlayerRef>) {
        players.forEach { hide(it) }
    }

    fun hideFromAll() {
        activeForPlayers.toList().forEach { hide(it) }
    }

    fun getActivePlayers(): Set<PlayerRef> = activeForPlayers.toSet()

    fun setText(player: PlayerRef, element: ElementRef, text: String) {
        update(player) { text(element, text) }
    }

    fun setText(player: PlayerRef, elementId: String, text: String) {
        update(player) { text(elementId, text) }
    }

    fun setVisible(player: PlayerRef, element: ElementRef, visible: Boolean) {
        update(player) { visible(element, visible) }
    }

    fun setVisible(player: PlayerRef, elementId: String, visible: Boolean) {
        update(player) { visible(elementId, visible) }
    }

    fun setProgress(player: PlayerRef, element: ElementRef, current: Number, max: Number) {
        update(player) { progress(element, current, max) }
    }

    fun setProgress(player: PlayerRef, elementId: String, current: Number, max: Number) {
        update(player) { progress(elementId, current, max) }
    }
}

fun Player.showHud(hud: TaleHud) {
    hud.show(this.playerRef)
}

fun Player.hideHud(hud: TaleHud) {
    hud.hide(this.playerRef)
}

fun PlayerRef.showHud(hud: TaleHud) {
    hud.show(this)
}

fun PlayerRef.hideHud(hud: TaleHud) {
    hud.hide(this)
}
