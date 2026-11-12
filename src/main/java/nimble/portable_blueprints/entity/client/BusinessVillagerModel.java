package nimble.portable_blueprints.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;
import nimble.portable_blueprints.entity.custom.BusinessVillagerEntity;
import nimble.portable_blueprints.robautile.Operazioni;

public class BusinessVillagerModel<T extends Entity> extends HierarchicalModel<T> {
   private final ModelPart businessvillager;
   private final ModelPart head;
   private final ModelPart nose;
   private final ModelPart body;
   private final ModelPart arms;
   private final ModelPart mirrored;
   private final ModelPart right_leg;
   private final ModelPart left_leg;
   private final ModelPart briefcase;
   private final ModelPart top;

   public BusinessVillagerModel(ModelPart root) {
      this.businessvillager = root.getChild("businessvillager");
      this.head = this.businessvillager.getChild("head");
      this.nose = this.businessvillager.getChild("nose");
      this.body = this.businessvillager.getChild("body");
      this.arms = this.businessvillager.getChild("arms");
      this.mirrored = this.arms.getChild("mirrored");
      this.right_leg = this.businessvillager.getChild("right_leg");
      this.left_leg = this.businessvillager.getChild("left_leg");
      this.briefcase = this.businessvillager.getChild("briefcase");
      this.top = this.briefcase.getChild("top");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition businessvillager = partdefinition.addOrReplaceChild("businessvillager", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition head = businessvillager.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -24.0F, 0.0F)
      );
      PartDefinition nose = businessvillager.addOrReplaceChild(
         "nose",
         CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -26.0F, 0.0F)
      );
      PartDefinition body = businessvillager.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -24.0F, 0.0F)
      );
      PartDefinition arms = businessvillager.addOrReplaceChild(
         "arms",
         CubeListBuilder.create()
            .texOffs(40, 38)
            .addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(44, 22)
            .addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, -21.05F, -1.05F, -0.7505F, 0.0F, 0.0F)
      );
      PartDefinition mirrored = arms.addOrReplaceChild(
         "mirrored",
         CubeListBuilder.create().texOffs(44, 22).mirror().addBox(4.0F, -23.05F, -3.05F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(0.0F, 21.05F, 1.05F)
      );
      PartDefinition right_leg = businessvillager.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, -12.0F, 0.0F)
      );
      PartDefinition left_leg = businessvillager.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(2.0F, -12.0F, 0.0F)
      );
      PartDefinition briefcase = businessvillager.addOrReplaceChild("briefcase", CubeListBuilder.create(), PartPose.offset(0.0F, -18.0F, -7.0F));
      PartDefinition bottom_r1 = briefcase.addOrReplaceChild(
         "bottom_r1",
         CubeListBuilder.create().texOffs(0, 50).addBox(-7.0F, -11.0F, -1.0F, 14.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, -3.1416F)
      );
      PartDefinition top = briefcase.addOrReplaceChild("top", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));
      PartDefinition top_r1 = top.addOrReplaceChild(
         "top_r1",
         CubeListBuilder.create().texOffs(0, 50).addBox(-7.0F, -11.0F, -1.0F, 14.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void setupAnim(Entity _entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.root().getAllParts().forEach(ModelPart::resetPose);
      BusinessVillagerEntity entity = (BusinessVillagerEntity)_entity;
      if (entity.isSpawning) {
         float t = entity.animationProgress;
         float size = Operazioni.lerp(0.0F, 1.0F, t < 0.5F ? t / 0.5F : 1.0F);
         this.briefcase.xScale = size;
         this.briefcase.yScale = size;
         this.briefcase.zScale = size;
         if (t > 0.5F) {
            t -= 0.5F;
            float rot = Operazioni.lerp(0.0F, 90.0F, t / 0.5F);
            this.top.xRot = -((float)Math.toRadians(rot));
         }
      } else {
         this.briefcase.xScale = 1.0F;
         this.briefcase.yScale = 1.0F;
         this.briefcase.zScale = 1.0F;
         this.top.xRot = -((float)Math.toRadians(90.0));
      }
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
      this.businessvillager.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
   }

   public ModelPart root() {
      return this.businessvillager;
   }
}
