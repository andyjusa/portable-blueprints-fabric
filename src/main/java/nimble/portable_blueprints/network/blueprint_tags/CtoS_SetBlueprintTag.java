package nimble.portable_blueprints.network.blueprint_tags;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.init.ModTabletTags;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record CtoS_SetBlueprintTag(UUID OwnerBP, String BPName, String TagID, boolean toAdd) implements CustomPacketPayload {
   public static final Type<CtoS_SetBlueprintTag> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_setblueprinttag"));
   public static final StreamCodec<ByteBuf, CtoS_SetBlueprintTag> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.UUID,
      CtoS_SetBlueprintTag::OwnerBP,
      ByteBufCodecs.STRING_UTF8,
      CtoS_SetBlueprintTag::BPName,
      ByteBufCodecs.STRING_UTF8,
      CtoS_SetBlueprintTag::TagID,
      ByteBufCodecs.BOOL,
      CtoS_SetBlueprintTag::toAdd,
      CtoS_SetBlueprintTag::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_SetBlueprintTag packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         ServerLevel level = player.serverLevel();
         String key = packet.OwnerBP == null ? "server" : packet.OwnerBP.toString();
         String id = Blueprint.getIDBlueprint(level, key, packet.OwnerBP, packet.BPName);
         if (!id.isEmpty()) {
            if (!ModTabletTags.BlueprintTagsData.containsKey(id)) {
               ModTabletTags.BlueprintTagsData.put(id, new ArrayList<>());
            }

            if (packet.toAdd) {
               if (!ModTabletTags.BlueprintTagsData.get(id).contains(packet.TagID)) {
                  ModTabletTags.BlueprintTagsData.get(id).add(packet.TagID);
               }
            } else {
               ModTabletTags.BlueprintTagsData.get(id).remove(packet.TagID);
            }

            if (ModTabletTags.BlueprintTagsData.get(id).isEmpty()) {
               ModTabletTags.BlueprintTagsData.remove(id);
            }
         }
      });
   }
}
