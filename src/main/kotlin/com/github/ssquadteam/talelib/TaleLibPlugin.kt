package com.github.ssquadteam.talelib

import com.github.ssquadteam.talelib.entity.EntityEventDispatcher
import com.github.ssquadteam.talelib.entity.ItemEntityWatcher
import com.github.ssquadteam.talelib.entity.ItemQuantityWatcher
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit

class TaleLibPlugin(init: JavaPluginInit) : JavaPlugin(init) {

    companion object {
        private val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()
    }

    override fun setup() {
        LOGGER.atInfo().log("TaleLib v${TaleLib.VERSION} loaded!")
        LOGGER.atInfo().log("A flexible, expandable Kotlin library for Hytale plugin development")

        ItemEntityWatcher.register()
        ItemQuantityWatcher.register()
    }

    override fun shutdown() {
        LOGGER.atInfo().log("TaleLib v${TaleLib.VERSION} unloaded!")

        EntityEventDispatcher.clear()
        ItemQuantityWatcher.clear()
    }
}
