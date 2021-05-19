package net.minecraft.entity.vehicle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;
import net.minecraft.world.PortalUtil;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMinecartEntity extends Entity {
   private static final TrackedData<Integer> DAMAGE_WOBBLE_TICKS;
   private static final TrackedData<Integer> DAMAGE_WOBBLE_SIDE;
   private static final TrackedData<Float> DAMAGE_WOBBLE_STRENGTH;
   private static final TrackedData<Integer> CUSTOM_BLOCK_ID;
   private static final TrackedData<Integer> CUSTOM_BLOCK_OFFSET;
   private static final TrackedData<Boolean> CUSTOM_BLOCK_PRESENT;
   private static final ImmutableMap<EntityPose, ImmutableList<Integer>> DISMOUNT_FREE_Y_SPACES_NEEDED;
   private boolean yawFlipped;
   private static final Map<RailShape, Pair<Vec3i, Vec3i>> ADJACENT_RAIL_POSITIONS_BY_SHAPE;
   private int clientInterpolationSteps;
   private double clientX;
   private double clientY;
   private double clientZ;
   private double clientYaw;
   private double clientPitch;
   @Environment(EnvType.CLIENT)
   private double clientXVelocity;
   @Environment(EnvType.CLIENT)
   private double clientYVelocity;
   @Environment(EnvType.CLIENT)
   private double clientZVelocity;

   protected AbstractMinecartEntity(EntityType<?> entityType, World world) {
      super(entityType, world);
      this.inanimate = true;
   }

   protected AbstractMinecartEntity(EntityType<?> type, World world, double x, double y, double z) {
      this(type, world);
      this.updatePosition(x, y, z);
      this.setVelocity(Vec3d.ZERO);
      this.prevX = x;
      this.prevY = y;
      this.prevZ = z;
   }

   public static AbstractMinecartEntity create(World world, double x, double y, double z, AbstractMinecartEntity.Type type) {
      if (type == AbstractMinecartEntity.Type.CHEST) {
         return new ChestMinecartEntity(world, x, y, z);
      } else if (type == AbstractMinecartEntity.Type.FURNACE) {
         return new FurnaceMinecartEntity(world, x, y, z);
      } else if (type == AbstractMinecartEntity.Type.TNT) {
         return new TntMinecartEntity(world, x, y, z);
      } else if (type == AbstractMinecartEntity.Type.SPAWNER) {
         return new SpawnerMinecartEntity(world, x, y, z);
      } else if (type == AbstractMinecartEntity.Type.HOPPER) {
         return new HopperMinecartEntity(world, x, y, z);
      } else {
         return (AbstractMinecartEntity)(type == AbstractMinecartEntity.Type.COMMAND_BLOCK ? new CommandBlockMinecartEntity(world, x, y, z) : new MinecartEntity(world, x, y, z));
      }
   }

   protected boolean canClimb() {
      return false;
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(DAMAGE_WOBBLE_TICKS, 0);
      this.dataTracker.startTracking(DAMAGE_WOBBLE_SIDE, 1);
      this.dataTracker.startTracking(DAMAGE_WOBBLE_STRENGTH, 0.0F);
      this.dataTracker.startTracking(CUSTOM_BLOCK_ID, Block.getRawIdFromState(Blocks.AIR.getDefaultState()));
      this.dataTracker.startTracking(CUSTOM_BLOCK_OFFSET, 6);
      this.dataTracker.startTracking(CUSTOM_BLOCK_PRESENT, false);
   }

   public boolean collidesWith(Entity other) {
      return BoatEntity.method_30959(this, other);
   }

   public boolean isPushable() {
      return true;
   }

   protected Vec3d method_30633(Direction.Axis axis, PortalUtil.Rectangle rectangle) {
      return LivingEntity.method_31079(super.method_30633(axis, rectangle));
   }

   public double getMountedHeightOffset() {
      return 0.0D;
   }

   public Vec3d updatePassengerForDismount(LivingEntity passenger) {
      Direction direction = this.getMovementDirection();
      if (direction.getAxis() == Direction.Axis.Y) {
         return super.updatePassengerForDismount(passenger);
      } else {
         int[][] is = Dismounting.getDismountOffsets(direction);
         BlockPos blockPos = this.getBlockPos();
         BlockPos.Mutable mutable = new BlockPos.Mutable();
         ImmutableList<EntityPose> immutableList = passenger.getPoses();
         UnmodifiableIterator var7 = immutableList.iterator();

         while(var7.hasNext()) {
            EntityPose entityPose = (EntityPose)var7.next();
            EntityDimensions entityDimensions = passenger.getDimensions(entityPose);
            float f = Math.min(entityDimensions.width, 1.0F) / 2.0F;
            UnmodifiableIterator var11 = ((ImmutableList)DISMOUNT_FREE_Y_SPACES_NEEDED.get(entityPose)).iterator();

            while(var11.hasNext()) {
               int i = (Integer)var11.next();
               int[][] var13 = is;
               int var14 = is.length;

               for(int var15 = 0; var15 < var14; ++var15) {
                  int[] js = var13[var15];
                  mutable.set(blockPos.getX() + js[0], blockPos.getY() + i, blockPos.getZ() + js[1]);
                  double d = this.world.getDismountHeight(Dismounting.getCollisionShape(this.world, mutable), () -> {
                     return Dismounting.getCollisionShape(this.world, mutable.down());
                  });
                  if (Dismounting.canDismountInBlock(d)) {
                     Box box = new Box((double)(-f), 0.0D, (double)(-f), (double)f, (double)entityDimensions.height, (double)f);
                     Vec3d vec3d = Vec3d.ofCenter(mutable, d);
                     if (Dismounting.canPlaceEntityAt(this.world, passenger, box.offset(vec3d))) {
                        passenger.setPose(entityPose);
                        return vec3d;
                     }
                  }
               }
            }
         }

         double e = this.getBoundingBox().maxY;
         mutable.set((double)blockPos.getX(), e, (double)blockPos.getZ());
         UnmodifiableIterator var22 = immutableList.iterator();

         while(var22.hasNext()) {
            EntityPose entityPose2 = (EntityPose)var22.next();
            double g = (double)passenger.getDimensions(entityPose2).height;
            int j = MathHelper.ceil(e - (double)mutable.getY() + g);
            double h = Dismounting.getCeilingHeight(mutable, j, (blockPosx) -> {
               return this.world.getBlockState(blockPosx).getCollisionShape(this.world, blockPosx);
            });
            if (e + g <= h) {
               passenger.setPose(entityPose2);
               break;
            }
         }

         return super.updatePassengerForDismount(passenger);
      }
   }

   public boolean damage(DamageSource source, float amount) {
      if (!this.world.isClient && !this.removed) {
         if (this.isInvulnerableTo(source)) {
            return false;
         } else {
            this.setDamageWobbleSide(-this.getDamageWobbleSide());
            this.setDamageWobbleTicks(10);
            this.scheduleVelocityUpdate();
            this.setDamageWobbleStrength(this.getDamageWobbleStrength() + amount * 10.0F);
            boolean bl = source.getAttacker() instanceof PlayerEntity && ((PlayerEntity)source.getAttacker()).abilities.creativeMode;
            if (bl || this.getDamageWobbleStrength() > 40.0F) {
               this.removeAllPassengers();
               if (bl && !this.hasCustomName()) {
                  this.remove();
               } else {
                  this.dropItems(source);
               }
            }

            return true;
         }
      } else {
         return true;
      }
   }

   protected float getVelocityMultiplier() {
      BlockState blockState = this.world.getBlockState(this.getBlockPos());
      return blockState.isIn(BlockTags.RAILS) ? 1.0F : super.getVelocityMultiplier();
   }

   public void dropItems(DamageSource damageSource) {
      this.remove();
      if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
         ItemStack itemStack = new ItemStack(Items.MINECART);
         if (this.hasCustomName()) {
            itemStack.setCustomName(this.getCustomName());
         }

         this.dropStack(itemStack);
      }

   }

   @Environment(EnvType.CLIENT)
   public void animateDamage() {
      this.setDamageWobbleSide(-this.getDamageWobbleSide());
      this.setDamageWobbleTicks(10);
      this.setDamageWobbleStrength(this.getDamageWobbleStrength() + this.getDamageWobbleStrength() * 10.0F);
   }

   public boolean collides() {
      return !this.removed;
   }

   private static Pair<Vec3i, Vec3i> getAdjacentRailPositionsByShape(RailShape shape) {
      return (Pair)ADJACENT_RAIL_POSITIONS_BY_SHAPE.get(shape);
   }

   public Direction getMovementDirection() {
      return this.yawFlipped ? this.getHorizontalFacing().getOpposite().rotateYClockwise() : this.getHorizontalFacing().rotateYClockwise();
   }

   public void tick() {
      if (this.getDamageWobbleTicks() > 0) {
         this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
      }

      if (this.getDamageWobbleStrength() > 0.0F) {
         this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0F);
      }

      if (this.getY() < -64.0D) {
         this.destroy();
      }

      this.tickNetherPortal();
      if (this.world.isClient) {
         if (this.clientInterpolationSteps > 0) {
            double d = this.getX() + (this.clientX - this.getX()) / (double)this.clientInterpolationSteps;
            double e = this.getY() + (this.clientY - this.getY()) / (double)this.clientInterpolationSteps;
            double f = this.getZ() + (this.clientZ - this.getZ()) / (double)this.clientInterpolationSteps;
            double g = MathHelper.wrapDegrees(this.clientYaw - (double)this.yaw);
            this.yaw = (float)((double)this.yaw + g / (double)this.clientInterpolationSteps);
            this.pitch = (float)((double)this.pitch + (this.clientPitch - (double)this.pitch) / (double)this.clientInterpolationSteps);
            --this.clientInterpolationSteps;
            this.updatePosition(d, e, f);
            this.setRotation(this.yaw, this.pitch);
         } else {
            this.refreshPosition();
            this.setRotation(this.yaw, this.pitch);
         }

      } else {
         if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0D, -0.04D, 0.0D));
         }

         int i = MathHelper.floor(this.getX());
         int j = MathHelper.floor(this.getY());
         int k = MathHelper.floor(this.getZ());
         if (this.world.getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
            --j;
         }

         BlockPos blockPos = new BlockPos(i, j, k);
         BlockState blockState = this.world.getBlockState(blockPos);
         if (AbstractRailBlock.isRail(blockState)) {
            this.moveOnRail(blockPos, blockState);
            if (blockState.isOf(Blocks.ACTIVATOR_RAIL)) {
               this.onActivatorRail(i, j, k, (Boolean)blockState.get(PoweredRailBlock.POWERED));
            }
         } else {
            this.moveOffRail();
         }

         this.checkBlockCollision();
         this.pitch = 0.0F;
         double h = this.prevX - this.getX();
         double l = this.prevZ - this.getZ();
         if (h * h + l * l > 0.001D) {
            this.yaw = (float)(MathHelper.atan2(l, h) * 180.0D / 3.141592653589793D);
            if (this.yawFlipped) {
               this.yaw += 180.0F;
            }
         }

         double m = (double)MathHelper.wrapDegrees(this.yaw - this.prevYaw);
         if (m < -170.0D || m >= 170.0D) {
            this.yaw += 180.0F;
            this.yawFlipped = !this.yawFlipped;
         }

         this.setRotation(this.yaw, this.pitch);
         if (this.getMinecartType() == AbstractMinecartEntity.Type.RIDEABLE && squaredHorizontalLength(this.getVelocity()) > 0.01D) {
            List<Entity> list = this.world.getOtherEntities(this, this.getBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D), EntityPredicates.canBePushedBy(this));
            if (!list.isEmpty()) {
               for(int n = 0; n < list.size(); ++n) {
                  Entity entity = (Entity)list.get(n);
                  if (!(entity instanceof PlayerEntity) && !(entity instanceof IronGolemEntity) && !(entity instanceof AbstractMinecartEntity) && !this.hasPassengers() && !entity.hasVehicle()) {
                     entity.startRiding(this);
                  } else {
                     entity.pushAwayFrom(this);
                  }
               }
            }
         } else {
            Iterator var12 = this.world.getOtherEntities(this, this.getBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D)).iterator();

            while(var12.hasNext()) {
               Entity entity2 = (Entity)var12.next();
               if (!this.hasPassenger(entity2) && entity2.isPushable() && entity2 instanceof AbstractMinecartEntity) {
                  entity2.pushAwayFrom(this);
               }
            }
         }

         this.updateWaterState();
         if (this.isInLava()) {
            this.setOnFireFromLava();
            this.fallDistance *= 0.5F;
         }

         this.firstUpdate = false;
      }
   }

   protected double getMaxOffRailSpeed() {
      return 0.4D;
   }

   public void onActivatorRail(int x, int y, int z, boolean powered) {
   }

   protected void moveOffRail() {
      double d = this.getMaxOffRailSpeed();
      Vec3d vec3d = this.getVelocity();
      this.setVelocity(MathHelper.clamp(vec3d.x, -d, d), vec3d.y, MathHelper.clamp(vec3d.z, -d, d));
      if (this.onGround) {
         this.setVelocity(this.getVelocity().multiply(0.5D));
      }

      this.move(MovementType.SELF, this.getVelocity());
      if (!this.onGround) {
         this.setVelocity(this.getVelocity().multiply(0.95D));
      }

   }

   protected void moveOnRail(BlockPos pos, BlockState state) {
      this.fallDistance = 0.0F;
      double d = this.getX();
      double e = this.getY();
      double f = this.getZ();
      Vec3d vec3d = this.snapPositionToRail(d, e, f);
      e = (double)pos.getY();
      boolean bl = false;
      boolean bl2 = false;
      AbstractRailBlock abstractRailBlock = (AbstractRailBlock)state.getBlock();
      if (abstractRailBlock == Blocks.POWERED_RAIL) {
         bl = (Boolean)state.get(PoweredRailBlock.POWERED);
         bl2 = !bl;
      }

      double g = 0.0078125D;
      Vec3d vec3d2 = this.getVelocity();
      RailShape railShape = (RailShape)state.get(abstractRailBlock.getShapeProperty());
      switch(railShape) {
      case ASCENDING_EAST:
         this.setVelocity(vec3d2.add(-0.0078125D, 0.0D, 0.0D));
         ++e;
         break;
      case ASCENDING_WEST:
         this.setVelocity(vec3d2.add(0.0078125D, 0.0D, 0.0D));
         ++e;
         break;
      case ASCENDING_NORTH:
         this.setVelocity(vec3d2.add(0.0D, 0.0D, 0.0078125D));
         ++e;
         break;
      case ASCENDING_SOUTH:
         this.setVelocity(vec3d2.add(0.0D, 0.0D, -0.0078125D));
         ++e;
      }

      vec3d2 = this.getVelocity();
      Pair<Vec3i, Vec3i> pair = getAdjacentRailPositionsByShape(railShape);
      Vec3i vec3i = (Vec3i)pair.getFirst();
      Vec3i vec3i2 = (Vec3i)pair.getSecond();
      double h = (double)(vec3i2.getX() - vec3i.getX());
      double i = (double)(vec3i2.getZ() - vec3i.getZ());
      double j = Math.sqrt(h * h + i * i);
      double k = vec3d2.x * h + vec3d2.z * i;
      if (k < 0.0D) {
         h = -h;
         i = -i;
      }

      double l = Math.min(2.0D, Math.sqrt(squaredHorizontalLength(vec3d2)));
      vec3d2 = new Vec3d(l * h / j, vec3d2.y, l * i / j);
      this.setVelocity(vec3d2);
      Entity entity = this.getPassengerList().isEmpty() ? null : (Entity)this.getPassengerList().get(0);
      if (entity instanceof PlayerEntity) {
         Vec3d vec3d3 = entity.getVelocity();
         double m = squaredHorizontalLength(vec3d3);
         double n = squaredHorizontalLength(this.getVelocity());
         if (m > 1.0E-4D && n < 0.01D) {
            this.setVelocity(this.getVelocity().add(vec3d3.x * 0.1D, 0.0D, vec3d3.z * 0.1D));
            bl2 = false;
         }
      }

      double p;
      if (bl2) {
         p = Math.sqrt(squaredHorizontalLength(this.getVelocity()));
         if (p < 0.03D) {
            this.setVelocity(Vec3d.ZERO);
         } else {
            this.setVelocity(this.getVelocity().multiply(0.5D, 0.0D, 0.5D));
         }
      }

      p = (double)pos.getX() + 0.5D + (double)vec3i.getX() * 0.5D;
      double q = (double)pos.getZ() + 0.5D + (double)vec3i.getZ() * 0.5D;
      double r = (double)pos.getX() + 0.5D + (double)vec3i2.getX() * 0.5D;
      double s = (double)pos.getZ() + 0.5D + (double)vec3i2.getZ() * 0.5D;
      h = r - p;
      i = s - q;
      double x;
      double v;
      double w;
      if (h == 0.0D) {
         x = f - (double)pos.getZ();
      } else if (i == 0.0D) {
         x = d - (double)pos.getX();
      } else {
         v = d - p;
         w = f - q;
         x = (v * h + w * i) * 2.0D;
      }

      d = p + h * x;
      f = q + i * x;
      this.updatePosition(d, e, f);
      v = this.hasPassengers() ? 0.75D : 1.0D;
      w = this.getMaxOffRailSpeed();
      vec3d2 = this.getVelocity();
      this.move(MovementType.SELF, new Vec3d(MathHelper.clamp(v * vec3d2.x, -w, w), 0.0D, MathHelper.clamp(v * vec3d2.z, -w, w)));
      if (vec3i.getY() != 0 && MathHelper.floor(this.getX()) - pos.getX() == vec3i.getX() && MathHelper.floor(this.getZ()) - pos.getZ() == vec3i.getZ()) {
         this.updatePosition(this.getX(), this.getY() + (double)vec3i.getY(), this.getZ());
      } else if (vec3i2.getY() != 0 && MathHelper.floor(this.getX()) - pos.getX() == vec3i2.getX() && MathHelper.floor(this.getZ()) - pos.getZ() == vec3i2.getZ()) {
         this.updatePosition(this.getX(), this.getY() + (double)vec3i2.getY(), this.getZ());
      }

      this.applySlowdown();
      Vec3d vec3d4 = this.snapPositionToRail(this.getX(), this.getY(), this.getZ());
      Vec3d vec3d7;
      double af;
      if (vec3d4 != null && vec3d != null) {
         double aa = (vec3d.y - vec3d4.y) * 0.05D;
         vec3d7 = this.getVelocity();
         af = Math.sqrt(squaredHorizontalLength(vec3d7));
         if (af > 0.0D) {
            this.setVelocity(vec3d7.multiply((af + aa) / af, 1.0D, (af + aa) / af));
         }

         this.updatePosition(this.getX(), vec3d4.y, this.getZ());
      }

      int ac = MathHelper.floor(this.getX());
      int ad = MathHelper.floor(this.getZ());
      if (ac != pos.getX() || ad != pos.getZ()) {
         vec3d7 = this.getVelocity();
         af = Math.sqrt(squaredHorizontalLength(vec3d7));
         this.setVelocity(af * (double)(ac - pos.getX()), vec3d7.y, af * (double)(ad - pos.getZ()));
      }

      if (bl) {
         vec3d7 = this.getVelocity();
         af = Math.sqrt(squaredHorizontalLength(vec3d7));
         if (af > 0.01D) {
            double ag = 0.06D;
            this.setVelocity(vec3d7.add(vec3d7.x / af * 0.06D, 0.0D, vec3d7.z / af * 0.06D));
         } else {
            Vec3d vec3d8 = this.getVelocity();
            double ah = vec3d8.x;
            double ai = vec3d8.z;
            if (railShape == RailShape.EAST_WEST) {
               if (this.willHitBlockAt(pos.west())) {
                  ah = 0.02D;
               } else if (this.willHitBlockAt(pos.east())) {
                  ah = -0.02D;
               }
            } else {
               if (railShape != RailShape.NORTH_SOUTH) {
                  return;
               }

               if (this.willHitBlockAt(pos.north())) {
                  ai = 0.02D;
               } else if (this.willHitBlockAt(pos.south())) {
                  ai = -0.02D;
               }
            }

            this.setVelocity(ah, vec3d8.y, ai);
         }
      }

   }

   private boolean willHitBlockAt(BlockPos pos) {
      return this.world.getBlockState(pos).isSolidBlock(this.world, pos);
   }

   protected void applySlowdown() {
      double d = this.hasPassengers() ? 0.997D : 0.96D;
      this.setVelocity(this.getVelocity().multiply(d, 0.0D, d));
   }

   /**
    * This method is used to determine the minecart's render orientation, by computing a position along the rail slightly before and slightly after the minecart's actual position.
    */
   @Nullable
   @Environment(EnvType.CLIENT)
   public Vec3d snapPositionToRailWithOffset(double x, double y, double z, double offset) {
      int i = MathHelper.floor(x);
      int j = MathHelper.floor(y);
      int k = MathHelper.floor(z);
      if (this.world.getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
         --j;
      }

      BlockState blockState = this.world.getBlockState(new BlockPos(i, j, k));
      if (AbstractRailBlock.isRail(blockState)) {
         RailShape railShape = (RailShape)blockState.get(((AbstractRailBlock)blockState.getBlock()).getShapeProperty());
         y = (double)j;
         if (railShape.isAscending()) {
            y = (double)(j + 1);
         }

         Pair<Vec3i, Vec3i> pair = getAdjacentRailPositionsByShape(railShape);
         Vec3i vec3i = (Vec3i)pair.getFirst();
         Vec3i vec3i2 = (Vec3i)pair.getSecond();
         double d = (double)(vec3i2.getX() - vec3i.getX());
         double e = (double)(vec3i2.getZ() - vec3i.getZ());
         double f = Math.sqrt(d * d + e * e);
         d /= f;
         e /= f;
         x += d * offset;
         z += e * offset;
         if (vec3i.getY() != 0 && MathHelper.floor(x) - i == vec3i.getX() && MathHelper.floor(z) - k == vec3i.getZ()) {
            y += (double)vec3i.getY();
         } else if (vec3i2.getY() != 0 && MathHelper.floor(x) - i == vec3i2.getX() && MathHelper.floor(z) - k == vec3i2.getZ()) {
            y += (double)vec3i2.getY();
         }

         return this.snapPositionToRail(x, y, z);
      } else {
         return null;
      }
   }

   @Nullable
   public Vec3d snapPositionToRail(double x, double y, double z) {
      int i = MathHelper.floor(x);
      int j = MathHelper.floor(y);
      int k = MathHelper.floor(z);
      if (this.world.getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
         --j;
      }

      BlockState blockState = this.world.getBlockState(new BlockPos(i, j, k));
      if (AbstractRailBlock.isRail(blockState)) {
         RailShape railShape = (RailShape)blockState.get(((AbstractRailBlock)blockState.getBlock()).getShapeProperty());
         Pair<Vec3i, Vec3i> pair = getAdjacentRailPositionsByShape(railShape);
         Vec3i vec3i = (Vec3i)pair.getFirst();
         Vec3i vec3i2 = (Vec3i)pair.getSecond();
         double d = (double)i + 0.5D + (double)vec3i.getX() * 0.5D;
         double e = (double)j + 0.0625D + (double)vec3i.getY() * 0.5D;
         double f = (double)k + 0.5D + (double)vec3i.getZ() * 0.5D;
         double g = (double)i + 0.5D + (double)vec3i2.getX() * 0.5D;
         double h = (double)j + 0.0625D + (double)vec3i2.getY() * 0.5D;
         double l = (double)k + 0.5D + (double)vec3i2.getZ() * 0.5D;
         double m = g - d;
         double n = (h - e) * 2.0D;
         double o = l - f;
         double t;
         if (m == 0.0D) {
            t = z - (double)k;
         } else if (o == 0.0D) {
            t = x - (double)i;
         } else {
            double r = x - d;
            double s = z - f;
            t = (r * m + s * o) * 2.0D;
         }

         x = d + m * t;
         y = e + n * t;
         z = f + o * t;
         if (n < 0.0D) {
            ++y;
         } else if (n > 0.0D) {
            y += 0.5D;
         }

         return new Vec3d(x, y, z);
      } else {
         return null;
      }
   }

   @Environment(EnvType.CLIENT)
   public Box getVisibilityBoundingBox() {
      Box box = this.getBoundingBox();
      return this.hasCustomBlock() ? box.expand((double)Math.abs(this.getBlockOffset()) / 16.0D) : box;
   }

   protected void readCustomDataFromTag(CompoundTag tag) {
      if (tag.getBoolean("CustomDisplayTile")) {
         this.setCustomBlock(NbtHelper.toBlockState(tag.getCompound("DisplayState")));
         this.setCustomBlockOffset(tag.getInt("DisplayOffset"));
      }

   }

   protected void writeCustomDataToTag(CompoundTag tag) {
      if (this.hasCustomBlock()) {
         tag.putBoolean("CustomDisplayTile", true);
         tag.put("DisplayState", NbtHelper.fromBlockState(this.getContainedBlock()));
         tag.putInt("DisplayOffset", this.getBlockOffset());
      }

   }

   public void pushAwayFrom(Entity entity) {
      if (!this.world.isClient) {
         if (!entity.noClip && !this.noClip) {
            if (!this.hasPassenger(entity)) {
               double d = entity.getX() - this.getX();
               double e = entity.getZ() - this.getZ();
               double f = d * d + e * e;
               if (f >= 9.999999747378752E-5D) {
                  f = (double)MathHelper.sqrt(f);
                  d /= f;
                  e /= f;
                  double g = 1.0D / f;
                  if (g > 1.0D) {
                     g = 1.0D;
                  }

                  d *= g;
                  e *= g;
                  d *= 0.10000000149011612D;
                  e *= 0.10000000149011612D;
                  d *= (double)(1.0F - this.pushSpeedReduction);
                  e *= (double)(1.0F - this.pushSpeedReduction);
                  d *= 0.5D;
                  e *= 0.5D;
                  if (entity instanceof AbstractMinecartEntity) {
                     double h = entity.getX() - this.getX();
                     double i = entity.getZ() - this.getZ();
                     Vec3d vec3d = (new Vec3d(h, 0.0D, i)).normalize();
                     Vec3d vec3d2 = (new Vec3d((double)MathHelper.cos(this.yaw * 0.017453292F), 0.0D, (double)MathHelper.sin(this.yaw * 0.017453292F))).normalize();
                     double j = Math.abs(vec3d.dotProduct(vec3d2));
                     if (j < 0.800000011920929D) {
                        return;
                     }

                     Vec3d vec3d3 = this.getVelocity();
                     Vec3d vec3d4 = entity.getVelocity();
                     if (((AbstractMinecartEntity)entity).getMinecartType() == AbstractMinecartEntity.Type.FURNACE && this.getMinecartType() != AbstractMinecartEntity.Type.FURNACE) {
                        this.setVelocity(vec3d3.multiply(0.2D, 1.0D, 0.2D));
                        this.addVelocity(vec3d4.x - d, 0.0D, vec3d4.z - e);
                        entity.setVelocity(vec3d4.multiply(0.95D, 1.0D, 0.95D));
                     } else if (((AbstractMinecartEntity)entity).getMinecartType() != AbstractMinecartEntity.Type.FURNACE && this.getMinecartType() == AbstractMinecartEntity.Type.FURNACE) {
                        entity.setVelocity(vec3d4.multiply(0.2D, 1.0D, 0.2D));
                        entity.addVelocity(vec3d3.x + d, 0.0D, vec3d3.z + e);
                        this.setVelocity(vec3d3.multiply(0.95D, 1.0D, 0.95D));
                     } else {
                        double k = (vec3d4.x + vec3d3.x) / 2.0D;
                        double l = (vec3d4.z + vec3d3.z) / 2.0D;
                        this.setVelocity(vec3d3.multiply(0.2D, 1.0D, 0.2D));
                        this.addVelocity(k - d, 0.0D, l - e);
                        entity.setVelocity(vec3d4.multiply(0.2D, 1.0D, 0.2D));
                        entity.addVelocity(k + d, 0.0D, l + e);
                     }
                  } else {
                     this.addVelocity(-d, 0.0D, -e);
                     entity.addVelocity(d / 4.0D, 0.0D, e / 4.0D);
                  }
               }

            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
      this.clientX = x;
      this.clientY = y;
      this.clientZ = z;
      this.clientYaw = (double)yaw;
      this.clientPitch = (double)pitch;
      this.clientInterpolationSteps = interpolationSteps + 2;
      this.setVelocity(this.clientXVelocity, this.clientYVelocity, this.clientZVelocity);
   }

   @Environment(EnvType.CLIENT)
   public void setVelocityClient(double x, double y, double z) {
      this.clientXVelocity = x;
      this.clientYVelocity = y;
      this.clientZVelocity = z;
      this.setVelocity(this.clientXVelocity, this.clientYVelocity, this.clientZVelocity);
   }

   public void setDamageWobbleStrength(float f) {
      this.dataTracker.set(DAMAGE_WOBBLE_STRENGTH, f);
   }

   public float getDamageWobbleStrength() {
      return (Float)this.dataTracker.get(DAMAGE_WOBBLE_STRENGTH);
   }

   public void setDamageWobbleTicks(int wobbleTicks) {
      this.dataTracker.set(DAMAGE_WOBBLE_TICKS, wobbleTicks);
   }

   public int getDamageWobbleTicks() {
      return (Integer)this.dataTracker.get(DAMAGE_WOBBLE_TICKS);
   }

   public void setDamageWobbleSide(int wobbleSide) {
      this.dataTracker.set(DAMAGE_WOBBLE_SIDE, wobbleSide);
   }

   public int getDamageWobbleSide() {
      return (Integer)this.dataTracker.get(DAMAGE_WOBBLE_SIDE);
   }

   public abstract AbstractMinecartEntity.Type getMinecartType();

   public BlockState getContainedBlock() {
      return !this.hasCustomBlock() ? this.getDefaultContainedBlock() : Block.getStateFromRawId((Integer)this.getDataTracker().get(CUSTOM_BLOCK_ID));
   }

   public BlockState getDefaultContainedBlock() {
      return Blocks.AIR.getDefaultState();
   }

   public int getBlockOffset() {
      return !this.hasCustomBlock() ? this.getDefaultBlockOffset() : (Integer)this.getDataTracker().get(CUSTOM_BLOCK_OFFSET);
   }

   public int getDefaultBlockOffset() {
      return 6;
   }

   public void setCustomBlock(BlockState state) {
      this.getDataTracker().set(CUSTOM_BLOCK_ID, Block.getRawIdFromState(state));
      this.setCustomBlockPresent(true);
   }

   public void setCustomBlockOffset(int offset) {
      this.getDataTracker().set(CUSTOM_BLOCK_OFFSET, offset);
      this.setCustomBlockPresent(true);
   }

   public boolean hasCustomBlock() {
      return (Boolean)this.getDataTracker().get(CUSTOM_BLOCK_PRESENT);
   }

   public void setCustomBlockPresent(boolean present) {
      this.getDataTracker().set(CUSTOM_BLOCK_PRESENT, present);
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }

   static {
      DAMAGE_WOBBLE_TICKS = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.INTEGER);
      DAMAGE_WOBBLE_SIDE = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.INTEGER);
      DAMAGE_WOBBLE_STRENGTH = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.FLOAT);
      CUSTOM_BLOCK_ID = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.INTEGER);
      CUSTOM_BLOCK_OFFSET = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.INTEGER);
      CUSTOM_BLOCK_PRESENT = DataTracker.registerData(AbstractMinecartEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      DISMOUNT_FREE_Y_SPACES_NEEDED = ImmutableMap.of(EntityPose.STANDING, ImmutableList.of(0, 1, -1), EntityPose.CROUCHING, ImmutableList.of(0, 1, -1), EntityPose.SWIMMING, ImmutableList.of(0, 1));
      ADJACENT_RAIL_POSITIONS_BY_SHAPE = (Map)Util.make(Maps.newEnumMap(RailShape.class), (enumMap) -> {
         Vec3i vec3i = Direction.WEST.getVector();
         Vec3i vec3i2 = Direction.EAST.getVector();
         Vec3i vec3i3 = Direction.NORTH.getVector();
         Vec3i vec3i4 = Direction.SOUTH.getVector();
         Vec3i vec3i5 = vec3i.down();
         Vec3i vec3i6 = vec3i2.down();
         Vec3i vec3i7 = vec3i3.down();
         Vec3i vec3i8 = vec3i4.down();
         enumMap.put(RailShape.NORTH_SOUTH, Pair.of(vec3i3, vec3i4));
         enumMap.put(RailShape.EAST_WEST, Pair.of(vec3i, vec3i2));
         enumMap.put(RailShape.ASCENDING_EAST, Pair.of(vec3i5, vec3i2));
         enumMap.put(RailShape.ASCENDING_WEST, Pair.of(vec3i, vec3i6));
         enumMap.put(RailShape.ASCENDING_NORTH, Pair.of(vec3i3, vec3i8));
         enumMap.put(RailShape.ASCENDING_SOUTH, Pair.of(vec3i7, vec3i4));
         enumMap.put(RailShape.SOUTH_EAST, Pair.of(vec3i4, vec3i2));
         enumMap.put(RailShape.SOUTH_WEST, Pair.of(vec3i4, vec3i));
         enumMap.put(RailShape.NORTH_WEST, Pair.of(vec3i3, vec3i));
         enumMap.put(RailShape.NORTH_EAST, Pair.of(vec3i3, vec3i2));
      });
   }

   public static enum Type {
      RIDEABLE,
      CHEST,
      FURNACE,
      TNT,
      SPAWNER,
      HOPPER,
      COMMAND_BLOCK;
   }
}
