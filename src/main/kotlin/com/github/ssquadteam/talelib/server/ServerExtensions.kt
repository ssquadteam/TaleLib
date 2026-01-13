package com.github.ssquadteam.talelib.server

import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe

val HytaleServer.onlinePlayers: List<PlayerRef>
    get() = Universe.get().getPlayers()
