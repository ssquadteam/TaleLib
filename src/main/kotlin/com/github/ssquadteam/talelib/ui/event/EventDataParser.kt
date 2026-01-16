@file:JvmName("EventDataParserKt")

package com.github.ssquadteam.talelib.ui.event

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Parsed event data from UI events.
 *
 * UI events are received as JSON strings like:
 * {"Action":"buttonId","Value":"inputValue"}
 */
class ParsedEventData(
    private val data: Map<String, String>
) {
    /**
     * Get the action from the event data
     */
    val action: String? get() = data["Action"]

    /**
     * Get a captured value from the event data
     */
    val value: String? get() = data["Value"] ?: data["@Value"]

    /**
     * Get a specific field by key
     */
    operator fun get(key: String): String? = data[key]

    /**
     * Check if a field exists
     */
    fun has(key: String): Boolean = data.containsKey(key)

    /**
     * Get all data as a map
     */
    fun toMap(): Map<String, String> = data

    /**
     * Check if this event matches a specific action
     */
    fun isAction(vararg actions: String): Boolean = action in actions

    override fun toString(): String = "ParsedEventData(action=$action, data=$data)"

    companion object {
        val EMPTY = ParsedEventData(emptyMap())
    }
}

/**
 * Parse raw event data string into ParsedEventData.
 *
 * Handles both JSON format {"Action":"value"} and raw strings.
 */
fun parseEventData(rawData: String?): ParsedEventData {
    if (rawData.isNullOrBlank()) return ParsedEventData.EMPTY

    return try {
        val json = Json.parseToJsonElement(rawData)
        val map = mutableMapOf<String, String>()

        json.jsonObject.forEach { (key, value) ->
            try {
                map[key] = value.jsonPrimitive.content
            } catch (e: Exception) {
                // Skip non-primitive values
            }
        }

        ParsedEventData(map)
    } catch (e: Exception) {
        // Not JSON - treat raw string as action
        ParsedEventData(mapOf("Action" to rawData))
    }
}

/**
 * Extract just the action from raw event data.
 *
 * Convenience function for simple event handling.
 */
fun parseAction(rawData: String?): String? {
    return parseEventData(rawData).action
}

/**
 * Extension to check if raw event data matches an action
 */
fun String?.isAction(vararg actions: String): Boolean {
    val parsed = parseEventData(this)
    return parsed.action in actions
}
