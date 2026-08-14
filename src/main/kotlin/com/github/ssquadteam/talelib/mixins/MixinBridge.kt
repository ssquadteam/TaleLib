package com.github.ssquadteam.talelib.mixins

import java.util.concurrent.ConcurrentHashMap

object MixinBridge {
    private val LOCK = Any()

    fun register(hookKey: String, hook: Any) {
        bridge().put(hookKey, hook)
    }

    fun unregister(hookKey: String) {
        bridge().remove(hookKey)
    }

    private fun bridge(): ConcurrentHashMap<String, Any> {
        val existing = System.getProperties()[BridgeKeys.BRIDGE_KEY]
        if (existing is ConcurrentHashMap<*, *>) return existing as ConcurrentHashMap<String, Any>
        synchronized(LOCK) {
            val current = System.getProperties()[BridgeKeys.BRIDGE_KEY]
            if (current is ConcurrentHashMap<*, *>) return current as ConcurrentHashMap<String, Any>
            val created = ConcurrentHashMap<String, Any>()
            System.getProperties().put(BridgeKeys.BRIDGE_KEY, created)
            return created
        }
    }
}
