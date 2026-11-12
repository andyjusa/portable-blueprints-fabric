package nimble.portable_blueprints.init
import java.util.function.Supplier
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity
import nimble.portable_blueprints.items.*
object ModItems {
    @JvmField val SCANNER = register("scanner", Scanner(properties(Rarity.UNCOMMON)))
    @JvmField val TABLET = register("tablet", Tablet(properties(Rarity.UNCOMMON)))
    @JvmField val BLUEPRINT = register("blueprint", Blueprint(properties(Rarity.RARE)))
    @JvmField val SERVER_BLUEPRINT = register("server_blueprint", Blueprint(properties(Rarity.UNCOMMON)))
    @JvmField val GLOBAL_BLUEPRINT = register("global_blueprint", Blueprint(properties(Rarity.EPIC)))
    @JvmField val WORN_BLUEPRINT = register("worn_blueprint", WornBlueprint(properties(Rarity.RARE)))
    private fun properties(rarity: Rarity) = Item.Properties().stacksTo(1).rarity(rarity)
    private fun register(name: String, item: Item): Supplier<Item> {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("portable_blueprints", name), item)
        return Supplier { item }
    }
    @JvmStatic fun register() = Unit
    @JvmStatic fun getBlueprintItems(): List<Item> = listOf(BLUEPRINT.get(), SERVER_BLUEPRINT.get(), GLOBAL_BLUEPRINT.get(), WORN_BLUEPRINT.get())
}
