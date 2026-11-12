package nimble.portable_blueprints.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.Events.ModEventClientForge;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record StoC_ObstructionBlock(boolean reset, BlockPos blockPos) implements CustomPacketPayload {
   public static final Type<StoC_ObstructionBlock> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_obstructionblock"));
   public static final StreamCodec<ByteBuf, StoC_ObstructionBlock> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.BOOL, StoC_ObstructionBlock::reset, ModCustomCodecs.BLOCKPOS, StoC_ObstructionBlock::blockPos, StoC_ObstructionBlock::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_ObstructionBlock msg, PayloadContext context) {
      context.enqueueWork(() -> {
         if (msg.reset) {
            ModEventClientForge.ObstructionBlockManager.reset();
         } else {
            ModEventClientForge.ObstructionBlockManager.aggiungiBlocco(msg.blockPos);
         }
      });
   }

   public static void send(ServerPlayer player) {
      nimble.portable_blueprints.fabric.network.PayloadValidation.clear(player.getUUID());
      PacketDistributor.sendToPlayer(player, new StoC_ObstructionBlock(true, BlockPos.ZERO), new CustomPacketPayload[0]);
   }

   public static void send(BlockPos blockPos, ServerPlayer player) {
      nimble.portable_blueprints.fabric.network.PayloadValidation.rememberObstruction(player, blockPos);
      PacketDistributor.sendToPlayer(player, new StoC_ObstructionBlock(false, blockPos), new CustomPacketPayload[0]);
   }
}
