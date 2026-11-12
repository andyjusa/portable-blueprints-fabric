package nimble.portable_blueprints.network.save_global_blueprint;

import io.netty.buffer.ByteBuf;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import nimble.portable_blueprints.fabric.network.PayloadContext;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.altro.ProgressBarBlueprintPlayer;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.init.ModToast;
import nimble.portable_blueprints.robautile.Operazioni;

public record StoC_SaveGlobalBlueprint(String valore) implements CustomPacketPayload {
   public static final Type<StoC_SaveGlobalBlueprint> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "stoc_saveglobalblueprint"));
   public static final StreamCodec<ByteBuf, StoC_SaveGlobalBlueprint> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, StoC_SaveGlobalBlueprint::valore, StoC_SaveGlobalBlueprint::new
   );
   public static boolean StaGiaGestendoUnaBlueprint = false;
   private static String ID_blueprint_in_gestione = "";
   private static String NOME_BLUEPRINT = "";
   private static int sizeData = 0;
   private static String BlueprintDatasString = "";

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(StoC_SaveGlobalBlueprint msg, PayloadContext context) {
      context.enqueueWork(
         () -> {
            if (msg.valore.contains("[INIZIO DATI BLUEPRINT ") && !StaGiaGestendoUnaBlueprint) {
               StaGiaGestendoUnaBlueprint = true;
               ID_blueprint_in_gestione = Operazioni.getValueInsideTag(msg.valore, "id");
               NOME_BLUEPRINT = Operazioni.getValueInsideTag(msg.valore, "nome");
               sizeData = Integer.valueOf(Operazioni.getValueInsideTag(msg.valore, "l"));

               try {
                  if (new File(Blueprint.getGlobalBlueprintDir(), NOME_BLUEPRINT + ".bp").exists()) {
                     new File(Blueprint.getGlobalBlueprintDir(), NOME_BLUEPRINT + ".bp").delete();
                  }

                  File f = new File(Blueprint.getGlobalBlueprintDir(), NOME_BLUEPRINT + ".tmp");
                  f.createNewFile();
                  Files.write(f.toPath(), "".getBytes());
               } catch (Exception e) {
                  e.printStackTrace();
               }

               ModToast.showToast(
                  Component.translatable("portable_blueprints.toast.global_blueprints.export.title"),
                  Component.translatable("portable_blueprints.toast.global_blueprints.export.subtitle"),
                  new ItemStack((ItemLike)ModItems.GLOBAL_BLUEPRINT.get()),
                  -1453289,
                  -1
               );
               ProgressBarBlueprintPlayer.aggiungi(2, sizeData + 1);
            } else if (msg.valore.contains("[FINE DATI BLUEPRINT DI ID: ") && StaGiaGestendoUnaBlueprint && msg.valore.contains(ID_blueprint_in_gestione)) {
               String nome = NOME_BLUEPRINT;

               for (int count = 0; Blueprint.globalBlueprintExist(nome); count++) {
                  nome = NOME_BLUEPRINT;
                  String add = "(" + count + ")";
                  if (nome.length() + add.length() > 25) {
                     nome = nome.substring(0, 25 - add.length());
                  }

                  nome = nome + add;
               }

               if (nome.length() > 25) {
                  nome = nome.substring(0, 25);
               }

               new File(Blueprint.getGlobalBlueprintDir(), NOME_BLUEPRINT + ".tmp").renameTo(new File(Blueprint.getGlobalBlueprintDir(), nome + ".bp"));
               NOME_BLUEPRINT = "";
               StaGiaGestendoUnaBlueprint = false;
               ID_blueprint_in_gestione = "";
               ModToast.showToast(
                  Component.translatable("portable_blueprints.toast.global_blueprints.export.title"),
                  Component.translatable("portable_blueprints.toast.global_blueprints.export.subtitle2"),
                  new ItemStack((ItemLike)ModItems.GLOBAL_BLUEPRINT.get()),
                  -13447886,
                  -1
               );
               ProgressBarBlueprintPlayer.impostaValore(2, sizeData + 1);
            } else if (msg.valore.contains("[" + ID_blueprint_in_gestione + "]")) {
               try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(Blueprint.getGlobalBlueprintDir(), NOME_BLUEPRINT + ".tmp"), true))) {
                  writer.write(msg.valore.replace("[" + ID_blueprint_in_gestione + "]", ""));
               } catch (Exception e) {
                  e.printStackTrace();
               }

               ProgressBarBlueprintPlayer.progressoPlus(2);
            }
         }
      );
   }
}
