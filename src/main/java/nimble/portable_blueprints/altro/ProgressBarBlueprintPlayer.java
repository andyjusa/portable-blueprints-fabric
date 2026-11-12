package nimble.portable_blueprints.altro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.robautile.Operazioni;
import nimble.portable_blueprints.robautile.RenderingMethods;

public class ProgressBarBlueprintPlayer {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/overlays_gui.png");
   public static Map<Integer, ProgressBarBlueprintPlayer.ProgressBar> BarreAttive = new HashMap<>();
   static short Ticks = 1;

   public static void RenderOverlay(GuiGraphics graphics) {
      if (!BarreAttive.isEmpty()) {
         List<Integer> daRimuovere = new ArrayList<>();
         int count = 0;

         for (int bar : BarreAttive.keySet()) {
            BarreAttive.get(bar).render(graphics, count);
            count -= 14;
            if (BarreAttive.get(bar).canDelete()) {
               daRimuovere.add(bar);
            }
         }

         for (int r : daRimuovere) {
            BarreAttive.remove(r);
         }
      }
   }

   public static void ClientTick() {
      if (Ticks % 5 == 0) {
         ProgressBarBlueprintPlayer.ProgressBar.updateG();
      }

      Ticks++;
      if (Ticks > 20) {
         Ticks = 1;
      }
   }

   public static void Reset() {
      BarreAttive.clear();
   }

   public static void aggiungi(int IDProgressBar, int maxSize) {
      if (BarreAttive.containsKey(IDProgressBar)) {
         BarreAttive.replace(IDProgressBar, new ProgressBarBlueprintPlayer.ProgressBar((short)IDProgressBar, maxSize));
      } else {
         BarreAttive.put(IDProgressBar, new ProgressBarBlueprintPlayer.ProgressBar((short)IDProgressBar, maxSize));
      }
   }

   public static void impostaValore(int IDProgressBar, int val) {
      if (BarreAttive.containsKey(IDProgressBar)) {
         BarreAttive.get(IDProgressBar).setProgresso_attuale(val);
      }
   }

   public static void progressoPlus(int IDProgressBar) {
      if (BarreAttive.containsKey(IDProgressBar) && BarreAttive.get(IDProgressBar).progresso_attuale + 1 <= BarreAttive.get(IDProgressBar).progresso_massimo) {
         BarreAttive.get(IDProgressBar).progresso_attuale++;
      }
   }

   private static class ProgressBar {
      private static String Points = "";
      private int progresso_attuale;
      private int progresso_massimo;
      private String Testo;
      private boolean daEliminare = false;
      private Long deleteAfter = 0L;
      private final short tipo;
      private int ultimoColore = -14185985;

      public ProgressBar(short tipo, int maxSize) {
         this.tipo = tipo;
         this.setProgresso_massimo(maxSize);
      }

      public static void updateG() {
         if (Points.length() < 3) {
            Points = Points + ".";
         } else {
            Points = ".";
         }
      }

      public void setProgresso_attuale(int p) {
         this.progresso_attuale = p;
      }

      public void setProgresso_massimo(int p) {
         this.progresso_attuale = 0;
         this.progresso_massimo = p;
      }

