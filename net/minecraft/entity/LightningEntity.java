package net.minecraft.entity;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LightningEntity extends Entity {
   private int ambientTick;
   public long seed;
   private int remainingActions;
   private boolean cosmetic;
   @Nullable
   private ServerPlayerEntity channeler;

   public LightningEntity(EntityType<? extends LightningEntity> entityType, World world) {
      super(entityType, world);
      this.ignoreCameraFrustum = true;
      this.ambientTick = 2;
      this.seed = this.random.nextLong();
      this.remainingActions = this.random.nextInt(3) + 1;
   }

   public void setCosmetic(boolean cosmetic) {
      this.cosmetic = cosmetic;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.WEATHER;
   }

   public void setChanneler(@Nullable ServerPlayerEntity channeler) {
      this.channeler = channeler;
   }

   public void tick() {
      super.tick();
      if (this.ambientTick == 2) {
         Difficulty difficulty = this.world.getDifficulty();
         if (difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD) {
            this.spawnFire(4);
         }

         this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000.0F, 0.8F + this.random.nextFloat() * 0.2F);
         this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F + this.random.nextFloat() * 0.2F);
      }

      --this.ambientTick;
      if (this.ambientTick < 0) {
         if (this.remainingActions == 0) {
            this.remove();
         } else if (this.ambientTick < -this.random.nextInt(10)) {
            --this.remainingActions;
            this.ambientTick = 1;
            this.seed = this.random.nextLong();
            this.spawnFire(0);
         }
      }

      if (this.ambientTick >= 0) {
         if (!(this.world instanceof ServerWorld)) {
            this.world.setLightningTicksLeft(2);
         } else if (!this.cosmetic) {
            double d = 3.0D;
            List<Entity> list = this.world.getOtherEntities(this, new Box(this.getX() - 3.0D, this.getY() - 3.0D, this.getZ() - 3.0D, this.getX() + 3.0D, this.getY() + 6.0D + 3.0D, this.getZ() + 3.0D), Entity::isAlive);
            Iterator var4 = list.iterator();

            while(var4.hasNext()) {
               Entity entity = (Entity)var4.next();
               entity.onStruckByLightning((ServerWorld)this.world, this);
            }

            if (this.channeler != null) {
               Criteria.CHANNELED_LIGHTNING.trigger(this.channeler, list);
            }
         }
      }

   }

   private void spawnFire(int spreadAttempts) {
      if (!this.cosmetic && !this.world.isClient && this.world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
         BlockPos blockPos = this.getBlockPos();
         BlockState blockState = AbstractFireBlock.getState(this.world, blockPos);
         if (this.world.getBlockState(blockPos).isAir() && blockState.canPlaceAt(this.world, blockPos)) {
            this.world.setBlockState(blockPos, blockState);
         }

         for(int i = 0; i < spreadAttempts; ++i) {
            BlockPos blockPos2 = blockPos.add(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
            blockState = AbstractFireBlock.getState(this.world, blockPos2);
            if (this.world.getBlockState(blockPos2).isAir() && blockState.canPlaceAt(this.world, blockPos2)) {
               this.world.setBlockState(blockPos2, blockState);
            }
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      double d = 64.0D * getRenderDistanceMultiplier();
      return distance < d * d;
   }

   protected void initDataTracker() {
   }

   protected void readCustomDataFromTag(CompoundTag tag) {
   }

   protected void writeCustomDataToTag(CompoundTag tag) {
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }
}
