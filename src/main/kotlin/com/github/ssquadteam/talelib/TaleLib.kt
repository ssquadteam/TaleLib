package com.github.ssquadteam.talelib

import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import java.util.concurrent.ConcurrentHashMap

object TaleLib {
    private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()
    const val VERSION = "0.2.0"

    private val pluginInstances = ConcurrentHashMap<String, JavaPlugin>()

    fun register(plugin: JavaPlugin) {
        pluginInstances[plugin.name] = plugin
        LOGGER.atInfo().log("TaleLib: Registered plugin ${plugin.name}")
    }

    fun unregister(plugin: JavaPlugin) {
        pluginInstances.remove(plugin.name)
        LOGGER.atInfo().log("TaleLib: Unregistered plugin ${plugin.name}")
    }

    fun getPlugin(name: String): JavaPlugin? = pluginInstances[name]
    fun isRegistered(name: String): Boolean = pluginInstances.containsKey(name)
    fun server(): HytaleServer = HytaleServer.get()
    fun allPlugins(): Collection<JavaPlugin> = pluginInstances.values
}
