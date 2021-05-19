package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Nameable;
import org.jetbrains.annotations.Nullable;

public abstract class LockableContainerBlockEntity extends BlockEntity implements Inventory, NamedScreenHandlerFactory, Nameable {
   private ContainerLock lock;
   private Text customName;

   protected LockableContainerBlockEntity(BlockEntityType<?> blockEntityType) {
      super(blockEntityType);
      this.lock = ContainerLock.EMPTY;
   }

   public void fromTag(BlockState state, CompoundTag tag) {
      super.fromTag(state, tag);
      this.lock = ContainerLock.fromTag(tag);
      if (tag.contains("CustomName", 8)) {
         this.customName = Text.Serializer.fromJson(tag.getString("CustomName"));
      }

   }

   public CompoundTag toTag(CompoundTag tag) {
      super.toTag(tag);
      this.lock.toTag(tag);
      if (this.customName != null) {
         tag.putString("CustomName", Text.Serializer.toJson(this.customName));
      }

      return tag;
   }

   public void setCustomName(Text customName) {
      this.customName = customName;
   }

   public Text getName() {
      return this.customName != null ? this.customName : this.getContainerName();
   }

   public Text getDisplayName() {
      return this.getName();
   }

   @Nullable
   public Text getCustomName() {
      return this.customName;
   }

   protected abstract Text getContainerName();

   public boolean checkUnlocked(PlayerEntity player) {
      return checkUnlocked(player, this.lock, this.getDisplayName());
   }

   public static boolean checkUnlocked(PlayerEntity player, ContainerLock lock, Text containerName) {
      if (!player.isSpectator() && !lock.canOpen(player.getMainHandStack())) {
         player.sendMessage(new TranslatableText("container.isLocked", new Object[]{containerName}), true);
         player.playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
      return this.checkUnlocked(playerEntity) ? this.createScreenHandler(i, playerInventory) : null;
   }

   protected abstract ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory);
}
