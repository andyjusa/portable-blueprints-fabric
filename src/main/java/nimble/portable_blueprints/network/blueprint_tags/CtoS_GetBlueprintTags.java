package nimble.portable_blueprints.network.blueprint_tags;

import io.netty.buffer.ByteBuf;
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
import nimble.portable_blueprints.init.ModTabletTags;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record CtoS_GetBlueprintTags(UUID OwnerBP, String BPName) implements CustomPacketPayload {
   public static final Type<CtoS_GetBlueprintTags> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_getblueprinttags"));
   public static final StreamCodec<ByteBuf, CtoS_GetBlueprintTags> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.UUID, CtoS_GetBlueprintTags::OwnerBP, ByteBufCodecs.STRING_UTF8, CtoS_GetBlueprintTags::BPName, CtoS_GetBlueprintTags::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_GetBlueprintTags packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         ServerLevel level = player.serverLevel();
         String key = packet.OwnerBP == null ? "server" : packet.OwnerBP.toString();
         String id = Blueprint.getIDBlueprint(level, key, packet.OwnerBP, packet.BPName);
         System.out.println(id);
         if (!id.isEmpty() && ModTabletTags.BlueprintTagsData.containsKey(id)) {
            UUID idRequest = UUID.randomUUID();
            PacketDistributor.sendToPlayer(player, new StoC_GetBlueprintTags(idRequest, id, "START"), new CustomPacketPayload[0]);

            for (String tag : ModTabletTags.BlueprintTagsData.get(id)) {
               PacketDistributor.sendToPlayer(player, new StoC_GetBlueprintTags(idRequest, id, tag), new CustomPacketPayload[0]);
            }

            PacketDistributor.sendToPlayer(player, new StoC_GetBlueprintTags(idRequest, id, "END"), new CustomPacketPayload[0]);
         }
      });
   }
}
