package nimble.portable_blueprints.robautile;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

public class AdvancedEditBox extends EditBox {
   private final Font font;
   private final float fontSize;
   private Component Message;
   private boolean visibleText;
   private AdvancedEditBox.ColorHover colorHover;
   public int _posX = 0;
   public int _posY = 0;

   public AdvancedEditBox(
      int pX,
      int pY,
      int pWidth,
      int pHeight,
      Component pMessage,
      Font font,
      float fontSize,
      AdvancedEditBox.ColorHover colorHover,
      boolean withBorder,
      boolean visibleText
   ) {
      super(font, pX, pY, pWidth, pHeight, pMessage);
      this._posX = pX;
      this._posY = pY;
      this.Message = pMessage;
      this.font = Minecraft.getInstance().font;
      this.colorHover = colorHover;
      this.setBordered(withBorder);
      this.visibleText = visibleText;
      this.fontSize = fontSize;
   }

   public AdvancedEditBox(AdvancedEditBox.Builder builder) {
      this(
         builder.x,
         builder.y,
         builder.width,
         builder.height,
         builder.message,
         builder.font,
         builder.fontSize,
         builder.colorHover,
         builder.withBorder,
         builder.visibleText
      );
      this.setTooltip(builder.tooltip);
   }

   public static AdvancedEditBox.Builder builder(Component pMessage) {
      return new AdvancedEditBox.Builder(pMessage);
   }

   public void renderWidget(GuiGraphics guiGraphics, int MouseX, int MouseY, float p_93660_) {
      if (this.visibleText) {
         super.renderWidget(guiGraphics, MouseX, MouseY, p_93660_);
         if (this.colorHover != null && !this.isFocused() && this.isMouseOver(MouseX, MouseY)) {
            RenderSystem.setShaderColor(
               (float)(this.colorHover.R / 255.0),
               (float)(this.colorHover.G / 255.0),
               (float)(this.colorHover.B / 255.0),
               (float)(this.colorHover.alpha / 255.0)
            );
            guiGraphics.fill((int)this.colorHover.StartPos.x, (int)this.colorHover.StartPos.y, (int)this.colorHover.EndPos.x, (int)this.colorHover.EndPos.y, -1);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         }
      }
   }

   public static class Builder {
      private final Component message;
      private int x;
      private int y;
      private int width = 150;
      private int height = 20;
      private Tooltip tooltip;
      private float fontSize = 1.0F;
      private boolean withBorder = true;
      private boolean visibleText = true;
      private Font font;
      private AdvancedEditBox.ColorHover colorHover;

      public Builder(Component TestoPulsante) {
         this.font = Minecraft.getInstance().font;
         this.message = TestoPulsante;
      }

      public AdvancedEditBox.Builder pos(int pX, int pY) {
         this.x = pX;
         this.y = pY;
         return this;
      }

      public AdvancedEditBox.Builder size(int pWidth, int pHeight) {
         this.width = pWidth;
         this.height = pHeight;
         return this;
      }

      public AdvancedEditBox.Builder tooltip(@Nullable Tooltip pTooltip) {
         this.tooltip = pTooltip;
         return this;
      }

      public AdvancedEditBox.Builder addColorHover(AdvancedEditBox.ColorHover colorHover) {
         this.colorHover = colorHover;
         return this;
      }

      public AdvancedEditBox.Builder setWithBorder(boolean withBorder) {
         this.withBorder = withBorder;
         return this;
      }

      public AdvancedEditBox.Builder setvisibleText(boolean visibleText) {
         this.visibleText = visibleText;
         return this;
      }

      public AdvancedEditBox build() {
         return (AdvancedEditBox)this.build(AdvancedEditBox::new);
      }

      public Object build(Function<AdvancedEditBox.Builder, Object> builder) {
         return builder.apply(this);
      }
   }

   public static class ColorHover {
      public double R;
      public double G;
      public double B;
      public double alpha;
      public Vec2 StartPos;
      public Vec2 EndPos;

      public ColorHover(int Color, Vec2 StartPos, Vec2 EndPos) {
         this.alpha = Color >> 24 & 0xFF;
         this.R = Color >> 16 & 0xFF;
         this.G = Color >> 8 & 0xFF;
         this.B = Color & 0xFF;
         this.StartPos = StartPos;
         this.EndPos = EndPos;
      }
   }
}
