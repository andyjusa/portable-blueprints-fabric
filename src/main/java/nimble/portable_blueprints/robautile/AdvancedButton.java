package nimble.portable_blueprints.robautile;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2i;

public class AdvancedButton extends AbstractWidget {
   protected final AdvancedButton.OnPress onPress;
   private final int tickTime;
   private final Font font;
   private int fontColor;
   private final int originalColor;
   private final Vec2 fontOffset;
   private final boolean fontHasShadow;
   private final float fontSize;
   private final Vector2i messagePositionOffset;
   private final Vector2i messageMaxSize;
   private Component Message;
   private AdvancedButton.Image DefaultImage;
   private AdvancedButton.Image ImageCondition;
   private AdvancedButton.Image PressedImageTick;
   private AdvancedButton.Image HoverImage;
   private boolean hasPressedCondition;
   private boolean hasPressedTick;
   public boolean PressedImageCondition = false;
   private int PressedTickRemain = 0;
   private AdvancedButton.ColorHover colorHover;
   private boolean hasHoverImage = false;
   private long LastPressTime = System.currentTimeMillis();
   private long AspettaTot = System.currentTimeMillis();
   private long TempoCheDevePassarePerIlProssimo = 500L;
   private boolean hasHoldPress = false;
   private Vec2 LongPressPosStart;
   public Tooltip tooltip = null;
   private boolean tenendoPremuto = false;
   public int posX;
   public int posY;

   public AdvancedButton(
      int pX,
      int pY,
      int pWidth,
      int pHeight,
      Component pMessage,
      AdvancedButton.OnPress onPress,
      AdvancedButton.Image DefaultImage,
      AdvancedButton.Image ImageCondition,
      boolean hasPressedCondition,
      boolean hasPressedTick,
      AdvancedButton.Image PressedImageTick,
      int tickTime,
      int fontColor,
      Vec2 fontOffset,
      boolean fontHasShadow,
      float fontSize,
      Vector2i messagePositionOffset,
      Vector2i messageMaxSize,
      boolean hasHoverImage,
      AdvancedButton.Image HoverImage,
      AdvancedButton.ColorHover colorHover,
      boolean hasHoldPress,
      long TempoCheDevePassarePerIlProssimo
   ) {
      super(pX, pY, pWidth, pHeight, pMessage);
      this.posX = pX;
      this.posY = pY;
      this.onPress = onPress;
      this.Message = pMessage;
      this.tickTime = tickTime;
      this.DefaultImage = DefaultImage;
      this.ImageCondition = ImageCondition;
      this.PressedImageTick = PressedImageTick;
      this.HoverImage = HoverImage;
      this.colorHover = colorHover;
      this.hasPressedCondition = hasPressedCondition;
      this.hasPressedTick = hasPressedTick;
      this.hasHoverImage = hasHoverImage;
      this.font = Minecraft.getInstance().font;
      this.originalColor = fontColor;
      this.fontColor = this.originalColor;
      this.hasHoldPress = hasHoldPress;
      this.TempoCheDevePassarePerIlProssimo = TempoCheDevePassarePerIlProssimo;
      if (fontOffset == null) {
         fontOffset = new Vec2(0.0F, 0.0F);
      }

      this.fontOffset = fontOffset;
      this.fontSize = fontSize;
      this.fontHasShadow = fontHasShadow;
      this.messagePositionOffset = messagePositionOffset;
      if (messageMaxSize == null) {
         this.messageMaxSize = new Vector2i(pWidth, pHeight);
      } else {
         this.messageMaxSize = messageMaxSize;
      }
   }

   public AdvancedButton(AdvancedButton.Builder builder) {
      this(
         builder.x,
         builder.y,
         builder.width,
         builder.height,
         builder.message,
         builder.onPress,
         builder.DefaultImage,
         builder.PressedImageCondition,
         builder.hasPressedImage,
         builder.hasPressedTick,
         builder.PressedImageTick,
         builder.tickTime,
         builder.fontColor,
         builder.fontOffset,
         builder.fontHasShadow,
         builder.fontSize,
         builder.messagePositionOffset,
         builder.messageMaxSize,
         builder.hasHoverImage,
         builder.HoverImage,
         builder.colorHover,
         builder.hasHoldPress,
         builder.DistaccoDaUnaChiamataELAltra
      );
      this.tooltip = builder.tooltip;
      this.setTooltip(builder.tooltip);
   }

   public static AdvancedButton.Builder builder(Component pMessage, AdvancedButton.OnPress pOnPress) {
      return new AdvancedButton.Builder(pMessage, pOnPress);
   }

   protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      if (this.hasHoldPress
         && this.tenendoPremuto
         && System.currentTimeMillis() > this.LastPressTime + this.TempoCheDevePassarePerIlProssimo
         && System.currentTimeMillis() > this.AspettaTot) {
         this.LastPressTime = System.currentTimeMillis();
         this.onPress();
      }

