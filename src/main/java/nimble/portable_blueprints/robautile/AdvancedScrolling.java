package nimble.portable_blueprints.robautile;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AdvancedScrolling extends AbstractWidget {
   private int posX = 0;
   private int posY = 0;
   private AdvancedScrolling.Image DefaultImage;
   private AdvancedScrolling.Image SelectedImage;
   private boolean tenendoPremuto = false;
   private int AltezzaMinima = 0;
   private int AltezzaMassima = 0;
   private int AltezzaAttuale = 0;

   public AdvancedScrolling(
      int x, int y, int width, int height, AdvancedScrolling.Image DefaultImage, AdvancedScrolling.Image SelectedImage, int altezzaMinima, int altezzaMassima
   ) {
      super(-1000, -1000, width, height, Component.literal(""));
      this.DefaultImage = DefaultImage;
      this.SelectedImage = SelectedImage;
      this.posX = x;
      this.posY = y;
      this.AltezzaMinima = altezzaMinima;
      this.AltezzaMassima = altezzaMassima;
      this.AltezzaAttuale = altezzaMinima;
   }

   public void setPosX(int v) {
      this.posX = v;
   }

   public void setPosY(int v) {
      this.posY = v;
   }

   public void setPos(int x, int y) {
      this.posX = x;
      this.posY = y;
   }

   protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float delta) {
      if (this.tenendoPremuto) {
         this.AltezzaAttuale = mouseY - this.getHeight() / 2;
         if (this.AltezzaAttuale > this.AltezzaMassima) {
            this.AltezzaAttuale = this.AltezzaMassima;
         } else if (this.AltezzaAttuale < this.AltezzaMinima) {
            this.AltezzaAttuale = this.AltezzaMinima;
         }
      }

      if (!this.tenendoPremuto) {
         gg.blit(
            this.DefaultImage.ImageResource,
            this.posX + this.DefaultImage.posX,
            this.AltezzaAttuale + this.DefaultImage.posY,
            this.DefaultImage.IMGoffsetX,
            this.DefaultImage.IMGoffsetY,
            this.DefaultImage.width,
            this.DefaultImage.height
         );
      } else {
         gg.blit(
            this.SelectedImage.ImageResource,
            this.posX + this.SelectedImage.posX,
            this.AltezzaAttuale + this.SelectedImage.posY,
            this.SelectedImage.IMGoffsetX,
            this.SelectedImage.IMGoffsetY,
            this.SelectedImage.width,
            this.SelectedImage.height
         );
      }
   }

   protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
   }

   public void _mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0
         && mouseX > this.posX
         && mouseX < this.posX + this.getWidth()
         && mouseY > this.AltezzaAttuale
         && mouseY < this.AltezzaAttuale + this.getHeight()) {
         this.tenendoPremuto = true;
      }
   }

   public void _mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0) {
         this.tenendoPremuto = false;
      }
   }

   public boolean _mouseScrolled(double mouseX, double mouseY, double delta, double delta2) {
      if (delta < 0.0) {
         if (++this.AltezzaAttuale > this.AltezzaMassima) {
            this.AltezzaAttuale = this.AltezzaMassima;
         }
      } else if (delta > 0.0 && --this.AltezzaAttuale < this.AltezzaMinima) {
         this.AltezzaAttuale = this.AltezzaMinima;
      }

      return super.mouseScrolled(mouseX, mouseY, delta, delta2);
   }

   public double getValue() {
      return (double)(this.AltezzaAttuale - this.AltezzaMinima) / (this.AltezzaMassima - this.AltezzaMinima);
   }

   public static class Image {
      private final ResourceLocation ImageResource;
      private final int posX;
      private final int posY;
      private final int IMGoffsetX;
      private final int IMGoffsetY;
      private final int width;
      private final int height;

      public Image(ResourceLocation ImageResource, int x, int y, int IMGoffsetX, int IMGoffsetY, int width, int height) {
         this.posX = x;
         this.posY = y;
         this.ImageResource = ImageResource;
         this.IMGoffsetX = IMGoffsetX;
         this.IMGoffsetY = IMGoffsetY;
         this.width = width;
         this.height = height;
      }
   }
}
