package net.minecraft.entity.decoration;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ArmorStandEntity extends LivingEntity {
   private static final EulerAngle DEFAULT_HEAD_ROTATION = new EulerAngle(0.0F, 0.0F, 0.0F);
   private static final EulerAngle DEFAULT_BODY_ROTATION = new EulerAngle(0.0F, 0.0F, 0.0F);
   private static final EulerAngle DEFAULT_LEFT_ARM_ROTATION = new EulerAngle(-10.0F, 0.0F, -10.0F);
   private static final EulerAngle DEFAULT_RIGHT_ARM_ROTATION = new EulerAngle(-15.0F, 0.0F, 10.0F);
   private static final EulerAngle DEFAULT_LEFT_LEG_ROTATION = new EulerAngle(-1.0F, 0.0F, -1.0F);
   private static final EulerAngle DEFAULT_RIGHT_LEG_ROTATION = new EulerAngle(1.0F, 0.0F, 1.0F);
   private static final EntityDimensions field_26745 = new EntityDimensions(0.0F, 0.0F, true);
   private static final EntityDimensions field_26746;
   public static final TrackedData<Byte> ARMOR_STAND_FLAGS;
   public static final TrackedData<EulerAngle> TRACKER_HEAD_ROTATION;
   public static final TrackedData<EulerAngle> TRACKER_BODY_ROTATION;
   public static final TrackedData<EulerAngle> TRACKER_LEFT_ARM_ROTATION;
   public static final TrackedData<EulerAngle> TRACKER_RIGHT_ARM_ROTATION;
   public static final TrackedData<EulerAngle> TRACKER_LEFT_LEG_ROTATION;
   public static final TrackedData<EulerAngle> TRACKER_RIGHT_LEG_ROTATION;
   private static final Predicate<Entity> RIDEABLE_MINECART_PREDICATE;
   private final DefaultedList<ItemStack> heldItems;
   private final DefaultedList<ItemStack> armorItems;
   private boolean invisible;
   public long lastHitTime;
   private int disabledSlots;
   private EulerAngle headRotation;
   private EulerAngle bodyRotation;
   private EulerAngle leftArmRotation;
   private EulerAngle rightArmRotation;
   private EulerAngle leftLegRotation;
   private EulerAngle rightLegRotation;

   public ArmorStandEntity(EntityType<? extends ArmorStandEntity> entityType, World world) {
      super(entityType, world);
      this.heldItems = DefaultedList.ofSize(2, ItemStack.EMPTY);
      this.armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
      this.headRotation = DEFAULT_HEAD_ROTATION;
      this.bodyRotation = DEFAULT_BODY_ROTATION;
      this.leftArmRotation = DEFAULT_LEFT_ARM_ROTATION;
      this.rightArmRotation = DEFAULT_RIGHT_ARM_ROTATION;
      this.leftLegRotation = DEFAULT_LEFT_LEG_ROTATION;
      this.rightLegRotation = DEFAULT_RIGHT_LEG_ROTATION;
      this.stepHeight = 0.0F;
   }

   public ArmorStandEntity(World world, double x, double y, double z) {
      this(EntityType.ARMOR_STAND, world);
      this.updatePosition(x, y, z);
   }

   public void calculateDimensions() {
      double d = this.getX();
      double e = this.getY();
      double f = this.getZ();
      super.calculateDimensions();
      this.updatePosition(d, e, f);
   }

   private boolean canClip() {
      return !this.isMarker() && !this.hasNoGravity();
   }

   public boolean canMoveVoluntarily() {
      return super.canMoveVoluntarily() && this.canClip();
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(ARMOR_STAND_FLAGS, (byte)0);
      this.dataTracker.startTracking(TRACKER_HEAD_ROTATION, DEFAULT_HEAD_ROTATION);
      this.dataTracker.startTracking(TRACKER_BODY_ROTATION, DEFAULT_BODY_ROTATION);
      this.dataTracker.startTracking(TRACKER_LEFT_ARM_ROTATION, DEFAULT_LEFT_ARM_ROTATION);
      this.dataTracker.startTracking(TRACKER_RIGHT_ARM_ROTATION, DEFAULT_RIGHT_ARM_ROTATION);
      this.dataTracker.startTracking(TRACKER_LEFT_LEG_ROTATION, DEFAULT_LEFT_LEG_ROTATION);
      this.dataTracker.startTracking(TRACKER_RIGHT_LEG_ROTATION, DEFAULT_RIGHT_LEG_ROTATION);
   }

   public Iterable<ItemStack> getItemsHand() {
      return this.heldItems;
   }

   public Iterable<ItemStack> getArmorItems() {
      return this.armorItems;
   }

   public ItemStack getEquippedStack(EquipmentSlot slot) {
      switch(slot.getType()) {
      case HAND:
         return (ItemStack)this.heldItems.get(slot.getEntitySlotId());
      case ARMOR:
         return (ItemStack)this.armorItems.get(slot.getEntitySlotId());
      default:
         return ItemStack.EMPTY;
      }
   }

   public void equipStack(EquipmentSlot slot, ItemStack stack) {
      switch(slot.getType()) {
      case HAND:
         this.onEquipStack(stack);
         this.heldItems.set(slot.getEntitySlotId(), stack);
         break;
      case ARMOR:
         this.onEquipStack(stack);
         this.armorItems.set(slot.getEntitySlotId(), stack);
      }

   }

   public boolean equip(int slot, ItemStack item) {
      EquipmentSlot equipmentSlot7;
      if (slot == 98) {
         equipmentSlot7 = EquipmentSlot.MAINHAND;
      } else if (slot == 99) {
         equipmentSlot7 = EquipmentSlot.OFFHAND;
      } else if (slot == 100 + EquipmentSlot.HEAD.getEntitySlotId()) {
         equipmentSlot7 = EquipmentSlot.HEAD;
      } else if (slot == 100 + EquipmentSlot.CHEST.getEntitySlotId()) {
         equipmentSlot7 = EquipmentSlot.CHEST;
      } else if (slot == 100 + EquipmentSlot.LEGS.getEntitySlotId()) {
         equipmentSlot7 = EquipmentSlot.LEGS;
      } else {
         if (slot != 100 + EquipmentSlot.FEET.getEntitySlotId()) {
            return false;
         }

         equipmentSlot7 = EquipmentSlot.FEET;
      }

      if (!item.isEmpty() && !MobEntity.canEquipmentSlotContain(equipmentSlot7, item) && equipmentSlot7 != EquipmentSlot.HEAD) {
         return false;
      } else {
         this.equipStack(equipmentSlot7, item);
         return true;
      }
   }

   public boolean canEquip(ItemStack stack) {
      EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(stack);
      return this.getEquippedStack(equipmentSlot).isEmpty() && !this.isSlotDisabled(equipmentSlot);
   }

   public void writeCustomDataToTag(CompoundTag tag) {
      super.writeCustomDataToTag(tag);
      ListTag listTag = new ListTag();

      CompoundTag compoundTag;
      for(Iterator var3 = this.armorItems.iterator(); var3.hasNext(); listTag.add(compoundTag)) {
         ItemStack itemStack = (ItemStack)var3.next();
         compoundTag = new CompoundTag();
         if (!itemStack.isEmpty()) {
            itemStack.toTag(compoundTag);
         }
      }

      tag.put("ArmorItems", listTag);
      ListTag listTag2 = new ListTag();

      CompoundTag compoundTag2;
      for(Iterator var8 = this.heldItems.iterator(); var8.hasNext(); listTag2.add(compoundTag2)) {
         ItemStack itemStack2 = (ItemStack)var8.next();
         compoundTag2 = new CompoundTag();
         if (!itemStack2.isEmpty()) {
            itemStack2.toTag(compoundTag2);
         }
      }

      tag.put("HandItems", listTag2);
      tag.putBoolean("Invisible", this.isInvisible());
      tag.putBoolean("Small", this.isSmall());
      tag.putBoolean("ShowArms", this.shouldShowArms());
      tag.putInt("DisabledSlots", this.disabledSlots);
      tag.putBoolean("NoBasePlate", this.shouldHideBasePlate());
      if (this.isMarker()) {
         tag.putBoolean("Marker", this.isMarker());
      }

      tag.put("Pose", this.serializePose());
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      super.readCustomDataFromTag(tag);
      ListTag listTag2;
      int j;
      if (tag.contains("ArmorItems", 9)) {
         listTag2 = tag.getList("ArmorItems", 10);

         for(j = 0; j < this.armorItems.size(); ++j) {
            this.armorItems.set(j, ItemStack.fromTag(listTag2.getCompound(j)));
         }
      }

      if (tag.contains("HandItems", 9)) {
         listTag2 = tag.getList("HandItems", 10);

         for(j = 0; j < this.heldItems.size(); ++j) {
            this.heldItems.set(j, ItemStack.fromTag(listTag2.getCompound(j)));
         }
      }

      this.setInvisible(tag.getBoolean("Invisible"));
      this.setSmall(tag.getBoolean("Small"));
      this.setShowArms(tag.getBoolean("ShowArms"));
      this.disabledSlots = tag.getInt("DisabledSlots");
      this.setHideBasePlate(tag.getBoolean("NoBasePlate"));
      this.setMarker(tag.getBoolean("Marker"));
      this.noClip = !this.canClip();
      CompoundTag compoundTag = tag.getCompound("Pose");
      this.deserializePose(compoundTag);
   }

   private void deserializePose(CompoundTag compoundTag) {
      ListTag listTag = compoundTag.getList("Head", 5);
      this.setHeadRotation(listTag.isEmpty() ? DEFAULT_HEAD_ROTATION : new EulerAngle(listTag));
      ListTag listTag2 = compoundTag.getList("Body", 5);
      this.setBodyRotation(listTag2.isEmpty() ? DEFAULT_BODY_ROTATION : new EulerAngle(listTag2));
      ListTag listTag3 = compoundTag.getList("LeftArm", 5);
      this.setLeftArmRotation(listTag3.isEmpty() ? DEFAULT_LEFT_ARM_ROTATION : new EulerAngle(listTag3));
      ListTag listTag4 = compoundTag.getList("RightArm", 5);
      this.setRightArmRotation(listTag4.isEmpty() ? DEFAULT_RIGHT_ARM_ROTATION : new EulerAngle(listTag4));
      ListTag listTag5 = compoundTag.getList("LeftLeg", 5);
      this.setLeftLegRotation(listTag5.isEmpty() ? DEFAULT_LEFT_LEG_ROTATION : new EulerAngle(listTag5));
      ListTag listTag6 = compoundTag.getList("RightLeg", 5);
      this.setRightLegRotation(listTag6.isEmpty() ? DEFAULT_RIGHT_LEG_ROTATION : new EulerAngle(listTag6));
   }

   private CompoundTag serializePose() {
      CompoundTag compoundTag = new CompoundTag();
      if (!DEFAULT_HEAD_ROTATION.equals(this.headRotation)) {
         compoundTag.put("Head", this.headRotation.serialize());
      }

      if (!DEFAULT_BODY_ROTATION.equals(this.bodyRotation)) {
         compoundTag.put("Body", this.bodyRotation.serialize());
      }

      if (!DEFAULT_LEFT_ARM_ROTATION.equals(this.leftArmRotation)) {
         compoundTag.put("LeftArm", this.leftArmRotation.serialize());
      }

      if (!DEFAULT_RIGHT_ARM_ROTATION.equals(this.rightArmRotation)) {
         compoundTag.put("RightArm", this.rightArmRotation.serialize());
      }

      if (!DEFAULT_LEFT_LEG_ROTATION.equals(this.leftLegRotation)) {
         compoundTag.put("LeftLeg", this.leftLegRotation.serialize());
      }

      if (!DEFAULT_RIGHT_LEG_ROTATION.equals(this.rightLegRotation)) {
         compoundTag.put("RightLeg", this.rightLegRotation.serialize());
      }

      return compoundTag;
   }

   public boolean isPushable() {
      return false;
   }

   protected void pushAway(Entity entity) {
   }

   protected void tickCramming() {
      List<Entity> list = this.world.getOtherEntities(this, this.getBoundingBox(), RIDEABLE_MINECART_PREDICATE);

      for(int i = 0; i < list.size(); ++i) {
         Entity entity = (Entity)list.get(i);
         if (this.squaredDistanceTo(entity) <= 0.2D) {
            entity.pushAwayFrom(this);
         }
      }

   }

   public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (!this.isMarker() && itemStack.getItem() != Items.NAME_TAG) {
         if (player.isSpectator()) {
            return ActionResult.SUCCESS;
         } else if (player.world.isClient) {
            return ActionResult.CONSUME;
         } else {
            EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(itemStack);
            if (itemStack.isEmpty()) {
               EquipmentSlot equipmentSlot2 = this.slotFromPosition(hitPos);
               EquipmentSlot equipmentSlot3 = this.isSlotDisabled(equipmentSlot2) ? equipmentSlot : equipmentSlot2;
               if (this.hasStackEquipped(equipmentSlot3) && this.equip(player, equipmentSlot3, itemStack, hand)) {
                  return ActionResult.SUCCESS;
               }
            } else {
               if (this.isSlotDisabled(equipmentSlot)) {
                  return ActionResult.FAIL;
               }

               if (equipmentSlot.getType() == EquipmentSlot.Type.HAND && !this.shouldShowArms()) {
                  return ActionResult.FAIL;
               }

               if (this.equip(player, equipmentSlot, itemStack, hand)) {
                  return ActionResult.SUCCESS;
               }
            }

            return ActionResult.PASS;
         }
      } else {
         return ActionResult.PASS;
      }
   }

   private EquipmentSlot slotFromPosition(Vec3d vec3d) {
      EquipmentSlot equipmentSlot = EquipmentSlot.MAINHAND;
      boolean bl = this.isSmall();
      double d = bl ? vec3d.y * 2.0D : vec3d.y;
      EquipmentSlot equipmentSlot2 = EquipmentSlot.FEET;
      if (d >= 0.1D && d < 0.1D + (bl ? 0.8D : 0.45D) && this.hasStackEquipped(equipmentSlot2)) {
         equipmentSlot = EquipmentSlot.FEET;
      } else if (d >= 0.9D + (bl ? 0.3D : 0.0D) && d < 0.9D + (bl ? 1.0D : 0.7D) && this.hasStackEquipped(EquipmentSlot.CHEST)) {
         equipmentSlot = EquipmentSlot.CHEST;
      } else if (d >= 0.4D && d < 0.4D + (bl ? 1.0D : 0.8D) && this.hasStackEquipped(EquipmentSlot.LEGS)) {
         equipmentSlot = EquipmentSlot.LEGS;
      } else if (d >= 1.6D && this.hasStackEquipped(EquipmentSlot.HEAD)) {
         equipmentSlot = EquipmentSlot.HEAD;
      } else if (!this.hasStackEquipped(EquipmentSlot.MAINHAND) && this.hasStackEquipped(EquipmentSlot.OFFHAND)) {
         equipmentSlot = EquipmentSlot.OFFHAND;
      }

      return equipmentSlot;
   }

   private boolean isSlotDisabled(EquipmentSlot slot) {
      return (this.disabledSlots & 1 << slot.getArmorStandSlotId()) != 0 || slot.getType() == EquipmentSlot.Type.HAND && !this.shouldShowArms();
   }

   private boolean equip(PlayerEntity player, EquipmentSlot slot, ItemStack stack, Hand hand) {
      ItemStack itemStack = this.getEquippedStack(slot);
      if (!itemStack.isEmpty() && (this.disabledSlots & 1 << slot.getArmorStandSlotId() + 8) != 0) {
         return false;
      } else if (itemStack.isEmpty() && (this.disabledSlots & 1 << slot.getArmorStandSlotId() + 16) != 0) {
         return false;
      } else {
         ItemStack itemStack3;
         if (player.abilities.creativeMode && itemStack.isEmpty() && !stack.isEmpty()) {
            itemStack3 = stack.copy();
            itemStack3.setCount(1);
            this.equipStack(slot, itemStack3);
            return true;
         } else if (!stack.isEmpty() && stack.getCount() > 1) {
            if (!itemStack.isEmpty()) {
               return false;
            } else {
               itemStack3 = stack.copy();
               itemStack3.setCount(1);
               this.equipStack(slot, itemStack3);
               stack.decrement(1);
               return true;
            }
         } else {
            this.equipStack(slot, stack);
            player.setStackInHand(hand, itemStack);
            return true;
         }
      }
   }

   public boolean damage(DamageSource source, float amount) {
      if (!this.world.isClient && !this.removed) {
         if (DamageSource.OUT_OF_WORLD.equals(source)) {
            this.remove();
            return false;
         } else if (!this.isInvulnerableTo(source) && !this.invisible && !this.isMarker()) {
            if (source.isExplosive()) {
               this.onBreak(source);
               this.remove();
               return false;
            } else if (DamageSource.IN_FIRE.equals(source)) {
               if (this.isOnFire()) {
                  this.updateHealth(source, 0.15F);
               } else {
                  this.setOnFireFor(5);
               }

               return false;
            } else if (DamageSource.ON_FIRE.equals(source) && this.getHealth() > 0.5F) {
               this.updateHealth(source, 4.0F);
               return false;
            } else {
               boolean bl = source.getSource() instanceof PersistentProjectileEntity;
               boolean bl2 = bl && ((PersistentProjectileEntity)source.getSource()).getPierceLevel() > 0;
               boolean bl3 = "player".equals(source.getName());
               if (!bl3 && !bl) {
                  return false;
               } else if (source.getAttacker() instanceof PlayerEntity && !((PlayerEntity)source.getAttacker()).abilities.allowModifyWorld) {
                  return false;
               } else if (source.isSourceCreativePlayer()) {
                  this.playBreakSound();
                  this.spawnBreakParticles();
                  this.remove();
                  return bl2;
               } else {
                  long l = this.world.getTime();
                  if (l - this.lastHitTime > 5L && !bl) {
                     this.world.sendEntityStatus(this, (byte)32);
                     this.lastHitTime = l;
                  } else {
                     this.breakAndDropItem(source);
                     this.spawnBreakParticles();
                     this.remove();
                  }

                  return true;
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 32) {
         if (this.world.isClient) {
            this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_HIT, this.getSoundCategory(), 0.3F, 1.0F, false);
            this.lastHitTime = this.world.getTime();
         }
      } else {
         super.handleStatus(status);
      }

   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      double d = this.getBoundingBox().getAverageSideLength() * 4.0D;
      if (Double.isNaN(d) || d == 0.0D) {
         d = 4.0D;
      }

      d *= 64.0D;
      return distance < d * d;
   }

   private void spawnBreakParticles() {
      if (this.world instanceof ServerWorld) {
         ((ServerWorld)this.world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.getDefaultState()), this.getX(), this.getBodyY(0.6666666666666666D), this.getZ(), 10, (double)(this.getWidth() / 4.0F), (double)(this.getHeight() / 4.0F), (double)(this.getWidth() / 4.0F), 0.05D);
      }

   }

   private void updateHealth(DamageSource damageSource, float amount) {
      float f = this.getHealth();
      f -= amount;
      if (f <= 0.5F) {
         this.onBreak(damageSource);
         this.remove();
      } else {
         this.setHealth(f);
      }

   }

   private void breakAndDropItem(DamageSource damageSource) {
      Block.dropStack(this.world, this.getBlockPos(), new ItemStack(Items.ARMOR_STAND));
      this.onBreak(damageSource);
   }

   private void onBreak(DamageSource damageSource) {
      this.playBreakSound();
      this.drop(damageSource);

      int j;
      ItemStack itemStack2;
      for(j = 0; j < this.heldItems.size(); ++j) {
         itemStack2 = (ItemStack)this.heldItems.get(j);
         if (!itemStack2.isEmpty()) {
            Block.dropStack(this.world, this.getBlockPos().up(), itemStack2);
            this.heldItems.set(j, ItemStack.EMPTY);
         }
      }

      for(j = 0; j < this.armorItems.size(); ++j) {
         itemStack2 = (ItemStack)this.armorItems.get(j);
         if (!itemStack2.isEmpty()) {
            Block.dropStack(this.world, this.getBlockPos().up(), itemStack2);
            this.armorItems.set(j, ItemStack.EMPTY);
         }
      }

   }

   private void playBreakSound() {
      this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_BREAK, this.getSoundCategory(), 1.0F, 1.0F);
   }

   protected float turnHead(float bodyRotation, float headRotation) {
      this.prevBodyYaw = this.prevYaw;
      this.bodyYaw = this.yaw;
      return 0.0F;
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return dimensions.height * (this.isBaby() ? 0.5F : 0.9F);
   }

   public double getHeightOffset() {
      return this.isMarker() ? 0.0D : 0.10000000149011612D;
   }

   public void travel(Vec3d movementInput) {
      if (this.canClip()) {
         super.travel(movementInput);
      }
   }

   public void setYaw(float yaw) {
      this.prevBodyYaw = this.prevYaw = yaw;
      this.prevHeadYaw = this.headYaw = yaw;
   }

   public void setHeadYaw(float headYaw) {
      this.prevBodyYaw = this.prevYaw = headYaw;
      this.prevHeadYaw = this.headYaw = headYaw;
   }

   public void tick() {
      super.tick();
      EulerAngle eulerAngle = (EulerAngle)this.dataTracker.get(TRACKER_HEAD_ROTATION);
      if (!this.headRotation.equals(eulerAngle)) {
         this.setHeadRotation(eulerAngle);
      }

      EulerAngle eulerAngle2 = (EulerAngle)this.dataTracker.get(TRACKER_BODY_ROTATION);
      if (!this.bodyRotation.equals(eulerAngle2)) {
         this.setBodyRotation(eulerAngle2);
      }

      EulerAngle eulerAngle3 = (EulerAngle)this.dataTracker.get(TRACKER_LEFT_ARM_ROTATION);
      if (!this.leftArmRotation.equals(eulerAngle3)) {
         this.setLeftArmRotation(eulerAngle3);
      }

      EulerAngle eulerAngle4 = (EulerAngle)this.dataTracker.get(TRACKER_RIGHT_ARM_ROTATION);
      if (!this.rightArmRotation.equals(eulerAngle4)) {
         this.setRightArmRotation(eulerAngle4);
      }

      EulerAngle eulerAngle5 = (EulerAngle)this.dataTracker.get(TRACKER_LEFT_LEG_ROTATION);
      if (!this.leftLegRotation.equals(eulerAngle5)) {
         this.setLeftLegRotation(eulerAngle5);
      }

      EulerAngle eulerAngle6 = (EulerAngle)this.dataTracker.get(TRACKER_RIGHT_LEG_ROTATION);
      if (!this.rightLegRotation.equals(eulerAngle6)) {
         this.setRightLegRotation(eulerAngle6);
      }

   }

   protected void updatePotionVisibility() {
      this.setInvisible(this.invisible);
   }

   public void setInvisible(boolean invisible) {
      this.invisible = invisible;
      super.setInvisible(invisible);
   }

   public boolean isBaby() {
      return this.isSmall();
   }

   public void kill() {
      this.remove();
   }

   public boolean isImmuneToExplosion() {
      return this.isInvisible();
   }

   public PistonBehavior getPistonBehavior() {
      return this.isMarker() ? PistonBehavior.IGNORE : super.getPistonBehavior();
   }

   private void setSmall(boolean small) {
      this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS), 1, small));
   }

   public boolean isSmall() {
      return ((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS) & 1) != 0;
   }

   private void setShowArms(boolean showArms) {
      this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS), 4, showArms));
   }

   public boolean shouldShowArms() {
      return ((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS) & 4) != 0;
   }

   private void setHideBasePlate(boolean hideBasePlate) {
      this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS), 8, hideBasePlate));
   }

   public boolean shouldHideBasePlate() {
      return ((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS) & 8) != 0;
   }

   private void setMarker(boolean marker) {
      this.dataTracker.set(ARMOR_STAND_FLAGS, this.setBitField((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS), 16, marker));
   }

   public boolean isMarker() {
      return ((Byte)this.dataTracker.get(ARMOR_STAND_FLAGS) & 16) != 0;
   }

   private byte setBitField(byte value, int bitField, boolean set) {
      if (set) {
         value = (byte)(value | bitField);
      } else {
         value = (byte)(value & ~bitField);
      }

      return value;
   }

   public void setHeadRotation(EulerAngle eulerAngle) {
      this.headRotation = eulerAngle;
      this.dataTracker.set(TRACKER_HEAD_ROTATION, eulerAngle);
   }

   public void setBodyRotation(EulerAngle eulerAngle) {
      this.bodyRotation = eulerAngle;
      this.dataTracker.set(TRACKER_BODY_ROTATION, eulerAngle);
   }

   public void setLeftArmRotation(EulerAngle eulerAngle) {
      this.leftArmRotation = eulerAngle;
      this.dataTracker.set(TRACKER_LEFT_ARM_ROTATION, eulerAngle);
   }

   public void setRightArmRotation(EulerAngle eulerAngle) {
      this.rightArmRotation = eulerAngle;
      this.dataTracker.set(TRACKER_RIGHT_ARM_ROTATION, eulerAngle);
   }

   public void setLeftLegRotation(EulerAngle eulerAngle) {
      this.leftLegRotation = eulerAngle;
      this.dataTracker.set(TRACKER_LEFT_LEG_ROTATION, eulerAngle);
   }

   public void setRightLegRotation(EulerAngle eulerAngle) {
      this.rightLegRotation = eulerAngle;
      this.dataTracker.set(TRACKER_RIGHT_LEG_ROTATION, eulerAngle);
   }

   public EulerAngle getHeadRotation() {
      return this.headRotation;
   }

   public EulerAngle getBodyRotation() {
      return this.bodyRotation;
   }

   @Environment(EnvType.CLIENT)
   public EulerAngle getLeftArmRotation() {
      return this.leftArmRotation;
   }

   @Environment(EnvType.CLIENT)
   public EulerAngle getRightArmRotation() {
      return this.rightArmRotation;
   }

   @Environment(EnvType.CLIENT)
   public EulerAngle getLeftLegRotation() {
      return this.leftLegRotation;
   }

   @Environment(EnvType.CLIENT)
   public EulerAngle getRightLegRotation() {
      return this.rightLegRotation;
   }

   public boolean collides() {
      return super.collides() && !this.isMarker();
   }

   public boolean handleAttack(Entity attacker) {
      return attacker instanceof PlayerEntity && !this.world.canPlayerModifyAt((PlayerEntity)attacker, this.getBlockPos());
   }

   public Arm getMainArm() {
      return Arm.RIGHT;
   }

   protected SoundEvent getFallSound(int distance) {
      return SoundEvents.ENTITY_ARMOR_STAND_FALL;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ARMOR_STAND_HIT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ARMOR_STAND_BREAK;
   }

   public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
   }

   public boolean isAffectedBySplashPotions() {
      return false;
   }

   public void onTrackedDataSet(TrackedData<?> data) {
      if (ARMOR_STAND_FLAGS.equals(data)) {
         this.calculateDimensions();
         this.inanimate = !this.isMarker();
      }

      super.onTrackedDataSet(data);
   }

   public boolean isMobOrPlayer() {
      return false;
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return this.method_31168(this.isMarker());
   }

   private EntityDimensions method_31168(boolean bl) {
      if (bl) {
         return field_26745;
      } else {
         return this.isBaby() ? field_26746 : this.getType().getDimensions();
      }
   }

   @Environment(EnvType.CLIENT)
   public Vec3d method_31166(float tickDelta) {
      if (this.isMarker()) {
         Box box = this.method_31168(false).getBoxAt(this.getPos());
         BlockPos blockPos = this.getBlockPos();
         int i = Integer.MIN_VALUE;
         Iterator var5 = BlockPos.iterate(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ)).iterator();

         while(var5.hasNext()) {
            BlockPos blockPos2 = (BlockPos)var5.next();
            int j = Math.max(this.world.getLightLevel(LightType.BLOCK, blockPos2), this.world.getLightLevel(LightType.SKY, blockPos2));
            if (j == 15) {
               return Vec3d.ofCenter(blockPos2);
            }

            if (j > i) {
               i = j;
               blockPos = blockPos2.toImmutable();
            }
         }

         return Vec3d.ofCenter(blockPos);
      } else {
         return super.method_31166(tickDelta);
      }
   }

   static {
      field_26746 = EntityType.ARMOR_STAND.getDimensions().scaled(0.5F);
      ARMOR_STAND_FLAGS = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.BYTE);
      TRACKER_HEAD_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
      TRACKER_BODY_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
      TRACKER_LEFT_ARM_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
      TRACKER_RIGHT_ARM_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
      TRACKER_LEFT_LEG_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
      TRACKER_RIGHT_LEG_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
      RIDEABLE_MINECART_PREDICATE = (entity) -> {
         return entity instanceof AbstractMinecartEntity && ((AbstractMinecartEntity)entity).getMinecartType() == AbstractMinecartEntity.Type.RIDEABLE;
      };
   }
}
