package net.minecraft.block.entity;

import java.util.Arrays;
import java.util.Iterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class BrewingStandBlockEntity extends LockableContainerBlockEntity implements SidedInventory, Tickable {
   private static final int[] TOP_SLOTS = new int[]{3};
   private static final int[] BOTTOM_SLOTS = new int[]{0, 1, 2, 3};
   private static final int[] SIDE_SLOTS = new int[]{0, 1, 2, 4};
   private DefaultedList<ItemStack> inventory;
   private int brewTime;
   private boolean[] slotsEmptyLastTick;
   private Item itemBrewing;
   private int fuel;
   protected final PropertyDelegate propertyDelegate;

   public BrewingStandBlockEntity() {
      super(BlockEntityType.BREWING_STAND);
      this.inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
      this.propertyDelegate = new PropertyDelegate() {
         public int get(int index) {
            switch(index) {
            case 0:
               return BrewingStandBlockEntity.this.brewTime;
            case 1:
               return BrewingStandBlockEntity.this.fuel;
            default:
               return 0;
            }
         }

         public void set(int index, int value) {
            switch(index) {
            case 0:
               BrewingStandBlockEntity.this.brewTime = value;
               break;
            case 1:
               BrewingStandBlockEntity.this.fuel = value;
            }

         }

         public int size() {
            return 2;
         }
      };
   }

   protected Text getContainerName() {
      return new TranslatableText("container.brewing");
   }

   public int size() {
      return this.inventory.size();
   }

   public boolean isEmpty() {
      Iterator var1 = this.inventory.iterator();

      ItemStack itemStack;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         itemStack = (ItemStack)var1.next();
      } while(itemStack.isEmpty());

      return false;
   }

   public void tick() {
      ItemStack itemStack = (ItemStack)this.inventory.get(4);
      if (this.fuel <= 0 && itemStack.getItem() == Items.BLAZE_POWDER) {
         this.fuel = 20;
         itemStack.decrement(1);
         this.markDirty();
      }

      boolean bl = this.canCraft();
      boolean bl2 = this.brewTime > 0;
      ItemStack itemStack2 = (ItemStack)this.inventory.get(3);
      if (bl2) {
         --this.brewTime;
         boolean bl3 = this.brewTime == 0;
         if (bl3 && bl) {
            this.craft();
            this.markDirty();
         } else if (!bl) {
            this.brewTime = 0;
            this.markDirty();
         } else if (this.itemBrewing != itemStack2.getItem()) {
            this.brewTime = 0;
            this.markDirty();
         }
      } else if (bl && this.fuel > 0) {
         --this.fuel;
         this.brewTime = 400;
         this.itemBrewing = itemStack2.getItem();
         this.markDirty();
      }

      if (!this.world.isClient) {
         boolean[] bls = this.getSlotsEmpty();
         if (!Arrays.equals(bls, this.slotsEmptyLastTick)) {
            this.slotsEmptyLastTick = bls;
            BlockState blockState = this.world.getBlockState(this.getPos());
            if (!(blockState.getBlock() instanceof BrewingStandBlock)) {
               return;
            }

            for(int i = 0; i < BrewingStandBlock.BOTTLE_PROPERTIES.length; ++i) {
               blockState = (BlockState)blockState.with(BrewingStandBlock.BOTTLE_PROPERTIES[i], bls[i]);
            }

            this.world.setBlockState(this.pos, blockState, 2);
         }
      }

   }

   public boolean[] getSlotsEmpty() {
      boolean[] bls = new boolean[3];

      for(int i = 0; i < 3; ++i) {
         if (!((ItemStack)this.inventory.get(i)).isEmpty()) {
            bls[i] = true;
         }
      }

      return bls;
   }

   private boolean canCraft() {
      ItemStack itemStack = (ItemStack)this.inventory.get(3);
      if (itemStack.isEmpty()) {
         return false;
      } else if (!BrewingRecipeRegistry.isValidIngredient(itemStack)) {
         return false;
      } else {
         for(int i = 0; i < 3; ++i) {
            ItemStack itemStack2 = (ItemStack)this.inventory.get(i);
            if (!itemStack2.isEmpty() && BrewingRecipeRegistry.hasRecipe(itemStack2, itemStack)) {
               return true;
            }
         }

         return false;
      }
   }

   private void craft() {
      ItemStack itemStack = (ItemStack)this.inventory.get(3);

      for(int i = 0; i < 3; ++i) {
         this.inventory.set(i, BrewingRecipeRegistry.craft(itemStack, (ItemStack)this.inventory.get(i)));
      }

      itemStack.decrement(1);
      BlockPos blockPos = this.getPos();
      if (itemStack.getItem().hasRecipeRemainder()) {
         ItemStack itemStack2 = new ItemStack(itemStack.getItem().getRecipeRemainder());
         if (itemStack.isEmpty()) {
            itemStack = itemStack2;
         } else if (!this.world.isClient) {
            ItemScatterer.spawn(this.world, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack2);
         }
      }

      this.inventory.set(3, itemStack);
      this.world.syncWorldEvent(1035, blockPos, 0);
   }

   public void fromTag(BlockState state, CompoundTag tag) {
      super.fromTag(state, tag);
      this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
      Inventories.fromTag(tag, this.inventory);
      this.brewTime = tag.getShort("BrewTime");
      this.fuel = tag.getByte("Fuel");
   }

   public CompoundTag toTag(CompoundTag tag) {
      super.toTag(tag);
      tag.putShort("BrewTime", (short)this.brewTime);
      Inventories.toTag(tag, this.inventory);
      tag.putByte("Fuel", (byte)this.fuel);
      return tag;
   }

   public ItemStack getStack(int slot) {
      return slot >= 0 && slot < this.inventory.size() ? (ItemStack)this.inventory.get(slot) : ItemStack.EMPTY;
   }

   public ItemStack removeStack(int slot, int amount) {
      return Inventories.splitStack(this.inventory, slot, amount);
   }

   public ItemStack removeStack(int slot) {
      return Inventories.removeStack(this.inventory, slot);
   }

   public void setStack(int slot, ItemStack stack) {
      if (slot >= 0 && slot < this.inventory.size()) {
         this.inventory.set(slot, stack);
      }

   }

   public boolean canPlayerUse(PlayerEntity player) {
      if (this.world.getBlockEntity(this.pos) != this) {
         return false;
      } else {
         return !(player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) > 64.0D);
      }
   }

   public boolean isValid(int slot, ItemStack stack) {
      if (slot == 3) {
         return BrewingRecipeRegistry.isValidIngredient(stack);
      } else {
         Item item = stack.getItem();
         if (slot == 4) {
            return item == Items.BLAZE_POWDER;
         } else {
            return (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.GLASS_BOTTLE) && this.getStack(slot).isEmpty();
         }
      }
   }

   public int[] getAvailableSlots(Direction side) {
      if (side == Direction.UP) {
         return TOP_SLOTS;
      } else {
         return side == Direction.DOWN ? BOTTOM_SLOTS : SIDE_SLOTS;
      }
   }

   public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
      return this.isValid(slot, stack);
   }

   public boolean canExtract(int slot, ItemStack stack, Direction dir) {
      if (slot == 3) {
         return stack.getItem() == Items.GLASS_BOTTLE;
      } else {
         return true;
      }
   }

   public void clear() {
      this.inventory.clear();
   }

   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
      return new BrewingStandScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
   }
}
