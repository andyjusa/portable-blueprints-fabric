package nimble.portable_blueprints.init
import java.util.function.Supplier
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import nimble.portable_blueprints.entity.custom.BusinessVillagerEntity
object ModEntities {
    private val type: EntityType<BusinessVillagerEntity> = Registry.register(BuiltInRegistries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("portable_blueprints", "business_villager"), EntityType.Builder.of(::BusinessVillagerEntity, MobCategory.MISC).clientTrackingRange(64).updateInterval(3).sized(0.6F, 1.8F).build("portable_blueprints:business_villager"))
    @JvmField val BUSINESS_VILLAGER = Supplier { type }
    @JvmStatic fun register() { FabricDefaultAttributeRegistry.register(type, BusinessVillagerEntity.createAttributes()) }
}
