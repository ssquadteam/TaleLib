@file:JvmName("BuilderToolHelpers")

package com.github.ssquadteam.talelib.input

import com.hypixel.hytale.protocol.GameMode
import com.hypixel.hytale.server.core.entity.entities.Player

object BuilderToolPermissions {
    const val BASE = "hytale.editor.builderTools"
    const val HISTORY = "hytale.editor.history"
    const val BRUSH_USE = "hytale.editor.brush.use"
    const val BRUSH_CONFIG = "hytale.editor.brush.config"
    const val SELECTION_USE = "hytale.editor.selection.use"
    const val SELECTION_MODIFY = "hytale.editor.selection.modify"
    const val SELECTION_CLIPBOARD = "hytale.editor.selection.clipboard"
    const val PREFAB_USE = "hytale.editor.prefab.use"
    const val PREFAB_MANAGE = "hytale.editor.prefab.manage"
}

enum class BuilderToolAction {
    UNDO,
    REDO,
    COPY,
    PASTE,
    CUT,
    TOGGLE_TOOLS_ON,
    TOGGLE_TOOLS_OFF,
    SELECTION_UPDATE,
    ROTATE_CLIPBOARD
}

fun Player.hasBuilderToolsPermission(): Boolean =
    this.hasPermission(BuilderToolPermissions.BASE)

fun Player.hasBuilderToolsPermission(permission: String): Boolean =
    this.hasPermission(permission) || this.hasPermission(BuilderToolPermissions.BASE)

val Player.isInCreativeMode: Boolean
    get() = this.gameMode == GameMode.Creative

val Player.canUseBuilderTools: Boolean
    get() = isInCreativeMode && hasBuilderToolsPermission()
