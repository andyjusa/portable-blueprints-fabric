package nimble.portable_blueprints.network.request_specific_data;

import io.netty.buffer.ByteBuf;
import java.io.File;
import net.minecraft.network.codec.ByteBufCodecs;
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
import nimble.portable_blueprints.init.ModGameRules;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;
import nimble.portable_blueprints.portable_blueprints.ModServerConfig;

public record CtoS_RequestSpecificData(int idRichiesta, CtoS_RequestSpecificData.TipoRichiesta tipoRichiesta) implements CustomPacketPayload {
   public static final Type<CtoS_RequestSpecificData> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_requestspecificdata"));
   public static final StreamCodec<ByteBuf, CtoS_RequestSpecificData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      CtoS_RequestSpecificData::idRichiesta,
      ModCustomCodecs.ENUM_CtoS_RequestSpecificData_TIPORICHIESTA,
      CtoS_RequestSpecificData::tipoRichiesta,
      CtoS_RequestSpecificData::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_RequestSpecificData packet, PayloadContext context) {
      context.enqueueWork(
         () -> {
            ServerPlayer player = (ServerPlayer)context.player();
            ServerLevel level = player.serverLevel();
            switch (packet.tipoRichiesta) {
               case MAX_DIGITIZED_STRUCTURES_FOR_PLAYER:
                  StoC_RequestSpecificData.send(
                     player, packet.idRichiesta, level.getLevelData().getGameRules().getInt(ModGameRules.Max_digitized_structures_for_player)
                  );
                  break;
               case MAX_SCANNER_BLOCK_COUNT:
                  StoC_RequestSpecificData.send(player, packet.idRichiesta, level.getLevelData().getGameRules().getInt(ModGameRules.MaxScannerBlockCount));
                  break;
               case ALLOW_GLOBAL_BLUEPRINTS:
                  StoC_RequestSpecificData.send(player, packet.idRichiesta, level.getLevelData().getGameRules().getBoolean(ModGameRules.AllowGlobalBlueprints));
                  break;
               case IS_PLAYER_OP:
                  StoC_RequestSpecificData.send(player, packet.idRichiesta, player.hasPermissions(3));
                  break;
               case PLAYER_BLUEPRINTS_QUANTITY:
                  int risultato = 0;

                  for (File file : Blueprint.getFileForBlueprints(level, player.getUUID()).listFiles()) {
                     if (file.getName().contains(".bp")) {
                        risultato++;
                     }
                  }

                  StoC_RequestSpecificData.send(player, packet.idRichiesta, risultato);
                  break;
               case GET_TABLET_SOCIAL_LINKS:
                  StoC_RequestSpecificData.send(
                     player,
                     packet.idRichiesta,
                     ModServerConfig.tablet_social_1
                        + ";"
                        + ModServerConfig.tablet_social_2
                        + ";"
                        + ModServerConfig.tablet_social_3
                        + ";"
                        + ModServerConfig.tablet_social_4
                        + ";"
                        + ModServerConfig.tablet_social_5
                  );
                  break;
               case MAX_BLUEPRINT_CONTAINERS:
                  StoC_RequestSpecificData.send(player, packet.idRichiesta, level.getLevelData().getGameRules().getInt(ModGameRules.MaxBlueprintContainers));
                  break;
               case BLUEPRINTS_COOLDOWN:
                  StoC_RequestSpecificData.send(player, packet.idRichiesta, level.getLevelData().getGameRules().getInt(ModGameRules.BlueprintsCooldown));
                  break;
               case ALLOW_NBT_DATA_COPY:
                  StoC_RequestSpecificData.send(player, packet.idRichiesta, level.getLevelData().getGameRules().getBoolean(ModGameRules.AllowNBTDataCopy));
                  break;
               case BUSINESS_VILLAGER_ENABLED:
                  StoC_RequestSpecificData.send(
                     player, packet.idRichiesta, level.getLevelData().getGameRules().getBoolean(ModGameRules.BusinessVillagerEnabled)
                  );
            }
         }
      );
   }

   public static void send(int idRichesta, CtoS_RequestSpecificData.TipoRichiesta tipoRichiesta) {
      switch (tipoRichiesta) {
         case MAX_DIGITIZED_STRUCTURES_FOR_PLAYER:
         case MAX_SCANNER_BLOCK_COUNT:
         case MAX_BLUEPRINT_CONTAINERS:
         case BLUEPRINTS_COOLDOWN:
            ServerToClientDatasShared.RichiesteInteger.remove(idRichesta);
            break;
         case ALLOW_GLOBAL_BLUEPRINTS:
         case IS_PLAYER_OP:
         case ALLOW_NBT_DATA_COPY:
         case BUSINESS_VILLAGER_ENABLED:
            ServerToClientDatasShared.RichiesteBooleane.remove(idRichesta);
         case PLAYER_BLUEPRINTS_QUANTITY:
         default:
            break;
         case GET_TABLET_SOCIAL_LINKS:
            ServerToClientDatasShared.RichiestaString.remove(idRichesta);
      }

      PacketDistributor.sendToServer(new CtoS_RequestSpecificData(idRichesta, tipoRichiesta), new CustomPacketPayload[0]);
   }

   public enum TipoRichiesta {
      MAX_DIGITIZED_STRUCTURES_FOR_PLAYER,
      MAX_SCANNER_BLOCK_COUNT,
      ALLOW_GLOBAL_BLUEPRINTS,
      IS_PLAYER_OP,
      PLAYER_BLUEPRINTS_QUANTITY,
      GET_TABLET_SOCIAL_LINKS,
      MAX_BLUEPRINT_CONTAINERS,
      BLUEPRINTS_COOLDOWN,
      ALLOW_NBT_DATA_COPY,
      BUSINESS_VILLAGER_ENABLED;
   }
}
