package nimble.portable_blueprints.fabric

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraft.core.BlockPos

/** Preserve the existing .bp coordinate format across Minecraft runtime mappings. */
object BlueprintJson {
    @JvmStatic
    fun create(): Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(BlockPos::class.java, BlockPosAdapter.nullSafe())
        .create()

    private object BlockPosAdapter : TypeAdapter<BlockPos>() {
        override fun write(out: JsonWriter, value: BlockPos) {
            out.beginObject()
            out.name("x").value(value.x)
            out.name("y").value(value.y)
            out.name("z").value(value.z)
            out.endObject()
        }

        override fun read(reader: JsonReader): BlockPos {
            var x: Int? = null
            var y: Int? = null
            var z: Int? = null
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "x" -> x = reader.nextInt()
                    "y" -> y = reader.nextInt()
                    "z" -> z = reader.nextInt()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            return BlockPos(
                x ?: throw JsonParseException("Missing BlockPos x coordinate"),
                y ?: throw JsonParseException("Missing BlockPos y coordinate"),
                z ?: throw JsonParseException("Missing BlockPos z coordinate")
            )
        }
    }
}
