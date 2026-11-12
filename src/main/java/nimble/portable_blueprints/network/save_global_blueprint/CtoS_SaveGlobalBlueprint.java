package nimble.portable_blueprints.network.save_global_blueprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
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
import nimble.portable_blueprints.init.ModGameRules;
import nimble.portable_blueprints.robautile.Operazioni;

public record CtoS_SaveGlobalBlueprint(String nome) implements CustomPacketPayload {
   public static final Type<CtoS_SaveGlobalBlueprint> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_saveglobalblueprint"));
   public static final StreamCodec<ByteBuf, CtoS_SaveGlobalBlueprint> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, CtoS_SaveGlobalBlueprint::nome, CtoS_SaveGlobalBlueprint::new
   );
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_SaveGlobalBlueprint packet, PayloadContext context) {
      context.enqueueWork(
         () -> {
            ServerPlayer player = (ServerPlayer)context.player();
            ServerLevel level = player.serverLevel();
            if (level.getGameRules().getBoolean(ModGameRules.AllowGlobalBlueprints)) {
               String nomeBlueprint = packet.nome.contains(";") ? packet.nome.split(";")[0] : packet.nome;
               String nomeDiRename = packet.nome.contains(";") ? packet.nome.split(";")[1] : packet.nome;
               File blueprintFile = null;

               for (File f : Blueprint.getFileForBlueprints(level, player.getUUID()).listFiles()) {
                  if (f.getName().equals(nomeBlueprint + ".bp")) {
                     blueprintFile = f;
                     break;
                  }
               }

               if (blueprintFile != null) {
                  File finalBlueprintFile = blueprintFile;
                  new Thread(
                        () -> {
                           try (Reader reader = new FileReader(finalBlueprintFile)) {
                              Blueprint.BlueprintDatas bd = (Blueprint.BlueprintDatas)GSON.fromJson(reader, Blueprint.BlueprintDatas.class);
                              bd.nome = nomeDiRename;
                              String bd_string_datas = GSON.toJson(bd);
                              String id_richiesta = UUID.randomUUID().toString();
                              List<String> dati = Operazioni.splitStringByLength(bd_string_datas, 10000);
                              PacketDistributor.sendToPlayer(
                                 player,
                                 new StoC_SaveGlobalBlueprint(
                                    "[INIZIO DATI BLUEPRINT <id>" + id_richiesta + "</id><nome>" + nomeDiRename + "</nome><l>" + dati.size() + "</l>"
                                 ),
                                 new CustomPacketPayload[0]
                              );
                              PacketDistributor.sendToPlayer(player, new StoC_SaveGlobalBlueprint("[NOME DEL FILE:" + nomeDiRename), new CustomPacketPayload[0]);

                              for (String val : dati) {
                                 PacketDistributor.sendToPlayer(
                                    player, new StoC_SaveGlobalBlueprint("[" + id_richiesta + "]" + val), new CustomPacketPayload[0]
                                 );
                                 Thread.sleep(10L);
                              }

                              PacketDistributor.sendToPlayer(
                                 player, new StoC_SaveGlobalBlueprint("[FINE DATI BLUEPRINT DI ID: " + id_richiesta + "]"), new CustomPacketPayload[0]
                              );
                           } catch (Exception e) {
                              e.printStackTrace();
                           }
                        }
                     )
                     .start();
               }
            }
         }
      );
   }
}
