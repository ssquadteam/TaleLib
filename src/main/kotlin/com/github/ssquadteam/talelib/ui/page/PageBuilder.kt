@file:JvmName("PageBuilder")

package com.github.ssquadteam.talelib.ui.page

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef

class TalePageBuilder(
    private val id: String,
    private val uiPath: String
) {
    private var onOpenHandler: ((PlayerRef) -> Unit)? = null
    private var onCloseHandler: ((PlayerRef) -> Unit)? = null
    private var onBuildHandler: ((PlayerRef, UICommandBuilder, UIEventBuilder) -> Unit)? = null
    private var onEventHandler: ((PlayerRef, String?) -> Unit)? = null

    fun onOpen(handler: (PlayerRef) -> Unit): TalePageBuilder {
        this.onOpenHandler = handler
        return this
    }

    fun onClose(handler: (PlayerRef) -> Unit): TalePageBuilder {
        this.onCloseHandler = handler
        return this
    }

    fun onBuild(handler: (PlayerRef, UICommandBuilder, UIEventBuilder) -> Unit): TalePageBuilder {
        this.onBuildHandler = handler
        return this
    }

    fun onEvent(handler: (PlayerRef, String?) -> Unit): TalePageBuilder {
        this.onEventHandler = handler
        return this
    }

    fun build(): TalePage {
        val openHandler = onOpenHandler
        val closeHandler = onCloseHandler
        val buildHandler = onBuildHandler
        val eventHandler = onEventHandler
        val path = uiPath

        return object : TalePage(id, path) {
            override fun onOpen(player: PlayerRef) {
                openHandler?.invoke(player)
            }

            override fun onClose(player: PlayerRef) {
                closeHandler?.invoke(player)
            }

            override fun onBuild(player: PlayerRef, builder: UICommandBuilder, eventBuilder: UIEventBuilder) {
                builder.append(path)
                buildHandler?.invoke(player, builder, eventBuilder)
            }

            override fun onEvent(player: PlayerRef, eventData: String?) {
                eventHandler?.invoke(player, eventData)
            }
        }
    }
}

fun talePage(
    id: String,
    uiPath: String,
    block: TalePageBuilder.() -> Unit = {}
): TalePage {
    return TalePageBuilder(id, uiPath).apply(block).build()
}

class SimplePage(id: String, uiPath: String) : TalePage(id, uiPath)

fun simplePage(id: String, uiPath: String): SimplePage {
    return SimplePage(id, uiPath)
}
