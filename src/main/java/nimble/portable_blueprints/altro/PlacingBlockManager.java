package nimble.portable_blueprints.altro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.init.ModGameRules;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.Operazioni;

public class PlacingBlockManager {
   public static final List<PlacingBlockManager.PlaceBlock> PlaceBlocks = Collections.synchronizedList(new ArrayList<>());
   private static ScheduledExecutorService scheduler;
   private static final List<String> NormalPlaceBlocksList = List.of(
      "minecraft:white_bed",
      "minecraft:light_gray_bed",
      "minecraft:gray_bed",
      "minecraft:black_bed",
      "minecraft:brown_bed",
      "minecraft:red_bed",
      "minecraft:orange_bed",
      "minecraft:yellow_bed",
      "minecraft:lime_bed",
      "minecraft:green_bed",
      "minecraft:cyan_bed",
      "minecraft:light_blue_bed",
      "minecraft:blue_bed",
      "minecraft:purple_bed",
      "minecraft:magenta_bed",
      "minecraft:pink_bed",
      "meadow:flecked_bed",
      "meadow:highland_bed",
      "meadow:patched_bed",
      "meadow:rocky_bed",
      "meadow:umbra_bed",
      "meadow:inky_bed",
      "meadow:warped_bed",
      "meadow:straw_bed"
   );

