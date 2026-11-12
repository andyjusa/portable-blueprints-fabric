package nimble.portable_blueprints.altro;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.fabric.inventory.ItemHandlers;
import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.init.ModGameRules;
import nimble.portable_blueprints.network.StoC_SendLinkedContainer;
import nimble.portable_blueprints.portable_blueprints.MOD;
import nimble.portable_blueprints.portable_blueprints.ModConfig;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.Operazioni;
import nimble.portable_blueprints.robautile.RenderingGraphics;

public class ExternalBlueprintContainerMetodi {
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();
   public static final String IDTAGCONTAINER = "containers";

   public static boolean linkToBlueprint(ItemStack item_blueprint, BlockPos _containerPos, ServerLevel level, ServerPlayer player) {
      BlockPos containerPos = new BlockPos(_containerPos);
      if (level.getBlockEntity(containerPos) != null
         && ItemHandlers.find(level, containerPos, Direction.NORTH) != null
         && MOD.isModContainerConsentito(level.getBlockEntity(containerPos))) {
         List<ExternalBlueprintContainerMetodi.Container> containers = getContainersFromBlueprint(item_blueprint);
         if (containers.size() + 1 > level.getGameRules().getInt(ModGameRules.MaxBlueprintContainers)) {
            containers.remove(0);
         }

         BlockEntity be = level.getBlockEntity(containerPos);
         String id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()).toString();
         if (be instanceof ChestBlockEntity chest && be.getBlockState().getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockState state = be.getBlockState();
            ChestType type = (ChestType)state.getValue(ChestBlock.TYPE);
            Direction facing = (Direction)state.getValue(ChestBlock.FACING);
            BlockPos otherHalf;
            if (type == ChestType.LEFT) {
               otherHalf = containerPos.relative(facing.getClockWise());
            } else {
               otherHalf = containerPos.relative(facing.getCounterClockWise());
            }

            System.out.println("double chest " + otherHalf);
            containers.add(
               new ExternalBlueprintContainerMetodi.Container(_containerPos, otherHalf, id, ExternalBlueprintContainerMetodi.Container.TYPE.DOUBLECHEST)
            );
         } else {
            containers.add(new ExternalBlueprintContainerMetodi.Container(containerPos, id, ExternalBlueprintContainerMetodi.Container.TYPE.SINGLE));
         }

         setContainersToBlueprint(item_blueprint, containers);
         if (player != null) {
            PacketDistributor.sendToPlayer(
               player,
               new StoC_SendLinkedContainer(true, Operazioni.getVec3iString(new Vec3i(containerPos.getX(), containerPos.getY(), containerPos.getZ()))),
               new CustomPacketPayload[0]
            );
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean unlinkToBlueprint(ItemStack item_blueprint, BlockPos _containerPos, ServerLevel level, ServerPlayer player) {
      List<ExternalBlueprintContainerMetodi.Container> containers = getContainersFromBlueprint(item_blueprint);
      ExternalBlueprintContainerMetodi.Container toremove = null;

      for (ExternalBlueprintContainerMetodi.Container container : containers) {
         if (container.insideArea(_containerPos)) {
            toremove = container;
            break;
         }
      }

      if (toremove != null) {
         containers.remove(toremove);
         if (player != null) {
            PacketDistributor.sendToPlayer(
               player,
               new StoC_SendLinkedContainer(false, Operazioni.getVec3iString(new Vec3i(_containerPos.getX(), _containerPos.getY(), _containerPos.getZ()))),
               new CustomPacketPayload[0]
            );
         }
      }

      setContainersToBlueprint(item_blueprint, containers);
      return toremove != null;
   }

   public static void setContainersToBlueprint(ItemStack item_blueprint, List<ExternalBlueprintContainerMetodi.Container> containers) {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(item_blueprint);
      nbt.putString("containers", GSON.toJson(containers));
      OldVersionUtilities.impostaItemStackNBT(item_blueprint, nbt);
   }

   public static List<ExternalBlueprintContainerMetodi.Container> getContainersFromBlueprint(ItemStack item_blueprint) {
      List<ExternalBlueprintContainerMetodi.Container> containers = new ArrayList<>();
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(item_blueprint);
      if (nbt.contains("containers")) {
         Type type = (new TypeToken<List<ExternalBlueprintContainerMetodi.Container>>() {}).getType();

         try {
            containers = (List<ExternalBlueprintContainerMetodi.Container>)GSON.fromJson(nbt.getString("containers"), type);
         } catch (Exception var5) {
         }
      }

      return containers;
   }

   public static void toggleContainerBlueprint(ItemStack item_blueprint, BlockPos _containerPos, ServerLevel level, ServerPlayer player) {
      if (!unlinkToBlueprint(item_blueprint, _containerPos, level, player)) {
         linkToBlueprint(item_blueprint, _containerPos, level, player);
         System.out.println("Aggiunto container");
      } else {
         System.out.println("Rimosso container");
      }
   }

   public static void removeContainersNoLongerExist(ItemStack item_blueprint, Level level) {
      List<ExternalBlueprintContainerMetodi.Container> containers = getContainersFromBlueprint(item_blueprint);
      int sizeBefore = containers.size();
      containers.removeIf(container -> !container.exist(level));
      if (sizeBefore != containers.size()) {
         setContainersToBlueprint(item_blueprint, containers);
      }
   }

   public static void renderBordoAllContainers(ItemStack item_blueprint, RenderLevelStageEvent event) {
      for (ExternalBlueprintContainerMetodi.Container container : getContainersFromBlueprint(item_blueprint)) {
         container.renderBordoContainer(event);
      }
   }

   public static List<BlockPos> getContainersBlockPosFromBlueprint(ItemStack item_blueprint) {
      List<ExternalBlueprintContainerMetodi.Container> containers = getContainersFromBlueprint(item_blueprint);
      List<BlockPos> blockPosList = new ArrayList<>();

      for (ExternalBlueprintContainerMetodi.Container container : containers) {
         blockPosList.add(container.position);
      }

      return blockPosList;
   }

   public static class Container {
      ExternalBlueprintContainerMetodi.Container.TYPE type = ExternalBlueprintContainerMetodi.Container.TYPE.SINGLE;
      BlockPos position;
      boolean islarge = false;
      BlockPos angolo2 = null;
      String idBlockContainer = "";

      public Container(BlockPos position, String idBlockContainer, ExternalBlueprintContainerMetodi.Container.TYPE type) {
         this.idBlockContainer = idBlockContainer;
         this.position = position;
         this.type = type;
      }

      public Container(BlockPos angolo1, BlockPos angolo2, String idBlockContainer, ExternalBlueprintContainerMetodi.Container.TYPE type) {
         this.idBlockContainer = idBlockContainer;
         this.position = angolo1;
         this.angolo2 = angolo2;
         this.islarge = true;
         this.type = type;
      }

      public boolean insideArea(BlockPos containerPos) {
         if (!this.islarge) {
            return this.position.equals(containerPos);
         }

         int minX = Math.min(this.position.getX(), this.angolo2.getX());
         int maxX = Math.max(this.position.getX(), this.angolo2.getX());
         int minY = Math.min(this.position.getY(), this.angolo2.getY());
         int maxY = Math.max(this.position.getY(), this.angolo2.getY());
         int minZ = Math.min(this.position.getZ(), this.angolo2.getZ());
         int maxZ = Math.max(this.position.getZ(), this.angolo2.getZ());
         int x = containerPos.getX();
         int y = containerPos.getY();
         int z = containerPos.getZ();
         return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
      }

      public void renderBordoContainer(RenderLevelStageEvent event) {
         if (this.islarge) {
            RenderingGraphics.renderBorderBox(
               event.getPoseStack(),
               event.getProjectionMatrix(),
               new Vec3(this.position.getX(), this.position.getY(), this.position.getZ()),
               new Vec3(this.angolo2.getX(), this.angolo2.getY(), this.angolo2.getZ()),
               ModConfig.getContainerAreaColor(),
               1.0F,
               (Boolean)ModConfig.SelectedContainerAreaTextureEnabled.get()
            );
         } else {
            RenderingGraphics.renderBorderBox(
               event.getPoseStack(),
               event.getProjectionMatrix(),
               new Vec3(this.position.getX(), this.position.getY(), this.position.getZ()),
               new Vec3(this.position.getX(), this.position.getY(), this.position.getZ()),
               ModConfig.getContainerAreaColor(),
               1.0F,
               (Boolean)ModConfig.SelectedContainerAreaTextureEnabled.get()
            );
         }
      }

      public boolean exist(Level level) {
         BlockEntity be = level.getBlockEntity(this.position);
         if (be == null) {
            return false;
         }

         String idBlocco = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()).toString();
         if (this.idBlockContainer.equals(idBlocco) && this.type != null) {
            switch (this.type) {
               case SINGLE:
                  if (be instanceof ChestBlockEntity) {
                     return be.getBlockState().getValue(ChestBlock.TYPE) == ChestType.SINGLE;
                  }

                  return true;
               case DOUBLECHEST:
                  return be instanceof ChestBlockEntity && be.getBlockState().getValue(ChestBlock.TYPE) != ChestType.SINGLE;
               default:
                  return false;
            }
         } else {
            return false;
         }
      }

      enum TYPE {
         SINGLE,
         DOUBLECHEST;
      }
   }
}
