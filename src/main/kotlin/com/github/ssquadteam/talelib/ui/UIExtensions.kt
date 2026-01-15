@file:JvmName("UIExtensions")

package com.github.ssquadteam.talelib.ui

import com.github.ssquadteam.talelib.TalePlugin
import com.github.ssquadteam.talelib.ui.command.UICommandDsl
import com.github.ssquadteam.talelib.ui.command.dsl
import com.github.ssquadteam.talelib.ui.hud.TaleHud
import com.github.ssquadteam.talelib.ui.hud.TaleHudBuilder
import com.github.ssquadteam.talelib.ui.hud.taleHud
import com.github.ssquadteam.talelib.ui.page.TalePage
import com.github.ssquadteam.talelib.ui.page.TalePageBuilder
import com.github.ssquadteam.talelib.ui.page.talePage
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.ui.HudLayer
import com.hypixel.hytale.server.core.ui.UICommandBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.ConcurrentHashMap

private val pluginRegistries = ConcurrentHashMap<TalePlugin, TaleUIRegistry>()

val TalePlugin.taleUI: TaleUIRegistry
    get() = pluginRegistries.getOrPut(this) { TaleUIRegistry() }

fun TalePlugin.registerHud(hud: TaleHud): TaleHud = taleUI.registerHud(hud)

fun <T : Any> TalePlugin.registerPage(page: TalePage<T>): TalePage<T> = taleUI.registerPage(page)

fun TalePlugin.createHud(id: String, uiPath: String, block: TaleHudBuilder.() -> Unit = {}): TaleHud {
    return taleUI.registerHud(taleHud(id, uiPath, block))
}

fun TalePlugin.createHud(id: String, uiPath: String, layer: HudLayer, block: TaleHudBuilder.() -> Unit = {}): TaleHud {
    return taleUI.registerHud(taleHud(id, uiPath, layer, block))
}

inline fun <reified T : Any> TalePlugin.createPage(
    id: String,
    uiPath: String,
    noinline block: TalePageBuilder<T>.() -> Unit = {}
): TalePage<T> {
    return taleUI.registerPage(talePage(id, uiPath, block))
}

fun TalePlugin.shutdownUI() {
    taleUI.shutdown()
    pluginRegistries.remove(this)
}

fun Player.updateHud(block: UICommandDsl.() -> Unit) {
    val builder = UICommandBuilder()
    builder.dsl(block)
    this.hud.update(builder)
}

fun PlayerRef.updateHud(block: UICommandDsl.() -> Unit) {
    player?.updateHud(block)
}

fun Player.showHud(uiPath: String, layer: HudLayer = HudLayer.DEFAULT) {
    this.hud.add(uiPath, layer)
}

fun Player.hideHud(uiPath: String) {
    this.hud.remove(uiPath)
}

fun PlayerRef.showHud(uiPath: String, layer: HudLayer = HudLayer.DEFAULT) {
    player?.showHud(uiPath, layer)
}

fun PlayerRef.hideHud(uiPath: String) {
    player?.hideHud(uiPath)
}

fun <T : Any> Player.openUI(uiPath: String, data: T, dataClass: Class<T>) {
    this.openInteractiveUI(uiPath, data, dataClass) { _, d -> d }
}

inline fun <reified T : Any> Player.openUI(uiPath: String, data: T) {
    openUI(uiPath, data, T::class.java)
}

inline fun <reified T : Any> PlayerRef.openUI(uiPath: String, data: T) {
    player?.openUI(uiPath, data)
}
