package com.github.ssquadteam.talelib.entity

import com.hypixel.hytale.logger.HytaleLogger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Centralized dispatcher for entity events.
 * Plugins can register listeners here to receive notifications when entities are added/removed.
 */
object EntityEventDispatcher {
    private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()

    private val itemAddedHandlers = CopyOnWriteArrayList<ItemEntityAddedHandler>()
    private val itemRemovedHandlers = CopyOnWriteArrayList<ItemEntityRemovedHandler>()
    private val itemMovedHandlers = CopyOnWriteArrayList<ItemEntityMovedHandler>()
    private val itemQuantityChangedHandlers = CopyOnWriteArrayList<ItemQuantityChangedHandler>()
    private val itemListeners = CopyOnWriteArrayList<ItemEntityListener>()

    private val handlersByOwner = ConcurrentHashMap<Any, MutableList<Any>>()

    fun onItemAdded(owner: Any, handler: ItemEntityAddedHandler) {
        itemAddedHandlers.add(handler)
        handlersByOwner.getOrPut(owner) { mutableListOf() }.add(handler)
    }

    fun onItemRemoved(owner: Any, handler: ItemEntityRemovedHandler) {
        itemRemovedHandlers.add(handler)
        handlersByOwner.getOrPut(owner) { mutableListOf() }.add(handler)
    }

    fun onItemMoved(owner: Any, handler: ItemEntityMovedHandler) {
        itemMovedHandlers.add(handler)
        handlersByOwner.getOrPut(owner) { mutableListOf() }.add(handler)
    }

    fun onItemQuantityChanged(owner: Any, handler: ItemQuantityChangedHandler) {
        itemQuantityChangedHandlers.add(handler)
        handlersByOwner.getOrPut(owner) { mutableListOf() }.add(handler)
    }

    fun addItemListener(owner: Any, listener: ItemEntityListener) {
        itemListeners.add(listener)
        handlersByOwner.getOrPut(owner) { mutableListOf() }.add(listener)
    }

    fun unregisterAll(owner: Any) {
        val handlers = handlersByOwner.remove(owner) ?: return
        for (handler in handlers) {
            when (handler) {
                is ItemEntityAddedHandler -> itemAddedHandlers.remove(handler)
                is ItemEntityRemovedHandler -> itemRemovedHandlers.remove(handler)
                is ItemEntityMovedHandler -> itemMovedHandlers.remove(handler)
                is ItemQuantityChangedHandler -> itemQuantityChangedHandlers.remove(handler)
                is ItemEntityListener -> itemListeners.remove(handler)
            }
        }
    }

    internal fun dispatchItemAdded(event: ItemEntityAddedEvent) {
        for (handler in itemAddedHandlers) {
            try {
                handler.handle(event)
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("Error in ItemEntityAddedHandler")
            }
        }
        for (listener in itemListeners) {
            try {
                listener.onItemEntityAdded(event)
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("Error in ItemEntityListener.onItemEntityAdded")
            }
        }
    }

    internal fun dispatchItemRemoved(event: ItemEntityRemovedEvent) {
        for (handler in itemRemovedHandlers) {
            try {
                handler.handle(event)
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("Error in ItemEntityRemovedHandler")
            }
        }
        for (listener in itemListeners) {
            try {
                listener.onItemEntityRemoved(event)
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("Error in ItemEntityListener.onItemEntityRemoved")
            }
        }
    }

    internal fun dispatchItemMoved(event: ItemEntityMovedEvent) {
        for (handler in itemMovedHandlers) {
            try {
                handler.handle(event)
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("Error in ItemEntityMovedHandler")
            }
        }
        for (listener in itemListeners) {
            try {
                listener.onItemEntityMoved(event)
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("Error in ItemEntityListener.onItemEntityMoved")
            }
        }
    }

    internal fun dispatchItemQuantityChanged(event: ItemQuantityChangedEvent) {
        for (handler in itemQuantityChangedHandlers) {
            try {
                handler.handle(event)
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("Error in ItemQuantityChangedHandler")
            }
        }
        for (listener in itemListeners) {
            try {
                listener.onItemQuantityChanged(event)
            } catch (e: Exception) {
                LOGGER.atWarning().withCause(e).log("Error in ItemEntityListener.onItemQuantityChanged")
            }
        }
    }

    fun hasItemHandlers(): Boolean =
        itemAddedHandlers.isNotEmpty() ||
        itemRemovedHandlers.isNotEmpty() ||
        itemMovedHandlers.isNotEmpty() ||
        itemQuantityChangedHandlers.isNotEmpty() ||
        itemListeners.isNotEmpty()

    fun clear() {
        itemAddedHandlers.clear()
        itemRemovedHandlers.clear()
        itemMovedHandlers.clear()
        itemQuantityChangedHandlers.clear()
        itemListeners.clear()
        handlersByOwner.clear()
    }
}