   public static void onServerStarted(MinecraftServer server) {
      if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
         scheduler = Executors.newSingleThreadScheduledExecutor();
         scheduler.scheduleAtFixedRate(() -> {
            if (server != null && !server.isStopped()) {
               long now = System.currentTimeMillis();
               List<PlacingBlockManager.PlaceBlock> toPlace = new ArrayList<>();
               synchronized (PlaceBlocks) {
                  Iterator<PlacingBlockManager.PlaceBlock> iterator = PlaceBlocks.iterator();

                  while (iterator.hasNext()) {
                     PlacingBlockManager.PlaceBlock pb = iterator.next();
                     if (pb.canPlaceNow(now)) {
                        toPlace.add(pb);
                        iterator.remove();
                     }
                  }
               }

               if (!toPlace.isEmpty()) {
                  server.execute(() -> {
                     for (PlacingBlockManager.PlaceBlock pbx : toPlace) {
                        pbx.placeNow();
                     }
                  });
               }
            }
         }, 0L, 100L, TimeUnit.MILLISECONDS);
      }
   }

   public static void onServerStopped(MinecraftServer server) {
      if (scheduler != null && !scheduler.isShutdown()) {
         scheduler.shutdownNow();
         scheduler = null;
      }

      PlaceBlocks.clear();
   }

   public static ScheduledExecutorService getScheduler() {
      return scheduler;
   }

   public static void addNewPlaceBlock(
      ServerPlayer player, ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, String nbt, short allow_nbt, long delay
   ) {
      if (!blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
         || blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
         PlaceBlocks.add(new PlacingBlockManager.PlaceBlock(player, serverLevel, blockPos, blockState, nbt, allow_nbt, delay));
      }
   }

   public static boolean isRimpiazzabile(BlockState blockState) {
      return blockState.getBlock().equals(Blocks.AIR) || blockState.canBeReplaced() || blockState.getPistonPushReaction().equals(PushReaction.DESTROY);
   }

   public static boolean droppaBloccoRimpiazzabile(ServerLevel serverLevel, BlockPos pos) {
      BlockState blockState = serverLevel.getBlockState(pos);
      if (blockState.getBlock().equals(Blocks.AIR)) {
         return true;
      } else if (isRimpiazzabile(blockState)) {
         serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
         Block.dropResources(blockState, serverLevel, pos, serverLevel.getBlockEntity(pos));
         return true;
      } else {
         return false;
      }
   }

   private static void placeBlockContext(ServerLevel level, BlockPos pos, BlockState desiredState, ServerPlayer player) {
      Block block = desiredState.getBlock();
      Item item = (Item)Item.BY_BLOCK.get(block);
      if (item instanceof BlockItem blockItem && !NormalPlaceBlocksList.contains(BuiltInRegistries.BLOCK.getKey(block).toString())) {
         ItemStack stack = new ItemStack(block);
         Direction clickedFace = Direction.UP;
         BlockPlaceContext context = new BlockPlaceContext(
            level, player, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.atCenterOf(pos), clickedFace, pos, false)
         ) {
            public boolean canPlace() {
               return true;
            }

            public boolean isInside() {
               return false;
            }
         };
         InteractionResult result = blockItem.place(context);
         if (result.consumesAction()) {
            BlockState placed = level.getBlockState(pos);
            if (placed.getBlock() == block) {
               BlockState finalState = copyBlockStateProperties(desiredState, placed);
               placeBlock(level, pos, finalState);
            }
         } else {
            placeBlock(level, pos, desiredState);
         }
      } else {
         placeBlock(level, pos, desiredState);
      }
   }

   private static void placeBlock(ServerLevel serverLevel, BlockPos pos, BlockState desiredState) {
      String id = BuiltInRegistries.BLOCK.getKey(desiredState.getBlock()).toString();
      serverLevel.setBlock(pos, desiredState, 3);
      serverLevel.updateNeighborsAt(pos, desiredState.getBlock());
      if (id.equals("vinery:window")
         || id.equals("meadow:heart_patterned_window")
         || id.equals("meadow:sun_patterned_window")
         || id.equals("meadow:pine_window")) {
         BlockPos lastwindow = null;

         do {
            if (lastwindow == null) {
               lastwindow = pos;
            } else {
               lastwindow = lastwindow.below();
            }
         } while (serverLevel.getBlockState(lastwindow.below()).getBlock().equals(desiredState.getBlock()));

         if (!lastwindow.equals(pos) && serverLevel.getBlockState(lastwindow).getBlock().equals(desiredState.getBlock())) {
            BlockState bs = serverLevel.getBlockState(lastwindow);
            placeBlock(serverLevel, lastwindow, Blocks.AIR.defaultBlockState());
            serverLevel.setBlock(lastwindow, bs, 2);
         }
      }
   }

   public static BlockState copyBlockStateProperties(BlockState from, BlockState to) {
      for (Property<?> property : from.getProperties()) {
         if (to.hasProperty(property)) {
            to = setPropertySafe(to, property, from.getValue(property));
         }
      }

      return to;
   }

   private static <T extends Comparable<T>> BlockState setPropertySafe(BlockState state, Property<T> property, Comparable<?> value) {
      try {
         return (BlockState)state.setValue(property, property.getValueClass().cast(value));
      } catch (Exception e) {
         return state;
      }
   }

   public static class PlaceBlock {
      private final ServerPlayer player;
      public final ServerLevel serverLevel;
      public final BlockPos blockPos;
      public final BlockState blockState;
      public final long placeAtMillis;
      public final String nbt;
      public final short allow_nbt;

      public PlaceBlock(ServerPlayer player, ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, String nbt, short allow_nbt, long delayMillis) {
         this.player = player;
         this.serverLevel = serverLevel;
         this.blockPos = blockPos;
         this.blockState = blockState;
         this.placeAtMillis = System.currentTimeMillis() + delayMillis;
         this.allow_nbt = allow_nbt;
         this.nbt = nbt;
      }

      public boolean canPlaceNow(long now) {
         return now >= this.placeAtMillis;
      }

      public void placeNow() {
         try {
            GameType gameModeForPlayer = this.player.gameMode.getGameModeForPlayer();
            if (gameModeForPlayer.isBlockPlacingRestricted()) {
               gameModeForPlayer = GameType.SURVIVAL;
            }

            if (this.serverLevel != null
               && this.player != null
               && this.serverLevel.mayInteract(this.player, this.blockPos) && this.player.mayBuild() && net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(this.serverLevel, this.player, this.blockPos, this.serverLevel.getBlockState(this.blockPos), this.serverLevel.getBlockEntity(this.blockPos))) {
               if (PlacingBlockManager.droppaBloccoRimpiazzabile(this.serverLevel, this.blockPos)) {
                  PlacingBlockManager.placeBlockContext(this.serverLevel, this.blockPos, this.blockState, this.player);
                  if ((this.allow_nbt == 1 || this.allow_nbt == -1 && this.serverLevel.getGameRules().getBoolean(ModGameRules.AllowNBTDataCopy))
                     && !this.nbt.isEmpty()) {
                     BlockEntity be = this.serverLevel.getBlockEntity(this.blockPos);
                     if (be != null) {
                        Tuple<DataComponentMap, CompoundTag> vals = OldVersionUtilities.convertJsonToDataComponentMapAndNBT(this.nbt);
                        if (!((DataComponentMap)vals.getA()).isEmpty()) {
                           be.setComponents((DataComponentMap)vals.getA());
                        }

                        if (!((CompoundTag)vals.getB()).isEmpty()) {
                           be.loadWithComponents((CompoundTag)vals.getB(), this.serverLevel.registryAccess());
                        }

                        be.setChanged();
                        this.serverLevel.sendBlockUpdated(this.blockPos, this.blockState, this.blockState, 3);
                     }
                  }
               } else {
                  Block.popResource(this.serverLevel, this.blockPos, Operazioni.getCorrectItemStackFromId(this.blockState.getBlock()));
               }
            }
         } catch (Exception e) {
            System.err.println("Placement Block Error!");
            e.printStackTrace();
         }
      }
   }
}
