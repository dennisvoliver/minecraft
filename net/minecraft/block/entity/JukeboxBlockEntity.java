package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Clearable;

public class JukeboxBlockEntity extends BlockEntity implements Clearable {
   private ItemStack record;

   public JukeboxBlockEntity() {
      super(BlockEntityType.JUKEBOX);
      this.record = ItemStack.EMPTY;
   }

   public void fromTag(BlockState state, CompoundTag tag) {
      super.fromTag(state, tag);
      if (tag.contains("RecordItem", 10)) {
         this.setRecord(ItemStack.fromTag(tag.getCompound("RecordItem")));
      }

   }

   public CompoundTag toTag(CompoundTag tag) {
      super.toTag(tag);
      if (!this.getRecord().isEmpty()) {
         tag.put("RecordItem", this.getRecord().toTag(new CompoundTag()));
      }

      return tag;
   }

   public ItemStack getRecord() {
      return this.record;
   }

   public void setRecord(ItemStack stack) {
      this.record = stack;
      this.markDirty();
   }

   public void clear() {
      this.setRecord(ItemStack.EMPTY);
   }
}