      public void render(GuiGraphics gg, int posY) {
         int CentroX = gg.guiWidth() / 2;
         int CentroY = gg.guiHeight() / 2 + posY;
         int posXMovimento = 0;
         int size = 0;
         Item itemInHand = !Minecraft.getInstance().player.getMainHandItem().isEmpty() ? Minecraft.getInstance().player.getMainHandItem().getItem() : Items.AIR;
         this.ultimoColore = this.tipo == 4
            ? -11141291
            : (
               this.tipo != 2 && this.tipo != 3
                  ? (
                     this.tipo == 0
                        ? -15878339
                        : (
                           itemInHand.equals(ModItems.BLUEPRINT.get())
                              ? -14185985
                              : (
                                 itemInHand.equals(ModItems.SERVER_BLUEPRINT.get())
                                    ? -2232778
                                    : (
                                       itemInHand.equals(ModItems.GLOBAL_BLUEPRINT.get())
                                          ? -1759774
                                          : (itemInHand.equals(ModItems.WORN_BLUEPRINT.get()) ? -11410958 : this.ultimoColore)
                                    )
                              )
                        )
                  )
                  : -1453289
            );
         if (this.daEliminare) {
            float t = 1.0F - (float)(this.deleteAfter - System.currentTimeMillis()) / 1000.0F;
            if (t <= 0.25) {
               size = (int)Operazioni.lerp(0.0F, 7.0F, t / 0.25F);
            } else if (t > 0.25 && t <= 0.75F) {
               float T = (t - 0.25F) / 0.5F;
               posXMovimento = (int)Operazioni.lerp(0.0F, 140.0F, T);
               size = 7;
            } else {
               float T = (t - 0.75F) / 0.25F;
               posXMovimento = 140;
               size = (int)Operazioni.lerp(7.0F, 0.0F, T);
            }
         }

         String Title = Component.translatable(
               "portable_blueprints.progress_bar."
                  + (
                     this.tipo == 0
                        ? "building_structure"
                        : (
                           this.tipo == 1
                              ? "loading_hologram"
                              : (this.tipo == 2 ? "exporting_blueprint" : (this.tipo == 4 ? "scanning" : "importing_blueprint"))
                        )
                  )
            )
            .getString();
         Title = Title + Points;
         if (!this.daEliminare) {
            gg.fill(CentroX - 50, CentroY + 61, CentroX + 50, CentroY + 71, -10263709);
            RenderingMethods.GuiGraphicsTestoScalato(
               gg, Title, 1.0F, Minecraft.getInstance().font, new Vec2(CentroX, CentroY + 66.5F), 9605779, false, RenderingMethods.PosizioneTesto.CENTRO, true
            );
            RenderingMethods.enableScissorFromGui(
               Minecraft.getInstance(), CentroX - 50, CentroY + 61, (int)((float)this.progresso_attuale / this.progresso_massimo * 100.0F), 10
            );
         } else {
            RenderingMethods.enableScissorFromGui(Minecraft.getInstance(), CentroX - 51, CentroY + 60, 120, 12);
         }

         if (!this.daEliminare) {
            RenderingMethods.enableScissorFromGui(
               Minecraft.getInstance(), CentroX - 50 + posXMovimento, CentroY + 61, (int)((float)this.progresso_attuale / this.progresso_massimo * 100.0F), 10
            );
         }

         gg.fill(CentroX - 50 + posXMovimento, CentroY + 61, CentroX + 50 + posXMovimento, CentroY + 71, this.ultimoColore);
         RenderingMethods.GuiGraphicsTestoScalato(
            gg,
            Title,
            1.0F,
            Minecraft.getInstance().font,
            new Vec2(CentroX + posXMovimento, CentroY + 66.5F),
            16777215,
            false,
            RenderingMethods.PosizioneTesto.CENTRO,
            true
         );
         if (!this.daEliminare) {
            RenderingMethods.disableScissor();
         }

         gg.blit(ProgressBarBlueprintPlayer.TEXTURE, CentroX - 51 + posXMovimento, CentroY + 60, 0, 244, 102, 12);
         if (this.daEliminare) {
            RenderingMethods.disableScissor();
            gg.blit(ProgressBarBlueprintPlayer.TEXTURE, CentroX + 72 - size, CentroY + 66 - size, 249 - size, 249 - size, size * 2, size * 2);
         }

         if (this.progresso_attuale == this.progresso_massimo && !this.daEliminare) {
            this.daEliminare = true;
            this.deleteAfter = System.currentTimeMillis() + 1000L;
         }
      }

      public boolean canDelete() {
         return this.daEliminare && System.currentTimeMillis() > this.deleteAfter;
      }
   }
}
