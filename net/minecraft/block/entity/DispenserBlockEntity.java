package net.minecraft.block.entity;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;

public class DispenserBlockEntity extends LootableContainerBlockEntity {
   private static final Random RANDOM = new Random();
   private DefaultedList<ItemStack> inventory;

   protected DispenserBlockEntity(BlockEntityType<?> blockEntityType) {
      super(blockEntityType);
      this.inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);
   }

   public DispenserBlockEntity() {
      this(BlockEntityType.DISPENSER);
   }

   public int size() {
      return 9;
   }

   public int chooseNonEmptySlot() {
      this.checkLootInteraction((PlayerEntity)null);
      int i = -1;
      int j = 1;

      for(int k = 0; k < this.inventory.size(); ++k) {
         if (!((ItemStack)this.inventory.get(k)).isEmpty() && RANDOM.nextInt(j++) == 0) {
            i = k;
         }
      }

      return i;
   }

   public int addToFirstFreeSlot(ItemStack stack) {
      for(int i = 0; i < this.inventory.size(); ++i) {
         if (((ItemStack)this.inventory.get(i)).isEmpty()) {
            this.setStack(i, stack);
            return i;
         }
      }

      return -1;
   }

   protected Text getContainerName() {
      return new TranslatableText("container.dispenser");
   }

   public void fromTag(BlockState state, CompoundTag tag) {
      super.fromTag(state, tag);
      this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
      if (!this.deserializeLootTable(tag)) {
         Inventories.fromTag(tag, this.inventory);
      }

   }

   public CompoundTag toTag(CompoundTag tag) {
      super.toTag(tag);
      if (!this.serializeLootTable(tag)) {
         Inventories.toTag(tag, this.inventory);
      }

      return tag;
   }

   protected DefaultedList<ItemStack> getInvStackList() {
      return this.inventory;
   }

   protected void setInvStackList(DefaultedList<ItemStack> list) {
      this.inventory = list;
   }

   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
      return new Generic3x3ContainerScreenHandler(syncId, playerInventory, this);
   }
}
