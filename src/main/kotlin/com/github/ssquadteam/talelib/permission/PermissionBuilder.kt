package com.github.ssquadteam.talelib.permission

import com.hypixel.hytale.server.core.universe.PlayerRef

class PermissionBuilder(private val player: PlayerRef) {
    private val toAdd = mutableSetOf<String>()
    private val toRemove = mutableSetOf<String>()
    private val groupsToJoin = mutableSetOf<String>()
    private val groupsToLeave = mutableSetOf<String>()

    fun grant(vararg perms: String): PermissionBuilder {
        toAdd.addAll(perms)
        return this
    }

    fun revoke(vararg perms: String): PermissionBuilder {
        toRemove.addAll(perms)
        return this
    }

    fun joinGroup(vararg groups: String): PermissionBuilder {
        groupsToJoin.addAll(groups)
        return this
    }

    fun leaveGroup(vararg groups: String): PermissionBuilder {
        groupsToLeave.addAll(groups)
        return this
    }

    fun apply() {
        if (toAdd.isNotEmpty()) {
            player.addPermission(*toAdd.toTypedArray())
        }
        if (toRemove.isNotEmpty()) {
            player.removePermission(*toRemove.toTypedArray())
        }
        groupsToJoin.forEach { player.addToGroup(it) }
        groupsToLeave.forEach { player.removeFromGroup(it) }
    }
}

fun PlayerRef.permissions(block: PermissionBuilder.() -> Unit) {
    PermissionBuilder(this).apply(block).apply()
}
