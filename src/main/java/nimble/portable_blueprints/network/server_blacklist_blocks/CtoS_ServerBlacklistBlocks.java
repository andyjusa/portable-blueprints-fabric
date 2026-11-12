package nimble.portable_blueprints.network.server_blacklist_blocks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;
import nimble.portable_blueprints.portable_blueprints.ModServerConfig;

public record CtoS_ServerBlacklistBlocks(UUID idPacket) implements CustomPacketPayload {
   public static final Type<CtoS_ServerBlacklistBlocks> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_serverblacklistblocks")
   );
   public static final StreamCodec<ByteBuf, CtoS_ServerBlacklistBlocks> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.UUID, CtoS_ServerBlacklistBlocks::idPacket, CtoS_ServerBlacklistBlocks::new
   );
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_ServerBlacklistBlocks packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         long delayMillis = 1L;
         new Thread(() -> {
            int volte = 0;
            PacketDistributor.sendToPlayer(player, new StoC_ServerBlacklistBlocks(packet.idPacket, "[START]"), new CustomPacketPayload[0]);

            try {
               for (String elemento : ModServerConfig.blacklistBlock) {
                  PacketDistributor.sendToPlayer(player, new StoC_ServerBlacklistBlocks(packet.idPacket, elemento), new CustomPacketPayload[0]);
                  if (++volte % 4 == 0) {
                     Thread.sleep(delayMillis);
                  }
               }
            } catch (Exception var7) {
            }

            PacketDistributor.sendToPlayer(player, new StoC_ServerBlacklistBlocks(packet.idPacket, "[END]"), new CustomPacketPayload[0]);
         }).start();
      });
   }

   public static void send(UUID idPacket) {
      StoC_ServerBlacklistBlocks.IDPACKET = idPacket;
      PacketDistributor.sendToServer(new CtoS_ServerBlacklistBlocks(idPacket), new CustomPacketPayload[0]);
   }
}
