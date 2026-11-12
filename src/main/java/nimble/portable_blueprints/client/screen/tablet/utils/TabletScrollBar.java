package nimble.portable_blueprints.client.screen.tablet.utils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TabletScrollBar extends AbstractWidget {
   private int posX = 0;
   private int posY = 0;
   private int larghezza = 0;
   private int altezza = 0;
   private int coloreSotto = -16777216;
   private int coloreSopra = -16776961;
   private TabletScrollBar.ScrollingArea scrollingArea;
   public boolean indrag = false;
   private int altezzaAttuale = 0;
   private int altezzaMassima = 0;
   private int altezzaBar = 5;

   public TabletScrollBar(int posX, int posY, int larghezza, int altezza, int coloreSotto, int coloreSopra, TabletScrollBar.ScrollingArea scrollingArea) {
      super(0, 0, 0, 0, Component.literal(""));
      this.posX = posX;
      this.posY = posY;
      this.larghezza = larghezza;
      this.altezza = altezza;
      this.coloreSotto = coloreSotto;
      this.coloreSopra = coloreSopra;
      this.scrollingArea = scrollingArea;
      this.altezzaMassima = altezza - this.altezzaBar;
   }

   public void setAltezzaAttuale(int altezzaDesiderata) {
      this.altezzaAttuale = altezzaDesiderata;
   }

   protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float deltaTime) {
      gg.fill(this.posX, this.posY, this.posX + this.larghezza, this.posY + this.altezza, this.coloreSotto);
      gg.fill(this.posX, this.posY + this.altezzaAttuale, this.posX + this.larghezza, this.posY + this.altezzaAttuale + this.altezzaBar, this.coloreSopra);
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0
         && mouseX >= this.posX
         && mouseX < this.posX + this.larghezza
         && mouseY >= this.posY + this.altezzaAttuale
         && mouseY < this.posY + this.altezzaAttuale + this.altezzaBar) {
         this.indrag = true;
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.indrag = false;
      return true;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.indrag) {
         this.altezzaAttuale = (int)(mouseY - this.posY);
         this.altezzaAttuale = Mth.clamp(this.altezzaAttuale, 0, this.altezzaMassima);
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta, double delta2) {
      if (mouseX >= this.scrollingArea.StartX
         && mouseX < this.scrollingArea.StartX + this.scrollingArea.Larghezza
         && mouseY >= this.scrollingArea.StartY
         && mouseY < this.scrollingArea.StartY + this.scrollingArea.Altezza) {
         this.altezzaAttuale = this.altezzaAttuale + (delta < 0.0 ? this.scrollingArea.Scrollingvalue : (delta > 0.0 ? -this.scrollingArea.Scrollingvalue : 0));
         this.altezzaAttuale = Mth.clamp(this.altezzaAttuale, 0, this.altezzaMassima);
      }

      return super.mouseScrolled(mouseX, mouseY, delta2, delta);
   }

   public double getValore(double valMax) {
      return (float)this.altezzaAttuale / this.altezzaMassima * valMax;
   }

   public static class ScrollingArea {
      public final int StartX;
      public final int StartY;
      public final int Larghezza;
      public final int Altezza;
      public final int Scrollingvalue;

      public ScrollingArea(int startX, int startY, int larghezza, int altezza, int scrollingvalue) {
         this.StartX = startX;
         this.StartY = startY;
         this.Larghezza = larghezza;
         this.Altezza = altezza;
         this.Scrollingvalue = scrollingvalue;
      }
   }
}
