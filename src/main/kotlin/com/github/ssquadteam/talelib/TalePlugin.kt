package com.github.ssquadteam.talelib

import com.github.ssquadteam.talelib.command.TaleCommandRegistry
import com.github.ssquadteam.talelib.event.TaleEventRegistry
import com.github.ssquadteam.talelib.scheduler.TaleScheduler
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import java.nio.file.Path

abstract class TalePlugin(init: JavaPluginInit) : JavaPlugin(init) {

    val pluginDataPath: Path get() = dataDirectory
    val taleCommands: TaleCommandRegistry by lazy { TaleCommandRegistry(this) }
    val taleEvents: TaleEventRegistry by lazy { TaleEventRegistry(this) }
    val taleScheduler: TaleScheduler by lazy { TaleScheduler(this) }

    override fun setup() {
        onSetup()
    }

    override fun start() {
        onStart()
    }

    override fun shutdown() {
        taleScheduler.cancelAll()
        onShutdown()
    }

    open fun onSetup() {}
    open fun onStart() {}
    open fun onShutdown() {}

    fun info(message: String) = logger.atInfo().log(message)
    fun warn(message: String) = logger.atWarning().log(message)
    fun error(message: String) = logger.atSevere().log(message)
    fun debug(message: String) = logger.atFine().log(message)
}
