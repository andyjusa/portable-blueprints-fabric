package nimble.portable_blueprints.init
import java.util.function.Supplier
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
object ModTabs {
    private val tab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath("portable_blueprints", "portable_blueprints"), FabricItemGroup.builder().title(Component.translatable("tab.portable_blueprints.title")).icon { ItemStack(ModItems.BLUEPRINT.get()) }.displayItems { _, entries -> entries.accept(ModItems.SCANNER.get()); entries.accept(ModItems.TABLET.get()) }.build())
    @JvmField val PORTABLE_BLUEPRINTS = Supplier { tab }
    @JvmStatic fun register() = Unit
}
