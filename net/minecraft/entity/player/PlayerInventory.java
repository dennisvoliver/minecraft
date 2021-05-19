package net.minecraft.entity.player;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.World;

public class PlayerInventory implements Inventory, Nameable {
   public final DefaultedList<ItemStack> main;
   public final DefaultedList<ItemStack> armor;
   public final DefaultedList<ItemStack> offHand;
   private final List<DefaultedList<ItemStack>> combinedInventory;
   public int selectedSlot;
   public final PlayerEntity player;
   private ItemStack cursorStack;
   private int changeCount;

   public PlayerInventory(PlayerEntity player) {
      this.main = DefaultedList.ofSize(36, ItemStack.EMPTY);
      this.armor = DefaultedList.ofSize(4, ItemStack.EMPTY);
      this.offHand = DefaultedList.ofSize(1, ItemStack.EMPTY);
      this.combinedInventory = ImmutableList.of(this.main, this.armor, this.offHand);
      this.cursorStack = ItemStack.EMPTY;
      this.player = player;
   }

   public ItemStack getMainHandStack() {
      return isValidHotbarIndex(this.selectedSlot) ? (ItemStack)this.main.get(this.selectedSlot) : ItemStack.EMPTY;
   }

   public static int getHotbarSize() {
      return 9;
   }

   private boolean canStackAddMore(ItemStack existingStack, ItemStack stack) {
      return !existingStack.isEmpty() && this.areItemsEqual(existingStack, stack) && existingStack.isStackable() && existingStack.getCount() < existingStack.getMaxCount() && existingStack.getCount() < this.getMaxCountPerStack();
   }

