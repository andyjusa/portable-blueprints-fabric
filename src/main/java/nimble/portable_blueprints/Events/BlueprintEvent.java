package nimble.portable_blueprints.Events;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Minecraft;
import nimble.portable_blueprints.fabric.client.MouseScrollingEvent;
import nimble.portable_blueprints.client.VisualizeBlueprintDatas;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.network.update_item_blueprint_nbt.CtoS_UpdateItemBlueprintNBT;
import nimble.portable_blueprints.portable_blueprints.ModConfig;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;

public class BlueprintEvent {
   public static final int MaxRayDistance = 500;
   public static final int maxCooldownTime = 260;
   private static long lastTime = 0L;
   private static Direction direzioneColpita = null;
   public static float HologramTrasparency = 0.0F;
   private static int trasparency = 0;
   private static float lastHologramTrasparencyConfig = -1.0F;

   public static void ScrollingMouseEvent(MouseScrollingEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.screen == null) {
         ItemStack item = mc.player.getMainHandItem().copy();
         boolean scrollAvanti = event.getScrollDeltaY() > 0.0;
         if (ModItems.getBlueprintItems().contains(item.getItem())) {
            CompoundTag nbt = OldVersionUtilities.getItemStackNBT(item);
            if (nbt.contains("originPos")
               && VisualizeBlueprintDatas.originPosVisualizeBlueprint != null
               && VisualizeBlueprintDatas.VisualizeBlueprint != null
               && InputConstants.isKeyDown(mc.getWindow().getWindow(), 341)) {
               event.setCanceled(true);
               if (lastTime < System.currentTimeMillis()) {
                  BlockPos posOrigin = VisualizeBlueprintDatas.originPosVisualizeBlueprint;
                  if (direzioneColpita != null) {
                     int v = -1;
                     if (scrollAvanti) {
                        v = 1;
                     }
                     posOrigin = switch (direzioneColpita) {
                        case NORTH -> posOrigin.offset(0, 0, -v);
                        case EAST -> posOrigin.offset(v, 0, 0);
                        case SOUTH -> posOrigin.offset(0, 0, v);
                        case WEST -> posOrigin.offset(-v, 0, 0);
                        case DOWN -> posOrigin.offset(0, -v, 0);
                        case UP -> posOrigin.offset(0, v, 0);
                        default -> throw new MatchException(null, null);
                     };
                     CtoS_UpdateItemBlueprintNBT.send(item, "originPos", posOrigin);
                     lastTime = System.currentTimeMillis() + 260L;
                  }
               }
            } else if (InputConstants.isKeyDown(mc.getWindow().getWindow(), 340)) {
               if (lastTime < System.currentTimeMillis()) {
                  int newValue = trasparency + (scrollAvanti ? 1 : -1);
                  if (newValue > 100) {
                     newValue = 100;
                  }

                  if (newValue < 0) {
                     newValue = 0;
                  }

                  trasparency = newValue;
                  HologramTrasparency = 1.0F - trasparency / 100.0F;

                  try {
                     TextColor t = (TextColor)TextColor.parseColor((String)ModConfig.BlueprintAreaColor.get()).getOrThrow();
                     mc.player.displayClientMessage(Component.literal(String.valueOf(newValue)).append("%").withStyle(Style.EMPTY.withColor(t)), true);
                  } catch (Exception var7) {
                  }

                  lastTime = System.currentTimeMillis() + 10L;
               }

               event.setCanceled(true);
            }
         }
      }
   }

   public static void clientTick(Minecraft event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         if (lastHologramTrasparencyConfig != ModConfig.getDefaultHologramTransparency()) {
            lastHologramTrasparencyConfig = ModConfig.getDefaultHologramTransparency();
            HologramTrasparency = lastHologramTrasparencyConfig;
            int v = (Integer)ModConfig.DefaultHologramTransparency.get();
            if (v > 100) {
               v = 100;
            }

            if (v < 0) {
               v = 0;
            }

            trasparency = v;
         }

         ItemStack item = mc.player.getMainHandItem().copy();
         if (mc.screen == null
            && item != null
            && ModItems.getBlueprintItems().contains(item.getItem())
            && VisualizeBlueprintDatas.VisualizeBlueprint != null
            && VisualizeBlueprintDatas.originPosVisualizeBlueprint != null
            && InputConstants.isKeyDown(mc.getWindow().getWindow(), 341)) {
            Vec3 posOrigin = new Vec3(
               VisualizeBlueprintDatas.originPosVisualizeBlueprint.getX(),
               VisualizeBlueprintDatas.originPosVisualizeBlueprint.getY(),
               VisualizeBlueprintDatas.originPosVisualizeBlueprint.getZ()
            );
            Vec3 posAngolo2 = posOrigin.add(
               VisualizeBlueprintDatas.VisualizeBlueprint.lunghezzaX,
               VisualizeBlueprintDatas.VisualizeBlueprint.altezza,
               VisualizeBlueprintDatas.VisualizeBlueprint.lunghezzaZ
            );
            AABB NORTH_HITBOX = new AABB(posOrigin, new Vec3(posAngolo2.x, posAngolo2.y, posOrigin.z));
            AABB EAST_HITBOX = new AABB(posAngolo2, new Vec3(posAngolo2.x, posOrigin.y, posOrigin.z));
            AABB SOUTH_HITBOX = new AABB(posAngolo2, new Vec3(posOrigin.x, posOrigin.y, posAngolo2.z));
            AABB WEST_HITBOX = new AABB(posOrigin, new Vec3(posOrigin.x, posAngolo2.y, posAngolo2.z));
            AABB DOWN_HITBOX = new AABB(posOrigin, new Vec3(posAngolo2.x, posOrigin.y, posAngolo2.z));
            AABB UP_HITBOX = new AABB(posAngolo2, new Vec3(posOrigin.x, posAngolo2.y, posOrigin.z));
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

         RenderBlueprintsPreview.DirezioneHitboxSelected = direzioneColpita;
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
