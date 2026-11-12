package nimble.portable_blueprints.items;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.EnvType;
import nimble.portable_blueprints.client.screen.tablet.TabletScreen;

public class Tablet extends Item {
   public Tablet(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
      if (pLevel.isClientSide() && pUsedHand.equals(InteractionHand.MAIN_HAND) && (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) && Minecraft.getInstance().player != null) {
         TabletScreen.OpenGUI();
      }

      return super.use(pLevel, pPlayer, pUsedHand);
   }
}
