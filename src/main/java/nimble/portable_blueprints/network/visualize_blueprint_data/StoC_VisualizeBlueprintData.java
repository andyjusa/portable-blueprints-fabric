package nimble.portable_blueprints.network.visualize_blueprint_data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.client.VisualizeBlueprintDatas;

public record StoC_VisualizeBlueprintData(int IDRichiestaBlueprint, String jsonData) implements CustomPacketPayload {
   public static final Type<StoC_VisualizeBlueprintData> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_visualizeblueprintdata")
   );
   public static final StreamCodec<ByteBuf, StoC_VisualizeBlueprintData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      StoC_VisualizeBlueprintData::IDRichiestaBlueprint,
      ByteBufCodecs.STRING_UTF8,
      StoC_VisualizeBlueprintData::jsonData,
      StoC_VisualizeBlueprintData::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_VisualizeBlueprintData msg, PayloadContext context) {
      context.enqueueWork(() -> VisualizeBlueprintDatas.sendJsonToVisualizeBlueprint(msg.IDRichiestaBlueprint, msg.jsonData));
   }
}
