package nimble.portable_blueprints.network.ottieni_nome_x_blueprint;

import io.netty.buffer.ByteBuf;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

public record CtoS_BlueprintXName(int idRichesta, int xPosition) implements CustomPacketPayload {
   public static final Type<CtoS_BlueprintXName> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_blueprintxname"));
   public static final StreamCodec<ByteBuf, CtoS_BlueprintXName> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT, CtoS_BlueprintXName::idRichesta, ByteBufCodecs.INT, CtoS_BlueprintXName::xPosition, CtoS_BlueprintXName::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_BlueprintXName packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         ServerLevel level = player.serverLevel();
         List<File> files = new ArrayList<>();

         for (File file : Blueprint.getFileForBlueprints(level, player.getUUID()).listFiles()) {
            if (file.getName().contains(".bp")) {
               files.add(file);
            }
         }

         String risultato = "";
         if (packet.xPosition < files.size() && packet.xPosition >= 0) {
            risultato = files.get(packet.xPosition).getName().substring(0, files.get(packet.xPosition).getName().length() - 3);
         }

         PacketDistributor.sendToPlayer(player, new StoC_BlueprintXName(packet.idRichesta, risultato), new CustomPacketPayload[0]);
      });
   }

   public static void send(int idRichesta, int xPosition) {
      if (ServerToClientDatasShared.RichiestaString.containsKey(idRichesta)) {
         ServerToClientDatasShared.RichiestaString.remove(idRichesta);
      }

      PacketDistributor.sendToServer(new CtoS_BlueprintXName(idRichesta, xPosition), new CustomPacketPayload[0]);
   }
}
