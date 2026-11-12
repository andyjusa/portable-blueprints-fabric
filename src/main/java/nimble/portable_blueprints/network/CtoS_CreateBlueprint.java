package nimble.portable_blueprints.network;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.altro.BlockBlueprintUsage;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.network.custom_codecs.ModCustomCodecs;

public record CtoS_CreateBlueprint(BlockPos angolo1, BlockPos angolo2, String nome, boolean consideraCreativeBlocks, String datoLista, UUID uuidRichiesta)
   implements CustomPacketPayload {
   public static final Type<CtoS_CreateBlueprint> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "ctos_create_blueprint"));
   public static final StreamCodec<ByteBuf, CtoS_CreateBlueprint> STREAM_CODEC = StreamCodec.composite(
      ModCustomCodecs.BLOCKPOS,
      CtoS_CreateBlueprint::angolo1,
      ModCustomCodecs.BLOCKPOS,
      CtoS_CreateBlueprint::angolo2,
      ByteBufCodecs.STRING_UTF8,
      CtoS_CreateBlueprint::nome,
      ByteBufCodecs.BOOL,
      CtoS_CreateBlueprint::consideraCreativeBlocks,
      ByteBufCodecs.STRING_UTF8,
      CtoS_CreateBlueprint::datoLista,
      ModCustomCodecs.UUID,
      CtoS_CreateBlueprint::uuidRichiesta,
      CtoS_CreateBlueprint::new
   );
   private static Map<RequestKey, CtoS_CreateBlueprint.PachettoDati> DatiPachetto = new HashMap<>();

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CtoS_CreateBlueprint packet, PayloadContext context) {
      context.enqueueWork(
         () -> {
            ServerPlayer player = (ServerPlayer)context.player();
            ServerLevel level = player.serverLevel();
            RequestKey request = new RequestKey(player.getUUID(), packet.uuidRichiesta);
            DatiPachetto.entrySet().removeIf(entry -> entry.getValue().expiresAt < System.currentTimeMillis());
            if (BlockBlueprintUsage.PlayersBlock.contains(player.getName().getString())) {
               if (!packet.nome.equals("")) {
                  player.displayClientMessage(Component.translatable("chat_message.portable_blueprints.block_scanner").withStyle(ChatFormatting.RED), false);
               }
            } else {
               if ((packet.nome.equals("") || packet.nome.isEmpty()) && DatiPachetto.containsKey(request)) {
                  if (packet.datoLista.equals("�END�")) {
                     CtoS_CreateBlueprint.PachettoDati pachettoDati = DatiPachetto.get(request);
                     DatiPachetto.remove(request);
                     if (!validSelection(player, pachettoDati.angolo1, pachettoDati.angolo2)) return;
                     Blueprint.CreateBlueprint(
                        level,
                        pachettoDati.angolo1,
                        pachettoDati.angolo2,
                        pachettoDati.nome,
                        player.getUUID(),
                        pachettoDati.consideraCreativeBlocks,
                        pachettoDati.ScannerIgnoredBlocks
                     );
                     DatiPachetto.remove(request);
                  } else {
                     PachettoDati pending = DatiPachetto.get(request);
                     if (pending.ScannerIgnoredBlocks.size() >= 4096 || pending.dataSize + packet.datoLista.length() > 1048576) {
                        DatiPachetto.remove(request);
                        return;
                     }
                     pending.dataSize += packet.datoLista.length();
                     pending.ScannerIgnoredBlocks.add(packet.datoLista);
                  }
               } else if (!packet.nome.equals("") && !packet.nome.isEmpty() && !DatiPachetto.containsKey(request)) {
                  int limit = level.getGameRules().getInt(nimble.portable_blueprints.init.ModGameRules.Max_digitized_structures_for_player);
                  java.io.File[] existing = Blueprint.getFileForBlueprints(level, player.getUUID()).listFiles((dir, name) -> name.endsWith(".bp"));
                  if (existing == null || existing.length >= limit) return;
                  if (!validSelection(player, packet.angolo1, packet.angolo2)
                     || DatiPachetto.size() >= 1024
                     || DatiPachetto.keySet().stream().filter(key -> key.player().equals(player.getUUID())).count() >= 4) return;
                  DatiPachetto.put(
                     request, new CtoS_CreateBlueprint.PachettoDati(packet.angolo1, packet.angolo2, packet.nome, packet.consideraCreativeBlocks && player.isCreative())
                  );
               }
            }
         }
      );
   }

   public static List<String> creaListaDati(List<String> dati) {
      List<String> listaDati = new ArrayList<>();

      for (String s : dati) {
         if (s.length() <= 30000) {
            listaDati.add(s);
         } else {
            listaDati.add(s.substring(0, 30000));
         }
      }

      listaDati.add("�END�");
      return listaDati;
   }

   public static void send(BlockPos angolo1, BlockPos angolo2, String nome, boolean consideraCreativeBlocks, UUID uuidRichiesta) {
      PacketDistributor.sendToServer(new CtoS_CreateBlueprint(angolo1, angolo2, nome, consideraCreativeBlocks, "", uuidRichiesta), new CustomPacketPayload[0]);
   }

   public static void send(String dato, UUID uuidRichiesta) {
      PacketDistributor.sendToServer(new CtoS_CreateBlueprint(BlockPos.ZERO, BlockPos.ZERO, "", false, dato, uuidRichiesta), new CustomPacketPayload[0]);
   }

   private static boolean validSelection(ServerPlayer player, BlockPos first, BlockPos second) {
      if (!nimble.portable_blueprints.fabric.network.PayloadValidation.validPosition(player, first)
            || !nimble.portable_blueprints.fabric.network.PayloadValidation.validPosition(player, second)) return false;
      long x = Math.abs((long)first.getX() - second.getX()) + 1;
      long y = Math.abs((long)first.getY() - second.getY()) + 1;
      long z = Math.abs((long)first.getZ() - second.getZ()) + 1;
      int limit = player.serverLevel().getGameRules().getInt(nimble.portable_blueprints.init.ModGameRules.MaxScannerBlockCount);
      return x <= 30000000 && z <= 30000000 && y <= 4096
         && x * y * z <= limit;
   }

   private record RequestKey(UUID player, UUID request) {}

   public static void resetPlayer(UUID player) {
      DatiPachetto.keySet().removeIf(key -> key.player().equals(player));
   }

   private static class PachettoDati {
      private final long expiresAt = System.currentTimeMillis() + 300000L;
      private int dataSize;
      public List<String> ScannerIgnoredBlocks = new ArrayList<>();
      private final BlockPos angolo1;
      private final BlockPos angolo2;
      private final String nome;
      private final boolean consideraCreativeBlocks;

      public PachettoDati(BlockPos angolo1, BlockPos angolo2, String nome, boolean consideraCreativeBlocks) {
         this.angolo1 = angolo1;
         this.angolo2 = angolo2;
         this.nome = nome;
         this.consideraCreativeBlocks = consideraCreativeBlocks;
      }
   }
}
