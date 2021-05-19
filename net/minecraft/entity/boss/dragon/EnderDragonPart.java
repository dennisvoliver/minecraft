package net.minecraft.entity.boss.dragon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;

public class EnderDragonPart extends Entity {
   public final EnderDragonEntity owner;
   public final String name;
   private final EntityDimensions partDimensions;

   public EnderDragonPart(EnderDragonEntity owner, String name, float width, float height) {
      super(owner.getType(), owner.world);
      this.partDimensions = EntityDimensions.changing(width, height);
      this.calculateDimensions();
      this.owner = owner;
      this.name = name;
   }

   protected void initDataTracker() {
   }

   protected void readCustomDataFromTag(CompoundTag tag) {
   }

   protected void writeCustomDataToTag(CompoundTag tag) {
   }

   public boolean collides() {
      return true;
   }

   public boolean damage(DamageSource source, float amount) {
      return this.isInvulnerableTo(source) ? false : this.owner.damagePart(this, source, amount);
   }

   public boolean isPartOf(Entity entity) {
      return this == entity || this.owner == entity;
   }

   public Packet<?> createSpawnPacket() {
      throw new UnsupportedOperationException();
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return this.partDimensions;
   }
}
