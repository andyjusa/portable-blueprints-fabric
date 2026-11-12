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

public record CtoS_AddShareWithOthers(String nome, UUID creatore_richiesta, UUID ricevente) implements CustomPacketPayload {
   public static final Type<CtoS_AddShareWithOthers> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_addsharewithothers"));
   public static final StreamCodec<ByteBuf, CtoS_AddShareWithOthers> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      CtoS_AddShareWithOthers::nome,
      ModCustomCodecs.UUID,
      CtoS_AddShareWithOthers::creatore_richiesta,
      ModCustomCodecs.UUID,
      CtoS_AddShareWithOthers::ricevente,
      CtoS_AddShareWithOthers::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_AddShareWithOthers packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         ServerLevel level = player.serverLevel();
         if (packet.creatore_richiesta.equals(packet.ricevente)) {
            Blueprint.setSharedBlueprintAsGlobal(level, packet.creatore_richiesta, packet.nome);
         } else {
            Blueprint.addSharedBlueprint(level, packet.nome, packet.creatore_richiesta, packet.ricevente);
         }
      });
   }
}
