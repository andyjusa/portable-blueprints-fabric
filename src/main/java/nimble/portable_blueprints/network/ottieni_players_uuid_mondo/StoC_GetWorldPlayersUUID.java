package nimble.portable_blueprints.network.ottieni_players_uuid_mondo;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record StoC_GetWorldPlayersUUID(UUID RequestID, String state, UUID PlayerUUID) implements CustomPacketPayload {
   public static final Type<StoC_GetWorldPlayersUUID> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_getworldplayersuuid"));
   public static final StreamCodec<ByteBuf, StoC_GetWorldPlayersUUID> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.UUID,
      StoC_GetWorldPlayersUUID::RequestID,
      ByteBufCodecs.STRING_UTF8,
      StoC_GetWorldPlayersUUID::state,
      ModCustomCodecs.UUID,
      StoC_GetWorldPlayersUUID::PlayerUUID,
      StoC_GetWorldPlayersUUID::new
   );
   private static UUID ActiveRequestID = null;

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_GetWorldPlayersUUID msg, PayloadContext context) {
      context.enqueueWork(() -> {
         if (msg.state.equals("START")) {
            ActiveRequestID = msg.RequestID;
         }

         if (ActiveRequestID.equals(msg.RequestID)) {
            if (!msg.state.isEmpty()) {
               if (msg.state.equals("START")) {
                  ServerToClientDatasShared.endTrasferingWorldPlayersUUID = false;
                  ServerToClientDatasShared.WorldPlayersUUID.clear();
               } else if (msg.state.equals("END")) {
                  ServerToClientDatasShared.endTrasferingWorldPlayersUUID = true;
               }
            } else {
               ServerToClientDatasShared.WorldPlayersUUID.add(msg.PlayerUUID);
            }
         }
      });
   }

   public static void send(UUID RID, String state, ServerPlayer player) {
      PacketDistributor.sendToPlayer(player, new StoC_GetWorldPlayersUUID(RID, state, UUID.randomUUID()), new CustomPacketPayload[0]);
   }

   public static void send(UUID RID, UUID PlayerUUID, ServerPlayer player) {
      PacketDistributor.sendToPlayer(player, new StoC_GetWorldPlayersUUID(RID, "", PlayerUUID), new CustomPacketPayload[0]);
   }
}
