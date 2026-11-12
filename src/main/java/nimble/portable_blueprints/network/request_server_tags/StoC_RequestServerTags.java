package nimble.portable_blueprints.network.request_server_tags;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.init.ModTabletTags;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record StoC_RequestServerTags(ModTabletTags.TagData tagData) implements CustomPacketPayload {
   public static final Type<StoC_RequestServerTags> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc-requestservertags"));
   public static final StreamCodec<ByteBuf, StoC_RequestServerTags> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.TAGDATA, StoC_RequestServerTags::tagData, StoC_RequestServerTags::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_RequestServerTags msg, PayloadContext context) {
      context.enqueueWork(() -> {
         boolean aggiungere = true;

         for (ModTabletTags.TagData td : ModTabletTags.TagDatas) {
            if (td.getOriginalTag().equals(msg.tagData.getOriginalTag())) {
               aggiungere = false;
               break;
            }
         }

         if (aggiungere) {
            ModTabletTags.TagDatas.add(msg.tagData);
         }
      });
   }
}
