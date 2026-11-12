package nimble.portable_blueprints.network.SharedBlueprints;

import io.netty.buffer.ByteBuf;
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
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record CtoS_RemoveShareWithOthers(String nome, UUID creatore_richiesta, UUID ricevente) implements CustomPacketPayload {
   public static final Type<CtoS_RemoveShareWithOthers> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_removesharewithothers")
   );
   public static final StreamCodec<ByteBuf, CtoS_RemoveShareWithOthers> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      CtoS_RemoveShareWithOthers::nome,
      ModCustomCodecs.UUID,
      CtoS_RemoveShareWithOthers::creatore_richiesta,
      ModCustomCodecs.UUID,
      CtoS_RemoveShareWithOthers::ricevente,
      CtoS_RemoveShareWithOthers::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_RemoveShareWithOthers packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         ServerLevel level = player.serverLevel();
         if (packet.creatore_richiesta.equals(packet.ricevente)) {
            Blueprint.removeSharedBlueprintAsGlobal(level, packet.creatore_richiesta, packet.nome);
         } else {
            Blueprint.removeSharedBlueprint(level, packet.nome, packet.creatore_richiesta, packet.ricevente);
         }
      });
   }
}
