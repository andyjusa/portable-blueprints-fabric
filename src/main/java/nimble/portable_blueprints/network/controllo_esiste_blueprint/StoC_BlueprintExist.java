package nimble.portable_blueprints.network.controllo_esiste_blueprint;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.client.ServerToClientDatasShared;

public record StoC_BlueprintExist(int idRichiesta, boolean valore) implements CustomPacketPayload {
   public static final Type<StoC_BlueprintExist> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_blueprintexist"));
   public static final StreamCodec<ByteBuf, StoC_BlueprintExist> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT, StoC_BlueprintExist::idRichiesta, ByteBufCodecs.BOOL, StoC_BlueprintExist::valore, StoC_BlueprintExist::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_BlueprintExist msg, PayloadContext context) {
      context.enqueueWork(() -> {
         LocalPlayer player = Minecraft.getInstance().player;
         ServerToClientDatasShared.addRichiestaBooleana(msg.idRichiesta(), msg.valore);
      });
   }
}
