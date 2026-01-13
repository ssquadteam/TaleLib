package com.github.ssquadteam.talelib.event

import com.hypixel.hytale.event.EventPriority
import com.hypixel.hytale.event.EventRegistration
import com.hypixel.hytale.event.IBaseEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import java.util.function.Consumer

class TaleEventRegistry(private val plugin: JavaPlugin) {

    private val registrations = mutableListOf<EventRegistration<*, *>>()

    fun <E : IBaseEvent<Void>> on(
        eventClass: Class<in E>,
        handler: (E) -> Unit
    ): TaleEventRegistry {
        val reg = plugin.eventRegistry.register(eventClass, Consumer { handler(it) })
        reg?.let { registrations.add(it) }
        return this
    }

    fun <E : IBaseEvent<Void>> on(
        eventClass: Class<in E>,
        priority: EventPriority,
        handler: (E) -> Unit
    ): TaleEventRegistry {
        val reg = plugin.eventRegistry.register(priority, eventClass, Consumer { handler(it) })
        reg?.let { registrations.add(it) }
        return this
    }

    fun <E : IBaseEvent<Void>> on(
        eventClass: Class<in E>,
        priority: Short,
        handler: (E) -> Unit
    ): TaleEventRegistry {
        val reg = plugin.eventRegistry.register(priority, eventClass, Consumer { handler(it) })
        reg?.let { registrations.add(it) }
        return this
    }

    fun <K : Any, E : IBaseEvent<K>> onKeyed(
        eventClass: Class<in E>,
        key: K,
        handler: (E) -> Unit
    ): TaleEventRegistry {
        val reg = plugin.eventRegistry.register(eventClass, key, Consumer { handler(it) })
        reg?.let { registrations.add(it) }
        return this
    }

    fun <K : Any, E : IBaseEvent<K>> onGlobal(
        eventClass: Class<in E>,
        handler: (E) -> Unit
    ): TaleEventRegistry {
        val reg = plugin.eventRegistry.registerGlobal(eventClass, Consumer { handler(it) })
        reg?.let { registrations.add(it) }
        return this
    }

    fun <K : Any, E : IBaseEvent<K>> onGlobal(
        eventClass: Class<in E>,
        priority: EventPriority,
        handler: (E) -> Unit
    ): TaleEventRegistry {
        val reg = plugin.eventRegistry.registerGlobal(priority, eventClass, Consumer { handler(it) })
        reg?.let { registrations.add(it) }
        return this
    }

    fun <K : Any, E : IBaseEvent<K>> onUnhandled(
        eventClass: Class<in E>,
        handler: (E) -> Unit
    ): TaleEventRegistry {
        val reg = plugin.eventRegistry.registerUnhandled(eventClass, Consumer { handler(it) })
        reg?.let { registrations.add(it) }
        return this
    }

    fun count(): Int = registrations.size
    fun all(): List<EventRegistration<*, *>> = registrations.toList()
}

inline fun <reified E : IBaseEvent<Void>> TaleEventRegistry.on(noinline handler: (E) -> Unit) =
    on(E::class.java, handler)

inline fun <reified E : IBaseEvent<Void>> TaleEventRegistry.on(priority: EventPriority, noinline handler: (E) -> Unit) =
    on(E::class.java, priority, handler)

inline fun <reified E : IBaseEvent<Void>> TaleEventRegistry.on(priority: Short, noinline handler: (E) -> Unit) =
    on(E::class.java, priority, handler)

inline fun <K : Any, reified E : IBaseEvent<K>> TaleEventRegistry.onKeyed(key: K, noinline handler: (E) -> Unit) =
    onKeyed(E::class.java, key, handler)

inline fun <K : Any, reified E : IBaseEvent<K>> TaleEventRegistry.onGlobal(noinline handler: (E) -> Unit) =
    onGlobal(E::class.java, handler)

inline fun <K : Any, reified E : IBaseEvent<K>> TaleEventRegistry.onUnhandled(noinline handler: (E) -> Unit) =
    onUnhandled(E::class.java, handler)
