package nimble.portable_blueprints.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import nimble.portable_blueprints.fabric.network.PayloadValidation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.entity.custom.BusinessVillagerEntity;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record CtoS_CreateRemoveBlockWorkRequest(BlockPos blockPos) implements CustomPacketPayload {
   public static final Type<CtoS_CreateRemoveBlockWorkRequest> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_createremoveblockworkrequest")
   );
   public static final StreamCodec<ByteBuf, CtoS_CreateRemoveBlockWorkRequest> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.BLOCKPOS, CtoS_CreateRemoveBlockWorkRequest::blockPos, CtoS_CreateRemoveBlockWorkRequest::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_CreateRemoveBlockWorkRequest packet, PayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = (ServerPlayer)context.player();
         ServerLevel level = player.serverLevel();
         if (player.isSpectator() || !player.mayBuild() || !PayloadValidation.validPosition(player, packet.blockPos)
               || !level.mayInteract(player, packet.blockPos)
               || !PayloadValidation.consumeObstruction(player, packet.blockPos)) return;
         if (BusinessVillagerEntity.listaBVEVivi.containsKey(player.getUUID()) && BusinessVillagerEntity.listaBVEVivi.get(player.getUUID()) != null) {
            BusinessVillagerEntity.listaBVEVivi.get(player.getUUID()).tickAlive = -1;
            BusinessVillagerEntity.listaBVEVivi.remove(player.getUUID());
         }

         BlockState blockState = player.level().getBlockState(packet.blockPos);
         boolean distruttibile = blockState.getDestroySpeed(player.level(), packet.blockPos) >= 0.0F;
         if (!blockState.getBlock().equals(Blocks.AIR) && distruttibile) {
            level.playSound(null, packet.blockPos, blockState.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            if (PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, packet.blockPos, blockState, level.getBlockEntity(packet.blockPos))) {
               player.level().destroyBlock(packet.blockPos, false, null);
            }
         }
      });
   }
}
