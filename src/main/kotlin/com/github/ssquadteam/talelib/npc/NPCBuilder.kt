package com.github.ssquadteam.talelib.npc

import com.hypixel.hytale.function.consumer.TriConsumer
import com.hypixel.hytale.server.core.asset.type.model.config.Model
import com.hypixel.hytale.server.core.entity.EntityStore
import com.hypixel.hytale.server.core.world.World
import com.hypixel.hytale.server.ecs.Holder
import com.hypixel.hytale.server.ecs.Ref
import com.hypixel.hytale.server.ecs.Store
import com.hypixel.hytale.server.npc.NPCPlugin
import com.hypixel.hytale.server.npc.entities.NPCEntity
import it.unimi.dsi.fastutil.Pair
import org.joml.Vector3d
import org.joml.Vector3f

// ============================================
// NPC Builder
// ============================================

class NPCBuilder {
    private var roleId: String? = null
    private var roleIndex: Int? = null
    private var position: Vector3d? = null
    private var rotation: Vector3f? = null
    private var model: Model? = null
    private var preAddCallback: TriConsumer<NPCEntity, Holder<EntityStore>, Store<EntityStore>>? = null
    private var postSpawnCallback: TriConsumer<NPCEntity, Ref<EntityStore>, Store<EntityStore>>? = null
    private var initialState: String? = null
    private var initialSubState: String? = null

    fun role(roleId: String): NPCBuilder {
        this.roleId = roleId
        this.roleIndex = null
        return this
    }

    fun role(roleIndex: Int): NPCBuilder {
        this.roleIndex = roleIndex
        this.roleId = null
        return this
    }

    fun position(x: Double, y: Double, z: Double): NPCBuilder {
        this.position = Vector3d(x, y, z)
        return this
    }

    fun position(pos: Vector3d): NPCBuilder {
        this.position = Vector3d(pos)
        return this
    }

    fun rotation(yaw: Float, pitch: Float, roll: Float = 0f): NPCBuilder {
        this.rotation = Vector3f(pitch, yaw, roll)
        return this
    }

    fun rotation(rot: Vector3f): NPCBuilder {
        this.rotation = Vector3f(rot)
        return this
    }

    fun facing(yaw: Float): NPCBuilder {
        this.rotation = Vector3f(0f, yaw, 0f)
        return this
    }

    fun model(model: Model): NPCBuilder {
        this.model = model
        return this
    }

    fun initialState(state: String, subState: String? = null): NPCBuilder {
        this.initialState = state
        this.initialSubState = subState
        return this
    }

    fun preAdd(callback: TriConsumer<NPCEntity, Holder<EntityStore>, Store<EntityStore>>): NPCBuilder {
        this.preAddCallback = callback
        return this
    }

    fun postSpawn(callback: TriConsumer<NPCEntity, Ref<EntityStore>, Store<EntityStore>>): NPCBuilder {
        this.postSpawnCallback = callback
        return this
    }

    fun onSpawn(callback: (NPCEntity, Ref<EntityStore>, Store<EntityStore>) -> Unit): NPCBuilder {
        this.postSpawnCallback = TriConsumer { npc, ref, store ->
            callback(npc, ref, store)
        }
        return this
    }

    fun spawn(world: World): Pair<Ref<EntityStore>, NPCEntity>? {
        return spawn(world.entityStore.store)
    }

    fun spawn(store: Store<EntityStore>): Pair<Ref<EntityStore>, NPCEntity>? {
        val resolvedIndex = resolveRoleIndex() ?: return null
        val pos = position ?: return null
        val rot = rotation ?: Vector3f(0f, 0f, 0f)

        val combinedPostSpawn: TriConsumer<NPCEntity, Ref<EntityStore>, Store<EntityStore>>? =
            if (initialState != null || postSpawnCallback != null) {
                TriConsumer { npc, ref, s ->
                    if (initialState != null) {
                        npc.role?.stateSupport?.setState(ref, initialState, initialSubState, s)
                    }
                    postSpawnCallback?.accept(npc, ref, s)
                }
            } else null

        return try {
            NPCPlugin.get().spawnEntity(
                store,
                resolvedIndex,
                pos,
                rot,
                model,
                preAddCallback,
                combinedPostSpawn
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun resolveRoleIndex(): Int? {
        return roleIndex ?: roleId?.let {
            try {
                val index = NPCPlugin.get().getIndex(it)
                if (index == Int.MIN_VALUE) null else index
            } catch (e: Exception) {
                null
            }
        }
    }
}

// ============================================
// Builder Functions
// ============================================

fun npc(block: NPCBuilder.() -> Unit): NPCBuilder {
    return NPCBuilder().apply(block)
}

fun World.spawnNPC(block: NPCBuilder.() -> Unit): Pair<Ref<EntityStore>, NPCEntity>? {
    return NPCBuilder().apply(block).spawn(this)
}

fun World.spawnNPC(roleId: String, x: Double, y: Double, z: Double): Pair<Ref<EntityStore>, NPCEntity>? {
    return spawnNPC {
        role(roleId)
        position(x, y, z)
    }
}

fun World.spawnNPC(roleId: String, position: Vector3d): Pair<Ref<EntityStore>, NPCEntity>? {
    return spawnNPC {
        role(roleId)
        position(position)
    }
}
