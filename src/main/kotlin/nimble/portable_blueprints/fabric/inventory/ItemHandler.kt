package nimble.portable_blueprints.fabric.inventory

import net.minecraft.world.item.ItemStack

/** Slot snapshots must be changed through extract/insert, never by mutating a returned stack. */
interface ItemHandler {
    fun getSlots(): Int
    fun getStackInSlot(slot: Int): ItemStack
    fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack
    fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack
}
