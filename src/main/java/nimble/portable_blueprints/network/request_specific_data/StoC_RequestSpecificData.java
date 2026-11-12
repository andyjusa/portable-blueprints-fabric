package nimble.portable_blueprints.network.request_specific_data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record StoC_RequestSpecificData(int IDRichiesta, int int_valore, boolean bool_valore, String string_valore, StoC_RequestSpecificData.TipoDato tipoDato)
   implements CustomPacketPayload {
   public static final Type<StoC_RequestSpecificData> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_requestspecificdata"));
   public static final StreamCodec<ByteBuf, StoC_RequestSpecificData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      StoC_RequestSpecificData::IDRichiesta,
      ByteBufCodecs.INT,
      StoC_RequestSpecificData::int_valore,
      ByteBufCodecs.BOOL,
      StoC_RequestSpecificData::bool_valore,
      ByteBufCodecs.STRING_UTF8,
      StoC_RequestSpecificData::string_valore,
      ModCustomCodecs.ENUM_StoC_RequestSpecificData_TIPODATO,
      StoC_RequestSpecificData::tipoDato,
      StoC_RequestSpecificData::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_RequestSpecificData msg, PayloadContext context) {
      context.enqueueWork(() -> {
         switch (msg.tipoDato) {
            case BOOL:
               ServerToClientDatasShared.addRichiestaBooleana(msg.IDRichiesta, msg.bool_valore);
               break;
            case INT:
               ServerToClientDatasShared.addRichiestaInteger(msg.IDRichiesta, msg.int_valore);
               break;
            case STRING:
               ServerToClientDatasShared.addRichiestaString(msg.IDRichiesta, msg.string_valore);
         }
      });
   }

   public static void send(ServerPlayer player, int IDRichiesta, int valore) {
      PacketDistributor.sendToPlayer(
         player, new StoC_RequestSpecificData(IDRichiesta, valore, false, "", StoC_RequestSpecificData.TipoDato.INT), new CustomPacketPayload[0]
      );
   }

   public static void send(ServerPlayer player, int IDRichiesta, boolean valore) {
      PacketDistributor.sendToPlayer(
         player, new StoC_RequestSpecificData(IDRichiesta, 0, valore, "", StoC_RequestSpecificData.TipoDato.BOOL), new CustomPacketPayload[0]
      );
   }

   public static void send(ServerPlayer player, int IDRichiesta, String valore) {
      PacketDistributor.sendToPlayer(
         player, new StoC_RequestSpecificData(IDRichiesta, 0, false, valore, StoC_RequestSpecificData.TipoDato.STRING), new CustomPacketPayload[0]
      );
   }

   public enum TipoDato {
      BOOL,
      INT,
      STRING;
   }
}
