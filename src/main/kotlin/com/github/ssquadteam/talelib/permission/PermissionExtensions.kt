@file:JvmName("PermissionExtensions")

package com.github.ssquadteam.talelib.permission

import com.hypixel.hytale.server.core.permissions.PermissionsModule
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.*

private val permissions: PermissionsModule get() = PermissionsModule.get()

fun PlayerRef.hasPermission(permission: String): Boolean =
    permissions.hasPermission(uuid, permission)

fun PlayerRef.hasPermission(permission: String, default: Boolean): Boolean =
    permissions.hasPermission(uuid, permission, default)

fun PlayerRef.addPermission(vararg perms: String) {
    if (perms.isNotEmpty()) {
        permissions.addUserPermission(uuid, perms.toSet())
    }
}

fun PlayerRef.removePermission(vararg perms: String) {
    if (perms.isNotEmpty()) {
        permissions.removeUserPermission(uuid, perms.toSet())
    }
}

fun PlayerRef.addToGroup(group: String) {
    permissions.addUserToGroup(uuid, group)
}

fun PlayerRef.removeFromGroup(group: String) {
    permissions.removeUserFromGroup(uuid, group)
}

val PlayerRef.groups: Set<String>
    get() = permissions.getGroupsForUser(uuid)

fun PlayerRef.isInGroup(group: String): Boolean = group in groups

fun UUID.hasPermission(permission: String): Boolean =
    permissions.hasPermission(this, permission)

fun UUID.hasPermission(permission: String, default: Boolean): Boolean =
    permissions.hasPermission(this, permission, default)

fun UUID.addPermission(vararg perms: String) {
    if (perms.isNotEmpty()) {
        permissions.addUserPermission(this, perms.toSet())
    }
}

fun UUID.removePermission(vararg perms: String) {
    if (perms.isNotEmpty()) {
        permissions.removeUserPermission(this, perms.toSet())
    }
}

fun UUID.addToGroup(group: String) {
    permissions.addUserToGroup(this, group)
}

fun UUID.removeFromGroup(group: String) {
    permissions.removeUserFromGroup(this, group)
}

fun UUID.getGroups(): Set<String> = permissions.getGroupsForUser(this)

fun addGroupPermission(group: String, vararg perms: String) {
    if (perms.isNotEmpty()) {
        permissions.addGroupPermission(group, perms.toSet())
    }
}

fun removeGroupPermission(group: String, vararg perms: String) {
    if (perms.isNotEmpty()) {
        permissions.removeGroupPermission(group, perms.toSet())
    }
}
