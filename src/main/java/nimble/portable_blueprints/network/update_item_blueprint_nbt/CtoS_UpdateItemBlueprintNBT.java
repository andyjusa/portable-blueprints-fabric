package nimble.portable_blueprints.network.update_item_blueprint_nbt;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
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
import nimble.portable_blueprints.client.VisualizeBlueprintDatas;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.Operazioni;

public record CtoS_UpdateItemBlueprintNBT(ItemStack item, String nome, boolean valore_booleano, double valore_double, BlockPos posNuova)
   implements CustomPacketPayload {
   public static final Type<CtoS_UpdateItemBlueprintNBT> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_updateitemblueprintnbt")
   );
   public static final StreamCodec<ByteBuf, CtoS_UpdateItemBlueprintNBT> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.ITEM_STACK,
      CtoS_UpdateItemBlueprintNBT::item,
      ByteBufCodecs.STRING_UTF8,
      CtoS_UpdateItemBlueprintNBT::nome,
      ByteBufCodecs.BOOL,
      CtoS_UpdateItemBlueprintNBT::valore_booleano,
      ByteBufCodecs.DOUBLE,
      CtoS_UpdateItemBlueprintNBT::valore_double,
      ModCustomCodecs.BLOCKPOS,
      CtoS_UpdateItemBlueprintNBT::posNuova,
      CtoS_UpdateItemBlueprintNBT::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_UpdateItemBlueprintNBT packet, PayloadContext context) {
      context.enqueueWork(
         () -> {
            ServerPlayer player = (ServerPlayer)context.player();
            ItemStack inhand = player.getMainHandItem();
            CompoundTag nbt = OldVersionUtilities.getItemStackNBT(inhand);
            if (packet.nome.equals("skipObstructionBlock")
               || packet.nome.equals("mirrorX")
               || packet.nome.equals("mirrorY")
               || packet.nome.equals("mirrorZ")
               || packet.nome.equals("buildAnyway")
               || packet.nome.equals("projecte_active_emc")) {
               nbt.putBoolean(packet.nome, packet.valore_booleano);
            } else if (packet.nome.equals("rotateValue")) {
               nbt.putDouble(packet.nome, packet.valore_double);
            } else if (packet.nome.equals("originPos")) {
               nbt.putString(packet.nome, Operazioni.getVec3iString(packet.posNuova));
            } else if (packet.nome.equals("autofillmode")) {
               nbt.putShort(packet.nome, (short)packet.valore_double);
            }

            OldVersionUtilities.impostaItemStackNBT(inhand, nbt);
            PacketDistributor.sendToPlayer(player, new StoC_UpdateItemBlueprintNBT(!packet.nome.equals("originPos"), true), new CustomPacketPayload[0]);
         }
      );
   }

   public static void send(ItemStack item, String nome, boolean valore_booleano) {
      PacketDistributor.sendToServer(new CtoS_UpdateItemBlueprintNBT(item, nome, valore_booleano, 0.0, BlockPos.ZERO), new CustomPacketPayload[0]);
   }

   public static void send(ItemStack item, String nome, double valore_double) {
      PacketDistributor.sendToServer(new CtoS_UpdateItemBlueprintNBT(item, nome, false, valore_double, BlockPos.ZERO), new CustomPacketPayload[0]);
   }

   public static void send(ItemStack item, String nome, BlockPos posNuova) {
      VisualizeBlueprintDatas.originPosVisualizeBlueprint = posNuova;
      PacketDistributor.sendToServer(new CtoS_UpdateItemBlueprintNBT(item, nome, false, 0.0, posNuova), new CustomPacketPayload[0]);
   }
}
