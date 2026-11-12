package nimble.portable_blueprints.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.Events.ModEventClientForge;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;
import nimble.portable_blueprints.portable_blueprints.ModConfig;

public record StoC_AnimazionePosizionamentoBlocco(String blockdata, long endTime, Vec3 startPos, Vec3 endPos) implements CustomPacketPayload {
   public static final Type<StoC_AnimazionePosizionamentoBlocco> TYPE = new Type(
      ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_animazione_posizionamento_blocco")
   );
   public static final StreamCodec<ByteBuf, StoC_AnimazionePosizionamentoBlocco> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      StoC_AnimazionePosizionamentoBlocco::blockdata,
      ByteBufCodecs.VAR_LONG,
      StoC_AnimazionePosizionamentoBlocco::endTime,
      ModCustomCodecs.VEC3,
      StoC_AnimazionePosizionamentoBlocco::startPos,
      ModCustomCodecs.VEC3,
      StoC_AnimazionePosizionamentoBlocco::endPos,
      StoC_AnimazionePosizionamentoBlocco::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_AnimazionePosizionamentoBlocco msg, PayloadContext context) {
      context.enqueueWork(
         () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
               Vec3i playerPos = mc.player.blockPosition();
               Vec3i startPosInt = new Vec3i((int)msg.startPos().x, (int)msg.startPos().y, (int)msg.startPos().z);
               if ((Boolean)ModConfig.BuildAnimationEnabled.get() && playerPos.distManhattan(startPosInt) <= 200) {
                  ModEventClientForge.AnimPosBlocks
                     .add(new ModEventClientForge.AnimazionePosizionamentoBlocco(msg.blockdata(), msg.endTime(), msg.startPos(), msg.endPos()));
               }
            }
         }
      );
   }
}
