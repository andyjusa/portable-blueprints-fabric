package nimble.portable_blueprints.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;

public class WornBlueprint extends Blueprint {
   public WornBlueprint(Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack itemStack = player.getMainHandItem();
      this.impostaWornBlueprint(itemStack);
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(itemStack);
      return itemStack == null || nbt.contains("worn_set") && nbt.getBoolean("worn_set")
         ? super.use(world, player, hand)
         : InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      ItemStack itemStack = context.getItemInHand();
      this.impostaWornBlueprint(itemStack);
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(itemStack);
      return itemStack == null || nbt.contains("worn_set") && nbt.getBoolean("worn_set") ? super.useOn(context) : InteractionResult.FAIL;
   }

   private void impostaWornBlueprint(ItemStack item) {
      CompoundTag nbt = OldVersionUtilities.getItemStackNBT(item);
      if (!nbt.contains("worn_set")) {
         if (!nbt.contains("remaining_uses")) {
            nbt.putInt("remaining_uses", 0);
         }

         if (!nbt.contains("blueprint_name")) {
            nbt.putBoolean("worn_set", false);
         }

         if (!nbt.contains("worn_set")) {
            nbt.putBoolean("worn_set", true);
         }

         if (nbt.getBoolean("worn_set")) {
            item.setDamageValue(1);
            nbt.putString("nome", nbt.getString("blueprint_name"));
            nbt.putString("owner", "worn");
            nbt.putString("owner_name", Component.translatable("item.portable_blueprints.worn_blueprint.owner_name").getString());
            nbt.putInt("altezza", 0);
            nbt.putInt("lunghezzaX", 0);
            nbt.putInt("lunghezzaZ", 0);
            nbt.putBoolean("visualizeBuild", false);
            nbt.putShort("rotateValue", (short)0);
            nbt.putBoolean("mirrorX", false);
            nbt.putBoolean("mirrorY", false);
            nbt.putBoolean("mirrorZ", false);
            nbt.putBoolean("wasHolding", false);
            nbt.putBoolean("skipObstructionBlock", false);
            nbt.putBoolean("buildAnyway", false);
            OldVersionUtilities.impostaItemStackNBT(item, nbt);
         }
      }
   }

   @Override
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
         tooltipComponents.add(
            Component.literal(
                  Component.translatable("item.portable_blueprints.description.blueprint_1")
                     .getString()
                     .replace("%v%", nbt.contains("owner_name") ? nbt.getString("owner_name") : "?")
               )
               .withStyle(ChatFormatting.DARK_AQUA)
         );
         tooltipComponents.add(
            Component.literal(
                  Component.translatable("item.portable_blueprints.description.blueprint_2")
                     .getString()
                     .replace("%v%", nbt.contains("nome") ? nbt.getString("nome") : "?")
               )
               .withStyle(ChatFormatting.DARK_AQUA)
         );
         tooltipComponents.add(
            Component.literal(
                  Component.translatable("item.portable_blueprints.description.worn_blueprint_use")
                     .getString()
                     .replace("%v%", nbt.contains("remaining_uses") ? String.valueOf(nbt.getInt("remaining_uses")) : "0")
               )
               .withStyle(ChatFormatting.DARK_AQUA)
         );
         if (nbt.contains("free_build") && nbt.getBoolean("free_build")) {
            tooltipComponents.add(
               Component.literal(Component.translatable("item.portable_blueprints.description.worn_blueprint.free_build").getString())
                  .withStyle(ChatFormatting.LIGHT_PURPLE)
            );
         }

         tooltipComponents.add(Component.translatable("item.portable_blueprints.description.blueprint_3").withStyle(ChatFormatting.GOLD));
      }
   }

   @Override
   public void onItemEquipped(Player player, CompoundTag nbt, ItemStack item) {
      this.impostaWornBlueprint(item);
      super.onItemEquipped(player, nbt, item);
   }
}
