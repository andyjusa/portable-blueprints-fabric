package nimble.portable_blueprints.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
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

public record CtoS_CreaItemBlueprint(String nome, UUID BPOwner, boolean isGlobalInstance, boolean isServerBlueprint) implements CustomPacketPayload {
   public static final Type<CtoS_CreaItemBlueprint> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_create_item_blueprint"));
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();
   public static final StreamCodec<ByteBuf, CtoS_CreaItemBlueprint> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      CtoS_CreaItemBlueprint::nome,
      ModCustomCodecs.UUID,
      CtoS_CreaItemBlueprint::BPOwner,
      ByteBufCodecs.BOOL,
      CtoS_CreaItemBlueprint::isGlobalInstance,
      ByteBufCodecs.BOOL,
      CtoS_CreaItemBlueprint::isServerBlueprint,
      CtoS_CreaItemBlueprint::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_CreaItemBlueprint packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         ServerLevel level = player.serverLevel();
         if (packet.isServerBlueprint) {
            File blueprintFile = null;

            for (File f : Blueprint.getServerBlueprintFile(level).listFiles()) {
               if (f.getName().equals(packet.nome + ".bp")) {
                  blueprintFile = f;
                  break;
               }
            }

            if (blueprintFile != null) {
               try {
                  try (Reader reader = new FileReader(blueprintFile)) {
                     Blueprint.BlueprintDatas bd = (Blueprint.BlueprintDatas)GSON.fromJson(reader, Blueprint.BlueprintDatas.class);
                     Blueprint.CreaItemServerBlueprintEDaiAGiocatore(player, packet.nome, bd.altezza, bd.lunghezzaX, bd.lunghezzaZ);
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               } catch (Exception e) {
                  throw new RuntimeException(e);
               }
            }
         } else {
            File blueprintFile = null;

            for (File f : Blueprint.getFileForBlueprints(level, packet.BPOwner == null ? player.getUUID() : packet.BPOwner).listFiles()) {
               if (f.getName().equals(packet.nome + ".bp")) {
                  blueprintFile = f;
                  break;
               }
            }

            if (blueprintFile != null) {
               try (Reader reader = new FileReader(blueprintFile)) {
                  Blueprint.BlueprintDatas bd = (Blueprint.BlueprintDatas)GSON.fromJson(reader, Blueprint.BlueprintDatas.class);
                  if (packet.BPOwner == null) {
                     Blueprint.CreaItemBlueprintEDaiAGiocatore(player, player.getUUID(), packet.nome, bd.altezza, bd.lunghezzaX, bd.lunghezzaZ);
                  } else {
                     Blueprint.CreaItemBlueprintEDaiAGiocatore(player, packet.BPOwner, packet.nome, bd.altezza, bd.lunghezzaX, bd.lunghezzaZ);
                  }
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
         }
      });
   }

   public static void send(boolean isServerBlueprint, String nome) {
      PacketDistributor.sendToServer(new CtoS_CreaItemBlueprint(nome, UUID.randomUUID(), false, isServerBlueprint), new CustomPacketPayload[0]);
   }

   public static void send(UUID BPOwner, String nome, boolean is_global_instance) {
      PacketDistributor.sendToServer(new CtoS_CreaItemBlueprint(nome, BPOwner, is_global_instance, false), new CustomPacketPayload[0]);
   }

   public static void send(UUID BPOwner, String nome) {
      PacketDistributor.sendToServer(new CtoS_CreaItemBlueprint(nome, BPOwner, false, false), new CustomPacketPayload[0]);
   }

   public static void send(String nome) {
      PacketDistributor.sendToServer(new CtoS_CreaItemBlueprint(nome, null, false, false), new CustomPacketPayload[0]);
   }
}
