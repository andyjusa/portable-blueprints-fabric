package nimble.portable_blueprints.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.client.ClientMethods;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record StoC_OpenScannerScreen(ItemStack item, int quantitaBlocchi) implements CustomPacketPayload {
   public static final Type<StoC_OpenScannerScreen> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_open_scanner_screen"));
   public static final StreamCodec<FriendlyByteBuf, StoC_OpenScannerScreen> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.ITEM_STACK, StoC_OpenScannerScreen::item, ByteBufCodecs.INT, StoC_OpenScannerScreen::quantitaBlocchi, StoC_OpenScannerScreen::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_OpenScannerScreen packet, PayloadContext context) {
      context.enqueueWork(() -> ClientMethods.openHoloScannerItemGUI(packet.item(), packet.quantitaBlocchi()));
   }
}
