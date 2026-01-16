package com.github.ssquadteam.talelib.npc

import com.hypixel.hytale.server.core.entity.EntityStore
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.world.World
import com.hypixel.hytale.server.ecs.Ref
import com.hypixel.hytale.server.ecs.Store
import com.hypixel.hytale.server.npc.NPCPlugin
import com.hypixel.hytale.server.npc.entities.NPCEntity
import com.hypixel.hytale.server.npc.role.Role
import org.joml.Vector3d
import java.time.Instant

// ============================================
// NPC Reference Extensions
// ============================================

fun Ref<EntityStore>.getNPCEntity(): NPCEntity? {
    if (!this.isValid) return null
    return this.store.getComponent(this, NPCEntity.getComponentType())
}

fun Ref<EntityStore>.isNPC(): Boolean {
    return getNPCEntity() != null
}

fun Ref<EntityStore>.getNPCRole(): Role? {
    return getNPCEntity()?.role
}

fun Ref<EntityStore>.getNPCRoleName(): String? {
    return getNPCEntity()?.roleName
}

fun Ref<EntityStore>.getNPCRoleIndex(): Int? {
    return getNPCEntity()?.roleIndex
}

// ============================================
// NPC State Management
// ============================================

fun Ref<EntityStore>.setNPCState(state: String, subState: String? = null): Boolean {
    val npc = getNPCEntity() ?: return false
    val store = this.store
    return try {
        npc.role?.stateSupport?.setState(this, state, subState, store)
        true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.getNPCState(): String? {
    return getNPCEntity()?.role?.stateSupport?.stateName
}

fun Ref<EntityStore>.getNPCSubState(): String? {
    return getNPCEntity()?.role?.stateSupport?.let {
        // SubState name is typically derived from the state support
        null // The API doesn't expose a direct subState name getter
    }
}

fun Ref<EntityStore>.isInNPCState(state: String): Boolean {
    val npc = getNPCEntity() ?: return false
    val stateIndex = npc.role?.stateSupport?.stateIndex ?: return false
    return npc.role?.stateSupport?.inState(stateIndex) == true
}

fun Ref<EntityStore>.isInNPCState(state: String, subState: String): Boolean {
    val npc = getNPCEntity() ?: return false
    return npc.role?.stateSupport?.inState(state, subState) == true
}

fun Ref<EntityStore>.isNPCBusy(): Boolean {
    return getNPCEntity()?.role?.stateSupport?.isInBusyState == true
}

// ============================================
// NPC Lifecycle
// ============================================

fun Ref<EntityStore>.despawnNPC(): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.setToDespawn()
        true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.despawnNPCAfter(seconds: Float): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.isDespawning
        npc.setToDespawn()
        npc.setDespawnTime(seconds)
        true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.removeNPC(): Boolean {
    if (!this.isValid) return false
    val store = this.store
    return try {
        store.removeEntity(this)
        true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.getNPCSpawnTime(): Instant? {
    return getNPCEntity()?.spawnInstant
}

fun Ref<EntityStore>.isNPCDespawning(): Boolean {
    return getNPCEntity()?.isDespawning == true
}

// ============================================
// NPC Target Management
// ============================================

fun Ref<EntityStore>.setNPCTarget(slotName: String, target: Ref<EntityStore>): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.setMarkedTarget(slotName, target)
        true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.getNPCTarget(slotName: String): Ref<EntityStore>? {
    val npc = getNPCEntity() ?: return null
    return try {
        npc.role?.entitySlots?.getEntity(slotName)
    } catch (e: Exception) {
        null
    }
}

fun Ref<EntityStore>.clearNPCTarget(slotName: String): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.entitySlots?.clearSlot(slotName)
        true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.hasNPCTarget(slotName: String): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.entitySlots?.hasEntity(slotName) == true
    } catch (e: Exception) {
        false
    }
}

// ============================================
// NPC Leash/Position
// ============================================

fun Ref<EntityStore>.getNPCLeashPoint(): Vector3d? {
    return getNPCEntity()?.leashPoint
}

fun Ref<EntityStore>.setNPCLeashPoint(position: Vector3d): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.setLeashPoint(position)
        true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.getNPCLeashHeading(): Float? {
    return getNPCEntity()?.leashHeading
}

fun Ref<EntityStore>.setNPCLeashHeading(heading: Float): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.setLeashHeading(heading)
        true
    } catch (e: Exception) {
        false
    }
}

// ============================================
// NPC Timers
// ============================================

fun Ref<EntityStore>.startNPCTimer(name: String, duration: Float): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.timers?.startTimer(name, duration.toDouble())
        true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.stopNPCTimer(name: String): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.timers?.stopTimer(name)
        true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.isNPCTimerFinished(name: String): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.timers?.isFinished(name) == true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.isNPCTimerRunning(name: String): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.timers?.isRunning(name) == true
    } catch (e: Exception) {
        false
    }
}

