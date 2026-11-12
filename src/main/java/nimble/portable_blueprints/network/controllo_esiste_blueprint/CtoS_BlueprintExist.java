package nimble.portable_blueprints.network.controllo_esiste_blueprint;

import io.netty.buffer.ByteBuf;
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

public record CtoS_BlueprintExist(int idRichiesta, String nome) implements CustomPacketPayload {
   public static final Type<CtoS_BlueprintExist> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_blueprintexist"));
   public static final StreamCodec<ByteBuf, CtoS_BlueprintExist> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT, CtoS_BlueprintExist::idRichiesta, ByteBufCodecs.STRING_UTF8, CtoS_BlueprintExist::nome, CtoS_BlueprintExist::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_BlueprintExist packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         ServerLevel level = player.serverLevel();
         boolean risultato = Blueprint.BlueprintExist(level, packet.nome, player.getUUID());
         PacketDistributor.sendToPlayer(player, new StoC_BlueprintExist(packet.idRichiesta(), risultato), new CustomPacketPayload[0]);
      });
   }
}
