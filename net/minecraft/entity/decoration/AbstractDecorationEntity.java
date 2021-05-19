package net.minecraft.entity.decoration;

import java.util.function.Predicate;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractDecorationEntity extends Entity {
   protected static final Predicate<Entity> PREDICATE = (entity) -> {
      return entity instanceof AbstractDecorationEntity;
   };
   private int obstructionCheckCounter;
   protected BlockPos attachmentPos;
   protected Direction facing;

   protected AbstractDecorationEntity(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
      super(entityType, world);
      this.facing = Direction.SOUTH;
   }

   protected AbstractDecorationEntity(EntityType<? extends AbstractDecorationEntity> type, World world, BlockPos pos) {
      this(type, world);
      this.attachmentPos = pos;
   }

   protected void initDataTracker() {
   }

   protected void setFacing(Direction facing) {
      Validate.notNull(facing);
      Validate.isTrue(facing.getAxis().isHorizontal());
      this.facing = facing;
      this.yaw = (float)(this.facing.getHorizontal() * 90);
      this.prevYaw = this.yaw;
      this.updateAttachmentPosition();
   }

   protected void updateAttachmentPosition() {
      if (this.facing != null) {
         double d = (double)this.attachmentPos.getX() + 0.5D;
         double e = (double)this.attachmentPos.getY() + 0.5D;
         double f = (double)this.attachmentPos.getZ() + 0.5D;
         double g = 0.46875D;
         double h = this.method_6893(this.getWidthPixels());
         double i = this.method_6893(this.getHeightPixels());
         d -= (double)this.facing.getOffsetX() * 0.46875D;
         f -= (double)this.facing.getOffsetZ() * 0.46875D;
         e += i;
         Direction direction = this.facing.rotateYCounterclockwise();
         d += h * (double)direction.getOffsetX();
         f += h * (double)direction.getOffsetZ();
         this.setPos(d, e, f);
         double j = (double)this.getWidthPixels();
         double k = (double)this.getHeightPixels();
         double l = (double)this.getWidthPixels();
         if (this.facing.getAxis() == Direction.Axis.Z) {
            l = 1.0D;
         } else {
            j = 1.0D;
         }

         j /= 32.0D;
         k /= 32.0D;
         l /= 32.0D;
         this.setBoundingBox(new Box(d - j, e - k, f - l, d + j, e + k, f + l));
      }
   }

   private double method_6893(int i) {
      return i % 32 == 0 ? 0.5D : 0.0D;
   }

   public void tick() {
      if (!this.world.isClient) {
         if (this.getY() < -64.0D) {
            this.destroy();
         }

         if (this.obstructionCheckCounter++ == 100) {
            this.obstructionCheckCounter = 0;
            if (!this.removed && !this.canStayAttached()) {
               this.remove();
               this.onBreak((Entity)null);
            }
         }
      }

   }

   public boolean canStayAttached() {
      if (!this.world.isSpaceEmpty(this)) {
         return false;
      } else {
         int i = Math.max(1, this.getWidthPixels() / 16);
         int j = Math.max(1, this.getHeightPixels() / 16);
         BlockPos blockPos = this.attachmentPos.offset(this.facing.getOpposite());
         Direction direction = this.facing.rotateYCounterclockwise();
         BlockPos.Mutable mutable = new BlockPos.Mutable();

         for(int k = 0; k < i; ++k) {
            for(int l = 0; l < j; ++l) {
               int m = (i - 1) / -2;
               int n = (j - 1) / -2;
               mutable.set(blockPos).move(direction, k + m).move(Direction.UP, l + n);
               BlockState blockState = this.world.getBlockState(mutable);
               if (!blockState.getMaterial().isSolid() && !AbstractRedstoneGateBlock.isRedstoneGate(blockState)) {
                  return false;
               }
            }
         }

         return this.world.getOtherEntities(this, this.getBoundingBox(), PREDICATE).isEmpty();
      }
   }

   public boolean collides() {
      return true;
   }

   public boolean handleAttack(Entity attacker) {
      if (attacker instanceof PlayerEntity) {
         PlayerEntity playerEntity = (PlayerEntity)attacker;
         return !this.world.canPlayerModifyAt(playerEntity, this.attachmentPos) ? true : this.damage(DamageSource.player(playerEntity), 0.0F);
      } else {
         return false;
      }
   }

   public Direction getHorizontalFacing() {
      return this.facing;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         if (!this.removed && !this.world.isClient) {
            this.remove();
            this.scheduleVelocityUpdate();
            this.onBreak(source.getAttacker());
         }

         return true;
      }
   }

   public void move(MovementType type, Vec3d movement) {
      if (!this.world.isClient && !this.removed && movement.lengthSquared() > 0.0D) {
         this.remove();
         this.onBreak((Entity)null);
      }

   }

   public void addVelocity(double deltaX, double deltaY, double deltaZ) {
      if (!this.world.isClient && !this.removed && deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 0.0D) {
         this.remove();
         this.onBreak((Entity)null);
      }

   }

   public void writeCustomDataToTag(CompoundTag tag) {
      BlockPos blockPos = this.getDecorationBlockPos();
      tag.putInt("TileX", blockPos.getX());
      tag.putInt("TileY", blockPos.getY());
      tag.putInt("TileZ", blockPos.getZ());
   }

   public void readCustomDataFromTag(CompoundTag tag) {
      this.attachmentPos = new BlockPos(tag.getInt("TileX"), tag.getInt("TileY"), tag.getInt("TileZ"));
   }

   public abstract int getWidthPixels();

   public abstract int getHeightPixels();

   public abstract void onBreak(@Nullable Entity entity);

   public abstract void onPlace();

   public ItemEntity dropStack(ItemStack stack, float yOffset) {
      ItemEntity itemEntity = new ItemEntity(this.world, this.getX() + (double)((float)this.facing.getOffsetX() * 0.15F), this.getY() + (double)yOffset, this.getZ() + (double)((float)this.facing.getOffsetZ() * 0.15F), stack);
      itemEntity.setToDefaultPickupDelay();
      this.world.spawnEntity(itemEntity);
      return itemEntity;
   }

   protected boolean shouldSetPositionOnLoad() {
      return false;
   }

   public void updatePosition(double x, double y, double z) {
      this.attachmentPos = new BlockPos(x, y, z);
      this.updateAttachmentPosition();
      this.velocityDirty = true;
   }

   public BlockPos getDecorationBlockPos() {
      return this.attachmentPos;
   }

   public float applyRotation(BlockRotation rotation) {
      if (this.facing.getAxis() != Direction.Axis.Y) {
         switch(rotation) {
         case CLOCKWISE_180:
            this.facing = this.facing.getOpposite();
            break;
         case COUNTERCLOCKWISE_90:
            this.facing = this.facing.rotateYCounterclockwise();
            break;
         case CLOCKWISE_90:
            this.facing = this.facing.rotateYClockwise();
         }
      }

      float f = MathHelper.wrapDegrees(this.yaw);
      switch(rotation) {
      case CLOCKWISE_180:
         return f + 180.0F;
      case COUNTERCLOCKWISE_90:
         return f + 90.0F;
      case CLOCKWISE_90:
         return f + 270.0F;
      default:
         return f;
      }
   }

   public float applyMirror(BlockMirror mirror) {
      return this.applyRotation(mirror.getRotation(this.facing));
   }

   public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
   }

   public void calculateDimensions() {
   }
}
