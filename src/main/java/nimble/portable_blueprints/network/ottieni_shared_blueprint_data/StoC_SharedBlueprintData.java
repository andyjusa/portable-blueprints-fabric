package nimble.portable_blueprints.network.ottieni_shared_blueprint_data;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record StoC_SharedBlueprintData(String valore, UUID IDRequest) implements CustomPacketPayload {
   public static final Type<StoC_SharedBlueprintData> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_sharedblueprintdata"));
   public static final StreamCodec<ByteBuf, StoC_SharedBlueprintData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, StoC_SharedBlueprintData::valore, ModCustomCodecs.UUID, StoC_SharedBlueprintData::IDRequest, StoC_SharedBlueprintData::new
   );
   private static UUID IDRequestAccettata = null;
   private static List<String> newSharedWithMe = new ArrayList<>();
   private static List<String> newSharedWithOthers = new ArrayList<>();

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_SharedBlueprintData msg, PayloadContext context) {
      context.enqueueWork(() -> {
         if (msg.valore.equals("inizio")) {
            IDRequestAccettata = msg.IDRequest;
            newSharedWithMe.clear();
            newSharedWithOthers.clear();
         } else if (IDRequestAccettata.equals(msg.IDRequest)) {
            if (msg.valore.equals("scambio")) {
               ServerToClientDatasShared.SharedWithMeBlueprints.SharedWithMeBlueprints = new ArrayList<>(newSharedWithMe);
               ServerToClientDatasShared.SharedWithMeBlueprints.SharedWithOthersBlueprints = new ArrayList<>(newSharedWithOthers);
               ServerToClientDatasShared.endTrasferingSharedWithMeBlueprints = true;
            } else if (msg.valore.contains("S.W.M")) {
               newSharedWithMe.add(msg.valore.replace("S.W.M", ""));
            } else if (msg.valore.contains("S.W.O")) {
               newSharedWithOthers.add(msg.valore.replace("S.W.O", ""));
            }
         }
      });
   }
}
