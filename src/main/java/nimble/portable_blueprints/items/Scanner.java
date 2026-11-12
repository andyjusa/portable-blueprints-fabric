package nimble.portable_blueprints.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.network.StoC_OpenScannerScreen;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.Operazioni;

public class Scanner extends Item {
   public static final int MaxScrollingDistance = 100;
   public static final int MinScrollingDistance = 5;
   public static int ScrollingDistanceAttuale = 5;
   private int Tick = 0;

   public Scanner(Properties properties) {
      super(properties);
   }

   public InteractionResult useOn(UseOnContext context) {
      if (!context.getLevel().isClientSide() && context.getHand().equals(InteractionHand.MAIN_HAND)) {
         Player pl = context.getPlayer();
         CompoundTag nbt = OldVersionUtilities.getItemStackNBT(context.getItemInHand());
         boolean angolo1 = nbt.contains("angolo1") ? nbt.getBoolean("angolo1") : false;
         boolean angolo2 = nbt.contains("angolo2") ? nbt.getBoolean("angolo2") : false;
         BlockHitResult rayTraceResult = rayTrace(pl, context.getLevel(), pl.isCrouching() ? Fluid.NONE : Fluid.ANY, ScrollingDistanceAttuale);
         this.impostaCoordinata(context.getItemInHand(), rayTraceResult.getBlockPos(), angolo1 && angolo2 ? true : !angolo1);
      }

      return InteractionResult.SUCCESS;
   }

