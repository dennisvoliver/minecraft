package net.minecraft.entity.decoration;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LeashKnotEntity extends AbstractDecorationEntity {
   public LeashKnotEntity(EntityType<? extends LeashKnotEntity> entityType, World world) {
      super(entityType, world);
   }

   public LeashKnotEntity(World world, BlockPos pos) {
      super(EntityType.LEASH_KNOT, world, pos);
      this.updatePosition((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D);
      float f = 0.125F;
      float g = 0.1875F;
      float h = 0.25F;
      this.setBoundingBox(new Box(this.getX() - 0.1875D, this.getY() - 0.25D + 0.125D, this.getZ() - 0.1875D, this.getX() + 0.1875D, this.getY() + 0.25D + 0.125D, this.getZ() + 0.1875D));
      this.teleporting = true;
   }

   public void updatePosition(double x, double y, double z) {
      super.updatePosition((double)MathHelper.floor(x) + 0.5D, (double)MathHelper.floor(y) + 0.5D, (double)MathHelper.floor(z) + 0.5D);
   }

   protected void updateAttachmentPosition() {
      this.setPos((double)this.attachmentPos.getX() + 0.5D, (double)this.attachmentPos.getY() + 0.5D, (double)this.attachmentPos.getZ() + 0.5D);
   }

   public void setFacing(Direction facing) {
   }

   public int getWidthPixels() {
      return 9;
   }

   public int getHeightPixels() {
      return 9;
   }

   protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return -0.0625F;
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      return distance < 1024.0D;
   }

   public void onBreak(@Nullable Entity entity) {
      this.playSound(SoundEvents.ENTITY_LEASH_KNOT_BREAK, 1.0F, 1.0F);
   }

   public void writeCustomDataToTag(CompoundTag tag) {
   }

   public void readCustomDataFromTag(CompoundTag tag) {
   }

   public ActionResult interact(PlayerEntity player, Hand hand) {
      if (this.world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         boolean bl = false;
         double d = 7.0D;
         List<MobEntity> list = this.world.getNonSpectatingEntities(MobEntity.class, new Box(this.getX() - 7.0D, this.getY() - 7.0D, this.getZ() - 7.0D, this.getX() + 7.0D, this.getY() + 7.0D, this.getZ() + 7.0D));
         Iterator var7 = list.iterator();

         MobEntity mobEntity2;
         while(var7.hasNext()) {
            mobEntity2 = (MobEntity)var7.next();
            if (mobEntity2.getHoldingEntity() == player) {
               mobEntity2.attachLeash(this, true);
               bl = true;
            }
         }

         if (!bl) {
            this.remove();
            if (player.abilities.creativeMode) {
               var7 = list.iterator();

               while(var7.hasNext()) {
                  mobEntity2 = (MobEntity)var7.next();
                  if (mobEntity2.isLeashed() && mobEntity2.getHoldingEntity() == this) {
                     mobEntity2.detachLeash(true, false);
                  }
               }
            }
         }

         return ActionResult.CONSUME;
      }
   }

   public boolean canStayAttached() {
      return this.world.getBlockState(this.attachmentPos).getBlock().isIn(BlockTags.FENCES);
   }

   public static LeashKnotEntity getOrCreate(World world, BlockPos pos) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      List<LeashKnotEntity> list = world.getNonSpectatingEntities(LeashKnotEntity.class, new Box((double)i - 1.0D, (double)j - 1.0D, (double)k - 1.0D, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D));
      Iterator var6 = list.iterator();

      LeashKnotEntity leashKnotEntity;
      do {
         if (!var6.hasNext()) {
            LeashKnotEntity leashKnotEntity2 = new LeashKnotEntity(world, pos);
            world.spawnEntity(leashKnotEntity2);
            leashKnotEntity2.onPlace();
            return leashKnotEntity2;
         }

         leashKnotEntity = (LeashKnotEntity)var6.next();
      } while(!leashKnotEntity.getDecorationBlockPos().equals(pos));

      return leashKnotEntity;
   }

   public void onPlace() {
      this.playSound(SoundEvents.ENTITY_LEASH_KNOT_PLACE, 1.0F, 1.0F);
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this, this.getType(), 0, this.getDecorationBlockPos());
   }

   @Environment(EnvType.CLIENT)
   public Vec3d method_30951(float f) {
      return this.method_30950(f).add(0.0D, 0.2D, 0.0D);
   }
}
