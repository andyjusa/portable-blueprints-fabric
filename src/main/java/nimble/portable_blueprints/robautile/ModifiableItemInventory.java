package nimble.portable_blueprints.robautile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import nimble.portable_blueprints.fabric.inventory.ItemHandlers;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntityType;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;

public class ModifiableItemInventory {
   private final ItemStack source;
   private final Container container;
   private final Storage<ItemVariant> storage;
   private final List<ItemStack> originalItems;

   private ModifiableItemInventory(ServerLevel serverLevel, ItemStack source, Container container) {
      this.source = source;
      this.container = container;
      this.storage = null;
      this.originalItems = List.of();
   }

   private ModifiableItemInventory(ServerLevel level, ItemStack source, Container container,
         Storage<ItemVariant> storage,
         List<ItemStack> originalItems) {
      this.source = source;
      this.container = container;
      this.storage = storage;
      this.originalItems = originalItems;
   }

   public static Optional<ModifiableItemInventory> of(ServerLevel serverLevel, ItemStack stack) {
      if (stack.isEmpty()) {
         return Optional.empty();
      }

      ItemContainerContents contents = OldVersionUtilities.getItemInventoryData(stack);
      if (contents == null) {
         var storage = ItemHandlers.itemStorage(stack);
         if (storage == null) return Optional.empty();
         List<ItemStack> items = new ArrayList<>();
         for (var view : storage) items.add(view.isResourceBlank() ? ItemStack.EMPTY : view.getResource().toStack((int)Math.min(Integer.MAX_VALUE, view.getAmount())));
         if (items.isEmpty()) return Optional.empty();
         SimpleContainer container = new SimpleContainer(items.size());
         for (int slot = 0; slot < items.size(); slot++) container.setItem(slot, items.get(slot).copy());
         return Optional.of(new ModifiableItemInventory(serverLevel, stack, container, storage, items));
      }
      List<ItemStack> items = contents.stream().map(ItemStack::copy).toList();
      SimpleContainer container = new SimpleContainer(items.size());
      for (int slot = 0; slot < items.size(); slot++) container.setItem(slot, items.get(slot));
      return Optional.of(new ModifiableItemInventory(serverLevel, stack, container));
   }

   public Container getContainer() {
      return this.container;
   }

   public void save() {
      List<ItemStack> items = new ArrayList<>();
      for (int slot = 0; slot < container.getContainerSize(); slot++) items.add(container.getItem(slot).copy());
      if (storage == null) {
         OldVersionUtilities.impostaItemInventoryData(source, ItemContainerContents.fromItems(items));
         return;
      }
      var views = new ArrayList<StorageView<ItemVariant>>();
      storage.forEach(views::add);
      try (var transaction = Transaction.openOuter()) {
         for (int slot = 0; slot < items.size(); slot++) {
            ItemStack before = originalItems.get(slot);
            ItemStack after = items.get(slot);
            if (ItemStack.matches(before, after)) continue;
            if (slot >= views.size()) throw new IllegalStateException("Item storage changed while editing");
            var view = views.get(slot);
            boolean same = ItemStack.isSameItemSameComponents(before, after);
            int remove = same ? Math.max(0, before.getCount() - after.getCount()) : before.getCount();
            int insert = same ? Math.max(0, after.getCount() - before.getCount()) : after.getCount();
            if (remove > 0 && view.extract(ItemVariant.of(before), remove, transaction) != remove)
               throw new IllegalStateException("Item storage refused removal");
            if (insert > 0 && storage.insert(ItemVariant.of(after), insert, transaction) != insert)
               throw new IllegalStateException("Item storage refused insertion");
         }
         transaction.commit();
      }
      originalItems.clear();
      items.stream().map(ItemStack::copy).forEach(originalItems::add);
   }

   @Nullable
   public static BlockEntityType<?> getBlockEntityType(ItemStack stack) {
      if (stack.getItem() instanceof BlockItem blockItem) {
         for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            if (type.isValid(blockItem.getBlock().defaultBlockState())) {
               return type;
            }
         }

         return null;
      } else {
         return null;
      }
   }

}
