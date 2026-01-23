package com.github.ssquadteam.talelib.block

import com.hypixel.hytale.logger.HytaleLogger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Centralized dispatcher for block events.
 * Plugins can register listeners here to receive notifications when blocks change.
 */
object BlockEventDispatcher {
    private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()

    private val blockChangedHandlers = CopyOnWriteArrayList<BlockChangedHandler>()
    private val blockListeners = CopyOnWriteArrayList<BlockChangeListener>()
    private val handlersByOwner = ConcurrentHashMap<Any, MutableList<Any>>()

    fun onBlockChanged(owner: Any, handler: BlockChangedHandler) {
        blockChangedHandlers.add(handler)
        handlersByOwner.getOrPut(owner) { mutableListOf() }.add(handler)
    }

    fun addBlockListener(owner: Any, listener: BlockChangeListener) {
        blockListeners.add(listener)
        handlersByOwner.getOrPut(owner) { mutableListOf() }.add(listener)
    }

    fun unregisterAll(owner: Any) {
        val handlers = handlersByOwner.remove(owner) ?: return
        for (handler in handlers) {
            when (handler) {
                is BlockChangedHandler -> blockChangedHandlers.remove(handler)
                is BlockChangeListener -> blockListeners.remove(handler)
            }
        }
    }

    internal fun dispatchBlockChanged(event: BlockChangedEvent) {
        for (handler in blockChangedHandlers) {
            try {
                handler.handle(event)
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("Error in BlockChangedHandler")
            }
        }
        for (listener in blockListeners) {
            try {
                listener.onBlockChanged(event)
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("Error in BlockChangeListener.onBlockChanged")
            }
        }
    }

    fun hasHandlers(): Boolean =
        blockChangedHandlers.isNotEmpty() || blockListeners.isNotEmpty()

    fun clear() {
        blockChangedHandlers.clear()
        blockListeners.clear()
        handlersByOwner.clear()
    }
}
