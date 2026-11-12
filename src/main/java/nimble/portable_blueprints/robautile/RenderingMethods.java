package nimble.portable_blueprints.robautile;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import org.joml.Vector2i;

public class RenderingMethods {
   public static void GuiGraphicsTestoScalato(
      GuiGraphics gg, String testo, float Scala, Font font, Vec2 position, int Color, boolean Ombra, RenderingMethods.PosizioneTesto Position
   ) {
      guiGraphicsTestoScalato(gg, testo, Scala, font, position, Color, Ombra, Position, false);
   }

   public static void GuiGraphicsTestoScalato(
      GuiGraphics gg,
      String testo,
      float Scala,
      Font font,
      Vec2 position,
      int Color,
      boolean Ombra,
      RenderingMethods.PosizioneTesto Position,
      boolean YCentrale
   ) {
      guiGraphicsTestoScalato(gg, testo, Scala, font, position, Color, Ombra, Position, YCentrale);
   }

   private static void guiGraphicsTestoScalato(
      GuiGraphics gg,
      String testo,
      float Scala,
      Font font,
      Vec2 position,
      int Color,
      boolean Ombra,
      RenderingMethods.PosizioneTesto Position,
      boolean YCentrale
   ) {
      String valore = Operazioni.getValueInsideTag(testo, "remove_size");
      if (!valore.isEmpty()) {
         testo = testo.replace("<remove_size>" + valore + "</remove_size>", "");
         Scala -= Float.parseFloat(valore);
      }

      PoseStack poseStack = gg.pose();
      poseStack.pushPose();
      poseStack.scale(Scala, Scala, Scala);
      String[] split = testo.split("\n");

      for (int x = 0; x < split.length; x++) {
         testo = split[x];
         float Y = YCentrale ? position.y / Scala - 9 / 2 + x * 9 : position.y / Scala + x * 9;
         if (Position.equals(RenderingMethods.PosizioneTesto.CENTRO)) {
            gg.drawString(font, testo, Math.round(position.x / Scala - font.width(testo) / 2), Math.round(Y), Color, Ombra);
         } else if (Position.equals(RenderingMethods.PosizioneTesto.SINISTRA)) {
            gg.drawString(font, testo, Math.round(position.x / Scala), Math.round(Y), Color, Ombra);
         } else {
            gg.drawString(font, testo, Math.round(position.x / Scala - font.width(testo)), Math.round(Y), Color, Ombra);
         }
      }

      poseStack.popPose();
   }

   public static void renderTextInWorld(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      Camera camera,
      Vec3 pos,
      String testo,
      Font font,
      float Scale,
      int Color,
      float MaxDistanceRender,
      float MinDistanceResize,
      boolean onTopOfBlocks
   ) {
      Minecraft mc = Minecraft.getInstance();
      double targetX = pos.x + 0.5;
      double targetY = pos.y + 0.5;
      double targetZ = pos.z + 0.5;
      double camX = camera.getPosition().x;
      double camY = camera.getPosition().y;
      double camZ = camera.getPosition().z;
      double distanceX = targetX - camX;
      double distanceY = targetY - camY;
      double distanceZ = targetZ - camZ;
      double distance = mc.player.position().distanceTo(pos);
      if (!(distance > MaxDistanceRender)) {
         poseStack.pushPose();
         poseStack.translate(distanceX, distanceY, distanceZ);
         poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
         poseStack.scale(-0.025F, -0.025F, -0.025F);
         if (distance > MinDistanceResize) {
            Scale = (float)Operazioni.lerp(Scale, Scale * 10.0F, (distance - MinDistanceResize) / (MaxDistanceRender - MinDistanceResize));
         }

         poseStack.scale(Scale, Scale, Scale);
         float textWidth = font.width(testo) / 2.0F;
         font.drawInBatch(
            testo,
            -textWidth,
            -(9 / 2),
            Color,
            !onTopOfBlocks,
            poseStack.last().pose(),
            bufferSource,
            onTopOfBlocks ? DisplayMode.SEE_THROUGH : DisplayMode.NORMAL,
            0,
            15728880
         );
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         poseStack.popPose();
      }
   }

