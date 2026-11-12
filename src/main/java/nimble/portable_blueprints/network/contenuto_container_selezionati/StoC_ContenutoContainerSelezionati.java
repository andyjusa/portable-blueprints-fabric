package nimble.portable_blueprints.network.contenuto_container_selezionati;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.client.ClientMethods;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record StoC_ContenutoContainerSelezionati(int idRequest, Item item, int quantity) implements CustomPacketPayload {
   public static final Type<StoC_ContenutoContainerSelezionati> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_contenuto_container_selezionati")
   );
   public static final StreamCodec<ByteBuf, StoC_ContenutoContainerSelezionati> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      StoC_ContenutoContainerSelezionati::idRequest,
      ModCustomCodecs.ITEM,
      StoC_ContenutoContainerSelezionati::item,
      ByteBufCodecs.INT,
      StoC_ContenutoContainerSelezionati::quantity,
      StoC_ContenutoContainerSelezionati::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_ContenutoContainerSelezionati msg, PayloadContext context) {
      context.enqueueWork(() -> ClientMethods.addItemQuantityContainer(msg.idRequest(), msg.item(), msg.quantity()));
   }
}
