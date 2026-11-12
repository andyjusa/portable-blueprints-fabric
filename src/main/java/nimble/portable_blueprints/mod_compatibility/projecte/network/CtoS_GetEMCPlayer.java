package nimble.portable_blueprints.mod_compatibility.projecte.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.mod_compatibility.projecte.ProjectEMetodi;

public record CtoS_GetEMCPlayer() implements CustomPacketPayload {
   public static final Type<CtoS_GetEMCPlayer> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_getemcplayer"));
   public static final StreamCodec<ByteBuf, CtoS_GetEMCPlayer> STREAM_CODEC = StreamCodec.unit(new CtoS_GetEMCPlayer());

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_GetEMCPlayer packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         PacketDistributor.sendToPlayer(player, new StoC_GetEMCPlayer(ProjectEMetodi.getEMCPlayer(player.getUUID())), new CustomPacketPayload[0]);
      });
   }
}
