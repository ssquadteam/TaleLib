@file:JvmName("TalePageKt")

package com.github.ssquadteam.talelib.ui.page

import com.github.ssquadteam.talelib.inventory.getPlayerComponent
import com.github.ssquadteam.talelib.ui.command.UICommandDsl
import com.github.ssquadteam.talelib.ui.command.dsl
import com.github.ssquadteam.talelib.ui.element.ElementRef
import com.github.ssquadteam.talelib.ui.event.ParsedEventData
import com.github.ssquadteam.talelib.ui.event.UIEventDsl
import com.github.ssquadteam.talelib.ui.event.dsl
import com.github.ssquadteam.talelib.ui.event.parseEventData
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage
import com.hypixel.hytale.protocol.packets.interface_.Page
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.util.concurrent.ConcurrentHashMap

/**
 * Base class for custom UI pages in TaleLib.
 *
 * @param id Unique identifier for this page
 * @param uiPath Path to the .ui file (relative to Common/UI/Custom/)
 * @param lifetime How the page can be closed (default: CanDismiss - ESC to close)
 */
abstract class TalePage(
    val id: String,
    val uiPath: String,
    val lifetime: CustomPageLifetime = CustomPageLifetime.CanDismiss
) {
    private val activePages = ConcurrentHashMap<PlayerRef, TaleCustomUIPage>()

    /**
     * Called when the page is opened for a player
     */
    open fun onOpen(player: PlayerRef) {}

    /**
     * Called when the page is closed for a player
     */
    open fun onClose(player: PlayerRef) {}

    /**
     * Called to build the initial UI.
     * Override this to customize the UI and bind events.
     *
     * Example:
     * ```kotlin
     * override fun onBuild(player: PlayerRef, builder: UICommandBuilder, eventBuilder: UIEventBuilder) {
     *     builder.append(uiPath)
     *     builder.dsl {
     *         text("title", "My Page")
     *     }
     *     eventBuilder.dsl {
     *         onClickButtons("saveBtn", "cancelBtn", "closeBtn")
     *     }
     * }
     * ```
     */
    open fun onBuild(player: PlayerRef, builder: UICommandBuilder, eventBuilder: UIEventBuilder) {
        builder.append(uiPath)
    }

    /**
     * Called when a UI event is received (raw string).
     * Consider using onParsedEvent instead for easier event handling.
     */
    open fun onEvent(player: PlayerRef, eventData: String?) {
        // Default: parse and delegate to onParsedEvent
        onParsedEvent(player, parseEventData(eventData))
    }

    /**
     * Called when a UI event is received (parsed).
     * Override this for easier event handling.
     *
     * Example:
     * ```kotlin
     * override fun onParsedEvent(player: PlayerRef, event: ParsedEventData) {
     *     when (event.action) {
     *         "saveBtn" -> handleSave(player)
     *         "cancelBtn" -> close(player)
     *     }
     * }
     * ```
     */
    open fun onParsedEvent(player: PlayerRef, event: ParsedEventData) {}

    /**
     * Open the page for a player
     */
    fun open(player: PlayerRef) {
        val p = player.getPlayerComponent() ?: return
        val ref = player.reference ?: return
        val store = ref.store
        val customPage = TaleCustomUIPage(player, this)
        activePages[player] = customPage
        p.pageManager.openCustomPage(ref, store, customPage)
        onOpen(player)
    }

    /**
     * Close the page for a player
     */
    fun close(player: PlayerRef) {
        val p = player.getPlayerComponent() ?: return
        val ref = player.reference ?: return
        val store = ref.store
        activePages.remove(player)
        p.pageManager.setPage(ref, store, Page.None)
        onClose(player)
    }

    /**
     * Check if the page is open for a player
     */
    fun isOpenFor(player: PlayerRef): Boolean = activePages.containsKey(player)

    /**
     * Send a UI update to a player
     */
    fun update(player: PlayerRef, block: UICommandDsl.() -> Unit) {
        val customPage = activePages[player] ?: return
        val builder = UICommandBuilder()
        builder.dsl(block)
        customPage.doSendUpdate(builder)
    }

    /**
     * Get all players who have this page open
     */
    fun getActivePlayers(): Set<PlayerRef> = activePages.keys.toSet()

    /**
     * Close the page for all players
     */
    fun closeAll() {
        activePages.keys.toList().forEach { close(it) }
    }

    // Convenience methods for common updates

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

    fun setEnabled(player: PlayerRef, element: ElementRef, enabled: Boolean) {
        update(player) { enabled(element, enabled) }
    }

    fun setEnabled(player: PlayerRef, elementId: String, enabled: Boolean) {
        update(player) { enabled(elementId, enabled) }
    }

    fun show(player: PlayerRef, elementId: String) {
        update(player) { show(elementId) }
    }

    fun hide(player: PlayerRef, elementId: String) {
        update(player) { hide(elementId) }
    }
}

/**
 * Internal custom UI page implementation
 */
internal class TaleCustomUIPage(
    private val playerRef: PlayerRef,
    private val talePage: TalePage
) : CustomUIPage(playerRef, talePage.lifetime) {

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

// Extension functions for convenient page operations

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
