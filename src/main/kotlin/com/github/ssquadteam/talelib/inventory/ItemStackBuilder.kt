package com.github.ssquadteam.talelib.inventory

import com.hypixel.hytale.server.core.inventory.ItemStack
import org.bson.BsonDocument
import org.bson.BsonString
import org.bson.BsonInt32
import org.bson.BsonDouble
import org.bson.BsonBoolean

class ItemStackBuilder(private var itemId: String) {
    private var quantity: Int = 1
    private var durability: Double? = null
    private var maxDurability: Double? = null
    private var metadata: BsonDocument? = null

    fun quantity(amount: Int): ItemStackBuilder {
        quantity = amount
        return this
    }

    fun durability(value: Double): ItemStackBuilder {
        durability = value
        return this
    }

    fun maxDurability(value: Double): ItemStackBuilder {
        maxDurability = value
        return this
    }

    fun metadata(key: String, value: String): ItemStackBuilder {
        ensureMetadata()
        metadata!!.append(key, BsonString(value))
        return this
    }

    fun metadata(key: String, value: Int): ItemStackBuilder {
        ensureMetadata()
        metadata!!.append(key, BsonInt32(value))
        return this
    }

    fun metadata(key: String, value: Double): ItemStackBuilder {
        ensureMetadata()
        metadata!!.append(key, BsonDouble(value))
        return this
    }

    fun metadata(key: String, value: Boolean): ItemStackBuilder {
        ensureMetadata()
        metadata!!.append(key, BsonBoolean(value))
        return this
    }

    fun metadata(doc: BsonDocument): ItemStackBuilder {
        metadata = doc
        return this
    }

    private fun ensureMetadata() {
        if (metadata == null) {
            metadata = BsonDocument()
        }
    }

    fun build(): ItemStack {
        val dur = durability
        val maxDur = maxDurability

        return if (dur != null && maxDur != null) {
            ItemStack(itemId, quantity, dur, maxDur, metadata)
        } else {
            ItemStack(itemId, quantity, metadata)
        }
    }
}

fun itemStack(itemId: String, block: ItemStackBuilder.() -> Unit = {}): ItemStack {
    return ItemStackBuilder(itemId).apply(block).build()
}

fun itemStack(itemId: String, quantity: Int): ItemStack = ItemStack(itemId, quantity)
