package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ConduitBlockEntity extends BlockEntity implements Tickable {
   private static final Block[] ACTIVATING_BLOCKS;
   public int ticks;
   private float ticksActive;
   private boolean active;
   private boolean eyeOpen;
   private final List<BlockPos> activatingBlocks;
   @Nullable
   private LivingEntity targetEntity;
   @Nullable
   private UUID targetUuid;
   private long nextAmbientSoundTime;

   public ConduitBlockEntity() {
      this(BlockEntityType.CONDUIT);
   }

   public ConduitBlockEntity(BlockEntityType<?> blockEntityType) {
      super(blockEntityType);
      this.activatingBlocks = Lists.newArrayList();
   }

   public void fromTag(BlockState state, CompoundTag tag) {
      super.fromTag(state, tag);
      if (tag.containsUuid("Target")) {
         this.targetUuid = tag.getUuid("Target");
      } else {
         this.targetUuid = null;
      }

   }

   public CompoundTag toTag(CompoundTag tag) {
      super.toTag(tag);
      if (this.targetEntity != null) {
         tag.putUuid("Target", this.targetEntity.getUuid());
      }

      return tag;
   }

   @Nullable
   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return new BlockEntityUpdateS2CPacket(this.pos, 5, this.toInitialChunkDataTag());
   }

   public CompoundTag toInitialChunkDataTag() {
      return this.toTag(new CompoundTag());
   }

   public void tick() {
      ++this.ticks;
      long l = this.world.getTime();
      if (l % 40L == 0L) {
         this.setActive(this.updateActivatingBlocks());
         if (!this.world.isClient && this.isActive()) {
            this.givePlayersEffects();
            this.attackHostileEntity();
         }
      }

      if (l % 80L == 0L && this.isActive()) {
         this.playSound(SoundEvents.BLOCK_CONDUIT_AMBIENT);
      }

      if (l > this.nextAmbientSoundTime && this.isActive()) {
         this.nextAmbientSoundTime = l + 60L + (long)this.world.getRandom().nextInt(40);
         this.playSound(SoundEvents.BLOCK_CONDUIT_AMBIENT_SHORT);
      }

      if (this.world.isClient) {
         this.updateTargetEntity();
         this.spawnNautilusParticles();
         if (this.isActive()) {
            ++this.ticksActive;
         }
      }

   }

   private boolean updateActivatingBlocks() {
      this.activatingBlocks.clear();

      int l;
      int m;
      int n;
      for(l = -1; l <= 1; ++l) {
         for(m = -1; m <= 1; ++m) {
            for(n = -1; n <= 1; ++n) {
               BlockPos blockPos = this.pos.add(l, m, n);
               if (!this.world.isWater(blockPos)) {
                  return false;
               }
            }
         }
      }

      for(l = -2; l <= 2; ++l) {
         for(m = -2; m <= 2; ++m) {
            for(n = -2; n <= 2; ++n) {
               int o = Math.abs(l);
               int p = Math.abs(m);
               int q = Math.abs(n);
               if ((o > 1 || p > 1 || q > 1) && (l == 0 && (p == 2 || q == 2) || m == 0 && (o == 2 || q == 2) || n == 0 && (o == 2 || p == 2))) {
                  BlockPos blockPos2 = this.pos.add(l, m, n);
                  BlockState blockState = this.world.getBlockState(blockPos2);
                  Block[] var9 = ACTIVATING_BLOCKS;
                  int var10 = var9.length;

                  for(int var11 = 0; var11 < var10; ++var11) {
                     Block block = var9[var11];
                     if (blockState.isOf(block)) {
                        this.activatingBlocks.add(blockPos2);
                     }
                  }
               }
            }
         }
      }

      this.setEyeOpen(this.activatingBlocks.size() >= 42);
      return this.activatingBlocks.size() >= 16;
   }

   private void givePlayersEffects() {
      int i = this.activatingBlocks.size();
      int j = i / 7 * 16;
      int k = this.pos.getX();
      int l = this.pos.getY();
      int m = this.pos.getZ();
      Box box = (new Box((double)k, (double)l, (double)m, (double)(k + 1), (double)(l + 1), (double)(m + 1))).expand((double)j).stretch(0.0D, (double)this.world.getHeight(), 0.0D);
      List<PlayerEntity> list = this.world.getNonSpectatingEntities(PlayerEntity.class, box);
      if (!list.isEmpty()) {
         Iterator var8 = list.iterator();

         while(var8.hasNext()) {
            PlayerEntity playerEntity = (PlayerEntity)var8.next();
            if (this.pos.isWithinDistance(playerEntity.getBlockPos(), (double)j) && playerEntity.isTouchingWaterOrRain()) {
               playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, 260, 0, true, true));
            }
         }

      }
   }

   private void attackHostileEntity() {
      LivingEntity livingEntity = this.targetEntity;
      int i = this.activatingBlocks.size();
      if (i < 42) {
         this.targetEntity = null;
      } else if (this.targetEntity == null && this.targetUuid != null) {
         this.targetEntity = this.findTargetEntity();
         this.targetUuid = null;
      } else if (this.targetEntity == null) {
         List<LivingEntity> list = this.world.getEntitiesByClass(LivingEntity.class, this.getAttackZone(), (livingEntityx) -> {
            return livingEntityx instanceof Monster && livingEntityx.isTouchingWaterOrRain();
         });
         if (!list.isEmpty()) {
            this.targetEntity = (LivingEntity)list.get(this.world.random.nextInt(list.size()));
         }
      } else if (!this.targetEntity.isAlive() || !this.pos.isWithinDistance(this.targetEntity.getBlockPos(), 8.0D)) {
         this.targetEntity = null;
      }

      if (this.targetEntity != null) {
         this.world.playSound((PlayerEntity)null, this.targetEntity.getX(), this.targetEntity.getY(), this.targetEntity.getZ(), SoundEvents.BLOCK_CONDUIT_ATTACK_TARGET, SoundCategory.BLOCKS, 1.0F, 1.0F);
         this.targetEntity.damage(DamageSource.MAGIC, 4.0F);
      }

      if (livingEntity != this.targetEntity) {
         BlockState blockState = this.getCachedState();
         this.world.updateListeners(this.pos, blockState, blockState, 2);
      }

   }

   private void updateTargetEntity() {
      if (this.targetUuid == null) {
         this.targetEntity = null;
      } else if (this.targetEntity == null || !this.targetEntity.getUuid().equals(this.targetUuid)) {
         this.targetEntity = this.findTargetEntity();
         if (this.targetEntity == null) {
            this.targetUuid = null;
         }
      }

   }

   private Box getAttackZone() {
      int i = this.pos.getX();
      int j = this.pos.getY();
      int k = this.pos.getZ();
      return (new Box((double)i, (double)j, (double)k, (double)(i + 1), (double)(j + 1), (double)(k + 1))).expand(8.0D);
   }

   @Nullable
   private LivingEntity findTargetEntity() {
      List<LivingEntity> list = this.world.getEntitiesByClass(LivingEntity.class, this.getAttackZone(), (livingEntity) -> {
         return livingEntity.getUuid().equals(this.targetUuid);
      });
      return list.size() == 1 ? (LivingEntity)list.get(0) : null;
   }

   private void spawnNautilusParticles() {
      Random random = this.world.random;
      double d = (double)(MathHelper.sin((float)(this.ticks + 35) * 0.1F) / 2.0F + 0.5F);
      d = (d * d + d) * 0.30000001192092896D;
      Vec3d vec3d = new Vec3d((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 1.5D + d, (double)this.pos.getZ() + 0.5D);
      Iterator var5 = this.activatingBlocks.iterator();

      float j;
      float k;
      while(var5.hasNext()) {
         BlockPos blockPos = (BlockPos)var5.next();
         if (random.nextInt(50) == 0) {
            j = -0.5F + random.nextFloat();
            k = -2.0F + random.nextFloat();
            float h = -0.5F + random.nextFloat();
            BlockPos blockPos2 = blockPos.subtract(this.pos);
            Vec3d vec3d2 = (new Vec3d((double)j, (double)k, (double)h)).add((double)blockPos2.getX(), (double)blockPos2.getY(), (double)blockPos2.getZ());
            this.world.addParticle(ParticleTypes.NAUTILUS, vec3d.x, vec3d.y, vec3d.z, vec3d2.x, vec3d2.y, vec3d2.z);
         }
      }

      if (this.targetEntity != null) {
         Vec3d vec3d3 = new Vec3d(this.targetEntity.getX(), this.targetEntity.getEyeY(), this.targetEntity.getZ());
         float i = (-0.5F + random.nextFloat()) * (3.0F + this.targetEntity.getWidth());
         j = -1.0F + random.nextFloat() * this.targetEntity.getHeight();
         k = (-0.5F + random.nextFloat()) * (3.0F + this.targetEntity.getWidth());
         Vec3d vec3d4 = new Vec3d((double)i, (double)j, (double)k);
         this.world.addParticle(ParticleTypes.NAUTILUS, vec3d3.x, vec3d3.y, vec3d3.z, vec3d4.x, vec3d4.y, vec3d4.z);
      }

   }

   public boolean isActive() {
      return this.active;
   }

   @Environment(EnvType.CLIENT)
   public boolean isEyeOpen() {
      return this.eyeOpen;
   }

   private void setActive(boolean active) {
      if (active != this.active) {
         this.playSound(active ? SoundEvents.BLOCK_CONDUIT_ACTIVATE : SoundEvents.BLOCK_CONDUIT_DEACTIVATE);
      }

      this.active = active;
   }

   private void setEyeOpen(boolean eyeOpen) {
      this.eyeOpen = eyeOpen;
   }

   @Environment(EnvType.CLIENT)
   public float getRotation(float tickDelta) {
      return (this.ticksActive + tickDelta) * -0.0375F;
   }

   public void playSound(SoundEvent soundEvent) {
      this.world.playSound((PlayerEntity)null, this.pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
   }

   static {
      ACTIVATING_BLOCKS = new Block[]{Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE};
   }
}
