package nimble.portable_blueprints.init;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import nimble.portable_blueprints.network.update_item_blueprint_nbt.CtoS_UpdateItemBlueprintNBT;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.Operazioni;

public class ModKeyMappings {
   public static final KeyMapping move_origin_up = new KeyMapping(
      "portable_blueprints.key_mapping.move_origin.up", 266, "portable_blueprints.key_mapping.category"
   ) {
      private boolean isDownOld = false;

      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            ModKeyMappings.moveOriginByOffset(new Vec3i(0, 1, 0));
         } else if (this.isDownOld != isDown && !isDown) {
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping move_origin_down = new KeyMapping(
      "portable_blueprints.key_mapping.move_origin.down", 267, "portable_blueprints.key_mapping.category"
   ) {
      private boolean isDownOld = false;

      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            ModKeyMappings.moveOriginByOffset(new Vec3i(0, -1, 0));
         } else if (this.isDownOld != isDown && !isDown) {
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping move_origin_px = new KeyMapping(
      "portable_blueprints.key_mapping.move_origin.px", 263, "portable_blueprints.key_mapping.category"
   ) {
      private boolean isDownOld = false;

      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            ModKeyMappings.moveOriginByOffset(new Vec3i(1, 0, 0));
         } else if (this.isDownOld != isDown && !isDown) {
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping move_origin_nx = new KeyMapping(
      "portable_blueprints.key_mapping.move_origin.nx", 262, "portable_blueprints.key_mapping.category"
   ) {
      private boolean isDownOld = false;

      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            ModKeyMappings.moveOriginByOffset(new Vec3i(-1, 0, 0));
         } else if (this.isDownOld != isDown && !isDown) {
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping move_origin_pz = new KeyMapping(
      "portable_blueprints.key_mapping.move_origin.pz", 265, "portable_blueprints.key_mapping.category"
   ) {
      private boolean isDownOld = false;

      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            ModKeyMappings.moveOriginByOffset(new Vec3i(0, 0, 1));
         } else if (this.isDownOld != isDown && !isDown) {
         }

         this.isDownOld = isDown;
      }
   };
   public static final KeyMapping move_origin_nz = new KeyMapping(
      "portable_blueprints.key_mapping.move_origin.nz", 264, "portable_blueprints.key_mapping.category"
   ) {
      private boolean isDownOld = false;

      public void setDown(boolean isDown) {
         super.setDown(isDown);
         if (this.isDownOld != isDown && isDown) {
            ModKeyMappings.moveOriginByOffset(new Vec3i(0, 0, -1));
         } else if (this.isDownOld != isDown && !isDown) {
         }

         this.isDownOld = isDown;
      }
   };

   public static void registerKeyMappings() {
      KeyBindingHelper.registerKeyBinding(move_origin_up);
      KeyBindingHelper.registerKeyBinding(move_origin_down);
      KeyBindingHelper.registerKeyBinding(move_origin_px);
      KeyBindingHelper.registerKeyBinding(move_origin_nx);
      KeyBindingHelper.registerKeyBinding(move_origin_pz);
      KeyBindingHelper.registerKeyBinding(move_origin_nz);
   }

   public static void moveOriginByOffset(Vec3i offset) {
      Player player = Minecraft.getInstance().player;
      if (player != null && Minecraft.getInstance().screen == null) {
         ItemStack itemStack = Minecraft.getInstance().player.getMainHandItem();
         if (!itemStack.isEmpty() && ModItems.getBlueprintItems().contains(itemStack.getItem())) {
            CompoundTag nbt = OldVersionUtilities.getItemStackNBT(itemStack);
            if (nbt.contains("originPos")) {
               CtoS_UpdateItemBlueprintNBT.send(itemStack, "originPos", new BlockPos(Operazioni.getVec3iFromString(nbt.getString("originPos")).offset(offset)));
            }
         }
      }
   }

   public static class KeyEventListener {
      public static void onClientTick() {
         if (Minecraft.getInstance().screen == null) {
            ModKeyMappings.move_origin_up.consumeClick();
            ModKeyMappings.move_origin_down.consumeClick();
            ModKeyMappings.move_origin_px.consumeClick();
            ModKeyMappings.move_origin_nx.consumeClick();
            ModKeyMappings.move_origin_pz.consumeClick();
            ModKeyMappings.move_origin_nz.consumeClick();
         }
      }
   }
}
