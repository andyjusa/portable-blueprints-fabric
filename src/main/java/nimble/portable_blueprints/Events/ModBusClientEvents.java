package nimble.portable_blueprints.Events;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import nimble.portable_blueprints.entity.client.BusinessVillagerModel;
import nimble.portable_blueprints.entity.client.ModModelLayers;

public class ModBusClientEvents {
   public static void registerLayer() {
      EntityModelLayerRegistry.registerModelLayer(ModModelLayers.BUSINESS_VILLAGER_LAYER, BusinessVillagerModel::createBodyLayer);
   }
}
