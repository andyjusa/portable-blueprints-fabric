package nimble.portable_blueprints.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent;

import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent.Stage;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import nimble.portable_blueprints.Events.BlueprintEvent;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.Operazioni;
import org.joml.Matrix4f;

public class BlueprintsPreviewMethods {
   public static List<BlueprintsPreviewMethods.BlueprintPreviewData> ConvertStructureToTextureDataInput = new ArrayList<>();
   public static boolean hideAllHud = false;
   private static boolean waitForNextIconGeneration = false;
   private static boolean inRenderingForIcon = false;
   public static Map<String, ResourceLocation> MapIconsRL = new HashMap<>();

   public static void cancelPendingIcons() {
      for (BlueprintPreviewData preview : ConvertStructureToTextureDataInput) preview.inCreazioneIcon = false;
      ConvertStructureToTextureDataInput.clear();
      hideAllHud = false;
      inRenderingForIcon = false;
      waitForNextIconGeneration = false;
   }

   /** Capture on the render thread after the preview's last world pass. */
   public static void capturePendingIcon() {
      if (!inRenderingForIcon || ConvertStructureToTextureDataInput.isEmpty()) return;
      BlueprintPreviewData preview = ConvertStructureToTextureDataInput.get(0);
      try {
         try (NativeImage screenshot = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget());
              NativeImage cropped = cropCentered(screenshot, 0.65F)) {
            NativeImage img = scale(cropped, 256, 256);
            String name = "cache/tablet_icons/" + preview.pendingIconId;
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("portable_blueprints", name);
            Minecraft.getInstance().getTextureManager().release(rl);
            preview.iconTexture = Minecraft.getInstance().getTextureManager().register(name, new DynamicTexture(img));
            MapIconsRL.put(preview.pendingIconId, preview.iconTexture);
         }
      } finally {
         hideAllHud = false;
         inRenderingForIcon = false;
         preview.inCreazioneIcon = false;
         ConvertStructureToTextureDataInput.remove(preview);
         waitForNextIconGeneration = false;
      }
   }


   public static boolean isBlockEntity(BlockState state) {
      if (state.getBlock() instanceof EntityBlock entityBlock) {
         try {
            BlockEntity be = entityBlock.newBlockEntity(BlockPos.ZERO, state);
            if (be == null) {
               return false;
            }

            BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
            return renderer != null;
         } catch (Exception e) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isLiquidBlock(BlockState state) {
      return state.getBlock() instanceof LiquidBlock ? !state.getFluidState().isEmpty() : false;
   }

   public static NativeImage cropCentered(NativeImage original, float percent) {
      int width = original.getWidth();
      int height = original.getHeight();
      int cropSize = (int)(Math.min(width, height) * percent);
      int startX = width / 2 - cropSize / 2;
      int startY = height / 2 - cropSize / 2;
      NativeImage cropped = new NativeImage(cropSize, cropSize, false);

      for (int x = 0; x < cropSize; x++) {
         for (int y = 0; y < cropSize; y++) {
            int pixel = original.getPixelRGBA(startX + x, startY + y);
            cropped.setPixelRGBA(x, y, pixel);
         }
      }

      return cropped;
   }

   public static NativeImage scale(NativeImage original, int targetWidth, int targetHeight) {
      NativeImage scaled = new NativeImage(targetWidth, targetHeight, false);
      int originalWidth = original.getWidth();
      int originalHeight = original.getHeight();

      for (int x = 0; x < targetWidth; x++) {
         for (int y = 0; y < targetHeight; y++) {
            int srcX = x * originalWidth / targetWidth;
            int srcY = y * originalHeight / targetHeight;
            int pixel = original.getPixelRGBA(srcX, srcY);
            scaled.setPixelRGBA(x, y, pixel);
         }
      }

      return scaled;
   }

   public static void renderStage(RenderLevelStageEvent event) {
      if (hideAllHud && !ConvertStructureToTextureDataInput.isEmpty()) {
         BlueprintsPreviewMethods.BlueprintPreviewData bpd = ConvertStructureToTextureDataInput.get(0);
         PoseStack poseStack = event.getPoseStack();
         Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
         float yaw = camera.getYRot();
         float pitch = camera.getXRot();
         if (BlueprintsPreviewMethods.BlueprintPreviewData.AllUsedStage.contains(event.getStage())) {
            float r = 0.18431373F;
            float g = 0.21176471F;
            float b = 0.2509804F;
            Vec3 _lookVec = Minecraft.getInstance().player.getLookAngle();
            poseStack.pushPose();
            poseStack.translate(_lookVec.x, _lookVec.y, _lookVec.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
            Tesselator tess = Tesselator.getInstance();
            BufferBuilder buf = tess.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            float size = 3.0F;
            buf.addVertex(poseStack.last().pose(), -size, -size, 0.0F).setColor(r, g, b, 1.0F);
            buf.addVertex(poseStack.last().pose(), size, -size, 0.0F).setColor(r, g, b, 1.0F);
            buf.addVertex(poseStack.last().pose(), size, size, 0.0F).setColor(r, g, b, 1.0F);
            buf.addVertex(poseStack.last().pose(), -size, size, 0.0F).setColor(r, g, b, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.enablePolygonOffset();
            RenderSystem.polygonOffset(-1.0F, -1900000.0F);
            BufferUploader.drawWithShader(buf.build());
            RenderSystem.disablePolygonOffset();
            poseStack.popPose();
         }

         Vec3 lookVec = Minecraft.getInstance().player.getLookAngle();
         PoseStack poseStack2 = OldVersionUtilities.getEventPosestack();
         poseStack2.pushPose();
         poseStack2.mulPose(Axis.YP.rotationDegrees(-yaw));
         poseStack2.mulPose(Axis.XP.rotationDegrees(pitch));
         poseStack2.translate(
            -0.5F * (bpd.visualizeBlueprintDatas.lunghezzaX / 20.0F),
            -23.5F * (bpd.visualizeBlueprintDatas.altezza / 46.0F),
            Math.max(15.0F, 50.0F * (bpd.visualizeBlueprintDatas.altezza / 46.0F))
         );
         poseStack2.mulPose(Axis.XP.rotationDegrees(-22.5F));
         poseStack2.mulPose(Axis.YP.rotationDegrees(-45.0F));
         poseStack2.pushPose();
         RenderSystem.enablePolygonOffset();
         RenderSystem.polygonOffset(-1.0F, -2900000.0F);
         bpd.renderStructureCustomPosestack(poseStack2, event.getProjectionMatrix(), event.getStage(), Vec3.ZERO, true);
         RenderSystem.disablePolygonOffset();
         poseStack2.popPose();
         if (event.getStage().equals(Stage.AFTER_PARTICLES) && !inRenderingForIcon) {
            inRenderingForIcon = true;
         }
      }
   }

   public static class BlueprintPreviewData {
      public static final List<Stage> AllUsedStage = List.of(Stage.AFTER_CUTOUT_BLOCKS, Stage.AFTER_SOLID_BLOCKS, Stage.AFTER_TRANSLUCENT_BLOCKS);
      private static final Map<Stage, List<RenderType>> STAGE_RENDER_TYPES = Map.of(
         Stage.AFTER_CUTOUT_BLOCKS,
         List.of(RenderType.solid(), RenderType.cutout(), RenderType.cutoutMipped()),
         Stage.AFTER_TRANSLUCENT_BLOCKS,
         List.of(RenderType.translucent())
      );
      private static final int MAX_BLOCKS_FOR_Time = 1000;
      private ScheduledExecutorService executor = null;
      private boolean datiVertexPronti = false;
      private final Map<RenderType, List<Tuple<BlockPos, BlockState>>> datiBlocchiRender = new HashMap<>();
      private final List<Tuple<BlockPos, BlockState>> datiRenderLiquid = new ArrayList<>();
      public Vec3i lunghezza = Vec3i.ZERO;
      public boolean isDatiVertexPronti = false;
      private final Map<RenderType, List<VertexBuffer>> vertexBuffers = new HashMap<>();
      private final List<Tuple<BlockPos, VertexBuffer>> vertexBuffersLiquids = new ArrayList<>();
      private final List<Tuple<BlockPos, BlockState>> ListaBlocchiSingleRender = new ArrayList<>();
      private final Blueprint.BlueprintDatas visualizeBlueprintDatas;
      public ResourceLocation iconTexture;
      private boolean inCreazioneIcon = false;

      public BlueprintPreviewData(Blueprint.BlueprintDatas visualizeBlueprintDatas) {
         this.visualizeBlueprintDatas = visualizeBlueprintDatas;
      }

      public void cleanupStructureData() {
         for (RenderType renderType : this.vertexBuffers.keySet()) {
            for (VertexBuffer vertex : this.vertexBuffers.get(renderType)) {
               vertex.close();
            }
         }

         this.vertexBuffers.clear();
         this.vertexBuffersLiquids.clear();
         this.ListaBlocchiSingleRender.clear();
         this.lunghezza = Vec3i.ZERO;
         if (this.executor != null && !this.executor.isShutdown()) {
            this.executor.shutdownNow();
            this.executor = null;
         }

         this.datiVertexPronti = false;
         this.isDatiVertexPronti = false;
      }

      public void prepareData(CompoundTag nbt_blueprint_item) {
         if (Minecraft.getInstance().level != null) {
            System.out.println("p1");
            this.cleanupStructureData();
            if (this.executor != null) {
               this.executor.shutdownNow();
            }

            this.executor = Executors.newSingleThreadScheduledExecutor();
            this.executor
               .schedule(
                  () -> {
                     try {
                        PoseStack poseStack = new PoseStack();
                        poseStack.pushPose();
                        System.out.println("p2");

                        try {
                           Blueprint.BlueprintDatas schem = this.visualizeBlueprintDatas;
                           Minecraft mc = Minecraft.getInstance();
                           BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
                           this.datiBlocchiRender.clear();
                           this.datiRenderLiquid.clear();
                           this.ListaBlocchiSingleRender.clear();
                           if (nbt_blueprint_item != null) {
                              if (nbt_blueprint_item.getDouble("rotateValue") != 0.0) {
                                 schem = schem.rotate360(nbt_blueprint_item.getDouble("rotateValue"), nbt_blueprint_item.getShort("autofillmode"), null);
                              }

                              if (nbt_blueprint_item.getBoolean("mirrorX")
                                 || nbt_blueprint_item.getBoolean("mirrorY")
                                 || nbt_blueprint_item.getBoolean("mirrorZ")) {
                                 schem = schem.Mirror(
                                    nbt_blueprint_item.getBoolean("mirrorX"),
                                    nbt_blueprint_item.getBoolean("mirrorY"),
                                    nbt_blueprint_item.getBoolean("mirrorZ")
                                 );
                              }
                           }

                           this.lunghezza = new Vec3i(schem.lunghezzaX, schem.altezza, schem.lunghezzaZ);
                           int n_volte = 0;
                           System.out.println("p3");

                           for (int v = 0; v < 2; v++) {
                              for (int altezze : v == 0 ? schem.BlocksData.keySet() : schem.NotSolidBlocksData.keySet()) {
                                 for (Entry<String, String> bloccoDati : v == 0
                                    ? schem.BlocksData.get(altezze).entrySet()
                                    : schem.NotSolidBlocksData.get(altezze).entrySet()) {
                                    BlockPos offsetPos = new BlockPos(Operazioni.getVec3iFromString(bloccoDati.getKey()));
                                    String blockstate = bloccoDati.getValue();
                                    String bloccoId = blockstate.substring(blockstate.indexOf("<bid>") + 5, blockstate.indexOf("</bid>"));
                                    blockstate = blockstate.replaceFirst("<bid>" + bloccoId + "</bid>", "");
                                    BlockState bs = blockstate.equalsIgnoreCase("null")
                                       ? Operazioni.getBlockFromString(bloccoId).defaultBlockState()
                                       : Operazioni.getPropertiesStringToBlock(Operazioni.getBlockFromString(bloccoId), blockstate);
                                    if (!bs.hasProperty(BlockStateProperties.BED_PART)
                                       || !((BedPart)bs.getValue(BlockStateProperties.BED_PART)).equals(BedPart.HEAD)) {
                                       BakedModel model = dispatcher.getBlockModel(bs);
                                       if (BlueprintsPreviewMethods.isLiquidBlock(bs)) {
                                          this.datiRenderLiquid.add(new Tuple(offsetPos, bs));
                                       } else {
                                          LegacyRandomSource threadRandom = new LegacyRandomSource(System.currentTimeMillis());

                                          for (RenderType renderType : List.of(ItemBlockRenderTypes.getChunkRenderType(bs))) {
                                             if (!this.datiBlocchiRender.containsKey(renderType)) {
                                                this.datiBlocchiRender.put(renderType, new ArrayList<>());
                                             }

                                             this.datiBlocchiRender.get(renderType).add(new Tuple(offsetPos, bs));
                                          }
                                       }

                                       if (++n_volte >= 1000) {
                                          Thread.sleep(1L);
                                          n_volte = 0;
                                       }
                                    }
                                 }
                              }
                           }

                           System.out.println("p4");
                           this.datiVertexPronti = true;
                        } finally {
                           poseStack.popPose();
                        }
                     } catch (InterruptedException var24) {
                     }

                     this.createVertexBuffers();
                  },
                  0L,
                  TimeUnit.MILLISECONDS
               );
         }
      }

      public void createVertexBuffers() {
         if (Minecraft.getInstance().level != null && this.datiVertexPronti && !this.isDatiVertexPronti) {
            System.out.println("p5");
            Minecraft mc = Minecraft.getInstance();
            PoseStack poseStack = new PoseStack();
            BlockColors blockColors = Minecraft.getInstance().getBlockColors();
            BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

            try {
               for (RenderType tipiRender : this.datiBlocchiRender.keySet()) {
                  int expectedVertices = this.datiBlocchiRender.size() * 24;
                  int initialBufferSize = Math.max(256, expectedVertices * 32);
                  ByteBufferBuilder byteBuffer = new ByteBufferBuilder(initialBufferSize);
                  BufferBuilder bufferBuilder = new BufferBuilder(byteBuffer, Mode.QUADS, DefaultVertexFormat.BLOCK);

                  for (Tuple<BlockPos, BlockState> datiBlocco : this.datiBlocchiRender.get(tipiRender)) {
                     if (BlueprintsPreviewMethods.isBlockEntity((BlockState)datiBlocco.getB())) {
                        if (!this.ListaBlocchiSingleRender.contains(datiBlocco)) {
                           this.ListaBlocchiSingleRender.add(datiBlocco);
                        }
                     } else {
                        poseStack.pushPose();
                        poseStack.translate(((BlockPos)datiBlocco.getA()).getX(), ((BlockPos)datiBlocco.getA()).getY(), ((BlockPos)datiBlocco.getA()).getZ());
                        int color = blockColors.getColor((BlockState)datiBlocco.getB(), mc.level, (BlockPos)datiBlocco.getA(), 0);
                        float r = (color >> 16 & 0xFF) / 255.0F;
                        float g = (color >> 8 & 0xFF) / 255.0F;
                        float b = (color & 0xFF) / 255.0F;
                        BakedModel model = dispatcher.getBlockModel((BlockState)datiBlocco.getB());
                        dispatcher.getModelRenderer()
                           .renderModel(
                              poseStack.last(),
                              bufferBuilder,
                              (BlockState)datiBlocco.getB(),
                              model,
                              r,
                              g,
                              b,
                              15728880,
                              OverlayTexture.NO_OVERLAY
                           );
                        poseStack.popPose();
                     }
                  }

                  MeshData rendered = bufferBuilder.build();
                  Minecraft.getInstance().execute(() -> {
                     VertexBuffer vertexBuffer = new VertexBuffer(Usage.STATIC);
                     vertexBuffer.bind();
                     vertexBuffer.upload(rendered);
                     if (!this.vertexBuffers.containsKey(tipiRender)) {
                        this.vertexBuffers.put(tipiRender, new ArrayList<>());
                     }

                     this.vertexBuffers.get(tipiRender).add(vertexBuffer);
                     VertexBuffer.unbind();
                  });
               }

               System.out.println("p6");

               for (Tuple<BlockPos, BlockState> datiBlocco : this.datiRenderLiquid) {
                  int expectedVertices = this.datiRenderLiquid.size() * 24;
                  int initialBufferSize = Math.max(256, expectedVertices * 32);
                  ByteBufferBuilder byteBuffer = new ByteBufferBuilder(initialBufferSize);
                  BufferBuilder bufferBuilder = new BufferBuilder(byteBuffer, Mode.QUADS, DefaultVertexFormat.BLOCK);
                  BlockPos pos = new BlockPos(
                     ((BlockPos)datiBlocco.getA()).getX() & 15, ((BlockPos)datiBlocco.getA()).getY(), ((BlockPos)datiBlocco.getA()).getZ() & 15
                  );
                  dispatcher.renderLiquid(pos, mc.level, bufferBuilder, (BlockState)datiBlocco.getB(), ((BlockState)datiBlocco.getB()).getFluidState());
                  MeshData rendered = bufferBuilder.build();
                  Minecraft.getInstance().execute(() -> {
                     VertexBuffer vertexBuffer = new VertexBuffer(Usage.STATIC);
                     vertexBuffer.bind();
                     vertexBuffer.upload(rendered);
                     this.vertexBuffersLiquids.add(new Tuple((BlockPos)datiBlocco.getA(), vertexBuffer));
                     VertexBuffer.unbind();
                  });
               }

               System.out.println("p7");
               this.isDatiVertexPronti = true;
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }

      private static void drawPreviewBuffer(VertexBuffer buffer, PoseStack poseStack, Matrix4f projection, boolean needsLightmap) {
         int previousLightmap = RenderSystem.getShaderTexture(2);
         try {
            // Vanilla disables Sampler2 between world passes. The moving-block shader
            // needs a live lightmap, and avoids the terrain shader's ChunkOffset uniform.
            if (needsLightmap) Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            buffer.drawWithShader(poseStack.last().pose(), projection, Objects.requireNonNull(RenderSystem.getShader()));
         } finally {
            if (needsLightmap) RenderSystem.setShaderTexture(2, previousLightmap);
         }
      }

      public void renderStructureCustomPosestack(PoseStack poseStack, Matrix4f matrix4f, Stage stage, Vec3 originPos, boolean renderDeep) {
         if (renderDeep) {
            RenderSystem.enableDepthTest();
         } else {
            RenderSystem.disableDepthTest();
         }

         for (RenderType renderType : this.vertexBuffers.keySet()) {
            if (STAGE_RENDER_TYPES.containsKey(stage) && STAGE_RENDER_TYPES.get(stage).contains(renderType)) {
               for (VertexBuffer vertexBuffer : this.vertexBuffers.get(renderType)) {
                  vertexBuffer.bind();
                  if (renderType.equals(RenderType.solid()) || renderType.equals(RenderType.cutout()) || renderType.equals(RenderType.cutoutMipped())) {
                     RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
                  } else if (renderType.equals(RenderType.translucent()) || renderType.equals(RenderType.translucentMovingBlock())) {
                     RenderSystem.setShader(GameRenderer::getRendertypeTranslucentMovingBlockShader);
                  } else if (renderType.equals(RenderType.tripwire())) {
                     RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                  }

                  RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);

                  try {
                     RenderSystem.enableBlend();
                     RenderSystem.defaultBlendFunc();
                     RenderSystem.depthMask(true);
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, BlueprintEvent.HologramTrasparency);
                     drawPreviewBuffer(vertexBuffer, poseStack, matrix4f, renderType == RenderType.translucent() || renderType == RenderType.translucentMovingBlock());
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                     RenderSystem.disableBlend();
                  } catch (Exception e) {
                     e.printStackTrace();
                  }

                  RenderSystem.depthMask(true);
                  RenderSystem.disableBlend();
               }

               VertexBuffer.unbind();
            }
         }

         Frustum frustum = RenderLevelStageEvent.getActiveFrustum();
         if (stage.equals(Stage.AFTER_TRANSLUCENT_BLOCKS)) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getRendertypeTranslucentMovingBlockShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);

            for (Tuple<BlockPos, VertexBuffer> datiBlocco : this.vertexBuffersLiquids) {
               ((VertexBuffer)datiBlocco.getB()).bind();
               Vec3 blockPos = new Vec3(((BlockPos)datiBlocco.getA()).getX(), ((BlockPos)datiBlocco.getA()).getY(), ((BlockPos)datiBlocco.getA()).getZ())
                  .add(originPos);
               AABB blockAABB = new AABB(blockPos, blockPos.add(1.0, 1.0, 1.0));
               if (frustum == null || frustum.isVisible(blockAABB)) {
                  try {
                     poseStack.pushPose();
                     BlockPos pos = new BlockPos(
                        ((BlockPos)datiBlocco.getA()).getX() & 15, ((BlockPos)datiBlocco.getA()).getY() & 15, ((BlockPos)datiBlocco.getA()).getZ() & 15
                     );
                     poseStack.translate(
                        ((BlockPos)datiBlocco.getA()).getX() - pos.getX(),
                        ((BlockPos)datiBlocco.getA()).getY() - pos.getY(),
                        ((BlockPos)datiBlocco.getA()).getZ() - pos.getZ()
                     );
                     RenderSystem.enableBlend();
                     RenderSystem.defaultBlendFunc();
                     RenderSystem.depthMask(true);
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, BlueprintEvent.HologramTrasparency);
                     drawPreviewBuffer((VertexBuffer)datiBlocco.getB(), poseStack, matrix4f, true);
                     poseStack.popPose();
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                     RenderSystem.disableBlend();
                  } catch (Exception e) {
                     e.printStackTrace();
                  }

                  VertexBuffer.unbind();
               }
            }

            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
         }

         if (stage.equals(Stage.AFTER_SOLID_BLOCKS)) {
            Minecraft mc = Minecraft.getInstance();
            BlockRenderDispatcher render = mc.getBlockRenderer();
            BlockEntityRenderDispatcher renderBlockEntity = mc.getBlockEntityRenderDispatcher();
            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            PoseStack poseStack2 = new PoseStack();

            for (Tuple<BlockPos, BlockState> datiBlocco : this.ListaBlocchiSingleRender) {
               Vec3 blockPos = new Vec3(((BlockPos)datiBlocco.getA()).getX(), ((BlockPos)datiBlocco.getA()).getY(), ((BlockPos)datiBlocco.getA()).getZ())
                  .add(originPos.x, originPos.y, originPos.z);
               AABB blockAABB = new AABB(blockPos, blockPos.add(1.0, 1.0, 1.0));
               if (frustum == null || frustum.isVisible(blockAABB)) {
                  poseStack2.pushPose();
                  poseStack2.translate(
                     originPos.x - cameraPos.x + ((BlockPos)datiBlocco.getA()).getX(),
                     originPos.y - cameraPos.y + ((BlockPos)datiBlocco.getA()).getY(),
                     originPos.z - cameraPos.z + ((BlockPos)datiBlocco.getA()).getZ()
                  );

                  try {
                     render.renderSingleBlock(
                        (BlockState)datiBlocco.getB(),
                        poseStack2,
                        mc.renderBuffers().bufferSource(),
                        15728880,
                        OverlayTexture.NO_OVERLAY
                     );
                  } catch (Exception e) {
                     e.printStackTrace();
                  }

                  poseStack2.popPose();
               }
            }
         }

         if (!renderDeep) {
            RenderSystem.enableDepthTest();
         }
      }

      public void renderStructure(PoseStack poseStack, Vec3 cameraPos, Matrix4f matrix4f, Stage stage, Vec3 originPos, boolean renderDeep) {
         if (this.isDatiVertexPronti && this.visualizeBlueprintDatas != null) {
            poseStack.pushPose();
            poseStack.translate(originPos.x - cameraPos.x, originPos.y - cameraPos.y, originPos.z - cameraPos.z);
            this.renderStructureCustomPosestack(poseStack, matrix4f, stage, originPos, renderDeep);
            poseStack.popPose();
         }
      }

      public void renderStructureImage(GuiGraphics gg, int posX, int posY, int width, int height, String namespace) {
         if (this.iconTexture != null || BlueprintsPreviewMethods.ConvertStructureToTextureDataInput.contains(this)) {
            gg.blit(this.iconTexture, posX, posY, width, height, 0.0F, 0.0F, 256, 256, 256, 256);
         } else if (this.visualizeBlueprintDatas != null) {
            this.generatePreviewIcon(namespace);
         }
      }

      private String pendingIconId;

      public void generatePreviewIcon(String namespace) {
         if (!BlueprintsPreviewMethods.hideAllHud && this.iconTexture == null && !this.inCreazioneIcon && !BlueprintsPreviewMethods.waitForNextIconGeneration) {
            String nsID = this.visualizeBlueprintDatas == null ? "" : Blueprint.getIDBlueprint(namespace, this.visualizeBlueprintDatas);
            nsID = Blueprint.sanitizePathUnique(nsID);
            if (BlueprintsPreviewMethods.MapIconsRL.containsKey(nsID)) {
               this.iconTexture = BlueprintsPreviewMethods.MapIconsRL.get(nsID);
            } else if (this.isDatiVertexPronti) {
               BlueprintsPreviewMethods.waitForNextIconGeneration = true;
               this.inCreazioneIcon = true;
               BlueprintsPreviewMethods.hideAllHud = true;
               BlueprintsPreviewMethods.inRenderingForIcon = false;
               BlueprintsPreviewMethods.ConvertStructureToTextureDataInput.add(this);
               this.pendingIconId = nsID;
            }
         }
      }

      public String getUnicID(String namespace) {
         return this.visualizeBlueprintDatas == null ? "" : Blueprint.getIDBlueprint(namespace, this.visualizeBlueprintDatas);
      }
   }
}
