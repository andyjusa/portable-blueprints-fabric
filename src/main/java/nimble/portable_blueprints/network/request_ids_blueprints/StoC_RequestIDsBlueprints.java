package nimble.portable_blueprints.network.request_ids_blueprints;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.client.ServerToClientDatasShared;

public record StoC_RequestIDsBlueprints(String valore, int idRequest) implements CustomPacketPayload {
   public static final Type<StoC_RequestIDsBlueprints> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_requestidsblueprints")
   );
   public static final StreamCodec<ByteBuf, StoC_RequestIDsBlueprints> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, StoC_RequestIDsBlueprints::valore, ByteBufCodecs.INT, StoC_RequestIDsBlueprints::idRequest, StoC_RequestIDsBlueprints::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_RequestIDsBlueprints msg, PayloadContext context) {
      context.enqueueWork(() -> {
         if (msg.idRequest == -1) {
            ServerToClientDatasShared.blueprintsIDs.add(msg.valore);
         } else {
            ServerToClientDatasShared.StringMap.put(msg.idRequest, msg.valore);
         }
      });
   }
}
