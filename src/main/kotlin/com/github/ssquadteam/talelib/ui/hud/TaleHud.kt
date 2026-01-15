@file:JvmName("TaleHudKt")

package com.github.ssquadteam.talelib.ui.hud

import com.github.ssquadteam.talelib.inventory.getPlayerComponent
import com.github.ssquadteam.talelib.ui.command.UICommandDsl
import com.github.ssquadteam.talelib.ui.command.dsl
import com.github.ssquadteam.talelib.ui.element.ElementRef
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.ConcurrentHashMap

abstract class TaleHud(
    val id: String,
    val uiPath: String
) {
    private val activeForPlayers = ConcurrentHashMap.newKeySet<PlayerRef>()
    private val customHuds = ConcurrentHashMap<PlayerRef, TaleCustomUIHud>()

    open fun onShow(player: PlayerRef) {}

    open fun onHide(player: PlayerRef) {}

    open fun onBuild(player: PlayerRef, builder: UICommandBuilder) {
        builder.append(uiPath)
    }

    fun show(player: PlayerRef) {
        val p = player.getPlayerComponent() ?: return
        val customHud = TaleCustomUIHud(player, this)
        customHuds[player] = customHud
        p.hudManager.setCustomHud(player, customHud)
        activeForPlayers.add(player)
        onShow(player)
    }

    fun hide(player: PlayerRef) {
        val p = player.getPlayerComponent() ?: return
        customHuds.remove(player)
        p.hudManager.setCustomHud(player, null)
        activeForPlayers.remove(player)
        onHide(player)
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
        val customHud = customHuds[player] ?: return
        val builder = UICommandBuilder()
        builder.dsl(block)
        customHud.update(false, builder)
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

    fun setProgress(player: PlayerRef, element: ElementRef, current: Int, max: Int) {
        update(player) { progress(element, current, max) }
    }

    fun setProgress(player: PlayerRef, elementId: String, current: Int, max: Int) {
        update(player) { progress(elementId, current, max) }
    }
}

internal class TaleCustomUIHud(
    playerRef: PlayerRef,
    private val taleHud: TaleHud
) : CustomUIHud(playerRef) {
    override fun build(builder: UICommandBuilder) {
        taleHud.onBuild(playerRef, builder)
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
