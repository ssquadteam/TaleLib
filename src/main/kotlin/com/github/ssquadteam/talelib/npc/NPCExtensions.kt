package com.github.ssquadteam.talelib.npc

import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.npc.NPCPlugin
import com.hypixel.hytale.server.npc.components.Timers
import com.hypixel.hytale.server.npc.entities.NPCEntity
import com.hypixel.hytale.server.npc.role.Role
import com.hypixel.hytale.server.npc.util.Timer
import java.time.Instant
import java.util.function.BiConsumer

// ============================================
// NPC Reference Extensions
// ============================================

fun Ref<EntityStore>.getNPCEntity(): NPCEntity? {
    if (!this.isValid) return null
    val componentType = NPCEntity.getComponentType() ?: return null
    return this.store.getComponent(this, componentType)
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
        store.removeEntity(this, RemoveReason.REMOVE)
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
        npc.role?.markedEntitySupport?.getMarkedEntityRef(slotName)
    } catch (e: Exception) {
        null
    }
}

fun Ref<EntityStore>.clearNPCTarget(slotName: String): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.markedEntitySupport?.setMarkedEntity(slotName, null)
        true
    } catch (e: Exception) {
        false
    }
}

fun Ref<EntityStore>.hasNPCTarget(slotName: String): Boolean {
    val npc = getNPCEntity() ?: return false
    return try {
        npc.role?.markedEntitySupport?.hasMarkedEntityInSlot(slotName) == true
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

fun Ref<EntityStore>.getTimersComponent(): Timers? {
    if (!this.isValid) return null
    return this.store.getComponent(this, Timers.getComponentType())
}

fun Ref<EntityStore>.getTimerByIndex(index: Int): Timer? {
    val timers = getTimersComponent()?.timers ?: return null
    if (index < 0 || index >= timers.size) return null
    return timers[index] as? Timer
}

fun Ref<EntityStore>.isNPCTimerRunning(index: Int): Boolean {
    return getTimerByIndex(index)?.isRunning == true
}

fun Ref<EntityStore>.isNPCTimerStopped(index: Int): Boolean {
    return getTimerByIndex(index)?.isStopped == true
}

fun Ref<EntityStore>.stopNPCTimer(index: Int): Boolean {
    val timer = getTimerByIndex(index) ?: return false
    return try {
        timer.stop()
        true
    } catch (e: Exception) {
        false
    }
}

// ============================================
// NPC Navigation
// ============================================
// Note: NPC navigation is handled through MotionControllers, not a direct navigationSupport API.
// Navigation state is internal to the NPC role's motion controller system.
// For path-based movement, use the PathManager accessible via NPCEntity.getPathManager().

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

// Note: NPCPlugin doesn't have a direct getRole() method.
// Roles are obtained from NPCEntity instances via getNPCRole() extension function.
// Use NPCPlugin.get().tryGetCachedValidRole(roleIndex) to get a Builder<Role> if needed.

fun npcRoleExists(roleId: String): Boolean {
    return getNPCRoleIndex(roleId) != null
}

// ============================================
// World NPC Queries
// ============================================

/**
 * Gets all NPCs within a given radius of a center point.
 * Note: Uses Store.getEntityCountFor() with NPCEntity query for iteration.
 */
@Suppress("UNCHECKED_CAST")
fun World.getNPCsInRange(center: Vector3d, radius: Double): List<Ref<EntityStore>> {
    val results = mutableListOf<Ref<EntityStore>>()
    val store = this.entityStore.store
    val radiusSq = radius * radius
    val npcComponentType = NPCEntity.getComponentType()
    val transformComponentType = TransformComponent.getComponentType()

    try {
        val query = npcComponentType as Query<EntityStore>
        store.forEachChunk(query, object : BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> {
            override fun accept(chunk: ArchetypeChunk<EntityStore>, buffer: CommandBuffer<EntityStore>) {
                for (i in 0 until chunk.size()) {
                    val ref = chunk.getReferenceTo(i)
                    val transform = store.getComponent(ref, transformComponentType) as? TransformComponent
                    if (transform != null) {
                        val pos = transform.position as Vector3d
                        val distSq = center.distanceSquaredTo(pos)
                        if (distSq <= radiusSq) {
                            results.add(ref)
                        }
                    }
                }
            }
        })
    } catch (e: Exception) {
        // Ignore iteration errors
    }

    return results
}

/**
 * Gets all NPCs with a specific role ID.
 */
@Suppress("UNCHECKED_CAST")
fun World.getNPCsByRole(roleId: String): List<Ref<EntityStore>> {
    val results = mutableListOf<Ref<EntityStore>>()
    val store = this.entityStore.store
    val npcComponentType = NPCEntity.getComponentType()

    try {
        val query = npcComponentType as Query<EntityStore>
        store.forEachChunk(query, object : BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> {
            override fun accept(chunk: ArchetypeChunk<EntityStore>, buffer: CommandBuffer<EntityStore>) {
                for (i in 0 until chunk.size()) {
                    val ref = chunk.getReferenceTo(i)
                    val npc = chunk.getComponent(i, npcComponentType)
                    if (npc != null && npc.roleName == roleId) {
                        results.add(ref)
                    }
                }
            }
        })
    } catch (e: Exception) {
        // Ignore iteration errors
    }

    return results
}

/**
 * Counts all NPCs with a specific role ID.
 */
@Suppress("UNCHECKED_CAST")
fun World.countNPCsByRole(roleId: String): Int {
    var count = 0
    val store = this.entityStore.store
    val npcComponentType = NPCEntity.getComponentType()

    try {
        val query = npcComponentType as Query<EntityStore>
        store.forEachChunk(query, object : BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> {
            override fun accept(chunk: ArchetypeChunk<EntityStore>, buffer: CommandBuffer<EntityStore>) {
                for (i in 0 until chunk.size()) {
                    val npc = chunk.getComponent(i, npcComponentType)
                    if (npc != null && npc.roleName == roleId) {
                        count++
                    }
                }
            }
        })
    } catch (e: Exception) {
        // Ignore iteration errors
    }

    return count
}