   public static void RenderItemInGui(GuiGraphics gg, Vector2i pos, Vec2 offset, float Scale, ItemStack item) {
      PoseStack pMatrixStack = gg.pose();
      pMatrixStack.pushPose();
      pMatrixStack.scale(Scale, Scale, Scale);
      pMatrixStack.translate(offset.x, offset.y, 0.0F);
      gg.renderFakeItem(item, (int)(pos.x / Scale), (int)(pos.y / Scale));
      pMatrixStack.popPose();
   }

   public static void renderFixedTextInWorld(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      Camera camera,
      Vec3 pos,
      Vec3 rotation,
      String testo,
      Font font,
      float Scale,
      int Color,
      float MaxDistanceRender,
      boolean onTopOfBlocks
   ) {
      Minecraft mc = Minecraft.getInstance();
      double targetX = pos.x + 0.5;
      double targetY = pos.y + 0.5;
      double targetZ = pos.z + 0.5;
      double camX = camera.getPosition().x;
      double camY = camera.getPosition().y;
      double camZ = camera.getPosition().z;
      double distanceX = targetX - camX;
      double distanceY = targetY - camY;
      double distanceZ = targetZ - camZ;
      double distance = mc.player.position().distanceTo(pos);
      if (!(distance > MaxDistanceRender)) {
         poseStack.pushPose();
         poseStack.translate(distanceX, distanceY, distanceZ);
         poseStack.mulPose(Axis.XP.rotationDegrees((float)rotation.x));
         poseStack.mulPose(Axis.YP.rotationDegrees((float)rotation.y));
         poseStack.mulPose(Axis.ZP.rotationDegrees((float)rotation.z));
         poseStack.scale(-Scale, -Scale, -Scale);
         float textWidth = font.width(testo) / 2.0F;
         font.drawInBatch(
            testo,
            -textWidth,
            -(9 / 2),
            Color,
            !onTopOfBlocks,
            poseStack.last().pose(),
            bufferSource,
            onTopOfBlocks ? DisplayMode.SEE_THROUGH : DisplayMode.NORMAL,
            0,
            15728880
         );
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         poseStack.popPose();
      }
   }

   public static void RenderItemInWorld(PoseStack poseStack, Vec3 renderPos, Item item, float scale) {
      Minecraft mc = Minecraft.getInstance();
      ItemStack stack = new ItemStack(item);
      Camera camera = mc.gameRenderer.getMainCamera();
      Vec3 cameraPos = camera.getPosition();
      double x = renderPos.x - cameraPos.x;
      double y = renderPos.y - cameraPos.y;
      double z = renderPos.z - cameraPos.z;
      poseStack.pushPose();
      BufferSource buffer = mc.renderBuffers().bufferSource();
      if (stack.getItem() instanceof BlockItem blockItem) {
         float val = (1.0F - scale) / 2.0F;
         poseStack.translate(x - 0.5 + val, y - 0.5 + val, z - 0.5 + val);
         poseStack.scale(scale, scale, scale);
         BlockRenderDispatcher render = mc.getBlockRenderer();
         BlockPos pos = new BlockPos(0, 0, 0);
         render.renderSingleBlock(
            blockItem.getBlock().defaultBlockState(),
            poseStack,
            mc.renderBuffers().crumblingBufferSource(),
            15728880,
            OverlayTexture.NO_OVERLAY
         );
      } else {
         poseStack.scale(scale, scale, scale);
         poseStack.translate(x, y, z);
         ItemRenderer itemRenderer = mc.getItemRenderer();
         itemRenderer.render(stack, ItemDisplayContext.FIXED, true, poseStack, buffer, 15728880, 0, itemRenderer.getModel(stack, null, null, 0));
      }

      buffer.endBatch();
      poseStack.popPose();
   }

   public static void GUITextAutoResize(
      GuiGraphics gg, String testo, Font font, int Color, float StartPosX, float StartPosY, int width, int height, boolean Ombra
   ) {
      float scale = 1.0F;
      int textWidth = font.width(testo);
      int textHeight = 9;
      if (!(textWidth * scale > width) && !(textHeight * scale > height)) {
         if (textWidth * scale < width && textHeight * scale < height) {
            while (textWidth * scale < width && textHeight * scale < height) {
               scale += 0.01F;
               if (scale >= 100.0F) {
                  break;
               }
            }
         }
      } else {
         while (textWidth * scale > width || textHeight * scale > height) {
            scale -= 0.01F;
            if (scale <= 0.1F) {
               break;
            }
         }
      }

      float scaledWidth = textWidth * scale;
      float scaledHeight = textHeight * scale;
      float centeredPosX = StartPosX + (width - scaledWidth) / 2.0F;
      float centeredPosY = StartPosY + (height - scaledHeight) / 2.0F;
      gg.pose().pushPose();
      gg.pose().scale(scale, scale, 1.0F);
      gg.drawString(font, testo, Math.round(centeredPosX / scale), Math.round(centeredPosY / scale), Color, Ombra);
      gg.pose().popPose();
   }

