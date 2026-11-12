package nimble.portable_blueprints.network.global_blueprint_net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.init.ModGameRules;
import nimble.portable_blueprints.robautile.Operazioni;

public record CtoS_SaveGlobalBlueprintInServer(String valore) implements CustomPacketPayload {
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();
   public static final Type<CtoS_SaveGlobalBlueprintInServer> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_saveglobalblueprintinserver")
   );
   public static final StreamCodec<ByteBuf, CtoS_SaveGlobalBlueprintInServer> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, CtoS_SaveGlobalBlueprintInServer::valore, CtoS_SaveGlobalBlueprintInServer::new
   );
   public static Map<UUID, Boolean> StaGiaGestendoUnaBlueprint = new HashMap<>();
   public static Map<UUID, String> ID_blueprint_in_gestione = new HashMap<>();
   public static Map<UUID, String> NOME_BLUEPRINT = new HashMap<>();
   private static final Map<UUID, Long> UPLOAD_STARTED = new HashMap<>();
   private static final Map<UUID, Integer> UPLOAD_BYTES = new HashMap<>();

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_SaveGlobalBlueprintInServer packet, PayloadContext context) {
      context.enqueueWork(
         () -> {
            ServerPlayer player = (ServerPlayer)context.player();
            ServerLevel level = player.serverLevel();
            UUID uuidPlayer = player.getUUID();
            long now = System.currentTimeMillis();
            if (UPLOAD_STARTED.containsKey(uuidPlayer) && now - UPLOAD_STARTED.get(uuidPlayer) > 300000L) resetPlayer(uuidPlayer);
            if (level.getGameRules().getBoolean(ModGameRules.AllowGlobalBlueprints)) {
               if (!StaGiaGestendoUnaBlueprint.containsKey(uuidPlayer)) {
                  StaGiaGestendoUnaBlueprint.put(uuidPlayer, false);
               }

               if (!ID_blueprint_in_gestione.containsKey(uuidPlayer)) {
                  ID_blueprint_in_gestione.put(uuidPlayer, "");
               }

               if (!NOME_BLUEPRINT.containsKey(uuidPlayer)) {
                  NOME_BLUEPRINT.put(uuidPlayer, "");
               }

               if (packet.valore.contains("[INIZIO DATI BLUEPRINT ") && !StaGiaGestendoUnaBlueprint.get(uuidPlayer)) {
                  String uploadName = Operazioni.getValueInsideTag(packet.valore, "nome");
                  String uploadID = Operazioni.getValueInsideTag(packet.valore, "id");
                  if (!nimble.portable_blueprints.fabric.network.PayloadValidation.validName(uploadName)
                        || uploadName.length() > 25 || uploadID.length() != 36) return;
                  try { UUID.fromString(uploadID); } catch (IllegalArgumentException invalidID) { return; }
                  int limit = level.getGameRules().getInt(ModGameRules.Max_digitized_structures_for_player);
                  File[] existing = Blueprint.getFileForBlueprints(level, uuidPlayer).listFiles((dir, name) -> name.endsWith(".bp"));
                  if (existing == null || existing.length >= limit) return;
                  UPLOAD_STARTED.put(uuidPlayer, now);
                  UPLOAD_BYTES.put(uuidPlayer, 0);
                  StaGiaGestendoUnaBlueprint.put(uuidPlayer, true);
                  ID_blueprint_in_gestione.put(uuidPlayer, Operazioni.getValueInsideTag(packet.valore, "id"));
                  NOME_BLUEPRINT.put(uuidPlayer, Operazioni.getValueInsideTag(packet.valore, "nome"));

                  try {
                     File f = new File(Blueprint.getFileForBlueprints(level, uuidPlayer), NOME_BLUEPRINT.get(uuidPlayer) + ".tmp");
                     f.createNewFile();
                     Files.write(f.toPath(), "".getBytes());
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               } else if (packet.valore.contains("[FINE DATI BLUEPRINT DI ID: ")
                  && StaGiaGestendoUnaBlueprint.get(uuidPlayer)
                  && packet.valore.contains(ID_blueprint_in_gestione.get(uuidPlayer))) {
                  String nome = NOME_BLUEPRINT.get(uuidPlayer);
                  File temporary = new File(Blueprint.getFileForBlueprints(level, uuidPlayer), nome + ".tmp");
                  try (Reader reader = new FileReader(temporary)) {
                     Blueprint.BlueprintDatas incoming = GSON.fromJson(reader, Blueprint.BlueprintDatas.class);
                     if (incoming == null || incoming.getAllBlocksCount() > level.getGameRules().getInt(ModGameRules.MaxScannerBlockCount)) {
                        temporary.delete();
                        resetPlayer(uuidPlayer);
                        return;
                     }
                  } catch (Exception invalidBlueprint) {
                     temporary.delete();
                     resetPlayer(uuidPlayer);
                     return;
                  }
                  String nomeFinale = nome;

                  for (int count = 0; Blueprint.BlueprintExist(level, nomeFinale, uuidPlayer); count++) {
                     nomeFinale = NOME_BLUEPRINT.get(uuidPlayer);
                     String add = "(" + count + ")";
                     if (nomeFinale.length() + add.length() > 25) {
                        nomeFinale = nomeFinale.substring(0, 25 - add.length());
                     }

                     nomeFinale = nomeFinale + add;
                  }

                  if (nomeFinale.length() > 25) {
                     nomeFinale = nomeFinale.substring(0, 25);
                  }

                  new File(Blueprint.getFileForBlueprints(level, uuidPlayer), NOME_BLUEPRINT.get(uuidPlayer) + ".tmp")
                     .renameTo(new File(Blueprint.getFileForBlueprints(level, uuidPlayer), nomeFinale + ".bp"));
                  if (!nome.equals(nomeFinale)) {
                     Path dir = Blueprint.getFileForBlueprints(level, uuidPlayer).toPath();

                     try (Reader reader = new FileReader(dir.resolve(nomeFinale + ".bp").toFile())) {
                        Blueprint.BlueprintDatas bd = (Blueprint.BlueprintDatas)GSON.fromJson(reader, Blueprint.BlueprintDatas.class);
                        bd.nome = nomeFinale;

                        try (FileWriter writer = new FileWriter(dir.resolve(nomeFinale + ".bp").toFile())) {
                           GSON.toJson(bd, writer);
                        } catch (IOException e) {
                           e.printStackTrace();
                        }
                     } catch (Exception e) {
                        e.printStackTrace();
                     }
                  }

                  NOME_BLUEPRINT.put(uuidPlayer, "");
                  StaGiaGestendoUnaBlueprint.put(uuidPlayer, false);
                  ID_blueprint_in_gestione.put(uuidPlayer, "");
               } else if (Boolean.TRUE.equals(StaGiaGestendoUnaBlueprint.get(uuidPlayer))
                     && packet.valore.startsWith("[" + ID_blueprint_in_gestione.get(uuidPlayer) + "]")) {
                  int bytes = UPLOAD_BYTES.getOrDefault(uuidPlayer, 0) + packet.valore.length();
                  if (bytes > 33554432) { resetPlayer(uuidPlayer); return; }
                  UPLOAD_BYTES.put(uuidPlayer, bytes);
                  try (BufferedWriter writer = new BufferedWriter(
                        new FileWriter(new File(Blueprint.getFileForBlueprints(level, uuidPlayer), NOME_BLUEPRINT.get(uuidPlayer) + ".tmp"), true)
                     )) {
                     writer.write(packet.valore.replace("[" + ID_blueprint_in_gestione.get(uuidPlayer) + "]", ""));
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               }
            }
         }
      );
   }

   public static void resetPlayer(UUID uuid) {
      UPLOAD_STARTED.remove(uuid);
      UPLOAD_BYTES.remove(uuid);
      NOME_BLUEPRINT.remove(uuid);
      StaGiaGestendoUnaBlueprint.remove(uuid);
      ID_blueprint_in_gestione.remove(uuid);
   }
}
