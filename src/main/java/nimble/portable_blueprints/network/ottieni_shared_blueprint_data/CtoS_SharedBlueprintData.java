package nimble.portable_blueprints.network.ottieni_shared_blueprint_data;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record CtoS_SharedBlueprintData(UUID randomUUIDSending) implements CustomPacketPayload {
   public static final Type<CtoS_SharedBlueprintData> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_sharedblueprintdata"));
   public static final StreamCodec<ByteBuf, CtoS_SharedBlueprintData> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.UUID, CtoS_SharedBlueprintData::randomUUIDSending, CtoS_SharedBlueprintData::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_SharedBlueprintData packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         ServerLevel level = player.serverLevel();
         Blueprint.SharedWithMe sharedWithMe = Blueprint.getSharedBlueprints(level, player.getUUID());
         PacketDistributor.sendToPlayer(player, new StoC_SharedBlueprintData("inizio", packet.randomUUIDSending), new CustomPacketPayload[0]);

         for (String v : sharedWithMe.SharedWithMeBlueprints) {
            PacketDistributor.sendToPlayer(player, new StoC_SharedBlueprintData("S.W.M" + v, packet.randomUUIDSending), new CustomPacketPayload[0]);
         }

         for (String v : sharedWithMe.SharedWithOthersBlueprints) {
            PacketDistributor.sendToPlayer(player, new StoC_SharedBlueprintData("S.W.O" + v, packet.randomUUIDSending), new CustomPacketPayload[0]);
         }

         PacketDistributor.sendToPlayer(player, new StoC_SharedBlueprintData("scambio", packet.randomUUIDSending), new CustomPacketPayload[0]);
      });
   }

   public static void send() {
      ServerToClientDatasShared.endTrasferingSharedWithMeBlueprints = false;
      PacketDistributor.sendToServer(new CtoS_SharedBlueprintData(UUID.randomUUID()), new CustomPacketPayload[0]);
   }
}
