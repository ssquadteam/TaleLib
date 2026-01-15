@file:JvmName("PageBuilder")

package com.github.ssquadteam.talelib.ui.page

import com.hypixel.hytale.server.core.ui.interactive.UIInteraction
import com.hypixel.hytale.server.core.universe.PlayerRef

class TalePageBuilder<T : Any>(
    private val id: String,
    private val uiPath: String,
    private val dataClass: Class<T>
) {
    private var onOpenHandler: ((PlayerRef, T) -> Unit)? = null
    private var onCloseHandler: ((PlayerRef) -> Unit)? = null
    private var onInteractionHandler: ((PlayerRef, UIInteraction, T) -> T)? = null
    private val elementHandlers = mutableMapOf<String, (PlayerRef, UIInteraction, T) -> T>()
    private val clickHandlers = mutableMapOf<String, (PlayerRef, T) -> T>()

    fun onOpen(handler: (PlayerRef, T) -> Unit): TalePageBuilder<T> {
        this.onOpenHandler = handler
        return this
    }

    fun onClose(handler: (PlayerRef) -> Unit): TalePageBuilder<T> {
        this.onCloseHandler = handler
        return this
    }

    fun onInteraction(handler: (PlayerRef, UIInteraction, T) -> T): TalePageBuilder<T> {
        this.onInteractionHandler = handler
        return this
    }

    fun onElement(elementId: String, handler: (PlayerRef, UIInteraction, T) -> T): TalePageBuilder<T> {
        elementHandlers[elementId] = handler
        return this
    }

    fun onClick(elementId: String, handler: (PlayerRef, T) -> T): TalePageBuilder<T> {
        clickHandlers[elementId] = handler
        return this
    }

    fun build(): TalePage<T> {
        val openHandler = onOpenHandler
        val closeHandler = onCloseHandler
        val interactionHandler = onInteractionHandler
        val elemHandlers = elementHandlers.toMap()
        val clkHandlers = clickHandlers.toMap()

        return object : TalePage<T>(id, uiPath, dataClass) {
            override fun onOpen(player: PlayerRef, data: T) {
                openHandler?.invoke(player, data)
            }

            override fun onClose(player: PlayerRef) {
                closeHandler?.invoke(player)
            }

            override fun onInteraction(player: PlayerRef, interaction: UIInteraction, data: T): T {
                var result = data

                val elementId = interaction.elementId
                if (elementId != null) {
                    elemHandlers[elementId]?.let { handler ->
                        result = handler(player, interaction, result)
                    }

                    clkHandlers[elementId]?.let { handler ->
                        result = handler(player, result)
                    }
                }

                interactionHandler?.let { handler ->
                    result = handler(player, interaction, result)
                }

                return result
            }
        }
    }
}

inline fun <reified T : Any> talePage(
    id: String,
    uiPath: String,
    noinline block: TalePageBuilder<T>.() -> Unit = {}
): TalePage<T> {
    return TalePageBuilder(id, uiPath, T::class.java).apply(block).build()
}

class SimplePage<T : Any>(
    id: String,
    uiPath: String,
    dataClass: Class<T>,
    private val interactionHandler: ((PlayerRef, UIInteraction, T) -> T)? = null
) : TalePage<T>(id, uiPath, dataClass) {
    override fun onInteraction(player: PlayerRef, interaction: UIInteraction, data: T): T {
        return interactionHandler?.invoke(player, interaction, data) ?: data
    }
}

inline fun <reified T : Any> simplePage(
    id: String,
    uiPath: String,
    noinline interactionHandler: ((PlayerRef, UIInteraction, T) -> T)? = null
): SimplePage<T> {
    return SimplePage(id, uiPath, T::class.java, interactionHandler)
}
