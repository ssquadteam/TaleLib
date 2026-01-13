package com.github.ssquadteam.talelib.util

import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.JavaPlugin

class TaleLogger private constructor(private val logger: HytaleLogger) {

    companion object {
        fun forPlugin(plugin: JavaPlugin): TaleLogger = TaleLogger(plugin.logger)
        fun forName(name: String): TaleLogger = TaleLogger(HytaleLogger.get(name))
        fun forClass(): TaleLogger = TaleLogger(HytaleLogger.forEnclosingClass())
    }

    fun info(message: String) = logger.atInfo().log(message)
    fun info(message: String, vararg args: Any?) = logger.atInfo().log(message.format(*args))

    fun debug(message: String) = logger.atFine().log(message)
    fun debug(message: String, vararg args: Any?) = logger.atFine().log(message.format(*args))

    fun warn(message: String) = logger.atWarning().log(message)
    fun warn(message: String, vararg args: Any?) = logger.atWarning().log(message.format(*args))
    fun warn(message: String, throwable: Throwable) = logger.atWarning().withCause(throwable).log(message)

    fun error(message: String) = logger.atSevere().log(message)
    fun error(message: String, vararg args: Any?) = logger.atSevere().log(message.format(*args))
    fun error(message: String, throwable: Throwable) = logger.atSevere().withCause(throwable).log(message)

    fun infoIf(condition: Boolean, message: () -> String) { if (condition) info(message()) }
    fun debugIf(condition: Boolean, message: () -> String) { if (condition) debug(message()) }
    fun warnIf(condition: Boolean, message: () -> String) { if (condition) warn(message()) }
}

fun JavaPlugin.taleLogger(): TaleLogger = TaleLogger.forPlugin(this)
