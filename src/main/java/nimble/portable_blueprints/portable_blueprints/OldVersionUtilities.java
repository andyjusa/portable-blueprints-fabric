package nimble.portable_blueprints.portable_blueprints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import nimble.portable_blueprints.robautile.Operazioni;
import org.joml.Quaternionf;

public class OldVersionUtilities {
   private static final Gson GSON = new GsonBuilder()
      .registerTypeAdapter(
         Optional.class, (JsonSerializer<Optional<?>>)(src, typeOfSrc, context) -> (JsonElement)(src.isPresent() ? context.serialize(src.get()) : JsonNull.INSTANCE)
      )
      .create();

   public static CompoundTag getItemStackNBT(ItemStack stack) {
      return stack != null && !stack.isEmpty() && stack.get(DataComponents.CUSTOM_DATA) != null
         ? ((CustomData)stack.get(DataComponents.CUSTOM_DATA)).copyTag()
         : new CompoundTag();
   }

   public static void impostaItemStackNBT(ItemStack stack, CompoundTag nbt) {
      if (stack != null && !stack.isEmpty()) {
         stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
      }
   }

   public static PoseStack getEventPosestack() {
      PoseStack p = new PoseStack();
      Quaternionf rotation = new Quaternionf(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
      p.mulPose(rotation.invert());
      return p;
   }

   public static ItemContainerContents getItemInventoryData(ItemStack stack) {
      return stack != null && !stack.isEmpty() && stack.get(DataComponents.CONTAINER) != null
         ? (ItemContainerContents)stack.get(DataComponents.CONTAINER)
         : null;
   }

   public static void impostaItemInventoryData(ItemStack stack, ItemContainerContents icc) {
      if (stack != null && !stack.isEmpty()) {
         stack.set(DataComponents.CONTAINER, icc);
      }
   }

   public static String getAllComponentasAndNBTInJson(BlockEntity blockEntity) {
      String nbt = "";

      try {
         DataComponentMap map = blockEntity.collectComponents();
         JsonObject obj = new JsonObject();

         for (DataComponentType<?> entry : map.keySet()) {
            try {
               obj.addProperty(entry.toString(), Objects.requireNonNull(map.get(entry)).toString());
            } catch (Exception var7) {
            }
         }

         String json = obj.toString();
         if (json != null && !json.isEmpty() && !json.equals("null")) {
            nbt = "<components>" + json + "</components>";
         }

         if (blockEntity.getLevel() != null) {
            nbt = nbt + "<nbt>" + blockEntity.getUpdateTag(blockEntity.getLevel().registryAccess()) + "</nbt>";
         }
      } catch (Exception e) {
         MOD.LOGGER.error("[Portable Blueprint] error extracting block entity nbt");
      }

      return nbt;
   }

   public static Tuple<DataComponentMap, CompoundTag> convertJsonToDataComponentMapAndNBT(String json) {
      DataComponentMap map = DataComponentMap.EMPTY;
      CompoundTag nbt = new CompoundTag();

      try {
         if (json.contains("<components>")) {
            map = (DataComponentMap)GSON.fromJson(Operazioni.getValueInsideTag(json, "components"), DataComponentMap.class);
         }
      } catch (Exception e) {
         MOD.LOGGER.error("[Portable Blueprint] error load block entity nbt id:1");
         System.out.println("DataComponentMap ERROR!");
      }

      try {
         if (json.contains("<nbt>") && TagParser.parseTag(Operazioni.getValueInsideTag(json, "nbt")) != null) {
            nbt = TagParser.parseTag(Operazioni.getValueInsideTag(json, "nbt"));
         }
      } catch (Exception e) {
         MOD.LOGGER.error("[Portable Blueprint] error load block entity nbt id:2");
         System.out.println("NBT ERROR!");
      }

      return new Tuple(map, nbt);
   }
}