   public static void enableScissorFromGui(Minecraft mc, int x, int y, int width, int height) {
      double scale = mc.getWindow().getGuiScale();
      int windowWidth = mc.getWindow().getWidth();
      int windowHeight = mc.getWindow().getHeight();
      int px = (int)(x * scale);
      int py = (int)(y * scale);
      int pw = (int)(width * scale);
      int ph = (int)(height * scale);
      py = windowHeight - ph - py;
      RenderSystem.enableScissor(px, py, pw, ph);
   }

   public static void disableScissor() {
      RenderSystem.disableScissor();
   }

   public static class GuiGraphicsElement {
      private RenderingMethods.GuiGraphicsElement.Tipo type = RenderingMethods.GuiGraphicsElement.Tipo.FILL;
      public RenderingMethods.GuiGraphicsElement.FILL_Parametri fillParametri = null;
      public RenderingMethods.GuiGraphicsElement.BLIT_Parametri blitParametri = null;
      public RenderingMethods.GuiGraphicsElement.TESTO_Parametri testoParametri = null;
      private RenderingMethods.GuiGraphicsElement.ITEM_Parametri itemParametri = null;

      public GuiGraphicsElement(RenderingMethods.GuiGraphicsElement.FILL_Parametri fillParametri) {
         this.type = RenderingMethods.GuiGraphicsElement.Tipo.FILL;
         this.fillParametri = fillParametri;
      }

      public GuiGraphicsElement(RenderingMethods.GuiGraphicsElement.BLIT_Parametri blitParametri) {
         this.type = RenderingMethods.GuiGraphicsElement.Tipo.BLIT;
         this.blitParametri = blitParametri;
      }

      public GuiGraphicsElement(RenderingMethods.GuiGraphicsElement.TESTO_Parametri testoParametri) {
         this.type = RenderingMethods.GuiGraphicsElement.Tipo.TESTO;
         this.testoParametri = testoParametri;
      }

      public GuiGraphicsElement(RenderingMethods.GuiGraphicsElement.ITEM_Parametri itemParametri) {
         this.type = RenderingMethods.GuiGraphicsElement.Tipo.ITEM;
         this.itemParametri = itemParametri;
      }

      public void render(GuiGraphics gg) {
         switch (this.type) {
            case FILL:
               if (this.fillParametri == null) {
                  break;
               }

               gg.pose().pushPose();
               gg.pose().translate(this.fillParametri.PosOffsetX, this.fillParametri.PosOffsetY, 0.0);
               gg.fill(
                  this.fillParametri.startPosX, this.fillParametri.startPosY, this.fillParametri.endPosX, this.fillParametri.endPosY, this.fillParametri.color
               );
               gg.pose().popPose();
            case BLIT:
               if (this.blitParametri == null) {
                  break;
               }

               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               gg.pose().pushPose();
               gg.pose().translate(this.blitParametri.PosOffsetX, this.blitParametri.PosOffsetY, 0.0F);
               gg.blit(
                  this.blitParametri.img,
                  this.blitParametri.startPosX,
                  this.blitParametri.startPosY,
                  this.blitParametri.offsetX,
                  this.blitParametri.offsetY,
                  this.blitParametri.width,
                  this.blitParametri.height
               );
               gg.pose().popPose();
               RenderSystem.disableBlend();
            case TESTO:
               if (this.testoParametri == null) {
                  break;
               }

               RenderingMethods.GuiGraphicsTestoScalato(
                  gg,
                  this.testoParametri.testo,
                  this.testoParametri.scale,
                  this.testoParametri.font,
                  new Vec2(this.testoParametri.startPosX, this.testoParametri.startPosY),
                  this.testoParametri.color,
                  this.testoParametri.ombra,
                  this.testoParametri.posizione,
                  this.testoParametri.YCentrata
               );
            case ITEM:
               if (this.itemParametri != null) {
                  RenderingMethods.RenderItemInGui(
                     gg,
                     new Vector2i(this.itemParametri.startPosX, this.itemParametri.startPosY),
                     new Vec2(this.itemParametri.offsetX, this.itemParametri.offsetY),
                     this.itemParametri.scale,
                     Operazioni.getCorrectItemStackFromId(Operazioni.getBlockFromString(this.itemParametri.id))
                  );
               }
         }
      }