   public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
      if (pEntity instanceof Player && pIsSelected) {
         if (this.Tick++ > 20) {
            this.Tick = 0;
         }

         CompoundTag nbt = OldVersionUtilities.getItemStackNBT(pStack);
         Player pl = (Player)pEntity;
         if (this.Tick % 2 == 0) {
            BlockHitResult rayTraceResult = rayTrace(pl, pLevel, pl.isCrouching() ? Fluid.NONE : Fluid.ANY, ScrollingDistanceAttuale);
            if (!nbt.contains("angolo1") || !nbt.getBoolean("angolo1") || !nbt.contains("angolo2") || !nbt.getBoolean("angolo2")) {
               nbt.putBoolean("angoloFantasma", true);
               nbt.putString(
                  "coordinateAngoloFantasma",
                  Operazioni.getVec3iString(
                     new Vec3i(rayTraceResult.getBlockPos().getX(), rayTraceResult.getBlockPos().getY(), rayTraceResult.getBlockPos().getZ())
                  )
               );
               OldVersionUtilities.impostaItemStackNBT(pStack, nbt);
            }
         }
      }
   }

   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
      BlockHitResult rayTraceResult = rayTrace(pPlayer, pLevel, pPlayer.isCrouching() ? Fluid.NONE : Fluid.ANY, ScrollingDistanceAttuale);
      if (!pLevel.isClientSide) {
         ItemStack item = pPlayer.getMainHandItem();
         CompoundTag nbt = OldVersionUtilities.getItemStackNBT(item);
         if (pPlayer.isShiftKeyDown()) {
            this.annullaSelezione(pPlayer.getMainHandItem());
         } else if (!pPlayer.isShiftKeyDown()
            && nbt.contains("angolo1")
            && nbt.getBoolean("angolo1")
            && !nbt.getString("coordinateAngolo1").equals("")
            && nbt.contains("angolo2")
            && nbt.getBoolean("angolo2")
            && !nbt.getString("coordinateAngolo2").equals("")) {
            BlockPos angolo1 = new BlockPos(Operazioni.getVec3iFromString(nbt.getString("coordinateAngolo1")));
            BlockPos angolo2 = new BlockPos(Operazioni.getVec3iFromString(nbt.getString("coordinateAngolo2")));
            BlockPos angoloIniziale = new BlockPos(
               angolo1.getX() < angolo2.getX() ? angolo1.getX() : angolo2.getX(),
               angolo1.getY() < angolo2.getY() ? angolo1.getY() : angolo2.getY(),
               angolo1.getZ() < angolo2.getZ() ? angolo1.getZ() : angolo2.getZ()
            );
            BlockPos angoloFinale = new BlockPos(
               angolo1.getX() < angolo2.getX() ? angolo2.getX() : angolo1.getX(),
               angolo1.getY() < angolo2.getY() ? angolo2.getY() : angolo1.getY(),
               angolo1.getZ() < angolo2.getZ() ? angolo2.getZ() : angolo1.getZ()
            );
            int quantitaBlocchi = (angoloFinale.getX() - angoloIniziale.getX() + 1)
               * (angoloFinale.getZ() - angoloIniziale.getZ() + 1)
               * (angoloFinale.getY() - angoloIniziale.getY() + 1);
            PacketDistributor.sendToPlayer((ServerPlayer)pPlayer, new StoC_OpenScannerScreen(item, quantitaBlocchi), new CustomPacketPayload[0]);
         } else if (!pPlayer.isShiftKeyDown()) {
            boolean angolo1 = nbt.contains("angolo1") ? nbt.getBoolean("angolo1") : false;
            boolean angolo2 = nbt.contains("angolo2") ? nbt.getBoolean("angolo2") : false;
            this.impostaCoordinata(pPlayer.getMainHandItem(), rayTraceResult.getBlockPos(), angolo1 && angolo2 ? true : !angolo1);
         }
      }

      return super.use(pLevel, pPlayer, pUsedHand);
   }

   public void impostaCoordinata(ItemStack item, BlockPos pos, Boolean angolo1) {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(item);
      if (nbt.contains("angolo1") && nbt.getBoolean("angolo1") && nbt.contains("angolo2") && nbt.getBoolean("angolo2")) {
         this.annullaSelezione(item);
      }

      if (angolo1) {
         nbt.putString("coordinateAngolo1", Operazioni.getVec3iString(pos));
         nbt.putBoolean("angolo1", true);
         Operazioni.glowingItemStack(item, true);
      } else {
         BlockPos posAngolo1 = new BlockPos(Operazioni.getVec3iFromString(nbt.getString("coordinateAngolo1")));
         BlockPos posAngolo2 = pos;
         BlockPos angoloMaggiore = new BlockPos(
            Math.max(posAngolo1.getX(), posAngolo2.getX()), Math.max(posAngolo1.getY(), posAngolo2.getY()), Math.max(posAngolo1.getZ(), posAngolo2.getZ())
         );
         BlockPos angoloMinore = new BlockPos(
            Math.min(posAngolo1.getX(), posAngolo2.getX()), Math.min(posAngolo1.getY(), posAngolo2.getY()), Math.min(posAngolo1.getZ(), posAngolo2.getZ())
         );
         nbt.putString("coordinateAngolo1", Operazioni.getVec3iString(angoloMinore));
         nbt.putString("coordinateAngolo2", Operazioni.getVec3iString(angoloMaggiore));
         nbt.putBoolean("angolo2", true);
      }

      if (nbt.contains("angolo1") && nbt.getBoolean("angolo1") && nbt.contains("angolo2") && nbt.getBoolean("angolo2")) {
         Operazioni.glowingItemStack(item, false);
      }

      OldVersionUtilities.impostaItemStackNBT(item, nbt);
   }

   public void annullaSelezione(ItemStack item) {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(item);
      nbt.putBoolean("angolo1", false);
      nbt.putString("coordinateAngolo1", "");
      nbt.putBoolean("angolo2", false);
      nbt.putString("coordinateAngolo2", "");
      nbt.putBoolean("angoloFantasma", false);
      nbt.putString("coordinateAngoloFantasma", "");
      Operazioni.glowingItemStack(item, false);
      OldVersionUtilities.impostaItemStackNBT(item, nbt);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      if (Screen.hasShiftDown()) {
         boolean alterna_colore = true;

         for (String val : Component.translatable("item.portable_blueprints.description.scanner_2").getString().split("\n")) {
            if (!val.equals("")) {
               tooltipComponents.add(Component.literal(val).withStyle(alterna_colore ? ChatFormatting.GOLD : ChatFormatting.YELLOW));
               alterna_colore = !alterna_colore;
            }
         }
      } else {
         tooltipComponents.add(Component.translatable("item.portable_blueprints.description.scanner_1").withStyle(ChatFormatting.GOLD));
      }

      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }

   public static BlockHitResult rayTrace(Player player, Level world, Fluid fluidMode, double maxDistance) {
      Vec3 eyePosition = player.getEyePosition(1.0F);
      Vec3 lookVector = player.getViewVector(1.0F);
      Vec3 reachVector = eyePosition.add(lookVector.x * maxDistance, lookVector.y * maxDistance, lookVector.z * maxDistance);
      ClipContext context = new ClipContext(eyePosition, reachVector, Block.OUTLINE, fluidMode, player);
      return world.clip(context);
   }
}
