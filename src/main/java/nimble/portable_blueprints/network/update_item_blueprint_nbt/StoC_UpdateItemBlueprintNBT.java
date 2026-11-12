package nimble.portable_blueprints.network.update_item_blueprint_nbt;

import io.netty.buffer.ByteBuf;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.Events.RenderBlueprintsPreview;

public record StoC_UpdateItemBlueprintNBT(boolean doCleanUp, boolean doCleanOnlyBlueprint) implements CustomPacketPayload {
   public static final Type<StoC_UpdateItemBlueprintNBT> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_updateitemblueprintnbt")
   );
   public static final StreamCodec<ByteBuf, StoC_UpdateItemBlueprintNBT> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.BOOL,
      StoC_UpdateItemBlueprintNBT::doCleanUp,
      ByteBufCodecs.BOOL,
      StoC_UpdateItemBlueprintNBT::doCleanOnlyBlueprint,
      StoC_UpdateItemBlueprintNBT::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_UpdateItemBlueprintNBT msg, PayloadContext context) {
      context.enqueueWork(() -> {
         if (msg.doCleanUp) {
            if (msg.doCleanOnlyBlueprint) {
               Executors.newSingleThreadScheduledExecutor().schedule(() -> RenderBlueprintsPreview.doCleanupOnlyBlueprint(), 100L, TimeUnit.MILLISECONDS);
            } else {
               Executors.newSingleThreadScheduledExecutor().schedule(() -> RenderBlueprintsPreview.doCleanUp(), 100L, TimeUnit.MILLISECONDS);
            }
         }
      });
   }
}
