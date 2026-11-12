package nimble.portable_blueprints.network.request_server_tags;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.init.ModTabletTags;

public record CtoS_RequestServerTags() implements CustomPacketPayload {
   public static final Type<CtoS_RequestServerTags> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_requestservertags"));
   public static final StreamCodec<ByteBuf, CtoS_RequestServerTags> STREAM_CODEC = StreamCodec.unit(new CtoS_RequestServerTags());
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_RequestServerTags packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         ServerLevel level = player.serverLevel();
         File server_tags_file = ModTabletTags.ServerSide.getFile(level);

         try {
            java.lang.reflect.Type type = (new TypeToken<List<ModTabletTags.TagData>>() {}).getType();
            List<ModTabletTags.TagData> tagDatas = new ArrayList<>();

            try (Reader reader = new FileReader(server_tags_file)) {
               tagDatas = (List<ModTabletTags.TagData>)GSON.fromJson(reader, type);
            } catch (Exception e) {
               e.printStackTrace();
            }

            for (ModTabletTags.TagData td : tagDatas) {
               PacketDistributor.sendToPlayer(player, new StoC_RequestServerTags(td), new CustomPacketPayload[0]);
            }
         } catch (Exception var12) {
         }
      });
   }
}
