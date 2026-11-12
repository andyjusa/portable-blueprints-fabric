package nimble.portable_blueprints.Events;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent;

import nimble.portable_blueprints.fabric.client.MouseScrollingEvent;
import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent.Stage;
import net.minecraft.client.renderer.ItemBlockRenderTypes;

import nimble.portable_blueprints.altro.ExternalBlueprintContainerMetodi;
import nimble.portable_blueprints.client.BlueprintsPreviewMethods;
import nimble.portable_blueprints.client.ClientMethods;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.items.Scanner;
import nimble.portable_blueprints.network.ServerBlueprints.CtoS_ServerBlueprintsData;
import nimble.portable_blueprints.network.ottieni_shared_blueprint_data.CtoS_SharedBlueprintData;
import nimble.portable_blueprints.network.request_specific_data.CtoS_RequestSpecificData;
import nimble.portable_blueprints.network.save_global_blueprint.StoC_SaveGlobalBlueprint;
import nimble.portable_blueprints.portable_blueprints.ModConfig;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.Operazioni;
import nimble.portable_blueprints.robautile.RenderingGraphics;
import nimble.portable_blueprints.robautile.RenderingMethods;
import org.joml.Matrix4f;

public class ModEventClientForge {
   public static List<ModEventClientForge.AnimazionePosizionamentoBlocco> AnimPosBlocks = new ArrayList<>();
   public static List<Tuple<BlockState, Vec3>> datiBlocchi = new ArrayList<>();
   public static BlockPos offsetVisualizeBlueprint = BlockPos.ZERO;
   public static BlockPos ultimaLXAltezzaLZSalvata = BlockPos.ZERO;
   public static Vec3 posPrima = Vec3.ZERO;
   public static Vec3 posNonFinita = Vec3.ZERO;
   public static Vec3 posTarget = Vec3.ZERO;
   public static Long inizioAnim = System.currentTimeMillis();
   public static Direction DirezioneHitboxSelected = null;
   private static boolean hasJoinedWorld = false;
   private static int joinRequestDelay = 0;

   public static void renderLevelStageEvent(RenderLevelStageEvent event) {
      event.getPoseStack().pushPose();
      if (BlueprintsPreviewMethods.BlueprintPreviewData.AllUsedStage.contains(event.getStage())) {
         renderAnimazionePosizionamentoBlocco(event);
      }

      if (event.getStage().equals(Stage.AFTER_PARTICLES)) {
         renderHoloScannerSelectionBox(event);
         ItemStack item = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND);
         if (ModItems.getBlueprintItems().contains(item.getItem())) {
            ExternalBlueprintContainerMetodi.renderBordoAllContainers(item, event);
         }
      } else if (event.getStage().equals(Stage.AFTER_TRANSLUCENT_BLOCKS)) {
         ModEventClientForge.ObstructionBlockManager.render(OldVersionUtilities.getEventPosestack(), event.getProjectionMatrix(), event.getCamera());
      }

