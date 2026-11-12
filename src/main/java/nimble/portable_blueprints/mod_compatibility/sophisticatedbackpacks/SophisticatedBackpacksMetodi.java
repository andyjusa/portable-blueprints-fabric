package nimble.portable_blueprints.mod_compatibility.sophisticatedbackpacks;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.fabric.inventory.ItemHandler;

/** NeoForge Sophisticated Backpacks integration is explicitly disabled on Fabric.
 * Items using vanilla CONTAINER components still work through ModifiableItemInventory. */
public final class SophisticatedBackpacksMetodi {
    public static boolean isABackpack(ItemStack stack) { return false; }
    public static boolean isABackpack(BlockEntity blockEntity) { return false; }
    public static ItemHandler getInventoryFromBackpack(ItemStack stack) { return null; }
    public static ItemHandler getInventoryFromBackpack(BlockEntity blockEntity) { return null; }
    public static Tuple<Boolean, Vec3> removeItemFromBackpack(ServerPlayer player, ServerLevel level, ItemStack backpack, Item target) { return null; }
    public static Tuple<Boolean, Vec3> removeItemFromBackpack(ServerPlayer player, ServerLevel level, BlockEntity backpack, Item target, BlockPos position) { return null; }
    public static void getAllItemFromBackpack(ItemStack backpack, Map<Item, Integer> quantities) {}
    public static void getAllItemFromBackpack(BlockEntity backpack, Map<Item, Integer> quantities) {}
}
