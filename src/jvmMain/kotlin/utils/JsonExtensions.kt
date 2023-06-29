package utils

import kotlinx.serialization.json.*

class JsonElementCastException(msg: String) : Exception(msg)

@Suppress("FunctionName", "unused")
fun JsonElement.Object(key: String): JsonObject {
    val obj = this as? JsonObject ?: throw JsonElementCastException("Cannot cast to JsonObject")
    if (!obj.containsKey(key)) {
        throw JsonElementCastException("Key '$key' not found")
    }
    val value = obj.getValue(key)
    if (value !is JsonObject) {
        throw JsonElementCastException("Value of key '$key' is not a JsonObject, value '$value'")
    }
    return value
}

@Suppress("FunctionName", "unused")
fun JsonElement.Array(key: String): JsonArray {
    val obj = this as? JsonObject ?: throw JsonElementCastException("Cannot cast to JsonObject")
    if (!obj.containsKey(key)) {
        throw JsonElementCastException("Key '$key' not found")
    }
    val value = obj.getValue(key)
    if (value !is JsonArray) {
        throw JsonElementCastException("Value of key '$key' is not a JsonArray, value '$value'")
    }
    return value
}

@Suppress("FunctionName", "unused")
fun JsonElement.ObjectArray(key: String): List<JsonObject> {
    val obj = this as? JsonObject ?: throw JsonElementCastException("Cannot cast to JsonObject")
    if (!obj.containsKey(key)) {
        throw JsonElementCastException("Key '$key' not found")
    }
    val value = obj.getValue(key)
    if (value !is JsonArray) {
        throw JsonElementCastException("Value of key '$key' is not a JsonArray, value '$value'")
    }
    if (!value.all { it is JsonObject })
        throw JsonElementCastException("Values of key '$key' is not all JsonObject, value '$value'")

    return value.map { it as JsonObject }
}

@Suppress("unused")
val JsonElement.objectArray: List<JsonObject>
    get() {
        val value = this
        if (value !is JsonArray) {
            throw JsonElementCastException("This is not a JsonArray, this '$value'")
        }
        if (!value.all { it is JsonObject })
            throw JsonElementCastException("Values of this array are not all JsonObject, this '$value'")

        return value.map { it as JsonObject }
    }

@Suppress("unused")
fun JsonElement.String(key: String): String {
    val obj = this as? JsonObject ?: throw JsonElementCastException("Cannot cast to JsonObject")
    if (!obj.containsKey(key)) {
        throw JsonElementCastException("Key '$key' not found")
    }
    val jsonPrimitive = try {
        obj.getValue(key).jsonPrimitive
    } catch (_: IllegalArgumentException) {
        throw JsonElementCastException("Value of key '$key' is not a JsonPrimitive, value '${obj.getValue(key)}'")
    }
    val value = jsonPrimitive.contentOrNull
        ?: throw JsonElementCastException("Value of key '$key' is not a String, value '${obj.getValue(key)}'")
    return value
}

@Suppress("FunctionName", "unused")
fun JsonElement.StringOrNull(key: String): String? {
    val obj = this as? JsonObject ?: throw JsonElementCastException("Cannot cast to JsonObject")
    if (!obj.containsKey(key)) {
        throw JsonElementCastException("Key '$key' not found")
    }
    val jsonPrimitive = try {
        obj.getValue(key).jsonPrimitive
    } catch (_: IllegalArgumentException) {
        throw JsonElementCastException("Value of key '$key' is not a JsonPrimitive, value '${obj.getValue(key)}'")
    }
    return jsonPrimitive.contentOrNull
}

@Suppress("unused")
fun JsonElement.Int(key: String): Int {
    val obj = this as? JsonObject ?: throw JsonElementCastException("Cannot cast to JsonObject")
    if (!obj.containsKey(key)) {
        throw JsonElementCastException("Key '$key' not found")
    }
    val jsonPrimitive = try {
        obj.getValue(key).jsonPrimitive
    } catch (_: IllegalArgumentException) {
        throw JsonElementCastException("Value of key '$key' is not a JsonPrimitive, value '${obj.getValue(key)}'")
    }
    val value = jsonPrimitive.intOrNull
        ?: throw JsonElementCastException("Value of key '$key' is not an Int, value '${obj.getValue(key)}'")
    return value
}

@Suppress("unused")
fun JsonElement.Long(key: String): Long {
    val obj = this as? JsonObject ?: throw JsonElementCastException("Cannot cast to JsonObject")
    if (!obj.containsKey(key)) {
        throw JsonElementCastException("Key '$key' not found")
    }
    val jsonPrimitive = try {
        obj.getValue(key).jsonPrimitive
    } catch (_: IllegalArgumentException) {
        throw JsonElementCastException("Value of key '$key' is not a JsonPrimitive, value '${obj.getValue(key)}'")
    }
    val value = jsonPrimitive.longOrNull
        ?: throw JsonElementCastException("Value of key '$key' is not a Long, value '${obj.getValue(key)}'")
    return value
}

@Suppress("unused")
fun JsonElement.Float(key: String): Float {
    val obj = this as? JsonObject ?: throw JsonElementCastException("Cannot cast to JsonObject")
    if (!obj.containsKey(key)) {
        throw JsonElementCastException("Key '$key' not found")
    }
    val jsonPrimitive = try {
        obj.getValue(key).jsonPrimitive
    } catch (_: IllegalArgumentException) {
        throw JsonElementCastException("Value of key '$key' is not a JsonPrimitive, value '${obj.getValue(key)}'")
    }
    val value = jsonPrimitive.floatOrNull
        ?: throw JsonElementCastException("Value of key '$key' is not a Float, value '${obj.getValue(key)}'")
    return value
}

@Suppress("unused")
fun JsonElement.Double(key: String): Double {
    val obj = this as? JsonObject ?: throw JsonElementCastException("Cannot cast to JsonObject")
    if (!obj.containsKey(key)) {
        throw JsonElementCastException("Key '$key' not found")
    }
    val jsonPrimitive = try {
        obj.getValue(key).jsonPrimitive
    } catch (_: IllegalArgumentException) {
        throw JsonElementCastException("Value of key '$key' is not a JsonPrimitive, value '${obj.getValue(key)}'")
    }
    val value = jsonPrimitive.doubleOrNull
        ?: throw JsonElementCastException("Value of key '$key' is not a Double, value '${obj.getValue(key)}'")
    return value
}
