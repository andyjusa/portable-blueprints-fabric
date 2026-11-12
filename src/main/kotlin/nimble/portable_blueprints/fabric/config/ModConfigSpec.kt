package nimble.portable_blueprints.fabric.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.function.Predicate

/** Typed JSON configuration shared by common and client code. */
class ModConfigSpec private constructor(private val values: Map<String, ConfigValue<*>>) {
    private var file: Path? = null

    @Synchronized
    fun load(path: Path) {
        file = path
        if (Files.exists(path)) {
            try {
                Files.newBufferedReader(path).use { reader ->
                    val json = JsonParser.parseReader(reader).asJsonObject
                    values.forEach { (key, value) -> json[key]?.let(value::read) }
                }
            } catch (exception: Exception) {
                throw IllegalStateException("Cannot load config $path", exception)
            }
        }
        save()
    }

    @Synchronized
    fun reload() = load(checkNotNull(file) { "Config has not been loaded" })

    @Synchronized
    fun save() {
        val path = file ?: return
        val json = JsonObject().apply { values.forEach { (key, value) -> add(key, gson.toJsonTree(value.get())) } }
        try {
            Files.createDirectories(path.parent)
            val temporary = Files.createTempFile(path.parent, path.fileName.toString(), ".tmp")
            try {
                Files.writeString(temporary, gson.toJson(json) + System.lineSeparator())
                try {
                    Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
                } catch (_: AtomicMoveNotSupportedException) {
                    Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING)
                }
            } finally {
                Files.deleteIfExists(temporary)
            }
        } catch (exception: Exception) {
            throw IllegalStateException("Cannot save config $path", exception)
        }
    }

    open class ConfigValue<T> internal constructor(private val fallback: T, private val validator: Predicate<Any?>) {
        private var value: T = fallback
        fun get(): T = value
        fun set(value: T) {
            require(validator.test(value)) { "Invalid config value: $value" }
            this.value = value
        }

        @Suppress("UNCHECKED_CAST")
        internal fun read(element: JsonElement) {
            try {
                val candidate: Any = when (fallback) {
                    is Boolean -> element.asBoolean
                    is Int -> element.asInt
                    is String -> element.asString
                    is List<*> -> element.asJsonArray.map {
                        require(it.isJsonPrimitive && it.asJsonPrimitive.isString)
                        it.asString
                    }
                    else -> return
                }
                if (validator.test(candidate)) value = candidate as T
            } catch (_: RuntimeException) {
                // Invalid entries retain the typed default/current value.
            }
        }
    }

    class BooleanValue internal constructor(value: Boolean) : ConfigValue<Boolean>(value, Predicate { it is Boolean })

    class Builder {
        private val values = linkedMapOf<String, ConfigValue<*>>()
        fun comment(vararg comments: String): Builder = this
        fun <T : Any> define(name: String, fallback: T): ConfigValue<T> =
            ConfigValue(fallback, Predicate<Any?> { it != null && fallback.javaClass.isInstance(it) }).also { values[name] = it }
        fun define(name: String, fallback: Boolean): BooleanValue = BooleanValue(fallback).also { values[name] = it }
        fun defineListAllowEmpty(name: String, fallback: List<String>, validator: Predicate<Any>): ConfigValue<List<@JvmWildcard String>> =
            ConfigValue(fallback.toList(), Predicate<Any?> { candidate -> candidate is List<*> && candidate.all { it != null && validator.test(it) } }).also { values[name] = it }
        // Keep this map shared: the original callers build the spec before defining its values.
        fun build(): ModConfigSpec = ModConfigSpec(values)
    }

    companion object {
        private val gson = GsonBuilder().setPrettyPrinting().create()
    }
}
