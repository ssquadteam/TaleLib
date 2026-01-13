package com.github.ssquadteam.talelib.lang

import com.github.ssquadteam.talelib.message.toMessage
import com.github.ssquadteam.hytaleminiformat.MiniFormat
import com.github.ssquadteam.talelib.player.send
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.command.system.CommandSender
import com.hypixel.hytale.server.core.Message
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractConfigLang<K> {
    protected val messages: MutableMap<K, String> = ConcurrentHashMap()

    abstract fun getDefaultMessages(): Map<K, String>

    open fun load(overrides: Map<K, String> = emptyMap()) {
        messages.putAll(getDefaultMessages())
        messages.putAll(overrides)
    }

    fun getRaw(key: K): String {
        return messages[key] ?: getDefaultMessages()[key] ?: key.toString()
    }

    fun get(key: K, placeholders: Map<String, String> = emptyMap()): Message {
        var raw = getRaw(key)
        
        placeholders.forEach { (k, v) ->
            raw = raw.replace("{$k}", v).replace("%$k%", v)
        }
        
        return MiniFormat.parse(raw) 
    }

    fun send(player: PlayerRef, key: K, placeholders: Map<String, String> = emptyMap()) {
        player.send(get(key, placeholders))
    }

    fun send(player: PlayerRef, message: String) {
        player.send(message.toMessage())
    }

    fun send(sender: CommandSender, key: K, placeholders: Map<String, String> = emptyMap()) {
        sender.sendMessage(get(key, placeholders))
    }

    fun send(sender: CommandSender, message: String) {
        sender.sendMessage(message.toMessage())
    }
}
