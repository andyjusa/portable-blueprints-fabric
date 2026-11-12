package nimble.portable_blueprints.items;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult.Type;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.EnvType;
import nimble.portable_blueprints.fabric.inventory.ItemHandlers;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.Events.RenderBlueprintsPreview;
import nimble.portable_blueprints.altro.ExternalBlueprintContainerMetodi;
import nimble.portable_blueprints.client.ClientMethods;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.client.VisualizeBlueprintDatas;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.network.CtoS_RemoveItemIfBlueprintNotExist;
import nimble.portable_blueprints.network.ServerBlueprints.CtoS_ServerBlueprintsData;
import nimble.portable_blueprints.network.ottieni_shared_blueprint_data.CtoS_SharedBlueprintData;
import nimble.portable_blueprints.portable_blueprints.MOD;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.Operazioni;

public class Blueprint extends Item {
   private int Tick = 0;
   public static String currentInHandBlueprint = "";
   private static ItemStack beforeHolding = ItemStack.EMPTY;

   public Blueprint(Properties properties) {
      super(properties);
   }

   public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
      if (pEntity instanceof Player player) {
         CompoundTag nbt = OldVersionUtilities.getItemStackNBT(pStack);
         if (pLevel.isClientSide && pEntity instanceof LocalPlayer Lplayer) {
            ItemStack mainHand = Lplayer.getMainHandItem();
            if (ItemStack.matches(pStack, mainHand) && (nbt.getString("owner").equals(Lplayer.getUUID().toString()) || condiviso_con_player(nbt))) {
               if (nbt.contains("originPos") && nbt.contains("nome")) {
                  VisualizeBlueprintDatas.originPosVisualizeBlueprint = new BlockPos(Operazioni.getVec3iFromString(nbt.getString("originPos")));
               }
            } else {
               nbt.putBoolean("visualizeBuild", false);
            }

            OldVersionUtilities.impostaItemStackNBT(pStack, nbt);
            boolean hasBlueprintInHand = ItemStack.isSameItemSameComponents(pStack, mainHand);
            if (hasBlueprintInHand
               && (currentInHandBlueprint.isEmpty() || !currentInHandBlueprint.equals(nbt.getString("owner") + "_" + nbt.getString("nome")))) {
               currentInHandBlueprint = nbt.getString("owner") + "_" + nbt.getString("nome");
               this.onItemEquipped(player, nbt, pStack);
            }

            if (!ItemStack.isSameItem(beforeHolding, mainHand)) {
               beforeHolding = mainHand;
               if (ModItems.getBlueprintItems().contains(mainHand.getItem())) {
                  CompoundTag nbt2 = OldVersionUtilities.getItemStackNBT(mainHand);
                  nbt2.putBoolean("visualizeBuild", true);
                  OldVersionUtilities.impostaItemStackNBT(mainHand, nbt2);
               }
            }
         } else if (!pLevel.isClientSide) {
            ExternalBlueprintContainerMetodi.removeContainersNoLongerExist(pStack, pLevel);
         }
      }
   }

   public InteractionResult useOn(UseOnContext context) {
      if (!context.getLevel().isClientSide() && context.getHand().equals(InteractionHand.MAIN_HAND)) {
         Level level = context.getLevel();
         BlockPos positionClicked = context.getClickedPos();
         CompoundTag nbt = OldVersionUtilities.getItemStackNBT(context.getItemInHand());
         if (level.getBlockEntity(positionClicked) != null
            && ItemHandlers.find(level, positionClicked, Direction.NORTH) != null
            && MOD.isModContainerConsentito(level.getBlockEntity(positionClicked))) {
            ExternalBlueprintContainerMetodi.toggleContainerBlueprint(
               context.getItemInHand(), positionClicked, (ServerLevel)level, (ServerPlayer)context.getPlayer()
            );
            return InteractionResult.FAIL;
         }

         if (nbt.contains("originPos") && positionClicked.equals(new BlockPos(Operazioni.getVec3iFromString(nbt.getString("originPos"))).offset(0, -1, 0))) {
            nbt.remove("originPos");
         } else {
            nbt.putBoolean("visualizeBuild", true);
            nbt.putString("originPos", Operazioni.getVec3iString(new Vec3i(positionClicked.getX(), positionClicked.getY() + 1, positionClicked.getZ())));
         }

         OldVersionUtilities.impostaItemStackNBT(context.getItemInHand(), nbt);
      }

      return super.useOn(context);
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      if (player.pick(5.0, 0.0F, false).getType() == Type.MISS && world.isClientSide) {
         CompoundTag nbt = OldVersionUtilities.getItemStackNBT(player.getMainHandItem());
         if (!condiviso_con_player(nbt)
            && !nbt.getString("owner").equals("server")
            && !nbt.getString("owner").equals("worn")
            && (!nbt.getString("owner").equals("") && !UUID.fromString(nbt.getString("owner")).equals(player.getUUID()) || nbt.getString("owner").equals(""))) {
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
         }

         if ((FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) && Minecraft.getInstance().player != null) {
            ClientMethods.openBlueprintItemGUI(
               player.getMainHandItem(), ExternalBlueprintContainerMetodi.getContainersBlockPosFromBlueprint(player.getMainHandItem()), true
            );
         }
      }

      return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
   }

   public void setBlueprint(ItemStack b, ServerPlayer player, UUID owner, String nome, int altezza, int lunghezzaX, int lunghezzaZ) {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(b);
      String owner_name = "?";
      Optional<GameProfile> profile = player.getServer().getProfileCache().get(owner);
      if (profile != null) {
         owner_name = profile.get().getName();
      }

      nbt.putString("nome", nome);
      nbt.putString("owner", owner.toString());
      nbt.putString("owner_name", owner_name);
      nbt.putInt("altezza", altezza);
      nbt.putInt("lunghezzaX", lunghezzaX);
      nbt.putInt("lunghezzaZ", lunghezzaZ);
      nbt.putBoolean("visualizeBuild", false);
      nbt.putShort("rotateValue", (short)0);
      nbt.putBoolean("mirrorX", false);
      nbt.putBoolean("mirrorY", false);
      nbt.putBoolean("mirrorZ", false);
      nbt.putBoolean("wasHolding", false);
      nbt.putBoolean("skipObstructionBlock", false);
      nbt.putBoolean("buildAnyway", false);
      nbt.putBoolean("projecte_active_emc", false);
      OldVersionUtilities.impostaItemStackNBT(b, nbt);
   }

   public void setServerBlueprint(ItemStack blueprint, String nome, int altezza, int lunghezzaX, int lunghezzaZ) {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(blueprint);
      String owner_name = Component.translatable("gui.portable_blueprints.tablet.shared_by_server").getString();
      nbt.putString("nome", nome);
      nbt.putString("owner", "server");
      nbt.putString("owner_name", owner_name);
      nbt.putInt("altezza", altezza);
      nbt.putInt("lunghezzaX", lunghezzaX);
      nbt.putInt("lunghezzaZ", lunghezzaZ);
      nbt.putBoolean("visualizeBuild", false);
      nbt.putShort("rotateValue", (short)0);
      nbt.putBoolean("mirrorX", false);
      nbt.putBoolean("mirrorY", false);
      nbt.putBoolean("mirrorZ", false);
      nbt.putBoolean("wasHolding", false);
      nbt.putBoolean("skipObstructionBlock", false);
      nbt.putBoolean("buildAnyway", false);
      nbt.putBoolean("projecte_active_emc", false);
      OldVersionUtilities.impostaItemStackNBT(blueprint, nbt);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(stack);
      if (Screen.hasShiftDown()) {
         boolean alterna_colore = true;

         for (String val : Component.translatable("item.portable_blueprints.description.blueprint_4").getString().split("\n")) {
            if (!val.equals("")) {
               tooltipComponents.add(Component.literal(val).withStyle(alterna_colore ? ChatFormatting.GOLD : ChatFormatting.YELLOW));
               alterna_colore = !alterna_colore;
            }
         }
      } else {
         if (stack.is((Item)ModItems.BLUEPRINT.get())) {
            tooltipComponents.add(
               Component.literal(
                     Component.translatable("item.portable_blueprints.description.blueprint_1")
                        .getString()
                        .replace("%v%", nbt.contains("owner_name") ? nbt.getString("owner_name") : "?")
                  )
                  .withStyle(ChatFormatting.DARK_AQUA)
            );
         }

         if (nbt.contains("nome") && nbt.getString("nome").contains("[Global]")) {
            tooltipComponents.add(
               Component.literal(
                     Component.translatable("item.portable_blueprints.description.blueprint_2")
                        .getString()
                        .replace("%v%", nbt.getString("nome").replace("[Global]", ""))
                  )
                  .withStyle(ChatFormatting.DARK_AQUA)
                  .append(Component.literal("§d[Global]"))
            );
         } else {
            tooltipComponents.add(
               Component.literal(
                     Component.translatable("item.portable_blueprints.description.blueprint_2")
                        .getString()
                        .replace("%v%", nbt.contains("nome") ? nbt.getString("nome") : "?")
                  )
                  .withStyle(ChatFormatting.DARK_AQUA)
            );
         }

         if (condiviso_con_player(nbt)) {
            tooltipComponents.add(Component.translatable("item.portable_blueprints.description.blueprint_swm").withStyle(ChatFormatting.LIGHT_PURPLE));
         }

         tooltipComponents.add(Component.translatable("item.portable_blueprints.description.blueprint_3").withStyle(ChatFormatting.GOLD));
      }

      super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
   }

   public void onItemEquipped(Player player, CompoundTag nbt, ItemStack item) {
      CtoS_SharedBlueprintData.send();
      CtoS_ServerBlueprintsData.send();
      PacketDistributor.sendToServer(new CtoS_RemoveItemIfBlueprintNotExist(nbt.getString("nome"), item), new CustomPacketPayload[0]);
      if (nbt.getString("owner").equals("server")
         || nbt.getString("owner").equals("worn")
         || !nbt.getString("owner").equals("server") && !nbt.getString("owner").equals("") && UUID.fromString(nbt.getString("owner")).equals(player.getUUID())
         || condiviso_con_player(nbt)) {
         if (nbt.getString("owner").equals("server")) {
            VisualizeBlueprintDatas.newVisualizeBlueprint(true, nbt.getString("nome"), false, true);
         } else if (nbt.getString("owner").equals("worn")) {
            VisualizeBlueprintDatas.newVisualizeBlueprint(false, true, nbt.getString("nome"), false, true);
         } else if (!nbt.getString("owner").equals("")) {
            VisualizeBlueprintDatas.newVisualizeBlueprint(UUID.fromString(nbt.getString("owner")), nbt.getString("nome"), false, true);
         }

         RenderBlueprintsPreview.doCleanUp();
      }
   }

   public void onItemUnequipped(Player player, CompoundTag nbt) {
   }

   public static boolean condiviso_con_player(CompoundTag nbt) {
      try {
         CtoS_ServerBlueprintsData.send();
         if (nbt.getString("owner").equals("server")) {
            return ServerToClientDatasShared.ServerBlueprints.contains(nbt.getString("nome"));
         } else {
            return nbt.getString("owner").equals("worn")
               ? true
               : ServerToClientDatasShared.SharedWithMeBlueprints != null
                  && ServerToClientDatasShared.SharedWithMeBlueprints.ContieneSWMBlueprint(UUID.fromString(nbt.getString("owner")), nbt.getString("nome"));
         }
      } catch (Exception e) {
         return false;
      }
   }
}
