package com.github.ssquadteam.talelib.command

import com.hypixel.hytale.server.core.command.system.AbstractCommand
import com.hypixel.hytale.server.core.plugin.JavaPlugin

class TaleCommandRegistry(private val plugin: JavaPlugin) {

    private val commands = mutableListOf<AbstractCommand>()

    fun register(command: AbstractCommand): TaleCommandRegistry {
        plugin.commandRegistry.registerCommand(command)
        commands.add(command)
        return this
    }

    fun registerAll(vararg cmds: AbstractCommand): TaleCommandRegistry {
        cmds.forEach { register(it) }
        return this
    }

    fun all(): List<AbstractCommand> = commands.toList()
    fun count(): Int = commands.size
}
