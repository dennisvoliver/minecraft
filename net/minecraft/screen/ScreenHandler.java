package net.minecraft.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class ScreenHandler {
   /**
    * A list of item stacks that is used for tracking changes in {@link #sendContentUpdates()}.
    */
   private final DefaultedList<ItemStack> trackedStacks = DefaultedList.of();
   public final List<Slot> slots = Lists.newArrayList();
   private final List<Property> properties = Lists.newArrayList();
   @Nullable
   private final ScreenHandlerType<?> type;
   public final int syncId;
   @Environment(EnvType.CLIENT)
   private short actionId;
   private int quickCraftStage = -1;
   private int quickCraftButton;
   private final Set<Slot> quickCraftSlots = Sets.newHashSet();
   private final List<ScreenHandlerListener> listeners = Lists.newArrayList();
   private final Set<PlayerEntity> restrictedPlayers = Sets.newHashSet();

   protected ScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId) {
      this.type = type;
      this.syncId = syncId;
   }

   protected static boolean canUse(ScreenHandlerContext context, PlayerEntity player, Block block) {
      return (Boolean)context.run((world, blockPos) -> {
         return !world.getBlockState(blockPos).isOf(block) ? false : player.squaredDistanceTo((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D) <= 64.0D;
      }, true);
   }

   public ScreenHandlerType<?> getType() {
      if (this.type == null) {
         throw new UnsupportedOperationException("Unable to construct this menu by type");
      } else {
         return this.type;
      }
   }

   /**
    * Checks that the size of the provided inventory is at least as large as the {@code expectedSize}.
    * 
    * @throws IllegalArgumentException if the inventory size is smaller than {@code expectedSize}
    */
   protected static void checkSize(Inventory inventory, int expectedSize) {
      int i = inventory.size();
      if (i < expectedSize) {
         throw new IllegalArgumentException("Container size " + i + " is smaller than expected " + expectedSize);
      }
   }

   /**
    * Checks that the size of the {@code data} is at least as large as the {@code expectedCount}.
    * 
    * @throws IllegalArgumentException if the {@code data} has a smaller size than {@code expectedCount}
    */
   protected static void checkDataCount(PropertyDelegate data, int expectedCount) {
      int i = data.size();
      if (i < expectedCount) {
         throw new IllegalArgumentException("Container data count " + i + " is smaller than expected " + expectedCount);
      }
   }

   protected Slot addSlot(Slot slot) {
      slot.id = this.slots.size();
      this.slots.add(slot);
      this.trackedStacks.add(ItemStack.EMPTY);
      return slot;
   }

   protected Property addProperty(Property property) {
      this.properties.add(property);
      return property;
   }

   protected void addProperties(PropertyDelegate propertyDelegate) {
      for(int i = 0; i < propertyDelegate.size(); ++i) {
         this.addProperty(Property.create(propertyDelegate, i));
      }

   }

   public void addListener(ScreenHandlerListener listener) {
      if (!this.listeners.contains(listener)) {
         this.listeners.add(listener);
         listener.onHandlerRegistered(this, this.getStacks());
         this.sendContentUpdates();
      }
   }

   @Environment(EnvType.CLIENT)
   public void removeListener(ScreenHandlerListener listener) {
      this.listeners.remove(listener);
   }

   public DefaultedList<ItemStack> getStacks() {
      DefaultedList<ItemStack> defaultedList = DefaultedList.of();

      for(int i = 0; i < this.slots.size(); ++i) {
         defaultedList.add(((Slot)this.slots.get(i)).getStack());
      }

      return defaultedList;
   }

   /**
    * Sends updates to listeners if any properties or slot stacks have changed.
    */
   public void sendContentUpdates() {
      int j;
      for(j = 0; j < this.slots.size(); ++j) {
         ItemStack itemStack = ((Slot)this.slots.get(j)).getStack();
         ItemStack itemStack2 = (ItemStack)this.trackedStacks.get(j);
         if (!ItemStack.areEqual(itemStack2, itemStack)) {
            ItemStack itemStack3 = itemStack.copy();
            this.trackedStacks.set(j, itemStack3);
            Iterator var5 = this.listeners.iterator();

            while(var5.hasNext()) {
               ScreenHandlerListener screenHandlerListener = (ScreenHandlerListener)var5.next();
               screenHandlerListener.onSlotUpdate(this, j, itemStack3);
            }
         }
      }

      for(j = 0; j < this.properties.size(); ++j) {
         Property property = (Property)this.properties.get(j);
         if (property.hasChanged()) {
            Iterator var8 = this.listeners.iterator();

            while(var8.hasNext()) {
               ScreenHandlerListener screenHandlerListener2 = (ScreenHandlerListener)var8.next();
               screenHandlerListener2.onPropertyUpdate(this, j, property.get());
            }
         }
      }

   }

   public boolean onButtonClick(PlayerEntity player, int id) {
      return false;
   }

   public Slot getSlot(int index) {
      return (Slot)this.slots.get(index);
   }

   public ItemStack transferSlot(PlayerEntity player, int index) {
      Slot slot = (Slot)this.slots.get(index);
      return slot != null ? slot.getStack() : ItemStack.EMPTY;
   }

   /**
    * Performs a slot click. This can behave in many different ways depending mainly on the action type.
    * @return The stack that was clicked on before anything changed, used mostly for verifying that the client and server are in sync
    * 
    * @param actionType The type of slot click. Check the docs for each SlotActionType value for details
    */
   public ItemStack onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
      try {
         return this.method_30010(i, j, actionType, playerEntity);
      } catch (Exception var8) {
         CrashReport crashReport = CrashReport.create(var8, "Container click");
         CrashReportSection crashReportSection = crashReport.addElement("Click info");
         crashReportSection.add("Menu Type", () -> {
            return this.type != null ? Registry.SCREEN_HANDLER.getId(this.type).toString() : "<no type>";
         });
         crashReportSection.add("Menu Class", () -> {
            return this.getClass().getCanonicalName();
         });
         crashReportSection.add("Slot Count", (Object)this.slots.size());
         crashReportSection.add("Slot", (Object)i);
         crashReportSection.add("Button", (Object)j);
         crashReportSection.add("Type", (Object)actionType);
         throw new CrashException(crashReport);
      }
   }

   private ItemStack method_30010(int i, int j, SlotActionType slotActionType, PlayerEntity playerEntity) {
      ItemStack itemStack = ItemStack.EMPTY;
      PlayerInventory playerInventory = playerEntity.inventory;
      ItemStack itemStack7;
      ItemStack itemStack8;
      int n;
      int l;
      if (slotActionType == SlotActionType.QUICK_CRAFT) {
         int k = this.quickCraftButton;
         this.quickCraftButton = unpackQuickCraftStage(j);
         if ((k != 1 || this.quickCraftButton != 2) && k != this.quickCraftButton) {
            this.endQuickCraft();
         } else if (playerInventory.getCursorStack().isEmpty()) {
            this.endQuickCraft();
         } else if (this.quickCraftButton == 0) {
            this.quickCraftStage = unpackQuickCraftButton(j);
            if (shouldQuickCraftContinue(this.quickCraftStage, playerEntity)) {
               this.quickCraftButton = 1;
               this.quickCraftSlots.clear();
            } else {
               this.endQuickCraft();
            }
         } else if (this.quickCraftButton == 1) {
            Slot slot = (Slot)this.slots.get(i);
            itemStack8 = playerInventory.getCursorStack();
            if (slot != null && canInsertItemIntoSlot(slot, itemStack8, true) && slot.canInsert(itemStack8) && (this.quickCraftStage == 2 || itemStack8.getCount() > this.quickCraftSlots.size()) && this.canInsertIntoSlot(slot)) {
               this.quickCraftSlots.add(slot);
            }
         } else if (this.quickCraftButton == 2) {
            if (!this.quickCraftSlots.isEmpty()) {
               itemStack7 = playerInventory.getCursorStack().copy();
               l = playerInventory.getCursorStack().getCount();
               Iterator var23 = this.quickCraftSlots.iterator();

               label336:
               while(true) {
                  Slot slot2;
                  ItemStack itemStack4;
                  do {
                     do {
                        do {
                           do {
                              if (!var23.hasNext()) {
                                 itemStack7.setCount(l);
                                 playerInventory.setCursorStack(itemStack7);
                                 break label336;
                              }

                              slot2 = (Slot)var23.next();
                              itemStack4 = playerInventory.getCursorStack();
                           } while(slot2 == null);
                        } while(!canInsertItemIntoSlot(slot2, itemStack4, true));
                     } while(!slot2.canInsert(itemStack4));
                  } while(this.quickCraftStage != 2 && itemStack4.getCount() < this.quickCraftSlots.size());

                  if (this.canInsertIntoSlot(slot2)) {
                     ItemStack itemStack5 = itemStack7.copy();
                     int m = slot2.hasStack() ? slot2.getStack().getCount() : 0;
                     calculateStackSize(this.quickCraftSlots, this.quickCraftStage, itemStack5, m);
                     n = Math.min(itemStack5.getMaxCount(), slot2.getMaxItemCount(itemStack5));
                     if (itemStack5.getCount() > n) {
                        itemStack5.setCount(n);
                     }

                     l -= itemStack5.getCount() - m;
                     slot2.setStack(itemStack5);
                  }
               }
            }

            this.endQuickCraft();
         } else {
            this.endQuickCraft();
         }
      } else if (this.quickCraftButton != 0) {
         this.endQuickCraft();
      } else {
         Slot slot4;
         int q;
         if ((slotActionType == SlotActionType.PICKUP || slotActionType == SlotActionType.QUICK_MOVE) && (j == 0 || j == 1)) {
            if (i == -999) {
               if (!playerInventory.getCursorStack().isEmpty()) {
                  if (j == 0) {
                     playerEntity.dropItem(playerInventory.getCursorStack(), true);
                     playerInventory.setCursorStack(ItemStack.EMPTY);
                  }

                  if (j == 1) {
                     playerEntity.dropItem(playerInventory.getCursorStack().split(1), true);
                  }
               }
            } else if (slotActionType == SlotActionType.QUICK_MOVE) {
               if (i < 0) {
                  return ItemStack.EMPTY;
               }

               slot4 = (Slot)this.slots.get(i);
               if (slot4 == null || !slot4.canTakeItems(playerEntity)) {
                  return ItemStack.EMPTY;
               }

               for(itemStack7 = this.transferSlot(playerEntity, i); !itemStack7.isEmpty() && ItemStack.areItemsEqualIgnoreDamage(slot4.getStack(), itemStack7); itemStack7 = this.transferSlot(playerEntity, i)) {
                  itemStack = itemStack7.copy();
               }
            } else {
               if (i < 0) {
                  return ItemStack.EMPTY;
               }

               slot4 = (Slot)this.slots.get(i);
               if (slot4 != null) {
                  itemStack7 = slot4.getStack();
                  itemStack8 = playerInventory.getCursorStack();
                  if (!itemStack7.isEmpty()) {
                     itemStack = itemStack7.copy();
                  }

                  if (itemStack7.isEmpty()) {
                     if (!itemStack8.isEmpty() && slot4.canInsert(itemStack8)) {
                        q = j == 0 ? itemStack8.getCount() : 1;
                        if (q > slot4.getMaxItemCount(itemStack8)) {
                           q = slot4.getMaxItemCount(itemStack8);
                        }

                        slot4.setStack(itemStack8.split(q));
                     }
                  } else if (slot4.canTakeItems(playerEntity)) {
                     if (itemStack8.isEmpty()) {
                        if (itemStack7.isEmpty()) {
                           slot4.setStack(ItemStack.EMPTY);
                           playerInventory.setCursorStack(ItemStack.EMPTY);
                        } else {
                           q = j == 0 ? itemStack7.getCount() : (itemStack7.getCount() + 1) / 2;
                           playerInventory.setCursorStack(slot4.takeStack(q));
                           if (itemStack7.isEmpty()) {
                              slot4.setStack(ItemStack.EMPTY);
                           }

                           slot4.onTakeItem(playerEntity, playerInventory.getCursorStack());
                        }
                     } else if (slot4.canInsert(itemStack8)) {
                        if (canStacksCombine(itemStack7, itemStack8)) {
                           q = j == 0 ? itemStack8.getCount() : 1;
                           if (q > slot4.getMaxItemCount(itemStack8) - itemStack7.getCount()) {
                              q = slot4.getMaxItemCount(itemStack8) - itemStack7.getCount();
                           }

                           if (q > itemStack8.getMaxCount() - itemStack7.getCount()) {
                              q = itemStack8.getMaxCount() - itemStack7.getCount();
                           }

                           itemStack8.decrement(q);
                           itemStack7.increment(q);
                        } else if (itemStack8.getCount() <= slot4.getMaxItemCount(itemStack8)) {
                           slot4.setStack(itemStack8);
                           playerInventory.setCursorStack(itemStack7);
                        }
                     } else if (itemStack8.getMaxCount() > 1 && canStacksCombine(itemStack7, itemStack8) && !itemStack7.isEmpty()) {
                        q = itemStack7.getCount();
                        if (q + itemStack8.getCount() <= itemStack8.getMaxCount()) {
                           itemStack8.increment(q);
                           itemStack7 = slot4.takeStack(q);
                           if (itemStack7.isEmpty()) {
                              slot4.setStack(ItemStack.EMPTY);
                           }

                           slot4.onTakeItem(playerEntity, playerInventory.getCursorStack());
                        }
                     }
                  }

                  slot4.markDirty();
               }
            }
         } else if (slotActionType == SlotActionType.SWAP) {
            slot4 = (Slot)this.slots.get(i);
            itemStack7 = playerInventory.getStack(j);
            itemStack8 = slot4.getStack();
            if (!itemStack7.isEmpty() || !itemStack8.isEmpty()) {
               if (itemStack7.isEmpty()) {
                  if (slot4.canTakeItems(playerEntity)) {
                     playerInventory.setStack(j, itemStack8);
                     slot4.onTake(itemStack8.getCount());
                     slot4.setStack(ItemStack.EMPTY);
                     slot4.onTakeItem(playerEntity, itemStack8);
                  }
               } else if (itemStack8.isEmpty()) {
                  if (slot4.canInsert(itemStack7)) {
                     q = slot4.getMaxItemCount(itemStack7);
                     if (itemStack7.getCount() > q) {
                        slot4.setStack(itemStack7.split(q));
                     } else {
                        slot4.setStack(itemStack7);
                        playerInventory.setStack(j, ItemStack.EMPTY);
                     }
                  }
               } else if (slot4.canTakeItems(playerEntity) && slot4.canInsert(itemStack7)) {
                  q = slot4.getMaxItemCount(itemStack7);
                  if (itemStack7.getCount() > q) {
                     slot4.setStack(itemStack7.split(q));
                     slot4.onTakeItem(playerEntity, itemStack8);
                     if (!playerInventory.insertStack(itemStack8)) {
                        playerEntity.dropItem(itemStack8, true);
                     }
                  } else {
                     slot4.setStack(itemStack7);
                     playerInventory.setStack(j, itemStack8);
                     slot4.onTakeItem(playerEntity, itemStack8);
                  }
               }
            }
         } else if (slotActionType == SlotActionType.CLONE && playerEntity.abilities.creativeMode && playerInventory.getCursorStack().isEmpty() && i >= 0) {
            slot4 = (Slot)this.slots.get(i);
            if (slot4 != null && slot4.hasStack()) {
               itemStack7 = slot4.getStack().copy();
               itemStack7.setCount(itemStack7.getMaxCount());
               playerInventory.setCursorStack(itemStack7);
            }
         } else if (slotActionType == SlotActionType.THROW && playerInventory.getCursorStack().isEmpty() && i >= 0) {
            slot4 = (Slot)this.slots.get(i);
            if (slot4 != null && slot4.hasStack() && slot4.canTakeItems(playerEntity)) {
               itemStack7 = slot4.takeStack(j == 0 ? 1 : slot4.getStack().getCount());
               slot4.onTakeItem(playerEntity, itemStack7);
               playerEntity.dropItem(itemStack7, true);
            }
         } else if (slotActionType == SlotActionType.PICKUP_ALL && i >= 0) {
            slot4 = (Slot)this.slots.get(i);
            itemStack7 = playerInventory.getCursorStack();
            if (!itemStack7.isEmpty() && (slot4 == null || !slot4.hasStack() || !slot4.canTakeItems(playerEntity))) {
               l = j == 0 ? 0 : this.slots.size() - 1;
               q = j == 0 ? 1 : -1;

               for(int w = 0; w < 2; ++w) {
                  for(int x = l; x >= 0 && x < this.slots.size() && itemStack7.getCount() < itemStack7.getMaxCount(); x += q) {
                     Slot slot9 = (Slot)this.slots.get(x);
                     if (slot9.hasStack() && canInsertItemIntoSlot(slot9, itemStack7, true) && slot9.canTakeItems(playerEntity) && this.canInsertIntoSlot(itemStack7, slot9)) {
                        ItemStack itemStack14 = slot9.getStack();
                        if (w != 0 || itemStack14.getCount() != itemStack14.getMaxCount()) {
                           n = Math.min(itemStack7.getMaxCount() - itemStack7.getCount(), itemStack14.getCount());
                           ItemStack itemStack15 = slot9.takeStack(n);
                           itemStack7.increment(n);
                           if (itemStack15.isEmpty()) {
                              slot9.setStack(ItemStack.EMPTY);
                           }

                           slot9.onTakeItem(playerEntity, itemStack15);
                        }
                     }
                  }
               }
            }

            this.sendContentUpdates();
         }
      }

      return itemStack;
   }

   public static boolean canStacksCombine(ItemStack first, ItemStack second) {
      return first.getItem() == second.getItem() && ItemStack.areTagsEqual(first, second);
   }

   public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
      return true;
   }

   public void close(PlayerEntity player) {
      PlayerInventory playerInventory = player.inventory;
      if (!playerInventory.getCursorStack().isEmpty()) {
         player.dropItem(playerInventory.getCursorStack(), false);
         playerInventory.setCursorStack(ItemStack.EMPTY);
      }

   }

   protected void dropInventory(PlayerEntity player, World world, Inventory inventory) {
      int j;
      if (!player.isAlive() || player instanceof ServerPlayerEntity && ((ServerPlayerEntity)player).isDisconnected()) {
         for(j = 0; j < inventory.size(); ++j) {
            player.dropItem(inventory.removeStack(j), false);
         }

      } else {
         for(j = 0; j < inventory.size(); ++j) {
            player.inventory.offerOrDrop(world, inventory.removeStack(j));
         }

      }
   }

   public void onContentChanged(Inventory inventory) {
      this.sendContentUpdates();
   }

   public void setStackInSlot(int slot, ItemStack stack) {
      this.getSlot(slot).setStack(stack);
   }

   @Environment(EnvType.CLIENT)
   public void updateSlotStacks(List<ItemStack> stacks) {
      for(int i = 0; i < stacks.size(); ++i) {
         this.getSlot(i).setStack((ItemStack)stacks.get(i));
      }

   }

   public void setProperty(int id, int value) {
      ((Property)this.properties.get(id)).set(value);
   }

   @Environment(EnvType.CLIENT)
   public short getNextActionId(PlayerInventory playerInventory) {
      ++this.actionId;
      return this.actionId;
   }

   public boolean isNotRestricted(PlayerEntity player) {
      return !this.restrictedPlayers.contains(player);
   }

   public void setPlayerRestriction(PlayerEntity player, boolean unrestricted) {
      if (unrestricted) {
         this.restrictedPlayers.remove(player);
      } else {
         this.restrictedPlayers.add(player);
      }

   }

   public abstract boolean canUse(PlayerEntity player);

   protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
      boolean bl = false;
      int i = startIndex;
      if (fromLast) {
         i = endIndex - 1;
      }

      Slot slot2;
      ItemStack itemStack;
      if (stack.isStackable()) {
         while(!stack.isEmpty()) {
            if (fromLast) {
               if (i < startIndex) {
                  break;
               }
            } else if (i >= endIndex) {
               break;
            }

            slot2 = (Slot)this.slots.get(i);
            itemStack = slot2.getStack();
            if (!itemStack.isEmpty() && canStacksCombine(stack, itemStack)) {
               int j = itemStack.getCount() + stack.getCount();
               if (j <= stack.getMaxCount()) {
                  stack.setCount(0);
                  itemStack.setCount(j);
                  slot2.markDirty();
                  bl = true;
               } else if (itemStack.getCount() < stack.getMaxCount()) {
                  stack.decrement(stack.getMaxCount() - itemStack.getCount());
                  itemStack.setCount(stack.getMaxCount());
                  slot2.markDirty();
                  bl = true;
               }
            }

            if (fromLast) {
               --i;
            } else {
               ++i;
            }
         }
      }

      if (!stack.isEmpty()) {
         if (fromLast) {
            i = endIndex - 1;
         } else {
            i = startIndex;
         }

         while(true) {
            if (fromLast) {
               if (i < startIndex) {
                  break;
               }
            } else if (i >= endIndex) {
               break;
            }

            slot2 = (Slot)this.slots.get(i);
            itemStack = slot2.getStack();
            if (itemStack.isEmpty() && slot2.canInsert(stack)) {
               if (stack.getCount() > slot2.getMaxItemCount()) {
                  slot2.setStack(stack.split(slot2.getMaxItemCount()));
               } else {
                  slot2.setStack(stack.split(stack.getCount()));
               }

               slot2.markDirty();
               bl = true;
               break;
            }

            if (fromLast) {
               --i;
            } else {
               ++i;
            }
         }
      }

      return bl;
   }

   public static int unpackQuickCraftButton(int quickCraftData) {
      return quickCraftData >> 2 & 3;
   }

   public static int unpackQuickCraftStage(int quickCraftData) {
      return quickCraftData & 3;
   }

   @Environment(EnvType.CLIENT)
   public static int packQuickCraftData(int quickCraftStage, int buttonId) {
      return quickCraftStage & 3 | (buttonId & 3) << 2;
   }

   public static boolean shouldQuickCraftContinue(int stage, PlayerEntity player) {
      if (stage == 0) {
         return true;
      } else if (stage == 1) {
         return true;
      } else {
         return stage == 2 && player.abilities.creativeMode;
      }
   }

   protected void endQuickCraft() {
      this.quickCraftButton = 0;
      this.quickCraftSlots.clear();
   }

   public static boolean canInsertItemIntoSlot(@Nullable Slot slot, ItemStack stack, boolean allowOverflow) {
      boolean bl = slot == null || !slot.hasStack();
      if (!bl && stack.isItemEqualIgnoreDamage(slot.getStack()) && ItemStack.areTagsEqual(slot.getStack(), stack)) {
         return slot.getStack().getCount() + (allowOverflow ? 0 : stack.getCount()) <= stack.getMaxCount();
      } else {
         return bl;
      }
   }

   public static void calculateStackSize(Set<Slot> slots, int mode, ItemStack stack, int stackSize) {
      switch(mode) {
      case 0:
         stack.setCount(MathHelper.floor((float)stack.getCount() / (float)slots.size()));
         break;
      case 1:
         stack.setCount(1);
         break;
      case 2:
         stack.setCount(stack.getItem().getMaxCount());
      }

      stack.increment(stackSize);
   }

   public boolean canInsertIntoSlot(Slot slot) {
      return true;
   }

   public static int calculateComparatorOutput(@Nullable BlockEntity entity) {
      return entity instanceof Inventory ? calculateComparatorOutput((Inventory)entity) : 0;
   }

   public static int calculateComparatorOutput(@Nullable Inventory inventory) {
      if (inventory == null) {
         return 0;
      } else {
         int i = 0;
         float f = 0.0F;

         for(int j = 0; j < inventory.size(); ++j) {
            ItemStack itemStack = inventory.getStack(j);
            if (!itemStack.isEmpty()) {
               f += (float)itemStack.getCount() / (float)Math.min(inventory.getMaxCountPerStack(), itemStack.getMaxCount());
               ++i;
            }
         }

         f /= (float)inventory.size();
         return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
      }
   }
}
