@file:JvmName("TaleUIRegistry")

package com.github.ssquadteam.talelib.ui

import com.github.ssquadteam.talelib.ui.hud.TaleHud
import com.github.ssquadteam.talelib.ui.page.TalePage
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.ConcurrentHashMap

class TaleUIRegistry {
    private val huds = ConcurrentHashMap<String, TaleHud>()
    private val pages = ConcurrentHashMap<String, TalePage<*>>()

    fun registerHud(hud: TaleHud): TaleHud {
        huds[hud.id] = hud
        return hud
    }

    fun <T : Any> registerPage(page: TalePage<T>): TalePage<T> {
        pages[page.id] = page
        return page
    }

    fun unregisterHud(id: String) {
        huds.remove(id)
    }

    fun unregisterPage(id: String) {
        pages.remove(id)
    }

    fun getHud(id: String): TaleHud? = huds[id]

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getPage(id: String): TalePage<T>? = pages[id] as? TalePage<T>

    fun getAllHuds(): Collection<TaleHud> = huds.values.toList()

    fun getAllPages(): Collection<TalePage<*>> = pages.values.toList()

    fun hideAllHudsFrom(player: PlayerRef) {
        huds.values.forEach { hud ->
            if (hud.isShownTo(player)) {
                hud.hide(player)
            }
        }
    }

    fun closeAllPagesFor(player: PlayerRef) {
        pages.values.forEach { page ->
            if (page.isOpenFor(player)) {
                page.close(player)
            }
        }
    }

    fun cleanupPlayer(player: PlayerRef) {
        hideAllHudsFrom(player)
        closeAllPagesFor(player)
    }

    fun shutdown() {
        huds.values.forEach { it.hideFromAll() }
        pages.values.forEach { it.closeAll() }
        huds.clear()
        pages.clear()
    }

    fun isHudRegistered(id: String): Boolean = huds.containsKey(id)

    fun isPageRegistered(id: String): Boolean = pages.containsKey(id)

    val hudCount: Int get() = huds.size

    val pageCount: Int get() = pages.size
}
