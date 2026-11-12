package nimble.portable_blueprints.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.IntegerValue;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.init.ModGameRules;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;
import nimble.portable_blueprints.portable_blueprints.ModServerConfig;

public record CtoS_UpdateServerConfigurationValue(CtoS_UpdateServerConfigurationValue.TipoValore tipoValore, String value) implements CustomPacketPayload {
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();
   public static final Type<CtoS_UpdateServerConfigurationValue> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_updateserverconfigurationvalue")
   );
   public static final StreamCodec<ByteBuf, CtoS_UpdateServerConfigurationValue> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.ENUM_TIPOVALOREUPDATESERVER,
      CtoS_UpdateServerConfigurationValue::tipoValore,
      ByteBufCodecs.STRING_UTF8,
      CtoS_UpdateServerConfigurationValue::value,
      CtoS_UpdateServerConfigurationValue::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_UpdateServerConfigurationValue packet, PayloadContext context) {
      context.enqueueWork(
         () -> {
            ServerPlayer player = (ServerPlayer)context.player();
            ServerLevel level = player.serverLevel();
            if (player.hasPermissions(3)) {
               switch (packet.tipoValore) {
                  case Max_digitized_structures_for_player:
                     ((IntegerValue)level.getGameRules().getRule(ModGameRules.Max_digitized_structures_for_player))
                        .set(Integer.parseInt(packet.value), level.getServer());
                     break;
                  case SOCIAL_LINK:
                     String[] v = new String[]{packet.value.split(";")[0], packet.value.split(";").length > 1 ? packet.value.split(";")[1] : ""};
                     if (Integer.parseInt(v[0]) == 1) {
                        ModServerConfig.TABLET_SOCIAL_1.set(v[1]);
                     } else if (Integer.parseInt(v[0]) == 2) {
                        ModServerConfig.TABLET_SOCIAL_2.set(v[1]);
                     } else if (Integer.parseInt(v[0]) == 3) {
                        ModServerConfig.TABLET_SOCIAL_3.set(v[1]);
                     } else if (Integer.parseInt(v[0]) == 4) {
                        ModServerConfig.TABLET_SOCIAL_4.set(v[1]);
                     } else if (Integer.parseInt(v[0]) == 5) {
                        ModServerConfig.TABLET_SOCIAL_5.set(v[1]);
                     }

                     ModServerConfig.saveConfigFile();
                     break;
                  case MaxBlueprintContainers:
                     ((IntegerValue)level.getGameRules().getRule(ModGameRules.MaxBlueprintContainers)).set(Integer.parseInt(packet.value), level.getServer());
                     break;
                  case BlueprintsCooldown:
                     ((IntegerValue)level.getGameRules().getRule(ModGameRules.BlueprintsCooldown)).set(Integer.parseInt(packet.value), level.getServer());
                     break;
                  case AllowGlobalBlueprints:
                     ((BooleanValue)level.getGameRules().getRule(ModGameRules.AllowGlobalBlueprints))
                        .set(Boolean.parseBoolean(packet.value), level.getServer());
                     break;
                  case AllowNBTDataCopy:
                     ((BooleanValue)level.getGameRules().getRule(ModGameRules.AllowNBTDataCopy)).set(Boolean.parseBoolean(packet.value), level.getServer());
                     break;
                  case BusinessVillagerEnabled:
                     ((BooleanValue)level.getGameRules().getRule(ModGameRules.BusinessVillagerEnabled))
                        .set(Boolean.parseBoolean(packet.value), level.getServer());
                     break;
                  case MaxScannerBlockCount:
                     ((IntegerValue)level.getGameRules().getRule(ModGameRules.MaxScannerBlockCount)).set(Integer.parseInt(packet.value), level.getServer());
               }
            }
         }
      );
   }

   public enum TipoValore {
      Max_digitized_structures_for_player,
      SOCIAL_LINK,
      MaxBlueprintContainers,
      BlueprintsCooldown,
      AllowGlobalBlueprints,
      AllowNBTDataCopy,
      BusinessVillagerEnabled,
      MaxScannerBlockCount;
   }
}