      public static class BLIT_Parametri {
         public int startPosX;
         public int startPosY;
         public int offsetX;
         public int offsetY;
         public int width;
         public int height;
         public float PosOffsetX = 0.0F;
         public float PosOffsetY = 0.0F;
         public ResourceLocation img;

         public BLIT_Parametri(int startPosX, int startPosY, int offsetX, int offsetY, int width, int height, ResourceLocation img) {
            this.startPosX = startPosX;
            this.startPosY = startPosY;
            this.img = img;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.width = width;
            this.height = height;
         }

         public void setPos(int startPosX, int startPosY) {
            this.startPosX = startPosX;
            this.startPosY = startPosY;
         }

         public void setPosOffset(float PosOffsetX, float PosOffsetY) {
            this.PosOffsetX = PosOffsetX;
            this.PosOffsetY = PosOffsetY;
         }

         public void setImage(int offsetX, int offsetY, int width, int height, ResourceLocation img) {
            this.img = img;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.width = width;
            this.height = height;
         }
      }

      public static class FILL_Parametri {
         public int startPosX;
         public int startPosY;
         public int endPosX;
         public int endPosY;
         public int color;
         public double PosOffsetX;
         public double PosOffsetY;

         public FILL_Parametri(int startPosX, int startPosY, int endPosX, int endPosY, int color) {
            this.startPosX = startPosX;
            this.startPosY = startPosY;
            this.endPosX = endPosX;
            this.endPosY = endPosY;
            this.color = color;
         }

         public FILL_Parametri(int startPosX, int startPosY, int endPosX, int endPosY, int color, double PosOffsetX, double PosOffsetY) {
            this.startPosX = startPosX;
            this.startPosY = startPosY;
            this.endPosX = endPosX;
            this.endPosY = endPosY;
            this.color = color;
            this.PosOffsetX = PosOffsetX;
            this.PosOffsetY = PosOffsetY;
         }

         public void setPosOffset(double PosOffsetX, double PosOffsetY) {
            this.PosOffsetX = PosOffsetX;
            this.PosOffsetY = PosOffsetY;
         }

         public void setColor(int color) {
            this.color = color;
         }
      }

      public static class ITEM_Parametri {
         public String id;
         public int startPosX;
         public int startPosY;
         public float scale;
         public float offsetX;
         public float offsetY;

         public ITEM_Parametri(int startPosX, int startPosY, float offsetX, float offsetY, String id, float scale) {
            this.startPosX = startPosX;
            this.startPosY = startPosY;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.id = id;
            this.scale = scale;
         }
      }

      public static class TESTO_Parametri {
         public int color;
         public String testo;
         public Font font;
         public float startPosX;
         public float startPosY;
         public float scale;
         public boolean ombra;
         public boolean YCentrata;
         public RenderingMethods.PosizioneTesto posizione;

         public TESTO_Parametri(
            float startPosX,
            float startPosY,
            String testo,
            float scale,
            Font font,
            int color,
            boolean ombra,
            RenderingMethods.PosizioneTesto posizione,
            boolean YCentrata
         ) {
            this.startPosX = startPosX;
            this.startPosY = startPosY;
            this.color = color;
            this.testo = testo;
            this.font = font;
            this.scale = scale;
            this.posizione = posizione;
            this.ombra = ombra;
            this.YCentrata = YCentrata;
         }

         public void setTesto(String val) {
            this.testo = val;
         }
      }

      public enum Tipo {
         FILL,
         BLIT,
         TESTO,
         ITEM;
      }
   }

   public enum PosizioneTesto {
      SINISTRA,
      CENTRO,
      DESTRA;
   }
}
