package nimble.portable_blueprints.fabric.inventory

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.Container
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

object ItemHandlers {
    /** Fabric's lookup handles vanilla inventory holders, double chests and sided rules. */
    @JvmStatic
    fun find(level: Level, position: BlockPos, side: Direction?): ItemHandler? =
        ItemStorage.SIDED.find(level, position, side)?.let(::of)

    /** Commit component updates into the original stack held by the player's inventory. */
    @JvmStatic
    fun itemStorage(source: ItemStack): Storage<ItemVariant>? {
        val slot = object : SingleStackStorage() {
            private var content = source.copy()
            override fun getStack(): ItemStack = content
            override fun setStack(stack: ItemStack) { content = stack }
            override fun canInsert(variant: ItemVariant): Boolean = variant.item === source.item
            override fun onFinalCommit() {
                source.components.keySet().toSet().forEach { if (!content.has(it)) source.remove(it) }
                source.applyComponents(content.components)
                source.count = content.count
            }
        }
        return ContainerItemContext.ofSingleSlot(slot).find(ItemStorage.ITEM)
    }

    @JvmStatic
    fun of(container: Container): ItemHandler = of(container, null)

    @JvmStatic
    fun of(container: Container, side: Direction?): ItemHandler = object : ItemHandler {
        private val slots = if (container is WorldlyContainer && side != null) container.getSlotsForFace(side)
            else IntArray(container.containerSize) { it }
        override fun getSlots(): Int = slots.size
        override fun getStackInSlot(slot: Int): ItemStack = container.getItem(slots[slot]).copy()
        override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
            if (amount <= 0) return ItemStack.EMPTY
            val actual = slots[slot]
            val existing = container.getItem(actual)
            if (existing.isEmpty || container is WorldlyContainer && side != null && !container.canTakeItemThroughFace(actual, existing, side)) return ItemStack.EMPTY
            val extracted = minOf(amount, existing.count)
            if (simulate) return existing.copyWithCount(extracted)
            return container.removeItem(actual, extracted).also { container.setChanged() }
        }
        override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
            if (stack.isEmpty) return ItemStack.EMPTY
            val actual = slots[slot]
            if (!container.canPlaceItem(actual, stack) || container is WorldlyContainer && side != null && !container.canPlaceItemThroughFace(actual, stack, side)) return stack.copy()
            val current = container.getItem(actual)
            if (!current.isEmpty && !ItemStack.isSameItemSameComponents(current, stack)) return stack.copy()
            val accepted = minOf(stack.count, minOf(container.maxStackSize, stack.maxStackSize) - current.count).coerceAtLeast(0)
            if (accepted > 0 && !simulate) {
                container.setItem(actual, stack.copyWithCount(current.count + accepted))
                container.setChanged()
            }
            return remainder(stack, accepted)
        }
    }

    @JvmStatic
    fun of(storage: Storage<ItemVariant>): ItemHandler = object : ItemHandler {
        private fun slots() = storage.toList()
        override fun getSlots(): Int = slots().size
        override fun getStackInSlot(slot: Int): ItemStack {
            val view = slots()[slot]
            return if (view.isResourceBlank) ItemStack.EMPTY else view.resource.toStack(view.amount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
        }
        override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
            if (amount <= 0) return ItemStack.EMPTY
            val view = slots()[slot]
            if (view.isResourceBlank) return ItemStack.EMPTY
            val resource = view.resource
            Transaction.openOuter().use { transaction ->
                val extracted = view.extract(resource, amount.toLong(), transaction).toInt()
                if (!simulate) transaction.commit()
                return if (extracted == 0) ItemStack.EMPTY else resource.toStack(extracted)
            }
        }
        override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
            if (stack.isEmpty) return ItemStack.EMPTY
            val view = slots()[slot]
            // General views are extract-only; reject an insertion that cannot target this slot.
            @Suppress("UNCHECKED_CAST")
            val target = view as? SingleSlotStorage<ItemVariant> ?: return stack.copy()
            Transaction.openOuter().use { transaction ->
                val inserted = target.insert(ItemVariant.of(stack), stack.count.toLong(), transaction).toInt()
                if (!simulate) transaction.commit()
                return remainder(stack, inserted)
            }
        }
    }

    private fun remainder(stack: ItemStack, accepted: Int): ItemStack =
        if (accepted == stack.count) ItemStack.EMPTY else stack.copyWithCount(stack.count - accepted)
}
