@file:JvmName("TalePageKt")

package com.github.ssquadteam.talelib.ui.page

import com.github.ssquadteam.talelib.inventory.getPlayerComponent
import com.github.ssquadteam.talelib.ui.command.UICommandDsl
import com.github.ssquadteam.talelib.ui.command.dsl
import com.github.ssquadteam.talelib.ui.element.ElementRef
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.util.concurrent.ConcurrentHashMap

abstract class TalePage(
    val id: String,
    val uiPath: String
) {
    private val activePages = ConcurrentHashMap<PlayerRef, TaleCustomUIPage>()

    open fun onOpen(player: PlayerRef) {}

    open fun onClose(player: PlayerRef) {}

    open fun onBuild(player: PlayerRef, builder: UICommandBuilder, eventBuilder: UIEventBuilder) {
        builder.append(uiPath)
    }

    open fun onEvent(player: PlayerRef, eventData: String?) {}

    fun open(player: PlayerRef) {
        val p = player.getPlayerComponent() ?: return
        val ref = player.reference ?: return
        val store = ref.store
        val customPage = TaleCustomUIPage(player, this)
        activePages[player] = customPage
        p.pageManager.openCustomPage(ref, store, customPage)
        onOpen(player)
    }

    fun close(player: PlayerRef) {
        val p = player.getPlayerComponent() ?: return
        val ref = player.reference ?: return
        val store = ref.store
        activePages.remove(player)
        onClose(player)
    }

    fun isOpenFor(player: PlayerRef): Boolean = activePages.containsKey(player)

    fun update(player: PlayerRef, block: UICommandDsl.() -> Unit) {
        val customPage = activePages[player] ?: return
        val builder = UICommandBuilder()
        builder.dsl(block)
        customPage.doSendUpdate(builder)
    }

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

internal class TaleCustomUIPage(
    private val playerRef: PlayerRef,
    private val talePage: TalePage
) : CustomUIPage(playerRef, CustomPageLifetime.CanDismiss) {

    override fun build(ref: Ref<EntityStore>, builder: UICommandBuilder, eventBuilder: UIEventBuilder, store: Store<EntityStore>) {
        talePage.onBuild(playerRef, builder, eventBuilder)
    }

    override fun handleDataEvent(ref: Ref<EntityStore>, store: Store<EntityStore>, rawData: String) {
        talePage.onEvent(playerRef, rawData)
    }

    override fun onDismiss(ref: Ref<EntityStore>, store: Store<EntityStore>) {
        talePage.onClose(playerRef)
    }

    fun doSendUpdate(builder: UICommandBuilder) {
        sendUpdate(builder)
    }
}

fun Player.openPage(page: TalePage) {
    page.open(this.playerRef)
}

fun PlayerRef.openPage(page: TalePage) {
    page.open(this)
}

fun Player.closePage(page: TalePage) {
    page.close(this.playerRef)
}

fun PlayerRef.closePage(page: TalePage) {
    page.close(this)
}
