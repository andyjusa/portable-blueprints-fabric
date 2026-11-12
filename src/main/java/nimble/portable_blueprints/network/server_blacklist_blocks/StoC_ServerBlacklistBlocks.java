package nimble.portable_blueprints.network.server_blacklist_blocks;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.client.ClientMethods;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record StoC_ServerBlacklistBlocks(UUID idPacket, String elemento) implements CustomPacketPayload {
   public static final Type<StoC_ServerBlacklistBlocks> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_serverblacklistblocks")
   );
   public static final StreamCodec<ByteBuf, StoC_ServerBlacklistBlocks> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.UUID,
      StoC_ServerBlacklistBlocks::idPacket,
      ByteBufCodecs.STRING_UTF8,
      StoC_ServerBlacklistBlocks::elemento,
      StoC_ServerBlacklistBlocks::new
   );
   public static UUID IDPACKET = UUID.randomUUID();

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_ServerBlacklistBlocks msg, PayloadContext context) {
      context.enqueueWork(() -> {
         if (msg.idPacket.equals(IDPACKET)) {
            if (msg.elemento.equals("[START]")) {
               ClientMethods.ServerBlackListBlocks.clear();
               ClientMethods.ServerBlackListBlocksCompleta = false;
            } else if (msg.elemento.equals("[END]")) {
               ClientMethods.ServerBlackListBlocksCompleta = true;
            } else {
               ClientMethods.ServerBlackListBlocks.add(msg.elemento);
            }
         }
      });
   }
}
