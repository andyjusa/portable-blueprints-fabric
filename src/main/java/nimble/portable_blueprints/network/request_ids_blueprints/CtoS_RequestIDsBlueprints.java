package nimble.portable_blueprints.network.request_ids_blueprints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
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

public record CtoS_RequestIDsBlueprints(UUID owner, String BPnome, int idRequest, String namespace, boolean isServer, CtoS_RequestIDsBlueprints.Tipo tipo)
   implements CustomPacketPayload {
   public static final Type<CtoS_RequestIDsBlueprints> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_requestidsblueprints")
   );
   public static final StreamCodec<ByteBuf, CtoS_RequestIDsBlueprints> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.UUID,
      CtoS_RequestIDsBlueprints::owner,
      ByteBufCodecs.STRING_UTF8,
      CtoS_RequestIDsBlueprints::BPnome,
      ByteBufCodecs.INT,
      CtoS_RequestIDsBlueprints::idRequest,
      ByteBufCodecs.STRING_UTF8,
      CtoS_RequestIDsBlueprints::namespace,
      ByteBufCodecs.BOOL,
      CtoS_RequestIDsBlueprints::isServer,
      ModCustomCodecs.ENUM_TIPOIDsBlueprints,
      CtoS_RequestIDsBlueprints::tipo,
      CtoS_RequestIDsBlueprints::new
   );
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_RequestIDsBlueprints packet, PayloadContext context) {
      context.enqueueWork(
         () -> {
            ServerPlayer player = (ServerPlayer)context.player();
            ServerLevel level = player.serverLevel();
            if (packet.tipo.equals(CtoS_RequestIDsBlueprints.Tipo.PERSONAL)) {
               File blueprintFile = Arrays.stream(Blueprint.getFileForBlueprints(level, packet.owner == null ? player.getUUID() : packet.owner).listFiles())
                  .filter(f -> f.getName().equals(packet.BPnome + ".bp"))
                  .findFirst()
                  .orElse(null);
               if (blueprintFile != null) {
                  try (Reader reader = new FileReader(blueprintFile)) {
                     Blueprint.BlueprintDatas bd = (Blueprint.BlueprintDatas)GSON.fromJson(reader, Blueprint.BlueprintDatas.class);
                     if (bd != null) {
                        PacketDistributor.sendToPlayer(
                           player, new StoC_RequestIDsBlueprints(Blueprint.getIDBlueprint(packet.namespace, bd), packet.idRequest), new CustomPacketPayload[0]
                        );
                     }
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               }
            } else if (packet.tipo.equals(CtoS_RequestIDsBlueprints.Tipo.SERVER)) {
               try {
                  for (File bpS : Blueprint.getServerBlueprintFile(level).listFiles()) {
                     if (bpS.getName().endsWith(".bp") && (packet.BPnome.isEmpty() || packet.BPnome.equals(bpS.getName().replace(".bp", "")))) {
                        Blueprint.BlueprintDatas bd = null;

                        try (Reader reader = new FileReader(bpS)) {
                           bd = (Blueprint.BlueprintDatas)GSON.fromJson(reader, Blueprint.BlueprintDatas.class);
                        } catch (Exception e) {
                           e.printStackTrace();
                        }

                        if (bd != null) {
                           PacketDistributor.sendToPlayer(
                              player,
                              new StoC_RequestIDsBlueprints(Blueprint.getIDBlueprint(packet.namespace, bd), packet.idRequest),
                              new CustomPacketPayload[0]
                           );
                        }
                     }
                  }
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
         }
      );
   }

   public static void send(UUID owner, String BPnome, int idRequest, String namespace) {
      PacketDistributor.sendToServer(
         new CtoS_RequestIDsBlueprints(owner, BPnome, idRequest, namespace, false, CtoS_RequestIDsBlueprints.Tipo.PERSONAL), new CustomPacketPayload[0]
      );
   }

   public static void send(boolean isServer, String BPnome, int idRequest, String namespace) {
      PacketDistributor.sendToServer(
         new CtoS_RequestIDsBlueprints(UUID.randomUUID(), BPnome, idRequest, namespace, true, CtoS_RequestIDsBlueprints.Tipo.SERVER),
         new CustomPacketPayload[0]
      );
   }

   public static void send(boolean isServer) {
      PacketDistributor.sendToServer(
         new CtoS_RequestIDsBlueprints(UUID.randomUUID(), "", -1, "", true, CtoS_RequestIDsBlueprints.Tipo.SERVER), new CustomPacketPayload[0]
      );
   }

   public enum Tipo {
      PERSONAL,
      SERVER;
   }
}
