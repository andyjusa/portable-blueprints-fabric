package nimble.portable_blueprints.robautile;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderStateShard.CullStateShard;
import net.minecraft.client.renderer.RenderStateShard.DepthTestStateShard;
import net.minecraft.client.renderer.RenderStateShard.LightmapStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderStateShard.WriteMaskStateShard;
import net.minecraft.client.renderer.RenderStateShard;
import java.util.List;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RenderingGraphics {
   private static final RenderType TEXTURE_BORDER = createBorderRenderType();

   private static RenderType createBorderRenderType() {
      List<RenderStateShard> states = List.of(
         new ShaderStateShard(GameRenderer::getPositionTexColorShader),
         new TextureStateShard(ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/others/border.png"), false, false),
         new TransparencyStateShard("translucent_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
         }, RenderSystem::disableBlend),
         new DepthTestStateShard("lequal", 515),
         new CullStateShard(false),
         new WriteMaskStateShard(true, false),
         new LightmapStateShard(false)
      );
      return new RenderType("texture_border_no_light", DefaultVertexFormat.POSITION_TEX_COLOR, Mode.QUADS, 256, false, true,
         () -> states.forEach(RenderStateShard::setupRenderState),
         () -> states.forEach(RenderStateShard::clearRenderState)) {};
   }

   public static void addLineWidthToBufferBuilder(
      BufferBuilder buffer, Vector3f start, float lunghezza, float size, Direction direction, Vec3 color, float alpha
   ) {
      float meta = size / 2.0F;
      if (!direction.equals(Direction.UP) && !direction.equals(Direction.DOWN)) {
         if (!direction.equals(Direction.NORTH) && !direction.equals(Direction.SOUTH)) {
            float zAdd = direction.equals(Direction.EAST) ? meta : (direction.equals(Direction.WEST) ? -meta : 0.0F);
            float xAdd = direction.equals(Direction.EAST) ? lunghezza : (direction.equals(Direction.WEST) ? -lunghezza : 0.0F);
            int molt = direction.equals(Direction.EAST) ? 1 : -1;
            buffer.addVertex(start.x, start.y + meta, start.z - zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x, start.y + meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y + meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y + meta, start.z - zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x, start.y - meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x, start.y - meta, start.z - zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y - meta, start.z - zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y - meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x, start.y + meta, start.z + meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x, start.y - meta, start.z + meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y - meta, start.z + meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y + meta, start.z + meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x, start.y - meta, start.z - meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x, start.y + meta, start.z - meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y + meta, start.z - meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y - meta, start.z - meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x, start.y - meta, start.z - meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x, start.y - meta, start.z + meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x, start.y + meta, start.z + meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x, start.y + meta, start.z - meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y - meta, start.z + meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y - meta, start.z - meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y + meta, start.z - meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y + meta, start.z + meta * molt).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         } else {
            float xAdd = direction.equals(Direction.NORTH) ? meta : (direction.equals(Direction.SOUTH) ? -meta : 0.0F);
            float zAdd = direction.equals(Direction.NORTH) ? -lunghezza : (direction.equals(Direction.SOUTH) ? lunghezza : 0.0F);
            int molt = direction.equals(Direction.NORTH) ? 1 : -1;
            buffer.addVertex(start.x - xAdd, start.y + meta, start.z).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y + meta, start.z).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y + meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x - xAdd, start.y + meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y - meta, start.z).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x - xAdd, start.y - meta, start.z).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x - xAdd, start.y - meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + xAdd, start.y - meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + meta * molt, start.y + meta, start.z).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + meta * molt, start.y - meta, start.z).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + meta * molt, start.y - meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + meta * molt, start.y + meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x - meta * molt, start.y - meta, start.z).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x - meta * molt, start.y + meta, start.z).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x - meta * molt, start.y + meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x - meta * molt, start.y - meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x - meta * molt, start.y - meta, start.z).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + meta * molt, start.y - meta, start.z).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + meta * molt, start.y + meta, start.z).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x - meta * molt, start.y + meta, start.z).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + meta * molt, start.y - meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x - meta * molt, start.y - meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x - meta * molt, start.y + meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
            buffer.addVertex(start.x + meta * molt, start.y + meta, start.z + zAdd).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         }
      } else {
         float yAdd = direction.equals(Direction.UP) ? lunghezza : -lunghezza;
         buffer.addVertex(start.x + meta, start.y, start.z + meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x + meta, start.y, start.z - meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x + meta, start.y + yAdd, start.z - meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x + meta, start.y + yAdd, start.z + meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x - meta, start.y, start.z - meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x - meta, start.y, start.z + meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x - meta, start.y + yAdd, start.z + meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x - meta, start.y + yAdd, start.z - meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x - meta, start.y, start.z + meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x + meta, start.y, start.z + meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x + meta, start.y + yAdd, start.z + meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x - meta, start.y + yAdd, start.z + meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x + meta, start.y, start.z - meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x - meta, start.y, start.z - meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x - meta, start.y + yAdd, start.z - meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x + meta, start.y + yAdd, start.z - meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x - meta, start.y, start.z - meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x + meta, start.y, start.z - meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x + meta, start.y, start.z + meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x - meta, start.y, start.z + meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x - meta, start.y + yAdd, start.z - meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x - meta, start.y + yAdd, start.z + meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x + meta, start.y + yAdd, start.z + meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
         buffer.addVertex(start.x + meta, start.y + yAdd, start.z - meta).setColor((float)color.x, (float)color.y, (float)color.z, alpha);
      }
   }

   public static void renderBorderBox(PoseStack poseStack, Matrix4f matrix4f, Vec3 angolo1, Vec3 angolo2, Vec3 colore, float alpha, boolean hasTexture) {
      RenderSystem.enableDepthTest();
      Vec3 view = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder buffer = tesselator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      VertexBuffer vertexBuffer = new VertexBuffer(Usage.STATIC);
      float size = 0.1F;
      double y_minore = angolo1.y > angolo2.y ? angolo2.y : angolo1.y;
      double y_maggiore = angolo1.y < angolo2.y ? angolo2.y : angolo1.y;
      double x_minore = angolo1.x > angolo2.x ? angolo2.x : angolo1.x;
      double x_maggiore = angolo1.x < angolo2.x ? angolo2.x : angolo1.x;
      double z_minore = angolo1.z > angolo2.z ? angolo2.z : angolo1.z;
      double z_maggiore = angolo1.z < angolo2.z ? angolo2.z : angolo1.z;
      Vec3 vMinore = new Vec3(x_minore, y_minore, z_minore);
      Vec3 vMaggiore = new Vec3(x_maggiore, y_maggiore, z_maggiore);

      for (int x = 0; x < 2; x++) {
         float y = (float)(x == 0 ? vMinore.y : vMaggiore.y + 1.0);
         addLineWidthToBufferBuilder(
            buffer, new Vec3(vMaggiore.x + 1.0, y, vMinore.z).toVector3f(), (float)(vMaggiore.x - vMinore.x + 1.0), size, Direction.WEST, colore, alpha
         );
         addLineWidthToBufferBuilder(
            buffer, new Vec3(vMinore.x, y, vMinore.z).toVector3f(), (float)(Math.abs(vMaggiore.z - vMinore.z) + 1.0), size, Direction.SOUTH, colore, alpha
         );
         addLineWidthToBufferBuilder(
            buffer,
            new Vec3(vMinore.x, y, vMaggiore.z + 1.0).toVector3f(),
            (float)(Math.abs(vMaggiore.x - vMinore.x) + 1.0),
            size,
            Direction.EAST,
            colore,
            alpha
         );
         addLineWidthToBufferBuilder(
            buffer,
            new Vec3(vMaggiore.x + 1.0, y, vMaggiore.z + 1.0).toVector3f(),
            (float)(Math.abs(vMaggiore.z - vMinore.z) + 1.0),
            size,
            Direction.NORTH,
            colore,
            alpha
         );
      }

      addLineWidthToBufferBuilder(
         buffer,
         new Vec3(vMaggiore.x + 1.0, vMinore.y - 0.05F, vMinore.z).toVector3f(),
         (float)(vMaggiore.y - vMinore.y + 1.1F),
         size,
         Direction.UP,
         colore,
         alpha
      );
      addLineWidthToBufferBuilder(
         buffer, new Vec3(vMinore.x, vMinore.y - 0.05F, vMinore.z).toVector3f(), (float)(vMaggiore.y - vMinore.y + 1.1F), size, Direction.UP, colore, alpha
      );
      addLineWidthToBufferBuilder(
         buffer,
         new Vec3(vMinore.x, vMinore.y - 0.05F, vMaggiore.z + 1.0).toVector3f(),
         (float)(vMaggiore.y - vMinore.y + 1.1F),
         size,
         Direction.UP,
         colore,
         alpha
      );
      addLineWidthToBufferBuilder(
         buffer,
         new Vec3(vMaggiore.x + 1.0, vMinore.y - 0.05F, vMaggiore.z + 1.0).toVector3f(),
         (float)(vMaggiore.y - vMinore.y + 1.1F),
         size,
         Direction.UP,
         colore,
         alpha
      );
      vertexBuffer.bind();
      vertexBuffer.upload(buffer.build());
      poseStack.pushPose();
      Quaternionf rotation = new Quaternionf(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
      poseStack.mulPose(rotation.invert());
      poseStack.translate(-view.x, -view.y, -view.z);
      ShaderInstance shader = GameRenderer.getPositionColorShader();
      vertexBuffer.drawWithShader(poseStack.last().pose(), matrix4f, shader);
      poseStack.popPose();
      VertexBuffer.unbind();
      if (hasTexture) {
         poseStack.pushPose();
         poseStack.translate(-view.x, -view.y, -view.z);
         renderBorderTexturedFaces(poseStack, angolo1, angolo2, colore, alpha);
         poseStack.popPose();
      }
   }

   public static void renderBorderTexturedFaces(PoseStack poseStack, Vec3 angolo1, Vec3 angolo2, Vec3 colore, float alpha) {
      long time = System.currentTimeMillis();
      float period = 5000.0F;
      float scroll = (float)(time % (long)period) / period;
      double xMin = Math.min(angolo1.x, angolo2.x) - 0.001F;
      double xMax = Math.max(angolo1.x, angolo2.x) + 1.0 + 0.001F;
      double yMin = Math.min(angolo1.y, angolo2.y) - 0.001F;
      double yMax = Math.max(angolo1.y, angolo2.y) + 1.0 + 0.001F;
      double zMin = Math.min(angolo1.z, angolo2.z) - 0.001F;
      double zMax = Math.max(angolo1.z, angolo2.z) + 1.0 + 0.001F;
      float width = (float)(xMax - xMin);
      float height = (float)(yMax - yMin);
      float depth = (float)(zMax - zMin);
      int r = (int)(colore.x * 255.0);
      int g = (int)(colore.y * 255.0);
      int b = (int)(colore.z * 255.0);
      int a = (int)(alpha * 200.0F);
      BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
      VertexConsumer vertexConsumer = bufferSource.getBuffer(TEXTURE_BORDER);
      Matrix4f matrix4f = poseStack.last().pose();
      float uMaxN = width;
      float vMaxN = height;
      vertexConsumer.addVertex(matrix4f, (float)xMin, (float)yMin, (float)zMin)
         .setColor(r, g, b, a)
         .setUv(0.0F + scroll, 0.0F - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMax, (float)yMin, (float)zMin)
         .setColor(r, g, b, a)
         .setUv(uMaxN + scroll, 0.0F - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMax, (float)yMax, (float)zMin)
         .setColor(r, g, b, a)
         .setUv(uMaxN + scroll, vMaxN - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMin, (float)yMax, (float)zMin)
         .setColor(r, g, b, a)
         .setUv(0.0F + scroll, vMaxN - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      float uMaxS = width;
      float vMaxS = height;
      vertexConsumer.addVertex(matrix4f, (float)xMax, (float)yMin, (float)zMax)
         .setColor(r, g, b, a)
         .setUv(0.0F - scroll, vMaxS + scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMin, (float)yMin, (float)zMax)
         .setColor(r, g, b, a)
         .setUv(uMaxS - scroll, vMaxS + scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMin, (float)yMax, (float)zMax)
         .setColor(r, g, b, a)
         .setUv(uMaxS - scroll, 0.0F + scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMax, (float)yMax, (float)zMax)
         .setColor(r, g, b, a)
         .setUv(0.0F - scroll, 0.0F + scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      float uMaxW = depth;
      float vMaxW = height;
      vertexConsumer.addVertex(matrix4f, (float)xMin, (float)yMin, (float)zMax)
         .setColor(r, g, b, a)
         .setUv(0.0F + scroll, 0.0F - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMin, (float)yMin, (float)zMin)
         .setColor(r, g, b, a)
         .setUv(uMaxW + scroll, 0.0F - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMin, (float)yMax, (float)zMin)
         .setColor(r, g, b, a)
         .setUv(uMaxW + scroll, vMaxW - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMin, (float)yMax, (float)zMax)
         .setColor(r, g, b, a)
         .setUv(0.0F + scroll, vMaxW - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      float uMaxE = depth;
      float vMaxE = height;
      vertexConsumer.addVertex(matrix4f, (float)xMax, (float)yMin, (float)zMin)
         .setColor(r, g, b, a)
         .setUv(uMaxE + scroll, 0.0F - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMax, (float)yMin, (float)zMax)
         .setColor(r, g, b, a)
         .setUv(0.0F + scroll, 0.0F - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMax, (float)yMax, (float)zMax)
         .setColor(r, g, b, a)
         .setUv(0.0F + scroll, vMaxE - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMax, (float)yMax, (float)zMin)
         .setColor(r, g, b, a)
         .setUv(uMaxE + scroll, vMaxE - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      float uMaxU = width;
      float vMaxU = depth;
      vertexConsumer.addVertex(matrix4f, (float)xMin, (float)yMax, (float)zMin)
         .setColor(r, g, b, a)
         .setUv(0.0F + scroll, 0.0F - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMax, (float)yMax, (float)zMin)
         .setColor(r, g, b, a)
         .setUv(uMaxU + scroll, 0.0F - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMax, (float)yMax, (float)zMax)
         .setColor(r, g, b, a)
         .setUv(uMaxU + scroll, vMaxU - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMin, (float)yMax, (float)zMax)
         .setColor(r, g, b, a)
         .setUv(0.0F + scroll, vMaxU - scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      float uMaxD = width;
      float vMaxD = depth;
      vertexConsumer.addVertex(matrix4f, (float)xMin, (float)yMin, (float)zMax)
         .setColor(r, g, b, a)
         .setUv(uMaxD - scroll, 0.0F + scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMax, (float)yMin, (float)zMax)
         .setColor(r, g, b, a)
         .setUv(0.0F - scroll, 0.0F + scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMax, (float)yMin, (float)zMin)
         .setColor(r, g, b, a)
         .setUv(0.0F - scroll, vMaxD + scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      vertexConsumer.addVertex(matrix4f, (float)xMin, (float)yMin, (float)zMin)
         .setColor(r, g, b, a)
         .setUv(uMaxD - scroll, vMaxD + scroll)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(0.0F, 0.0F, 0.0F);
      bufferSource.endBatch(TEXTURE_BORDER);
   }
}
