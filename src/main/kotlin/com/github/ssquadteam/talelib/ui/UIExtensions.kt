@file:JvmName("UIExtensions")

package com.github.ssquadteam.talelib.ui

import com.github.ssquadteam.talelib.TalePlugin
import com.github.ssquadteam.talelib.ui.hud.TaleHud
import com.github.ssquadteam.talelib.ui.hud.TaleHudBuilder
import com.github.ssquadteam.talelib.ui.hud.taleHud
import com.github.ssquadteam.talelib.ui.page.TalePage
import com.github.ssquadteam.talelib.ui.page.TalePageBuilder
import com.github.ssquadteam.talelib.ui.page.talePage
import java.util.concurrent.ConcurrentHashMap

private val pluginRegistries = ConcurrentHashMap<TalePlugin, TaleUIRegistry>()

val TalePlugin.taleUI: TaleUIRegistry
    get() = pluginRegistries.getOrPut(this) { TaleUIRegistry() }

fun TalePlugin.registerHud(hud: TaleHud): TaleHud = taleUI.registerHud(hud)

fun TalePlugin.registerPage(page: TalePage): TalePage = taleUI.registerPage(page)

fun TalePlugin.createHud(id: String, uiPath: String, block: TaleHudBuilder.() -> Unit = {}): TaleHud {
    return taleUI.registerHud(taleHud(id, uiPath, block))
}

fun TalePlugin.createPage(
    id: String,
    uiPath: String,
    block: TalePageBuilder.() -> Unit = {}
): TalePage {
    return taleUI.registerPage(talePage(id, uiPath, block))
}

fun TalePlugin.shutdownUI() {
    taleUI.shutdown()
    pluginRegistries.remove(this)
}
