package net.minecraft.client.options;

import com.google.common.collect.ForwardingList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.collection.DefaultedList;

@Environment(EnvType.CLIENT)
public class HotbarStorageEntry extends ForwardingList<ItemStack> {
   private final DefaultedList<ItemStack> delegate;

   public HotbarStorageEntry() {
      this.delegate = DefaultedList.ofSize(PlayerInventory.getHotbarSize(), ItemStack.EMPTY);
   }

   protected List<ItemStack> delegate() {
      return this.delegate;
   }

   public ListTag toListTag() {
      ListTag listTag = new ListTag();
      Iterator var2 = this.delegate().iterator();

      while(var2.hasNext()) {
         ItemStack itemStack = (ItemStack)var2.next();
         listTag.add(itemStack.toTag(new CompoundTag()));
      }

      return listTag;
   }

   public void fromListTag(ListTag listTag) {
      List<ItemStack> list = this.delegate();

      for(int i = 0; i < list.size(); ++i) {
         list.set(i, ItemStack.fromTag(listTag.getCompound(i)));
      }

   }

   public boolean isEmpty() {
      Iterator var1 = this.delegate().iterator();

      ItemStack itemStack;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         itemStack = (ItemStack)var1.next();
      } while(itemStack.isEmpty());

      return false;
   }
}
