package nimble.portable_blueprints.network;

import io.netty.buffer.ByteBuf;
import java.io.File;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;

public record CtoS_RemoveItemIfBlueprintNotExist(String nome, ItemStack item) implements CustomPacketPayload {
   public static final Type<CtoS_RemoveItemIfBlueprintNotExist> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_removeitemifblueprintnotexist")
   );
   public static final StreamCodec<ByteBuf, CtoS_RemoveItemIfBlueprintNotExist> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      CtoS_RemoveItemIfBlueprintNotExist::nome,
      ModCustomCodecs.ITEM_STACK,
      CtoS_RemoveItemIfBlueprintNotExist::item,
      CtoS_RemoveItemIfBlueprintNotExist::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_RemoveItemIfBlueprintNotExist packet, PayloadContext context) {
      context.enqueueWork(
         () -> {
            ServerPlayer player = (ServerPlayer)context.player();
            ServerLevel level = player.serverLevel();
            boolean esiste = false;

            try {
               for (File f : OldVersionUtilities.getItemStackNBT(packet.item).getString("owner").equals("server")
                  ? Blueprint.getServerBlueprintFile(level).listFiles()
                  : (
                     OldVersionUtilities.getItemStackNBT(packet.item).getString("owner").equals("worn")
                        ? Blueprint.getWornBlueprintFile(level).listFiles()
                        : Blueprint.getFileForBlueprints(level, UUID.fromString(OldVersionUtilities.getItemStackNBT(packet.item).getString("owner")))
                           .listFiles()
                  )) {
                  if (f.getName().equals(packet.nome + ".bp")) {
                     esiste = true;
                     break;
                  }
               }
            } catch (Exception var9) {
            }

            if (!esiste) {
               for (int x = 0; x < player.getInventory().getContainerSize(); x++) {
                  if (OldVersionUtilities.getItemStackNBT(player.getInventory().getItem(x)).contains("nome")
                     && player.getInventory().getItem(x).getItem().equals(packet.item.getItem())) {
                     CompoundTag nbt = OldVersionUtilities.getItemStackNBT(player.getInventory().getItem(x));
                     if (nbt.getString("nome").equals(OldVersionUtilities.getItemStackNBT(packet.item).getString("nome"))
                        && nbt.contains("owner")
                        && nbt.getString("owner").equals(OldVersionUtilities.getItemStackNBT(packet.item).getString("owner"))) {
                        player.getInventory().setItem(x, ItemStack.EMPTY);
                        player.displayClientMessage(
                           Component.translatable("hotbar_message.portable_blueprints.blueprint_missing").withStyle(ChatFormatting.RED), true
                        );
                        break;
                     }
                  }
               }
            }
         }
      );
   }
}