      int messageX = this.getX() + this.width / 2;
      int messageY = this.getY() + this.height / 2;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.hasHoldPress && this.tenendoPremuto) {
         if (this.ImageCondition != null) {
            pGuiGraphics.blit(
               this.ImageCondition.ButtonImageResource,
               this.ImageCondition.x,
               this.ImageCondition.y,
               this.ImageCondition.offsetX,
               this.ImageCondition.offsetY,
               this.ImageCondition.width,
               this.ImageCondition.height
            );
            messageX = this.ImageCondition.x + this.width / 2;
            messageY = this.ImageCondition.y + this.height / 2;
         } else if (this.PressedImageTick != null) {
            pGuiGraphics.blit(
               this.PressedImageTick.ButtonImageResource,
               this.PressedImageTick.x,
               this.PressedImageTick.y,
               this.PressedImageTick.offsetX,
               this.PressedImageTick.offsetY,
               this.PressedImageTick.width,
               this.PressedImageTick.height
            );
            messageX = this.PressedImageTick.x + this.width / 2;
            messageY = this.PressedImageTick.y + this.height / 2;
         }
      } else if (this.hasPressedCondition && this.PressedImageCondition && this.ImageCondition != null) {
         pGuiGraphics.blit(
            this.ImageCondition.ButtonImageResource,
            this.ImageCondition.x,
            this.ImageCondition.y,
            this.ImageCondition.offsetX,
            this.ImageCondition.offsetY,
            this.ImageCondition.width,
            this.ImageCondition.height
         );
         messageX = this.ImageCondition.x + this.width / 2;
         messageY = this.ImageCondition.y + this.height / 2;
      } else if (this.hasPressedTick && this.PressedTickRemain > 0) {
         this.PressedTickRemain--;
         pGuiGraphics.blit(
            this.PressedImageTick.ButtonImageResource,
            this.PressedImageTick.x,
            this.PressedImageTick.y,
            this.PressedImageTick.offsetX,
            this.PressedImageTick.offsetY,
            this.PressedImageTick.width,
            this.PressedImageTick.height
         );
         messageX = this.PressedImageTick.x + this.width / 2;
         messageY = this.PressedImageTick.y + this.height / 2;
      } else if (this.hasHoverImage && this.isMouseOver(pMouseX, pMouseY)) {
         pGuiGraphics.blit(
            this.HoverImage.ButtonImageResource,
            this.HoverImage.x,
            this.HoverImage.y,
            this.HoverImage.offsetX,
            this.HoverImage.offsetY,
            this.HoverImage.width,
            this.HoverImage.height
         );
      } else if (this.DefaultImage != null) {
         pGuiGraphics.blit(
            this.DefaultImage.ButtonImageResource,
            this.DefaultImage.x,
            this.DefaultImage.y,
            this.DefaultImage.offsetX,
            this.DefaultImage.offsetY,
            this.DefaultImage.width,
            this.DefaultImage.height
         );
      }

      RenderSystem.disableBlend();
      if (this.colorHover != null && this.isMouseOver(pMouseX, pMouseY) && this.PressedTickRemain == 0) {
         RenderSystem.setShaderColor(
            (float)(this.colorHover.R / 255.0), (float)(this.colorHover.G / 255.0), (float)(this.colorHover.B / 255.0), (float)(this.colorHover.alpha / 255.0)
         );
         pGuiGraphics.fill((int)this.colorHover.StartPos.x, (int)this.colorHover.StartPos.y, (int)this.colorHover.EndPos.x, (int)this.colorHover.EndPos.y, -1);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      if (!this.Message.getString().equalsIgnoreCase("")) {
         float scale = 1.0F * this.fontSize;
         String valore = Operazioni.getValueInsideTag(this.Message.getString(), "remove_size");
         String messaggio = this.Message.getString();
         if (!valore.isEmpty()) {
            messaggio = messaggio.replace("<remove_size>" + valore + "</remove_size>", "");
            scale -= Float.parseFloat(valore);
         }

         if ((int)(this.font.width(messaggio) * this.fontSize) > this.messageMaxSize.x) {
            scale = this.messageMaxSize.x / (this.font.width(messaggio) * this.fontSize);
         }

         RenderingMethods.GuiGraphicsTestoScalato(
            pGuiGraphics,
            this.Message.getString(),
            scale,
            this.font,
            new Vec2(messageX, messageY),
            this.fontColor,
            false,
            RenderingMethods.PosizioneTesto.CENTRO,
            true
         );
      }
   }

   protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
   }

   public boolean mouseClicked(double MouseX, double MouseY, int Button) {
      if (this.isMouseOver(MouseX, MouseY)) {
         if (this.hasHoldPress) {
            this.tenendoPremuto = true;
            this.LastPressTime = System.currentTimeMillis();
            this.AspettaTot = System.currentTimeMillis() + 500L;
            this.LongPressPosStart = new Vec2((float)MouseX, (float)MouseY);
            this.onPress();
         } else {
            if (this.hasPressedTick) {
               this.PressedTickRemain = this.tickTime;
            }

            this.onPress();
         }
      }

      return super.mouseClicked(MouseX, MouseY, Button);
   }

   public boolean mouseReleased(double p_93684_, double p_93685_, int p_93686_) {
      if (this.tenendoPremuto) {
         this.tenendoPremuto = false;
      }

      return super.mouseReleased(p_93684_, p_93685_, p_93686_);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.tenendoPremuto && Operazioni.calculateDistance(this.LongPressPosStart, new Vec2((float)mouseX, (float)mouseY)) > this.width) {
         this.tenendoPremuto = false;
      }

      return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
   }

   public void onPress() {
      this.onPress.onPress(this);
   }

   public void setFontColor(int color) {
      this.fontColor = color;
   }

   public void resetFontColor() {
      this.fontColor = this.originalColor;
   }

   public void setOffsetButton(int offsetX, int offsetY) {
      this.setPosition(this.posX + offsetX, this.posY + offsetY);
   }

   public static class Builder {
      private final Component message;
      private final AdvancedButton.OnPress onPress;
      private int x;
      private int y;
      private int width = 150;
      private int height = 20;
      private AdvancedButton.Image DefaultImage;
      private AdvancedButton.Image PressedImageCondition;
      private AdvancedButton.Image PressedImageTick;
      private AdvancedButton.Image HoverImage;
      private boolean hasPressedImage = false;
      private boolean hasPressedTick = false;
      private int tickTime;
      private int fontColor;
      private Vec2 fontOffset;
      private boolean fontHasShadow;
      private Tooltip tooltip;
      private float fontSize = 1.0F;
      private Vector2i messagePositionOffset = new Vector2i(0, 0);
      private Vector2i messageMaxSize;
      private AdvancedButton.ColorHover colorHover;
      private boolean hasHoverImage = false;
      private long DistaccoDaUnaChiamataELAltra;
      private boolean hasHoldPress = false;

      public Builder(Component TestoPulsante, AdvancedButton.OnPress pOnPress) {
         this.message = TestoPulsante;
         this.onPress = pOnPress;
      }

      public AdvancedButton.Builder pos(int pX, int pY) {
         this.x = pX;
         this.y = pY;
         return this;
      }

      public AdvancedButton.Builder size(int pWidth, int pHeight) {
         this.width = pWidth;
         this.height = pHeight;
         return this;
      }

      public AdvancedButton.Builder addColorHover(AdvancedButton.ColorHover colorHover) {
         this.colorHover = colorHover;
         return this;
      }

      public AdvancedButton.Builder defaultImage(AdvancedButton.Image DefaultImage) {
         this.DefaultImage = DefaultImage;
         return this;
      }

      public AdvancedButton.Builder pressedImageCondition(AdvancedButton.Image PressedImageCondition) {
         this.PressedImageCondition = PressedImageCondition;
         this.hasPressedImage = true;
         return this;
      }

      public AdvancedButton.Builder pressedImageTick(AdvancedButton.Image PressedImageTick, int tickTime) {
         this.hasPressedTick = true;
         this.PressedImageTick = PressedImageTick;
         this.tickTime = tickTime;
         return this;
      }

      public AdvancedButton.Builder hoverImage(AdvancedButton.Image HoverImage) {
         this.HoverImage = HoverImage;
         this.hasHoverImage = true;
         return this;
      }

      public AdvancedButton.Builder fontColor(int colore) {
         this.fontColor = colore;
         return this;
      }

      public AdvancedButton.Builder fontOffset(int offsetX, int offsetY) {
         this.fontOffset = new Vec2(offsetX, offsetY);
         return this;
      }

      public AdvancedButton.Builder fontHasShadow(boolean fontHasShadow) {
         this.fontHasShadow = fontHasShadow;
         return this;
      }

      public AdvancedButton.Builder fontSize(float fontSize) {
         this.fontSize = fontSize;
         return this;
      }

      public AdvancedButton.Builder setHoldButton(long DistaccoDaUnaChiamataELAltra) {
         this.DistaccoDaUnaChiamataELAltra = DistaccoDaUnaChiamataELAltra;
         this.hasHoldPress = true;
         return this;
      }

      public AdvancedButton.Builder tooltip(@Nullable Tooltip pTooltip) {
         this.tooltip = pTooltip;
         return this;
      }

      public AdvancedButton build() {
         return this.build(AdvancedButton::new);
      }

      public AdvancedButton build(Function<AdvancedButton.Builder, AdvancedButton> builder) {
         return builder.apply(this);
      }

      public AdvancedButton.Builder messagePositionOffset(int offsetX, int offsetY) {
         this.messagePositionOffset = new Vector2i(offsetX, offsetY);
         return this;
      }

      public AdvancedButton.Builder messageMaxSize(int sizeX, int sizeY) {
         this.messageMaxSize = new Vector2i(sizeX, sizeY);
         return this;
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

   public static class Image {
      private final ResourceLocation ButtonImageResource;
      private final int x;
      private final int y;
      private final int offsetX;
      private final int offsetY;
      private final int width;
      private final int height;

      public Image(ResourceLocation buttonImageResource, int x, int y, int offsetX, int offsetY, int width, int height) {
         this.ButtonImageResource = buttonImageResource;
         this.x = x;
         this.y = y;
         this.offsetX = offsetX;
         this.offsetY = offsetY;
         this.width = width;
         this.height = height;
      }
   }

   public interface OnPress {
      void onPress(AdvancedButton var1);
   }
}
