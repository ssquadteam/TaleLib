@file:JvmName("HudBuilder")

package com.github.ssquadteam.talelib.ui.hud

import com.github.ssquadteam.talelib.ui.command.UICommandDsl
import com.hypixel.hytale.server.core.ui.HudLayer
import com.hypixel.hytale.server.core.ui.UICommandBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef

class TaleHudBuilder(
    private val id: String,
    private val uiPath: String
) {
    private var layer: HudLayer = HudLayer.DEFAULT
    private var onShowHandler: ((PlayerRef) -> Unit)? = null
    private var onHideHandler: ((PlayerRef) -> Unit)? = null
    private var onUpdateHandler: ((PlayerRef, UICommandBuilder) -> Unit)? = null

    fun layer(layer: HudLayer): TaleHudBuilder {
        this.layer = layer
        return this
    }

    fun layerBackground(): TaleHudBuilder = layer(HudLayer.BACKGROUND)

    fun layerDefault(): TaleHudBuilder = layer(HudLayer.DEFAULT)

    fun layerForeground(): TaleHudBuilder = layer(HudLayer.FOREGROUND)

    fun layerOverlay(): TaleHudBuilder = layer(HudLayer.OVERLAY)

    fun onShow(handler: (PlayerRef) -> Unit): TaleHudBuilder {
        this.onShowHandler = handler
        return this
    }

    fun onHide(handler: (PlayerRef) -> Unit): TaleHudBuilder {
        this.onHideHandler = handler
        return this
    }

    fun onUpdate(handler: (PlayerRef, UICommandBuilder) -> Unit): TaleHudBuilder {
        this.onUpdateHandler = handler
        return this
    }

    fun build(): TaleHud {
        val showHandler = onShowHandler
        val hideHandler = onHideHandler
        val updateHandler = onUpdateHandler

        return object : TaleHud(id, uiPath, layer) {
            override fun onShow(player: PlayerRef) {
                showHandler?.invoke(player)
            }

            override fun onHide(player: PlayerRef) {
                hideHandler?.invoke(player)
            }

            override fun onUpdate(player: PlayerRef, builder: UICommandBuilder) {
                updateHandler?.invoke(player, builder)
            }
        }
    }
}

fun taleHud(id: String, uiPath: String, block: TaleHudBuilder.() -> Unit = {}): TaleHud {
    return TaleHudBuilder(id, uiPath).apply(block).build()
}

fun taleHud(id: String, uiPath: String, layer: HudLayer, block: TaleHudBuilder.() -> Unit = {}): TaleHud {
    return TaleHudBuilder(id, uiPath).layer(layer).apply(block).build()
}

class SimpleHud(id: String, uiPath: String, layer: HudLayer = HudLayer.DEFAULT) : TaleHud(id, uiPath, layer)

fun simpleHud(id: String, uiPath: String, layer: HudLayer = HudLayer.DEFAULT): SimpleHud {
    return SimpleHud(id, uiPath, layer)
}
