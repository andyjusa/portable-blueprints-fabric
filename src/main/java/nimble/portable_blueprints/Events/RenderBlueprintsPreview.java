package nimble.portable_blueprints.Events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent;
import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent.Stage;
import nimble.portable_blueprints.client.BlueprintsPreviewMethods;
import nimble.portable_blueprints.client.VisualizeBlueprintDatas;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.portable_blueprints.ModConfig;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.Operazioni;
import nimble.portable_blueprints.robautile.RenderingGraphics;
import org.joml.Matrix4f;

public class RenderBlueprintsPreview {
   public static boolean prepared = false;
   private static boolean doCleanup = false;
   private static boolean CleanupOnlyBlueprint = false;
   private static Long TimeAnimation = System.currentTimeMillis();
   private static Vec3 originPos = Vec3.ZERO;
   private static Vec3 finalOriginPos = Vec3.ZERO;
   private static long moveTimeAnimation = 0L;
   public static Direction DirezioneHitboxSelected = null;
   private static BlueprintsPreviewMethods.BlueprintPreviewData BPD = null;

   public static void onRenderLevel(RenderLevelStageEvent event) {
      ItemStack item = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND);
      if (doCleanup) {
         if (BPD != null) {
            BPD.cleanupStructureData();
         }

         BPD = null;
         if (!CleanupOnlyBlueprint) {
            TimeAnimation = 0L;
         }

         CleanupOnlyBlueprint = false;
         doCleanup = false;
      }

      if (BPD == null && VisualizeBlueprintDatas.VisualizeBlueprint != null && ModItems.getBlueprintItems().contains(item.getItem())) {
         BPD = new BlueprintsPreviewMethods.BlueprintPreviewData(VisualizeBlueprintDatas.VisualizeBlueprint);
         BPD.prepareData(OldVersionUtilities.getItemStackNBT(item));
      }

