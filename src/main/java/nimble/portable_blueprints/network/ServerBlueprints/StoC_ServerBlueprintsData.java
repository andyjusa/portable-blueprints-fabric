package nimble.portable_blueprints.network.ServerBlueprints;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record StoC_ServerBlueprintsData(String valore, UUID IDRequest) implements CustomPacketPayload {
   public static final Type<StoC_ServerBlueprintsData> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_serverblueprintsdata")
   );
   public static final StreamCodec<ByteBuf, StoC_ServerBlueprintsData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, StoC_ServerBlueprintsData::valore, ModCustomCodecs.UUID, StoC_ServerBlueprintsData::IDRequest, StoC_ServerBlueprintsData::new
   );
   private static UUID IDRequestAccettata = null;

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_ServerBlueprintsData msg, PayloadContext context) {
      context.enqueueWork(() -> {
         if (msg.valore.equals("<START>")) {
            ServerToClientDatasShared.ServerBlueprints.clear();
            IDRequestAccettata = msg.IDRequest;
         } else if (IDRequestAccettata.equals(msg.IDRequest)) {
            if (msg.valore.equals("<END>")) {
               ServerToClientDatasShared.endTrasferingServerBlueprints = true;
            } else {
               ServerToClientDatasShared.ServerBlueprints.add(msg.valore);
            }
         }
      });
   }
}