      event.getPoseStack().popPose();
   }

   private static void renderAnimazionePosizionamentoBlocco(RenderLevelStageEvent event) {
      if (!AnimPosBlocks.isEmpty()) {
         List<ModEventClientForge.AnimazionePosizionamentoBlocco> daRimuovere = new ArrayList<>();

         for (ModEventClientForge.AnimazionePosizionamentoBlocco apb : AnimPosBlocks) {
            apb.render(OldVersionUtilities.getEventPosestack(), event.getProjectionMatrix(), event.getStage());
            if (apb.animazioneFinita) {
               daRimuovere.add(apb);
            }
         }

         for (ModEventClientForge.AnimazionePosizionamentoBlocco apb : daRimuovere) {
            AnimPosBlocks.remove(apb);
         }

         if (AnimPosBlocks.isEmpty() && !ModEventClientForge.AnimazionePosizionamentoBlocco.CACHE.isEmpty()) {
            ModEventClientForge.AnimazionePosizionamentoBlocco.CACHE.clear();
         }
      }
   }

   public static void renderHoloScannerSelectionBox(RenderLevelStageEvent event) {
      Minecraft mc = Minecraft.getInstance();
      Player player = mc.player;
      if (player != null
         && player.getItemInHand(InteractionHand.MAIN_HAND).is((Item)ModItems.SCANNER.get())
         && OldVersionUtilities.getItemStackNBT(player.getItemInHand(InteractionHand.MAIN_HAND)) != null) {
         ItemStack item = player.getItemInHand(InteractionHand.MAIN_HAND);
         CompoundTag nbt = OldVersionUtilities.getItemStackNBT(item);
         boolean angolo1 = nbt.contains("angolo1") ? nbt.getBoolean("angolo1") : false;
         boolean angolo2 = nbt.contains("angolo2") ? nbt.getBoolean("angolo2") : false;
         boolean angoloFantasma = nbt.contains("angoloFantasma") ? nbt.getBoolean("angoloFantasma") : false;
         Vec3i i1 = null;
         Vec3i i2 = null;
         if (!angolo1 && angoloFantasma) {
            i1 = Operazioni.getVec3iFromString(nbt.getString("coordinateAngoloFantasma"));
            i2 = i1;
         } else if (!angolo2 && angoloFantasma) {
            i1 = Operazioni.getVec3iFromString(nbt.getString("coordinateAngolo1"));
            i2 = Operazioni.getVec3iFromString(nbt.getString("coordinateAngoloFantasma"));
         } else if (angolo1 && angolo2) {
            i1 = Operazioni.getVec3iFromString(nbt.getString("coordinateAngolo1"));
            i2 = Operazioni.getVec3iFromString(nbt.getString("coordinateAngolo2"));
         }

         if (i1 != null && i2 != null) {
            Vec3 Vec3Angolo2 = new Vec3(i2.getX(), i2.getY(), i2.getZ());
            if (angolo1 && angolo2 && System.currentTimeMillis() >= inizioAnim) {
               posNonFinita = null;
               posPrima = Vec3.ZERO;
               posTarget = Vec3.ZERO;
            } else if (angolo1) {
               if (!posTarget.equals(Vec3Angolo2)) {
                  if (posNonFinita == null) {
                     posPrima = new Vec3(i1.getX(), i1.getY(), i1.getZ());
                  } else {
                     posPrima = posNonFinita;
                  }

                  posTarget = Vec3Angolo2;
                  inizioAnim = System.currentTimeMillis() + 300L;
               }

               float t = 1.0F;
               if (inizioAnim - System.currentTimeMillis() <= 300L) {
                  t = 1.0F - (float)(inizioAnim - System.currentTimeMillis()) / 300.0F;
               }

               if (t <= 1.0F) {
                  Vec3Angolo2 = Operazioni.lerp(posPrima, posTarget, t);
                  posNonFinita = Vec3Angolo2;
               }
            } else {
               posNonFinita = null;
            }

            RenderingGraphics.renderBorderBox(
               event.getPoseStack(),
               event.getProjectionMatrix(),
               angolo1 ? new Vec3(i1.getX(), i1.getY(), i1.getZ()) : Vec3Angolo2,
               Vec3Angolo2,
               ModConfig.getScannerAreaColor(),
               1.0F,
               (Boolean)ModConfig.ScanningAreaTextureEnabled.get()
            );
            if (DirezioneHitboxSelected != null) {
               renderScannerShapeZone(event.getPoseStack(), new Vec3(i1.getX(), i1.getY(), i1.getZ()), Vec3Angolo2);
            }
         }
      }
   }

   private static void renderScannerShapeZone(PoseStack poseStack, Vec3 angolo1, Vec3 angolo2) {
      angolo2 = angolo2.add(1.0, 1.0, 1.0);
      AABB box = null;
      if (DirezioneHitboxSelected.equals(Direction.NORTH)) {
         box = new AABB(angolo1, new Vec3(angolo2.x, angolo2.y, angolo1.z));
      } else if (DirezioneHitboxSelected.equals(Direction.SOUTH)) {
         box = new AABB(angolo2, new Vec3(angolo1.x, angolo1.y, angolo2.z));
      } else if (DirezioneHitboxSelected.equals(Direction.WEST)) {
         box = new AABB(angolo1, new Vec3(angolo1.x, angolo2.y, angolo2.z));
      } else if (DirezioneHitboxSelected.equals(Direction.EAST)) {
         box = new AABB(angolo2, new Vec3(angolo2.x, angolo1.y, angolo1.z));
      } else if (DirezioneHitboxSelected.equals(Direction.DOWN)) {
         box = new AABB(angolo1, new Vec3(angolo2.x, angolo1.y, angolo2.z));
      } else if (DirezioneHitboxSelected.equals(Direction.UP)) {
         box = new AABB(angolo2, new Vec3(angolo1.x, angolo2.y, angolo1.z));
      }

      Vec3 colore = ModConfig.getScannerAreaColor();
      float r = (float)colore.x;
      float g = (float)colore.y;
      float b = (float)colore.z;
      float a = 0.8F;
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

   public static void playerEsceDalMondo() {
      hasJoinedWorld = false;
      joinRequestDelay = 0;
      BlueprintsPreviewMethods.cancelPendingIcons();
      ClientMethods.Reset();
      offsetVisualizeBlueprint = BlockPos.ZERO;
      datiBlocchi.clear();
      StoC_SaveGlobalBlueprint.StaGiaGestendoUnaBlueprint = false;
      RenderBlueprintsPreview.doCleanUp();
      RenderScannerAnimation.resetAll();
   }

   public static void playerEntraNelMondo() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && !hasJoinedWorld) {
         hasJoinedWorld = true;
         joinRequestDelay = 20;
      }
      if (mc.level != null && mc.player != null && joinRequestDelay > 0 && --joinRequestDelay == 0) {
         CtoS_RequestSpecificData.send(510, CtoS_RequestSpecificData.TipoRichiesta.ALLOW_GLOBAL_BLUEPRINTS);
         CtoS_SharedBlueprintData.send();
         CtoS_ServerBlueprintsData.send();
      }
   }

   public static void ScrollingMouseEvent(MouseScrollingEvent event) {
      if (Minecraft.getInstance().player != null) {
         BlueprintEvent.ScrollingMouseEvent(event);
         ChangeShapeScanningAreaEvent.ScrollingMouseEvent(event);
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.screen == null) {
            ItemStack item = mc.player.getMainHandItem().copy();
            boolean scrollAvanti = event.getScrollDeltaY() > 0.0;
            if (item.is((Item)ModItems.SCANNER.get())) {
               boolean isCtrlDown = InputConstants.isKeyDown(mc.getWindow().getWindow(), 341) || InputConstants.isKeyDown(mc.getWindow().getWindow(), 345);
               if (!isCtrlDown) {
                  return;
               }

               int valore = Scanner.ScrollingDistanceAttuale + (scrollAvanti ? 1 : -1);
               if (valore > 100) {
                  valore = 100;
               } else if (valore < 5) {
                  valore = 5;
               }

               Scanner.ScrollingDistanceAttuale = valore;
               event.setCanceled(true);
            }
         }
      }
   }

   public static class AnimazionePosizionamentoBlocco {
      public Vec3 posIniziale;
      public Vec3 posObbiettivo;
      public Item item;
      public Long EndTime;
      public boolean animazioneFinita = false;
      public Block block;
      private List<ModEventClientForge.AnimazionePosizionamentoBlocco.LayerVBO> datiBlocco = null;
      private static final Map<BlockState, List<ModEventClientForge.AnimazionePosizionamentoBlocco.LayerVBO>> CACHE = new HashMap<>();

      public AnimazionePosizionamentoBlocco(String blockData, Long EndTime, Vec3 posIniziale, Vec3 posObbiettivo) {
         this.EndTime = EndTime;
         this.posIniziale = posIniziale;
         this.posObbiettivo = posObbiettivo;
         this.block = Operazioni.getBlockFromString(blockData);
         Item it = Operazioni.getCorrectItemStackFromId(this.block).getItem();
         if (!it.equals(Items.AIR)) {
            this.item = it;
         }
      }

      public void render(PoseStack poseStack, Matrix4f projectionMatrix, Stage stageAttuale) {
         if (this.block != null && this.datiBlocco == null) {
            this.datiBlocco = getOrCreate(
               this.block.defaultBlockState(), new BlockPos((int)this.posObbiettivo.x, (int)this.posObbiettivo.y, (int)this.posObbiettivo.z)
            );
         }

         float time = 1.0F - (float)(this.EndTime - System.currentTimeMillis()) / 1000.0F;
         if (time > 1.0F) {
            time = 1.0F;
         }

         if (time < 0.0F) {
            time = 0.0F;
         }

         if (this.datiBlocco != null) {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vec3 cam = camera.getPosition();
            Vec3 pos = Operazioni.lerp(this.posIniziale, this.posObbiettivo, time).add(-0.5, -0.5, -0.5);
            poseStack.pushPose();
            poseStack.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
            List<ModEventClientForge.AnimazionePosizionamentoBlocco.LayerVBO> layers = getOrCreate(
               this.block.defaultBlockState(), new BlockPos((int)pos.x, (int)pos.y, (int)pos.z)
            );
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            for (ModEventClientForge.AnimazionePosizionamentoBlocco.LayerVBO layer : layers) {
               if (layer.stage.equals(stageAttuale)) {
                  RenderType rt = layer.layer();
                  rt.setupRenderState();
                  layer.vbo().bind();
                  layer.vbo().drawWithShader(poseStack.last().pose(), projectionMatrix, Objects.requireNonNull(RenderSystem.getShader()));
                  VertexBuffer.unbind();
                  rt.clearRenderState();
               }
            }

            RenderSystem.disableBlend();
            poseStack.popPose();
         } else if (this.item != null) {
            RenderingMethods.RenderItemInWorld(poseStack, Operazioni.lerp(this.posIniziale, this.posObbiettivo, time), this.item, time < 0.25F ? 0.25F : time);
         }

         if (System.currentTimeMillis() > this.EndTime) {
            this.animazioneFinita = true;
         }
      }

      public static List<ModEventClientForge.AnimazionePosizionamentoBlocco.LayerVBO> getOrCreate(BlockState state, BlockPos pos) {
         if (CACHE.containsKey(state)) {
            return CACHE.get(state);
         }

         BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
         BakedModel model = dispatcher.getBlockModel(state);
         List<ModEventClientForge.AnimazionePosizionamentoBlocco.LayerVBO> list = new ArrayList<>();
         ClientLevel level = Minecraft.getInstance().level;
         BlockColors blockColors = Minecraft.getInstance().getBlockColors();
         int color = blockColors.getColor(state, level, pos, 0);
         float r = (color >> 16 & 0xFF) / 255.0F;
         float g = (color >> 8 & 0xFF) / 255.0F;
         float b = (color & 0xFF) / 255.0F;
         RandomSource random = RandomSource.create();
         if (pos != null) {
            random.setSeed(pos.asLong());
         }

         for (RenderType layer : java.util.List.of(ItemBlockRenderTypes.getChunkRenderType(state))) {
            ByteBufferBuilder byteBuffer = new ByteBufferBuilder(2048);
            BufferBuilder builder = new BufferBuilder(byteBuffer, layer.mode(), layer.format());
            PoseStack ps = new PoseStack();
            dispatcher.getModelRenderer().renderModel(ps.last(), builder, state, model, r, g, b, 15728880, OverlayTexture.NO_OVERLAY);
            MeshData rendered = builder.build();
            if (rendered != null) {
               VertexBuffer vbo = new VertexBuffer(Usage.STATIC);
               vbo.bind();
               vbo.upload(rendered);
               VertexBuffer.unbind();
               list.add(new ModEventClientForge.AnimazionePosizionamentoBlocco.LayerVBO(layer, vbo, getStageForLayer(layer)));
            } else {
               System.err.println("Failed to build mesh for layer: " + layer);
            }
         }

         CACHE.put(state, list);
         return list;
      }

      public static Stage getStageForLayer(RenderType type) {
         if (type == RenderType.solid()) {
            return Stage.AFTER_SOLID_BLOCKS;
         } else if (type == RenderType.cutout() || type == RenderType.cutoutMipped()) {
            return Stage.AFTER_CUTOUT_BLOCKS;
         } else {
            return type == RenderType.translucent() ? Stage.AFTER_TRANSLUCENT_BLOCKS : Stage.AFTER_TRANSLUCENT_BLOCKS;
         }
      }

      private record LayerVBO(RenderType layer, VertexBuffer vbo, Stage stage) {
      }
   }

   public static class ObstructionBlockManager {
      public static List<Tuple<Long, BlockPos>> Blocks = new ArrayList<>();
      private static VertexBuffer dynamicMesh = null;
      private static Long updateAfter = System.currentTimeMillis();
      private static boolean updated = true;

      public static void aggiungiBlocco(BlockPos pos) {
         boolean trovato = false;
         Tuple<Long, BlockPos> toRemove = null;

         for (Tuple<Long, BlockPos> data : Blocks) {
            if (((BlockPos)data.getB()).equals(pos)) {
               trovato = true;
               toRemove = data;
               break;
            }
         }

         if (!trovato) {
            Blocks.add(new Tuple(System.currentTimeMillis() + 60000L, pos));
            updateAfter = System.currentTimeMillis() + 100L;
            updated = false;
         } else {
            Blocks.remove(toRemove);
            Blocks.add(new Tuple(System.currentTimeMillis() + 60000L, pos));
         }
      }

      private static void updateMesh() {
         if (dynamicMesh != null) {
            dynamicMesh.close();
            dynamicMesh = null;
         }

         if (Blocks.size() != 0) {
            BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            for (Tuple<Long, BlockPos> pos : Blocks) {
               addCube(
                  builder,
                  ((BlockPos)pos.getB()).getX() - 0.0025F,
                  ((BlockPos)pos.getB()).getY() - 0.0025F,
                  ((BlockPos)pos.getB()).getZ() - 0.0025F,
                  1.005F,
                  255,
                  0,
                  0,
                  80
               );
            }

            MeshData meshData = builder.build();
            if (meshData != null) {
               dynamicMesh = new VertexBuffer(Usage.DYNAMIC);
               dynamicMesh.bind();
               dynamicMesh.upload(meshData);
            }

            VertexBuffer.unbind();
         }
      }

      private static void addCube(BufferBuilder builder, float x, float y, float z, float size, int r, int g, int b, int a) {
         float x1 = x;
         float y1 = y;
         float z1 = z;
         float x2 = x + size;
         float y2 = y + size;
         float z2 = z + size;
         builder.addVertex(x1, y1, z2).setColor(r, g, b, a);
         builder.addVertex(x2, y1, z2).setColor(r, g, b, a);
         builder.addVertex(x2, y2, z2).setColor(r, g, b, a);
         builder.addVertex(x1, y2, z2).setColor(r, g, b, a);
         builder.addVertex(x2, y1, z1).setColor(r, g, b, a);
         builder.addVertex(x1, y1, z1).setColor(r, g, b, a);
         builder.addVertex(x1, y2, z1).setColor(r, g, b, a);
         builder.addVertex(x2, y2, z1).setColor(r, g, b, a);
         builder.addVertex(x1, y1, z1).setColor(r, g, b, a);
         builder.addVertex(x1, y1, z2).setColor(r, g, b, a);
         builder.addVertex(x1, y2, z2).setColor(r, g, b, a);
         builder.addVertex(x1, y2, z1).setColor(r, g, b, a);
         builder.addVertex(x2, y1, z2).setColor(r, g, b, a);
         builder.addVertex(x2, y1, z1).setColor(r, g, b, a);
         builder.addVertex(x2, y2, z1).setColor(r, g, b, a);
         builder.addVertex(x2, y2, z2).setColor(r, g, b, a);
         builder.addVertex(x1, y2, z2).setColor(r, g, b, a);
         builder.addVertex(x2, y2, z2).setColor(r, g, b, a);
         builder.addVertex(x2, y2, z1).setColor(r, g, b, a);
         builder.addVertex(x1, y2, z1).setColor(r, g, b, a);
         builder.addVertex(x1, y1, z1).setColor(r, g, b, a);
         builder.addVertex(x2, y1, z1).setColor(r, g, b, a);
         builder.addVertex(x2, y1, z2).setColor(r, g, b, a);
         builder.addVertex(x1, y1, z2).setColor(r, g, b, a);
      }

      public static void reset() {
         if (dynamicMesh != null) {
            dynamicMesh.close();
         }

         dynamicMesh = null;
         Blocks.clear();
         updateAfter = System.currentTimeMillis() + 100L;
         updated = false;
      }

      public static void render(PoseStack poseStack, Matrix4f projectMatrix4f, Camera camera) {
         if (dynamicMesh != null) {
            poseStack.pushPose();
            poseStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            dynamicMesh.bind();
            dynamicMesh.drawWithShader(poseStack.last().pose(), projectMatrix4f, RenderSystem.getShader());
            VertexBuffer.unbind();
            RenderSystem.enableDepthTest();
            poseStack.popPose();
            List<Tuple<Long, BlockPos>> toRemove = new ArrayList<>();

            for (Tuple<Long, BlockPos> pos : Blocks) {
               if (System.currentTimeMillis() > (Long)pos.getA()
                  || Minecraft.getInstance().player.level().getBlockState((BlockPos)pos.getB()).getBlock().equals(net.minecraft.world.level.block.Blocks.AIR)) {
                  toRemove.add(pos);
               }
            }

            for (Tuple<Long, BlockPos> pos : toRemove) {
               Blocks.remove(pos);
            }

            if (toRemove.size() > 0) {
               updateAfter = System.currentTimeMillis() + 100L;
               updated = false;
            }
         }

         if (!updated && System.currentTimeMillis() > updateAfter) {
            updated = true;
            updateMesh();
         }
      }
   }
}
