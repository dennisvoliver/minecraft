package net.minecraft.block.entity;

import java.util.Random;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public abstract class LootableContainerBlockEntity extends LockableContainerBlockEntity {
   @Nullable
   protected Identifier lootTableId;
   protected long lootTableSeed;

   protected LootableContainerBlockEntity(BlockEntityType<?> blockEntityType) {
      super(blockEntityType);
   }

   public static void setLootTable(BlockView world, Random random, BlockPos pos, Identifier id) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof LootableContainerBlockEntity) {
         ((LootableContainerBlockEntity)blockEntity).setLootTable(id, random.nextLong());
      }

   }

   protected boolean deserializeLootTable(CompoundTag compoundTag) {
      if (compoundTag.contains("LootTable", 8)) {
         this.lootTableId = new Identifier(compoundTag.getString("LootTable"));
         this.lootTableSeed = compoundTag.getLong("LootTableSeed");
         return true;
      } else {
         return false;
      }
   }

   protected boolean serializeLootTable(CompoundTag compoundTag) {
      if (this.lootTableId == null) {
         return false;
      } else {
         compoundTag.putString("LootTable", this.lootTableId.toString());
         if (this.lootTableSeed != 0L) {
            compoundTag.putLong("LootTableSeed", this.lootTableSeed);
         }

         return true;
      }
   }

   public void checkLootInteraction(@Nullable PlayerEntity player) {
      if (this.lootTableId != null && this.world.getServer() != null) {
         LootTable lootTable = this.world.getServer().getLootManager().getTable(this.lootTableId);
         if (player instanceof ServerPlayerEntity) {
            Criteria.PLAYER_GENERATES_CONTAINER_LOOT.test((ServerPlayerEntity)player, this.lootTableId);
         }

         this.lootTableId = null;
         LootContext.Builder builder = (new LootContext.Builder((ServerWorld)this.world)).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.pos)).random(this.lootTableSeed);
         if (player != null) {
            builder.luck(player.getLuck()).parameter(LootContextParameters.THIS_ENTITY, player);
         }

         lootTable.supplyInventory(this, builder.build(LootContextTypes.CHEST));
      }

   }

   public void setLootTable(Identifier id, long seed) {
      this.lootTableId = id;
      this.lootTableSeed = seed;
   }

   public boolean isEmpty() {
      this.checkLootInteraction((PlayerEntity)null);
      return this.getInvStackList().stream().allMatch(ItemStack::isEmpty);
   }

   public ItemStack getStack(int slot) {
      this.checkLootInteraction((PlayerEntity)null);
      return (ItemStack)this.getInvStackList().get(slot);
   }

   public ItemStack removeStack(int slot, int amount) {
      this.checkLootInteraction((PlayerEntity)null);
      ItemStack itemStack = Inventories.splitStack(this.getInvStackList(), slot, amount);
      if (!itemStack.isEmpty()) {
         this.markDirty();
      }

      return itemStack;
   }

   public ItemStack removeStack(int slot) {
      this.checkLootInteraction((PlayerEntity)null);
      return Inventories.removeStack(this.getInvStackList(), slot);
   }

   public void setStack(int slot, ItemStack stack) {
      this.checkLootInteraction((PlayerEntity)null);
      this.getInvStackList().set(slot, stack);
      if (stack.getCount() > this.getMaxCountPerStack()) {
         stack.setCount(this.getMaxCountPerStack());
      }

      this.markDirty();
   }

   public boolean canPlayerUse(PlayerEntity player) {
      if (this.world.getBlockEntity(this.pos) != this) {
         return false;
      } else {
         return !(player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) > 64.0D);
      }
   }

   public void clear() {
      this.getInvStackList().clear();
   }

   protected abstract DefaultedList<ItemStack> getInvStackList();

   protected abstract void setInvStackList(DefaultedList<ItemStack> list);

   public boolean checkUnlocked(PlayerEntity player) {
      return super.checkUnlocked(player) && (this.lootTableId == null || !player.isSpectator());
   }

   @Nullable
   public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
      if (this.checkUnlocked(playerEntity)) {
         this.checkLootInteraction(playerInventory.player);
         return this.createScreenHandler(i, playerInventory);
      } else {
         return null;
      }
   }
}