// ============================================
// NPC Navigation
// ============================================

fun Ref<EntityStore>.isNPCAtDestination(): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.navigationSupport?.isAtDestination == true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.hasNPCPath(): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.navigationSupport?.hasPath() == true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.isNPCMoving(): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.navigationSupport?.isMoving == true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.isNPCObstructed(): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.navigationSupport?.isObstructed == true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.clearNPCPath(): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.navigationSupport?.clearPath()
        true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.getNPCDestination(): Vector3d? {
    val npc = getNPCEntity() ?: return null
    return try {
        npc.role?.navigationSupport?.destination
    } catch (e: Exception) {
        null
    }
}

fun Ref<EntityStore>.getDistanceToNPCDestination(): Double? {
    val npc = getNPCEntity() ?: return null
    return try {
        npc.role?.navigationSupport?.distanceToDestination
    } catch (e: Exception) {
        null
    }
}

// ============================================
// NPC Appearance
// ============================================

fun Ref<EntityStore>.setNPCAppearance(modelId: String): Boolean {
    if (!this.isValid) return false
    return try {
        NPCEntity.setAppearance(this, modelId, this.store)
    } catch (e: Exception) {
        false
    }
}

// ============================================
// NPC Reservation (for interactions)
// ============================================

fun Ref<EntityStore>.reserveNPC(playerRef: PlayerRef): Boolean {
    val npc = getNPCEntity() ?: return false
    val uuid = playerRef.uuid ?: return false
    return try {
        npc.addReservation(uuid)
        true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.releaseNPCReservation(playerRef: PlayerRef): Boolean {
    val npc = getNPCEntity() ?: return false
    val uuid = playerRef.uuid ?: return false
    return try {
        npc.removeReservation(uuid)
        true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.isNPCReserved(): Boolean {
    return getNPCEntity()?.isReserved == true
}

fun Ref<EntityStore>.isNPCReservedBy(playerRef: PlayerRef): Boolean {
    val npc = getNPCEntity() ?: return false
    val uuid = playerRef.uuid ?: return false
    return npc.isReservedBy(uuid)
}

// ============================================
// NPC Plugin Utilities
// ============================================

fun getNPCRoleIndex(roleId: String): Int? {
    return try {
        val index = NPCPlugin.get().getIndex(roleId)
        if (index == Int.MIN_VALUE) null else index
    } catch (e: Exception) {
        null
    }
}

fun getNPCRoleName(roleIndex: Int): String? {
    return try {
        NPCPlugin.get().getName(roleIndex)
    } catch (e: Exception) {
        null
    }
}

fun getNPCRole(roleIndex: Int): Role? {
    return try {
        NPCPlugin.get().getRole(roleIndex)
    } catch (e: Exception) {
        null
    }
}

fun npcRoleExists(roleId: String): Boolean {
    return getNPCRoleIndex(roleId) != null
}

// ============================================
// World NPC Queries
// ============================================

fun World.getNPCsInRange(center: Vector3d, radius: Double): List<Ref<EntityStore>> {
    val results = mutableListOf<Ref<EntityStore>>()
    val store = this.entityStore.store

    try {
        store.forEach { ref ->
            val npc = store.getComponent(ref, NPCEntity.getComponentType())
            if (npc != null) {
                val transform = store.getComponent(ref, com.hypixel.hytale.server.core.entity.component.TransformComponent.getComponentType())
                if (transform != null) {
                    val pos = transform.position
                    val distSq = center.distanceSquared(pos)
                    if (distSq <= radius * radius) {
                        results.add(ref)
                    }
                }
            }
        }
    } catch (e: Exception) {
        // Ignore iteration errors
    }

    return results
}

fun World.getNPCsByRole(roleId: String): List<Ref<EntityStore>> {
    val results = mutableListOf<Ref<EntityStore>>()
    val store = this.entityStore.store

    try {
        store.forEach { ref ->
            val npc = store.getComponent(ref, NPCEntity.getComponentType())
            if (npc != null && npc.roleName == roleId) {
                results.add(ref)
            }
        }
    } catch (e: Exception) {
        // Ignore iteration errors
    }

    return results
}

fun World.countNPCsByRole(roleId: String): Int {
    var count = 0
    val store = this.entityStore.store

    try {
        store.forEach { ref ->
            val npc = store.getComponent(ref, NPCEntity.getComponentType())
            if (npc != null && npc.roleName == roleId) {
                count++
            }
        }
    } catch (e: Exception) {
        // Ignore iteration errors
    }

    return count
}
