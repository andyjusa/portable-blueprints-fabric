package nimble.portable_blueprints.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.portable_blueprints.ModConfig;

public record StoC_SendLinkedContainer(boolean linked, String valore) implements CustomPacketPayload {
   public static final Type<StoC_SendLinkedContainer> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_sendlinkedcontainer"));
   public static final StreamCodec<ByteBuf, StoC_SendLinkedContainer> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.BOOL, StoC_SendLinkedContainer::linked, ByteBufCodecs.STRING_UTF8, StoC_SendLinkedContainer::valore, StoC_SendLinkedContainer::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_SendLinkedContainer msg, PayloadContext context) {
      context.enqueueWork(
         () -> {
            if ((Boolean)ModConfig.ContainerMessagesEnabled.get()) {
               if (msg.linked) {
                  try {
                     TextColor t = (TextColor)TextColor.parseColor((String)ModConfig.ContainerAreaColor.get()).getOrThrow();
                     Minecraft.getInstance()
                        .player
                        .displayClientMessage(
                           Component.literal(Component.translatable("chat_message.portable_blueprints.container.linked").getString().replace("%v%", msg.valore))
                              .withStyle(Style.EMPTY.withColor(t)),
                           false
                        );
                  } catch (Exception var3) {
                  }
               } else {
                  try {
                     TextColor t = (TextColor)TextColor.parseColor((String)ModConfig.ContainerAreaColor.get()).getOrThrow();
                     Minecraft.getInstance()
                        .player
                        .displayClientMessage(
                           Component.literal(
                                 Component.translatable("chat_message.portable_blueprints.container.unlinked").getString().replace("%v%", msg.valore)
                              )
                              .withStyle(Style.EMPTY.withColor(t)),
                           false
                        );
                  } catch (Exception var2) {
                  }
               }
            }
         }
      );
   }
}
