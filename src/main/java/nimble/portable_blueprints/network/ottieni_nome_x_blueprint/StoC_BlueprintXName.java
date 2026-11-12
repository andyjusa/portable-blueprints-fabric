package nimble.portable_blueprints.network.ottieni_nome_x_blueprint;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.client.ServerToClientDatasShared;

public record StoC_BlueprintXName(int idRichiesta, String nome) implements CustomPacketPayload {
   public static final Type<StoC_BlueprintXName> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_blueprintxname"));
   public static final StreamCodec<ByteBuf, StoC_BlueprintXName> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT, StoC_BlueprintXName::idRichiesta, ByteBufCodecs.STRING_UTF8, StoC_BlueprintXName::nome, StoC_BlueprintXName::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_BlueprintXName msg, PayloadContext context) {
      context.enqueueWork(() -> ServerToClientDatasShared.addRichiestaString(msg.idRichiesta, msg.nome));
   }
}
