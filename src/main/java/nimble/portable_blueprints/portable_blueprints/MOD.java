package nimble.portable_blueprints.portable_blueprints;

import com.mojang.logging.LogUtils;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import nimble.portable_blueprints.Events.BlueprintWorldInjection;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.init.ModEntities;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.init.ModTabs;
import nimble.portable_blueprints.mod_compatibility.ModCompatibilityInit;
import nimble.portable_blueprints.network.PacketHandler;
import org.slf4j.Logger;

public class MOD {
   public static final String MODID = "portable_blueprints";
   public static final Logger LOGGER = LogUtils.getLogger();
   public static final List<String> ListaModContainerNonConsentiti = List.of("storagedrawers");
   public static final List<String> ListForcedNotSolidBlock = List.of(
      "vinery:window", "meadow:heart_patterned_window", "meadow:sun_patterned_window", "meadow:pine_window"
   );

   public static boolean isModContainerConsentito(BlockEntity blockEntity) {
      ResourceLocation id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
      return id == null ? false : !ListaModContainerNonConsentiti.contains(id.getNamespace());
   }

   public static void printLoggerError(String msg) {
      LOGGER.error("[Portable Blueprints] -> " + msg);
   }
}
