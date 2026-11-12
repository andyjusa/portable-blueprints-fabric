package nimble.portable_blueprints.entity.custom;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class BusinessVillagerEntity extends Mob {
   public static Map<UUID, BusinessVillagerEntity> listaBVEVivi = new HashMap<>();
   private static final EntityDataAccessor<Optional<UUID>> TARGETBP = SynchedEntityData.defineId(
      BusinessVillagerEntity.class, EntityDataSerializers.OPTIONAL_UUID
   );
   public boolean selezionato = false;
   public boolean inviato = false;
   public final AnimationState spawnAnimationState = new AnimationState();
   public float animationProgress = 0.0F;
   public boolean isSpawning = true;
   public int tickAlive = 1000;

   public BusinessVillagerEntity(EntityType<? extends BusinessVillagerEntity> type, Level level) {
      super(type, level);
   }

   private boolean firstTick = true;
   public void tick() {
      super.tick();
      if (firstTick) { firstTick = false; this.poofParticle(); }
      if (this.level().isClientSide()) {
         if (this.isSpawning) {
            this.animationProgress += 0.05F;
            if (this.animationProgress >= 1.0F) {
               this.animationProgress = 1.0F;
               this.isSpawning = false;
            }
         }

         if (!this.inviato && this.getTargetBP().equals(Minecraft.getInstance().player.getUUID())) {
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("portable_blueprints.gui.entity.businessvillager.text"), false);
            this.inviato = true;
         }
      } else {
         this.tickAlive--;
         if (this.tickAlive <= 0 || this.getTargetBP() == null) {
            this.poofParticle();
            this.remove(RemovalReason.DISCARDED);
         }
      }
   }

   public void setTargetBP(UUID uuid) {
      this.entityData.set(TARGETBP, Optional.ofNullable(uuid));
      if (listaBVEVivi.containsKey(uuid) && listaBVEVivi.get(uuid) != null) {
         this.poofParticle();
         listaBVEVivi.get(uuid).tickAlive = -1;
      }

      listaBVEVivi.put(uuid, this);
   }

   @Nullable
   public UUID getTargetBP() {
      return (UUID)((Optional)this.entityData.get(TARGETBP)).orElse(null);
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TARGETBP, Optional.empty());
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.FOLLOW_RANGE, 0.0);
   }

   public Iterable<ItemStack> getArmorSlots() {
      return Collections.emptyList();
   }

   public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
      return ItemStack.EMPTY;
   }

   public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public HumanoidArm getMainArm() {
      return HumanoidArm.RIGHT;
   }

   public void aiStep() {
   }

   public boolean isInvulnerable() {
      return true;
   }

   public boolean hurt(DamageSource src, float amount) {
      return src.is(DamageTypes.GENERIC_KILL) ? super.hurt(src, amount) : false;
   }

   public boolean isInvulnerableTo(DamageSource src) {
      return !src.is(DamageTypes.GENERIC_KILL);
   }

   public void onAddedToLevel() {

      this.poofParticle();
   }

   public void die(DamageSource p_21014_) {
      super.die(p_21014_);
      if (!this.level().isClientSide) {
         UUID toremove = null;

         for (UUID uuid : listaBVEVivi.keySet()) {
            if (listaBVEVivi.get(uuid).equals(this)) {
               toremove = uuid;
               break;
            }
         }

         if (toremove != null) {
            listaBVEVivi.remove(toremove);
         }
      }

      this.poofParticle();
   }

   private void poofParticle() {
      if (!this.level().isClientSide) {
         AABB box = this.getBoundingBox();
         double w = box.getXsize() / 2.0;
         double h = box.getYsize();
         double d = box.getZsize() / 2.0;

         for (int i = 0; i < 40; i++) {
            double dx = this.getX() + (this.random.nextDouble() * 2.0 - 1.0) * w;
            double dy = this.getY() + this.random.nextDouble() * h;
            double dz = this.getZ() + (this.random.nextDouble() * 2.0 - 1.0) * d;
            ((ServerLevel)this.level()).sendParticles(ParticleTypes.POOF, dx, dy, dz, 1, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }
}
