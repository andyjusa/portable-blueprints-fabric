package nimble.portable_blueprints.network.progress_bar_player;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.altro.ProgressBarBlueprintPlayer;

public record StoC_ProgressBarPlayer(int IDProgressBar, int valore, boolean da_creare) implements CustomPacketPayload {
   public static final Type<StoC_ProgressBarPlayer> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_progressbarplayer"));
   public static final StreamCodec<ByteBuf, StoC_ProgressBarPlayer> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      StoC_ProgressBarPlayer::IDProgressBar,
      ByteBufCodecs.INT,
      StoC_ProgressBarPlayer::valore,
      ByteBufCodecs.BOOL,
      StoC_ProgressBarPlayer::da_creare,
      StoC_ProgressBarPlayer::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_ProgressBarPlayer msg, PayloadContext context) {
      context.enqueueWork(() -> {
         if (msg.da_creare) {
            ProgressBarBlueprintPlayer.aggiungi(msg.IDProgressBar, msg.valore);
         } else {
            ProgressBarBlueprintPlayer.progressoPlus(msg.IDProgressBar);
         }
      });
   }
}
