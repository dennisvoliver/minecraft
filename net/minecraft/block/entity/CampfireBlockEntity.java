package net.minecraft.block.entity;

import java.util.Optional;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Clearable;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CampfireBlockEntity extends BlockEntity implements Clearable, Tickable {
   private final DefaultedList<ItemStack> itemsBeingCooked;
   private final int[] cookingTimes;
   private final int[] cookingTotalTimes;

   public CampfireBlockEntity() {
      super(BlockEntityType.CAMPFIRE);
      this.itemsBeingCooked = DefaultedList.ofSize(4, ItemStack.EMPTY);
      this.cookingTimes = new int[4];
      this.cookingTotalTimes = new int[4];
   }

   public void tick() {
      boolean bl = (Boolean)this.getCachedState().get(CampfireBlock.LIT);
      boolean bl2 = this.world.isClient;
      if (bl2) {
         if (bl) {
            this.spawnSmokeParticles();
         }

      } else {
         if (bl) {
            this.updateItemsBeingCooked();
         } else {
            for(int i = 0; i < this.itemsBeingCooked.size(); ++i) {
               if (this.cookingTimes[i] > 0) {
                  this.cookingTimes[i] = MathHelper.clamp(this.cookingTimes[i] - 2, 0, this.cookingTotalTimes[i]);
               }
            }
         }

      }
   }

   private void updateItemsBeingCooked() {
      for(int i = 0; i < this.itemsBeingCooked.size(); ++i) {
         ItemStack itemStack = (ItemStack)this.itemsBeingCooked.get(i);
         if (!itemStack.isEmpty()) {
            int var10002 = this.cookingTimes[i]++;
            if (this.cookingTimes[i] >= this.cookingTotalTimes[i]) {
               Inventory inventory = new SimpleInventory(new ItemStack[]{itemStack});
               ItemStack itemStack2 = (ItemStack)this.world.getRecipeManager().getFirstMatch(RecipeType.CAMPFIRE_COOKING, inventory, this.world).map((campfireCookingRecipe) -> {
                  return campfireCookingRecipe.craft(inventory);
               }).orElse(itemStack);
               BlockPos blockPos = this.getPos();
               ItemScatterer.spawn(this.world, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack2);
               this.itemsBeingCooked.set(i, ItemStack.EMPTY);
               this.updateListeners();
            }
         }
      }

   }

   private void spawnSmokeParticles() {
      World world = this.getWorld();
      if (world != null) {
         BlockPos blockPos = this.getPos();
         Random random = world.random;
         int j;
         if (random.nextFloat() < 0.11F) {
            for(j = 0; j < random.nextInt(2) + 2; ++j) {
               CampfireBlock.spawnSmokeParticle(world, blockPos, (Boolean)this.getCachedState().get(CampfireBlock.SIGNAL_FIRE), false);
            }
         }

         j = ((Direction)this.getCachedState().get(CampfireBlock.FACING)).getHorizontal();

         for(int k = 0; k < this.itemsBeingCooked.size(); ++k) {
            if (!((ItemStack)this.itemsBeingCooked.get(k)).isEmpty() && random.nextFloat() < 0.2F) {
               Direction direction = Direction.fromHorizontal(Math.floorMod(k + j, 4));
               float f = 0.3125F;
               double d = (double)blockPos.getX() + 0.5D - (double)((float)direction.getOffsetX() * 0.3125F) + (double)((float)direction.rotateYClockwise().getOffsetX() * 0.3125F);
               double e = (double)blockPos.getY() + 0.5D;
               double g = (double)blockPos.getZ() + 0.5D - (double)((float)direction.getOffsetZ() * 0.3125F) + (double)((float)direction.rotateYClockwise().getOffsetZ() * 0.3125F);

               for(int l = 0; l < 4; ++l) {
                  world.addParticle(ParticleTypes.SMOKE, d, e, g, 0.0D, 5.0E-4D, 0.0D);
               }
            }
         }

      }
   }

   public DefaultedList<ItemStack> getItemsBeingCooked() {
      return this.itemsBeingCooked;
   }

   public void fromTag(BlockState state, CompoundTag tag) {
      super.fromTag(state, tag);
      this.itemsBeingCooked.clear();
      Inventories.fromTag(tag, this.itemsBeingCooked);
      int[] js;
      if (tag.contains("CookingTimes", 11)) {
         js = tag.getIntArray("CookingTimes");
         System.arraycopy(js, 0, this.cookingTimes, 0, Math.min(this.cookingTotalTimes.length, js.length));
      }

      if (tag.contains("CookingTotalTimes", 11)) {
         js = tag.getIntArray("CookingTotalTimes");
         System.arraycopy(js, 0, this.cookingTotalTimes, 0, Math.min(this.cookingTotalTimes.length, js.length));
      }

   }

   public CompoundTag toTag(CompoundTag tag) {
      this.saveInitialChunkData(tag);
      tag.putIntArray("CookingTimes", this.cookingTimes);
      tag.putIntArray("CookingTotalTimes", this.cookingTotalTimes);
      return tag;
   }

   private CompoundTag saveInitialChunkData(CompoundTag tag) {
      super.toTag(tag);
      Inventories.toTag(tag, this.itemsBeingCooked, true);
      return tag;
   }

   @Nullable
   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return new BlockEntityUpdateS2CPacket(this.pos, 13, this.toInitialChunkDataTag());
   }

   public CompoundTag toInitialChunkDataTag() {
      return this.saveInitialChunkData(new CompoundTag());
   }

   public Optional<CampfireCookingRecipe> getRecipeFor(ItemStack item) {
      return this.itemsBeingCooked.stream().noneMatch(ItemStack::isEmpty) ? Optional.empty() : this.world.getRecipeManager().getFirstMatch(RecipeType.CAMPFIRE_COOKING, new SimpleInventory(new ItemStack[]{item}), this.world);
   }

   public boolean addItem(ItemStack item, int integer) {
      for(int i = 0; i < this.itemsBeingCooked.size(); ++i) {
         ItemStack itemStack = (ItemStack)this.itemsBeingCooked.get(i);
         if (itemStack.isEmpty()) {
            this.cookingTotalTimes[i] = integer;
            this.cookingTimes[i] = 0;
            this.itemsBeingCooked.set(i, item.split(1));
            this.updateListeners();
            return true;
         }
      }

      return false;
   }

   private void updateListeners() {
      this.markDirty();
      this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), 3);
   }

   public void clear() {
      this.itemsBeingCooked.clear();
   }

   public void spawnItemsBeingCooked() {
      if (this.world != null) {
         if (!this.world.isClient) {
            ItemScatterer.spawn(this.world, this.getPos(), this.getItemsBeingCooked());
         }

         this.updateListeners();
      }

   }
}
