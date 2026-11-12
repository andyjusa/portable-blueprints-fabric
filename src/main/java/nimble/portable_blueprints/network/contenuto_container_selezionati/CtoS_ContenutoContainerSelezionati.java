package nimble.portable_blueprints.network.contenuto_container_selezionati;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record CtoS_ContenutoContainerSelezionati(List<BlockPos> posizioni, int IDRequest) implements CustomPacketPayload {
   public static final Type<CtoS_ContenutoContainerSelezionati> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_contenuto_container_selezionati")
   );
   public static final StreamCodec<ByteBuf, CtoS_ContenutoContainerSelezionati> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.LIST_BLOCKPOS,
      CtoS_ContenutoContainerSelezionati::posizioni,
      ByteBufCodecs.INT,
      CtoS_ContenutoContainerSelezionati::IDRequest,
      CtoS_ContenutoContainerSelezionati::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_ContenutoContainerSelezionati packet, PayloadContext context) {
      context.enqueueWork(
         () -> {
            ServerPlayer player = (ServerPlayer)context.player();
            ServerLevel level = player.serverLevel();
            Map<Item, Integer> container = Blueprint.getAllItemQuantityFromInventory(player, level, packet.posizioni);

            for (Entry<Item, Integer> entry : container.entrySet()) {
               PacketDistributor.sendToPlayer(
                  player, new StoC_ContenutoContainerSelezionati(packet.IDRequest(), entry.getKey(), entry.getValue()), new CustomPacketPayload[0]
               );
            }
         }
      );
   }
}
