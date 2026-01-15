@file:JvmName("HudBuilder")

package com.github.ssquadteam.talelib.ui.hud

import com.github.ssquadteam.talelib.ui.command.UICommandDsl
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef

class TaleHudBuilder(
    private val id: String,
    private val uiPath: String
) {
    private var onShowHandler: ((PlayerRef) -> Unit)? = null
    private var onHideHandler: ((PlayerRef) -> Unit)? = null
    private var onBuildHandler: ((PlayerRef, UICommandBuilder) -> Unit)? = null

    fun onShow(handler: (PlayerRef) -> Unit): TaleHudBuilder {
        this.onShowHandler = handler
        return this
    }

    fun onHide(handler: (PlayerRef) -> Unit): TaleHudBuilder {
        this.onHideHandler = handler
        return this
    }

    fun onBuild(handler: (PlayerRef, UICommandBuilder) -> Unit): TaleHudBuilder {
        this.onBuildHandler = handler
        return this
    }

    fun build(): TaleHud {
        val showHandler = onShowHandler
        val hideHandler = onHideHandler
        val buildHandler = onBuildHandler
        val path = uiPath

        return object : TaleHud(id, path) {
            override fun onShow(player: PlayerRef) {
                showHandler?.invoke(player)
            }

            override fun onHide(player: PlayerRef) {
                hideHandler?.invoke(player)
            }

            override fun onBuild(player: PlayerRef, builder: UICommandBuilder) {
                builder.append(path)
                buildHandler?.invoke(player, builder)
            }
        }
    }
}

fun taleHud(id: String, uiPath: String, block: TaleHudBuilder.() -> Unit = {}): TaleHud {
    return TaleHudBuilder(id, uiPath).apply(block).build()
}

class SimpleHud(id: String, uiPath: String) : TaleHud(id, uiPath)

fun simpleHud(id: String, uiPath: String): SimpleHud {
    return SimpleHud(id, uiPath)
}
