package nimble.portable_blueprints.Events;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import nimble.portable_blueprints.entity.client.BusinessVillagerRenderer;
import nimble.portable_blueprints.init.ModEntities;

public class ModClientEvents {
   public static void registerRenderers() {
      EntityRendererRegistry.register(ModEntities.BUSINESS_VILLAGER.get(), BusinessVillagerRenderer::new);
   }
}
