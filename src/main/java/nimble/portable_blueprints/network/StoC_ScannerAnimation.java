package nimble.portable_blueprints.network;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.Events.RenderScannerAnimation;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;
import nimble.portable_blueprints.portable_blueprints.ModConfig;

public record StoC_ScannerAnimation(UUID animationID, boolean active, BlockPos angolo1, BlockPos angolo2, boolean add) implements CustomPacketPayload {
   public static final Type<StoC_ScannerAnimation> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_scanner_animation"));
   public static final StreamCodec<ByteBuf, StoC_ScannerAnimation> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.UUID,
      StoC_ScannerAnimation::animationID,
      ByteBufCodecs.BOOL,
      StoC_ScannerAnimation::active,
      ModCustomCodecs.BLOCKPOS,
      StoC_ScannerAnimation::angolo1,
      ModCustomCodecs.BLOCKPOS,
      StoC_ScannerAnimation::angolo2,
      ByteBufCodecs.BOOL,
      StoC_ScannerAnimation::add,
      StoC_ScannerAnimation::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_ScannerAnimation msg, PayloadContext context) {
      context.enqueueWork(() -> {
         if ((Boolean)ModConfig.ScanningAnimationEnabled.get()) {
            if (msg.add()) {
               RenderScannerAnimation.addProgress(msg.animationID());
            } else if (msg.active()) {
               RenderScannerAnimation.addAnimation(msg.animationID(), msg.angolo1(), msg.angolo2());
            } else {
               RenderScannerAnimation.removeAnimazione(msg.animationID());
            }
         }
      });
   }

   public static void send(UUID animationID, boolean active, BlockPos angolo1, BlockPos angolo2, ServerPlayer player) {
      PacketDistributor.sendToPlayer(player, new StoC_ScannerAnimation(animationID, active, angolo1, angolo2, false), new CustomPacketPayload[0]);
   }

   public static void send(UUID animationID, ServerPlayer player) {
      PacketDistributor.sendToPlayer(
         player, new StoC_ScannerAnimation(animationID, false, new BlockPos(0, 0, 0), new BlockPos(0, 0, 0), true), new CustomPacketPayload[0]
      );
   }
}
