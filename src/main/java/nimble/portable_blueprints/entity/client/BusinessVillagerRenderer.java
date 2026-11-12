package nimble.portable_blueprints.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.Events.ModEventClientForge;
import nimble.portable_blueprints.entity.custom.BusinessVillagerEntity;
import nimble.portable_blueprints.network.CtoS_CreateRemoveBlockWorkRequest;

public class BusinessVillagerRenderer extends MobRenderer<BusinessVillagerEntity, BusinessVillagerModel<BusinessVillagerEntity>> {
   private final BusinessVillagerModel<BusinessVillagerEntity> model;
   private UUID TargetBP = null;

   public BusinessVillagerRenderer(Context ctx) {
      super(ctx, new BusinessVillagerModel(ctx.bakeLayer(ModModelLayers.BUSINESS_VILLAGER_LAYER)), 0.4F);
      this.model = new BusinessVillagerModel(ctx.bakeLayer(ModModelLayers.BUSINESS_VILLAGER_LAYER));
   }

   public ResourceLocation getTextureLocation(BusinessVillagerEntity businessVillagerEntity) {
      return ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/entity/business_villager.png");
   }

   public void render(BusinessVillagerEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
      if (this.TargetBP == null) {
         this.TargetBP = entity.getTargetBP();
      } else if (Minecraft.getInstance().player.getUUID().equals(this.TargetBP) && entity.animationProgress >= 0.5F) {
         float size = 0.6F * ((entity.animationProgress - 0.5F) / 0.5F);
         boolean Selected = this.renderItem(poseStack, new Vec3(0.75, 0.5, 0.0), Items.DIAMOND_PICKAXE, size, entity, buffer, packedLight, partialTicks);
         if (!entity.selezionato && Selected) {
            entity.selezionato = true;
            Thread tickerThread = new Thread(() -> {
               List<Tuple<Long, BlockPos>> copy = new ArrayList<>(ModEventClientForge.ObstructionBlockManager.Blocks);

               try {
                  for (Tuple<Long, BlockPos> bp : copy) {
                     PacketDistributor.sendToServer(new CtoS_CreateRemoveBlockWorkRequest((BlockPos)bp.getB()), new CustomPacketPayload[0]);
                     Thread.sleep(5L);
                  }
               } catch (Exception e) {
                  e.printStackTrace();
                  Thread.currentThread().interrupt();
               }
            });
            tickerThread.setDaemon(true);
            tickerThread.start();
         }
      }
   }

   private boolean renderItem(
      PoseStack poseStack,
      Vec3 currentPos,
      Item item,
      float baseSize,
      BusinessVillagerEntity entity,
      MultiBufferSource buffer,
      int packedLight,
      float partialTicks
   ) {
      Player player = Minecraft.getInstance().player;
      Vec3 worldOffset = this.transformOffset(entity, currentPos);
      Vec3 itemWorldPos = new Vec3(entity.getX() + worldOffset.x, entity.getY() + worldOffset.y + entity.getBbHeight() * 0.5, entity.getZ() + worldOffset.z);
      Vec3 eyePos = player.getEyePosition(partialTicks);
      Vec3 viewVec = player.getViewVector(partialTicks);
      Vec3 toItem = itemWorldPos.subtract(eyePos);
      double distance = toItem.length();
      Vec3 toItemNorm = distance > 1.0E-4 ? toItem.normalize() : Vec3.ZERO;
      double dot = viewVec.dot(toItemNorm);
      dot = Mth.clamp((float)dot, -1.0F, 1.0F);
      double angleRad = Math.acos(dot);
      double maxDistance = 20.0;
      double angleThresholdDeg = 6.0;
      double angleThreshold = Math.toRadians(angleThresholdDeg);
      double maxScaleBoost = 1.0;
      float scale = baseSize;
      if (distance <= maxDistance && angleRad <= angleThreshold && player.distanceTo(entity) <= 3.0F) {
         double angleFactor = 1.0 - angleRad / angleThreshold;
         double distanceFactor = 1.0 - distance / maxDistance;
         if (distanceFactor < 0.0) {
            distanceFactor = 0.0;
         }

         double combined = angleFactor * distanceFactor;
         double boost = 1.0 + combined * maxScaleBoost;
         scale = (float)(baseSize * boost);
      }

      poseStack.pushPose();
      poseStack.translate(worldOffset.x, worldOffset.y + entity.getBbHeight() * 0.5, worldOffset.z);
      poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
      poseStack.scale(scale, scale, scale);
      Minecraft.getInstance()
         .getItemRenderer()
         .renderStatic(new ItemStack(item), ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), 0);
      poseStack.popPose();
      return scale != baseSize && Minecraft.getInstance().options.keyUse.isDown();
   }

   private Vec3 transformOffset(BusinessVillagerEntity entity, Vec3 localOffset) {
      double yawRad = Math.toRadians(entity.getYRot());
      double sin = Math.sin(yawRad);
      double cos = Math.cos(yawRad);
      Vec3 forward = new Vec3(-sin, 0.0, cos);
      Vec3 right = new Vec3(cos, 0.0, sin);
      Vec3 up = new Vec3(0.0, 1.0, 0.0);
      return forward.scale(localOffset.x).add(up.scale(localOffset.y)).add(right.scale(localOffset.z));
   }
}