   private boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
      return stack1.getItem() == stack2.getItem() && ItemStack.areTagsEqual(stack1, stack2);
   }

   public int getEmptySlot() {
      for(int i = 0; i < this.main.size(); ++i) {
         if (((ItemStack)this.main.get(i)).isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   @Environment(EnvType.CLIENT)
   public void addPickBlock(ItemStack stack) {
      int i = this.getSlotWithStack(stack);
      if (isValidHotbarIndex(i)) {
         this.selectedSlot = i;
      } else {
         if (i == -1) {
            this.selectedSlot = this.getSwappableHotbarSlot();
            if (!((ItemStack)this.main.get(this.selectedSlot)).isEmpty()) {
               int j = this.getEmptySlot();
               if (j != -1) {
                  this.main.set(j, this.main.get(this.selectedSlot));
               }
            }

            this.main.set(this.selectedSlot, stack);
         } else {
            this.swapSlotWithHotbar(i);
         }

      }
   }

   public void swapSlotWithHotbar(int hotbarSlot) {
      this.selectedSlot = this.getSwappableHotbarSlot();
      ItemStack itemStack = (ItemStack)this.main.get(this.selectedSlot);
      this.main.set(this.selectedSlot, this.main.get(hotbarSlot));
      this.main.set(hotbarSlot, itemStack);
   }

   public static boolean isValidHotbarIndex(int slot) {
      return slot >= 0 && slot < 9;
   }

   @Environment(EnvType.CLIENT)
   public int getSlotWithStack(ItemStack stack) {
      for(int i = 0; i < this.main.size(); ++i) {
         if (!((ItemStack)this.main.get(i)).isEmpty() && this.areItemsEqual(stack, (ItemStack)this.main.get(i))) {
            return i;
         }
      }

      return -1;
   }

   public int method_7371(ItemStack itemStack) {
      for(int i = 0; i < this.main.size(); ++i) {
         ItemStack itemStack2 = (ItemStack)this.main.get(i);
         if (!((ItemStack)this.main.get(i)).isEmpty() && this.areItemsEqual(itemStack, (ItemStack)this.main.get(i)) && !((ItemStack)this.main.get(i)).isDamaged() && !itemStack2.hasEnchantments() && !itemStack2.hasCustomName()) {
            return i;
         }
      }

      return -1;
   }

   public int getSwappableHotbarSlot() {
      int k;
      int l;
      for(k = 0; k < 9; ++k) {
         l = (this.selectedSlot + k) % 9;
         if (((ItemStack)this.main.get(l)).isEmpty()) {
            return l;
         }
      }

      for(k = 0; k < 9; ++k) {
         l = (this.selectedSlot + k) % 9;
         if (!((ItemStack)this.main.get(l)).hasEnchantments()) {
            return l;
         }
      }

      return this.selectedSlot;
   }

   @Environment(EnvType.CLIENT)
   public void scrollInHotbar(double scrollAmount) {
      if (scrollAmount > 0.0D) {
         scrollAmount = 1.0D;
      }

      if (scrollAmount < 0.0D) {
         scrollAmount = -1.0D;
      }

      for(this.selectedSlot = (int)((double)this.selectedSlot - scrollAmount); this.selectedSlot < 0; this.selectedSlot += 9) {
      }

      while(this.selectedSlot >= 9) {
         this.selectedSlot -= 9;
      }

   }

   public int remove(Predicate<ItemStack> shouldRemove, int maxCount, Inventory craftingInventory) {
      int i = 0;
      boolean bl = maxCount == 0;
      int i = i + Inventories.remove((Inventory)this, shouldRemove, maxCount - i, bl);
      i += Inventories.remove(craftingInventory, shouldRemove, maxCount - i, bl);
      i += Inventories.remove(this.cursorStack, shouldRemove, maxCount - i, bl);
      if (this.cursorStack.isEmpty()) {
         this.cursorStack = ItemStack.EMPTY;
      }

      return i;
   }

   private int addStack(ItemStack stack) {
      int i = this.getOccupiedSlotWithRoomForStack(stack);
      if (i == -1) {
         i = this.getEmptySlot();
      }

      return i == -1 ? stack.getCount() : this.addStack(i, stack);
   }

   private int addStack(int slot, ItemStack stack) {
      Item item = stack.getItem();
      int i = stack.getCount();
      ItemStack itemStack = this.getStack(slot);
      if (itemStack.isEmpty()) {
         itemStack = new ItemStack(item, 0);
         if (stack.hasTag()) {
            itemStack.setTag(stack.getTag().copy());
         }

         this.setStack(slot, itemStack);
      }

      int j = i;
      if (i > itemStack.getMaxCount() - itemStack.getCount()) {
         j = itemStack.getMaxCount() - itemStack.getCount();
      }

      if (j > this.getMaxCountPerStack() - itemStack.getCount()) {
         j = this.getMaxCountPerStack() - itemStack.getCount();
      }

      if (j == 0) {
         return i;
      } else {
         i -= j;
         itemStack.increment(j);
         itemStack.setCooldown(5);
         return i;
      }
   }

   public int getOccupiedSlotWithRoomForStack(ItemStack stack) {
      if (this.canStackAddMore(this.getStack(this.selectedSlot), stack)) {
         return this.selectedSlot;
      } else if (this.canStackAddMore(this.getStack(40), stack)) {
         return 40;
      } else {
         for(int i = 0; i < this.main.size(); ++i) {
            if (this.canStackAddMore((ItemStack)this.main.get(i), stack)) {
               return i;
            }
         }

         return -1;
      }
   }

   public void updateItems() {
      Iterator var1 = this.combinedInventory.iterator();

      while(var1.hasNext()) {
         DefaultedList<ItemStack> defaultedList = (DefaultedList)var1.next();

         for(int i = 0; i < defaultedList.size(); ++i) {
            if (!((ItemStack)defaultedList.get(i)).isEmpty()) {
               ((ItemStack)defaultedList.get(i)).inventoryTick(this.player.world, this.player, i, this.selectedSlot == i);
            }
         }
      }

   }

   public boolean insertStack(ItemStack stack) {
      return this.insertStack(-1, stack);
   }

   public boolean insertStack(int slot, ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         try {
            if (stack.isDamaged()) {
               if (slot == -1) {
                  slot = this.getEmptySlot();
               }

               if (slot >= 0) {
                  this.main.set(slot, stack.copy());
                  ((ItemStack)this.main.get(slot)).setCooldown(5);
                  stack.setCount(0);
                  return true;
               } else if (this.player.abilities.creativeMode) {
                  stack.setCount(0);
                  return true;
               } else {
                  return false;
               }
            } else {
               int i;
               do {
                  i = stack.getCount();
                  if (slot == -1) {
                     stack.setCount(this.addStack(stack));
                  } else {
                     stack.setCount(this.addStack(slot, stack));
                  }
               } while(!stack.isEmpty() && stack.getCount() < i);

               if (stack.getCount() == i && this.player.abilities.creativeMode) {
                  stack.setCount(0);
                  return true;
               } else {
                  return stack.getCount() < i;
               }
            }
         } catch (Throwable var6) {
            CrashReport crashReport = CrashReport.create(var6, "Adding item to inventory");
            CrashReportSection crashReportSection = crashReport.addElement("Item being added");
            crashReportSection.add("Item ID", (Object)Item.getRawId(stack.getItem()));
            crashReportSection.add("Item data", (Object)stack.getDamage());
            crashReportSection.add("Item name", () -> {
               return stack.getName().getString();
            });
            throw new CrashException(crashReport);
         }
      }
   }

   public void offerOrDrop(World world, ItemStack stack) {
      if (!world.isClient) {
         while(!stack.isEmpty()) {
            int i = this.getOccupiedSlotWithRoomForStack(stack);
            if (i == -1) {
               i = this.getEmptySlot();
            }

            if (i == -1) {
               this.player.dropItem(stack, false);
               break;
            }

            int j = stack.getMaxCount() - this.getStack(i).getCount();
            if (this.insertStack(i, stack.split(j))) {
               ((ServerPlayerEntity)this.player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, i, this.getStack(i)));
            }
         }

      }
   }

   public ItemStack removeStack(int slot, int amount) {
      List<ItemStack> list = null;

      DefaultedList defaultedList;
      for(Iterator var4 = this.combinedInventory.iterator(); var4.hasNext(); slot -= defaultedList.size()) {
         defaultedList = (DefaultedList)var4.next();
         if (slot < defaultedList.size()) {
            list = defaultedList;
            break;
         }
      }

      return list != null && !((ItemStack)list.get(slot)).isEmpty() ? Inventories.splitStack(list, slot, amount) : ItemStack.EMPTY;
   }

   public void removeOne(ItemStack stack) {
      Iterator var2 = this.combinedInventory.iterator();

      while(true) {
         while(var2.hasNext()) {
            DefaultedList<ItemStack> defaultedList = (DefaultedList)var2.next();

            for(int i = 0; i < defaultedList.size(); ++i) {
               if (defaultedList.get(i) == stack) {
                  defaultedList.set(i, ItemStack.EMPTY);
                  break;
               }
            }
         }

         return;
      }
   }

   public ItemStack removeStack(int slot) {
      DefaultedList<ItemStack> defaultedList = null;

      DefaultedList defaultedList2;
      for(Iterator var3 = this.combinedInventory.iterator(); var3.hasNext(); slot -= defaultedList2.size()) {
         defaultedList2 = (DefaultedList)var3.next();
         if (slot < defaultedList2.size()) {
            defaultedList = defaultedList2;
            break;
         }
      }

      if (defaultedList != null && !((ItemStack)defaultedList.get(slot)).isEmpty()) {
         ItemStack itemStack = (ItemStack)defaultedList.get(slot);
         defaultedList.set(slot, ItemStack.EMPTY);
         return itemStack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setStack(int slot, ItemStack stack) {
      DefaultedList<ItemStack> defaultedList = null;

      DefaultedList defaultedList2;
      for(Iterator var4 = this.combinedInventory.iterator(); var4.hasNext(); slot -= defaultedList2.size()) {
         defaultedList2 = (DefaultedList)var4.next();
         if (slot < defaultedList2.size()) {
            defaultedList = defaultedList2;
            break;
         }
      }

      if (defaultedList != null) {
         defaultedList.set(slot, stack);
      }

   }

   public float getBlockBreakingSpeed(BlockState block) {
      return ((ItemStack)this.main.get(this.selectedSlot)).getMiningSpeedMultiplier(block);
   }

   public ListTag serialize(ListTag tag) {
      int k;
      CompoundTag compoundTag3;
      for(k = 0; k < this.main.size(); ++k) {
         if (!((ItemStack)this.main.get(k)).isEmpty()) {
            compoundTag3 = new CompoundTag();
            compoundTag3.putByte("Slot", (byte)k);
            ((ItemStack)this.main.get(k)).toTag(compoundTag3);
            tag.add(compoundTag3);
         }
      }

      for(k = 0; k < this.armor.size(); ++k) {
         if (!((ItemStack)this.armor.get(k)).isEmpty()) {
            compoundTag3 = new CompoundTag();
            compoundTag3.putByte("Slot", (byte)(k + 100));
            ((ItemStack)this.armor.get(k)).toTag(compoundTag3);
            tag.add(compoundTag3);
         }
      }

      for(k = 0; k < this.offHand.size(); ++k) {
         if (!((ItemStack)this.offHand.get(k)).isEmpty()) {
            compoundTag3 = new CompoundTag();
            compoundTag3.putByte("Slot", (byte)(k + 150));
            ((ItemStack)this.offHand.get(k)).toTag(compoundTag3);
            tag.add(compoundTag3);
         }
      }

      return tag;
   }

   public void deserialize(ListTag tag) {
      this.main.clear();
      this.armor.clear();
      this.offHand.clear();

      for(int i = 0; i < tag.size(); ++i) {
         CompoundTag compoundTag = tag.getCompound(i);
         int j = compoundTag.getByte("Slot") & 255;
         ItemStack itemStack = ItemStack.fromTag(compoundTag);
         if (!itemStack.isEmpty()) {
            if (j >= 0 && j < this.main.size()) {
               this.main.set(j, itemStack);
            } else if (j >= 100 && j < this.armor.size() + 100) {
               this.armor.set(j - 100, itemStack);
            } else if (j >= 150 && j < this.offHand.size() + 150) {
               this.offHand.set(j - 150, itemStack);
            }
         }
      }

   }

   public int size() {
      return this.main.size() + this.armor.size() + this.offHand.size();
   }

   public boolean isEmpty() {
      Iterator var1 = this.main.iterator();

      ItemStack itemStack3;
      do {
         if (!var1.hasNext()) {
            var1 = this.armor.iterator();

            do {
               if (!var1.hasNext()) {
                  var1 = this.offHand.iterator();

                  do {
                     if (!var1.hasNext()) {
                        return true;
                     }

                     itemStack3 = (ItemStack)var1.next();
                  } while(itemStack3.isEmpty());

                  return false;
               }

               itemStack3 = (ItemStack)var1.next();
            } while(itemStack3.isEmpty());

            return false;
         }

         itemStack3 = (ItemStack)var1.next();
      } while(itemStack3.isEmpty());

      return false;
   }

   public ItemStack getStack(int slot) {
      List<ItemStack> list = null;

      DefaultedList defaultedList;
      for(Iterator var3 = this.combinedInventory.iterator(); var3.hasNext(); slot -= defaultedList.size()) {
         defaultedList = (DefaultedList)var3.next();
         if (slot < defaultedList.size()) {
            list = defaultedList;
            break;
         }
      }

      return list == null ? ItemStack.EMPTY : (ItemStack)list.get(slot);
   }

   public Text getName() {
      return new TranslatableText("container.inventory");
   }

   @Environment(EnvType.CLIENT)
   public ItemStack getArmorStack(int slot) {
      return (ItemStack)this.armor.get(slot);
   }

   public void damageArmor(DamageSource damageSource, float f) {
      if (!(f <= 0.0F)) {
         f /= 4.0F;
         if (f < 1.0F) {
            f = 1.0F;
         }

         for(int i = 0; i < this.armor.size(); ++i) {
            ItemStack itemStack = (ItemStack)this.armor.get(i);
            if ((!damageSource.isFire() || !itemStack.getItem().isFireproof()) && itemStack.getItem() instanceof ArmorItem) {
               itemStack.damage((int)f, (LivingEntity)this.player, (Consumer)((playerEntity) -> {
                  playerEntity.sendEquipmentBreakStatus(EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, i));
               }));
            }
         }

      }
   }

   public void dropAll() {
      Iterator var1 = this.combinedInventory.iterator();

      while(var1.hasNext()) {
         List<ItemStack> list = (List)var1.next();

         for(int i = 0; i < list.size(); ++i) {
            ItemStack itemStack = (ItemStack)list.get(i);
            if (!itemStack.isEmpty()) {
               this.player.dropItem(itemStack, true, false);
               list.set(i, ItemStack.EMPTY);
            }
         }
      }

   }

   public void markDirty() {
      ++this.changeCount;
   }

   @Environment(EnvType.CLIENT)
   public int getChangeCount() {
      return this.changeCount;
   }

   public void setCursorStack(ItemStack stack) {
      this.cursorStack = stack;
   }

   public ItemStack getCursorStack() {
      return this.cursorStack;
   }

   public boolean canPlayerUse(PlayerEntity player) {
      if (this.player.removed) {
         return false;
      } else {
         return !(player.squaredDistanceTo(this.player) > 64.0D);
      }
   }

   public boolean contains(ItemStack stack) {
      Iterator var2 = this.combinedInventory.iterator();

      while(var2.hasNext()) {
         List<ItemStack> list = (List)var2.next();
         Iterator var4 = list.iterator();

         while(var4.hasNext()) {
            ItemStack itemStack = (ItemStack)var4.next();
            if (!itemStack.isEmpty() && itemStack.isItemEqualIgnoreDamage(stack)) {
               return true;
            }
         }
      }

      return false;
   }

   @Environment(EnvType.CLIENT)
   public boolean contains(Tag<Item> tag) {
      Iterator var2 = this.combinedInventory.iterator();

      while(var2.hasNext()) {
         List<ItemStack> list = (List)var2.next();
         Iterator var4 = list.iterator();

         while(var4.hasNext()) {
            ItemStack itemStack = (ItemStack)var4.next();
            if (!itemStack.isEmpty() && tag.contains(itemStack.getItem())) {
               return true;
            }
         }
      }

      return false;
   }

   public void clone(PlayerInventory other) {
      for(int i = 0; i < this.size(); ++i) {
         this.setStack(i, other.getStack(i));
      }

      this.selectedSlot = other.selectedSlot;
   }

   public void clear() {
      Iterator var1 = this.combinedInventory.iterator();

      while(var1.hasNext()) {
         List<ItemStack> list = (List)var1.next();
         list.clear();
      }

   }

   public void populateRecipeFinder(RecipeFinder finder) {
      Iterator var2 = this.main.iterator();

      while(var2.hasNext()) {
         ItemStack itemStack = (ItemStack)var2.next();
         finder.addNormalItem(itemStack);
      }

   }
}
