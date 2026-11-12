package nimble.portable_blueprints.network.visualize_blueprint_data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
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
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;
import nimble.portable_blueprints.robautile.Operazioni;

public record CtoS_VisualizeBlueprintData(
   int IDRichiestaBlueprint, String nomeBlueprintRichiesto, UUID owner_blueprint, boolean isServerBlueprint, boolean isWornBlueprint
) implements CustomPacketPayload {
   public static final Type<CtoS_VisualizeBlueprintData> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_visualizeblueprintdata")
   );
   public static final StreamCodec<ByteBuf, CtoS_VisualizeBlueprintData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      CtoS_VisualizeBlueprintData::IDRichiestaBlueprint,
      ByteBufCodecs.STRING_UTF8,
      CtoS_VisualizeBlueprintData::nomeBlueprintRichiesto,
      ModCustomCodecs.UUID,
      CtoS_VisualizeBlueprintData::owner_blueprint,
      ByteBufCodecs.BOOL,
      CtoS_VisualizeBlueprintData::isServerBlueprint,
      ByteBufCodecs.BOOL,
      CtoS_VisualizeBlueprintData::isWornBlueprint,
      CtoS_VisualizeBlueprintData::new
   );
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_VisualizeBlueprintData packet, PayloadContext context) {
      context.enqueueWork(
         () -> {
            ServerPlayer player = (ServerPlayer)context.player();
            ServerLevel level = player.serverLevel();
            File blueprintFile = null;

            for (File f : packet.isServerBlueprint
               ? Blueprint.getServerBlueprintFile(level).listFiles()
               : (
                  packet.isWornBlueprint
                     ? Blueprint.getWornBlueprintFile(level).listFiles()
                     : Blueprint.getFileForBlueprints(level, packet.owner_blueprint == null ? player.getUUID() : packet.owner_blueprint).listFiles()
               )) {
               if (f.getName().equals(packet.nomeBlueprintRichiesto + ".bp")) {
                  blueprintFile = f;
                  break;
               }
            }

            if (blueprintFile != null) {
               Blueprint.BlueprintDatas bd = null;

               try (Reader reader = new FileReader(blueprintFile)) {
                  bd = (Blueprint.BlueprintDatas)GSON.fromJson(reader, Blueprint.BlueprintDatas.class);
               } catch (Exception e) {
                  e.printStackTrace();
               }

               if (bd != null) {
                  long delayMillis = 1L;
                  List<String> Pachetti = null;

                  try {
                     String bd_string_datas = new String(Files.readAllBytes(blueprintFile.toPath()));
                     if (bd_string_datas.isEmpty()) {
                        return;
                     }

                     Pachetti = Operazioni.splitStringByLength(bd_string_datas, 30000);
                  } catch (IOException e) {
                     throw new RuntimeException(e);
                  }

                  if (Pachetti == null) {
                     return;
                  }

                  List<String> finalPachetti = Pachetti;
                  new Thread(
                        () -> {
                           int volte = 0;
                           PacketDistributor.sendToPlayer(
                              player,
                              new StoC_VisualizeBlueprintData(packet.IDRichiestaBlueprint, "[DATI INIZIALI]<bsize>" + finalPachetti.size() + "</bsize>"),
                              new CustomPacketPayload[0]
                           );

                           try {
                              for (String pachetto : finalPachetti) {
                                 PacketDistributor.sendToPlayer(
                                    player, new StoC_VisualizeBlueprintData(packet.IDRichiestaBlueprint, pachetto), new CustomPacketPayload[0]
                                 );
                                 if (++volte % 4 == 0) {
                                    Thread.sleep(delayMillis);
                                 }
                              }
                           } catch (Exception var8x) {
                           }

                           PacketDistributor.sendToPlayer(
                              player, new StoC_VisualizeBlueprintData(packet.IDRichiestaBlueprint, "[DATI FINITI]"), new CustomPacketPayload[0]
                           );
                        }
                     )
                     .start();
               }
            }
         }
      );
   }

   public static void send(int IDRichiestaBlueprint, String nomeBlueprintRichiesto, UUID owner_blueprint) {
      PacketDistributor.sendToServer(
         new CtoS_VisualizeBlueprintData(IDRichiestaBlueprint, nomeBlueprintRichiesto, owner_blueprint, false, false), new CustomPacketPayload[0]
      );
   }

   public static void send(int IDRichiestaBlueprint, String nomeBlueprintRichiesto, boolean isServerBlueprint) {
      PacketDistributor.sendToServer(
         new CtoS_VisualizeBlueprintData(IDRichiestaBlueprint, nomeBlueprintRichiesto, UUID.randomUUID(), isServerBlueprint, false), new CustomPacketPayload[0]
      );
   }

   public static void send(int IDRichiestaBlueprint, String nomeBlueprintRichiesto, boolean isServerBlueprint, boolean isWornBlueprint) {
      PacketDistributor.sendToServer(
         new CtoS_VisualizeBlueprintData(IDRichiestaBlueprint, nomeBlueprintRichiesto, UUID.randomUUID(), false, isWornBlueprint), new CustomPacketPayload[0]
      );
   }
}
