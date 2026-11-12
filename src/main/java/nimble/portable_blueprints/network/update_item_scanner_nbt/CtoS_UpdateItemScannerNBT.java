package nimble.portable_blueprints.network.update_item_scanner_nbt;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;

public record CtoS_UpdateItemScannerNBT(ItemStack item, String nome, String valore_stringa, boolean is_valore_string) implements CustomPacketPayload {
   public static final Type<CtoS_UpdateItemScannerNBT> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_updateitemscannernbt")
   );
   public static final StreamCodec<ByteBuf, CtoS_UpdateItemScannerNBT> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.ITEM_STACK,
      CtoS_UpdateItemScannerNBT::item,
      ByteBufCodecs.STRING_UTF8,
      CtoS_UpdateItemScannerNBT::nome,
      ByteBufCodecs.STRING_UTF8,
      CtoS_UpdateItemScannerNBT::valore_stringa,
      ByteBufCodecs.BOOL,
      CtoS_UpdateItemScannerNBT::is_valore_string,
      CtoS_UpdateItemScannerNBT::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_UpdateItemScannerNBT packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         if (player != null) {
            ItemStack inhand = player.getMainHandItem();
            CompoundTag nbt = OldVersionUtilities.getItemStackNBT(inhand);
            if (packet.is_valore_string && (packet.nome.equals("coordinateAngolo1") || packet.nome.equals("coordinateAngolo2"))) {
               try {
                  var pos = new net.minecraft.core.BlockPos(nimble.portable_blueprints.robautile.Operazioni.getVec3iFromString(packet.valore_stringa));
                  if (!nimble.portable_blueprints.fabric.network.PayloadValidation.validPosition(player, pos)) return;
                  nbt.putString(packet.nome, packet.valore_stringa);
               } catch (RuntimeException invalidCoordinates) {
                  return;
               }
            }

            OldVersionUtilities.impostaItemStackNBT(inhand, nbt);
         }
      });
   }

   public static void send(ItemStack item, String nome, String valore_stringa) {
      PacketDistributor.sendToServer(new CtoS_UpdateItemScannerNBT(item, nome, valore_stringa, true), new CustomPacketPayload[0]);
   }
}
