package nimble.portable_blueprints.Events;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import nimble.portable_blueprints.fabric.client.MouseScrollingEvent;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.network.update_item_scanner_nbt.CtoS_UpdateItemScannerNBT;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.Operazioni;

public class ChangeShapeScanningAreaEvent {
   public static final int MaxRayDistance = 500;
   public static final int maxCooldownTime = 50;
   private static long lastTime = 0L;
   private static Direction direzioneColpita = null;

   public static void ScrollingMouseEvent(MouseScrollingEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.screen == null) {
         ItemStack item = mc.player.getMainHandItem().copy();
         boolean scrollAvanti = event.getScrollDeltaY() > 0.0;
         if (((Item)ModItems.SCANNER.get()).equals(item.getItem())) {
            CompoundTag nbt = OldVersionUtilities.getItemStackNBT(item);
            if (nbt.contains("angolo1")
               && nbt.getBoolean("angolo1")
               && nbt.contains("angolo2")
               && nbt.getBoolean("angolo2")
               && InputConstants.isKeyDown(mc.getWindow().getWindow(), 341)) {
               event.setCanceled(true);
               if (lastTime < System.currentTimeMillis()) {
                  BlockPos posAngolo1 = new BlockPos(Operazioni.getVec3iFromString(nbt.getString("coordinateAngolo1")));
                  BlockPos posAngolo2 = new BlockPos(Operazioni.getVec3iFromString(nbt.getString("coordinateAngolo2")));
                  BlockPos angoloMaggiore = new BlockPos(
                     Math.max(posAngolo1.getX(), posAngolo2.getX()),
                     Math.max(posAngolo1.getY(), posAngolo2.getY()),
                     Math.max(posAngolo1.getZ(), posAngolo2.getZ())
                  );
                  BlockPos angoloMinore = new BlockPos(
                     Math.min(posAngolo1.getX(), posAngolo2.getX()),
                     Math.min(posAngolo1.getY(), posAngolo2.getY()),
                     Math.min(posAngolo1.getZ(), posAngolo2.getZ())
                  );
                  if (direzioneColpita != null) {
                     int v = -1;
                     if (scrollAvanti) {
                        v = 1;
                     }
                     angoloMinore = switch (direzioneColpita) {
                        case NORTH -> angoloMinore.offset(0, 0, -v);
                        case WEST -> angoloMinore.offset(-v, 0, 0);
                        case DOWN -> angoloMinore.offset(0, -v, 0);
                        default -> angoloMinore;
                     };

                     angoloMaggiore = switch (direzioneColpita) {
                        case SOUTH -> angoloMaggiore.offset(0, 0, v);
                        case EAST -> angoloMaggiore.offset(v, 0, 0);
                        case UP -> angoloMaggiore.offset(0, v, 0);
                        default -> angoloMaggiore;
                     };
                     CtoS_UpdateItemScannerNBT.send(item, "coordinateAngolo1", Operazioni.getVec3iString(angoloMinore));
                     CtoS_UpdateItemScannerNBT.send(item, "coordinateAngolo2", Operazioni.getVec3iString(angoloMaggiore));
                     lastTime = System.currentTimeMillis() + 50L;
                  }
               }
            }
         }
      }
   }

   public static void clientTick() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         ItemStack item = mc.player.getMainHandItem().copy();
         CompoundTag nbt = OldVersionUtilities.getItemStackNBT(item);
         if (mc.screen == null
            && item != null
            && ((Item)ModItems.SCANNER.get()).equals(item.getItem())
            && nbt.contains("angolo1")
            && nbt.getBoolean("angolo1")
            && nbt.contains("angolo2")
            && nbt.getBoolean("angolo2")
            && InputConstants.isKeyDown(mc.getWindow().getWindow(), 341)) {
            BlockPos posAngolo1 = new BlockPos(Operazioni.getVec3iFromString(nbt.getString("coordinateAngolo1")));
            BlockPos posAngolo2 = new BlockPos(Operazioni.getVec3iFromString(nbt.getString("coordinateAngolo2")));
            posAngolo2 = posAngolo2.offset(1, 1, 1);
            Vec3 angoloMaggiore = new Vec3(
               Math.max(posAngolo1.getX(), posAngolo2.getX()), Math.max(posAngolo1.getY(), posAngolo2.getY()), Math.max(posAngolo1.getZ(), posAngolo2.getZ())
            );
            Vec3 angoloMinore = new Vec3(
               Math.min(posAngolo1.getX(), posAngolo2.getX()), Math.min(posAngolo1.getY(), posAngolo2.getY()), Math.min(posAngolo1.getZ(), posAngolo2.getZ())
            );
            AABB NORTH_HITBOX = new AABB(angoloMinore, new Vec3(angoloMaggiore.x, angoloMaggiore.y, angoloMinore.z));
            AABB EAST_HITBOX = new AABB(angoloMaggiore, new Vec3(angoloMaggiore.x, angoloMinore.y, angoloMinore.z));
            AABB SOUTH_HITBOX = new AABB(angoloMaggiore, new Vec3(angoloMinore.x(), angoloMinore.y, angoloMaggiore.z));
            AABB WEST_HITBOX = new AABB(angoloMinore, new Vec3(angoloMinore.x, angoloMaggiore.y, angoloMaggiore.z));
            AABB DOWN_HITBOX = new AABB(angoloMinore, new Vec3(angoloMaggiore.x, angoloMinore.y, angoloMaggiore.z));
            AABB UP_HITBOX = new AABB(angoloMaggiore, new Vec3(angoloMinore.x, angoloMaggiore.y, angoloMinore.z));
            List<Tuple<Direction, Vec3>> hitboxToccate = new ArrayList<>();
            Tuple<Boolean, Vec3> valore = rayHitbox(mc.player, NORTH_HITBOX, 500.0F);
            if ((Boolean)valore.getA()) {
               hitboxToccate.add(new Tuple(Direction.NORTH, (Vec3)valore.getB()));
            }

            valore = rayHitbox(mc.player, EAST_HITBOX, 500.0F);
            if ((Boolean)valore.getA()) {
               hitboxToccate.add(new Tuple(Direction.EAST, (Vec3)valore.getB()));
            }

            valore = rayHitbox(mc.player, SOUTH_HITBOX, 500.0F);
            if ((Boolean)valore.getA()) {
               hitboxToccate.add(new Tuple(Direction.SOUTH, (Vec3)valore.getB()));
            }

            valore = rayHitbox(mc.player, WEST_HITBOX, 500.0F);
            if ((Boolean)valore.getA()) {
               hitboxToccate.add(new Tuple(Direction.WEST, (Vec3)valore.getB()));
            }

            valore = rayHitbox(mc.player, DOWN_HITBOX, 500.0F);
            if ((Boolean)valore.getA()) {
               hitboxToccate.add(new Tuple(Direction.DOWN, (Vec3)valore.getB()));
            }

            valore = rayHitbox(mc.player, UP_HITBOX, 500.0F);
            if ((Boolean)valore.getA()) {
               hitboxToccate.add(new Tuple(Direction.UP, (Vec3)valore.getB()));
            }

            if (hitboxToccate.isEmpty()) {
               direzioneColpita = null;
            } else {
               Direction _direzioneColpita = (Direction)hitboxToccate.get(0).getA();
               Vec3 valCalcolo = (Vec3)hitboxToccate.get(0).getB();

               for (Tuple<Direction, Vec3> i : hitboxToccate) {
                  if (mc.player.distanceToSqr(valCalcolo) > mc.player.distanceToSqr((Vec3)i.getB())) {
                     _direzioneColpita = (Direction)i.getA();
                     valCalcolo = (Vec3)i.getB();
                  }
               }

               direzioneColpita = _direzioneColpita;
            }
         } else {
            direzioneColpita = null;
         }

         ModEventClientForge.DirezioneHitboxSelected = direzioneColpita;
      }
   }

   private static Tuple<Boolean, Vec3> rayHitbox(Player player, AABB box, float reachDistance) {
      Tuple<Boolean, Vec3> ret = new Tuple(false, Vec3.ZERO);
      Vec3 eyePos = player.getEyePosition(1.0F);
      Vec3 lookVec = player.getLookAngle();
      Vec3 reachVec = eyePos.add(lookVec.scale(reachDistance));
      Optional<Vec3> hit = box.clip(eyePos, reachVec);
      if (hit.isPresent()) {
         ret.setA(true);
         ret.setB(hit.get());
      }

      return ret;
   }
}
