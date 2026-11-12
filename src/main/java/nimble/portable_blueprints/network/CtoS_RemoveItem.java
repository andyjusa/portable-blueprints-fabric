package nimble.portable_blueprints.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;

public record CtoS_RemoveItem(ItemStack item_da_rimuovere) implements CustomPacketPayload {
   public static final Type<CtoS_RemoveItem> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_removeitem"));
   public static final StreamCodec<ByteBuf, CtoS_RemoveItem> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.ITEM_STACK, CtoS_RemoveItem::item_da_rimuovere, CtoS_RemoveItem::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_RemoveItem packet, PayloadContext context) {
      context.enqueueWork(
         () -> {
            ServerPlayer player = (ServerPlayer)context.player();
            ServerLevel level = player.serverLevel();

            for (int x = 0; x < player.getInventory().getContainerSize(); x++) {
               if (player.getInventory().getItem(x).getItem().equals(packet.item_da_rimuovere.getItem())) {
                  CompoundTag nbt = OldVersionUtilities.getItemStackNBT(player.getInventory().getItem(x));
                  if (OldVersionUtilities.getItemStackNBT(packet.item_da_rimuovere).equals(nbt)) {
                     player.getInventory().setItem(x, ItemStack.EMPTY);
                     player.displayClientMessage(
                        Component.translatable("hotbar_message.portable_blueprints.blueprint_missing").withStyle(ChatFormatting.RED), true
                     );
                     break;
                  }
               }
            }
         }
      );
   }
}
