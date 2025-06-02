package no.nav.pensjon.client

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.util.TreeMap

object ResponseFormatter{
    val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        allowSpecialFloatingPointValues = true
    }

    fun format(response: String): String {
        val jsonElement = json.parseToJsonElement(response)
        val sortedJsonElement = sortJsonElement(jsonElement)
        return json.encodeToString(sortedJsonElement)
    }

    fun sortJsonElement(element: JsonElement): JsonElement {
        return when (element) {
            is JsonObject -> {
                val sortedMap = TreeMap<String, JsonElement>()
                element.forEach { (key, value) ->
                    sortedMap[key] = sortJsonElement(value)
                }
                JsonObject(sortedMap)
            }
            is JsonArray -> {
                JsonArray(element.map { sortJsonElement(it) })
            }
            else -> element
        }
    }

}