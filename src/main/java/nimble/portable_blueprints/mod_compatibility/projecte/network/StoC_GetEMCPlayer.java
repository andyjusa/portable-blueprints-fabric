package nimble.portable_blueprints.mod_compatibility.projecte.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.mod_compatibility.projecte.ProjectEBlueprintScreen;

public record StoC_GetEMCPlayer(Long EMC) implements CustomPacketPayload {
   public static final Type<StoC_GetEMCPlayer> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_getemcplayer"));
   public static final StreamCodec<ByteBuf, StoC_GetEMCPlayer> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_LONG, StoC_GetEMCPlayer::EMC, StoC_GetEMCPlayer::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_GetEMCPlayer msg, PayloadContext context) {
      context.enqueueWork(() -> ProjectEBlueprintScreen.PlayerEMC = msg.EMC);
   }
}
