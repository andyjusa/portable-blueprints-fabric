package nimble.portable_blueprints.Events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent;
import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent.Stage;
import nimble.portable_blueprints.portable_blueprints.ModConfig;
import nimble.portable_blueprints.robautile.Operazioni;
import nimble.portable_blueprints.robautile.RenderingGraphics;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RenderScannerAnimation {
   private static Map<UUID, RenderScannerAnimation.AnimazioneScanningDatas> animazioneScanningDatas = new HashMap<>();

   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_PARTICLES) {
         List<UUID> toRemove = new ArrayList<>();

         for (UUID id : animazioneScanningDatas.keySet()) {
            RenderScannerAnimation.AnimazioneScanningDatas animazioneScanningData = animazioneScanningDatas.get(id);
            animazioneScanningData.render(event);
            if (animazioneScanningData.canRemove) {
               toRemove.add(id);
            }
         }

         for (UUID id : toRemove) {
            animazioneScanningDatas.remove(id);
         }
      }
   }

   public static void resetAll() {
      animazioneScanningDatas.clear();
   }

   public static void addAnimation(UUID idAnimazione, BlockPos angolo1, BlockPos angolo2) {
      animazioneScanningDatas.put(idAnimazione, new RenderScannerAnimation.AnimazioneScanningDatas(angolo1, angolo2));
   }

   public static void removeAnimazione(UUID idAnimazione) {
      if (animazioneScanningDatas.containsKey(idAnimazione)) {
         animazioneScanningDatas.get(idAnimazione).stop();
      }
   }

   public static void addProgress(UUID animationID) {
      if (animazioneScanningDatas.containsKey(animationID)) {
         animazioneScanningDatas.get(animationID).addProgresso();
      }
   }

   private static class AnimazioneScanningDatas {
      private static final int TempoOscillazione = 1000;
      private final BlockPos angolo1;
      private final BlockPos angolo2;
      private long startTime;
      private boolean canStartOscillatingScanArea = false;
      private long startOscillatingTime = System.currentTimeMillis();
      private boolean oscillatingDirection = false;
      private final Vec3 minAngolo;
      private final Vec3 maxAngolo;
      private boolean isStopped = false;
      public boolean canRemove = false;
      private boolean usedOnce = false;
      private double altezzaLast = 0.0;
      private int progresso = 0;
      private int maxProgresso;

      public AnimazioneScanningDatas(BlockPos angolo1, BlockPos angolo2) {
         this.angolo1 = angolo1;
         this.angolo2 = angolo2;
         this.startTime = System.currentTimeMillis();
         int minX = Math.min(angolo1.getX(), angolo2.getX());
         int maxX = Math.max(angolo1.getX(), angolo2.getX()) + 1;
         int minZ = Math.min(angolo1.getZ(), angolo2.getZ());
         int maxZ = Math.max(angolo1.getZ(), angolo2.getZ()) + 1;
         int maxY = Math.max(angolo1.getY(), angolo2.getY()) + 1;
         int minY = Math.min(angolo1.getY(), angolo2.getY());
         this.minAngolo = new Vec3(minX, minY, minZ);
         this.maxAngolo = new Vec3(maxX, maxY, maxZ);
         this.maxProgresso = (maxX - minX) * (maxY - minY) * (maxZ - minZ);
         this.altezzaLast = this.minAngolo.y + 0.05F;
      }

      public void addProgresso() {
         this.progresso++;
         if (this.progresso >= this.maxProgresso) {
            this.stop();
         }
      }

      public void render(RenderLevelStageEvent event) {
         Vec3 view = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
         BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
         VertexBuffer vertexBuffer = new VertexBuffer(Usage.STATIC);
         PoseStack poseStack = event.getPoseStack();
         this.renderScanningArea(poseStack, buffer);
         vertexBuffer.bind();
         vertexBuffer.upload(buffer.build());
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         poseStack.pushPose();
         Quaternionf rotation = new Quaternionf(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
         poseStack.mulPose(rotation.invert());
         poseStack.translate(-view.x, -view.y, -view.z);
         ShaderInstance shader = GameRenderer.getPositionColorShader();
         vertexBuffer.drawWithShader(poseStack.last().pose(), event.getProjectionMatrix(), shader);
         poseStack.popPose();
         VertexBuffer.unbind();
         RenderSystem.disableBlend();
      }

      private void renderScanningArea(PoseStack poseStack, BufferBuilder bufferBuilder) {
         float minX = (float)this.minAngolo.x;
         float minY = (float)this.minAngolo.y;
         float minZ = (float)this.minAngolo.z;
         float maxX = (float)this.maxAngolo.x;
         float maxY = (float)this.maxAngolo.y;
         float maxZ = (float)this.maxAngolo.z;
         double alphaV = (float)(System.currentTimeMillis() - this.startTime) / 250.0F;
         if (alphaV > 1.0) {
            alphaV = 1.0;
         } else if (alphaV < 0.0) {
            alphaV = 0.0;
         }

         alphaV = !this.isStopped ? alphaV : 1.0 - alphaV;
         float alphaInside = (float)Operazioni.lerp(0.0, 0.5, alphaV);
         float alphaOutside = (float)Operazioni.lerp(0.0, 1.0, alphaV);
         if (this.isStopped && alphaV <= 0.0) {
            this.canRemove = true;
         }

         double Targetaltezza = this.minAngolo.y + 0.05F;
         if (alphaV >= 1.0 || this.isStopped) {
            Targetaltezza = (float)this.progresso / this.maxProgresso * (this.maxAngolo.y - 0.05F - (this.minAngolo.y + 0.05F)) + this.minAngolo.y;
         }

         float altezza = (float)Operazioni.lerp(this.altezzaLast, Targetaltezza, 0.05F);
         this.altezzaLast = altezza;
         float lunghezzaX = (float)(this.maxAngolo.x - this.minAngolo.x);
         float lunghezzaZ = (float)(this.maxAngolo.z - this.minAngolo.z);
         Vec3 color = ModConfig.getScannerAreaColor();
         bufferBuilder.addVertex(minX, altezza, minZ).setColor((float)color.x, (float)color.y, (float)color.z, alphaInside);
         bufferBuilder.addVertex(maxX, altezza, minZ).setColor((float)color.x, (float)color.y, (float)color.z, alphaInside);
         bufferBuilder.addVertex(maxX, altezza, maxZ).setColor((float)color.x, (float)color.y, (float)color.z, alphaInside);
         bufferBuilder.addVertex(minX, altezza, maxZ).setColor((float)color.x, (float)color.y, (float)color.z, alphaInside);
         bufferBuilder.addVertex(minX, altezza, maxZ).setColor((float)color.x, (float)color.y, (float)color.z, alphaInside);
         bufferBuilder.addVertex(maxX, altezza, maxZ).setColor((float)color.x, (float)color.y, (float)color.z, alphaInside);
         bufferBuilder.addVertex(maxX, altezza, minZ).setColor((float)color.x, (float)color.y, (float)color.z, alphaInside);
         bufferBuilder.addVertex(minX, altezza, minZ).setColor((float)color.x, (float)color.y, (float)color.z, alphaInside);
         float scale = 0.25F;
         float meta = scale / 2.0F;
         RenderingGraphics.addLineWidthToBufferBuilder(
            bufferBuilder,
            new Vec3(minX - meta, altezza, minZ - scale).toVector3f(),
            lunghezzaZ + scale,
            scale,
            Direction.SOUTH,
            color.scale(0.8),
            alphaOutside
         );
         RenderingGraphics.addLineWidthToBufferBuilder(
            bufferBuilder,
            new Vec3(maxX + meta, altezza, maxZ + scale).toVector3f(),
            lunghezzaZ + scale,
            scale,
            Direction.NORTH,
            color.scale(0.8),
            alphaOutside
         );
         RenderingGraphics.addLineWidthToBufferBuilder(
            bufferBuilder, new Vec3(minX - scale, altezza, maxZ + meta).toVector3f(), lunghezzaX + scale, scale, Direction.EAST, color.scale(0.8), alphaOutside
         );
         RenderingGraphics.addLineWidthToBufferBuilder(
            bufferBuilder, new Vec3(maxX + scale, altezza, minZ - meta).toVector3f(), lunghezzaX + scale, scale, Direction.WEST, color.scale(0.8), alphaOutside
         );
         if (alphaV >= 1.0) {
            this.renderParticles(altezza);
         }
      }

      private void renderParticles(double altezza) {
         if (System.currentTimeMillis() % 5L == 0L) {
            Player player = Minecraft.getInstance().player;
            double randomX = this.minAngolo.x + (this.maxAngolo.x - this.minAngolo.x) * Math.random();
            double randomZ = this.minAngolo.z + (this.maxAngolo.z - this.minAngolo.z) * Math.random();
            altezza = altezza + 2.0 * Math.random() - 1.0;
            if (player.blockPosition().distManhattan(new Vec3i((int)randomX, (int)altezza, (int)randomZ)) <= 30) {
               Vec3 color = ModConfig.getScannerAreaColor();
               player.level()
                  .addParticle(
                     new DustParticleOptions(new Vector3f((float)color.x, (float)color.y, (float)color.z), 1.5F), randomX, altezza, randomZ, 0.0, 0.0, 0.0
                  );
            }
         }
      }

      public void stop() {
         if (!this.isStopped) {
            this.isStopped = true;
            this.startTime = System.currentTimeMillis();
         }
      }
   }
}
