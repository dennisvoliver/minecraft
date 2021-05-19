package net.minecraft.entity.damage;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class DamageTracker {
   private final List<DamageRecord> recentDamage = Lists.newArrayList();
   private final LivingEntity entity;
   private int ageOnLastDamage;
   private int ageOnLastAttacked;
   private int ageOnLastUpdate;
   private boolean recentlyAttacked;
   private boolean hasDamage;
   private String fallDeathSuffix;

   public DamageTracker(LivingEntity entity) {
      this.entity = entity;
   }

   public void setFallDeathSuffix() {
      this.clearFallDeathSuffix();
      Optional<BlockPos> optional = this.entity.getClimbingPos();
      if (optional.isPresent()) {
         BlockState blockState = this.entity.world.getBlockState((BlockPos)optional.get());
         if (!blockState.isOf(Blocks.LADDER) && !blockState.isIn(BlockTags.TRAPDOORS)) {
            if (blockState.isOf(Blocks.VINE)) {
               this.fallDeathSuffix = "vines";
            } else if (!blockState.isOf(Blocks.WEEPING_VINES) && !blockState.isOf(Blocks.WEEPING_VINES_PLANT)) {
               if (!blockState.isOf(Blocks.TWISTING_VINES) && !blockState.isOf(Blocks.TWISTING_VINES_PLANT)) {
                  if (blockState.isOf(Blocks.SCAFFOLDING)) {
                     this.fallDeathSuffix = "scaffolding";
                  } else {
                     this.fallDeathSuffix = "other_climbable";
                  }
               } else {
                  this.fallDeathSuffix = "twisting_vines";
               }
            } else {
               this.fallDeathSuffix = "weeping_vines";
            }
         } else {
            this.fallDeathSuffix = "ladder";
         }
      } else if (this.entity.isTouchingWater()) {
         this.fallDeathSuffix = "water";
      }

   }

   public void onDamage(DamageSource damageSource, float originalHealth, float f) {
      this.update();
      this.setFallDeathSuffix();
      DamageRecord damageRecord = new DamageRecord(damageSource, this.entity.age, originalHealth, f, this.fallDeathSuffix, this.entity.fallDistance);
      this.recentDamage.add(damageRecord);
      this.ageOnLastDamage = this.entity.age;
      this.hasDamage = true;
      if (damageRecord.isAttackerLiving() && !this.recentlyAttacked && this.entity.isAlive()) {
         this.recentlyAttacked = true;
         this.ageOnLastAttacked = this.entity.age;
         this.ageOnLastUpdate = this.ageOnLastAttacked;
         this.entity.enterCombat();
      }

   }

   public Text getDeathMessage() {
      if (this.recentDamage.isEmpty()) {
         return new TranslatableText("death.attack.generic", new Object[]{this.entity.getDisplayName()});
      } else {
         DamageRecord damageRecord = this.getBiggestFall();
         DamageRecord damageRecord2 = (DamageRecord)this.recentDamage.get(this.recentDamage.size() - 1);
         Text text = damageRecord2.getAttackerName();
         Entity entity = damageRecord2.getDamageSource().getAttacker();
         Object text9;
         if (damageRecord != null && damageRecord2.getDamageSource() == DamageSource.FALL) {
            Text text2 = damageRecord.getAttackerName();
            if (damageRecord.getDamageSource() != DamageSource.FALL && damageRecord.getDamageSource() != DamageSource.OUT_OF_WORLD) {
               if (text2 == null || text != null && text2.equals(text)) {
                  if (text != null) {
                     ItemStack itemStack2 = entity instanceof LivingEntity ? ((LivingEntity)entity).getMainHandStack() : ItemStack.EMPTY;
                     if (!itemStack2.isEmpty() && itemStack2.hasCustomName()) {
                        text9 = new TranslatableText("death.fell.finish.item", new Object[]{this.entity.getDisplayName(), text, itemStack2.toHoverableText()});
                     } else {
                        text9 = new TranslatableText("death.fell.finish", new Object[]{this.entity.getDisplayName(), text});
                     }
                  } else {
                     text9 = new TranslatableText("death.fell.killer", new Object[]{this.entity.getDisplayName()});
                  }
               } else {
                  Entity entity2 = damageRecord.getDamageSource().getAttacker();
                  ItemStack itemStack = entity2 instanceof LivingEntity ? ((LivingEntity)entity2).getMainHandStack() : ItemStack.EMPTY;
                  if (!itemStack.isEmpty() && itemStack.hasCustomName()) {
                     text9 = new TranslatableText("death.fell.assist.item", new Object[]{this.entity.getDisplayName(), text2, itemStack.toHoverableText()});
                  } else {
                     text9 = new TranslatableText("death.fell.assist", new Object[]{this.entity.getDisplayName(), text2});
                  }
               }
            } else {
               text9 = new TranslatableText("death.fell.accident." + this.getFallDeathSuffix(damageRecord), new Object[]{this.entity.getDisplayName()});
            }
         } else {
            text9 = damageRecord2.getDamageSource().getDeathMessage(this.entity);
         }

         return (Text)text9;
      }
   }

   @Nullable
   public LivingEntity getBiggestAttacker() {
      LivingEntity livingEntity = null;
      PlayerEntity playerEntity = null;
      float f = 0.0F;
      float g = 0.0F;
      Iterator var5 = this.recentDamage.iterator();

      while(true) {
         DamageRecord damageRecord;
         do {
            do {
               if (!var5.hasNext()) {
                  if (playerEntity != null && g >= f / 3.0F) {
                     return playerEntity;
                  }

                  return livingEntity;
               }

               damageRecord = (DamageRecord)var5.next();
               if (damageRecord.getDamageSource().getAttacker() instanceof PlayerEntity && (playerEntity == null || damageRecord.getDamage() > g)) {
                  g = damageRecord.getDamage();
                  playerEntity = (PlayerEntity)damageRecord.getDamageSource().getAttacker();
               }
            } while(!(damageRecord.getDamageSource().getAttacker() instanceof LivingEntity));
         } while(livingEntity != null && !(damageRecord.getDamage() > f));

         f = damageRecord.getDamage();
         livingEntity = (LivingEntity)damageRecord.getDamageSource().getAttacker();
      }
   }

   @Nullable
   private DamageRecord getBiggestFall() {
      DamageRecord damageRecord = null;
      DamageRecord damageRecord2 = null;
      float f = 0.0F;
      float g = 0.0F;

      for(int i = 0; i < this.recentDamage.size(); ++i) {
         DamageRecord damageRecord3 = (DamageRecord)this.recentDamage.get(i);
         DamageRecord damageRecord4 = i > 0 ? (DamageRecord)this.recentDamage.get(i - 1) : null;
         if ((damageRecord3.getDamageSource() == DamageSource.FALL || damageRecord3.getDamageSource() == DamageSource.OUT_OF_WORLD) && damageRecord3.getFallDistance() > 0.0F && (damageRecord == null || damageRecord3.getFallDistance() > g)) {
            if (i > 0) {
               damageRecord = damageRecord4;
            } else {
               damageRecord = damageRecord3;
            }

            g = damageRecord3.getFallDistance();
         }

         if (damageRecord3.getFallDeathSuffix() != null && (damageRecord2 == null || damageRecord3.getDamage() > f)) {
            damageRecord2 = damageRecord3;
            f = damageRecord3.getDamage();
         }
      }

      if (g > 5.0F && damageRecord != null) {
         return damageRecord;
      } else if (f > 5.0F && damageRecord2 != null) {
         return damageRecord2;
      } else {
         return null;
      }
   }

   private String getFallDeathSuffix(DamageRecord damageRecord) {
      return damageRecord.getFallDeathSuffix() == null ? "generic" : damageRecord.getFallDeathSuffix();
   }

   public int getTimeSinceLastAttack() {
      return this.recentlyAttacked ? this.entity.age - this.ageOnLastAttacked : this.ageOnLastUpdate - this.ageOnLastAttacked;
   }

   private void clearFallDeathSuffix() {
      this.fallDeathSuffix = null;
   }

   public void update() {
      int i = this.recentlyAttacked ? 300 : 100;
      if (this.hasDamage && (!this.entity.isAlive() || this.entity.age - this.ageOnLastDamage > i)) {
         boolean bl = this.recentlyAttacked;
         this.hasDamage = false;
         this.recentlyAttacked = false;
         this.ageOnLastUpdate = this.entity.age;
         if (bl) {
            this.entity.endCombat();
         }

         this.recentDamage.clear();
      }

   }

   public LivingEntity getEntity() {
      return this.entity;
   }
}
