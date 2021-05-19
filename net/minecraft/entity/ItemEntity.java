package net.minecraft.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ItemEntity extends Entity {
   private static final TrackedData<ItemStack> STACK;
   private int age;
   private int pickupDelay;
   private int health;
   private UUID thrower;
   private UUID owner;
   public final float hoverHeight;

   public ItemEntity(EntityType<? extends ItemEntity> entityType, World world) {
      super(entityType, world);
      this.health = 5;
      this.hoverHeight = (float)(Math.random() * 3.141592653589793D * 2.0D);
   }

   public ItemEntity(World world, double x, double y, double z) {
      this(EntityType.ITEM, world);
      this.updatePosition(x, y, z);
      this.yaw = this.random.nextFloat() * 360.0F;
      this.setVelocity(this.random.nextDouble() * 0.2D - 0.1D, 0.2D, this.random.nextDouble() * 0.2D - 0.1D);
   }

   public ItemEntity(World world, double x, double y, double z, ItemStack stack) {
      this(world, x, y, z);
      this.setStack(stack);
   }

   @Environment(EnvType.CLIENT)
   private ItemEntity(ItemEntity itemEntity) {
      super(itemEntity.getType(), itemEntity.world);
      this.health = 5;
      this.setStack(itemEntity.getStack().copy());
      this.copyPositionAndRotation(itemEntity);
      this.age = itemEntity.age;
      this.hoverHeight = itemEntity.hoverHeight;
   }

   protected boolean canClimb() {
      return false;
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(STACK, ItemStack.EMPTY);
   }

   public void tick() {
      if (this.getStack().isEmpty()) {
         this.remove();
      } else {
         super.tick();
         if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
            --this.pickupDelay;
         }

         this.prevX = this.getX();
         this.prevY = this.getY();
         this.prevZ = this.getZ();
         Vec3d vec3d = this.getVelocity();
         float f = this.getStandingEyeHeight() - 0.11111111F;
         if (this.isTouchingWater() && this.getFluidHeight(FluidTags.WATER) > (double)f) {
            this.applyBuoyancy();
         } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double)f) {
            this.method_24348();
         } else if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0D, -0.04D, 0.0D));
         }

         if (this.world.isClient) {
            this.noClip = false;
         } else {
            this.noClip = !this.world.isSpaceEmpty(this);
            if (this.noClip) {
               this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
            }
         }

         if (!this.onGround || squaredHorizontalLength(this.getVelocity()) > 9.999999747378752E-6D || (this.age + this.getEntityId()) % 4 == 0) {
            this.move(MovementType.SELF, this.getVelocity());
            float g = 0.98F;
            if (this.onGround) {
               g = this.world.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ())).getBlock().getSlipperiness() * 0.98F;
            }

            this.setVelocity(this.getVelocity().multiply((double)g, 0.98D, (double)g));
            if (this.onGround) {
               Vec3d vec3d2 = this.getVelocity();
               if (vec3d2.y < 0.0D) {
                  this.setVelocity(vec3d2.multiply(1.0D, -0.5D, 1.0D));
               }
            }
         }

         boolean bl = MathHelper.floor(this.prevX) != MathHelper.floor(this.getX()) || MathHelper.floor(this.prevY) != MathHelper.floor(this.getY()) || MathHelper.floor(this.prevZ) != MathHelper.floor(this.getZ());
         int i = bl ? 2 : 40;
         if (this.age % i == 0) {
            if (this.world.getFluidState(this.getBlockPos()).isIn(FluidTags.LAVA) && !this.isFireImmune()) {
               this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
            }

            if (!this.world.isClient && this.canMerge()) {
               this.tryMerge();
            }
         }

         if (this.age != -32768) {
            ++this.age;
         }

         this.velocityDirty |= this.updateWaterState();
         if (!this.world.isClient) {
            double d = this.getVelocity().subtract(vec3d).lengthSquared();
            if (d > 0.01D) {
               this.velocityDirty = true;
            }
         }

         if (!this.world.isClient && this.age >= 6000) {
            this.remove();
         }

      }
   }

   private void applyBuoyancy() {
      Vec3d vec3d = this.getVelocity();
      this.setVelocity(vec3d.x * 0.9900000095367432D, vec3d.y + (double)(vec3d.y < 0.05999999865889549D ? 5.0E-4F : 0.0F), vec3d.z * 0.9900000095367432D);
   }

   private void method_24348() {
      Vec3d vec3d = this.getVelocity();
      this.setVelocity(vec3d.x * 0.949999988079071D, vec3d.y + (double)(vec3d.y < 0.05999999865889549D ? 5.0E-4F : 0.0F), vec3d.z * 0.949999988079071D);
   }

   private void tryMerge() {
      if (this.canMerge()) {
         List<ItemEntity> list = this.world.getEntitiesByClass(ItemEntity.class, this.getBoundingBox().expand(0.5D, 0.0D, 0.5D), (itemEntityx) -> {
            return itemEntityx != this && itemEntityx.canMerge();
         });
         Iterator var2 = list.iterator();

         while(var2.hasNext()) {
            ItemEntity itemEntity = (ItemEntity)var2.next();
            if (itemEntity.canMerge()) {
               this.tryMerge(itemEntity);
               if (this.removed) {
                  break;
               }
            }
         }

      }
   }

   private boolean canMerge() {
      ItemStack itemStack = this.getStack();
      return this.isAlive() && this.pickupDelay != 32767 && this.age != -32768 && this.age < 6000 && itemStack.getCount() < itemStack.getMaxCount();
   }

   private void tryMerge(ItemEntity other) {
      ItemStack itemStack = this.getStack();
      ItemStack itemStack2 = other.getStack();
      if (Objects.equals(this.getOwner(), other.getOwner()) && canMerge(itemStack, itemStack2)) {
         if (itemStack2.getCount() < itemStack.getCount()) {
            merge(this, itemStack, other, itemStack2);
         } else {
            merge(other, itemStack2, this, itemStack);
         }

      }
   }

   public static boolean canMerge(ItemStack stack1, ItemStack stack2) {
      if (stack2.getItem() != stack1.getItem()) {
         return false;
      } else if (stack2.getCount() + stack1.getCount() > stack2.getMaxCount()) {
         return false;
      } else if (stack2.hasTag() ^ stack1.hasTag()) {
         return false;
      } else {
         return !stack2.hasTag() || stack2.getTag().equals(stack1.getTag());
      }
   }

   public static ItemStack merge(ItemStack stack1, ItemStack stack2, int maxCount) {
      int i = Math.min(Math.min(stack1.getMaxCount(), maxCount) - stack1.getCount(), stack2.getCount());
      ItemStack itemStack = stack1.copy();
      itemStack.increment(i);
      stack2.decrement(i);
      return itemStack;
   }

   private static void merge(ItemEntity targetEntity, ItemStack stack1, ItemStack stack2) {
      ItemStack itemStack = merge(stack1, stack2, 64);
      targetEntity.setStack(itemStack);
   }

   private static void merge(ItemEntity targetEntity, ItemStack targetStack, ItemEntity sourceEntity, ItemStack sourceStack) {
      merge(targetEntity, targetStack, sourceStack);
      targetEntity.pickupDelay = Math.max(targetEntity.pickupDelay, sourceEntity.pickupDelay);
      targetEntity.age = Math.min(targetEntity.age, sourceEntity.age);
      if (sourceStack.isEmpty()) {
         sourceEntity.remove();
      }

   }

   public boolean isFireImmune() {
      return this.getStack().getItem().isFireproof() || super.isFireImmune();
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else if (!this.getStack().isEmpty() && this.getStack().getItem() == Items.NETHER_STAR && source.isExplosive()) {
         return false;
      } else if (!this.getStack().getItem().damage(source)) {
         return false;
      } else {
         this.scheduleVelocityUpdate();
         this.health = (int)((float)this.health - amount);
         if (this.health <= 0) {
            this.remove();
         }

         return false;
      }
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      tag.putShort("Health", (short)this.health);
      tag.putShort("Age", (short)this.age);
      tag.putShort("PickupDelay", (short)this.pickupDelay);
      if (this.getThrower() != null) {
         tag.putUuid("Thrower", this.getThrower());
      }

      if (this.getOwner() != null) {
         tag.putUuid("Owner", this.getOwner());
      }

      if (!this.getStack().isEmpty()) {
         tag.put("Item", this.getStack().toTag(new CompoundTag()));
      }

   }

   public void readCustomDataFromTag(CompoundTag tag) {
      this.health = tag.getShort("Health");
      this.age = tag.getShort("Age");
      if (tag.contains("PickupDelay")) {
         this.pickupDelay = tag.getShort("PickupDelay");
      }

      if (tag.containsUuid("Owner")) {
         this.owner = tag.getUuid("Owner");
      }

      if (tag.containsUuid("Thrower")) {
         this.thrower = tag.getUuid("Thrower");
      }

      CompoundTag compoundTag = tag.getCompound("Item");
      this.setStack(ItemStack.fromTag(compoundTag));
      if (this.getStack().isEmpty()) {
         this.remove();
      }

   }

   public void onPlayerCollision(PlayerEntity player) {
      if (!this.world.isClient) {
         ItemStack itemStack = this.getStack();
         Item item = itemStack.getItem();
         int i = itemStack.getCount();
         if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUuid())) && player.inventory.insertStack(itemStack)) {
            player.sendPickup(this, i);
            if (itemStack.isEmpty()) {
               this.remove();
               itemStack.setCount(i);
            }

            player.increaseStat(Stats.PICKED_UP.getOrCreateStat(item), i);
            player.method_29499(this);
         }

      }
   }

   public Text getName() {
      Text text = this.getCustomName();
      return (Text)(text != null ? text : new TranslatableText(this.getStack().getTranslationKey()));
   }

   public boolean isAttackable() {
      return false;
   }

   @Nullable
   public Entity moveToWorld(ServerWorld destination) {
      Entity entity = super.moveToWorld(destination);
      if (!this.world.isClient && entity instanceof ItemEntity) {
         ((ItemEntity)entity).tryMerge();
      }

      return entity;
   }

   public ItemStack getStack() {
      return (ItemStack)this.getDataTracker().get(STACK);
   }

   public void setStack(ItemStack stack) {
      this.getDataTracker().set(STACK, stack);
   }

   public void onTrackedDataSet(TrackedData<?> data) {
      super.onTrackedDataSet(data);
      if (STACK.equals(data)) {
         this.getStack().setHolder(this);
      }

   }

   @Nullable
   public UUID getOwner() {
      return this.owner;
   }

   public void setOwner(@Nullable UUID uuid) {
      this.owner = uuid;
   }

   @Nullable
   public UUID getThrower() {
      return this.thrower;
   }

   public void setThrower(@Nullable UUID uuid) {
      this.thrower = uuid;
   }

   @Environment(EnvType.CLIENT)
   public int getAge() {
      return this.age;
   }

   public void setToDefaultPickupDelay() {
      this.pickupDelay = 10;
   }

   public void resetPickupDelay() {
      this.pickupDelay = 0;
   }

   public void setPickupDelayInfinite() {
      this.pickupDelay = 32767;
   }

   public void setPickupDelay(int pickupDelay) {
      this.pickupDelay = pickupDelay;
   }

   public boolean cannotPickup() {
      return this.pickupDelay > 0;
   }

   public void setCovetedItem() {
      this.age = -6000;
   }

   public void setDespawnImmediately() {
      this.setPickupDelayInfinite();
      this.age = 5999;
   }

   @Environment(EnvType.CLIENT)
   public float method_27314(float f) {
      return ((float)this.getAge() + f) / 20.0F + this.hoverHeight;
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }

   @Environment(EnvType.CLIENT)
   public ItemEntity method_29271() {
      return new ItemEntity(this);
   }

   static {
      STACK = DataTracker.registerData(ItemEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
   }
}
