package nimble.portable_blueprints.network.ottieni_players_uuid_mondo;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.client.ServerToClientDatasShared;

public record CtoS_GetWorldPlayersUUID() implements CustomPacketPayload {
   public static final Type<CtoS_GetWorldPlayersUUID> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_getworldplayersuuid"));
   public static final StreamCodec<ByteBuf, CtoS_GetWorldPlayersUUID> STREAM_CODEC = StreamCodec.unit(new CtoS_GetWorldPlayersUUID());

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_GetWorldPlayersUUID packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         ServerLevel level = player.serverLevel();
         UUID RequestID = UUID.randomUUID();
         List<UUID> result = new ArrayList<>();

         try {
            Path playerDataDir = player.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR);
            if (playerDataDir != null && Files.exists(playerDataDir) && Files.isDirectory(playerDataDir)) {
               try (Stream<Path> paths = Files.list(playerDataDir)) {
                  paths.filter(p -> {
                     String n = p.getFileName().toString().toLowerCase();
                     return n.endsWith(".dat");
                  }).forEach(p -> {
                     String fileName = p.getFileName().toString();
                     String base = fileName.substring(0, fileName.length() - 4);
                     UUID uuidx = parseUuidLenient(base);
                     if (uuidx != null) {
                        result.add(uuidx);
                     }
                  });
               }
            }
         } catch (IOException e) {
            e.printStackTrace();
         }

         StoC_GetWorldPlayersUUID.send(RequestID, "START", player);

         for (UUID uuid : result) {
            StoC_GetWorldPlayersUUID.send(RequestID, uuid, player);
         }

         StoC_GetWorldPlayersUUID.send(RequestID, "END", player);
      });
   }

   private static UUID parseUuidLenient(String s) {
      if (s == null) {
         return null;
      }

      try {
         return UUID.fromString(s);
      } catch (IllegalArgumentException ex) {
         String clean = s.replaceAll("[^0-9a-fA-F]", "");
         if (clean.length() == 32) {
            String dashed = clean.substring(0, 8)
               + "-"
               + clean.substring(8, 12)
               + "-"
               + clean.substring(12, 16)
               + "-"
               + clean.substring(16, 20)
               + "-"
               + clean.substring(20, 32);

            try {
               return UUID.fromString(dashed);
            } catch (IllegalArgumentException ex2) {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   public static void send() {
      ServerToClientDatasShared.endTrasferingServerBlueprints = false;
      PacketDistributor.sendToServer(new CtoS_GetWorldPlayersUUID(), new CustomPacketPayload[0]);
   }
}
