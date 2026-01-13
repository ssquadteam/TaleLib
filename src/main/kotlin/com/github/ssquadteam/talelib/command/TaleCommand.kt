package com.github.ssquadteam.talelib.command

import com.github.ssquadteam.talelib.message.toMessage
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.AbstractCommand
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.CommandSender
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.CompletableFuture
import javax.annotation.Nonnull

abstract class TaleCommand(
    name: String,
    description: String = ""
) : AbstractCommand(name, description) {

    protected fun stringArg(name: String, desc: String = ""): RequiredArg<String> =
        withRequiredArg(name, desc, ArgTypes.STRING)

    protected fun optionalString(name: String, desc: String = ""): OptionalArg<String> =
        withOptionalArg(name, desc, ArgTypes.STRING)

    protected fun intArg(name: String, desc: String = ""): RequiredArg<Int> =
        withRequiredArg(name, desc, ArgTypes.INTEGER)

    protected fun optionalInt(name: String, desc: String = ""): OptionalArg<Int> =
        withOptionalArg(name, desc, ArgTypes.INTEGER)

    protected fun floatArg(name: String, desc: String = ""): RequiredArg<Float> =
        withRequiredArg(name, desc, ArgTypes.FLOAT)

    protected fun optionalFloat(name: String, desc: String = ""): OptionalArg<Float> =
        withOptionalArg(name, desc, ArgTypes.FLOAT)

    protected fun doubleArg(name: String, desc: String = ""): RequiredArg<Double> =
        withRequiredArg(name, desc, ArgTypes.DOUBLE)

    protected fun optionalDouble(name: String, desc: String = ""): OptionalArg<Double> =
        withOptionalArg(name, desc, ArgTypes.DOUBLE)

    protected fun boolArg(name: String, desc: String = ""): RequiredArg<Boolean> =
        withRequiredArg(name, desc, ArgTypes.BOOLEAN)

    protected fun flag(name: String, desc: String = ""): FlagArg =
        withFlagArg(name, desc)

    protected fun playerRefArg(name: String, desc: String = ""): RequiredArg<PlayerRef> =
        withRequiredArg(name, desc, ArgTypes.PLAYER_REF)

    protected fun optionalPlayerRef(name: String, desc: String = ""): OptionalArg<PlayerRef> =
        withOptionalArg(name, desc, ArgTypes.PLAYER_REF)

    protected fun aliases(vararg names: String) = addAliases(*names)
    protected fun subCommand(cmd: AbstractCommand) = addSubCommand(cmd)

    abstract fun onExecute(ctx: TaleContext)

    @Nonnull
    override fun execute(@Nonnull context: CommandContext): CompletableFuture<Void>? {
        onExecute(TaleContext(context))
        return null
    }
}

class TaleContext(val raw: CommandContext) {
    val sender: CommandSender get() = raw.sender()
    val senderName: String get() = sender.displayName

    val isPlayer: Boolean get() = raw.isPlayer

    val player: Player?
        get() = if (isPlayer) raw.senderAs(Player::class.java) else null

    val playerRef: PlayerRef?
        get() = player?.playerRef

    fun <T> get(arg: RequiredArg<T>): T = arg.get(raw)
    fun <T> get(arg: OptionalArg<T>): T? = arg.get(raw)
    fun has(arg: FlagArg): Boolean = arg.get(raw)

    fun reply(message: Message) = raw.sendMessage(message)
    fun reply(text: String) = raw.sendMessage(text.toMessage())

    fun requirePlayer(error: String = "This command requires a player."): Player? {
        if (!isPlayer) reply(error)
        return player
    }
}
