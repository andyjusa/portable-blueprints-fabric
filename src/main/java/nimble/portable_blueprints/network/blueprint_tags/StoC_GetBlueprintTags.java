package nimble.portable_blueprints.network.blueprint_tags;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record StoC_GetBlueprintTags(UUID IDRequest, String IDBP, String TAG) implements CustomPacketPayload {
   private static Map<String, List<String>> RaccoltaDati = new HashMap<>();
   private static Map<String, UUID> IDRequestForBlueprint = new HashMap<>();
   public static final Type<StoC_GetBlueprintTags> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_getblueprinttags"));
   public static final StreamCodec<ByteBuf, StoC_GetBlueprintTags> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.UUID,
      StoC_GetBlueprintTags::IDRequest,
      ByteBufCodecs.STRING_UTF8,
      StoC_GetBlueprintTags::IDBP,
      ByteBufCodecs.STRING_UTF8,
      StoC_GetBlueprintTags::TAG,
      StoC_GetBlueprintTags::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_GetBlueprintTags msg, PayloadContext context) {
      context.enqueueWork(() -> {
         if (msg.TAG.equals("START")) {
            RaccoltaDati.put(msg.IDBP, new ArrayList<>());
            IDRequestForBlueprint.put(msg.IDBP, msg.IDRequest);
            System.out.println("START: " + IDRequestForBlueprint.get(msg.IDBP));
         } else if (msg.TAG.equals("END") && IDRequestForBlueprint.containsKey(msg.IDBP) && IDRequestForBlueprint.get(msg.IDBP).equals(msg.IDRequest)) {
            ServerToClientDatasShared.BlueprintTags.put(msg.IDBP, new ArrayList<>(RaccoltaDati.get(msg.IDBP)));
            RaccoltaDati.remove(msg.IDBP);
            IDRequestForBlueprint.remove(msg.IDBP);
            System.out.println("END: " + msg.IDBP + " | " + msg.TAG);
         } else if (IDRequestForBlueprint.containsKey(msg.IDBP) && IDRequestForBlueprint.get(msg.IDBP).equals(msg.IDRequest)) {
            System.out.println("add: " + msg.TAG);
            RaccoltaDati.get(msg.IDBP).add(msg.TAG);
         }
      });
   }
}