      if (BPD != null && BPD.isDatiVertexPronti && ModItems.getBlueprintItems().contains(item.getItem())) {
         CompoundTag nbt = OldVersionUtilities.getItemStackNBT(item);
         if (nbt.contains("visualizeBuild")
            && nbt.getBoolean("visualizeBuild")
            && nbt.contains("originPos")
            && VisualizeBlueprintDatas.originPosVisualizeBlueprint != null) {
            if (TimeAnimation == 0L) {
               TimeAnimation = System.currentTimeMillis() + 2000L;
            }

            Minecraft mc = Minecraft.getInstance();
            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            PoseStack poseStack = event.getPoseStack();
            Vec3 vOriginPos = new Vec3(
               VisualizeBlueprintDatas.originPosVisualizeBlueprint.getX(),
               VisualizeBlueprintDatas.originPosVisualizeBlueprint.getY(),
               VisualizeBlueprintDatas.originPosVisualizeBlueprint.getZ()
            );
            int v = 100000;
            if (!finalOriginPos.equals(vOriginPos)) {
               if (finalOriginPos.distanceTo(Minecraft.getInstance().player.position()) <= (BPD.lunghezza.getX() + BPD.lunghezza.getZ()) * 4) {
                  moveTimeAnimation = System.currentTimeMillis() + v;
               }

               finalOriginPos = vOriginPos;
            }

            float t = 1.0F;
            if (moveTimeAnimation - System.currentTimeMillis() <= v) {
               t = 1.0F - (float)(moveTimeAnimation - System.currentTimeMillis()) / v;
            }

            originPos = Operazioni.lerp(originPos, finalOriginPos, t);
            if (Double.isNaN(originPos.x) || Double.isNaN(originPos.y) || Double.isNaN(originPos.z)) {
               originPos = vOriginPos;
            }

            if (!BPD.lunghezza.equals(Vec3i.ZERO) && event.getStage().equals(Stage.AFTER_PARTICLES)) {
               RenderingGraphics.renderBorderBox(
                  poseStack,
                  event.getProjectionMatrix(),
                  originPos,
                  originPos.add(BPD.lunghezza.getX() - 1, BPD.lunghezza.getY() - 1, BPD.lunghezza.getZ() - 1),
                  ModConfig.getBlueprintAreaColor(),
                  1.0F,
                  false
               );
            }

            float yAdd = 0.0F;
            if (TimeAnimation >= System.currentTimeMillis()) {
               yAdd = -VisualizeBlueprintDatas.VisualizeBlueprint.altezza * ((float)(TimeAnimation - System.currentTimeMillis()) / 2000.0F);
               yAdd *= 2.0F;
            }

            Vec3 posStruttura = new Vec3(originPos.x, originPos.y + yAdd, originPos.z);
            BPD.renderStructure(OldVersionUtilities.getEventPosestack(), cameraPos, event.getProjectionMatrix(), event.getStage(), posStruttura, true);
            if (DirezioneHitboxSelected != null && event.getStage().equals(Stage.AFTER_PARTICLES)) {
               renderingSelectMovingArea(poseStack);
            }
         }
      }
   }

   private static void renderingSelectMovingArea(PoseStack poseStack) {
      Vec3 angolo2 = originPos.add(BPD.lunghezza.getX(), BPD.lunghezza.getY(), BPD.lunghezza.getZ());
      AABB box = null;
      if (DirezioneHitboxSelected.equals(Direction.NORTH)) {
         box = new AABB(originPos, new Vec3(angolo2.x, angolo2.y, originPos.z));
      } else if (DirezioneHitboxSelected.equals(Direction.SOUTH)) {
         box = new AABB(angolo2, new Vec3(originPos.x, originPos.y, angolo2.z));
      } else if (DirezioneHitboxSelected.equals(Direction.WEST)) {
         box = new AABB(originPos, new Vec3(originPos.x, angolo2.y, angolo2.z));
      } else if (DirezioneHitboxSelected.equals(Direction.EAST)) {
         box = new AABB(angolo2, new Vec3(angolo2.x, originPos.y, originPos.z));
      } else if (DirezioneHitboxSelected.equals(Direction.DOWN)) {
         box = new AABB(originPos, new Vec3(angolo2.x, originPos.y, angolo2.z));
      } else if (DirezioneHitboxSelected.equals(Direction.UP)) {
         box = new AABB(angolo2, new Vec3(originPos.x, angolo2.y, originPos.z));
      }

      Vec3 colore = ModConfig.getBlueprintAreaColor();
      float r = (float)colore.x;
      float g = (float)colore.y;
      float b = (float)colore.z;
      float a = 0.5F;
      if (box != null) {
         Minecraft mc = Minecraft.getInstance();
         Vec3 view = mc.gameRenderer.getMainCamera().getPosition();
         Pose pose = poseStack.last();
         Matrix4f matrix = pose.pose();
         float minX = (float)(box.minX - view.x);
         float maxX = (float)(box.maxX - view.x);
         float minZ = (float)(box.minZ - view.z);
         float maxZ = (float)(box.maxZ - view.z);
         float maxY = (float)(box.maxY - view.y);
         float minY = (float)(box.minY - view.y);
         BufferSource bufferSource = mc.renderBuffers().bufferSource();
         VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugSectionQuads());
         if (DirezioneHitboxSelected.equals(Direction.NORTH) || DirezioneHitboxSelected.equals(Direction.SOUTH)) {
            buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
         }

         if (DirezioneHitboxSelected.equals(Direction.EAST) || DirezioneHitboxSelected.equals(Direction.WEST)) {
            buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
         }

         if (DirezioneHitboxSelected.equals(Direction.UP) || DirezioneHitboxSelected.equals(Direction.DOWN)) {
            buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
            buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
         }

         bufferSource.endBatch();
      }
   }

   public static void doCleanUp() {
      doCleanup = true;
   }

   public static void doCleanupOnlyBlueprint() {
      CleanupOnlyBlueprint = true;
      doCleanup = true;
   }

   public static boolean haBlueprintPreviewData() {
      return BPD != null;
   }

   public static Vec3i getLunghezza() {
      return haBlueprintPreviewData() ? BPD.lunghezza : Vec3i.ZERO;
   }
}
