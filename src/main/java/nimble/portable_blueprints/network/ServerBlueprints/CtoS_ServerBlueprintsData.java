package nimble.portable_blueprints.network.ServerBlueprints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
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

public record CtoS_ServerBlueprintsData(UUID randomUUIDSending) implements CustomPacketPayload {
   public static final Type<CtoS_ServerBlueprintsData> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_serverblueprintsdata")
   );
   public static final StreamCodec<ByteBuf, CtoS_ServerBlueprintsData> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.UUID, CtoS_ServerBlueprintsData::randomUUIDSending, CtoS_ServerBlueprintsData::new
   );
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_ServerBlueprintsData packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         ServerLevel level = player.serverLevel();
         File permissionFile = Blueprint.getServerPremissionFile(level);
         Blueprint.BlueprintPermission bp = null;

         try {
            try (Reader reader = new FileReader(permissionFile)) {
               bp = (Blueprint.BlueprintPermission)GSON.fromJson(reader, Blueprint.BlueprintPermission.class);
            } catch (Exception e) {
               e.printStackTrace();
            }

            PacketDistributor.sendToPlayer(player, new StoC_ServerBlueprintsData("<START>", packet.randomUUIDSending), new CustomPacketPayload[0]);

            for (String v : bp.getPlayerAllAllowedBlueprints(player.getUUID())) {
               PacketDistributor.sendToPlayer(player, new StoC_ServerBlueprintsData(v, packet.randomUUIDSending), new CustomPacketPayload[0]);
            }

            PacketDistributor.sendToPlayer(player, new StoC_ServerBlueprintsData("<END>", packet.randomUUIDSending), new CustomPacketPayload[0]);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      });
   }

   public static void send() {
      ServerToClientDatasShared.endTrasferingServerBlueprints = false;
      PacketDistributor.sendToServer(new CtoS_ServerBlueprintsData(UUID.randomUUID()), new CustomPacketPayload[0]);
   }
}
